package com.seps;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private static final List<UserAccount> users = new ArrayList<>();
    private static final List<SportEventWeb> events = new ArrayList<>();

    private static int nextUserId = 1;
    private static int nextEventId = 1;
    private static int nextRegistrationId = 1;

    static {
        users.add(new UserAccount(nextUserId++, "Demo Coach", "coach@seps.com", "1234", "COACH"));
        users.add(new UserAccount(nextUserId++, "Demo Player", "player@seps.com", "1234", "PLAYER"));
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("eventCount", events.size());
        model.addAttribute("userCount", users.size());
        return "index";
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        model.addAttribute("currentUser", getCurrentUser(session));
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam("name") String name,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("role") String role,
                                 Model model,
                                 HttpSession session) {
        String cleanEmail = email.trim().toLowerCase();

        for (UserAccount user : users) {
            if (user.getEmail().equalsIgnoreCase(cleanEmail)) {
                model.addAttribute("error", "An account with this email already exists.");
                model.addAttribute("currentUser", getCurrentUser(session));
                return "register";
            }
        }

        UserAccount user = new UserAccount(nextUserId++, name.trim(), cleanEmail, password, role.toUpperCase());
        users.add(user);
        session.setAttribute("currentUserId", user.getUserId());

        if (user.isCoach()) {
            return "redirect:/coach";
        }
        return "redirect:/player";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        model.addAttribute("currentUser", getCurrentUser(session));
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              Model model,
                              HttpSession session) {
        String cleanEmail = email.trim().toLowerCase();

        for (UserAccount user : users) {
            if (user.getEmail().equalsIgnoreCase(cleanEmail) && user.getPassword().equals(password)) {
                session.setAttribute("currentUserId", user.getUserId());
                if (user.isCoach()) {
                    return "redirect:/coach";
                }
                return "redirect:/player";
            }
        }

        model.addAttribute("error", "Invalid email or password. Please try again.");
        model.addAttribute("currentUser", getCurrentUser(session));
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/coach")
    public String coachDashboard(Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }

        List<SportEventWeb> coachEvents = getEventsForCoach(currentUser.getEmail());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("events", coachEvents);
        model.addAttribute("totalEvents", coachEvents.size());
        model.addAttribute("totalRegistrations", getTotalRegistrationsForCoach(currentUser.getEmail()));
        model.addAttribute("publishedEvents", getPublishedEventCountForCoach(currentUser.getEmail()));
        return "coach";
    }

    @GetMapping("/coach/create-event")
    public String createEventPage(Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sports", SportRules.getSports());
        model.addAttribute("sportLimits", SportRules.getMaxPlayersPerTeamMap());
        return "create-event";
    }

    @PostMapping("/coach/create-event")
    public String createEventSubmit(@RequestParam("eventName") String eventName,
                                    @RequestParam("sportType") String sportType,
                                    @RequestParam("eventDate") String eventDate,
                                    @RequestParam("registrationDeadLine") String registrationDeadLine,
                                    @RequestParam("teamAName") String teamAName,
                                    @RequestParam("teamBName") String teamBName,
                                    @RequestParam("selectedPlayersPerTeam") int selectedPlayersPerTeam,
                                    @RequestParam("teamAFormation") String teamAFormation,
                                    @RequestParam("teamBFormation") String teamBFormation,
                                    @RequestParam(value = "expectedPlayers", defaultValue = "0") int expectedPlayers,
                                    Model model,
                                    HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }

        LocalDate eventLocalDate = LocalDate.parse(eventDate);
        LocalDate deadlineLocalDate = LocalDate.parse(registrationDeadLine);
        if (deadlineLocalDate.isAfter(eventLocalDate)) {
            model.addAttribute("error", "Registration deadline cannot be after the event date.");
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("sports", SportRules.getSports());
            model.addAttribute("sportLimits", SportRules.getMaxPlayersPerTeamMap());
            return "create-event";
        }

        int sportMax = SportRules.getMaxPlayersPerTeam(sportType);
        int safePlayersPerTeam = clamp(selectedPlayersPerTeam, 1, sportMax);

        SportEventWeb event = new SportEventWeb(
                nextEventId++,
                eventName.trim(),
                sportType,
                eventDate,
                registrationDeadLine,
                emptyToDefault(teamAName, "Team A"),
                emptyToDefault(teamBName, "Team B"),
                safePlayersPerTeam,
                sportMax,
                Math.max(0, expectedPlayers),
                emptyToDefault(teamAFormation, SportRules.defaultFormation(sportType, safePlayersPerTeam)),
                emptyToDefault(teamBFormation, SportRules.defaultFormation(sportType, safePlayersPerTeam)),
                currentUser.getName(),
                currentUser.getEmail()
        );

        events.add(event);
        return "redirect:/coach/event-created/" + event.getEventId();
    }

    @GetMapping("/coach/event-created/{eventId}")
    public String eventCreatedPage(@PathVariable int eventId, Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
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
    public String manageEvent(@PathVariable int eventId, Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);
        if (event == null || !event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())) {
            return "redirect:/coach";
        }

        addEventManagementAttributes(model, currentUser, event);
        return "coach-event";
    }

    @PostMapping("/coach/event/{eventId}/settings")
    public String updateMatchSettings(@PathVariable int eventId,
                                      @RequestParam("teamAName") String teamAName,
                                      @RequestParam("teamBName") String teamBName,
                                      @RequestParam("selectedPlayersPerTeam") int selectedPlayersPerTeam,
                                      @RequestParam("teamAFormation") String teamAFormation,
                                      @RequestParam("teamBFormation") String teamBFormation,
                                      @RequestParam(value = "expectedPlayers", defaultValue = "0") int expectedPlayers,
                                      HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);
        if (event == null || !event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail()) || event.isFinalized()) {
            return "redirect:/coach/event/" + eventId;
        }

        int safePlayersPerTeam = clamp(selectedPlayersPerTeam, 1, event.getSportMaxPlayersPerTeam());
        event.setTeamAName(emptyToDefault(teamAName, "Team A"));
        event.setTeamBName(emptyToDefault(teamBName, "Team B"));
        event.setSelectedPlayersPerTeam(safePlayersPerTeam);
        event.setExpectedPlayers(Math.max(0, expectedPlayers));
        event.setTeamAFormation(emptyToDefault(teamAFormation, SportRules.defaultFormation(event.getSportType(), safePlayersPerTeam)));
        event.setTeamBFormation(emptyToDefault(teamBFormation, SportRules.defaultFormation(event.getSportType(), safePlayersPerTeam)));
        clearAssignments(event);
        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/close-registration")
    public String closeRegistration(@PathVariable int eventId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
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
    public String openRegistration(@PathVariable int eventId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }
        SportEventWeb event = findEventById(eventId);
        if (event != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail()) && !event.isFinalized()) {
            event.setRegistrationClosed(false);
            event.setStatus("Registration Open");
        }
        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/auto-assign")
    public String autoAssignLineup(@PathVariable int eventId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }
        SportEventWeb event = findEventById(eventId);
        if (event != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail()) && !event.isFinalized()) {
            autoAssignPlayers(event);
        }
        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/clear-lineup")
    public String clearLineup(@PathVariable int eventId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }
        SportEventWeb event = findEventById(eventId);
        if (event != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail()) && !event.isFinalized()) {
            clearAssignments(event);
        }
        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/assign/{registrationId}")
    public String assignPlayer(@PathVariable int eventId,
                               @PathVariable int registrationId,
                               @RequestParam("teamAssignment") String teamAssignment,
                               @RequestParam("assignedPosition") String assignedPosition,
                               HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }

        SportEventWeb event = findEventById(eventId);
        PlayerRegistration registration = findRegistrationById(event, registrationId);
        if (event != null && registration != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail()) && !event.isFinalized()) {
            if ("TEAM_A".equals(teamAssignment)) {
                registration.setTeamAssignment("Team A");
                registration.setAssignedPosition(assignedPosition);
                registration.setSelectionStatus("Selected");
                registration.setSubstitute(false);
            } else if ("TEAM_B".equals(teamAssignment)) {
                registration.setTeamAssignment("Team B");
                registration.setAssignedPosition(assignedPosition);
                registration.setSelectionStatus("Selected");
                registration.setSubstitute(false);
            } else {
                makeSubstitute(registration);
            }
            enforceActiveLimits(event);
        }
        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/finalize")
    public String finalizeLineup(@PathVariable int eventId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
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
    public String publishLineup(@PathVariable int eventId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }
        SportEventWeb event = findEventById(eventId);
        if (event != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())) {
            autoAssignPlayers(event);
            event.setFinalized(true);
            event.setRegistrationClosed(true);
            event.setPublished(true);
            event.setStatus("Lineup Published");
        }
        return "redirect:/coach/event/" + eventId;
    }

    @PostMapping("/coach/event/{eventId}/reopen")
    public String reopenLineup(@PathVariable int eventId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isCoach()) {
            return "redirect:/login";
        }
        SportEventWeb event = findEventById(eventId);
        if (event != null && event.getCoachEmail().equalsIgnoreCase(currentUser.getEmail())) {
            event.setFinalized(false);
            event.setPublished(false);
            event.setRegistrationClosed(false);
            event.setStatus("Registration Open");
        }
        return "redirect:/coach/event/" + eventId;
    }

    @GetMapping("/player")
    public String playerDashboard(@RequestParam(value = "coachName", required = false) String coachName,
                                  Model model,
                                  HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isPlayer()) {
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
    public String eventDetails(@PathVariable int eventId, Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isPlayer()) {
            return "redirect:/login";
        }
        SportEventWeb event = findEventById(eventId);
        if (event == null) {
            return "redirect:/player";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        model.addAttribute("canRegister", event.isRegistrationAvailable());
        model.addAttribute("myRegistration", findRegistrationByEmail(event, currentUser.getEmail()));
        return "event-details";
    }

    @GetMapping("/player/register/{eventId}")
    public String registerForEventPage(@PathVariable int eventId, Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isPlayer()) {
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
        model.addAttribute("positionOptions", SportRules.positionsFor(event.getSportType(), event.getSelectedPlayersPerTeam(), event.getTeamAFormation()));
        return "register-event";
    }

    @PostMapping("/player/register")
    public String registerPlayer(@RequestParam("eventId") int eventId,
                                 @RequestParam("requestedPosition") String requestedPosition,
                                 Model model,
                                 HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isPlayer()) {
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
        PlayerRegistration registration = new PlayerRegistration(nextRegistrationId++, currentUser.getName(), currentUser.getEmail(), requestedPosition);
        event.getRegistrations().add(registration);
        return "redirect:/player/registration-success/" + registration.getRegistrationId();
    }

    @GetMapping("/player/registration-success/{registrationId}")
    public String registrationSuccess(@PathVariable int registrationId, Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isPlayer()) {
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
    public String cancelRegistration(@PathVariable int registrationId, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isPlayer()) {
            return "redirect:/login";
        }
        RegistrationSearchResult result = findRegistrationByIdAcrossEvents(registrationId);
        if (result != null && !result.getEvent().isFinalized()) {
            result.getEvent().getRegistrations().remove(result.getRegistration());
        }
        return "redirect:/player";
    }

    @GetMapping("/player/lineup/{eventId}")
    public String viewLineup(@PathVariable int eventId, Model model, HttpSession session) {
        UserAccount currentUser = getCurrentUser(session);
        if (currentUser == null || !currentUser.isPlayer()) {
            return "redirect:/login";
        }
        SportEventWeb event = findEventById(eventId);
        if (event == null) {
            return "redirect:/player";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        model.addAttribute("myRegistration", findRegistrationByEmail(event, currentUser.getEmail()));
        model.addAttribute("teamAPlayers", getTeamAPlayers(event));
        model.addAttribute("teamBPlayers", getTeamBPlayers(event));
        model.addAttribute("substitutes", getSubstitutes(event));
        return "player-lineup";
    }

    private void addEventManagementAttributes(Model model, UserAccount currentUser, SportEventWeb event) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("event", event);
        model.addAttribute("playersPerTeamOptions", SportRules.playersPerTeamOptions(event.getSportType()));
        model.addAttribute("formationOptions", SportRules.formationOptions(event.getSportType(), event.getSelectedPlayersPerTeam()));
        model.addAttribute("positionOptions", SportRules.positionsFor(event.getSportType(), event.getSelectedPlayersPerTeam(), event.getTeamAFormation()));
        model.addAttribute("teamAPlayers", getTeamAPlayers(event));
        model.addAttribute("teamBPlayers", getTeamBPlayers(event));
        model.addAttribute("substitutes", getSubstitutes(event));
        model.addAttribute("unassignedPlayers", getUnassignedPlayers(event));
    }

    private UserAccount getCurrentUser(HttpSession session) {
        Object userIdObject = session.getAttribute("currentUserId");
        if (userIdObject == null) {
            return null;
        }
        int userId = (int) userIdObject;
        for (UserAccount user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    private void autoAssignPlayers(SportEventWeb event) {
        clearAssignments(event);
        List<PlayerRegistration> registrations = event.getRegistrations();
        int activeLimit = Math.min(registrations.size(), event.getSelectedActivePlayers());
        if (activeLimit % 2 != 0) {
            activeLimit--;
        }
        int perTeam = activeLimit / 2;
        List<String> teamAPositions = SportRules.positionsFor(event.getSportType(), event.getSelectedPlayersPerTeam(), event.getTeamAFormation());
        List<String> teamBPositions = SportRules.positionsFor(event.getSportType(), event.getSelectedPlayersPerTeam(), event.getTeamBFormation());

        for (int i = 0; i < registrations.size(); i++) {
            PlayerRegistration registration = registrations.get(i);
            if (i < perTeam) {
                registration.setTeamAssignment("Team A");
                registration.setAssignedPosition(safePosition(teamAPositions, i));
                registration.setSelectionStatus("Selected");
                registration.setSubstitute(false);
            } else if (i < activeLimit) {
                registration.setTeamAssignment("Team B");
                registration.setAssignedPosition(safePosition(teamBPositions, i - perTeam));
                registration.setSelectionStatus("Selected");
                registration.setSubstitute(false);
            } else {
                makeSubstitute(registration);
            }
        }
    }

    private void clearAssignments(SportEventWeb event) {
        for (PlayerRegistration registration : event.getRegistrations()) {
            registration.setTeamAssignment("Unassigned");
            registration.setAssignedPosition("Not assigned yet");
            registration.setSelectionStatus("Registered");
            registration.setSubstitute(false);
        }
    }

    private void enforceActiveLimits(SportEventWeb event) {
        moveExtraTeamPlayersToSubstitutes(getTeamAPlayers(event), event.getSelectedPlayersPerTeam());
        moveExtraTeamPlayersToSubstitutes(getTeamBPlayers(event), event.getSelectedPlayersPerTeam());
        int activeCount = getTeamAPlayers(event).size() + getTeamBPlayers(event).size();
        if (activeCount > event.getSelectedActivePlayers()) {
            List<PlayerRegistration> allActive = new ArrayList<>();
            allActive.addAll(getTeamAPlayers(event));
            allActive.addAll(getTeamBPlayers(event));
            for (int i = event.getSelectedActivePlayers(); i < allActive.size(); i++) {
                makeSubstitute(allActive.get(i));
            }
        }
    }

    private void moveExtraTeamPlayersToSubstitutes(List<PlayerRegistration> teamPlayers, int allowed) {
        for (int i = allowed; i < teamPlayers.size(); i++) {
            makeSubstitute(teamPlayers.get(i));
        }
    }

    private void makeSubstitute(PlayerRegistration registration) {
        registration.setTeamAssignment("Substitute");
        registration.setAssignedPosition("Substitute");
        registration.setSelectionStatus("Substitute");
        registration.setSubstitute(true);
    }

    private String safePosition(List<String> positions, int index) {
        if (positions == null || positions.isEmpty()) {
            return "Player";
        }
        if (index >= 0 && index < positions.size()) {
            return positions.get(index);
        }
        return "Player " + (index + 1);
    }

    private List<PlayerRegistration> getTeamAPlayers(SportEventWeb event) {
        List<PlayerRegistration> result = new ArrayList<>();
        for (PlayerRegistration registration : event.getRegistrations()) {
            if ("Team A".equalsIgnoreCase(registration.getTeamAssignment()) && !registration.isSubstitute()) {
                result.add(registration);
            }
        }
        return result;
    }

    private List<PlayerRegistration> getTeamBPlayers(SportEventWeb event) {
        List<PlayerRegistration> result = new ArrayList<>();
        for (PlayerRegistration registration : event.getRegistrations()) {
            if ("Team B".equalsIgnoreCase(registration.getTeamAssignment()) && !registration.isSubstitute()) {
                result.add(registration);
            }
        }
        return result;
    }

    private List<PlayerRegistration> getSubstitutes(SportEventWeb event) {
        List<PlayerRegistration> result = new ArrayList<>();
        for (PlayerRegistration registration : event.getRegistrations()) {
            if (registration.isSubstitute() || "Substitute".equalsIgnoreCase(registration.getTeamAssignment())) {
                result.add(registration);
            }
        }
        return result;
    }

    private List<PlayerRegistration> getUnassignedPlayers(SportEventWeb event) {
        List<PlayerRegistration> result = new ArrayList<>();
        for (PlayerRegistration registration : event.getRegistrations()) {
            if ("Unassigned".equalsIgnoreCase(registration.getTeamAssignment())) {
                result.add(registration);
            }
        }
        return result;
    }

    private SportEventWeb findEventById(int eventId) {
        for (SportEventWeb event : events) {
            if (event.getEventId() == eventId) {
                return event;
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

    private int getPublishedEventCountForCoach(String coachEmail) {
        int total = 0;
        for (SportEventWeb event : events) {
            if (event.getCoachEmail().equalsIgnoreCase(coachEmail) && event.isPublished()) {
                total++;
            }
        }
        return total;
    }

    private String emptyToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

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

        public int getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
        public boolean isCoach() { return role.equalsIgnoreCase("COACH"); }
        public boolean isPlayer() { return role.equalsIgnoreCase("PLAYER"); }
    }

    public static class SportEventWeb {
        private int eventId;
        private String eventName;
        private String sportType;
        private String eventDate;
        private String registrationDeadLine;
        private String teamAName;
        private String teamBName;
        private int selectedPlayersPerTeam;
        private int sportMaxPlayersPerTeam;
        private int expectedPlayers;
        private String teamAFormation;
        private String teamBFormation;
        private String coachName;
        private String coachEmail;
        private String status;
        private boolean registrationClosed;
        private boolean finalized;
        private boolean published;
        private List<PlayerRegistration> registrations;

        public SportEventWeb(int eventId, String eventName, String sportType, String eventDate,
                             String registrationDeadLine, String teamAName, String teamBName,
                             int selectedPlayersPerTeam, int sportMaxPlayersPerTeam, int expectedPlayers,
                             String teamAFormation, String teamBFormation, String coachName, String coachEmail) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.sportType = sportType;
            this.eventDate = eventDate;
            this.registrationDeadLine = registrationDeadLine;
            this.teamAName = teamAName;
            this.teamBName = teamBName;
            this.selectedPlayersPerTeam = selectedPlayersPerTeam;
            this.sportMaxPlayersPerTeam = sportMaxPlayersPerTeam;
            this.expectedPlayers = expectedPlayers;
            this.teamAFormation = teamAFormation;
            this.teamBFormation = teamBFormation;
            this.coachName = coachName;
            this.coachEmail = coachEmail;
            this.status = "Registration Open";
            this.registrationClosed = false;
            this.finalized = false;
            this.published = false;
            this.registrations = new ArrayList<>();
        }

        public int getEventId() { return eventId; }
        public String getEventName() { return eventName; }
        public String getSportType() { return sportType; }
        public String getEventDate() { return eventDate; }
        public String getRegistrationDeadLine() { return registrationDeadLine; }
        public String getTeamAName() { return teamAName; }
        public String getTeamBName() { return teamBName; }
        public int getSelectedPlayersPerTeam() { return selectedPlayersPerTeam; }
        public int getSelectedActivePlayers() { return selectedPlayersPerTeam * 2; }
        public int getSportMaxPlayersPerTeam() { return sportMaxPlayersPerTeam; }
        public int getSportMaxActivePlayers() { return sportMaxPlayersPerTeam * 2; }
        public int getExpectedPlayers() { return expectedPlayers; }
        public String getTeamAFormation() { return teamAFormation; }
        public String getTeamBFormation() { return teamBFormation; }
        public String getCoachName() { return coachName; }
        public String getCoachEmail() { return coachEmail; }
        public String getStatus() { return status; }
        public boolean isRegistrationClosed() { return registrationClosed; }
        public boolean isFinalized() { return finalized; }
        public boolean isPublished() { return published; }
        public List<PlayerRegistration> getRegistrations() { return registrations; }
        public int getRegisteredCount() { return registrations.size(); }
        public String getMatchFormat() { return selectedPlayersPerTeam + "v" + selectedPlayersPerTeam; }

        public int getActiveCount() {
            int count = 0;
            for (PlayerRegistration registration : registrations) {
                if (!registration.isSubstitute() && ("Team A".equalsIgnoreCase(registration.getTeamAssignment()) || "Team B".equalsIgnoreCase(registration.getTeamAssignment()))) {
                    count++;
                }
            }
            return count;
        }

        public int getSubstituteCount() {
            int count = 0;
            for (PlayerRegistration registration : registrations) {
                if (registration.isSubstitute() || "Substitute".equalsIgnoreCase(registration.getTeamAssignment())) {
                    count++;
                }
            }
            return count;
        }

        public int getUnassignedCount() {
            int count = 0;
            for (PlayerRegistration registration : registrations) {
                if ("Unassigned".equalsIgnoreCase(registration.getTeamAssignment())) {
                    count++;
                }
            }
            return count;
        }

        public boolean isRegistrationAvailable() {
            LocalDate today = LocalDate.now();
            LocalDate deadline = LocalDate.parse(registrationDeadLine);
            return !registrationClosed && !finalized && !today.isAfter(deadline);
        }

        public void setStatus(String status) { this.status = status; }
        public void setRegistrationClosed(boolean registrationClosed) { this.registrationClosed = registrationClosed; }
        public void setFinalized(boolean finalized) { this.finalized = finalized; }
        public void setPublished(boolean published) { this.published = published; }
        public void setTeamAName(String teamAName) { this.teamAName = teamAName; }
        public void setTeamBName(String teamBName) { this.teamBName = teamBName; }
        public void setSelectedPlayersPerTeam(int selectedPlayersPerTeam) { this.selectedPlayersPerTeam = selectedPlayersPerTeam; }
        public void setExpectedPlayers(int expectedPlayers) { this.expectedPlayers = expectedPlayers; }
        public void setTeamAFormation(String teamAFormation) { this.teamAFormation = teamAFormation; }
        public void setTeamBFormation(String teamBFormation) { this.teamBFormation = teamBFormation; }
    }

    public static class PlayerRegistration {
        private int registrationId;
        private String playerName;
        private String email;
        private String requestedPosition;
        private String assignedPosition;
        private String selectionStatus;
        private String teamAssignment;
        private boolean substitute;

        public PlayerRegistration(int registrationId, String playerName, String email, String requestedPosition) {
            this.registrationId = registrationId;
            this.playerName = playerName;
            this.email = email;
            this.requestedPosition = requestedPosition;
            this.assignedPosition = "Not assigned yet";
            this.selectionStatus = "Registered";
            this.teamAssignment = "Unassigned";
            this.substitute = false;
        }

        public int getRegistrationId() { return registrationId; }
        public String getPlayerName() { return playerName; }
        public String getEmail() { return email; }
        public String getRequestedPosition() { return requestedPosition; }
        public String getAssignedPosition() { return assignedPosition; }
        public String getSelectionStatus() { return selectionStatus; }
        public String getTeamAssignment() { return teamAssignment; }
        public boolean isSubstitute() { return substitute; }
        public void setAssignedPosition(String assignedPosition) { this.assignedPosition = assignedPosition; }
        public void setSelectionStatus(String selectionStatus) { this.selectionStatus = selectionStatus; }
        public void setTeamAssignment(String teamAssignment) { this.teamAssignment = teamAssignment; }
        public void setSubstitute(boolean substitute) { this.substitute = substitute; }
    }

    public static class RegistrationSearchResult {
        private SportEventWeb event;
        private PlayerRegistration registration;
        public RegistrationSearchResult(SportEventWeb event, PlayerRegistration registration) {
            this.event = event;
            this.registration = registration;
        }
        public SportEventWeb getEvent() { return event; }
        public PlayerRegistration getRegistration() { return registration; }
    }

    public static class SportRules {
        public static List<String> getSports() {
            return Arrays.asList("Soccer", "Basketball", "Volleyball", "Hockey", "Tennis");
        }

        public static Map<String, Integer> getMaxPlayersPerTeamMap() {
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("Soccer", 11);
            map.put("Basketball", 5);
            map.put("Volleyball", 6);
            map.put("Hockey", 6);
            map.put("Tennis", 2);
            return map;
        }

        public static int getMaxPlayersPerTeam(String sport) {
            if (sport == null) return 11;
            String value = sport.toLowerCase();
            if (value.contains("basketball")) return 5;
            if (value.contains("volleyball")) return 6;
            if (value.contains("hockey")) return 6;
            if (value.contains("tennis")) return 2;
            return 11;
        }

        public static List<Integer> playersPerTeamOptions(String sport) {
            List<Integer> options = new ArrayList<>();
            for (int i = 1; i <= getMaxPlayersPerTeam(sport); i++) {
                options.add(i);
            }
            return options;
        }

        public static String defaultFormation(String sport, int playersPerTeam) {
            List<String> formations = formationOptions(sport, playersPerTeam);
            return formations.isEmpty() ? playersPerTeam + " Players" : formations.get(0);
        }

        public static List<String> formationOptions(String sport, int playersPerTeam) {
            if (sport == null) return Arrays.asList(playersPerTeam + " Players");
            String value = sport.toLowerCase();
            if (value.contains("basketball")) return basketballFormations(playersPerTeam);
            if (value.contains("volleyball")) return volleyballFormations(playersPerTeam);
            if (value.contains("hockey")) return hockeyFormations(playersPerTeam);
            if (value.contains("tennis")) return tennisFormations(playersPerTeam);
            return soccerFormations(playersPerTeam);
        }

        public static List<String> positionsFor(String sport, int playersPerTeam, String formation) {
            List<String> positions = new ArrayList<>();
            if (playersPerTeam <= 0) {
                positions.add("Player");
                return positions;
            }
            if (sport != null && sport.toLowerCase().contains("basketball")) {
                List<String> base = Arrays.asList("Guard", "Wing", "Forward", "Power Forward", "Center");
                return take(base, playersPerTeam);
            }
            if (sport != null && sport.toLowerCase().contains("volleyball")) {
                List<String> base = Arrays.asList("Setter", "Outside Hitter", "Middle Blocker", "Opposite", "Libero", "Defensive Specialist");
                return take(base, playersPerTeam);
            }
            if (sport != null && sport.toLowerCase().contains("hockey")) {
                List<String> base = Arrays.asList("Goalie", "Left Defense", "Right Defense", "Center", "Left Wing", "Right Wing");
                return take(base, playersPerTeam);
            }
            if (sport != null && sport.toLowerCase().contains("tennis")) {
                if (playersPerTeam == 1) return Arrays.asList("Singles Player");
                return Arrays.asList("Player 1", "Player 2");
            }
            if (playersPerTeam == 1) return Arrays.asList("Player 1");
            positions.add("Goalkeeper");
            for (int i = 2; i <= playersPerTeam; i++) {
                positions.add("Player " + i);
            }
            return positions;
        }

        private static List<String> soccerFormations(int n) {
            switch (n) {
                case 1: return Arrays.asList("1 Player");
                case 2: return Arrays.asList("1-1", "2 Attackers");
                case 3: return Arrays.asList("1-1-1", "1-2");
                case 4: return Arrays.asList("1-2-1", "2-2");
                case 5: return Arrays.asList("1-2-1", "2-1-1", "1-1-2");
                case 6: return Arrays.asList("2-2-1", "1-3-1", "2-1-2");
                case 7: return Arrays.asList("2-3-1", "3-2-1", "2-2-2");
                case 8: return Arrays.asList("3-3-1", "2-3-2", "3-2-2");
                case 9: return Arrays.asList("3-3-2", "4-3-1", "3-2-3");
                case 10: return Arrays.asList("4-3-2", "3-4-2", "4-2-3");
                default: return Arrays.asList("4-4-2", "4-3-3", "3-5-2", "5-3-2");
            }
        }

        private static List<String> basketballFormations(int n) {
            switch (n) {
                case 1: return Arrays.asList("1 Guard");
                case 2: return Arrays.asList("1-1", "2 Guards");
                case 3: return Arrays.asList("1-1-1", "2-1");
                case 4: return Arrays.asList("2-2", "1-2-1");
                default: return Arrays.asList("2-1-2", "1-2-2", "2-2-1");
            }
        }

        private static List<String> volleyballFormations(int n) {
            switch (n) {
                case 1: return Arrays.asList("1 Player");
                case 2: return Arrays.asList("2 Player Rotation");
                case 3: return Arrays.asList("3 Player Rotation");
                case 4: return Arrays.asList("4 Player Rotation", "Diamond 4");
                case 5: return Arrays.asList("5 Player Rotation", "Setter 5");
                default: return Arrays.asList("6 Player Rotation", "Standard 6");
            }
        }

        private static List<String> hockeyFormations(int n) {
            switch (n) {
                case 1: return Arrays.asList("1 Skater");
                case 2: return Arrays.asList("1-1", "2 Skaters");
                case 3: return Arrays.asList("1 Goalie - 2 Skaters", "1-1-1");
                case 4: return Arrays.asList("1-1-2", "2-2");
                case 5: return Arrays.asList("1-2-2", "2-3");
                default: return Arrays.asList("1-2-3", "2-4");
            }
        }

        private static List<String> tennisFormations(int n) {
            if (n == 1) return Arrays.asList("Singles");
            return Arrays.asList("Doubles", "Two Back", "Two Up");
        }

        private static List<String> take(List<String> base, int count) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                if (i < base.size()) result.add(base.get(i));
                else result.add("Player " + (i + 1));
            }
            return result;
        }
    }
}
