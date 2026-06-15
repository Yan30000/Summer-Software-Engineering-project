public class Player extends Person {
    // external player id assigned when registering to a coach's event list
    private String playerId;
    // the position the player wishes to play (for coach to consider)
    private String assignedPosition;

    public Player(String firstName, String lastName, String email, String phone) {
        super(firstName, lastName, email, phone);
    }

    public String getPlayerId() { return playerId; }
    private void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getAssignedPosition() { return assignedPosition; }
    public void setAssignedPosition(String assignedPosition) { this.assignedPosition = assignedPosition; }

    /**
     * registerForEvent(): view coach events and add player to the event's registration requests.
     * Assigns a playerId on first registration (coach-specific id).
     */
    public boolean registerForEvent(Coach coach, Coach.Event event) {
        if (coach == null || event == null) return false;
        // simple check: ensure event belongs to coach
        boolean owns = coach.getEvents().stream().anyMatch(ev -> ev.getId().equals(event.getId()));
        if (!owns) return false;
        // assign a playerId if not already assigned
        if (this.playerId == null) this.setPlayerId("P-" + java.util.UUID.randomUUID().toString());
        boolean added = event.addRegistration(this.getId());
        if (added) {
            // ensure player is discoverable globally
            InMemoryDatabase.PERSONS.put(this.getId(), this);
            InMemoryDatabase.PLAYERS.put(this.getId(), this);
            // create a Registration record for tracking
            Registration reg = new Registration(this.getId(), event.getSportId(), Registration.Role.PLAYER, java.time.LocalDate.now());
            InMemoryDatabase.REGISTRATIONS.put(reg.getId(), reg);
        }
        return added;
    }

    /**
     * cancelRegistration(): the player cancels their pending registration for an event
     */
    public boolean cancelRegistration(Coach coach, Coach.Event event) {
        if (coach == null || event == null) return false;
        boolean owns = coach.getEvents().stream().anyMatch(ev -> ev.getId().equals(event.getId()));
        if (!owns) return false;
        boolean removed = event.removeRegistration(this.getId());
        // find corresponding registration record(s) and cancel
        for (Registration reg : InMemoryDatabase.REGISTRATIONS.values()) {
            if (reg.getPersonId().equals(this.getId()) && reg.getSportId().equals(event.getSportId())
                && reg.getStatus() != Registration.Status.CANCELLED) {
                reg.cancelRegistration();
            }
        }
        return removed;
    }

    /**
     * viewLineUp(): if the event is finalized, return the ordered lineup with formation positions.
     */
    public java.util.List<String> viewLineUp(Coach.Event event) {
        java.util.List<String> view = new java.util.ArrayList<>();
        if (event == null) return view;
        if (!event.isFinalized()) {
            view.add("Lineup not yet finalized");
            return view;
        }
        java.util.List<java.util.UUID> lineup = event.getLineup();
        java.util.List<String> formation = event.getFormation();
        for (int i = 0; i < lineup.size(); i++) {
            java.util.UUID pid = lineup.get(i);
            Person person = InMemoryDatabase.PERSONS.get(pid);
            String display = (person != null) ? person.getDisplayName() : pid.toString();
            String pos = (i < formation.size()) ? formation.get(i) : "Substitute";
            String desired = "";
            if (person instanceof Player) {
                String asp = ((Player) person).getAssignedPosition();
                if (asp != null && !asp.isEmpty()) desired = " (wants: " + asp + ")";
            }
            view.add(String.format("%d. %s - %s%s", i+1, display, pos, desired));
        }
        return view;
    }

    @Override
    public String toString() {
        return String.format("Player[name=%s, playerId=%s, desired=%s, person=%s]", getDisplayName(), playerId, assignedPosition, super.toString());
    }
}
