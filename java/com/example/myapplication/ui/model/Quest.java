package com.example.myapplication.ui.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Model class representing a quest/mission in the game.
 */
public class Quest {
    public enum Type {
        CULTURAL("Cultural", "#9C27B0"),
        EXPLORATION("Explorare", "#2196F3"),
        CHALLENGE("Provocare", "#F44336"),
        HISTORICAL("Istoric", "#795548"),
        CULINARY("Culinar", "#FF9800");
        
        private String label;
        private String color;
        
        Type(String label, String color) {
            this.label = label;
            this.color = color;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    public enum Status {
        AVAILABLE,
        ACTIVE,
        COMPLETED,
        LOCKED
    }
    
    private String id;
    private String title;
    private String description;
    private Type type;
    private Status status;
    private int progress;
    private int maxProgress;
    private int reward;
    private String regionId;
    private LatLng position;
    
    public Quest(String id, String title, String description, Type type, Status status, 
                 int progress, int maxProgress, int reward, String regionId, LatLng position) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.reward = reward;
        this.regionId = regionId;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = Math.min(progress, maxProgress);
    }
    
    public void incrementProgress() {
        if (progress < maxProgress) {
            progress++;
        }
    }

    public int getMaxProgress() {
        return maxProgress;
    }
    
    public int getProgressPercent() {
        return (int) (((float) progress / maxProgress) * 100);
    }

    public int getReward() {
        return reward;
    }

    public String getRegionId() {
        return regionId;
    }

    public LatLng getPosition() {
        return position;
    }
} 