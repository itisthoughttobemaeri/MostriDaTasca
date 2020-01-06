package com.example.progetto;

public class User {
    private String username;
    private String id;
    private int lp;
    private int xp;
    private String img;

    // Class used for rankings

    public String getUsername() {
        return username;
    }

    public int getLP() {
        return lp;
    }

    public int getXP() {
        return xp;
    }

    public String getImage() {
        return img;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return username + " " + lp + " " + xp;
    }

}
