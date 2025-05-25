package com.example.myapplication.Joc1;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a mission or quest in the Romanian cultural exploration app
 */
public class Mission {
    // Mission types
    public static final int TYPE_EXPLORATION = 1;
    public static final int TYPE_CULTURAL = 2;
    public static final int TYPE_CULINARY = 3;
    
    // Mission states
    public static final int STATE_AVAILABLE = 0;
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_COMPLETED = 2;
    
    // Mission properties
    private String id;
    private String title;
    private String description;
    private String regionId;
    private int rewardPoints;
    private int type;
    private int state = STATE_AVAILABLE;
    
    // Mission progress
    private List<MissionObjective> objectives = new ArrayList<>();
    private int chapter = 1;
    private int step = 1;
    
    public enum MissionType {
        REACH_LOCATION,
        COLLECT_ITEMS,
        INTERACT_NPC,
        ANSWER_QUIZ,
        TAKE_PHOTO,
        VISIT_ATTRACTIONS
    }
    
    public interface MissionListener {
        void onMissionCompleted(Mission mission);
        void onMissionProgress(Mission mission, float progress);
    }
    
    public static class MissionObjective {
        private String description;
        private boolean completed;
        
        public MissionObjective(String description) {
            this.description = description;
            this.completed = false;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }

    /**
     * Create a new mission
     */
    public Mission(String id, String title, String description, String regionId, int rewardPoints, int type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.regionId = regionId;
        this.rewardPoints = rewardPoints;
        this.type = type;
    }
    
    /**
     * Alternative constructor for story missions
     */
    public Mission(String description, int chapter, int rewardPoints, MissionType missionType, String regionId) {
        this.id = "mission_" + chapter + "_" + System.currentTimeMillis();
        this.title = description;
        this.description = description;
        this.chapter = chapter;
        this.rewardPoints = rewardPoints;
        
        // Convert enum type to int
        switch (missionType) {
            case VISIT_ATTRACTIONS:
                this.type = TYPE_EXPLORATION;
                break;
            case ANSWER_QUIZ:
                this.type = TYPE_CULTURAL;
                break;
            case TAKE_PHOTO:
                this.type = TYPE_EXPLORATION;
                break;
            case INTERACT_NPC:
                this.type = TYPE_CULTURAL;
                break;
            default:
                this.type = TYPE_EXPLORATION;
        }
        
        this.regionId = regionId;
    }
    
    /**
     * Add an objective to this mission
     */
    public void addObjective(String description) {
        objectives.add(new MissionObjective(description));
    }
    
    /**
     * Complete an objective at the given index
     */
    public void completeObjective(int index) {
        if (index >= 0 && index < objectives.size()) {
            objectives.get(index).setCompleted(true);
            
            // Check if all objectives are completed
            boolean allCompleted = true;
            for (MissionObjective objective : objectives) {
                if (!objective.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                setCompleted(true);
                notifyMissionCompleted();
            } else {
                int completedCount = 0;
                for (MissionObjective objective : objectives) {
                    if (objective.isCompleted()) {
                        completedCount++;
                    }
                }
                notifyMissionProgress((float) completedCount / objectives.size());
            }
        }
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getRegionId() {
        return regionId;
    }
    
    public String getCityName() {
        return regionId; // In this implementation, region ID is the city name
    }
    
    public int getRewardPoints() {
        return rewardPoints;
    }
    
    public int getType() {
        return type;
    }
    
    public boolean isActive() {
        return state == STATE_ACTIVE;
    }
    
    public boolean isCompleted() {
        return state == STATE_COMPLETED;
    }
    
    public void setActive(boolean active) {
        state = active ? STATE_ACTIVE : STATE_AVAILABLE;
    }
    
    public void setCompleted(boolean completed) {
        state = completed ? STATE_COMPLETED : STATE_ACTIVE;
    }
    
    public List<MissionObjective> getObjectives() {
        return objectives;
    }
    
    public List<String> getObjectiveTexts() {
        List<String> texts = new ArrayList<>();
        for (MissionObjective objective : objectives) {
            texts.add(objective.getDescription());
        }
        return texts;
    }
    
    public int getChapter() {
        return chapter;
    }
    
    public void setChapter(int chapter) {
        this.chapter = chapter;
    }
    
    public int getStep() {
        return step;
    }
    
    public void setStep(int step) {
        this.step = step;
    }

    // Legacy fields for backwards compatibility
    private float targetX, targetY;
    private boolean isCompleted;
    private int requiredItems;
    private int collectedItems;
    private MissionType missionType;
    private int experienceReward;
    private String cityName;
    private boolean isActive;
    private MissionListener listener;
    private String storyIntro;
    private String storyCompletion;
    private double lat;
    private double lng;

    public Mission(String description, float targetX, float targetY, int rewardPoints, MissionType missionType) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.targetX = targetX;
        this.targetY = targetY;
        this.rewardPoints = rewardPoints;
        this.missionType = missionType;
        this.isCompleted = false;
        this.requiredItems = 0;
        this.collectedItems = 0;
        this.experienceReward = rewardPoints / 2;
        this.isActive = false;
        this.objectives = new ArrayList<>();
        this.chapter = 1;
        this.step = 1;
        this.storyIntro = "";
        this.storyCompletion = "";
    }

    public Mission(String description, int requiredItems, int rewardPoints) {
        this(description, requiredItems, rewardPoints, MissionType.COLLECT_ITEMS);
    }

    public Mission(String description, int requiredItems, int rewardPoints, MissionType missionType) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.requiredItems = requiredItems;
        this.rewardPoints = rewardPoints;
        this.missionType = missionType;
        this.isCompleted = false;
        this.collectedItems = 0;
        this.experienceReward = rewardPoints / 2;
        this.isActive = false;
        this.objectives = new ArrayList<>();
        this.chapter = 1;
        this.step = 1;
        this.storyIntro = "";
        this.storyCompletion = "";
        this.cityName = null; // Default value
    }
    
    public boolean checkCompletion(MinigameGameView.PlayerData player) {
        if (isCompleted) return true;
        if (!isActive) return false;

        switch (missionType) {
            case REACH_LOCATION:
                RectF playerBounds = player.getBounds();
                float centerX = playerBounds.centerX();
                float centerY = playerBounds.centerY();
                float distance = (float) Math.sqrt(
                        Math.pow(centerX - targetX, 2) +
                                Math.pow(centerY - targetY, 2)
                );
                if (distance < 50) { // 50 pixels threshold
                    isCompleted = true;
                    notifyMissionCompleted();
                    return true;
                }
                // Update progress
                float maxDistance = 500; // Maximum tracking distance
                float progress = Math.max(0, 1 - (distance / maxDistance));
                notifyMissionProgress(progress);
                break;

            case COLLECT_ITEMS:
                if (collectedItems >= requiredItems) {
                    isCompleted = true;
                    notifyMissionCompleted();
                    return true;
                }
                notifyMissionProgress((float) collectedItems / requiredItems);
                break;

            case INTERACT_NPC:
                if (collectedItems >= requiredItems) {
                    isCompleted = true;
                    notifyMissionCompleted();
                    return true;
                }
                notifyMissionProgress((float) collectedItems / requiredItems);
                break;
                
            case ANSWER_QUIZ:
            case TAKE_PHOTO:
            case VISIT_ATTRACTIONS:
                // These mission types are completed through other mechanisms
                // Check if all objectives are completed
                boolean allCompleted = true;
                int completedCount = 0;
                
                for (MissionObjective objective : objectives) {
                    if (objective.isCompleted()) {
                        completedCount++;
                    } else {
                        allCompleted = false;
                    }
                }
                
                if (allCompleted && !objectives.isEmpty()) {
                    isCompleted = true;
                    notifyMissionCompleted();
                    return true;
                }
                
                if (!objectives.isEmpty()) {
                    notifyMissionProgress((float) completedCount / objectives.size());
                }
                break;
        }
        return false;
    }

    public void incrementCollectedItems() {
        if (missionType == MissionType.COLLECT_ITEMS && !isCompleted) {
            collectedItems++;
            notifyMissionProgress((float) collectedItems / requiredItems);
            
            if (collectedItems >= requiredItems) {
                isCompleted = true;
                notifyMissionCompleted();
            }
        }
    }
    
    private void notifyMissionCompleted() {
        if (listener != null) {
            listener.onMissionCompleted(this);
        }
    }
    
    private void notifyMissionProgress(float progress) {
        if (listener != null) {
            listener.onMissionProgress(this, progress);
        }
    }

    public float getTargetX() { return targetX; }
    public float getTargetY() { return targetY; }
    public int getExperienceReward() { return experienceReward; }
    public void setExperienceReward(int experienceReward) { this.experienceReward = experienceReward; }
    public void setMissionListener(MissionListener listener) {
        this.listener = listener;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
