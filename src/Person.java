import java.util.UUID;

public abstract class Person {
    // Unique ID for each person in the system
    private UUID id;

    // Person's first name
    private String firstName;

    // Person's last name
    private String lastName;

    // Person's email address
    private String email;

    // Person's phone number
    private String phone;

    // Constructor: used to create a new Person object
    public Person(String firstName, String lastName, String email, String phone) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    // Getter method for id
    public UUID getId() {
        return id;
    }

    // Getter method for firstName
    public String getFirstName() {
        return firstName;
    }

    // Setter method for firstName
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Getter method for lastName
    public String getLastName() {
        return lastName;
    }

    // Setter method for lastName
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Getter method for email
    public String getEmail() {
        return email;
    }

    // Setter method for email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter method for phone
    public String getPhone() {
        return phone;
    }

    // Setter method for phone
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Returns the full name of the person
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    // Displays the person's basic details
    public void displayPersonInfo() {
        System.out.println("Person ID: " + id);
        System.out.println("Name: " + getDisplayName());
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
    }

    // Converts the person object to readable text
    @Override
    public String toString() {
        return getDisplayName() + " (Email: " + email + ", Phone: " + phone + ")";
    }
}