package com.semp.inmem;

import java.time.Instant;

public class Registration {
    private static long NEXT = 1;
    private final Long id;
    private Long playerId;
    private Long eventId;
    private Instant registrationDate;
    private RegistrationStatus status;

    public Registration(Long playerId, Long eventId, RegistrationStatus status) {
        this.id = NEXT++;
        this.playerId = playerId;
        this.eventId = eventId;
        this.registrationDate = Instant.now();
        this.status = status;
    }

    public Long getId() { return id; }
    public Long getPlayerId() { return playerId; }
    public Long getEventId() { return eventId; }
    public Instant getRegistrationDate() { return registrationDate; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus s) { this.status = s; }
}
