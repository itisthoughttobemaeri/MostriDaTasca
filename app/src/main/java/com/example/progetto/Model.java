package com.example.progetto;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Model {

    private static final Model ourInstance = new Model();
    public static Model getInstance() {
        return ourInstance;
    }

    private String username;
    private String id;
    private int LP;
    private int XP;
    private String image;
    private double latitude;
    private double longitude;

    private ArrayList<Monster> monsters;
    private ArrayList<Candy> candies;
    private User[] users;

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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addLP(int points) {
        this.LP = this.LP + points;
    }

    public void addXP(int points) {
        this.XP = this.XP + points;
    }

    public void removeLP(int points) {
        this.LP = this.LP - points;
    }

    public void removeXP(int points) {
        this.XP = this.XP - points;
    }

    public void die(){
        this.LP = 100;
        this.XP = 0;
    }

    public Monster getMonsterById(int id) {
        for (Monster m : monsters) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public Candy getCandyById(int id) {
        for (Candy c : candies) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public User getUserById(String id) {
        for (int i=0; i<20; i++) {
            if (users[i].getId().equals(id)) {
                return users[i];
            }
        }
        return null;
    }

    public void refreshCandies(ArrayList<Candy> candies) {
        this.candies = candies;
    }

    public void refreshMonsters(ArrayList<Monster> monsters) {
        this.monsters = monsters;
    }

    public void refreshUsers(User[] users){
        this.users = users;
    }

    public boolean isDistanceCandyOk(int id) {
        Candy c = getCandyById(id);
        double x = Math.abs(this.longitude - c.getLon());
        double y = Math.abs(this.latitude - c.getLat());
        double value = Math.sqrt((x*x) + (y*y));
        if (value < 50) {
            return true;
        }
        return false;
    }

    public boolean isDistanceMonsterOk(int id) {
        Monster m = getMonsterById(id);
        double x = Math.abs(this.longitude - m.getLon());
        double y = Math.abs(this.latitude - m.getLat());
        double value = Math.sqrt((x*x) + (y*y));
        if (value < 50) {
            return true;
        }
        return false;
    }
}
