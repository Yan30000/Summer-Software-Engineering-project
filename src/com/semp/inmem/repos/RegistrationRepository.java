package com.semp.inmem.repos;

import com.semp.inmem.Registration;
import com.semp.inmem.RegistrationStatus;
import java.util.*;

public class RegistrationRepository {
    private final Map<Long, Registration> map = new LinkedHashMap<>();

    public synchronized Registration save(Registration r) { map.put(r.getId(), r); return r; }
    public synchronized List<Registration> findByEventId(Long eventId) {
        List<Registration> out = new ArrayList<>();
        for (Registration r : map.values()) if (r.getEventId().equals(eventId)) out.add(r);
        return out;
    }
    public synchronized List<Registration> findByPlayerId(Long playerId) {
        List<Registration> out = new ArrayList<>();
        for (Registration r : map.values()) if (r.getPlayerId().equals(playerId)) out.add(r);
        return out;
    }
    public synchronized Optional<Registration> findByPlayerAndEvent(Long playerId, Long eventId) {
        return map.values().stream().filter(r->r.getPlayerId().equals(playerId) && r.getEventId().equals(eventId)).findFirst();
    }
}
