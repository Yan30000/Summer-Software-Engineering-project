import java.time.LocalDate;
import java.util.HashMap;

public class LineUp {
    // Unique ID for each lineup
    private int lineUpId;

    // This checks if the lineup has been finalized
    // If it is finalized, players should not be changed again
    private boolean isFinalized;

    // Stores the date when the lineup was created
    private String creationDate;

    // Stores players and their assigned positions
    // Example: John -> Striker
    private HashMap<String, String> playerPositions;

    // Stores substitute players
    // Example: Peter -> Substitute
    private HashMap<String, String> substitutePlayers;

    // Constructor: used to create a new LineUp object
    public LineUp(int lineUpId) {
        this.lineUpId = lineUpId;

        // When the lineup is first created, it is not finalized yet
        this.isFinalized = false;

        // Automatically stores today's date as the creation date
        this.creationDate = LocalDate.now().toString();

        // Create empty lists for assigned players and substitutes
        this.playerPositions = new HashMap<>();
        this.substitutePlayers = new HashMap<>();
    }

    // Assigns a player to a specific position in the lineup
    public void assignPlayer(String playerName, String positionName) {
        // Players can only be assigned if the lineup is not finalized
        if (!isFinalized) {
            playerPositions.put(playerName, positionName);
            System.out.println(playerName + " assigned to position: " + positionName);
        } else {
            System.out.println("Lineup is finalized. Cannot assign player.");
        }
    }

    // Assigns a player as a substitute
    public void assignSubstitute(String playerName) {
        // Substitutes can only be assigned if the lineup is not finalized
        if (!isFinalized) {
            substitutePlayers.put(playerName, "Substitute");
            System.out.println(playerName + " assigned as substitute.");
        } else {
            System.out.println("Lineup is finalized. Cannot assign substitute.");
        }
    }

    // Swaps the positions of two players
    public void swapPlayers(String playerOne, String playerTwo) {
        // Players can only be swapped if the lineup is not finalized
        if (!isFinalized) {
            // Both players must already be assigned to positions before swapping
            if (playerPositions.containsKey(playerOne) && playerPositions.containsKey(playerTwo)) {
                String tempPosition = playerPositions.get(playerOne);

                playerPositions.put(playerOne, playerPositions.get(playerTwo));
                playerPositions.put(playerTwo, tempPosition);

                System.out.println(playerOne + " and " + playerTwo + " have been swapped.");
            } else {
                System.out.println("Both players must be assigned to positions before swapping.");
            }
        } else {
            System.out.println("Lineup is finalized. Cannot swap players.");
        }
    }

    // Finalizes the lineup
    // After this, players should not be assigned or swapped again
    public void finalizeLineUp() {
        isFinalized = true;
        System.out.println("Lineup finalized successfully.");
    }

    // Displays the full lineup details
    public void displayLineUp() {
        System.out.println("\nLineUp ID: " + lineUpId);
        System.out.println("Creation Date: " + creationDate);
        System.out.println("Finalized: " + isFinalized);

        System.out.println("\nAssigned Players:");
        if (playerPositions.isEmpty()) {
            System.out.println("No players assigned yet.");
        } else {
            for (String player : playerPositions.keySet()) {
                System.out.println(player + " -> " + playerPositions.get(player));
            }
        }

        System.out.println("\nSubstitute Players:");
        if (substitutePlayers.isEmpty()) {
            System.out.println("No substitutes assigned yet.");
        } else {
            for (String player : substitutePlayers.keySet()) {
                System.out.println(player + " -> Substitute");
            }
        }
    }

    // Getter method for lineUpId
    public int getLineUpId() {
        return lineUpId;
    }

    // Getter method to check if the lineup is finalized
    public boolean isFinalized() {
        return isFinalized;
    }

    // Getter method for creationDate
    public String getCreationDate() {
        return creationDate;
    }

    // Getter method for assigned player positions
    public HashMap<String, String> getPlayerPositions() {
        return playerPositions;
    }

    // Getter method for substitute players
    public HashMap<String, String> getSubstitutePlayers() {
        return substitutePlayers;
    }
}