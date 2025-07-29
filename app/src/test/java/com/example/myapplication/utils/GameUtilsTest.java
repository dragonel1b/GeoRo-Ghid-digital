package com.example.myapplication.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for game utility functions
 * Tests score calculation, progress tracking, and game mechanics
 */
public class GameUtilsTest {

    @Test
    public void testCalculateScore_CorrectAnswer_ReturnsPositiveScore() {
        // Arrange
        int basePoints = 100;
        int timeBonus = 50;
        int streakBonus = 25;

        // Act
        int score = GameUtils.calculateScore(true, basePoints, timeBonus, streakBonus);

        // Assert
        assertTrue("Score should be positive for correct answer", score > 0);
        assertEquals(175, score); // 100 + 50 + 25
    }

    @Test
    public void testCalculateScore_IncorrectAnswer_ReturnsZero() {
        // Arrange
        int basePoints = 100;
        int timeBonus = 50;
        int streakBonus = 25;

        // Act
        int score = GameUtils.calculateScore(false, basePoints, timeBonus, streakBonus);

        // Assert
        assertEquals(0, score);
    }

    @Test
    public void testCalculateTimeBonus_FastAnswer_ReturnsHighBonus() {
        // Arrange
        long timeSpent = 5000; // 5 seconds
        long maxTime = 30000; // 30 seconds

        // Act
        int bonus = GameUtils.calculateTimeBonus(timeSpent, maxTime);

        // Assert
        assertTrue("Time bonus should be positive for fast answer", bonus > 0);
        assertTrue("Time bonus should be reasonable", bonus <= 100);
    }

    @Test
    public void testCalculateTimeBonus_SlowAnswer_ReturnsLowBonus() {
        // Arrange
        long timeSpent = 25000; // 25 seconds
        long maxTime = 30000; // 30 seconds

        // Act
        int bonus = GameUtils.calculateTimeBonus(timeSpent, maxTime);

        // Assert
        assertTrue("Time bonus should be low for slow answer", bonus < 50);
    }

    @Test
    public void testCalculateStreakBonus_NoStreak_ReturnsZero() {
        // Act
        int bonus = GameUtils.calculateStreakBonus(0);

        // Assert
        assertEquals(0, bonus);
    }

    @Test
    public void testCalculateStreakBonus_WithStreak_ReturnsBonus() {
        // Act
        int bonus = GameUtils.calculateStreakBonus(5);

        // Assert
        assertTrue("Streak bonus should be positive", bonus > 0);
    }

    @Test
    public void testCalculateAccuracy_AllCorrect_Returns100() {
        // Arrange
        int correctAnswers = 10;
        int totalQuestions = 10;

        // Act
        double accuracy = GameUtils.calculateAccuracy(correctAnswers, totalQuestions);

        // Assert
        assertEquals(100.0, accuracy, 0.01);
    }

    @Test
    public void testCalculateAccuracy_HalfCorrect_Returns50() {
        // Arrange
        int correctAnswers = 5;
        int totalQuestions = 10;

        // Act
        double accuracy = GameUtils.calculateAccuracy(correctAnswers, totalQuestions);

        // Assert
        assertEquals(50.0, accuracy, 0.01);
    }

    @Test
    public void testCalculateAccuracy_NoCorrect_Returns0() {
        // Arrange
        int correctAnswers = 0;
        int totalQuestions = 10;

        // Act
        double accuracy = GameUtils.calculateAccuracy(correctAnswers, totalQuestions);

        // Assert
        assertEquals(0.0, accuracy, 0.01);
    }

    @Test
    public void testCalculateAccuracy_ZeroTotal_Returns0() {
        // Arrange
        int correctAnswers = 5;
        int totalQuestions = 0;

        // Act
        double accuracy = GameUtils.calculateAccuracy(correctAnswers, totalQuestions);

        // Assert
        assertEquals(0.0, accuracy, 0.01);
    }

    @Test
    public void testIsHighScore_NewHighScore_ReturnsTrue() {
        // Arrange
        int currentScore = 1000;
        int previousHighScore = 800;

        // Act
        boolean isHighScore = GameUtils.isHighScore(currentScore, previousHighScore);

        // Assert
        assertTrue("Should be high score", isHighScore);
    }

    @Test
    public void testIsHighScore_LowerScore_ReturnsFalse() {
        // Arrange
        int currentScore = 600;
        int previousHighScore = 800;

        // Act
        boolean isHighScore = GameUtils.isHighScore(currentScore, previousHighScore);

        // Assert
        assertFalse("Should not be high score", isHighScore);
    }

    @Test
    public void testCalculateLevel_Score100_ReturnsLevel1() {
        // Act
        int level = GameUtils.calculateLevel(100);

        // Assert
        assertEquals(1, level);
    }

    @Test
    public void testCalculateLevel_Score500_ReturnsLevel2() {
        // Act
        int level = GameUtils.calculateLevel(500);

        // Assert
        assertEquals(2, level);
    }

    @Test
    public void testCalculateLevel_Score1000_ReturnsLevel3() {
        // Act
        int level = GameUtils.calculateLevel(1000);

        // Assert
        assertEquals(3, level);
    }

    @Test
    public void testFormatTime_Seconds_ReturnsFormattedString() {
        // Act
        String formatted = GameUtils.formatTime(65); // 1 minute 5 seconds

        // Assert
        assertEquals("01:05", formatted);
    }

    @Test
    public void testFormatTime_ZeroSeconds_ReturnsFormattedString() {
        // Act
        String formatted = GameUtils.formatTime(0);

        // Assert
        assertEquals("00:00", formatted);
    }

    @Test
    public void testValidateGameSettings_ValidSettings_ReturnsTrue() {
        // Arrange
        int timePerQuestion = 30;
        int questionsPerGame = 10;
        int minDifficulty = 1;
        int maxDifficulty = 3;

        // Act
        boolean isValid = GameUtils.validateGameSettings(timePerQuestion, questionsPerGame, minDifficulty, maxDifficulty);

        // Assert
        assertTrue("Valid settings should return true", isValid);
    }

    @Test
    public void testValidateGameSettings_InvalidSettings_ReturnsFalse() {
        // Arrange
        int timePerQuestion = -5; // Invalid negative time
        int questionsPerGame = 10;
        int minDifficulty = 1;
        int maxDifficulty = 3;

        // Act
        boolean isValid = GameUtils.validateGameSettings(timePerQuestion, questionsPerGame, minDifficulty, maxDifficulty);

        // Assert
        assertFalse("Invalid settings should return false", isValid);
    }
} 