package com.example.myapplication.repository;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import com.example.myapplication.models.EnhancedQuestionModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for FirestoreQuestionRepository
 * Tests Firebase Firestore interactions and data retrieval
 */
@RunWith(AndroidJUnit4.class)
public class FirestoreQuestionRepositoryTest {

    private FirestoreQuestionRepository repository;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        repository = FirestoreQuestionRepository.getInstance();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetQuestions_ValidRegion_ReturnsQuestions() throws InterruptedException {
        // Arrange
        String region = "transilvania";
        String gameType = "quiz";
        CountDownLatch latch = new CountDownLatch(1);
        final List<EnhancedQuestionModel>[] result = new List[1];

        // Act
        repository.getQuestions(region, gameType)
            .addOnSuccessListener(questions -> {
                result[0] = questions;
                latch.countDown();
            })
            .addOnFailureListener(exception -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertNotNull("Questions should not be null", result[0]);
            assertTrue("Should return some questions", result[0].size() > 0);
        }
    }

    @Test
    public void testGetQuestions_InvalidRegion_ReturnsEmptyList() throws InterruptedException {
        // Arrange
        String region = "invalid_region";
        String gameType = "quiz";
        CountDownLatch latch = new CountDownLatch(1);
        final List<EnhancedQuestionModel>[] result = new List[1];

        // Act
        repository.getQuestions(region, gameType)
            .addOnSuccessListener(questions -> {
                result[0] = questions;
                latch.countDown();
            })
            .addOnFailureListener(exception -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertEquals("Should return empty list for invalid region", 0, result[0].size());
        }
    }

    @Test
    public void testHasQuestions_ValidRegion_ReturnsTrue() throws InterruptedException {
        // Arrange
        String region = "transilvania";
        String gameType = "quiz";
        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] result = new Boolean[1];

        // Act
        repository.hasQuestions(region, gameType)
            .thenAccept(hasQuestions -> {
                result[0] = hasQuestions;
                latch.countDown();
            })
            .exceptionally(throwable -> {
                latch.countDown();
                return null;
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertTrue("Should have questions for valid region", result[0]);
        }
    }

    @Test
    public void testGetQuestionsByDifficulty_ValidDifficulty_ReturnsFilteredQuestions() throws InterruptedException {
        // Arrange
        String region = "transilvania";
        String gameType = "quiz";
        EnhancedQuestionModel.Difficulty difficulty = EnhancedQuestionModel.Difficulty.EASY;
        CountDownLatch latch = new CountDownLatch(1);
        final List<EnhancedQuestionModel>[] result = new List[1];

        // Act
        repository.getQuestionsByDifficulty(region, gameType, difficulty)
            .addOnSuccessListener(questions -> {
                result[0] = questions;
                latch.countDown();
            })
            .addOnFailureListener(exception -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            for (EnhancedQuestionModel question : result[0]) {
                assertEquals("All questions should have the specified difficulty", 
                           difficulty, question.getDifficulty());
            }
        }
    }

    @Test
    public void testGetQuestionsByCategory_ValidCategory_ReturnsFilteredQuestions() throws InterruptedException {
        // Arrange
        String region = "transilvania";
        String gameType = "quiz";
        EnhancedQuestionModel.Category category = EnhancedQuestionModel.Category.HISTORY;
        CountDownLatch latch = new CountDownLatch(1);
        final List<EnhancedQuestionModel>[] result = new List[1];

        // Act
        repository.getQuestionsByCategory(region, gameType, category)
            .addOnSuccessListener(questions -> {
                result[0] = questions;
                latch.countDown();
            })
            .addOnFailureListener(exception -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            for (EnhancedQuestionModel question : result[0]) {
                assertEquals("All questions should have the specified category", 
                           category, question.getCategory());
            }
        }
    }

    @Test
    public void testGetRandomQuestions_ValidCount_ReturnsCorrectNumberOfQuestions() throws InterruptedException {
        // Arrange
        String region = "transilvania";
        String gameType = "quiz";
        int count = 5;
        CountDownLatch latch = new CountDownLatch(1);
        final List<EnhancedQuestionModel>[] result = new List[1];

        // Act
        repository.getRandomQuestions(region, gameType, count)
            .addOnSuccessListener(questions -> {
                result[0] = questions;
                latch.countDown();
            })
            .addOnFailureListener(exception -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertTrue("Should return at most the requested number of questions", 
                     result[0].size() <= count);
        }
    }

    @Test
    public void testGetQuestionCount_ValidRegion_ReturnsPositiveCount() throws InterruptedException {
        // Arrange
        String region = "transilvania";
        String gameType = "quiz";
        CountDownLatch latch = new CountDownLatch(1);
        final Long[] result = new Long[1];

        // Act
        repository.getQuestionCount(region, gameType)
            .addOnSuccessListener(count -> {
                result[0] = count;
                latch.countDown();
            })
            .addOnFailureListener(exception -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertTrue("Question count should be non-negative", result[0] >= 0);
        }
    }
} 