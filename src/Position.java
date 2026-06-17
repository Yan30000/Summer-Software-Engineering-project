public class Position {
    // Unique ID for each position
    private int positionId;

    // Name of the position, for example: Striker, Goalkeeper, Defender
    private String positionName;

    // Checks if this position already has a player assigned
    private boolean isOccupied;

    // Stores the name of the player assigned to this position
    private String assignedPlayer;

    // Constructor: used to create a new Position object
    public Position(int positionId, String positionName) {
        this.positionId = positionId;
        this.positionName = positionName;

        // At the beginning, the position is empty
        this.isOccupied = false;
        this.assignedPlayer = "";
    }

    // Assigns a player to this position
    public void assignPlayer(String playerName) {
        // A player can only be assigned if the position is not already occupied
        if (!isOccupied) {
            assignedPlayer = playerName;
            isOccupied = true;
            System.out.println(playerName + " assigned to " + positionName);
        } else {
            System.out.println("Position is already occupied by " + assignedPlayer);
        }
    }

    // Removes the assigned player from this position
    public void removePlayer() {
        // Only remove a player if someone is currently assigned
        if (isOccupied) {
            System.out.println(assignedPlayer + " removed from " + positionName);
            assignedPlayer = "";
            isOccupied = false;
        } else {
            System.out.println("No player is assigned to this position.");
        }
    }

    // Displays the details of this position
    public void displayPositionDetails() {
        System.out.println("Position ID: " + positionId);
        System.out.println("Position Name: " + positionName);
        System.out.println("Occupied: " + isOccupied);

        // Only show assigned player if the position is occupied
        if (isOccupied) {
            System.out.println("Assigned Player: " + assignedPlayer);
        }

        System.out.println();
    }

    // Getter method for positionId
    public int getPositionId() {
        return positionId;
    }

    // Getter method for positionName
    public String getPositionName() {
        return positionName;
    }

    // Getter method to check if the position is occupied
    public boolean isOccupied() {
        return isOccupied;
    }

    // Getter method for assignedPlayer
    public String getAssignedPlayer() {
        return assignedPlayer;
    }
}