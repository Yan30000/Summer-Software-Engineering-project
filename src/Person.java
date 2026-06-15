public abstract class Person {
    private java.util.UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    public Person(String firstName, String lastName, String email, String phone) {
        this.id = java.util.UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public java.util.UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Human-friendly display name used across the system (e.g., for listing coaches/players).
     */
    public String getDisplayName() { return String.format("%s %s", firstName != null ? firstName : "", lastName != null ? lastName : "").trim(); }

    @Override
    public String toString() {
        return String.format("%s %s (email=%s, phone=%s, id=%s)", firstName, lastName, email, phone, id);
    }
}
