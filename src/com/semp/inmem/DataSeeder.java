package com.semp.inmem;

import com.semp.inmem.repos.*;
import java.time.LocalDate;

public class DataSeeder {
    public static void seed(UserRepository userRepo, SportRepository sportRepo, EventRepository eventRepo, RegistrationRepository regRepo) {
        if (sportRepo.findAll().isEmpty()) {
            Sport soccer = new Sport("Soccer", 11); sportRepo.save(soccer);
            Sport basketball = new Sport("Basketball", 5); sportRepo.save(basketball);
        }
        // seed demo users
        if (userRepo.findByEmail("coach@example.com").isEmpty()) {
            User coach = new User("Demo","Coach","coach@example.com","demo", Role.COACH);
            userRepo.save(coach);
        }
        if (userRepo.findByEmail("player@example.com").isEmpty()) {
            User player = new User("Demo","Player","player@example.com","demo", Role.PLAYER);
            userRepo.save(player);
        }
        // seed an event
        Sport sp = sportRepo.findByName("Soccer").orElseGet(()->sportRepo.findAll().get(0));
        User coach = userRepo.findByEmail("coach@example.com").orElse(null);
        if (coach != null && eventRepo.findAll().isEmpty()) {
            SportEvent ev = new SportEvent(sp.getId(), coach.getId(), "Summer Football Cup", LocalDate.now().plusDays(30), LocalDate.now().plusDays(20), "Main Field", EventStatus.OPEN);
            eventRepo.save(ev);
            // register demo player
            userRepo.findByEmail("player@example.com").ifPresent(p -> regRepo.save(new Registration(p.getId(), ev.getEventId(), RegistrationStatus.CONFIRMED)));
        }
    }
}
