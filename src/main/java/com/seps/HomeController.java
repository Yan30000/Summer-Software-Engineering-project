package com.seps;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    private static final List<UserAccount> users = new ArrayList<>();
    private static final List<SportEventWeb> events = new ArrayList<>();
    private static final List<FormationOption> formationOptions = buildFormationOptions();

    private static int nextUserId = 1;
    private static int nextEventId = 1;
    private static int nextRegistrationId = 1;

    // Simple demo login state.
    // For this class project, this is okay for demonstration.
    // A real production system would use Spring Security sessions.
    private static UserAccount currentUser = null;

    // =========================
    // HOME
    // =========================

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("eventCount", events.size());
        model.addAttribute("userCount", users.size());
        model.addAttribute("currentUser", currentUser);
        return "index";
    }

    // =========================
    // ACCOUNT PAGES
    // =========================

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("currentUser", currentUser);
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam("name") String name,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("role") String role,
                                 Model model) {

        for (UserAccount user : users) {
            if (user.getEmail().equalsIgnoreCase(email.trim())) {
                model.addAttribute("error", "An account with this email already exists.");
                model.addAttribute("currentUser", currentUser);
                return "register";
            }
        }

        UserAccount user = new UserAccount(nextUserId, name.trim(), email.trim(), password, role);
        users.add(user);
        nextUserId++;

        model.addAttribute("success", "Account created successfully. Please log in.");
        model.addAttribute("currentUser", currentUser);
        return "login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("currentUser", currentUser);
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              Model model) {

        for (UserAccount user : users) {
            if (user.getEmail().equalsIgnoreCase(email.trim()) && user.getPassword().equals(password)) {
                currentUser = user;

                if (user.isCoach()) {
                    return "redirect:/coach";
                }

                return "redirect:/player";
            }
        }

        model.addAttribute("error", "Invalid email or password. Please try again.");
        model.addAttribute("currentUser", currentUser);
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        currentUser = null;
        return "redirect:/";
    }

    // =========================
    // COACH PAGES
    // =========================

    @GetMapping("/coach")
    public String coachDashboard(Model model) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        List<SportEventWeb> coachEvents = getEventsForCoach(currentUser.getEmail());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("events", coachEvents);
        model.addAttribute("totalEvents", coachEvents.size());
        model.addAttribute("totalRegistrations", getTotalRegistrationsForCoach(currentUser.getEmail()));
        model.addAttribute("finalizedEvents", getFinalizedEventCountForCoach(currentUser.getEmail()));
        model.addAttribute("publishedEvents", getPublishedEventCountForCoach(currentUser.getEmail()));

        return "coach";
    }

    @GetMapping("/coach/create-event")
    public String createEventPage(Model model) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("formationOptions", formationOptions);
        return "create-event";
    }

    @PostMapping("/coach/create-event")
    public String createEventSubmit(@RequestParam("eventName") String eventName,
                                    @RequestParam("sportType") String sportType,
                                    @RequestParam("eventDate") String eventDate,
                                    @RequestParam("registrationDeadLine") String registrationDeadLine,
                                    @RequestParam("teamName") String teamName,
                                    @RequestParam("formationCode") String formationCode,
                                    @RequestParam(value = "maxPlayers", defaultValue = "0") int maxPlayers,
                                    Model model) {

        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        LocalDate eventLocalDate = LocalDate.parse(eventDate);
        LocalDate deadlineLocalDate = LocalDate.parse(registrationDeadLine);

        if (deadlineLocalDate.isAfter(eventLocalDate)) {
            model.addAttribute("error", "Registration deadline cannot be after the event date.");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("formationOptions", formationOptions);
            return "create-event";
        }

        FormationOption selectedFormation = findFormationByCode(formationCode);

        if (selectedFormation == null) {
            model.addAttribute("error", "Please select a valid formation.");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("formationOptions", formationOptions);
            return "create-event";
        }

        int activePlayers = maxPlayers > 0 ? maxPlayers : selectedFormation.getActivePlayers();

        SportEventWeb event = new SportEventWeb(
                nextEventId,
                eventName,
                sportType,
                eventDate,
                registrationDeadLine,
                teamName,
                activePlayers,
                selectedFormation.getCode(),
                selectedFormation.getName(),
                selectedFormation.getDescription(),
                currentUser.getName(),
                currentUser.getEmail()
        );

        events.add(event);
        nextEventId++;

        return "redirect:/coach/event-created/" + event.getEventId();
    }

    @GetMapping("/coach/event-created/{eventId}")
    public String eventCreatedPage(@PathVariable int eventId, Model model) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event == null) {
            return "redirect:/coach";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        return "event-created";
    }

    @GetMapping("/coach/event/{eventId}")
    public String manageEvent(@PathVariable int eventId, Model model) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event == null || !event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())) {
            return "redirect:/coach";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        model.addAttribute("formationOptions", formationOptions);
        model.addAttribute("positionOptions", buildPositions(event.getFormationCode()));

        return "coach-event";
    }

    @PostMapping("/coach/event/{eventId}/close-registration")
    public String closeRegistration(@PathVariable int eventId) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())) {
            event.setRegistrationClosed(true);
            event.setStatus("Registration Closed");
        }

        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/open-registration")
    public String openRegistration(@PathVariable int eventId) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail()) && !event.isFinalized()) {
            event.setRegistrationClosed(false);
            event.setStatus("Registration Open");
        }

        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/modify-formation")
    public String modifyFormation(@PathVariable int eventId,
                                  @RequestParam("formationCode") String formationCode,
                                  @RequestParam(value = "maxPlayers", defaultValue = "0") int maxPlayers) {

        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);
        FormationOption selectedFormation = findFormationByCode(formationCode);

        if (event != null
                && selectedFormation != null
                && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())
                && !event.isFinalized()) {

            event.setFormationCode(selectedFormation.getCode());
            event.setFormationName(selectedFormation.getName());
            event.setFormationDescription(selectedFormation.getDescription());
            event.setMaxPlayers(maxPlayers > 0 ? maxPlayers : selectedFormation.getActivePlayers());

            for (PlayerRegistration registration : event.getRegistrations()) {
                registration.setAssignedPosition("Not assigned yet");
                registration.setSelectionStatus("Registered");
                registration.setSubstitute(false);
            }
        }

        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/assign/{registrationId}")
    public String assignPlayer(@PathVariable int eventId,
                               @PathVariable int registrationId,
                               @RequestParam("assignedPosition") String assignedPosition,
                               @RequestParam("lineupRole") String lineupRole) {

        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);
        PlayerRegistration registration = findRegistrationById(event, registrationId);

        if (event != null
                && registration != null
                && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())
                && !event.isFinalized()) {

            registration.setAssignedPosition(assignedPosition);

            if (lineupRole.equals("SUBSTITUTE")) {
                registration.setSubstitute(true);
                registration.setSelectionStatus("Substitute");
            } else {
                registration.setSubstitute(false);
                registration.setSelectionStatus("Selected");
            }
        }

        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/auto-assign")
    public String autoAssignLineup(@PathVariable int eventId) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event != null
                && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())
                && !event.isFinalized()) {
            autoAssignPlayers(event);
        }

        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/finalize")
    public String finalizeLineup(@PathVariable int eventId) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event == null || !event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())) {
            return "redirect:/coach";
        }

        autoAssignPlayers(event);

        event.setFinalized(true);
        event.setRegistrationClosed(true);
        event.setStatus("Lineup Finalized");

        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/publish")
    public String publishLineup(@PathVariable int eventId) {
        if (!isLoggedInCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event != null
                && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())
                && event.isFinalized()) {

            event.setPublished(true);
            event.setStatus("Lineup Published");
        }

        return "redirect:/coach/event/" + eventId;
    }

    // =========================
    // PLAYER PAGES
    // =========================

    @GetMapping("/player")
    public String playerDashboard(@RequestParam(value = "coachName", required = false) String coachName,
                                  Model model) {

        if (!isLoggedInPlayer()) {
            return "redirect:/login";
        }

        List<SportEventWeb> visibleEvents = new ArrayList<>();

        for (SportEventWeb event : events) {
            if (coachName == null || coachName.trim().isEmpty()) {
                visibleEvents.add(event);
            } else if (event.getCoachName().toLowerCase().contains(coachName.trim().toLowerCase())) {
                visibleEvents.add(event);
            }
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("events", visibleEvents);
        model.addAttribute("coachName", coachName);
        return "player";
    }

    @GetMapping("/player/event/{eventId}")
    public String eventDetails(@PathVariable int eventId, Model model) {
        if (!isLoggedInPlayer()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event == null) {
            return "redirect:/player";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        model.addAttribute("canRegister", event.isRegistrationAvailable());
        return "event-details";
    }

    @GetMapping("/player/register/{eventId}")
    public String registerForEventPage(@PathVariable int eventId, Model model) {
        if (!isLoggedInPlayer()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event == null) {
            return "redirect:/player";
        }

        if (!event.isRegistrationAvailable()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("event", event);
            return "registration-closed";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        model.addAttribute("positionOptions", buildPositions(event.getFormationCode()));
        return "register-event";
    }

    @PostMapping("/player/register")
    public String registerPlayer(@RequestParam("eventId") int eventId,
                                 @RequestParam("requestedPosition") String requestedPosition,
                                 Model model) {

        if (!isLoggedInPlayer()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event == null) {
            return "redirect:/player";
        }

        if (!event.isRegistrationAvailable()) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("event", event);
            return "registration-closed";
        }

        PlayerRegistration existingRegistration = findRegistrationByEmail(event, currentUser.getEmail());

        if (existingRegistration != null) {
            return "redirect:/player/registration-success/" + existingRegistration.getRegistrationId();
        }

        PlayerRegistration registration = new PlayerRegistration(
                nextRegistrationId,
                currentUser.getName(),
                currentUser.getEmail(),
                requestedPosition
        );

        event.getRegistrations().add(registration);
        nextRegistrationId++;

        return "redirect:/player/registration-success/" + registration.getRegistrationId();
    }

    @GetMapping("/player/registration-success/{registrationId}")
    public String registrationSuccess(@PathVariable int registrationId, Model model) {
        if (!isLoggedInPlayer()) {
            return "redirect:/login";
        }

        RegistrationSearchResult result = findRegistrationByIdAcrossEvents(registrationId);

        if (result == null) {
            return "redirect:/player";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", result.getEvent());
        model.addAttribute("registration", result.getRegistration());
        return "registration-success";
    }

    @PostMapping("/player/cancel-registration/{registrationId}")
    public String cancelRegistration(@PathVariable int registrationId) {
        if (!isLoggedInPlayer()) {
            return "redirect:/login";
        }

        RegistrationSearchResult result = findRegistrationByIdAcrossEvents(registrationId);

        if (result != null) {
            SportEventWeb event = result.getEvent();

            if (!event.isFinalized()) {
                event.getRegistrations().remove(result.getRegistration());
            }
        }

        return "redirect:/player";
    }

    @GetMapping("/player/lineup/{eventId}")
    public String viewLineup(@PathVariable int eventId, Model model) {
        if (!isLoggedInPlayer()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);

        if (event == null) {
            return "redirect:/player";
        }

        PlayerRegistration myRegistration = findRegistrationByEmail(event, currentUser.getEmail());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        model.addAttribute("myRegistration", myRegistration);
        return "player-lineup";
    }

    // =========================
    // HELPER METHODS
    // =========================

    private boolean isLoggedInCoach() {
        return currentUser != null && currentUser.isCoach();
    }

    private boolean isLoggedInPlayer() {
        return currentUser != null && currentUser.isPlayer();
    }

    private SportEventWeb findEventById(int eventId) {
        for (SportEventWeb event : events) {
            if (event.getEventId() == eventId) {
                return event;
            }
        }

        return null;
    }

    private FormationOption findFormationByCode(String formationCode) {
        for (FormationOption formation : formationOptions) {
            if (formation.getCode().equalsIgnoreCase(formationCode)) {
                return formation;
            }
        }

        return null;
    }

    private PlayerRegistration findRegistrationById(SportEventWeb event, int registrationId) {
        if (event == null) {
            return null;
        }

        for (PlayerRegistration registration : event.getRegistrations()) {
            if (registration.getRegistrationId() == registrationId) {
                return registration;
            }
        }

        return null;
    }

    private PlayerRegistration findRegistrationByEmail(SportEventWeb event, String email) {
        if (event == null) {
            return null;
        }

        for (PlayerRegistration registration : event.getRegistrations()) {
            if (registration.getEmail().equalsIgnoreCase(email)) {
                return registration;
            }
        }

        return null;
    }

    private RegistrationSearchResult findRegistrationByIdAcrossEvents(int registrationId) {
        for (SportEventWeb event : events) {
            for (PlayerRegistration registration : event.getRegistrations()) {
                if (registration.getRegistrationId() == registrationId) {
                    return new RegistrationSearchResult(event, registration);
                }
            }
        }

        return null;
    }

    private List<SportEventWeb> getEventsForCoach(String coachEmail) {
        List<SportEventWeb> coachEvents = new ArrayList<>();

        for (SportEventWeb event : events) {
            if (event.getCoachEmail().equalsIgnoreCase(coachEmail)) {
                coachEvents.add(event);
            }
        }

        return coachEvents;
    }

    private int getTotalRegistrationsForCoach(String coachEmail) {
        int total = 0;

        for (SportEventWeb event : events) {
            if (event.getCoachEmail().equalsIgnoreCase(coachEmail)) {
                total += event.getRegistrations().size();
            }
        }

        return total;
    }

    private int getFinalizedEventCountForCoach(String coachEmail) {
        int total = 0;

        for (SportEventWeb event : events) {
            if (event.getCoachEmail().equalsIgnoreCase(coachEmail) && event.isFinalized()) {
                total++;
            }
        }

        return total;
    }

    private int getPublishedEventCountForCoach(String coachEmail) {
        int total = 0;

        for (SportEventWeb event : events) {
            if (event.getCoachEmail().equalsIgnoreCase(coachEmail) && event.isPublished()) {
                total++;
            }
        }

        return total;
    }

    private List<String> buildPositions(String formationCode) {
        FormationOption formation = findFormationByCode(formationCode);

        if (formation == null) {
            return Arrays.asList("Player");
        }

        return formation.getPositions();
    }

    private void autoAssignPlayers(SportEventWeb event) {
        List<String> positions = buildPositions(event.getFormationCode());
        int activeCount = 0;

        for (PlayerRegistration registration : event.getRegistrations()) {
            if (registration.getSelectionStatus().equals("Selected")) {
                activeCount++;
            }
        }

        for (PlayerRegistration registration : event.getRegistrations()) {

            if (registration.getSelectionStatus().equals("Selected")
                    || registration.getSelectionStatus().equals("Substitute")) {
                continue;
            }

            if (activeCount < event.getMaxPlayers()) {
                String assignedPosition;

                if (activeCount < positions.size()) {
                    assignedPosition = positions.get(activeCount);
                } else {
                    assignedPosition = "Player";
                }

                registration.setAssignedPosition(assignedPosition);
                registration.setSelectionStatus("Selected");
                registration.setSubstitute(false);
                activeCount++;
            } else {
                registration.setAssignedPosition("Substitute");
                registration.setSelectionStatus("Substitute");
                registration.setSubstitute(true);
            }
        }
    }

    private static List<FormationOption> buildFormationOptions() {
        List<FormationOption> options = new ArrayList<>();

        // Soccer formations
        options.add(new FormationOption("SOCCER_442", "Soccer", "4-4-2", 11,
                "Balanced soccer formation with four defenders, four midfielders, and two forwards.",
                Arrays.asList("GK", "LB", "LCB", "RCB", "RB", "LM", "LCM", "RCM", "RM", "LS", "RS")));

        options.add(new FormationOption("SOCCER_433", "Soccer", "4-3-3", 11,
                "Attacking soccer formation with a front three and three midfielders.",
                Arrays.asList("GK", "LB", "LCB", "RCB", "RB", "LCM", "CM", "RCM", "LW", "ST", "RW")));

        options.add(new FormationOption("SOCCER_352", "Soccer", "3-5-2", 11,
                "Midfield-heavy soccer formation using three center backs, wing backs, and two forwards.",
                Arrays.asList("GK", "LCB", "CB", "RCB", "LWB", "LCM", "CAM", "RCM", "RWB", "LS", "RS")));

        options.add(new FormationOption("SOCCER_4231", "Soccer", "4-2-3-1", 11,
                "Modern soccer shape with two defensive midfielders, three attacking midfielders, and one striker.",
                Arrays.asList("GK", "LB", "LCB", "RCB", "RB", "LDM", "RDM", "LAM", "CAM", "RAM", "ST")));

        options.add(new FormationOption("SOCCER_321", "Soccer", "3-2-1", 7,
                "Small-sided soccer formation with goalkeeper, three defenders, two midfielders, and one striker.",
                Arrays.asList("GK", "LD", "CD", "RD", "LM", "RM", "ST")));

        options.add(new FormationOption("SOCCER_5V5", "Soccer", "5v5", 5,
                "Small-sided soccer formation for five active players.",
                Arrays.asList("GK", "LD", "RD", "CM", "ST")));

        // Basketball formations
        options.add(new FormationOption("BASKETBALL_STANDARD", "Basketball", "Standard 5", 5,
                "Standard basketball lineup using point guard, shooting guard, small forward, power forward, and center.",
                Arrays.asList("PG", "SG", "SF", "PF", "C")));

        options.add(new FormationOption("BASKETBALL_212", "Basketball", "2-1-2", 5,
                "Basketball zone-style shape with two guards, one center, and two forwards.",
                Arrays.asList("PG", "SG", "C", "SF", "PF")));

        options.add(new FormationOption("BASKETBALL_122", "Basketball", "1-2-2", 5,
                "Basketball shape with one top guard, two wings, and two inside players.",
                Arrays.asList("PG", "SG", "SF", "PF", "C")));

        options.add(new FormationOption("BASKETBALL_131", "Basketball", "1-3-1", 5,
                "Basketball shape with one top player, three middle players, and one back player.",
                Arrays.asList("Top", "LW", "Middle", "RW", "Back")));

        options.add(new FormationOption("BASKETBALL_3V3", "Basketball", "3v3", 3,
                "Small-sided basketball lineup for three active players.",
                Arrays.asList("Guard", "Wing", "Post")));

        // Volleyball formations
        options.add(new FormationOption("VOLLEYBALL_51", "Volleyball", "5-1", 6,
                "Volleyball system with one setter and five attacking options across six court zones.",
                Arrays.asList("Setter", "Outside Hitter", "Middle Blocker", "Opposite Hitter", "Libero", "Outside Hitter")));

        options.add(new FormationOption("VOLLEYBALL_62", "Volleyball", "6-2", 6,
                "Volleyball system using two setters who can also support attacking rotations.",
                Arrays.asList("Setter", "Outside Hitter", "Middle Blocker", "Setter/Opposite", "Libero", "Outside Hitter")));

        options.add(new FormationOption("VOLLEYBALL_42", "Volleyball", "4-2", 6,
                "Volleyball system with four hitters and two setters.",
                Arrays.asList("Setter", "Outside Hitter", "Middle Blocker", "Setter", "Defensive Specialist", "Opposite Hitter")));

        options.add(new FormationOption("VOLLEYBALL_BASIC", "Volleyball", "Basic 6 Zones", 6,
                "Basic six-zone volleyball layout for recreational events.",
                Arrays.asList("Zone 1", "Zone 2", "Zone 3", "Zone 4", "Zone 5", "Zone 6")));

        options.add(new FormationOption("VOLLEYBALL_BEACH", "Volleyball", "Beach Doubles", 2,
                "Two-player volleyball formation for beach or small-sided play.",
                Arrays.asList("Left Side", "Right Side")));

        // Hockey formations
        options.add(new FormationOption("HOCKEY_123", "Hockey", "1-2-3", 6,
                "Ice hockey-style six-player lineup with goalie, two defensemen, and three forwards.",
                Arrays.asList("Goalie", "Left Defense", "Right Defense", "Left Wing", "Center", "Right Wing")));

        options.add(new FormationOption("HOCKEY_1212", "Hockey", "1-2-1-2", 6,
                "Hockey lineup with goalie, two defensemen, one center, and two wings.",
                Arrays.asList("Goalie", "Left Defense", "Right Defense", "Center", "Left Wing", "Right Wing")));

        options.add(new FormationOption("HOCKEY_1221", "Hockey", "1-2-2-1", 6,
                "Hockey lineup with goalie, two defenders, two midfield/wing players, and one forward.",
                Arrays.asList("Goalie", "Left Defense", "Right Defense", "Left Mid/Wing", "Right Mid/Wing", "Forward")));

        options.add(new FormationOption("HOCKEY_FIELD_3331", "Hockey", "Field 3-3-3-1", 11,
                "Field hockey-style lineup with goalkeeper, defense, midfield, forwards, and striker.",
                Arrays.asList("Goalkeeper", "Left Back", "Center Back", "Right Back", "Left Mid", "Center Mid", "Right Mid", "Left Forward", "Center Forward", "Right Forward", "Striker")));

        // Tennis formations
        options.add(new FormationOption("TENNIS_SINGLES", "Tennis", "Singles", 1,
                "One-player tennis lineup for singles play.",
                Arrays.asList("Singles Player")));

        options.add(new FormationOption("TENNIS_DOUBLES_STANDARD", "Tennis", "Doubles Standard", 2,
                "Two-player tennis doubles lineup with baseline and net responsibilities.",
                Arrays.asList("Baseline Player", "Net Player")));

        options.add(new FormationOption("TENNIS_DOUBLES_TWO_BACK", "Tennis", "Doubles Two Back", 2,
                "Defensive doubles setup with both players covering from the baseline.",
                Arrays.asList("Left Baseline", "Right Baseline")));

        options.add(new FormationOption("TENNIS_DOUBLES_TWO_UP", "Tennis", "Doubles Two Up", 2,
                "Aggressive doubles setup with both players positioned near the net.",
                Arrays.asList("Left Net", "Right Net")));

        options.add(new FormationOption("TENNIS_I_FORMATION", "Tennis", "Doubles I Formation", 2,
                "Doubles setup using a server and a net player near the center line.",
                Arrays.asList("Server", "Net Player")));

        return options;
    }

    // =========================
    // DATA CLASSES
    // =========================

    public static class UserAccount {
        private int userId;
        private String name;
        private String email;
        private String password;
        private String role;

        public UserAccount(int userId, String name, String email, String password, String role) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
        }

        public int getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public String getRole() {
            return role;
        }

        public boolean isCoach() {
            return role.equalsIgnoreCase("COACH");
        }

        public boolean isPlayer() {
            return role.equalsIgnoreCase("PLAYER");
        }
    }

    public static class FormationOption {
        private String code;
        private String sport;
        private String name;
        private int activePlayers;
        private String description;
        private List<String> positions;

        public FormationOption(String code, String sport, String name, int activePlayers,
                               String description, List<String> positions) {
            this.code = code;
            this.sport = sport;
            this.name = name;
            this.activePlayers = activePlayers;
            this.description = description;
            this.positions = positions;
        }

        public String getCode() {
            return code;
        }

        public String getSport() {
            return sport;
        }

        public String getName() {
            return name;
        }

        public int getActivePlayers() {
            return activePlayers;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getPositions() {
            return positions;
        }
    }

    public static class SportEventWeb {
        private int eventId;
        private String eventName;
        private String sportType;
        private String eventDate;
        private String registrationDeadLine;
        private String teamName;
        private int maxPlayers;
        private String formationCode;
        private String formationName;
        private String formationDescription;
        private String coachName;
        private String coachEmail;
        private String status;
        private boolean registrationClosed;
        private boolean finalized;
        private boolean published;
        private List<PlayerRegistration> registrations;

        public SportEventWeb(int eventId, String eventName, String sportType,
                             String eventDate, String registrationDeadLine,
                             String teamName, int maxPlayers,
                             String formationCode, String formationName, String formationDescription,
                             String coachName, String coachEmail) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.sportType = sportType;
            this.eventDate = eventDate;
            this.registrationDeadLine = registrationDeadLine;
            this.teamName = teamName;
            this.maxPlayers = maxPlayers;
            this.formationCode = formationCode;
            this.formationName = formationName;
            this.formationDescription = formationDescription;
            this.coachName = coachName;
            this.coachEmail = coachEmail;
            this.status = "Registration Open";
            this.registrationClosed = false;
            this.finalized = false;
            this.published = false;
            this.registrations = new ArrayList<>();
        }

        public int getEventId() {
            return eventId;
        }

        public String getEventName() {
            return eventName;
        }

        public String getSportType() {
            return sportType;
        }

        public String getEventDate() {
            return eventDate;
        }

        public String getRegistrationDeadLine() {
            return registrationDeadLine;
        }

        public String getTeamName() {
            return teamName;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public String getFormationCode() {
            return formationCode;
        }

        public String getFormationName() {
            return formationName;
        }

        public String getFormationDescription() {
            return formationDescription;
        }

        public String getCoachName() {
            return coachName;
        }

        public String getCoachEmail() {
            return coachEmail;
        }

        public String getStatus() {
            return status;
        }

        public boolean isRegistrationClosed() {
            return registrationClosed;
        }

        public boolean isFinalized() {
            return finalized;
        }

        public boolean isPublished() {
            return published;
        }

        public List<PlayerRegistration> getRegistrations() {
            return registrations;
        }

        public boolean isRegistrationAvailable() {
            LocalDate today = LocalDate.now();
            LocalDate deadline = LocalDate.parse(registrationDeadLine);
            return !registrationClosed && !finalized && !today.isAfter(deadline);
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setRegistrationClosed(boolean registrationClosed) {
            this.registrationClosed = registrationClosed;
        }

        public void setFinalized(boolean finalized) {
            this.finalized = finalized;
        }

        public void setPublished(boolean published) {
            this.published = published;
        }

        public void setFormationCode(String formationCode) {
            this.formationCode = formationCode;
        }

        public void setFormationName(String formationName) {
            this.formationName = formationName;
        }

        public void setFormationDescription(String formationDescription) {
            this.formationDescription = formationDescription;
        }

        public void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }
    }

    public static class PlayerRegistration {
        private int registrationId;
        private String playerName;
        private String email;
        private String requestedPosition;
        private String assignedPosition;
        private String selectionStatus;
        private boolean substitute;

        public PlayerRegistration(int registrationId, String playerName,
                                  String email, String requestedPosition) {
            this.registrationId = registrationId;
            this.playerName = playerName;
            this.email = email;
            this.requestedPosition = requestedPosition;
            this.assignedPosition = "Not assigned yet";
            this.selectionStatus = "Registered";
            this.substitute = false;
        }

        public int getRegistrationId() {
            return registrationId;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getEmail() {
            return email;
        }

        public String getRequestedPosition() {
            return requestedPosition;
        }

        public String getAssignedPosition() {
            return assignedPosition;
        }

        public String getSelectionStatus() {
            return selectionStatus;
        }

        public boolean isSubstitute() {
            return substitute;
        }

        public void setAssignedPosition(String assignedPosition) {
            this.assignedPosition = assignedPosition;
        }

        public void setSelectionStatus(String selectionStatus) {
            this.selectionStatus = selectionStatus;
        }

        public void setSubstitute(boolean substitute) {
            this.substitute = substitute;
        }
    }

    public static class RegistrationSearchResult {
        private SportEventWeb event;
        private PlayerRegistration registration;

        public RegistrationSearchResult(SportEventWeb event, PlayerRegistration registration) {
            this.event = event;
            this.registration = registration;
        }

        public SportEventWeb getEvent() {
            return event;
        }

        public PlayerRegistration getRegistration() {
            return registration;
        }
    }
}