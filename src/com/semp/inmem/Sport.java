package com.semp.inmem;

public class Sport {
    private static long NEXT = 1;
    private final Long id;
    private String name;
    private int maxPlayers;

    public Sport(String name, int maxPlayers) {
        this.id = NEXT++;
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getMaxPlayers() { return maxPlayers; }
}
