import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Coach extends Person {
    // Coach ID that can be shown to players
    private String coachId;

    // Coach display name
    private String name;

    // Stores all events created by this coach
    private HashMap<UUID, Event> events;

    // Constructor: used to create a new Coach object
    public Coach(String coachId, String name, String firstName, String lastName, String email, String phone) {
        super(firstName, lastName, email, phone);
        this.coachId = coachId;
        this.name = name;
        this.events = new HashMap<>();
    }

    // Getter method for coachId
    public String getCoachId() {
        return coachId;
    }

    // Getter method for coach name
    public String getName() {
        return name;
    }

    // Getter method for all events created by this coach
    public Collection<Event> getEvents() {
        return events.values();
    }

    // Creates a new event for a sport
    public Event createEvent(Sport sport, LocalDate date, String location) {
        // A sport and date are required to create an event
        if (sport == null || date == null) {
            throw new IllegalArgumentException("Sport and date are required.");
        }

        Event event = new Event(sport.getSportId(), date, location);
        events.put(event.getId(), event);

        System.out.println("Event created by " + name);
        return event;
    }

    // Allows the coach to select a formation for an event
    public void selectFormation(Event event, List<String> formation) {
        // Event must exist before selecting formation
        if (event == null) {
            throw new IllegalArgumentException("Event is required.");
        }

        // Formation cannot be changed after the lineup is finalized
        if (event.isFinalized()) {
            System.out.println("Cannot select formation. Lineup is already finalized.");
            return;
        }

        event.setFormation(formation);
        System.out.println("Formation selected: " + formation);
    }

    // Assigns a player to the event lineup
    public boolean assignPlayer(Event event, Player player) {
        // Event and player must exist
        if (event == null || player == null) {
            return false;
        }

        // Players cannot be assigned after lineup is finalized
        if (event.isFinalized()) {
            System.out.println("Cannot assign player. Lineup is finalized.");
            return false;
        }

        boolean assigned = event.addToLineup(player.getId());

        if (assigned) {
            System.out.println(player.getDisplayName() + " assigned to lineup.");
        } else {
            System.out.println(player.getDisplayName() + " is already in the lineup.");
        }

        return assigned;
    }

    // Allows the coach to modify the formation before finalizing
    public boolean modifyFormation(Event event, List<String> newFormation) {
        if (event == null || event.isFinalized()) {
            System.out.println("Cannot modify formation.");
            return false;
        }

        event.setFormation(newFormation);
        System.out.println("Formation modified: " + newFormation);
        return true;
    }

    // Finalizes the lineup
    public boolean finalizeLineUp(Event event) {
        if (event == null) {
            return false;
        }

        event.setFinalized(true);
        System.out.println("Lineup finalized by coach.");
        return true;
    }

    // Sends the lineup to players
    // This is simulated using printed messages instead of real emails
    public List<String> sendLineUp(Event event) {
        List<String> sentEmails = new ArrayList<>();

        if (event == null) {
            return sentEmails;
        }

        for (UUID playerId : event.getLineup()) {
            Person person = InMemoryDatabase.PERSONS.get(playerId);

            if (person != null) {
                String email = person.getEmail();

                System.out.println("Sending lineup to " + person.getDisplayName() + " at " + email);

                if (email != null && !email.isEmpty()) {
                    sentEmails.add(email);
                }
            }
        }

        return sentEmails;
    }

    // Displays coach details
    public void displayCoachInfo() {
        System.out.println("Coach ID: " + coachId);
        System.out.println("Coach Name: " + name);
        displayPersonInfo();
    }

    // Converts the coach object to readable text
    @Override
    public String toString() {
        return "Coach: " + name + ", Coach ID: " + coachId;
    }

    // Inner Event class used by Coach, Player, and Registration
    public static class Event {
        // Unique ID for each event
        private UUID id;

        // ID of the sport connected to this event
        private UUID sportId;

        // Date of the event
        private LocalDate date;

        // Location where the event will take place
        private String location;

        // List of position names selected by the coach
        private List<String> formation;

        // List of players selected for the lineup
        private List<UUID> lineup;

        // List of players who requested to register
        private List<UUID> registrationRequests;

        // Checks if the lineup has been finalized
        private boolean finalized;

        // Constructor: used to create a new event
        public Event(UUID sportId, LocalDate date, String location) {
            this.id = UUID.randomUUID();
            this.sportId = sportId;
            this.date = date;
            this.location = location;
            this.formation = new ArrayList<>();
            this.lineup = new ArrayList<>();
            this.registrationRequests = new ArrayList<>();
            this.finalized = false;
        }

        // Getter method for event id
        public UUID getId() {
            return id;
        }

        // Getter method for sportId
        public UUID getSportId() {
            return sportId;
        }

        // Getter method for date
        public LocalDate getDate() {
            return date;
        }

        // Getter method for location
        public String getLocation() {
            return location;
        }

        // Getter method for formation
        public List<String> getFormation() {
            return formation;
        }

        // Setter method for formation
        public void setFormation(List<String> formation) {
            if (formation == null) {
                this.formation = new ArrayList<>();
            } else {
                this.formation = new ArrayList<>(formation);
            }
        }

        // Getter method for lineup
        public List<UUID> getLineup() {
            return lineup;
        }

        // Getter method for registration requests
        public List<UUID> getRegistrationRequests() {
            return registrationRequests;
        }

        // Getter method for finalized
        public boolean isFinalized() {
            return finalized;
        }

        // Setter method for finalized
        public void setFinalized(boolean finalized) {
            this.finalized = finalized;
        }

        // Adds a player registration request
        public boolean addRegistration(UUID playerId) {
            if (playerId == null) {
                return false;
            }

            if (registrationRequests.contains(playerId)) {
                return false;
            }

            registrationRequests.add(playerId);
            return true;
        }

        // Removes a player registration request
        public boolean removeRegistration(UUID playerId) {
            if (playerId == null) {
                return false;
            }

            lineup.remove(playerId);
            return registrationRequests.remove(playerId);
        }

        // Adds a player to the lineup
        public boolean addToLineup(UUID playerId) {
            if (playerId == null) {
                return false;
            }

            if (lineup.contains(playerId)) {
                return false;
            }

            lineup.add(playerId);
            return true;
        }

        // Converts the event object to readable text
        @Override
        public String toString() {
            return "Event ID: " + id +
                    ", Sport ID: " + sportId +
                    ", Date: " + date +
                    ", Location: " + location +
                    ", Finalized: " + finalized;
        }
    }
}