import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * Single-file JavaFX app with an in-memory backend to demonstrate sequential flow and usable UI:
 * - Registration, Login
 * - Events listing and register/unregister
 * - Profile shows user's registrations
 * - Teams listing
 */
public class UIApp extends Application {

    private BorderPane root;
    private VBox sideNav;
    private StackPane mainContent;

    // Current authenticated user (null if none)
    private BackendService.User currentUser = null;

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        root.setPrefSize(1100, 700);

        createSideNav();
        createMainContent();

        root.setLeft(sideNav);
        root.setCenter(mainContent);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Sports Event Management");
        primaryStage.setScene(scene);

        // Start with registration as required sequential step
        showRegistration();

        primaryStage.show();
    }

    private void createSideNav() {
        sideNav = new VBox(14);
        sideNav.setPadding(new Insets(22));
        sideNav.setStyle("-fx-background-color: linear-gradient(#0d4668, #082a40);");
        sideNav.setPrefWidth(240);

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        Circle avatar = new Circle(36, Color.web("#1e90ff"));
        Text title = new Text("SEMP");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        header.getChildren().addAll(avatar, title);

        // nav
        ToggleGroup navGroup = new ToggleGroup();
        ToggleButton btnDashboard = navToggle("⚽", "Dashboard", navGroup);
        ToggleButton btnEvents = navToggle("📅", "Events", navGroup);
        ToggleButton btnTeam = navToggle("👥", "Team", navGroup);
        ToggleButton btnProfile = navToggle("🙍", "Profile", navGroup);
        Button btnLogout = new Button("Logout");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff7070;");

        btnDashboard.setOnAction(e -> showDashboard());
        btnEvents.setOnAction(e -> showEvents());
        btnTeam.setOnAction(e -> showTeam());
        btnProfile.setOnAction(e -> showProfile());
        btnLogout.setOnAction(e -> doLogout());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sideNav.getChildren().addAll(header, btnDashboard, btnEvents, btnTeam, btnProfile, spacer, btnLogout);

        // Hide sideNav until user logs in
        sideNav.setVisible(false);
        sideNav.setManaged(false);
    }

    private ToggleButton navToggle(String icon, String text, ToggleGroup group) {
        ToggleButton t = new ToggleButton(icon + "  " + text);
        t.setToggleGroup(group);
        t.setMaxWidth(Double.MAX_VALUE);
        t.setAlignment(Pos.CENTER_LEFT);
        t.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 12 8 12;");
        t.setOnMouseEntered(e -> t.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 12 8 12;"));
        t.setOnMouseExited(e -> t.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 12 8 12;"));
        return t;
    }

    private void createMainContent() {
        mainContent = new StackPane();
        mainContent.setPadding(new Insets(24));
    }

    private void setContent(Node node) {
        node.setOpacity(0);
        mainContent.getChildren().clear();
        mainContent.getChildren().add(node);
        FadeTransition ft = new FadeTransition(Duration.millis(240), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // Sequential flow: Registration -> Login -> Main
    private void showRegistration() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setMaxWidth(520);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        box.setEffect(new DropShadow(8, Color.rgb(0,0,0,0.12)));

        Text h = new Text("Create an account");
        h.setFont(Font.font("System", FontWeight.BOLD, 20));

        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");

        ChoiceBox<String> role = new ChoiceBox<>();
        role.getItems().addAll("Player", "Coach");
        role.setValue("Player");

        Button btnRegister = new Button("Register");
        btnRegister.setStyle("-fx-background-color: linear-gradient(#2ea3ff,#1b78d1); -fx-text-fill: white; -fx-background-radius: 6;");

        ProgressIndicator pi = new ProgressIndicator();
        pi.setVisible(false);
        pi.setMaxSize(32, 32);

        btnRegister.setOnAction(e -> {
            String em = email.getText().trim();
            String pw = pass.getText();
            String r = role.getValue();
            if (em.isEmpty() || pw.isEmpty()) {
                alert("Please fill email and password.");
                return;
            }
            // disable while registering
            btnRegister.setDisable(true);
            pi.setVisible(true);

            Task<BackendService.Result> t = new Task<>() {
                @Override
                protected BackendService.Result call() throws Exception {
                    return BackendService.registerUser(em, pw, r);
                }
            };
            t.setOnSucceeded(ev -> {
                BackendService.Result res = t.getValue();
                btnRegister.setDisable(false);
                pi.setVisible(false);
                if (res.success) {
                    alert("Registration successful. Please log in.");
                    showLogin();
                } else {
                    alert("Registration failed: " + res.message);
                }
            });
            t.setOnFailed(ev -> {
                btnRegister.setDisable(false);
                pi.setVisible(false);
                Throwable ex = t.getException();
                alert("Registration error: " + (ex != null ? ex.toString() : "unknown"));
            });
            new Thread(t).start();
        });

        box.getChildren().addAll(h, email, pass, role, new HBox(12, btnRegister, pi));

        StackPane wrapper = new StackPane(box);
        wrapper.setAlignment(Pos.CENTER);
        setContent(wrapper);
    }

    private void showLogin() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setMaxWidth(520);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        box.setEffect(new DropShadow(8, Color.rgb(0,0,0,0.12)));

        Text h = new Text("Sign in");
        h.setFont(Font.font("System", FontWeight.BOLD, 20));

        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");

        Button btnLogin = new Button("Sign in");
        btnLogin.setStyle("-fx-background-color: linear-gradient(#2ea3ff,#1b78d1); -fx-text-fill: white; -fx-background-radius: 6;");
        ProgressIndicator pi = new ProgressIndicator();
        pi.setVisible(false);
        pi.setMaxSize(32,32);

        Button toRegister = new Button("Create account");
        toRegister.setOnAction(e -> showRegistration());

        // show demo credentials to help debugging
        Text demoCreds = new Text("Demo accounts: coach@example.com / demo    player@example.com / demo");
        demoCreds.setFill(Color.GRAY);
        demoCreds.setFont(Font.font(12));

        btnLogin.setOnAction(e -> {
            String em = email.getText().trim();
            String pw = pass.getText();
            if (em.isEmpty() || pw.isEmpty()) { alert("Please fill email and password."); return; }

            btnLogin.setDisable(true);
            pi.setVisible(true);

            Task<BackendService.User> t = new Task<>() {
                @Override
                protected BackendService.User call() throws Exception {
                    return BackendService.loginUser(em, pw);
                }
            };
            t.setOnSucceeded(ev -> {
                btnLogin.setDisable(false);
                pi.setVisible(false);
                BackendService.User u = t.getValue();
                if (u != null) {
                    currentUser = u;
                    sideNav.setVisible(true);
                    sideNav.setManaged(true);
                    showDashboard();
                } else {
                    alert("Invalid credentials.");
                }
            });
            t.setOnFailed(ev -> {
                btnLogin.setDisable(false);
                pi.setVisible(false);
                Throwable ex = t.getException();
                alert("Login error: " + (ex != null ? ex.toString() : "unknown"));
            });
            new Thread(t).start();
        });

        box.getChildren().addAll(h, email, pass, new HBox(12, btnLogin, pi), toRegister, demoCreds);
        StackPane wrapper = new StackPane(box);
        wrapper.setAlignment(Pos.CENTER);
        setContent(wrapper);
    }

    private void doLogout() {
        currentUser = null;
        sideNav.setVisible(false);
        sideNav.setManaged(false);
        alert("Logged out.");
        showRegistration();
    }

    private Node createDashboardContent() {
        VBox rootBox = new VBox(18);
        rootBox.setPadding(new Insets(12));

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text h = new Text("Dashboard");
        h.setFont(Font.font("System", FontWeight.BOLD, 22));
        header.getChildren().add(h);

        FlowPane cards = new FlowPane();
        cards.setHgap(12);
        cards.setVgap(12);
        cards.setPrefWrapLength(700);

        // Loading indicator while fetching events
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(36,36);

        VBox container = new VBox(12, header, pi);

        // Async load
        Task<List<BackendService.Event>> t = new Task<>() {
            @Override
            protected List<BackendService.Event> call() throws Exception {
                return BackendService.getEvents();
            }
        };
        t.setOnSucceeded(ev -> {
            List<BackendService.Event> events = t.getValue();
            cards.getChildren().clear();
            for (BackendService.Event e : events) cards.getChildren().add(createEventCard(e.title, e.date));
            if (!container.getChildren().contains(cards)) container.getChildren().setAll(header, cards);
        });
        t.setOnFailed(ev -> {
            alert("Failed to load events.");
        });
        new Thread(t).start();

        return container;
    }

    private VBox createEventCard(String title, String subtitle) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setPrefSize(220, 110);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        card.setEffect(new DropShadow(6, Color.rgb(0,0,0,0.08)));

        Text t = new Text(title);
        t.setFont(Font.font("System", FontWeight.BOLD, 14));
        Text s = new Text(subtitle);
        s.setFill(Color.GRAY);
        Button action = new Button("View");
        action.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white; -fx-background-radius: 6;");
        action.setOnAction(e -> alert("Open: " + title));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(t, s, spacer, action);
        return card;
    }

    private void showDashboard() {
        setContent(createDashboardContent());
    }

    private void showEvents() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(12));
        Text header = new Text("Events / Registration");
        header.setFont(Font.font(18));

        ListView<HBox> list = new ListView<>();
        list.setPrefHeight(420);

        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(28,28);

        pane.getChildren().addAll(new HBox(8, header, pi), list);
        setContent(pane);

        // load events
        Task<List<BackendService.Event>> t = new Task<>() {
            @Override
            protected List<BackendService.Event> call() throws Exception {
                return BackendService.getEvents();
            }
        };
        t.setOnSucceeded(ev -> {
            List<BackendService.Event> events = t.getValue();
            list.getItems().clear();
            for (BackendService.Event e : events) {
                HBox row = new HBox(12);
                row.setPadding(new Insets(8));
                VBox info = new VBox(4, new Text(e.title), new Text(e.date));
                Button reg = new Button("Register");
                reg.setStyle("-fx-background-color: #1e90ff; -fx-text-fill: white;");
                // check registration state
                boolean registered = currentUser != null && BackendService.isUserRegistered(currentUser.email, e.title);
                reg.setDisable(registered);
                reg.setText(registered ? "Registered" : "Register");
                reg.setOnAction(ae -> {
                    reg.setDisable(true);
                    Task<Boolean> rt = new Task<>() {
                        @Override
                        protected Boolean call() throws Exception {
                            return BackendService.registerForEvent(currentUser.email, e.title);
                        }
                    };
                    rt.setOnSucceeded(rsev -> {
                        boolean ok = rt.getValue();
                        if (ok) { reg.setText("Registered"); alert("Registered for " + e.title); }
                        else { reg.setDisable(false); alert("Could not register."); }
                    });
                    rt.setOnFailed(rf -> { reg.setDisable(false); alert("Registration failed."); });
                    new Thread(rt).start();
                });
                Region spacerRow = new Region();
                HBox.setHgrow(spacerRow, Priority.ALWAYS);
                row.getChildren().addAll(info, spacerRow, reg);
                list.getItems().add(row);
            }
            pi.setVisible(false);
        });
        t.setOnFailed(ev -> { alert("Failed to load events."); pi.setVisible(false); });
        new Thread(t).start();
    }

    private void showTeam() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(12));
        Text header = new Text("Teams");
        header.setFont(Font.font(18));

        ListView<Text> list = new ListView<>();
        list.setPrefHeight(420);

        pane.getChildren().addAll(header, list);
        setContent(pane);

        Task<List<BackendService.Team>> t = new Task<>() {
            @Override
            protected List<BackendService.Team> call() throws Exception {
                return BackendService.getTeams();
            }
        };
        t.setOnSucceeded(ev -> {
            list.getItems().clear();
            for (BackendService.Team tm : t.getValue()) {
                list.getItems().add(new Text(tm.name + " — Coach: " + tm.coach));
            }
        });
        t.setOnFailed(ev -> alert("Failed to load teams."));
        new Thread(t).start();
    }

    private void showProfile() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(12));
        Text header = new Text("Profile");
        header.setFont(Font.font(18));

        if (currentUser == null) { setContent(new VBox(new Text("Not logged in."))); return; }

        Text me = new Text("Email: " + currentUser.email);
        Text role = new Text("Role: " + currentUser.role);

        ListView<HBox> regs = new ListView<>();
        regs.setPrefHeight(240);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> {
            regs.getItems().clear();
            List<BackendService.Event> my = BackendService.getUserEvents(currentUser.email);
            for (BackendService.Event ev : my) {
                HBox row = new HBox(12);
                Text t = new Text(ev.title + " (" + ev.date + ")");
                Button un = new Button("Unregister");
                un.setOnAction(ae -> {
                    boolean ok = BackendService.unregisterFromEvent(currentUser.email, ev.title);
                    if (ok) { alert("Unregistered from " + ev.title); refresh.fire(); }
                    else alert("Unregister failed.");
                });
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(t, sp, un);
                regs.getItems().add(row);
            }
        });

        pane.getChildren().addAll(header, me, role, new Text("Your registrations:"), regs, refresh);
        setContent(pane);
        refresh.fire();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Adapter backend wired to the domain model (InMemoryDatabase, Coach, Player, Sport, Registration)
    static class BackendService {
        static class User { String email; String password; String role; User(String e, String p, String r) {email=e;password=p;role=r;} }
        static class Event { String title; String date; Event(String t, String d){title=t;date=d;} }
        static class Team { String name; String coach; Team(String n, String c){name=n;coach=c;} }
        static class Result { boolean success; String message; Result(boolean s, String m){success=s;message=m;} }

        // simple credential store and lookups
        private static final Map<String, String> passwords = new HashMap<>(); // email -> password
        // map human-readable event title -> (coach,event) reference
        private static final Map<String, Coach.Event> eventByTitle = new HashMap<>();
        private static final Map<String, Coach> coachByEventTitle = new HashMap<>();

        static {
            // seed a default sport and coach/event if none exist so UI has something to show
            if (InMemoryDatabase.SPORTS.isEmpty()) {
                Sport s = new Sport("Football", 11);
                InMemoryDatabase.SPORTS.put(s.getSportId(), s);
            }
            if (InMemoryDatabase.COACHES.isEmpty()) {
                Sport any = InMemoryDatabase.SPORTS.values().iterator().next();
                Coach demo = new Coach("C-1", "Demo Coach", "Demo", "Coach", "coach@example.com", "");
                InMemoryDatabase.COACHES.put(demo.getId(), demo);
                InMemoryDatabase.PERSONS.put(demo.getId(), demo);
                // seed demo credentials so the pre-seeded coach can log in from UI (password: demo)
                passwords.put("coach@example.com", "demo");
                // also create a demo player account
                Player demoPlayer = new Player("Demo","Player","player@example.com","" );
                InMemoryDatabase.PLAYERS.put(demoPlayer.getId(), demoPlayer);
                InMemoryDatabase.PERSONS.put(demoPlayer.getId(), demoPlayer);
                passwords.put("player@example.com", "demo");
                Coach.Event ev = demo.createEvent(any, java.time.LocalDate.now().plusDays(7), "Main Field");
                String title = formatTitle(demo, ev);
                eventByTitle.put(title, ev);
                coachByEventTitle.put(title, demo);
            } else {
                // build lookup from existing coaches
                rebuildEventLookup();
            }
        }

        private static void rebuildEventLookup() {
            eventByTitle.clear(); coachByEventTitle.clear();
            for (Coach coach : InMemoryDatabase.COACHES.values()) {
                for (Coach.Event ev : coach.getEvents()) {
                    String title = formatTitle(coach, ev);
                    eventByTitle.put(title, ev);
                    coachByEventTitle.put(title, coach);
                }
            }
        }

        private static String formatTitle(Coach coach, Coach.Event ev) {
            String sportName = "";
            if (ev.getSportId() != null) {
                Sport sp = InMemoryDatabase.SPORTS.get(ev.getSportId());
                if (sp != null) sportName = sp.getSportName();
            }
            return String.format("%s - %s - %s", sportName.isEmpty() ? "Event" : sportName, coach.getName(), ev.getId());
        }

        static Result registerUser(String email, String password, String role) {
            System.out.println("[BackendService] registerUser called: " + email + " role=" + role);
            simulateDelay(200);
            synchronized (passwords) {
                if (passwords.containsKey(email)) {
                    System.out.println("[BackendService] registerUser failed - already registered: " + email);
                    return new Result(false, "Email already registered");
                }
                passwords.put(email, password);
                // create domain object
                String namePart = email.split("@")[0];
                String[] parts = namePart.split("[._-]", 2);
                String first = parts.length > 0 ? parts[0] : namePart;
                String last = parts.length > 1 ? parts[1] : "";
                if ("Coach".equalsIgnoreCase(role)) {
                    Coach c = new Coach("C-" + java.util.UUID.randomUUID().toString().substring(0,6), namePart, first, last, email, "");
                    InMemoryDatabase.COACHES.put(c.getId(), c);
                    InMemoryDatabase.PERSONS.put(c.getId(), c);
                    System.out.println("[BackendService] created coach: " + c.getEmail() + " id=" + c.getId());
                    // rebuild events lookup
                    for (Coach.Event ev : c.getEvents()) {
                        String title = formatTitle(c, ev);
                        eventByTitle.put(title, ev);
                        coachByEventTitle.put(title, c);
                    }
                } else {
                    Player p = new Player(first, last, email, "");
                    InMemoryDatabase.PLAYERS.put(p.getId(), p);
                    InMemoryDatabase.PERSONS.put(p.getId(), p);
                    System.out.println("[BackendService] created player: " + p.getEmail() + " id=" + p.getId());
                }
                System.out.println("[BackendService] registerUser succeeded: " + email);
                return new Result(true, "OK");
            }
        }

        static User loginUser(String email, String password) {
            System.out.println("[BackendService] loginUser called: " + email);
            simulateDelay(120);
            String pw = passwords.get(email);
            if (pw == null) { System.out.println("[BackendService] loginUser: no such user: " + email); return null; }
            if (!Objects.equals(pw, password)) { System.out.println("[BackendService] loginUser: wrong password for " + email); return null; }
            // determine role
            Person p = findPersonByEmail(email);
            String role = "Player";
            if (p instanceof Coach) role = "Coach";
            System.out.println("[BackendService] loginUser success: " + email + " role=" + role);
            return new User(email, password, role);
        }

        private static Person findPersonByEmail(String email) {
            System.out.println("[BackendService] findPersonByEmail: " + email);
            for (Person p : InMemoryDatabase.PERSONS.values()) {
                if (p == null) continue;
                String e = p.getEmail();
                if (email.equals(e)) { System.out.println("[BackendService] findPersonByEmail -> found id=" + p.getId()); return p; }
            }
            System.out.println("[BackendService] findPersonByEmail -> not found: " + email);
            return null;
        }

        static List<Event> getEvents() {
            simulateDelay(80);
            rebuildEventLookup();
            List<Event> out = new ArrayList<>();
            for (Map.Entry<String, Coach.Event> e : eventByTitle.entrySet()) {
                Coach.Event ev = e.getValue();
                String date = ev.getDate() != null ? ev.getDate().toString() : "TBD";
                out.add(new Event(e.getKey(), date));
            }
            return out;
        }

        static List<Team> getTeams() {
            simulateDelay(60);
            List<Team> out = new ArrayList<>();
            for (Coach c : InMemoryDatabase.COACHES.values()) {
                out.add(new Team(c.getName(), c.getCoachId()));
            }
            return out;
        }

        static boolean registerForEvent(String email, String eventTitle) {
            System.out.println("[BackendService] registerForEvent called: " + email + " -> " + eventTitle);
            simulateDelay(60);
            Person p = findPersonByEmail(email);
            if (p == null) { System.out.println("[BackendService] registerForEvent: no person for " + email); return false; }
            Coach.Event ev = eventByTitle.get(eventTitle);
            Coach coach = coachByEventTitle.get(eventTitle);
            if (ev == null || coach == null) { System.out.println("[BackendService] registerForEvent: no event/coach found for title: " + eventTitle); return false; }
            Player player = null;
            if (p instanceof Player) player = (Player)p;
            else {
                // create a lightweight player record for this email
                Player newP = new Player(p.getFirstName(), p.getLastName(), p.getEmail(), p.getPhone());
                InMemoryDatabase.PLAYERS.put(newP.getId(), newP);
                InMemoryDatabase.PERSONS.put(newP.getId(), newP);
                player = newP;
                System.out.println("[BackendService] registerForEvent: created lightweight player id=" + newP.getId());
            }
            boolean added = player.registerForEvent(coach, ev);
            System.out.println("[BackendService] registerForEvent result=" + added);
            return added;
        }

        static boolean unregisterFromEvent(String email, String eventTitle) {
            System.out.println("[BackendService] unregisterFromEvent called: " + email + " -> " + eventTitle);
            simulateDelay(60);
            Person p = findPersonByEmail(email);
            if (p == null) { System.out.println("[BackendService] unregisterFromEvent: no person for " + email); return false; }
            Coach.Event ev = eventByTitle.get(eventTitle);
            Coach coach = coachByEventTitle.get(eventTitle);
            if (ev == null || coach == null) { System.out.println("[BackendService] unregisterFromEvent: no event/coach for title " + eventTitle); return false; }
            if (!(p instanceof Player)) { System.out.println("[BackendService] unregisterFromEvent: person is not a Player: " + email); return false; }
            Player player = (Player)p;
            boolean ok = player.cancelRegistration(coach, ev);
            System.out.println("[BackendService] unregisterFromEvent result=" + ok);
            return ok;
        }

        static boolean isUserRegistered(String email, String eventTitle) {
            Person p = findPersonByEmail(email);
            if (p == null) return false;
            Coach.Event ev = eventByTitle.get(eventTitle);
            if (ev == null) return false;
            for (java.util.UUID id : ev.getRegistrationRequests()) if (InMemoryDatabase.PERSONS.get(id) == p || InMemoryDatabase.PERSONS.get(id) != null && InMemoryDatabase.PERSONS.get(id).getEmail().equals(email)) return true;
            // also check lineup
            for (java.util.UUID id : ev.getLineup()) {
                Person per = InMemoryDatabase.PERSONS.get(id);
                if (per != null && email.equals(per.getEmail())) return true;
            }
            return false;
        }

        static List<Event> getUserEvents(String email) {
            simulateDelay(40);
            Person p = findPersonByEmail(email);
            List<Event> out = new ArrayList<>();
            if (p == null) return out;
            rebuildEventLookup();
            for (Map.Entry<String, Coach.Event> en : eventByTitle.entrySet()) {
                String title = en.getKey();
                Coach.Event ev = en.getValue();
                boolean contains = false;
                for (java.util.UUID id : ev.getRegistrationRequests()) {
                    Person per = InMemoryDatabase.PERSONS.get(id);
                    if (per != null && email.equals(per.getEmail())) { contains = true; break; }
                }
                for (java.util.UUID id : ev.getLineup()) {
                    Person per = InMemoryDatabase.PERSONS.get(id);
                    if (per != null && email.equals(per.getEmail())) { contains = true; break; }
                }
                if (contains) out.add(new Event(title, ev.getDate() != null ? ev.getDate().toString() : "TBD"));
            }
            return out;
        }

        private static void simulateDelay(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
    }
}

