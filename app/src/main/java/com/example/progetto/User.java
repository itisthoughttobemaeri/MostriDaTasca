package com.example.progetto;

import androidx.annotation.NonNull;

public class User {
    private String username;
    private String id;
    private int LP;
    private int XP;
    private String image;

    // Class used for both profile and rankings

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

    @Override
    public String toString() {
        return username + " " + LP + " " + XP;
    }
}
