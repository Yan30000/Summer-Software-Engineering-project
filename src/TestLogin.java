public class TestLogin {
    public static void main(String[] args) {
        System.out.println("Testing login for coach@example.com / demo");
        UIApp.BackendService.User u = UIApp.BackendService.loginUser("coach@example.com", "demo");
        System.out.println("Result: " + (u == null ? "null" : (u.email + " role=" + u.role)));
        System.out.println("Testing login for player@example.com / demo");
        UIApp.BackendService.User p = UIApp.BackendService.loginUser("player@example.com", "demo");
        System.out.println("Result: " + (p == null ? "null" : (p.email + " role=" + p.role)));
    }
}