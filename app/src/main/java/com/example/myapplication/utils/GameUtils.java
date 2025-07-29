package com.example.myapplication.utils;

/**
 * Utility class for game-related calculations and operations
 * Provides methods for score calculation, progress tracking, and game mechanics
 */
public class GameUtils {

    /**
     * Calculate total score based on answer correctness and bonuses
     * 
     * @param isCorrect Whether the answer was correct
     * @param basePoints Base points for the question
     * @param timeBonus Time bonus points
     * @param streakBonus Streak bonus points
     * @return Total score for the answer
     */
    public static int calculateScore(boolean isCorrect, int basePoints, int timeBonus, int streakBonus) {
        if (!isCorrect) {
            return 0;
        }
        return basePoints + timeBonus + streakBonus;
    }

    /**
     * Calculate time bonus based on how quickly the question was answered
     * 
     * @param timeSpent Time spent answering in milliseconds
     * @param maxTime Maximum allowed time in milliseconds
     * @return Time bonus points
     */
    public static int calculateTimeBonus(long timeSpent, long maxTime) {
        if (timeSpent >= maxTime) {
            return 0;
        }
        
        // Calculate bonus as percentage of remaining time
        double timeRatio = (double) (maxTime - timeSpent) / maxTime;
        return (int) (timeRatio * 100); // Max 100 bonus points
    }

    /**
     * Calculate streak bonus based on consecutive correct answers
     * 
     * @param currentStreak Current streak of correct answers
     * @return Streak bonus points
     */
    public static int calculateStreakBonus(int currentStreak) {
        if (currentStreak <= 0) {
            return 0;
        }
        
        // Bonus increases with streak, but caps at 50 points
        return Math.min(currentStreak * 10, 50);
    }

    /**
     * Calculate accuracy percentage
     * 
     * @param correctAnswers Number of correct answers
     * @param totalQuestions Total number of questions
     * @return Accuracy percentage (0.0 to 100.0)
     */
    public static double calculateAccuracy(int correctAnswers, int totalQuestions) {
        if (totalQuestions == 0) {
            return 0.0;
        }
        return (double) correctAnswers / totalQuestions * 100.0;
    }

    /**
     * Check if current score is a new high score
     * 
     * @param currentScore Current game score
     * @param previousHighScore Previous high score
     * @return True if current score is higher
     */
    public static boolean isHighScore(int currentScore, int previousHighScore) {
        return currentScore > previousHighScore;
    }

    /**
     * Calculate player level based on total score
     * 
     * @param totalScore Total accumulated score
     * @return Player level
     */
    public static int calculateLevel(int totalScore) {
        if (totalScore < 100) {
            return 1;
        } else if (totalScore < 500) {
            return 2;
        } else if (totalScore < 1000) {
            return 3;
        } else if (totalScore < 2000) {
            return 4;
        } else {
            return 5;
        }
    }

    /**
     * Format time in seconds to MM:SS format
     * 
     * @param seconds Time in seconds
     * @return Formatted time string
     */
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * Validate game settings
     * 
     * @param timePerQuestion Time per question in seconds
     * @param questionsPerGame Number of questions per game
     * @param minDifficulty Minimum difficulty level
     * @param maxDifficulty Maximum difficulty level
     * @return True if settings are valid
     */
    public static boolean validateGameSettings(int timePerQuestion, int questionsPerGame, 
                                            int minDifficulty, int maxDifficulty) {
        return timePerQuestion > 0 && 
               questionsPerGame > 0 && 
               minDifficulty >= 1 && 
               maxDifficulty <= 5 && 
               minDifficulty <= maxDifficulty;
    }

    /**
     * Calculate experience points based on performance
     * 
     * @param score Game score
     * @param accuracy Accuracy percentage
     * @param timeSpent Total time spent
     * @return Experience points earned
     */
    public static int calculateExperiencePoints(int score, double accuracy, long timeSpent) {
        int baseXP = score / 10; // Base XP from score
        int accuracyBonus = (int) (accuracy * 2); // Bonus for high accuracy
        int timeBonus = timeSpent < 300000 ? 50 : 0; // Bonus for completing under 5 minutes
        
        return baseXP + accuracyBonus + timeBonus;
    }

    /**
     * Check if player qualifies for an achievement
     * 
     * @param achievementType Type of achievement
     * @param currentValue Current value for the achievement
     * @param requiredValue Required value for the achievement
     * @return True if achievement is unlocked
     */
    public static boolean checkAchievement(String achievementType, int currentValue, int requiredValue) {
        switch (achievementType) {
            case "score":
            case "streak":
            case "accuracy":
                return currentValue >= requiredValue;
            case "time":
                return currentValue <= requiredValue; // For time-based achievements (faster is better)
            default:
                return false;
        }
    }

    /**
     * Calculate difficulty multiplier for scoring
     * 
     * @param difficulty Difficulty level (1-5)
     * @return Multiplier for score calculation
     */
    public static double getDifficultyMultiplier(int difficulty) {
        switch (difficulty) {
            case 1: return 1.0; // Easy
            case 2: return 1.2; // Medium
            case 3: return 1.5; // Hard
            case 4: return 2.0; // Expert
            case 5: return 3.0; // Master
            default: return 1.0;
        }
    }
} 