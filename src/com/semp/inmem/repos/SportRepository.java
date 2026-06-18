package com.semp.inmem.repos;

import com.semp.inmem.Sport;
import java.util.*;

public class SportRepository {
    private final Map<Long, Sport> map = new LinkedHashMap<>();
    public synchronized Sport save(Sport s) { map.put(s.getId(), s); return s; }
    public synchronized Optional<Sport> findById(Long id) { return Optional.ofNullable(map.get(id)); }
    public synchronized Optional<Sport> findByName(String name) { return map.values().stream().filter(s->s.getName().equalsIgnoreCase(name)).findFirst(); }
    public synchronized List<Sport> findAll() { return new ArrayList<>(map.values()); }
}
