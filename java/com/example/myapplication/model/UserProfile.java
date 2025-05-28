package com.example.myapplication.model;

import androidx.annotation.NonNull;

/**
 * Represents a user profile with basic information.
 */
public class UserProfile {
    private String userId;
    private String username;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private int contributedRecipes;
    private boolean isPremiumUser;

    public UserProfile() {
        // Required empty constructor for Firebase
    }

    public UserProfile(String userId, String username, String displayName, String email) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.contributedRecipes = 0;
        this.isPremiumUser = false;
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getContributedRecipes() {
        return contributedRecipes;
    }

    public void setContributedRecipes(int contributedRecipes) {
        this.contributedRecipes = contributedRecipes;
    }

    public void incrementContributedRecipes() {
        this.contributedRecipes++;
    }

    public boolean isPremiumUser() {
        return isPremiumUser;
    }

    public void setPremiumUser(boolean premiumUser) {
        isPremiumUser = premiumUser;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserProfile{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
} 