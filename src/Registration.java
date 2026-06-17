import java.time.LocalDate;
import java.util.UUID;

public class Registration {
    // Role shows whether the registered person is a player or coach
    public enum Role {
        PLAYER,
        COACH
    }

    // Status shows the current state of the registration
    public enum Status {
        PENDING,
        REGISTERED,
        REJECTED,
        CANCELLED
    }

    // Unique ID for each registration
    private UUID registrationId;

    // ID of the person who is registering
    private UUID personId;

    // ID of the sport connected to this registration
    private UUID sportId;

    // Role of the person registering
    private Role role;

    // Date when the registration was created
    private LocalDate registrationDate;

    // Current status of the registration
    private Status status;

    // Constructor: used to create a new Registration object
    public Registration(UUID personId, UUID sportId, Role role, LocalDate registrationDate) {
        this.registrationId = UUID.randomUUID();
        this.personId = personId;
        this.sportId = sportId;
        this.role = role;

        // If no date is provided, use today's date
        if (registrationDate == null) {
            this.registrationDate = LocalDate.now();
        } else {
            this.registrationDate = registrationDate;
        }

        // New registrations start as pending
        this.status = Status.PENDING;
    }

    // Getter method for registrationId
    public UUID getRegistrationId() {
        return registrationId;
    }

    // This method is included so other classes can call getId()
    public UUID getId() {
        return registrationId;
    }

    // Getter method for personId
    public UUID getPersonId() {
        return personId;
    }

    // Getter method for sportId
    public UUID getSportId() {
        return sportId;
    }

    // Getter method for role
    public Role getRole() {
        return role;
    }

    // Getter method for registrationDate
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    // This method matches your teammate's sample code
    public LocalDate getDate() {
        return registrationDate;
    }

    // Getter method for status
    public Status getStatus() {
        return status;
    }

    // Setter method for status
    public void setStatus(Status status) {
        this.status = status;
    }

    // Confirms the registration
    public void register() {
        this.status = Status.REGISTERED;
        System.out.println("Registration confirmed.");
    }

    // Cancels the registration
    public void cancelRegistration() {
        this.status = Status.CANCELLED;
        System.out.println("Registration cancelled.");
    }

    // Displays registration details
    public void displayRegistration() {
        System.out.println("Registration ID: " + registrationId);
        System.out.println("Person ID: " + personId);
        System.out.println("Sport ID: " + sportId);
        System.out.println("Role: " + role);
        System.out.println("Registration Date: " + registrationDate);
        System.out.println("Status: " + status);
    }

    // Converts the registration object to readable text
    @Override
    public String toString() {
        return "Registration ID: " + registrationId +
                ", Person ID: " + personId +
                ", Sport ID: " + sportId +
                ", Role: " + role +
                ", Date: " + registrationDate +
                ", Status: " + status;
    }
}