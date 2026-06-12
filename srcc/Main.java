public class Main {
    public static void main(String[] args) {
        SportEvent event = new SportEvent(
                1,
                "Sunday Soccer Match",
                "2026-06-20",
                "2026-06-18",
                "Registration Open",
                "Soccer"
        );

        event.displayEvent();
        event.closeRegistration();
        event.publishEvent();
    }
}