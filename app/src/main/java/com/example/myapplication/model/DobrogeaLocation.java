package com.example.myapplication.model;

public class DobrogeaLocation {
    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private int pointsValue;

    public DobrogeaLocation(String name, double lat, double lng, String desc, int points) {
        this.name = name;
        this.latitude = lat;
        this.longitude = lng;
        this.description = desc;
        this.pointsValue = points;
    }

    // Getters and setters
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public int getPointsValue() { return pointsValue; }
}
