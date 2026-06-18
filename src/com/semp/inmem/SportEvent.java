package com.semp.inmem;

import java.time.LocalDate;

public class SportEvent {
    private static long NEXT = 1;
    private final Long eventId;
    private Long sportId;
    private Long coachId;
    private String eventName;
    private LocalDate eventDate;
    private LocalDate registrationDeadline;
    private String location;
    private EventStatus status;

    public SportEvent(Long sportId, Long coachId, String eventName, LocalDate eventDate, LocalDate registrationDeadline, String location, EventStatus status) {
        this.eventId = NEXT++;
        this.sportId = sportId;
        this.coachId = coachId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.registrationDeadline = registrationDeadline;
        this.location = location;
        this.status = status;
    }

    public Long getEventId() { return eventId; }
    public Long getSportId() { return sportId; }
    public Long getCoachId() { return coachId; }
    public String getEventName() { return eventName; }
    public LocalDate getEventDate() { return eventDate; }
    public LocalDate getRegistrationDeadline() { return registrationDeadline; }
    public String getLocation() { return location; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus s) { this.status = s; }
}
