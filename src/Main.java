import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" SPORT EVENT PLANNER SYSTEM (SEPS) - DEMO");
        System.out.println("=================================================");

        // =====================================================
        // 1. CREATE A SPORT
        // =====================================================
        System.out.println("\n1. CREATING SPORT");

        Sport soccer = new Sport("Soccer", 11);
        InMemoryDatabase.SPORTS.put(soccer.getSportId(), soccer);

        soccer.displaySport();


        // =====================================================
        // 2. CREATE A COACH
        // =====================================================
        System.out.println("\n2. CREATING COACH");

        Coach coach = new Coach(
                "C-001",
                "Coach Daniel",
                "Daniel",
                "Smith",
                "coach.daniel@example.com",
                "+1-506-111-2222"
        );

        InMemoryDatabase.COACHES.put(coach.getId(), coach);
        InMemoryDatabase.PERSONS.put(coach.getId(), coach);

        coach.displayCoachInfo();


        // =====================================================
        // 3. COACH CREATES A SPORT EVENT
        // =====================================================
        System.out.println("\n3. COACH CREATES SPORT EVENT");

        SportEvent sportEvent = new SportEvent(
                1,
                "Friday Evening Soccer Match",
                "2026-06-20",
                "2026-06-18",
                "Registration Open",
                "Soccer"
        );

        sportEvent.displayEvent();

        Coach.Event coachEvent = coach.createEvent(
                soccer,
                LocalDate.of(2026, 6, 20),
                "UNBSJ Soccer Field"
        );


        // =====================================================
        // 4. CREATE PLAYERS
        // =====================================================
        System.out.println("\n4. CREATING PLAYERS");

        Player player1 = new Player("John", "Brown", "john@example.com", "+1-506-333-1111");
        Player player2 = new Player("Mary", "White", "mary@example.com", "+1-506-333-2222");
        Player player3 = new Player("David", "Green", "david@example.com", "+1-506-333-3333");
        Player player4 = new Player("Peter", "Black", "peter@example.com", "+1-506-333-4444");

        player1.setAssignedPosition("Striker");
        player2.setAssignedPosition("Midfielder");
        player3.setAssignedPosition("Defender");
        player4.setAssignedPosition("Substitute");

        InMemoryDatabase.PLAYERS.put(player1.getId(), player1);
        InMemoryDatabase.PERSONS.put(player1.getId(), player1);

        InMemoryDatabase.PLAYERS.put(player2.getId(), player2);
        InMemoryDatabase.PERSONS.put(player2.getId(), player2);

        InMemoryDatabase.PLAYERS.put(player3.getId(), player3);
        InMemoryDatabase.PERSONS.put(player3.getId(), player3);

        InMemoryDatabase.PLAYERS.put(player4.getId(), player4);
        InMemoryDatabase.PERSONS.put(player4.getId(), player4);

        player1.displayPlayerInfo();
        player2.displayPlayerInfo();
        player3.displayPlayerInfo();
        player4.displayPlayerInfo();


        // =====================================================
        // 5. PLAYERS REGISTER FOR EVENT
        // =====================================================
        System.out.println("\n5. PLAYERS REGISTER FOR EVENT");

        player1.registerForEvent(coach, coachEvent);
        player2.registerForEvent(coach, coachEvent);
        player3.registerForEvent(coach, coachEvent);
        player4.registerForEvent(coach, coachEvent);

        System.out.println("\nRegistration Requests:");
        System.out.println(coachEvent.getRegistrationRequests());

        System.out.println("\nRegistration Records:");
        for (Registration registration : InMemoryDatabase.REGISTRATIONS.values()) {
            System.out.println(registration);
        }


        // =====================================================
        // 6. COACH CREATES TEAM
        // =====================================================
        System.out.println("\n6. COACH CREATES TEAM");

        Team team = new Team(1, "Team A", 3);

        team.addPlayer(player1.getDisplayName());
        team.addPlayer(player2.getDisplayName());
        team.addPlayer(player3.getDisplayName());

        // Peter is not added because the team is already full
        team.addPlayer(player4.getDisplayName());

        team.getPlayer();


        // =====================================================
        // 7. COACH SELECTS FORMATION AND POSITIONS
        // =====================================================
        System.out.println("\n7. COACH SELECTS FORMATION AND POSITIONS");

        Formation formation = new Formation(1, "3-2-1", 3);

        Position striker = new Position(1, "Striker");
        Position midfielder = new Position(2, "Midfielder");
        Position defender = new Position(3, "Defender");

        formation.addPosition(striker);
        formation.addPosition(midfielder);
        formation.addPosition(defender);

        striker.assignPlayer(player1.getDisplayName());
        midfielder.assignPlayer(player2.getDisplayName());
        defender.assignPlayer(player3.getDisplayName());

        formation.displayPosition();

        List<String> coachFormation = Arrays.asList("Striker", "Midfielder", "Defender");
        coach.selectFormation(coachEvent, coachFormation);


        // =====================================================
        // 8. COACH ASSIGNS PLAYERS TO LINEUP
        // =====================================================
        System.out.println("\n8. COACH ASSIGNS PLAYERS TO LINEUP");

        LineUp lineUp = new LineUp(1);

        lineUp.assignPlayer(player1.getDisplayName(), "Striker");
        lineUp.assignPlayer(player2.getDisplayName(), "Midfielder");
        lineUp.assignPlayer(player3.getDisplayName(), "Defender");
        lineUp.assignSubstitute(player4.getDisplayName());

        coach.assignPlayer(coachEvent, player1);
        coach.assignPlayer(coachEvent, player2);
        coach.assignPlayer(coachEvent, player3);

        lineUp.displayLineUp();


        // =====================================================
        // 9. COACH MODIFIES LINEUP BEFORE FINALIZING
        // =====================================================
        System.out.println("\n9. COACH MODIFIES LINEUP BEFORE FINALIZING");

        lineUp.swapPlayers(player1.getDisplayName(), player3.getDisplayName());
        lineUp.displayLineUp();


        // =====================================================
        // 10. COACH FINALIZES AND SENDS LINEUP
        // =====================================================
        System.out.println("\n10. COACH FINALIZES AND SENDS LINEUP");

        lineUp.finalizeLineUp();
        coach.finalizeLineUp(coachEvent);

        sportEvent.closeRegistration();
        sportEvent.publishEvent();

        System.out.println("\nSending lineup to selected players:");
        coach.sendLineUp(coachEvent);


        // =====================================================
        // 11. PLAYERS VIEW FINALIZED LINEUP
        // =====================================================
        System.out.println("\n11. PLAYERS VIEW FINALIZED LINEUP");

        for (String lineupText : player1.viewLineUp(coachEvent)) {
            System.out.println(lineupText);
        }


        // =====================================================
        // 12. PLAYER CANCELS REGISTRATION EXAMPLE
        // =====================================================
        System.out.println("\n12. PLAYER CANCELS REGISTRATION EXAMPLE");

        Player extraPlayer = new Player("Kevin", "Stone", "kevin@example.com", "+1-506-333-5555");

        InMemoryDatabase.PLAYERS.put(extraPlayer.getId(), extraPlayer);
        InMemoryDatabase.PERSONS.put(extraPlayer.getId(), extraPlayer);

        extraPlayer.registerForEvent(coach, coachEvent);
        extraPlayer.cancelRegistration(coach, coachEvent);

        System.out.println("\nUpdated Registration Requests:");
        System.out.println(coachEvent.getRegistrationRequests());


        // =====================================================
        // 13. DEMO COMPLETE
        // =====================================================
        System.out.println("\n=================================================");
        System.out.println(" SEPS DEMO COMPLETED SUCCESSFULLY");
        System.out.println("=================================================");
    }
}