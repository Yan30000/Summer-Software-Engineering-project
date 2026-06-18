/* Simple single-file frontend for SEMP prototype */
const api = {
  json: (path, opts = {}) => {

    const token = localStorage.getItem('semp_token');

    const headers = {
      'Content-Type': 'application/json'
    };

    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    return fetch(path, {
      ...opts,
      headers
    })
        .then(r =>
            r.json()
                .catch(() => ({}))
                .then(body => ({
                  status: r.status,
                  body
                }))
        );
  }
};

const content = document.getElementById('content');
const sidebar = document.getElementById('sidebar');
const navList = document.getElementById('nav-list');
const logoutBtn = document.getElementById('logoutBtn');

logoutBtn.addEventListener('click', () => {
  localStorage.removeItem('semp_token');
  localStorage.removeItem('semp_user');
  sidebar.classList.add('d-none');
  showRegistration();
});

function showRegistration(){
  sidebar.classList.add('d-none');
  content.innerHTML = `
    <div class="row justify-content-center align-items-center vh-100">
      <div class="col-md-6">
        <div class="card p-4 card-compact">
          <h4>Create account</h4>
          <input id="regEmail" class="form-control my-2" placeholder="Email">
          <input id="regPass" type="password" class="form-control my-2" placeholder="Password">
          <select id="regRole" class="form-select my-2"><option>Player</option><option>Coach</option></select>
          <div class="d-flex gap-2">
            <button id="doRegister" class="btn btn-primary">Register</button>
            <button id="toLogin" class="btn btn-outline-secondary">Already have account</button>
          </div>
        </div>
      </div>
    </div>`;

  document.getElementById('toLogin').onclick = showLogin;
  document.getElementById('doRegister').onclick = () => {

    const emailField = document.getElementById('regEmail');
    const passField = document.getElementById('regPass');
    const roleField = document.getElementById('regRole');

    const email = emailField ? emailField.value.trim() : '';
    const password = passField ? passField.value : '';
    const role = roleField ? roleField.value : 'Player';

    console.log("EMAIL:", email);
    console.log("PASSWORD:", password);
    console.log("ROLE:", role);

    if (!email || !password) {
      alert('Email + password required');
      return;
    }

    console.log("SENDING:");

    console.log(JSON.stringify({
      email,
      password,
      role
    }));

    api.json('/api/register', {
      method: 'POST',
      body: JSON.stringify({
        email,
        password,
        role
      })
    })
        .then(res => {
          console.log(res);

          if (res.status === 200 && res.body.success) {
            alert('Registered — please log in');
            showLogin();
          } else {
            alert(res.body.message || 'Registration error');
          }
        });
  };
}

function showLogin(){
  sidebar.classList.add('d-none');
  content.innerHTML = `
    <div class="row justify-content-center align-items-center vh-100">
      <div class="col-md-6">
        <div class="card p-4 card-compact">
          <h4>Sign in</h4>
          <input id="loginEmail" class="form-control my-2" placeholder="Email">
          <input id="loginPass" type="password" class="form-control my-2" placeholder="Password">
          <div class="d-flex gap-2">
            <button id="doLogin" class="btn btn-primary">Sign in</button>
            <button id="toReg" class="btn btn-outline-secondary">Create account</button>
          </div>
        </div>
      </div>
    </div>`;

  document.getElementById('toReg').onclick = showRegistration;
  document.getElementById('doLogin').onclick = () => {
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPass').value;
    if (!email || !password) return alert('Email + password required');
    api.json('/api/login', { method: 'POST', body: JSON.stringify({email,password}) })
      .then(res => {
        if (res.status === 200 && res.body.success) {
          localStorage.setItem('semp_token', res.body.token);
          localStorage.setItem('semp_user', JSON.stringify(res.body.user));
          initSidebar();
          showDashboard();
        } else {
          alert(res.body.message || 'Login failed');
        }
      });
  };
}

function initSidebar(){
  sidebar.classList.remove('d-none');
  navList.innerHTML = '';
  const items = [
    { id: 'dash', label: 'Dashboard', fn: showDashboard },
    { id: 'events', label: 'Events', fn: showEvents },
    { id: 'teams', label: 'Teams', fn: showTeams },
    { id: 'profile', label: 'Profile', fn: showProfile }
  ];
  items.forEach(it => {
    const li = document.createElement('li'); li.className = 'nav-item';
    const a = document.createElement('a'); a.href = '#'; a.className = 'nav-link text-white'; a.textContent = it.label;
    a.onclick = (e) => { e.preventDefault(); it.fn(); };
    li.appendChild(a); navList.appendChild(li);
  });
}

function showDashboard(){

  content.innerHTML = `

  <h2 class="mb-4">Dashboard</h2>

  <div class="row mb-4">

      <div class="col-md-4">
          <div class="card stat-card">
              <div class="stat-number" id="totalEvents">0</div>
              <div>Total Events</div>
          </div>
      </div>

      <div class="col-md-4">
          <div class="card stat-card">
              <div class="stat-number" id="registeredEvents">0</div>
              <div>My Registrations</div>
          </div>
      </div>

      <div class="col-md-4">
          <div class="card stat-card">
              <div class="stat-number" id="totalTeams">0</div>
              <div>Teams</div>
          </div>
      </div>

  </div>

  <h4 class="mb-3">Recent Events</h4>

  <div id="cards" class="row g-3"></div>
  `;

  api.json('/api/events').then(res => {

    const events = res.body || [];

    document.getElementById('totalEvents').textContent =
        events.length;

    const cards = document.getElementById('cards');

    cards.innerHTML = '';

    events.forEach(ev => {

      const col = document.createElement('div');

      col.className = 'col-md-4';

      col.innerHTML = `
              <div class="card p-3 card-compact event-card">
                  <div class="card-body">

                      <div class="event-title">
                          ${ev.title}
                      </div>

                      <div class="small-muted">
                          ${ev.date}
                      </div>

                      <div class="mt-3">
                          <button class="btn btn-sm btn-register">
                              View
                          </button>
                      </div>

                  </div>
              </div>
          `;

      cards.appendChild(col);
    });

  });

  api.json('/api/teams').then(res => {

    document.getElementById('totalTeams').textContent =
        (res.body || []).length;

  });

  api.json('/api/profile').then(res => {

    const registrations =
        res.body?.registrations || [];

    document.getElementById('registeredEvents').textContent =
        registrations.length;

  });
}

function loadDashboardStats(){

  api.json('/api/events')
      .then(res => {

        document.getElementById('totalEvents').textContent =
            res.body.length;

        document.getElementById('recentEvents').innerHTML =
            res.body.map(e => `
                <div class="card p-3 mb-2 event-card">
                    <h5>${e.title}</h5>
                    <small>${e.date}</small>
                </div>
                `).join('');
      });

  api.json('/api/teams')
      .then(res => {
        document.getElementById('totalTeams')
            .textContent = res.body.length;
      });

  api.json('/api/profile')
      .then(res => {
        document.getElementById('registeredEvents')
            .textContent =
            (res.body.registrations || []).length;
      });
}

function showEvents() {

  const user =
      JSON.parse(localStorage.getItem('semp_user'));

  content.innerHTML = `

    <div class="d-flex justify-content-between align-items-center mb-4">

        <h2>Events</h2>

    </div>

    <div id="coachCreateArea"></div>

    <div id="eventList"></div>

    `;

  if (user && user.role === "Coach") {

    document.getElementById('coachCreateArea').innerHTML = `

        <div class="card p-4 mb-4">

            <h4>Create Event</h4>

            <input
                id="eventTitle"
                class="form-control mb-2"
                placeholder="Event Name">

            <input
                id="eventDate"
                type="date"
                class="form-control mb-2">

            <button
                id="createEventBtn"
                class="btn btn-primary">
                Create Event
            </button>

        </div>
        `;

    document.getElementById('createEventBtn').onclick = () => {

      const title =
          document.getElementById('eventTitle').value;

      const date =
          document.getElementById('eventDate').value;

      if (!title || !date) {

        alert('Please enter title and date');

        return;
      }

      api.json('/api/events', {
        method: 'POST',
        body: JSON.stringify({
          title,
          date
        })
      })
          .then(() => {

            alert('Event created');

            showEvents();
          });
    };
  }

  api.json('/api/events')
      .then(res => {

        const events = res.body || [];

        const container =
            document.getElementById('eventList');

        container.innerHTML = '';

        events.forEach(ev => {

          container.innerHTML += `

                <div class="card p-3 mb-3 event-card">

                    <h5>${ev.title}</h5>

                    <small class="text-muted">
                        ${ev.date}
                    </small>

                    <div class="mt-3">

                        <button
                            class="btn btn-outline-primary">

                            View Event

                        </button>

                    </div>

                </div>

                `;
        });

      });
}

function showTeams() {

  content.innerHTML = `

    <div class="d-flex justify-content-between align-items-center mb-4">

        <h2>Teams</h2>

        <button
            id="createTeamBtn"
            class="btn btn-success">

            Create Team

        </button>

    </div>

    <div id="teamList"></div>

    `;

  document.getElementById('createTeamBtn').onclick = () => {

    const name = prompt("Enter Team Name");

    if (!name) return;

    api.json('/api/teams', {
      method:'POST',
      body: JSON.stringify({
        name:name
      })
    })
        .then(() => {

          showTeams();

        });
  };

  api.json('/api/teams')
      .then(res => {

        const teams = res.body || [];

        const list =
            document.getElementById('teamList');

        list.innerHTML = '';

        teams.forEach(team => {

          list.innerHTML += `

                <div class="card p-3 mb-3">

                    <h5>${team.name}</h5>

                    <small>

                        Team ID:
                        ${team.id || 'N/A'}

                    </small>

                </div>

                `;

        });

      });
}

function showProfile() {

  const user =
      JSON.parse(localStorage.getItem('semp_user'));

  content.innerHTML = `

    <div class="row justify-content-center">

        <div class="col-md-8">

            <div class="card p-4">

                <div class="text-center">

                    <h2>

                        ${user.email}

                    </h2>

                    <span class="badge bg-primary">

                        ${user.role}

                    </span>

                </div>

                <hr>

                <div class="row">

                    <div class="col-md-4">

                        <div class="card stat-card">

                            <div
                                id="profileEvents"
                                class="stat-number">

                                0

                            </div>

                            <div>

                                Registrations

                            </div>

                        </div>

                    </div>

                    <div class="col-md-4">

                        <div class="card stat-card">

                            <div
                                id="profileTeams"
                                class="stat-number">

                                0

                            </div>

                            <div>

                                Teams

                            </div>

                        </div>

                    </div>

                    <div class="col-md-4">

                        <div class="card stat-card">

                            <div
                                id="profileRole"
                                class="stat-number">

                                1

                            </div>

                            <div>

                                Active Role

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    </div>

    `;

  api.json('/api/profile')
      .then(res => {

        const profile =
            res.body || {};

        document.getElementById('profileEvents')
            .textContent =
            (profile.registrations || []).length;

        document.getElementById('profileTeams')
            .textContent =
            (profile.teams || []).length;

      });
}

function showFormations() {

  content.innerHTML = `

    <h2 class="mb-4">

        Formation Builder

    </h2>

    <div class="card p-4">

        <label class="mb-2">

            Select Formation

        </label>

        <select
            id="formationSelect"
            class="form-select mb-4">

            <option>4-4-2</option>

            <option>4-3-3</option>

            <option>3-5-2</option>

            <option>5-3-2</option>

        </select>

        <div
            id="formationPreview"
            class="text-center">

        </div>

    </div>

    `;

  const select =
      document.getElementById('formationSelect');

  const preview =
      document.getElementById('formationPreview');

  function renderFormation() {

    preview.innerHTML = `

        <h3>

            ${select.value}

        </h3>

        <div class="mt-3">

            Formation Preview

        </div>

        `;

  }

  select.onchange = renderFormation;

  renderFormation();
}
// Start: always show registration first (blueprint requirement). If already logged in, go to dashboard
if (localStorage.getItem('semp_token')) {
  initSidebar();
  showDashboard();
} else {
  showRegistration();
}