package com.example.progetto;

public class ShownObject {
    private int id;

    private double lat;
    private double lon;

    private String type;
    private String size;
    private String name;

    private String image;

    public int getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public ShownObject(int id, double lat, double lon, String type, String size, String name, String image) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.size = size;
        this.type = type;
        this.image = image;
    }

    @Override
    public String toString() {
        return "ShownObject: " +
                "id=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", type='" + type + '\'' +
                ", size='" + size + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image;
    }
}
