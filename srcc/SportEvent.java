public class SportEvent {
    private int eventId;
    private String eventName;
    private String eventDate;
    private String registrationDeadLine;
    private String status;
    private String sportType;

    public SportEvent(int eventId, String eventName, String eventDate,
                      String registrationDeadLine, String status, String sportType) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.registrationDeadLine = registrationDeadLine;
        this.status = status;
        this.sportType = sportType;
    }

    public void openRegistration() {
        status = "Registration Open";
        System.out.println("Registration is now open for " + eventName);
    }

    public void closeRegistration() {
        status = "Registration Closed";
        System.out.println("Registration is now closed for " + eventName);
    }

    public void publishEvent() {
        status = "Published";
        System.out.println("Event published: " + eventName);
    }

    public void cancelEvent() {
        status = "Cancelled";
        System.out.println("Event cancelled: " + eventName);
    }

    public void displayEvent() {
        System.out.println("Event ID: " + eventId);
        System.out.println("Event Name: " + eventName);
        System.out.println("Event Date: " + eventDate);
        System.out.println("Registration Deadline: " + registrationDeadLine);
        System.out.println("Status: " + status);
        System.out.println("Sport Type: " + sportType);
    }
}