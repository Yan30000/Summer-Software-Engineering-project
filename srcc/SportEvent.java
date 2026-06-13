public class SportEvent {
    // Unique ID for each sport event
    private int eventId;

    // Name of the event, for example: Sunday Soccer Match
    private String eventName;

    // Date when the event will happen
    private String eventDate;

    // Last date players are allowed to register
    // The spelling registrationDeadLine is kept the same as the class diagram
    private String registrationDeadLine;

    // Current status of the event, for example: Registration Open, Published, Cancelled
    private String status;

    // Type of sport for this event, for example: Soccer, Basketball, Volleyball
    private String sportType;

    // Constructor: used to create a new SportEvent object
    public SportEvent(int eventId, String eventName, String eventDate,
                      String registrationDeadLine, String status, String sportType) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.registrationDeadLine = registrationDeadLine;
        this.status = status;
        this.sportType = sportType;
    }

    // Opens registration for the event
    public void openRegistration() {
        status = "Registration Open";
        System.out.println("Registration is now open for " + eventName);
    }

    // Closes registration for the event
    public void closeRegistration() {
        status = "Registration Closed";
        System.out.println("Registration is now closed for " + eventName);
    }

    // Publishes the event so players can see it
    public void publishEvent() {
        status = "Published";
        System.out.println("Event published: " + eventName);
    }

    // Cancels the event
    public void cancelEvent() {
        status = "Cancelled";
        System.out.println("Event cancelled: " + eventName);
    }

    // Displays the full event details
    public void displayEvent() {
        System.out.println("\nEvent ID: " + eventId);
        System.out.println("Event Name: " + eventName);
        System.out.println("Event Date: " + eventDate);
        System.out.println("Registration Deadline: " + registrationDeadLine);
        System.out.println("Status: " + status);
        System.out.println("Sport Type: " + sportType);
    }

    // Getter method for eventId
    public int getEventId() {
        return eventId;
    }

    // Getter method for eventName
    public String getEventName() {
        return eventName;
    }

    // Getter method for eventDate
    public String getEventDate() {
        return eventDate;
    }

    // Getter method for registrationDeadLine
    public String getRegistrationDeadLine() {
        return registrationDeadLine;
    }

    // Getter method for status
    public String getStatus() {
        return status;
    }

    // Getter method for sportType
    public String getSportType() {
        return sportType;
    }
}