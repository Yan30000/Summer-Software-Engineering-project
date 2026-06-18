package com.semp.inmem.repos;

import com.semp.inmem.SportEvent;
import java.util.*;

public class EventRepository {
    private final Map<Long, SportEvent> map = new LinkedHashMap<>();

    public synchronized SportEvent save(SportEvent e) { map.put(e.getEventId(), e); return e; }
    public synchronized Optional<SportEvent> findById(Long id) { return Optional.ofNullable(map.get(id)); }
    public synchronized List<SportEvent> findAll() { return new ArrayList<>(map.values()); }
    public synchronized List<SportEvent> findByCoachId(Long coachId) {
        List<SportEvent> out = new ArrayList<>();
        for (SportEvent e : map.values()) if (e.getCoachId().equals(coachId)) out.add(e);
        return out;
    }
}
