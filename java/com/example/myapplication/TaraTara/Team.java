package com.example.myapplication.TaraTara;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Team {
    private String id;
    private String name;
    private int color;
    private boolean isPlayerTeam;
    private Drawable soldierDrawable;
    private List<Soldier> soldiers;
    private float morale;
    private Random random;

    public Team(String id, String name, int color, boolean isPlayerTeam, Drawable soldierDrawable) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.isPlayerTeam = isPlayerTeam;
        this.soldierDrawable = soldierDrawable;
        this.soldiers = new ArrayList<>();
        this.morale = 100.0f;
        this.random = new Random();
    }

    public void initializeTeam(float screenWidth, float screenHeight, int soldierCount) {
        soldiers.clear();

        // Calculate team position based on screen dimensions
        float startX;
        if (id.equals("player_team_1")) {
            startX = screenWidth * 0.2f;
        } else if (id.equals("player_team_2")) {
            startX = screenWidth * 0.4f;
        } else { // enemy team
            startX = screenWidth * 0.8f;
        }

        float startY = screenHeight * 0.5f;
        float spacing = 100; // Space between soldiers
        int rows = (int)Math.ceil(Math.sqrt(soldierCount));
        int cols = (int)Math.ceil(soldierCount / (float)rows);

        int index = 0;
        for (int row = 0; row < rows && index < soldierCount; row++) {
            for (int col = 0; col < cols && index < soldierCount; col++) {
                float x = startX + (col - cols/2f) * spacing + random.nextFloat() * 20 - 10;
                float y = startY + (row - rows/2f) * spacing + random.nextFloat() * 20 - 10;

                Soldier soldier = new Soldier(x, y, color, soldierDrawable);
                soldier.setTeam(this);
                soldiers.add(soldier);
                index++;
            }
        }
    }

    public void addSoldier(float x, float y) {
        Soldier soldier = new Soldier(x, y, color, soldierDrawable);
        soldier.setTeam(this);
        soldiers.add(soldier);
    }

    public Soldier getRandomSoldier() {
        if (soldiers.isEmpty()) {
            return null;
        }
        return soldiers.get(random.nextInt(soldiers.size()));
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
        soldiers.remove(soldier);
    }
}
