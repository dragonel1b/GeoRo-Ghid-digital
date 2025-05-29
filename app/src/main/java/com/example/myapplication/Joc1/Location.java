package com.example.myapplication.Joc1;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.List;

public class Location {
    private String name;
    private String description;
    private int imageResourceId;
    private double latitude;
    private double longitude;
    private String id;
    private List<String> facts;
    private List<String> historicalEvents;
    private boolean hasQuiz;
    private Bitmap userPhoto;
    private boolean isUnlocked;

    public Location(String name, String description, int imageResourceId) {
        this.name = name;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.facts = new ArrayList<>();
        this.historicalEvents = new ArrayList<>();
        this.hasQuiz = false;
        this.isUnlocked = true;
    }
    
    public Location(String name, String description, double latitude, double longitude, String id) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.facts = new ArrayList<>();
        this.historicalEvents = new ArrayList<>();
        this.hasQuiz = false;
        this.isUnlocked = true;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
    
    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void addFact(String fact) {
        facts.add(fact);
    }
    
    public List<String> getFacts() {
        return facts;
    }
    
    public void addHistoricalEvent(String event) {
        historicalEvents.add(event);
    }
    
    public List<String> getHistoricalEvents() {
        return historicalEvents;
    }
    
    public boolean hasQuiz() {
        return hasQuiz;
    }
    
    public void setHasQuiz(boolean hasQuiz) {
        this.hasQuiz = hasQuiz;
    }
    
    public Bitmap getUserPhoto() {
        return userPhoto;
    }
    
    public void setUserPhoto(Bitmap userPhoto) {
        this.userPhoto = userPhoto;
    }
    
    public boolean isUnlocked() {
        return isUnlocked;
    }
    
    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
    
    /**
     * Calculate distance between this location and another point in meters
     */
    public double distanceTo(double otherLat, double otherLng) {
        final int R = 6371000; // Earth's radius in meters
        
        double latDistance = Math.toRadians(otherLat - latitude);
        double lngDistance = Math.toRadians(otherLng - longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(otherLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Checks if a user is near this location (within specified range in meters)
     */
    public boolean isNearby(double userLat, double userLng, int rangeInMeters) {
        return distanceTo(userLat, userLng) <= rangeInMeters;
    }
}
