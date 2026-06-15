public class Coach extends Person {
    private String coachId; // lightweight external id visible to players
    private String name;    // display name for roster listings

    // events created by this coach
    private java.util.Map<java.util.UUID, Event> events = new java.util.LinkedHashMap<>();

    public Coach(String coachId, String name, String firstName, String lastName, String email, String phone) {
        super(firstName, lastName, email, phone);
        this.coachId = coachId;
        this.name = name != null ? name : getDisplayName();
    }

    public String getCoachId() { return coachId; }
    public String getName() { return name; }
    public java.util.Collection<Event> getEvents() { return events.values(); }

    @Override
    public String toString() {
        return String.format("Coach[name=%s, coachId=%s, person=%s]", name, coachId, super.toString());
    }

    // --- Event model (simple, nested for now) ---
    public static class Event {
        private java.util.UUID id = java.util.UUID.randomUUID();
        private java.util.UUID sportId;
        private java.time.LocalDate date;
        private String location;
        private java.util.List<String> formation = new java.util.ArrayList<>();
        private java.util.List<java.util.UUID> lineup = new java.util.ArrayList<>();
        private boolean finalized = false;
        // players who requested to register for this event (pending coach assignment)
        private java.util.List<java.util.UUID> registrationRequests = new java.util.ArrayList<>();

        public Event(java.util.UUID sportId, java.time.LocalDate date, String location) {
            this.sportId = sportId;
            this.date = date;
            this.location = location;
        }

        public java.util.UUID getId() { return id; }
        public java.util.UUID getSportId() { return sportId; }
        public java.time.LocalDate getDate() { return date; }
        public String getLocation() { return location; }
        public java.util.List<String> getFormation() { return java.util.Collections.unmodifiableList(formation); }
        public java.util.List<java.util.UUID> getLineup() { return java.util.Collections.unmodifiableList(lineup); }
        public boolean isFinalized() { return finalized; }
        public java.util.List<java.util.UUID> getRegistrationRequests() { return java.util.Collections.unmodifiableList(registrationRequests); }

        /** Add a registration request from a player. Returns true if added, false if already requested. */
        public boolean addRegistration(java.util.UUID playerId) {
            if (playerId == null) return false;
            if (registrationRequests.contains(playerId)) return false;
            registrationRequests.add(playerId);
            return true;
        }

        /** Remove a player's registration request (returns true if removed) */
        public boolean removeRegistration(java.util.UUID playerId) {
            if (playerId == null) return false;
            boolean removed = registrationRequests.remove(playerId);
            // also remove from lineup if present
            if (removed) {
                lineup.remove(playerId);
            }
            return removed;
        }

        @Override
        public String toString() {
            return String.format("Event[id=%s,sport=%s,date=%s,loc=%s,formation=%s,lineup=%s,finalized=%s,requests=%s]",
                id, sportId, date, location, formation, lineup, finalized, registrationRequests);
        }
    }

    // --- Coach actions requested ---

    /**
     * createEvents(): ability to create a game/event for players
     */
    public Event createEvent(Sport sport, java.time.LocalDate date, String location) {
        if (sport == null || date == null) throw new IllegalArgumentException("sport and date required");
        Event e = new Event(sport.getId(), date, location);
        events.put(e.id, e);
        return e;
    }

    /**
     * selectFormation(): choose formation (list of position names)
     */
    public void selectFormation(Event e, java.util.List<String> formation) {
        if (e == null) throw new IllegalArgumentException("event required");
        if (e.finalized) throw new IllegalStateException("event finalized");
        e.formation = new java.util.ArrayList<>(formation != null ? formation : java.util.Collections.emptyList());
        // trim or expand lineup to match formation size (keep existing players at front)
        int size = e.formation.size();
        if (e.lineup.size() > size) {
            e.lineup = new java.util.ArrayList<>(e.lineup.subList(0, size));
        }
    }

    /**
     * assignPlayer(): select who is playing first (adds player to lineup)
     */
    public boolean assignPlayer(Event e, Player p) {
        if (e == null || p == null) return false;
        if (e.finalized) return false;
        if (e.lineup.contains(p.getId())) return false; // already assigned
        // if formation capacity reached, add to end (substitute) or refuse depending on policy; here allow substitutes after capacity
        int capacity = e.formation.size() > 0 ? e.formation.size() : Integer.MAX_VALUE;
        if (e.lineup.size() < capacity) {
            e.lineup.add(p.getId());
        } else {
            e.lineup.add(p.getId()); // substitute
        }
        return true;
    }

    /**
     * modifyFormation(): change the shape of the lineup
     */
    public boolean modifyFormation(Event e, java.util.List<String> newFormation) {
        if (e == null || e.finalized) return false;
        selectFormation(e, newFormation);
        return true;
    }

    /**
     * finalizeLineUp(): confirm game is planned
     */
    public boolean finalizeLineUp(Event e) {
        if (e == null) return false;
        e.finalized = true;
        return true;
    }

    /**
     * sendLineUp(): simulate sending the lineup to each player by printing and returning list of recipient emails
     */
    public java.util.List<String> sendLineUp(Event e) {
        java.util.List<String> sent = new java.util.ArrayList<>();
        if (e == null) return sent;
        for (java.util.UUID pid : e.lineup) {
            Person person = InMemoryDatabase.PERSONS.get(pid);
            if (person != null) {
                String email = person.getEmail();
                String msg = String.format("[Simulated email] Dear %s, you are selected for event %s on %s at %s.",
                    person.getDisplayName(), e.id, e.date, e.location);
                System.out.println(msg + " -> " + email);
                if (email != null && !email.isEmpty()) sent.add(email);
            }
        }
        return sent;
    }
}
