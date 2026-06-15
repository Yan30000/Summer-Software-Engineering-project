public class Sport {
    private java.util.UUID sportId; // sportId
    private String sportName;      // SportName
    private int maxPlayers;        // maxPlayers

    public Sport(String sportName, int maxPlayers) {
        this.sportId = java.util.UUID.randomUUID();
        this.sportName = sportName;
        this.maxPlayers = maxPlayers;
    }

    // keep getId for backward compatibility
    public java.util.UUID getId() { return sportId; }
    public java.util.UUID getSportId() { return sportId; }

    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }

    public int getMaximumPlayer() { return maxPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    /**
     * getSportRules(): returns a short human-readable summary of rules for this sport.
     */
    public String getSportRules() {
        if (sportName == null) return "No rules available.";
        switch (sportName.toLowerCase()) {
            case "football":
            case "soccer":
                return "11 players per team, 90 minutes (two halves), offside rules apply.";
            case "basketball":
                return "5 players per team on court, 4 quarters, shot clock rules apply.";
            case "volleyball":
                return "6 players per team on court, best-of-5 sets, rally scoring.";
            case "handball":
                return "7 players per team, two 30-minute halves.";
            default:
                return "Standard sport rules apply. Maximum players: " + maxPlayers + ".";
        }
    }

    @Override
    public String toString() {
        return String.format("Sport[name=%s, maxPlayers=%d, id=%s]", sportName, maxPlayers, sportId);
    }
}
