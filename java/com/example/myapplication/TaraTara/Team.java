package com.example.myapplication.TaraTara;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Team {
    private static final String TAG = "Team";
    private String id;
    private String name;
    private int color;
    private boolean isPlayerTeam;
    private Drawable soldierDrawable;
    private List<Soldier> soldiers;
    private float morale;
    private Random random;

    // Store screen dimensions and team area info
    private float screenWidth;
    private float screenHeight;
    private float teamCenterX;
    private float teamCenterY;
    private float teamFormationSpacing = 100; // Default spacing, could be adjusted

    public Team(String id, String name, int color, boolean isPlayerTeam, Drawable soldierDrawable) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.isPlayerTeam = isPlayerTeam;
        this.soldierDrawable = soldierDrawable;
        this.soldiers = new ArrayList<>();
        this.morale = 100.0f;
        this.random = new Random();
        
        Log.d(TAG, "Team created: " + id + " (" + name + ")");
    }

    public void setTeamCenter(float centerX, float centerY) {
        this.teamCenterX = centerX;
        this.teamCenterY = centerY;
        Log.d(TAG, "Team " + id + " center set to: " + centerX + "," + centerY);
    }

    public void initializeTeam(float screenWidth, float screenHeight, int soldierCount) {
        try {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            soldiers.clear();
            
            Log.d(TAG, "Initializing team " + id + " with " + soldierCount + " soldiers on screen " + 
                  screenWidth + "x" + screenHeight);

            // Use the team center position if it was set, otherwise calculate based on team ID
            if (teamCenterX == 0 && teamCenterY == 0) {
                if (id.equals("player_team_1")) { // Assuming player is on the left
                    this.teamCenterX = screenWidth * 0.2f;
                } else if (id.equals("player_team_2")) { // Assuming player2 is on the right
                    this.teamCenterX = screenWidth * 0.8f;
                } else { // Assuming enemy is on the top
                    this.teamCenterX = screenWidth * 0.5f;
                }
                
                if (isPlayerTeam) {
                    this.teamCenterY = screenHeight * 0.7f; // Players at bottom
                } else {
                    this.teamCenterY = screenHeight * 0.3f; // Enemy at top
                }
                
                Log.d(TAG, "Team " + id + " center calculated as: " + teamCenterX + "," + teamCenterY);
            }

            // Use a grid layout logic similar to original, centered on teamCenterX/Y
            float spacing = teamFormationSpacing;
            int rows = Math.max(1, (int)Math.ceil(Math.sqrt(soldierCount))); // Ensure at least 1 row
            int cols = Math.max(1, (int)Math.ceil(soldierCount / (float)rows)); // Ensure at least 1 col

            int index = 0;
            for (int row = 0; row < rows && index < soldierCount; row++) {
                for (int col = 0; col < cols && index < soldierCount; col++) {
                    // Calculate position relative to team center
                    float xOffset = (col - (cols - 1) / 2f) * spacing;
                    float yOffset = (row - (rows - 1) / 2f) * spacing;

                    // Add small random jitter for more natural look
                    float x = teamCenterX + xOffset + random.nextFloat() * 20 - 10;
                    float y = teamCenterY + yOffset + random.nextFloat() * 20 - 10;

                    // Ensure soldier is within screen bounds
                    x = Math.max(50, Math.min(screenWidth - 50, x));
                    y = Math.max(50, Math.min(screenHeight - 50, y));

                    Soldier soldier = new Soldier(x, y, color, soldierDrawable);
                    soldier.setTeam(this);
                    soldiers.add(soldier);
                    index++;
                }
            }
            
            Log.d(TAG, "Team " + id + " initialized with " + soldiers.size() + " soldiers");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing team " + id, e);
        }
    }

    // Overload for adding a soldier without specifying coordinates
    public void addSoldier() {
        // Calculate a position for the new soldier within the team's area
        if (screenWidth == 0 || screenHeight == 0 || teamCenterX == 0 || teamCenterY == 0) {
            Log.e(TAG, "Cannot add soldier, team not properly initialized: " + id);
            return;
        }

        // Find an empty-ish spot near the team center
        float x = teamCenterX + random.nextFloat() * 80 - 40; // Random offset within 80 units
        float y = teamCenterY + random.nextFloat() * 80 - 40;

        // Ensure soldier is within screen bounds
        x = Math.max(50, Math.min(screenWidth - 50, x));
        y = Math.max(50, Math.min(screenHeight - 50, y));

        // Try to avoid collisions with existing soldiers
        boolean positionOccupied = true;
        int attempts = 0;
        while (positionOccupied && attempts < 10) {
            positionOccupied = false;
            for (Soldier existing : soldiers) {
                float distance = (float) Math.sqrt(
                    Math.pow(existing.getX() - x, 2) + 
                    Math.pow(existing.getY() - y, 2)
                );
                if (distance < 50) { // Simple distance check
                    positionOccupied = true;
                    x = teamCenterX + random.nextFloat() * 80 - 40;
                    y = teamCenterY + random.nextFloat() * 80 - 40;
                    
                    // Ensure soldier is within screen bounds
                    x = Math.max(50, Math.min(screenWidth - 50, x));
                    y = Math.max(50, Math.min(screenHeight - 50, y));
                    break;
                }
            }
            attempts++;
        }

        // Create and add the soldier
        addSoldier(x, y);
        Log.d(TAG, "Added soldier to team " + id + " at position " + x + "," + y + ", total now: " + soldiers.size());
    }

    // Existing method for adding soldier at specific coordinates
    public void addSoldier(float x, float y) {
        try {
            if (soldierDrawable == null) {
                Log.e(TAG, "Cannot add soldier, drawable is null for team " + id);
                return;
            }
            
            Soldier soldier = new Soldier(x, y, color, soldierDrawable);
            soldier.setTeam(this);
            soldiers.add(soldier);
            Log.d(TAG, "Added soldier at specific position to team " + id + ", total now: " + soldiers.size());
        } catch (Exception e) {
            Log.e(TAG, "Error adding soldier to team " + id, e);
        }
    }

    public Soldier getRandomSoldier() {
        if (soldiers.isEmpty()) {
            Log.d(TAG, "No soldiers available in team " + id);
            return null;
        }
        
        // Make sure we don't go out of bounds
        int index = random.nextInt(soldiers.size());
        if (index >= 0 && index < soldiers.size()) {
            return soldiers.get(index);
        } else {
            Log.e(TAG, "Invalid index generated: " + index + " for team size " + soldiers.size());
            return soldiers.size() > 0 ? soldiers.get(0) : null;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public boolean isPlayerTeam() {
        return isPlayerTeam;
    }

    public Drawable getSoldierDrawable() {
        return soldierDrawable;
    }

    public List<Soldier> getSoldiers() {
        return soldiers;
    }

    public int getSoldierCount() {
        return soldiers.size();
    }

    public float getMorale() {
        return morale;
    }

    public void setMorale(float morale) {
        this.morale = morale;
    }

    public void removeSoldier(Soldier soldier) {
        if (soldier != null && soldiers.contains(soldier)) {
            boolean removed = soldiers.remove(soldier);
            Log.d(TAG, "Soldier removed from team " + id + ": " + removed + ", remaining: " + soldiers.size());
        } else if (soldier == null) {
            Log.w(TAG, "Attempted to remove null soldier from team " + id);
        } else {
            Log.w(TAG, "Attempted to remove non-existent soldier from team " + id);
            
            // If the soldier wasn't found, remove any soldier as a fallback
            if (!soldiers.isEmpty()) {
                Soldier toRemove = soldiers.get(0);
                soldiers.remove(toRemove);
                Log.d(TAG, "Removed fallback soldier instead, remaining: " + soldiers.size());
            }
        }
    }
    
    public float getTeamCenterX() {
        return teamCenterX;
    }
    
    public float getTeamCenterY() {
        return teamCenterY;
    }
}
