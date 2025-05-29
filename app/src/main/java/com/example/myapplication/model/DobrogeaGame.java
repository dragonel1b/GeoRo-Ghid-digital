package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

public class DobrogeaGame {
    private String currentLandmark;
    private int score;
    private int currentLevel;
    private List<String> discoveredLandmarks;

    // Dobrogea landmarks and facts
    private static final String[][] LANDMARK_FACTS = {
            {"Histria", "Ancient Greek colony founded in 7th century BC"},
            {"Mamaia", "Popular seaside resort on the Black Sea coast"},
            {"Sulina", "Easternmost point of Romania on the Danube Delta"},
            {"Constanta", "Largest port on the Black Sea with ancient history"},
            {"Cernavoda", "Home to Romania's first nuclear power plant"},
            {"Tulcea", "Gateway to the Danube Delta biosphere reserve"}
    };

    public DobrogeaGame() {
        this.score = 0;
        this.currentLevel = 1;
        this.discoveredLandmarks = new ArrayList<>();
    }

    public DobrogeaGame(String name, String description, int initialScore) {
        this();
        this.score = initialScore;
        this.currentLandmark = name;
        // Store the description as a special landmark fact
        this.discoveredLandmarks.add(name + ": " + description);
    }

    public void discoverLandmark(String landmark) {
        if (!discoveredLandmarks.contains(landmark)) {
            discoveredLandmarks.add(landmark);
            score += 100 * currentLevel;
        }
        currentLandmark = landmark;
    }

    public String getCurrentFact() {
        for (String[] fact : LANDMARK_FACTS) {
            if (fact[0].equalsIgnoreCase(currentLandmark)) {
                return fact[1];
            }
        }
        return "Learn more about this Dobrogea landmark!";
    }

    public int getScore() {
        return score;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void levelUp() {
        currentLevel++;
    }

    public List<String> getDiscoveredLandmarks() {
        return new ArrayList<>(discoveredLandmarks);
    }

    public String getCurrentLandmark() {
        return currentLandmark;
    }
}