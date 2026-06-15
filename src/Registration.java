public class Registration {
    public enum Role { PLAYER, COACH }
    public enum Status { PENDING, REGISTERED, REJECTED, CANCELLED }

    private java.util.UUID id; // registrationId
    private java.util.UUID personId;
    private java.util.UUID sportId;
    private Role role;
    private java.time.LocalDate date; // registrationDate
    private Status status;

    public Registration(java.util.UUID personId, java.util.UUID sportId, Role role, java.time.LocalDate date) {
        this.id = java.util.UUID.randomUUID();
        this.personId = personId;
        this.sportId = sportId;
        this.role = role;
        this.date = date != null ? date : java.time.LocalDate.now();
        this.status = Status.PENDING;
    }

    public java.util.UUID getId() { return id; }
    public java.util.UUID getPersonId() { return personId; }
    public java.util.UUID getSportId() { return sportId; }
    public Role getRole() { return role; }
    public java.time.LocalDate getDate() { return date; }
    public java.time.LocalDate getRegistrationDate() { return date; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    /**
     * register(): mark registration as registered/confirmed
     */
    public void register() {
        this.status = Status.REGISTERED;
        if (this.date == null) this.date = java.time.LocalDate.now();
    }

    /**
     * cancelRegistration(): cancel this registration
     */
    public void cancelRegistration() {
        this.status = Status.CANCELLED;
    }

    @Override
    public String toString() {
        return String.format("Registration[id=%s, person=%s, sport=%s, role=%s, date=%s, status=%s]",
            id, personId, sportId, role, date, status);
    }
}
