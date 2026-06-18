public class DemoMain {
    public static void main(String[] args) {
        System.out.println("=== Demo: Sport Event Planner backend flow ===\n");

        // Create sport
        Sport basketball = new Sport("Basketball", 12);
        InMemoryDatabase.SPORTS.put(basketball.getSportId(), basketball);
        System.out.println("Created sport: " + basketball + " rules: " + basketball.getSportRules());

        // Create coach
        Coach coach = new Coach("C-123", "Coach Carter", "Samuel", "Carter", "sam.c@example.com", "+100000000");
        InMemoryDatabase.COACHES.put(coach.getId(), coach);
        InMemoryDatabase.PERSONS.put(coach.getId(), coach);
        System.out.println("Created coach: " + coach);

        // Create players
        Player p1 = new Player("Alice","Anderson","alice@example.com","+111");
        Player p2 = new Player("Bob","Brown","bob@example.com","+222");
        Player p3 = new Player("Carol","Clark","carol@example.com","+333");

        // Players register in the system (they will register for events later)
        InMemoryDatabase.PLAYERS.put(p1.getId(), p1);
        InMemoryDatabase.PERSONS.put(p1.getId(), p1);
        InMemoryDatabase.PLAYERS.put(p2.getId(), p2);
        InMemoryDatabase.PERSONS.put(p2.getId(), p2);
        InMemoryDatabase.PLAYERS.put(p3.getId(), p3);
        InMemoryDatabase.PERSONS.put(p3.getId(), p3);

        System.out.println("Players created: ");
        InMemoryDatabase.PLAYERS.values().forEach(pl -> System.out.println("  " + pl));

        // Coach creates an event
        Coach.Event event = coach.createEvent(basketball, java.time.LocalDate.now().plusDays(7), "Main Court");
        System.out.println("\nCoach created event: " + event.getId() + " on " + event.getDate());

        // Players register for the event
        p1.setAssignedPosition("Point Guard");
        p2.setAssignedPosition("Center");
        p3.setAssignedPosition("Shooting Guard");

        System.out.println("\nPlayers registering for event...");
        p1.registerForEvent(coach, event);
        p2.registerForEvent(coach, event);
        p3.registerForEvent(coach, event);

        System.out.println("Registration requests (ids): " + event.getRegistrationRequests());
        System.out.println("Registrations stored: ");
        InMemoryDatabase.REGISTRATIONS.values().forEach(r -> System.out.println("  " + r));

        // Coach selects formation
        java.util.List<String> formation = java.util.Arrays.asList("PG","SG","SF","PF","C");
        coach.selectFormation(event, formation);
        System.out.println("\nCoach selected formation: " + formation);

        // Coach assigns players from registration requests
        System.out.println("Assigning players to lineup...");
        for (java.util.UUID pid : event.getRegistrationRequests()) {
            Person person = InMemoryDatabase.PERSONS.get(pid);
            if (person instanceof Player) {
                coach.assignPlayer(event, (Player) person);
            }
        }
        System.out.println("Lineup after assignment: " + event.getLineup());

        // Finalize lineup
        coach.finalizeLineUp(event);
        System.out.println("Event finalized: " + event.isFinalized());

        // Send lineup (simulated emails)
        System.out.println("\nSending lineup (simulated):");
        java.util.List<String> recipients = coach.sendLineUp(event);
        System.out.println("Emails sent to: " + recipients);

        // Players view lineup
        System.out.println("\nPlayers view the finalized lineup:");
        p1.viewLineUp(event).forEach(System.out::println);

        // Example: player cancels before approval (demonstrate cancel)
        Player p4 = new Player("Derek","Dawson","derek@example.com","+444");
        InMemoryDatabase.PLAYERS.put(p4.getId(), p4);
        InMemoryDatabase.PERSONS.put(p4.getId(), p4);
        p4.setAssignedPosition("Forward");
        p4.registerForEvent(coach, event); // registers as request
        System.out.println("\nNew registration requests: " + event.getRegistrationRequests());
        boolean cancelled = p4.cancelRegistration(coach, event);
        System.out.println("Player p4 cancelled registration: " + cancelled + ", requests now: " + event.getRegistrationRequests());

        System.out.println("\nDemo complete.");
    }
}
