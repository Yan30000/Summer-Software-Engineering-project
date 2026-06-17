import java.util.ArrayList;

public class Formation {
    // Unique ID for each formation
    private int formationId;

    // Name of the formation, for example: 4-4-2, 3-2-1, 2-3-1
    private String formationName;

    // Total number of positions allowed in this formation
    private int numberOfPositions;

    // List that stores all positions in this formation
    private ArrayList<Position> positions;

    // Constructor: used to create a new Formation object
    public Formation(int formationId, String formationName, int numberOfPositions) {
        this.formationId = formationId;
        this.formationName = formationName;
        this.numberOfPositions = numberOfPositions;
        this.positions = new ArrayList<>();
    }

    // Adds a position to the formation
    public void addPosition(Position position) {
        // A formation should not have more positions than the number allowed
        if (positions.size() < numberOfPositions) {
            positions.add(position);
            System.out.println(position.getPositionName() + " added to formation " + formationName);
        } else {
            System.out.println("Formation already has the maximum number of positions.");
        }
    }

    // Removes a position from the formation using the position name
    public void removePosition(String positionName) {
        Position positionToRemove = null;

        // Search for the position by name
        for (Position position : positions) {
            if (position.getPositionName().equalsIgnoreCase(positionName)) {
                positionToRemove = position;
                break;
            }
        }

        // If the position was found, remove it
        if (positionToRemove != null) {
            positions.remove(positionToRemove);
            System.out.println(positionName + " removed from formation " + formationName);
        } else {
            System.out.println("Position not found in this formation.");
        }
    }

    // Displays all positions inside the formation
    public void displayPosition() {
        System.out.println("\nFormation ID: " + formationId);
        System.out.println("Formation Name: " + formationName);
        System.out.println("Number of Positions: " + numberOfPositions);

        if (positions.isEmpty()) {
            System.out.println("No positions have been added yet.");
        } else {
            System.out.println("\nPositions in this formation:");
            for (Position position : positions) {
                position.displayPositionDetails();
            }
        }
    }

    // Getter method for formationId
    public int getFormationId() {
        return formationId;
    }

    // Getter method for formationName
    public String getFormationName() {
        return formationName;
    }

    // Getter method for numberOfPositions
    public int getNumberOfPositions() {
        return numberOfPositions;
    }

    // Getter method for the list of positions
    public ArrayList<Position> getPositions() {
        return positions;
    }
}