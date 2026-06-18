public class InMemoryDatabase {
    public static final java.util.Map<java.util.UUID, Person> PERSONS = new java.util.LinkedHashMap<>();
    public static final java.util.Map<java.util.UUID, Player> PLAYERS = new java.util.LinkedHashMap<>();
    public static final java.util.Map<java.util.UUID, Coach> COACHES = new java.util.LinkedHashMap<>();
    public static final java.util.Map<java.util.UUID, Sport> SPORTS = new java.util.LinkedHashMap<>();
    public static final java.util.Map<java.util.UUID, Registration> REGISTRATIONS = new java.util.LinkedHashMap<>();

    private InMemoryDatabase() {}
}
