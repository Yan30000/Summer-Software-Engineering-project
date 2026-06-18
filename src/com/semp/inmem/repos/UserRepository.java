package com.semp.inmem.repos;

import com.semp.inmem.User;
import java.util.*;

public class UserRepository {
    private final Map<Long, User> users = new LinkedHashMap<>();

    public synchronized User save(User u) { users.put(u.getId(), u); return u; }
    public synchronized Optional<User> findByEmail(String email) {
        return users.values().stream().filter(x->email.equals(x.getEmail())).findFirst();
    }
    public synchronized Optional<User> findById(Long id) { return Optional.ofNullable(users.get(id)); }
    public synchronized List<User> findAll() { return new ArrayList<>(users.values()); }
}
