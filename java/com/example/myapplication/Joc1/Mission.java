package com.example.myapplication.Joc1;

import android.graphics.RectF;

public class Mission {
    private String description;
    private float targetX, targetY;
    private boolean isCompleted;
    private int rewardPoints;
    private int requiredItems;
    private int collectedItems;
    private MissionType type;

    public enum MissionType {
        REACH_LOCATION,
        COLLECT_ITEMS,
        INTERACT_NPC
    }

    public Mission(String description, float targetX, float targetY, int rewardPoints, MissionType type) {
        this.description = description;
        this.targetX = targetX;
        this.targetY = targetY;
        this.rewardPoints = rewardPoints;
        this.type = type;
        this.isCompleted = false;
        this.requiredItems = 0;
        this.collectedItems = 0;
    }

    public Mission(String description, int requiredItems, int rewardPoints) {
        this(description, requiredItems, rewardPoints, MissionType.COLLECT_ITEMS);
    }

    public Mission(String description, int requiredItems, int rewardPoints, MissionType type) {
        this.description = description;
        this.requiredItems = requiredItems;
        this.rewardPoints = rewardPoints;
        this.type = type;
        this.isCompleted = false;
        this.collectedItems = 0;
    }

    public boolean checkCompletion(MinigameGameView.PlayerData player) {
        if (isCompleted) return true;

        switch (type) {
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
                    return true;
                }
                break;

            case COLLECT_ITEMS:
                if (collectedItems >= requiredItems) {
                    isCompleted = true;
                    return true;
                }
                break;

            case INTERACT_NPC:
                if (collectedItems >= requiredItems) {
                    isCompleted = true;
                    return true;
                }
                break;
        }
        return false;
    }

    public void incrementCollectedItems() {
        if (type == MissionType.COLLECT_ITEMS) {
            collectedItems++;
            if (collectedItems >= requiredItems) {
                isCompleted = true;
            }
        }
    }

    public String getDescription() {
        if (type == MissionType.COLLECT_ITEMS) {
            return description + " (" + collectedItems + "/" + requiredItems + ")";
        }
        return description;
    }

    public float getTargetX() { return targetX; }
    public float getTargetY() { return targetY; }
    public boolean isCompleted() { return isCompleted; }
    public int getRewardPoints() { return rewardPoints; }
    public MissionType getType() { return type; }
}
