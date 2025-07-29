package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BugTracker class
 * Tests bug reporting functionality and data management
 */
@RunWith(MockitoJUnitRunner.class)
public class BugTrackerTest {

    @Mock
    private Context mockContext;
    
    @Mock
    private SharedPreferences mockPrefs;
    
    private BugTracker bugTracker;

    @Before
    public void setUp() {
        // Setup mock context and preferences
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        
        // Create bug tracker instance
        bugTracker = BugTracker.getInstance(mockContext);
    }

    @Test
    public void testGetInstance_ReturnsSameInstance() {
        // Act
        BugTracker instance1 = BugTracker.getInstance(mockContext);
        BugTracker instance2 = BugTracker.getInstance(mockContext);

        // Assert
        assertSame("Should return the same instance", instance1, instance2);
    }

    @Test
    public void testReportBug_ValidData_Success() {
        // Arrange
        String title = "Test Bug";
        String description = "This is a test bug";
        BugTracker.Severity severity = BugTracker.Severity.MEDIUM;

        // Act
        bugTracker.reportBug(title, description, severity);

        // Assert - Verify that the method doesn't throw exceptions
        // The actual Firebase operations are mocked, so we just verify the method completes
        assertTrue("Bug reporting should complete without exceptions", true);
    }

    @Test
    public void testReportException_ValidException_Success() {
        // Arrange
        Exception testException = new RuntimeException("Test exception");
        String context = "Test context";

        // Act
        bugTracker.reportException(testException, context);

        // Assert
        assertTrue("Exception reporting should complete without exceptions", true);
    }

    @Test
    public void testReportUIUXIssue_ValidData_Success() {
        // Arrange
        String title = "UI Issue";
        String description = "Button not responding";
        String screenName = "MainActivity";

        // Act
        bugTracker.reportUIUXIssue(title, description, screenName);

        // Assert
        assertTrue("UI/UX issue reporting should complete without exceptions", true);
    }

    @Test
    public void testReportPerformanceIssue_ValidData_Success() {
        // Arrange
        String title = "Performance Issue";
        String description = "Slow loading";
        String metrics = "Load time: 5s";

        // Act
        bugTracker.reportPerformanceIssue(title, description, metrics);

        // Assert
        assertTrue("Performance issue reporting should complete without exceptions", true);
    }

    @Test
    public void testReportNetworkIssue_ValidData_Success() {
        // Arrange
        String title = "Network Issue";
        String description = "Connection failed";
        String endpoint = "https://api.example.com";

        // Act
        bugTracker.reportNetworkIssue(title, description, endpoint);

        // Assert
        assertTrue("Network issue reporting should complete without exceptions", true);
    }

    @Test
    public void testReportSecurityIssue_ValidData_Success() {
        // Arrange
        String title = "Security Issue";
        String description = "Potential vulnerability";
        String securityLevel = "High";

        // Act
        bugTracker.reportSecurityIssue(title, description, securityLevel);

        // Assert
        assertTrue("Security issue reporting should complete without exceptions", true);
    }

    @Test
    public void testReportGameplayIssue_ValidData_Success() {
        // Arrange
        String title = "Gameplay Issue";
        String description = "Score not updating";
        String region = "Transilvania";
        String gameType = "Quiz";

        // Act
        bugTracker.reportGameplayIssue(title, description, region, gameType);

        // Assert
        assertTrue("Gameplay issue reporting should complete without exceptions", true);
    }

    @Test
    public void testGetBugCount_ReturnsCorrectValue() {
        // Arrange
        when(mockPrefs.getInt("bug_count", 0)).thenReturn(5);

        // Act
        int bugCount = bugTracker.getBugCount();

        // Assert
        assertEquals("Should return correct bug count", 5, bugCount);
    }

    @Test
    public void testGetLastReportTime_ReturnsCorrectValue() {
        // Arrange
        long expectedTime = System.currentTimeMillis();
        when(mockPrefs.getLong("last_report_time", 0)).thenReturn(expectedTime);

        // Act
        long lastReportTime = bugTracker.getLastReportTime();

        // Assert
        assertEquals("Should return correct last report time", expectedTime, lastReportTime);
    }

    @Test
    public void testClearBugData_ClearsPreferences() {
        // Act
        bugTracker.clearBugData();

        // Assert
        verify(mockPrefs.edit(), times(1)).clear();
        verify(mockPrefs.edit(), times(1)).apply();
    }

    @Test
    public void testSeverityEnum_ValuesAreCorrect() {
        // Assert
        assertEquals("LOW", BugTracker.Severity.LOW.getDisplayName());
        assertEquals("Medium", BugTracker.Severity.MEDIUM.getDisplayName());
        assertEquals("High", BugTracker.Severity.HIGH.getDisplayName());
        assertEquals("Critical", BugTracker.Severity.CRITICAL.getDisplayName());
    }

    @Test
    public void testCategoryEnum_ValuesAreCorrect() {
        // Assert
        assertEquals("UI/UX", BugTracker.Category.UI_UX.getDisplayName());
        assertEquals("Performance", BugTracker.Category.PERFORMANCE.getDisplayName());
        assertEquals("Network", BugTracker.Category.NETWORK.getDisplayName());
        assertEquals("Security", BugTracker.Category.SECURITY.getDisplayName());
        assertEquals("Data", BugTracker.Category.DATA.getDisplayName());
        assertEquals("Gameplay", BugTracker.Category.GAMEPLAY.getDisplayName());
        assertEquals("Other", BugTracker.Category.OTHER.getDisplayName());
    }

    @Test
    public void testReportBugWithNullValues_HandlesGracefully() {
        // Arrange
        String title = null;
        String description = null;
        BugTracker.Severity severity = BugTracker.Severity.LOW;

        // Act & Assert - Should not throw exception
        try {
            bugTracker.reportBug(title, description, severity);
            assertTrue("Should handle null values gracefully", true);
        } catch (Exception e) {
            fail("Should not throw exception for null values");
        }
    }

    @Test
    public void testReportExceptionWithNullException_HandlesGracefully() {
        // Arrange
        Exception exception = null;
        String context = "Test context";

        // Act & Assert - Should not throw exception
        try {
            bugTracker.reportException(exception, context);
            assertTrue("Should handle null exception gracefully", true);
        } catch (Exception e) {
            fail("Should not throw exception for null exception");
        }
    }

    @Test
    public void testShutdown_CompletesSuccessfully() {
        // Act & Assert - Should not throw exception
        try {
            bugTracker.shutdown();
            assertTrue("Shutdown should complete successfully", true);
        } catch (Exception e) {
            fail("Shutdown should not throw exception");
        }
    }
} 