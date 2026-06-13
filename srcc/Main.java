public class Main {
    public static void main(String[] args) {
        // ==============================
        // TESTING SPORT EVENT
        // ==============================

        // Create a sport event
        SportEvent event = new SportEvent(
                1,
                "Sunday Soccer Match",
                "2026-06-20",
                "2026-06-18",
                "Registration Open",
                "Soccer"
        );

        // Display event details
        event.displayEvent();

        // Test event actions
        event.closeRegistration();
        event.publishEvent();


        // ==============================
        // TESTING POSITION AND FORMATION
        // ==============================

        // Create positions for the formation
        Position striker = new Position(1, "Striker");
        Position midfielder = new Position(2, "Midfielder");
        Position defender = new Position(3, "Defender");

        // Create a formation that can hold 3 positions
        Formation formation = new Formation(1, "3-2-1", 3);

        // Add positions to the formation
        formation.addPosition(striker);
        formation.addPosition(midfielder);
        formation.addPosition(defender);

        // Assign players to some positions
        striker.assignPlayer("John");
        midfielder.assignPlayer("Mary");

        // Display the formation and its positions
        formation.displayPosition();


        // ==============================
        // TESTING TEAM
        // ==============================

        // Create a team with a maximum of 3 players
        Team team = new Team(1, "Team A", 3);

        // Add players to the team
        team.addPlayer("John");
        team.addPlayer("Mary");
        team.addPlayer("David");

        // This player should not be added because the team is already full
        team.addPlayer("Peter");

        // Display all players in the team
        team.getPlayer();

        // Remove one player from the team
        team.removePLayer("Mary");

        // Display the players again after removing Mary
        team.getPlayer();


        // ==============================
        // TESTING LINEUP
        // ==============================

        // Create a new lineup
        LineUp lineUp = new LineUp(1);

        // Assign players to positions in the lineup
        lineUp.assignPlayer("John", "Striker");
        lineUp.assignPlayer("David", "Defender");

        // Assign a substitute player
        lineUp.assignSubstitute("Peter");

        // Display the lineup before swapping
        lineUp.displayLineUp();

        // Swap the positions of John and David
        lineUp.swapPlayers("John", "David");

        // Display the lineup after swapping
        lineUp.displayLineUp();

        // Finalize the lineup
        lineUp.finalizeLineUp();

        // Try to assign another player after finalizing
        // This should not work because the lineup is finalized
        lineUp.assignPlayer("Mary", "Midfielder");

        // Display the final lineup
        lineUp.displayLineUp();
    }
}