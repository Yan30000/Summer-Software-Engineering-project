import java.util.HashMap;
import java.util.UUID;

public class InMemoryDatabase {
    // Stores all people in the system, including coaches and players
    public static HashMap<UUID, Person> PERSONS = new HashMap<>();

    // Stores all coaches in the system
    public static HashMap<UUID, Coach> COACHES = new HashMap<>();

    // Stores all players in the system
    public static HashMap<UUID, Player> PLAYERS = new HashMap<>();

    // Stores all sports in the system
    public static HashMap<UUID, Sport> SPORTS = new HashMap<>();

    // Stores all registrations in the system
    public static HashMap<UUID, Registration> REGISTRATIONS = new HashMap<>();

    // Private constructor prevents creating an object from this class
    private InMemoryDatabase() {
    }
}