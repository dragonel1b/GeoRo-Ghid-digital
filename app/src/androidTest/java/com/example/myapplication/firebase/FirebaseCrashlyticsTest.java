package com.example.myapplication.firebase;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.utils.FirebaseCrashlyticsManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Integration tests for Firebase Crashlytics
 * Tests error reporting and crash tracking functionality
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseCrashlyticsTest {

    private FirebaseCrashlyticsManager crashlyticsManager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        crashlyticsManager = FirebaseCrashlyticsManager.getInstance(context);
    }

    @Test
    public void testCrashlyticsManager_IsInitialized() {
        // Assert
        assertNotNull("CrashlyticsManager should be initialized", crashlyticsManager);
    }

    @Test
    public void testCrashlyticsManager_GetInstance_ReturnsSameInstance() {
        // Act
        FirebaseCrashlyticsManager instance1 = FirebaseCrashlyticsManager.getInstance(context);
        FirebaseCrashlyticsManager instance2 = FirebaseCrashlyticsManager.getInstance(context);

        // Assert
        assertSame("Should return the same instance", instance1, instance2);
    }

    @Test
    public void testCrashlyticsManager_Initialize_Success() {
        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.initialize();
            assertTrue("Initialization should succeed", true);
        } catch (Exception e) {
            fail("Initialization should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportException_Success() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportException(testException);
            assertTrue("Exception reporting should succeed", true);
        } catch (Exception e) {
            fail("Exception reporting should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportExceptionWithContext_Success() {
        // Arrange
        Exception testException = new RuntimeException("Test exception with context");
        String context = "TestActivity";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportException(testException, context);
            assertTrue("Exception reporting with context should succeed", true);
        } catch (Exception e) {
            fail("Exception reporting with context should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportCustomError_Success() {
        // Arrange
        String errorType = "test_error";
        String errorMessage = "This is a test error";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportError(errorType, errorMessage);
            assertTrue("Custom error reporting should succeed", true);
        } catch (Exception e) {
            fail("Custom error reporting should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportNetworkError_Success() {
        // Arrange
        String endpoint = "https://api.example.com";
        String error = "Connection timeout";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportNetworkError(endpoint, error);
            assertTrue("Network error reporting should succeed", true);
        } catch (Exception e) {
            fail("Network error reporting should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportFirebaseError_Success() {
        // Arrange
        String operation = "firestore_read";
        String error = "Permission denied";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportFirebaseError(operation, error);
            assertTrue("Firebase error reporting should succeed", true);
        } catch (Exception e) {
            fail("Firebase error reporting should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportUIError_Success() {
        // Arrange
        String screen = "MainActivity";
        String action = "button_click";
        String error = "Button not responding";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportUIError(screen, action, error);
            assertTrue("UI error reporting should succeed", true);
        } catch (Exception e) {
            fail("UI error reporting should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportGameError_Success() {
        // Arrange
        String region = "Transilvania";
        String gameType = "Quiz";
        String error = "Score not updating";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportGameError(region, gameType, error);
            assertTrue("Game error reporting should succeed", true);
        } catch (Exception e) {
            fail("Game error reporting should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportSecurityError_Success() {
        // Arrange
        String threatType = "root_detected";
        String details = "Device appears to be rooted";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportSecurityError(threatType, details);
            assertTrue("Security error reporting should succeed", true);
        } catch (Exception e) {
            fail("Security error reporting should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_SetCustomKeyString_Success() {
        // Arrange
        String key = "test_key";
        String value = "test_value";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.setCustomKey(key, value);
            assertTrue("Setting custom key should succeed", true);
        } catch (Exception e) {
            fail("Setting custom key should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_SetCustomKeyInt_Success() {
        // Arrange
        String key = "test_int_key";
        int value = 42;

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.setCustomKey(key, value);
            assertTrue("Setting custom int key should succeed", true);
        } catch (Exception e) {
            fail("Setting custom int key should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_SetCustomKeyBoolean_Success() {
        // Arrange
        String key = "test_bool_key";
        boolean value = true;

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.setCustomKey(key, value);
            assertTrue("Setting custom boolean key should succeed", true);
        } catch (Exception e) {
            fail("Setting custom boolean key should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_LogMessage_Success() {
        // Arrange
        String message = "Test log message";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.log(message);
            assertTrue("Logging message should succeed", true);
        } catch (Exception e) {
            fail("Logging message should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_SetCrashlyticsEnabled_Success() {
        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.setCrashlyticsEnabled(true);
            crashlyticsManager.setCrashlyticsEnabled(false);
            crashlyticsManager.setCrashlyticsEnabled(true);
            assertTrue("Setting Crashlytics enabled should succeed", true);
        } catch (Exception e) {
            fail("Setting Crashlytics enabled should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportNullException_HandlesGracefully() {
        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportException(null);
            assertTrue("Should handle null exception gracefully", true);
        } catch (Exception e) {
            fail("Should handle null exception gracefully: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_ReportNullError_HandlesGracefully() {
        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.reportError(null, null);
            assertTrue("Should handle null error gracefully", true);
        } catch (Exception e) {
            fail("Should handle null error gracefully: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_LogNullMessage_HandlesGracefully() {
        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.log(null);
            assertTrue("Should handle null message gracefully", true);
        } catch (Exception e) {
            fail("Should handle null message gracefully: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_LogBreadcrumb_Success() {
        // Arrange
        String category = "test_category";
        String message = "test breadcrumb message";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.logBreadcrumb(category, message);
            assertTrue("Breadcrumb logging should succeed", true);
        } catch (Exception e) {
            fail("Breadcrumb logging should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_LogUserEvent_Success() {
        // Arrange
        String eventName = "test_event";
        String details = "test event details";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.logUserEvent(eventName, details);
            assertTrue("User event logging should succeed", true);
        } catch (Exception e) {
            fail("User event logging should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_LogGameAction_Success() {
        // Arrange
        String region = "Transilvania";
        String action = "quiz_started";
        String details = "User started quiz";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.logGameAction(region, action, details);
            assertTrue("Game action logging should succeed", true);
        } catch (Exception e) {
            fail("Game action logging should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testCrashlyticsManager_LogUIAction_Success() {
        // Arrange
        String screen = "MainActivity";
        String action = "button_click";
        String details = "User clicked start button";

        // Act & Assert - Should not throw exception
        try {
            crashlyticsManager.logUIAction(screen, action, details);
            assertTrue("UI action logging should succeed", true);
        } catch (Exception e) {
            fail("UI action logging should not throw exception: " + e.getMessage());
        }
    }
} 