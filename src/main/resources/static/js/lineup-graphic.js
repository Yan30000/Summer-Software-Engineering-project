document.addEventListener("DOMContentLoaded", () => {
    setupFormationFilter();
    renderAllLineupBoards();
});

function setupFormationFilter() {
    const sportSelect = document.getElementById("sportType");
    const formationSelect = document.getElementById("formationCode");
    const maxPlayersInput = document.getElementById("maxPlayers");
    const formationInfo = document.getElementById("formationInfo");

    if (!sportSelect || !formationSelect) {
        return;
    }

    function refreshFormationOptions() {
        const sport = sportSelect.value;
        let firstVisible = null;

        Array.from(formationSelect.options).forEach(option => {
            if (!option.value) {
                option.hidden = false;
                return;
            }

            const optionSport = option.dataset.sport;
            const shouldShow = !sport || optionSport === sport;

            option.hidden = !shouldShow;

            if (shouldShow && firstVisible === null) {
                firstVisible = option;
            }
        });

        if (formationSelect.selectedOptions.length > 0 && formationSelect.selectedOptions[0].hidden) {
            formationSelect.value = "";
        }

        if (!formationSelect.value && firstVisible) {
            formationSelect.value = firstVisible.value;
        }

        updateFormationDetails();
    }

    function updateFormationDetails() {
        const selected = formationSelect.selectedOptions[0];

        if (!selected || !selected.value) {
            if (formationInfo) {
                formationInfo.innerText = "Select a sport and formation to see the recommended active players.";
            }

            return;
        }

        const players = selected.dataset.players;
        const description = selected.dataset.description;

        if (maxPlayersInput) {
            maxPlayersInput.value = players;
        }

        if (formationInfo) {
            formationInfo.innerText = selected.text + " uses " + players + " active player(s). " + description;
        }
    }

    sportSelect.addEventListener("change", refreshFormationOptions);
    formationSelect.addEventListener("change", updateFormationDetails);

    refreshFormationOptions();
}

function renderAllLineupBoards() {
    const previews = document.querySelectorAll(".lineup-preview-wrapper");

    previews.forEach(preview => {
        renderOneLineupBoard(preview);
    });
}

function renderOneLineupBoard(preview) {
    const boardTarget = preview.querySelector(".lineupBoard");
    const lineupList = preview.querySelector(".lineupList");
    const benchList = preview.querySelector(".benchList");

    const sport = normalize(preview.dataset.sport || "");
    const formationCode = preview.dataset.formationCode || "";
    const formationName = preview.dataset.formationName || "Custom";
    const teamName = preview.dataset.team || "SEPS Team";

    const players = extractPlayers(preview);

    const starters = players.filter(player => isStarter(player));
    const substitutes = players.filter(player => isSubstitute(player));

    const slots = buildSlots(sport, formationCode, formationName, starters);

    renderBoard(boardTarget, sport, teamName, formationName, slots);
    renderStartingList(lineupList, slots);
    renderBenchList(benchList, substitutes);
}

function extractPlayers(preview) {
    const rows = preview.querySelectorAll(".lineup-data-row");

    return Array.from(rows).map((row, index) => {
        return {
            number: index + 1,
            name: row.dataset.playerName || "Player " + (index + 1),
            email: row.dataset.playerEmail || "",
            requested: row.dataset.requested || "",
            assigned: row.dataset.assigned || "",
            status: normalize(row.dataset.status || "")
        };
    });
}

function isStarter(player) {
    const status = player.status;
    const assigned = normalize(player.assigned);

    return (
        status.includes("selected") ||
        status.includes("starter") ||
        status.includes("starting") ||
        status.includes("active")
    ) && !assigned.includes("substitute");
}

function isSubstitute(player) {
    const status = player.status;
    const assigned = normalize(player.assigned);

    return (
        status.includes("substitute") ||
        assigned.includes("substitute") ||
        assigned.includes("bench") ||
        assigned.includes("reserve")
    );
}

function buildSlots(sport, formationCode, formationName, starters) {
    const labelRows = getLabelRows(sport, formationCode, formationName, starters.length);
    const coordinates = buildCoordinates(labelRows);
    const slots = [];

    for (let i = 0; i < coordinates.length; i++) {
        const matchedPlayer = findPlayerForSlot(starters, coordinates[i].label, slots);

        slots.push({
            index: i + 1,
            label: coordinates[i].label,
            x: coordinates[i].x,
            y: coordinates[i].y,
            player: matchedPlayer || null
        });
    }

    return slots;
}

function findPlayerForSlot(players, label, existingSlots) {
    const usedNames = existingSlots
        .filter(slot => slot.player)
        .map(slot => slot.player.name);

    const exact = players.find(player =>
        !usedNames.includes(player.name) &&
        normalize(player.assigned) === normalize(label)
    );

    if (exact) {
        return exact;
    }

    return players.find(player => !usedNames.includes(player.name));
}

function getLabelRows(sport, formationCode, formationName, starterCount) {
    const code = formationCode.toUpperCase();

    const map = {
        SOCCER_442: [
            ["GK"],
            ["LB", "LCB", "RCB", "RB"],
            ["LM", "LCM", "RCM", "RM"],
            ["LS", "RS"]
        ],

        SOCCER_433: [
            ["GK"],
            ["LB", "LCB", "RCB", "RB"],
            ["LCM", "CM", "RCM"],
            ["LW", "ST", "RW"]
        ],

        SOCCER_352: [
            ["GK"],
            ["LCB", "CB", "RCB"],
            ["LWB", "LCM", "CAM", "RCM", "RWB"],
            ["LS", "RS"]
        ],

        SOCCER_4231: [
            ["GK"],
            ["LB", "LCB", "RCB", "RB"],
            ["LDM", "RDM"],
            ["LAM", "CAM", "RAM"],
            ["ST"]
        ],

        SOCCER_321: [
            ["GK"],
            ["LD", "CD", "RD"],
            ["LM", "RM"],
            ["ST"]
        ],

        SOCCER_5V5: [
            ["GK"],
            ["LD", "RD"],
            ["CM"],
            ["ST"]
        ],

        BASKETBALL_STANDARD: [
            ["PG", "SG"],
            ["C"],
            ["SF", "PF"]
        ],

        BASKETBALL_212: [
            ["PG", "SG"],
            ["C"],
            ["SF", "PF"]
        ],

        BASKETBALL_122: [
            ["PG"],
            ["SG", "SF"],
            ["PF", "C"]
        ],

        BASKETBALL_131: [
            ["Top"],
            ["LW", "Middle", "RW"],
            ["Back"]
        ],

        BASKETBALL_3V3: [
            ["Guard"],
            ["Wing", "Post"]
        ],

        VOLLEYBALL_51: [
            ["Zone 5", "Zone 6", "Zone 1"],
            ["Zone 4", "Zone 3", "Zone 2"]
        ],

        VOLLEYBALL_62: [
            ["Zone 5", "Zone 6", "Zone 1"],
            ["Zone 4", "Zone 3", "Zone 2"]
        ],

        VOLLEYBALL_42: [
            ["Zone 5", "Zone 6", "Zone 1"],
            ["Zone 4", "Zone 3", "Zone 2"]
        ],

        VOLLEYBALL_BASIC: [
            ["Zone 5", "Zone 6", "Zone 1"],
            ["Zone 4", "Zone 3", "Zone 2"]
        ],

        VOLLEYBALL_BEACH: [
            ["Left Side", "Right Side"]
        ],

        HOCKEY_123: [
            ["Goalie"],
            ["Left Defense", "Right Defense"],
            ["Left Wing", "Center", "Right Wing"]
        ],

        HOCKEY_1212: [
            ["Goalie"],
            ["Left Defense", "Right Defense"],
            ["Center"],
            ["Left Wing", "Right Wing"]
        ],

        HOCKEY_1221: [
            ["Goalie"],
            ["Left Defense", "Right Defense"],
            ["Left Mid/Wing", "Right Mid/Wing"],
            ["Forward"]
        ],

        HOCKEY_FIELD_3331: [
            ["Goalkeeper"],
            ["Left Back", "Center Back", "Right Back"],
            ["Left Mid", "Center Mid", "Right Mid"],
            ["Left Forward", "Center Forward", "Right Forward"],
            ["Striker"]
        ],

        TENNIS_SINGLES: [
            ["Singles Player"]
        ],

        TENNIS_DOUBLES_STANDARD: [
            ["Baseline Player"],
            ["Net Player"]
        ],

        TENNIS_DOUBLES_TWO_BACK: [
            ["Left Baseline", "Right Baseline"]
        ],

        TENNIS_DOUBLES_TWO_UP: [
            ["Left Net", "Right Net"]
        ],

        TENNIS_I_FORMATION: [
            ["Server"],
            ["Net Player"]
        ]
    };

    if (map[code]) {
        return map[code];
    }

    return buildGenericRows(starterCount);
}

function buildGenericRows(count) {
    if (count <= 1) {
        return [["P1"]];
    }

    if (count === 2) {
        return [["P1"], ["P2"]];
    }

    if (count === 3) {
        return [["P1"], ["P2", "P3"]];
    }

    if (count === 4) {
        return [["P1", "P2"], ["P3", "P4"]];
    }

    if (count === 5) {
        return [["P1"], ["P2", "P3"], ["P4", "P5"]];
    }

    if (count === 6) {
        return [["P1"], ["P2", "P3"], ["P4", "P5", "P6"]];
    }

    if (count === 7) {
        return [["P1"], ["P2", "P3", "P4"], ["P5", "P6", "P7"]];
    }

    return [
        ["P1"],
        ["P2", "P3", "P4"],
        ["P5", "P6", "P7"],
        ["P8", "P9", "P10", "P11"]
    ];
}

function buildCoordinates(labelRows) {
    const slots = [];
    const rowCount = labelRows.length;
    const yPositions = distribute(rowCount, 88, 14);

    labelRows.forEach((rowLabels, rowIndex) => {
        const xPositions = distribute(rowLabels.length, 18, 82);

        rowLabels.forEach((label, colIndex) => {
            slots.push({
                label: label,
                x: xPositions[colIndex],
                y: yPositions[rowIndex]
            });
        });
    });

    return slots;
}

function distribute(count, start, end) {
    if (count <= 1) {
        return [(start + end) / 2];
    }

    const values = [];
    const step = (end - start) / (count - 1);

    for (let i = 0; i < count; i++) {
        values.push(start + step * i);
    }

    return values;
}

function renderBoard(container, sport, teamName, formationName, slots) {
    if (!container) {
        return;
    }

    const sportClass = getSportClass(sport);
    const startersCount = slots.filter(slot => slot.player).length;

    container.innerHTML = `
        <div class="lineup-board-shell">
            <div class="board-top">
                <div>
                    <h3 class="board-title">${escapeHtml(teamName)}</h3>
                    <div class="board-subtitle">
                        ${capitalize(sport)} • Formation: ${escapeHtml(formationName)}
                    </div>
                </div>
                <div class="board-badge">${startersCount} Starter${startersCount === 1 ? "" : "s"}</div>
            </div>

            <div class="board-surface ${sportClass}">
                ${renderSurfaceMarkings(sportClass)}
                ${slots.map(renderSlot).join("")}
            </div>
        </div>
    `;
}

function renderSurfaceMarkings(sportClass) {
    if (sportClass === "soccer") {
        return `
            <div class="field-markings">
                <div class="half-line"></div>
                <div class="center-circle"></div>
                <div class="center-dot"></div>
                <div class="penalty-box top"></div>
                <div class="penalty-box bottom"></div>
                <div class="goal-box top"></div>
                <div class="goal-box bottom"></div>
            </div>
        `;
    }

    if (sportClass === "tennis") {
        return `
            <div class="field-markings">
                <div class="outer-rect"></div>
                <div class="net-line"></div>
                <div class="service-line top"></div>
                <div class="service-line bottom"></div>
                <div class="center-service top"></div>
                <div class="center-service bottom"></div>
            </div>
        `;
    }

    if (sportClass === "basketball") {
        return `
            <div class="court-markings">
                <div class="center-line"></div>
                <div class="center-circle"></div>
                <div class="paint top"></div>
                <div class="paint bottom"></div>
            </div>
        `;
    }

    if (sportClass === "volleyball") {
        return `
            <div class="court-markings">
                <div class="outer-rect"></div>
                <div class="net-line"></div>
                <div class="attack-line top"></div>
                <div class="attack-line bottom"></div>
            </div>
        `;
    }

    if (sportClass === "hockey") {
        return `
            <div class="court-markings">
                <div class="rink"></div>
                <div class="center-line"></div>
                <div class="blue-line top"></div>
                <div class="blue-line bottom"></div>
                <div class="center-circle"></div>
            </div>
        `;
    }

    return "";
}

function renderSlot(slot) {
    const player = slot.player;
    const name = player ? player.name : "Open Slot";
    const role = player && player.assigned && player.assigned !== "Player"
        ? player.assigned
        : slot.label;

    return `
        <div class="lineup-token ${player ? "" : "placeholder"}"
             style="left:${slot.x}%; top:${slot.y}%;">
            <div class="token-number">${String(slot.index).padStart(2, "0")}</div>
            <div class="token-name">${escapeHtml(name)}</div>
            <div class="token-role">${escapeHtml(role)}</div>
        </div>
    `;
}

function renderStartingList(container, slots) {
    if (!container) {
        return;
    }

    const filled = slots.filter(slot => slot.player);

    if (!filled.length) {
        container.innerHTML = `<li class="side-muted">No active lineup selected yet.</li>`;
        return;
    }

    container.innerHTML = filled.map(slot => {
        const role = slot.player.assigned && slot.player.assigned !== "Player"
            ? slot.player.assigned
            : slot.label;

        return `
            <li>
                <strong>${escapeHtml(slot.player.name)}</strong>
                <span class="player-tag">${escapeHtml(role)}</span>
            </li>
        `;
    }).join("");
}

function renderBenchList(container, benchPlayers) {
    if (!container) {
        return;
    }

    if (!benchPlayers.length) {
        container.innerHTML = `<li class="side-muted">No substitutes assigned.</li>`;
        return;
    }

    container.innerHTML = benchPlayers
        .map(player => `<li>${escapeHtml(player.name)} <span class="player-tag">Substitute</span></li>`)
        .join("");
}

function getSportClass(sport) {
    if (sport.includes("soccer")) {
        return "soccer";
    }

    if (sport.includes("tennis")) {
        return "tennis";
    }

    if (sport.includes("basketball")) {
        return "basketball";
    }

    if (sport.includes("volleyball")) {
        return "volleyball";
    }

    if (sport.includes("hockey")) {
        return "hockey";
    }

    return "soccer";
}

function downloadLineupCard() {
    const preview = document.querySelector(".lineup-preview-wrapper");

    if (!preview) {
        alert("No lineup preview available.");
        return;
    }

    const html = `
<!DOCTYPE html>
<html>
<head>
    <title>SEPS Lineup Card</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            padding: 30px;
            background: #f4f7fb;
            color: #16233b;
        }

        .card {
            background: white;
            padding: 25px;
            border-radius: 16px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.08);
        }

        .lineup-board-shell {
            background: #f8fbff;
            border-radius: 16px;
            padding: 20px;
        }
    </style>
</head>
<body>
    <div class="card">
        <h1>SEPS Final Lineup Card</h1>
        ${preview.outerHTML}
    </div>
</body>
</html>
`;

    const blob = new Blob([html], { type: "text/html" });
    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.download = "SEPS-Lineup-Card.html";
    link.click();

    URL.revokeObjectURL(url);
}

function copyLineupSummary() {
    const lines = [];
    const preview = document.querySelector(".lineup-preview-wrapper");

    if (!preview) {
        alert("No lineup available to copy.");
        return;
    }

    lines.push("SEPS FINAL LINEUP SUMMARY");
    lines.push("--------------------------------");

    preview.querySelectorAll(".lineup-data-row").forEach(row => {
        const name = row.dataset.playerName || "";
        const assigned = row.dataset.assigned || "";
        const status = row.dataset.status || "";

        lines.push(name + " - " + assigned + " (" + status + ")");
    });

    navigator.clipboard.writeText(lines.join("\n"))
        .then(() => alert("Lineup summary copied."))
        .catch(() => alert("Could not copy lineup summary."));
}

function normalize(value) {
    return String(value || "").trim().toLowerCase();
}

function capitalize(value) {
    if (!value) {
        return "Sport";
    }

    return value.charAt(0).toUpperCase() + value.slice(1);
}

function escapeHtml(value) {
    return String(value || "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}