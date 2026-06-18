package com.semp.inmem;

public class User {
    private static long NEXT = 1;
    private final Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;

    public User(String firstName, String lastName, String email, String password, Role role) {
        this.id = NEXT++;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public String getDisplayName() { return (firstName==null?"":firstName) + " " + (lastName==null?"":lastName); }
}
