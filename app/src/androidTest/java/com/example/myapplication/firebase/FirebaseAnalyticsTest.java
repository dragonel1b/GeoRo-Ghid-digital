package com.example.myapplication.firebase;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for Firebase Analytics
 * Tests event tracking and analytics functionality
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseAnalyticsTest {

    private FirebaseAnalytics firebaseAnalytics;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Test
    public void testFirebaseAnalytics_IsInitialized() {
        // Assert
        assertNotNull("FirebaseAnalytics should be initialized", firebaseAnalytics);
    }

    @Test
    public void testFirebaseAnalytics_LogEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        // Act
        firebaseAnalytics.logEvent(Event.APP_OPEN, null)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogCustomEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString("region", "Transilvania");
        params.putString("game_type", "Quiz");
        params.putInt("score", 100);

        // Act
        firebaseAnalytics.logEvent("game_completed", params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Custom event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogGameEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString(Param.ITEM_ID, "quiz_transilvania");
        params.putString(Param.ITEM_NAME, "Transilvania Quiz");
        params.putString(Param.ITEM_CATEGORY, "History");
        params.putInt("difficulty", 2);
        params.putInt("questions_answered", 10);
        params.putInt("correct_answers", 8);

        // Act
        firebaseAnalytics.logEvent(Event.SELECT_ITEM, params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Game event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogUserProperty_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        // Act
        firebaseAnalytics.setUserProperty("user_level", "5")
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("User property setting should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogScreenView_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity");
        params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "com.example.myapplication.RomApp.MainActivity");

        // Act
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Screen view logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogAchievementEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString(Param.ACHIEVEMENT_ID, "first_quiz_completed");
        params.putString(Param.ITEM_NAME, "First Quiz Completed");
        params.putString("region", "Muntenia");
        params.putInt("score", 150);

        // Act
        firebaseAnalytics.logEvent(Event.UNLOCK_ACHIEVEMENT, params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Achievement event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogTutorialEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString(Param.TUTORIAL_NAME, "app_tutorial");
        params.putString("step", "region_selection");
        params.putInt("step_number", 1);

        // Act
        firebaseAnalytics.logEvent(Event.TUTORIAL_BEGIN, params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Tutorial event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogSearchEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString(Param.SEARCH_TERM, "Bran Castle");
        params.putString("search_category", "attractions");
        params.putString("region", "Transilvania");

        // Act
        firebaseAnalytics.logEvent(Event.SEARCH, params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Search event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogShareEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString(Param.CONTENT_TYPE, "quiz_result");
        params.putString(Param.ITEM_ID, "transilvania_quiz");
        params.putString("share_method", "social_media");

        // Act
        firebaseAnalytics.logEvent(Event.SHARE, params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Share event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogErrorEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString("error_type", "network_error");
        params.putString("error_message", "Connection failed");
        params.putString("screen", "MainActivity");

        // Act
        firebaseAnalytics.logEvent("app_error", params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Error event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_LogPerformanceEvent_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        android.os.Bundle params = new android.os.Bundle();
        params.putString("performance_metric", "app_start_time");
        params.putLong("duration_ms", 2500);
        params.putString("device_model", android.os.Build.MODEL);

        // Act
        firebaseAnalytics.logEvent("performance_metric", params)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Performance event logging should succeed", success[0]);
    }

    @Test
    public void testFirebaseAnalytics_GetInstance_ReturnsSameInstance() {
        // Act
        FirebaseAnalytics instance1 = FirebaseAnalytics.getInstance(context);
        FirebaseAnalytics instance2 = FirebaseAnalytics.getInstance(context);

        // Assert
        assertSame("Should return the same instance", instance1, instance2);
    }

    @Test
    public void testFirebaseAnalytics_LogEventWithNullParams_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        // Act
        firebaseAnalytics.logEvent(Event.APP_OPEN, null)
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Event logging with null params should succeed", success[0]);
    }
} 