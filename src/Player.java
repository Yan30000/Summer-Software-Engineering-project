import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player extends Person {
    // Unique player ID used for display or event registration
    private String playerId;

    // The position the player wants or is assigned to play
    private String assignedPosition;

    // Constructor: used to create a new Player object
    public Player(String firstName, String lastName, String email, String phone) {
        super(firstName, lastName, email, phone);
        this.playerId = "P-" + UUID.randomUUID();
        this.assignedPosition = "";
    }

    // Getter method for playerId
    public String getPlayerId() {
        return playerId;
    }

    // Getter method for assignedPosition
    public String getAssignedPosition() {
        return assignedPosition;
    }

    // Setter method for assignedPosition
    public void setAssignedPosition(String assignedPosition) {
        this.assignedPosition = assignedPosition;
    }

    // Allows a player to register for an event created by a coach
    public boolean registerForEvent(Coach coach, Coach.Event event) {
        // Check that coach and event are not empty
        if (coach == null || event == null) {
            return false;
        }

        // Check that this event belongs to the coach
        boolean eventBelongsToCoach = false;

        for (Coach.Event coachEvent : coach.getEvents()) {
            if (coachEvent.getId().equals(event.getId())) {
                eventBelongsToCoach = true;
                break;
            }
        }

        if (!eventBelongsToCoach) {
            return false;
        }

        // Add this player to the event registration request list
        boolean added = event.addRegistration(this.getId());

        if (added) {
            // Store this player in the temporary database
            InMemoryDatabase.PERSONS.put(this.getId(), this);
            InMemoryDatabase.PLAYERS.put(this.getId(), this);

            // Create a registration record for this player
            Registration registration = new Registration(
                    this.getId(),
                    event.getSportId(),
                    Registration.Role.PLAYER,
                    LocalDate.now()
            );

            InMemoryDatabase.REGISTRATIONS.put(registration.getId(), registration);

            System.out.println(getDisplayName() + " registered for the event.");
        } else {
            System.out.println(getDisplayName() + " is already registered for this event.");
        }

        return added;
    }

    // Allows a player to cancel their registration for an event
    public boolean cancelRegistration(Coach coach, Coach.Event event) {
        // Check that coach and event are not empty
        if (coach == null || event == null) {
            return false;
        }

        // Remove this player from the event registration request list
        boolean removed = event.removeRegistration(this.getId());

        if (removed) {
            // Find this player's registration and mark it as cancelled
            for (Registration registration : InMemoryDatabase.REGISTRATIONS.values()) {
                if (registration.getPersonId().equals(this.getId())
                        && registration.getSportId().equals(event.getSportId())) {
                    registration.cancelRegistration();
                }
            }

            System.out.println(getDisplayName() + " cancelled registration.");
        } else {
            System.out.println(getDisplayName() + " was not registered for this event.");
        }

        return removed;
    }

    // Allows a player to view the finalized lineup
    public List<String> viewLineUp(Coach.Event event) {
        List<String> lineupView = new ArrayList<>();

        // Check that event is not empty
        if (event == null) {
            lineupView.add("No event selected.");
            return lineupView;
        }

        // Players should only view the lineup after it has been finalized
        if (!event.isFinalized()) {
            lineupView.add("Lineup not yet finalized.");
            return lineupView;
        }

        // Get the lineup and formation from the event
        List<UUID> lineup = event.getLineup();
        List<String> formation = event.getFormation();

        // Build a readable lineup list
        for (int i = 0; i < lineup.size(); i++) {
            UUID playerId = lineup.get(i);
            Person person = InMemoryDatabase.PERSONS.get(playerId);

            String playerName;

            if (person != null) {
                playerName = person.getDisplayName();
            } else {
                playerName = playerId.toString();
            }

            String position;

            if (i < formation.size()) {
                position = formation.get(i);
            } else {
                position = "Substitute";
            }

            lineupView.add((i + 1) + ". " + playerName + " - " + position);
        }

        return lineupView;
    }

    // Displays player details
    public void displayPlayerInfo() {
        System.out.println("Player ID: " + playerId);
        displayPersonInfo();
        System.out.println("Assigned Position: " + assignedPosition);
    }

    // Converts the player object to readable text
    @Override
    public String toString() {
        return "Player: " + getDisplayName() +
                ", Player ID: " + playerId +
                ", Assigned Position: " + assignedPosition;
    }
}