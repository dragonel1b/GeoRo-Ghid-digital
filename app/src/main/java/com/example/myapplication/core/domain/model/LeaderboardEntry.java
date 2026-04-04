package com.example.myapplication.core.domain.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Model pentru stocarea intrărilor din clasament (leaderboard) în Firestore
 */
public class LeaderboardEntry {
    private String userId;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private int score;
    private int rank;
    private String region;
    private String gameType;
    @ServerTimestamp
    private Date achievedAt;
    
    /**
     * Constructor gol necesar pentru Firestore
     */
    public LeaderboardEntry() {
        // Constructor gol necesar pentru Firestore
    }
    
    /**
     * Constructor complet pentru LeaderboardEntry
     */
    public LeaderboardEntry(String userId, String username, String displayName, 
                          String profileImageUrl, int score, String region, String gameType) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.score = score;
        this.region = region;
        this.gameType = gameType;
        this.rank = 0; // Rangul va fi setat ulterior
    }
    
    // Getters și setters
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getRank() {
        return rank;
    }
    
    public void setRank(int rank) {
        this.rank = rank;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    
    public Date getAchievedAt() {
        return achievedAt;
    }
    
    public void setAchievedAt(Date achievedAt) {
        this.achievedAt = achievedAt;
    }
    
    /**
     * Returnează numele de afișare sau username-ul dacă numele de afișare nu este disponibil
     */
    public String getDisplayNameOrUsername() {
        return displayName != null && !displayName.isEmpty() ? displayName : username;
    }
} 