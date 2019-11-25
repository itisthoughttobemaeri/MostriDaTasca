package com.example.progetto;

public class Monster {
    private String name;
    private int LP;
    private int XP;
    private String image;
    private String size;
    private int id;
    private double lat;
    private double lon;

    public Monster(int id, String name, int LP, int XP, String image, String size) {
        this.name = name;
        this.LP = LP;
        this.XP = XP;
        this.image = image;
        this.size = size;
        this.id = id;
    }

    public String getName() {
        return name;
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

    public String getSize() {
        return size;
    }

    public int getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
