import java.util.UUID;

public class Sport {
    // Unique ID for each sport
    private UUID sportId;

    // Name of the sport, for example: Soccer, Basketball, Volleyball
    private String sportName;

    // Maximum number of players allowed for this sport
    private int maxPlayers;

    // Constructor: used to create a new Sport object
    public Sport(String sportName, int maxPlayers) {
        this.sportId = UUID.randomUUID();
        this.sportName = sportName;
        this.maxPlayers = maxPlayers;
    }

    // Getter method for sportId
    public UUID getSportId() {
        return sportId;
    }

    // This method is added so other classes can also call getId()
    public UUID getId() {
        return sportId;
    }

    // Getter method for sportName
    public String getSportName() {
        return sportName;
    }

    // Setter method for sportName
    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    // Getter method for maxPlayers
    public int getMaxPlayers() {
        return maxPlayers;
    }

    // This method matches the sample code your teammate had
    public int getMaximumPlayer() {
        return maxPlayers;
    }

    // Setter method for maxPlayers
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    // Returns simple rules based on the sport name
    public String getSportRules() {
        if (sportName == null) {
            return "No rules available.";
        }

        switch (sportName.toLowerCase()) {
            case "football":
            case "soccer":
                return "11 players per team, 90 minutes, offside rules apply.";

            case "basketball":
                return "5 players per team on court, 4 quarters, shot clock rules apply.";

            case "volleyball":
                return "6 players per team on court, best-of-5 sets.";

            default:
                return "Standard sport rules apply. Maximum players: " + maxPlayers + ".";
        }
    }

    // Displays sport details
    public void displaySport() {
        System.out.println("Sport ID: " + sportId);
        System.out.println("Sport Name: " + sportName);
        System.out.println("Maximum Players: " + maxPlayers);
        System.out.println("Rules: " + getSportRules());
    }

    // Converts the sport object to readable text
    @Override
    public String toString() {
        return "Sport Name: " + sportName + ", Maximum Players: " + maxPlayers;
    }
}