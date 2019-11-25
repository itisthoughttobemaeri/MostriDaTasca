package com.example.progetto;

public class Candy {
    private String name;
    private int LP;
    private String size;
    private String image;
    private int id;
    private double lat;
    private double lon;

    public Candy (String name, int LP, String image, String size) {
        this.name = name;
        this.size = size;
        this.image = image;
        this.LP = LP;
    }

    public String getSize() {
        return size;
    }

    public String getImage() {
        return image;
    }

    public int getLP() {
        return LP;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getId() {
        return id;
    }
}

