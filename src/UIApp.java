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
                    System.out.println("[UI] Login attempt: " + em + " (pwd length=" + (pw!=null?pw.length():0) + ")");
                    if (em.isEmpty() || pw.isEmpty()) { alert("Please fill email and password."); return; }

                    btnLogin.setDisable(true);
                    pi.setVisible(true);

                    Task<BackendService.User> t = new Task<>() {
                        @Override
                        protected BackendService.User call() throws Exception {
                            System.out.println("[UI] backend login call for: " + em);
                            BackendService.User res = BackendService.loginUser(em, pw);
                            System.out.println("[UI] backend returned: " + (res==null?"null":res.email));
                            return res;
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
                    try {
                        showDashboard();
                    } catch (Exception ex) {
                        alert("Error showing dashboard: " + ex.toString());
                        ex.printStackTrace();
                    }
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
        action.setOnAction(e -> showEventDetail(title));

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

    // Detailed event view (uses backend event/coach objects)
    private void showEventDetail(String title) {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(12));
        Text header = new Text(title);
        header.setFont(Font.font(18));

        // fetch domain objects
        com.semp.inmem.SportEvent ev = BackendService.getEventByTitle(title);
        com.semp.inmem.User coach = BackendService.getCoachForEvent(title);
        if (ev == null || coach == null) { alert("Event not found"); return; }

        Text date = new Text("Date: " + (ev.getEventDate() != null ? ev.getEventDate().toString() : "TBD"));
        Text loc = new Text("Location: " + (ev.getLocation() != null ? ev.getLocation() : "TBD"));
        Text status = new Text("Status: " + (ev.getStatus() != null ? ev.getStatus().name() : "N/A"));

        ListView<HBox> regs = new ListView<>();
        regs.setPrefHeight(200);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> {
            regs.getItems().clear();
            // show pending registration requests
            for (com.semp.inmem.User person : BackendService.getRegistrationsForEvent(title)) {
                String name = person.getDisplayName();
                HBox row = new HBox(8);
                Text t = new Text(name + "");
                Button assign = new Button("Confirm");
                assign.setOnAction(ae -> {
                    boolean ok = BackendService.confirmRegistration(person.getId(), ev.getEventId());
                    if (ok) { alert("Confirmed " + name); refresh.fire(); }
                    else alert("Confirm failed.");
                });
                row.getChildren().addAll(t, assign);
                regs.getItems().add(row);
            }
        });

        pane.getChildren().addAll(header, date, loc, status, new Text("Registration requests:"), regs, refresh);
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

    // Adapter backend using the testcase in-memory model (com.semp.inmem)
    static class BackendService {
        static class User { String email; String password; String role; User(String e, String p, String r) {email=e;password=p;role=r;} }
        static class Event { String title; String date; Event(String t, String d){title=t;date=d;} }
        static class Team { String name; String coach; Team(String n, String c){name=n;coach=c;} }
        static class Result { boolean success; String message; Result(boolean s, String m){success=s;message=m;} }

        private static final com.semp.inmem.repos.UserRepository userRepo = new com.semp.inmem.repos.UserRepository();
        private static final com.semp.inmem.repos.SportRepository sportRepo = new com.semp.inmem.repos.SportRepository();
        private static final com.semp.inmem.repos.EventRepository eventRepo = new com.semp.inmem.repos.EventRepository();
        private static final com.semp.inmem.repos.RegistrationRepository regRepo = new com.semp.inmem.repos.RegistrationRepository();

        // title (formatted) -> eventId
        private static final Map<String, Long> titleToEventId = new HashMap<>();

        static {
            // seed demo data
            com.semp.inmem.DataSeeder.seed(userRepo, sportRepo, eventRepo, regRepo);
            rebuildLookup();
        }

        private static void rebuildLookup() {
            titleToEventId.clear();
            for (com.semp.inmem.SportEvent ev : eventRepo.findAll()) {
                String title = formatTitle(ev);
                titleToEventId.put(title, ev.getEventId());
            }
        }

        private static String formatTitle(com.semp.inmem.SportEvent ev) {
            String sportName = sportRepo.findById(ev.getSportId()).map(s->s.getName()).orElse("Event");
            String coachName = userRepo.findById(ev.getCoachId()).map(u->u.getDisplayName()).orElse("Coach");
            String date = ev.getEventDate() != null ? ev.getEventDate().toString() : "TBD";
            return String.format("%s - %s - %s", sportName, coachName, date);
        }

        static Result registerUser(String email, String password, String role) {
            simulateDelay(200);
            if (userRepo.findByEmail(email).isPresent()) return new Result(false, "Email already registered");
            // simple name split
            String namePart = email.split("@")[0];
            String[] parts = namePart.split("[._-]", 2);
            String first = parts.length>0?parts[0]:namePart;
            String last = parts.length>1?parts[1]:"";
            com.semp.inmem.Role r = "Coach".equalsIgnoreCase(role) ? com.semp.inmem.Role.COACH : com.semp.inmem.Role.PLAYER;
            com.semp.inmem.User u = new com.semp.inmem.User(first, last, email, password, r);
            userRepo.save(u);
            rebuildLookup();
            return new Result(true, "OK");
        }

        static User loginUser(String email, String password) {
            simulateDelay(80);
            var opt = userRepo.findByEmail(email);
            if (opt.isEmpty()) return null;
            com.semp.inmem.User u = opt.get();
            if (!Objects.equals(u.getPassword(), password)) return null;
            return new User(u.getEmail(), u.getPassword(), u.getRole().name());
        }

        static List<Event> getEvents() {
            simulateDelay(40);
            rebuildLookup();
            List<Event> out = new ArrayList<>();
            for (Map.Entry<String, Long> en : titleToEventId.entrySet()) {
                Long id = en.getValue();
                var ev = eventRepo.findById(id).orElse(null);
                if (ev == null) continue;
                String date = ev.getEventDate() != null ? ev.getEventDate().toString() : "TBD";
                out.add(new Event(en.getKey(), date));
            }
            return out;
        }

        static com.semp.inmem.SportEvent getEventByTitle(String title) {
            rebuildLookup();
            Long id = titleToEventId.get(title);
            if (id==null) return null;
            return eventRepo.findById(id).orElse(null);
        }

        static com.semp.inmem.User getCoachForEvent(String title) {
            var ev = getEventByTitle(title);
            if (ev==null) return null;
            return userRepo.findById(ev.getCoachId()).orElse(null);
        }

        static List<com.semp.inmem.User> getRegistrationsForEvent(String title) {
            var ev = getEventByTitle(title);
            List<com.semp.inmem.User> out = new ArrayList<>();
            if (ev==null) return out;
            for (com.semp.inmem.Registration r : regRepo.findByEventId(ev.getEventId())) {
                if (r.getStatus() == com.semp.inmem.RegistrationStatus.PENDING) {
                    userRepo.findById(r.getPlayerId()).ifPresent(out::add);
                }
            }
            return out;
        }

        static boolean confirmRegistration(Long playerId, Long eventId) {
            var opt = regRepo.findByPlayerAndEvent(playerId, eventId);
            if (opt.isEmpty()) return false;
            com.semp.inmem.Registration r = opt.get();
            r.setStatus(com.semp.inmem.RegistrationStatus.CONFIRMED);
            regRepo.save(r);
            return true;
        }

        static List<Team> getTeams() {
            // simplify: show coaches as teams
            List<Team> out = new ArrayList<>();
            for (com.semp.inmem.User u : userRepo.findAll()) if (u.getRole()==com.semp.inmem.Role.COACH) out.add(new Team(u.getDisplayName(), u.getEmail()));
            return out;
        }

        static boolean registerForEvent(String email, String eventTitle) {
            simulateDelay(60);
            var userOpt = userRepo.findByEmail(email);
            com.semp.inmem.User user;
            if (userOpt.isEmpty()) {
                // create simple player account
                String[] parts = email.split("@")[0].split("[._-]");
                String first = parts.length>0?parts[0]:email;
                user = new com.semp.inmem.User(first, "", email, "", com.semp.inmem.Role.PLAYER);
                userRepo.save(user);
            } else user = userOpt.get();
            var ev = getEventByTitle(eventTitle);
            if (ev==null) return false;
            var existing = regRepo.findByPlayerAndEvent(user.getId(), ev.getEventId());
            if (existing.isPresent()) {
                com.semp.inmem.Registration r = existing.get();
                if (r.getStatus()!=com.semp.inmem.RegistrationStatus.CANCELLED) return false;
                r.setStatus(com.semp.inmem.RegistrationStatus.PENDING);
                regRepo.save(r);
                return true;
            }
            com.semp.inmem.Registration reg = new com.semp.inmem.Registration(user.getId(), ev.getEventId(), com.semp.inmem.RegistrationStatus.PENDING);
            regRepo.save(reg);
            return true;
        }

        static boolean unregisterFromEvent(String email, String eventTitle) {
            simulateDelay(60);
            var userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty()) return false;
            var ev = getEventByTitle(eventTitle);
            if (ev==null) return false;
            var existing = regRepo.findByPlayerAndEvent(userOpt.get().getId(), ev.getEventId());
            if (existing.isEmpty()) return false;
            com.semp.inmem.Registration r = existing.get();
            r.setStatus(com.semp.inmem.RegistrationStatus.CANCELLED);
            regRepo.save(r);
            return true;
        }

        static boolean isUserRegistered(String email, String eventTitle) {
            var userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty()) return false;
            var ev = getEventByTitle(eventTitle);
            if (ev==null) return false;
            var existing = regRepo.findByPlayerAndEvent(userOpt.get().getId(), ev.getEventId());
            if (existing.isEmpty()) return false;
            return existing.get().getStatus() != com.semp.inmem.RegistrationStatus.CANCELLED;
        }

        static List<Event> getUserEvents(String email) {
            var out = new ArrayList<Event>();
            var userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty()) return out;
            var regs = regRepo.findByPlayerId(userOpt.get().getId());
            for (com.semp.inmem.Registration r : regs) {
                if (r.getStatus() == com.semp.inmem.RegistrationStatus.CANCELLED) continue;
                var ev = eventRepo.findById(r.getEventId()).orElse(null);
                if (ev!=null) out.add(new Event(formatTitle(ev), ev.getEventDate()!=null?ev.getEventDate().toString():"TBD"));
            }
            return out;
        }

        private static void simulateDelay(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
    }
}


