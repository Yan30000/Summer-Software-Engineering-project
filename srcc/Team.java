import java.util.ArrayList;

public class Team {
    // Unique ID for each team
    private int teamId;

    // Name of the team, for example: Team A or Team B
    private String teamName;

    // Maximum number of players allowed in the team
    // The spelling maxPLayer is kept the same as the class diagram
    private int maxPLayer;

    // List that stores the names of players in the team
    private ArrayList<String> players;

    // Constructor: used to create a new Team object
    public Team(int teamId, String teamName, int maxPLayer) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.maxPLayer = maxPLayer;
        this.players = new ArrayList<>();
    }

    // Adds a player to the team
    public void addPlayer(String playerName) {
        // Check if the team still has space
        if (players.size() < maxPLayer) {
            players.add(playerName);
            System.out.println(playerName + " added to team " + teamName);
        } else {
            System.out.println("Team is full. Cannot add more players.");
        }
    }

    // Removes a player from the team
    // The spelling removePLayer is kept the same as the class diagram
    public void removePLayer(String playerName) {
        // Try to remove the player from the list
        if (players.remove(playerName)) {
            System.out.println(playerName + " removed from team " + teamName);
        } else {
            System.out.println(playerName + " was not found in the team.");
        }
    }

    // Displays all players in the team
    public void getPlayer() {
        System.out.println("\nPlayers in Team: " + teamName);

        if (players.isEmpty()) {
            System.out.println("No players in this team yet.");
        } else {
            for (String player : players) {
                System.out.println("- " + player);
            }
        }
    }

    // Getter method for teamId
    public int getTeamId() {
        return teamId;
    }

    // Getter method for teamName
    public String getTeamName() {
        return teamName;
    }

    // Getter method for maxPLayer
    public int getMaxPLayer() {
        return maxPLayer;
    }

    // Getter method for the list of players
    public ArrayList<String> getPlayers() {
        return players;
    }
}