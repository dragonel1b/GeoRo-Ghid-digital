package com.example.myapplication.core.domain.model;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

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
    
    // Câmpuri pentru quiz-uri
    private int quizPoints;
    private int totalQuizzesTaken;
    private int correctAnswers;
    private int totalAnswers;
    private List<String> completedQuizzes;

    public UserProfile() {
        // Required empty constructor for Firebase
        this.quizPoints = 0;
        this.totalQuizzesTaken = 0;
        this.correctAnswers = 0;
        this.totalAnswers = 0;
        this.completedQuizzes = new ArrayList<>();
    }

    public UserProfile(String userId, String username, String displayName, String email) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.contributedRecipes = 0;
        this.isPremiumUser = false;
        this.quizPoints = 0;
        this.totalQuizzesTaken = 0;
        this.correctAnswers = 0;
        this.totalAnswers = 0;
        this.completedQuizzes = new ArrayList<>();
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
    
    // Getters și setters pentru câmpurile quiz
    
    public int getQuizPoints() {
        return quizPoints;
    }
    
    public void setQuizPoints(int quizPoints) {
        this.quizPoints = quizPoints;
    }
    
    public void addQuizPoints(int points) {
        this.quizPoints += points;
    }
    
    public int getTotalQuizzesTaken() {
        return totalQuizzesTaken;
    }
    
    public void setTotalQuizzesTaken(int totalQuizzesTaken) {
        this.totalQuizzesTaken = totalQuizzesTaken;
    }
    
    public void incrementTotalQuizzesTaken() {
        this.totalQuizzesTaken++;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public void addCorrectAnswers(int count) {
        this.correctAnswers += count;
    }
    
    public int getTotalAnswers() {
        return totalAnswers;
    }
    
    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }
    
    public void addTotalAnswers(int count) {
        this.totalAnswers += count;
    }
    
    public List<String> getCompletedQuizzes() {
        return completedQuizzes;
    }
    
    public void setCompletedQuizzes(List<String> completedQuizzes) {
        this.completedQuizzes = completedQuizzes;
    }
    
    public void addCompletedQuiz(String quizId) {
        if (completedQuizzes == null) {
            completedQuizzes = new ArrayList<>();
        }
        if (!completedQuizzes.contains(quizId)) {
            completedQuizzes.add(quizId);
        }
    }
    
    /**
     * Calculează acuratețea răspunsurilor (procentaj)
     */
    public float getAccuracy() {
        if (totalAnswers == 0) return 0;
        return ((float) correctAnswers / totalAnswers) * 100;
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