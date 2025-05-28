package com.example.myapplication.Joc1;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clasa pentru gestionarea misiunilor în joc
 */
public class Mission {
    // Tipuri de misiuni disponibile
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
    private String cityName;
    private int rewardPoints;
    private int type;
    private int state = STATE_AVAILABLE;
    
    // Mission progress
    private List<MissionObjective> objectives = new ArrayList<>();
    private int chapter = 1;
    private int step = 1;
    
    // Enumerare pentru tipurile de misiuni
    public enum MissionType {
        VISIT_ATTRACTIONS,
        ANSWER_QUIZ,
        TAKE_PHOTO,
        INTERACT_NPC,
        COLLECT_ITEMS
    }
    
    public interface MissionListener {
        void onMissionCompleted(Mission mission);
        void onMissionProgress(Mission mission, float progress);
    }
    
    // Constructor simplu
    public Mission(int type, int rewardPoints, MissionType missionType, String regionId) {
        this.type = type;
        this.rewardPoints = rewardPoints;
        this.regionId = regionId;
        this.cityName = regionId; // Implicit, orașul este același cu regiunea
        this.state = STATE_AVAILABLE;
        this.objectives = new ArrayList<>();
        this.chapter = 1;
        this.step = 1;
    }

    // Constructor complet
    public Mission(String id, String title, String description, String regionId, 
                   int rewardPoints, int type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.regionId = regionId;
        this.cityName = regionId; // Implicit, orașul este același cu regiunea
        this.rewardPoints = rewardPoints;
        this.type = type;
        this.state = STATE_AVAILABLE;
        this.objectives = new ArrayList<>();
        this.chapter = 1;
        this.step = 1;
    }

    // Adăugare obiectiv nou la misiune
    public void addObjective(String description) {
        MissionObjective objective = new MissionObjective(description);
        objectives.add(objective);
    }

    // Marcare obiectiv ca fiind completat
    public void completeObjective(int index) {
        if (index >= 0 && index < objectives.size()) {
            objectives.get(index).setCompleted(true);
            
            // Verificare dacă toate obiectivele sunt completate
            boolean allCompleted = true;
            for (MissionObjective obj : objectives) {
                if (!obj.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                setCompleted(true);
                notifyMissionCompleted();
            } else {
                int completedCount = 0;
                for (MissionObjective obj : objectives) {
                    if (obj.isCompleted()) {
                        completedCount++;
                    }
                }
                notifyMissionProgress((float) completedCount / objectives.size());
            }
        }
    }

    // Getters și setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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

    // Clasă internă pentru obiectivele unei misiuni
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

    // Legacy fields for backwards compatibility
    private float targetX, targetY;
    private boolean isCompleted;
    private int requiredItems;
    private int collectedItems;
    private MissionType missionType;
    private int experienceReward;
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
            case VISIT_ATTRACTIONS:
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
}
