package com.example.progetto;

public class User {
    private String username;
    private String id;
    private int LP;
    private int XP;
    private String image;

    public User (String id) {
        this.id = id;
        this.LP = 100;
        this.XP = 0;
    }

    public String getUsername() {
        return username;
    }

    public int getLP() {
        return LP;
    }

    public int getXP() {
        return XP;
    }

    public String getImage() {
        return image;
    }

    public String getId() {
        return id;
    }
}
