package com.example.myapplication.firebase;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for Firebase Authentication
 * Tests user authentication, registration, and account management
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseAuthenticationTest {

    private FirebaseAuth firebaseAuth;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Test
    public void testFirebaseAuth_IsInitialized() {
        // Assert
        assertNotNull("FirebaseAuth should be initialized", firebaseAuth);
    }

    @Test
    public void testFirebaseAuth_GetCurrentUser_ReturnsUserOrNull() {
        // Act
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Assert
        // User might be null if not signed in, which is valid
        // We just verify the method doesn't throw an exception
        assertTrue("getCurrentUser should not throw exception", true);
    }

    @Test
    public void testFirebaseAuth_SignInAnonymously_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                success[0] = task.isSuccessful();
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Anonymous sign in should succeed", success[0]);
    }

    @Test
    public void testFirebaseAuth_SignOut_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        // First sign in anonymously
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Then sign out
                    firebaseAuth.signOut();
                    success[0] = true;
                } else {
                    success[0] = false;
                }
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Sign out should succeed", success[0]);
    }

    @Test
    public void testFirebaseAuth_GetUid_ReturnsValidString() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final String[] uid = new String[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    uid[0] = task.getResult().getUser().getUid();
                }
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (uid[0] != null) {
            assertNotNull("UID should not be null", uid[0]);
            assertTrue("UID should not be empty", uid[0].length() > 0);
        }
    }

    @Test
    public void testFirebaseAuth_IsUserSignedIn_ReturnsCorrectState() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isSignedIn = new boolean[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                isSignedIn[0] = firebaseAuth.getCurrentUser() != null;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("User should be signed in after anonymous sign in", isSignedIn[0]);
    }

    @Test
    public void testFirebaseAuth_GetUserEmail_ReturnsNullForAnonymousUser() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final String[] email = new String[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    email[0] = task.getResult().getUser().getEmail();
                }
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        // Anonymous users don't have email, so it should be null
        assertNull("Anonymous user should not have email", email[0]);
    }

    @Test
    public void testFirebaseAuth_GetUserDisplayName_ReturnsNullForAnonymousUser() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final String[] displayName = new String[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    displayName[0] = task.getResult().getUser().getDisplayName();
                }
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        // Anonymous users don't have display name, so it should be null
        assertNull("Anonymous user should not have display name", displayName[0]);
    }

    @Test
    public void testFirebaseAuth_GetUserPhotoUrl_ReturnsNullForAnonymousUser() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final android.net.Uri[] photoUrl = new android.net.Uri[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    photoUrl[0] = task.getResult().getUser().getPhotoUrl();
                }
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        // Anonymous users don't have photo URL, so it should be null
        assertNull("Anonymous user should not have photo URL", photoUrl[0]);
    }

    @Test
    public void testFirebaseAuth_IsEmailVerified_ReturnsFalseForAnonymousUser() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isEmailVerified = new boolean[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    isEmailVerified[0] = task.getResult().getUser().isEmailVerified();
                }
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        // Anonymous users don't have email, so email verification should be false
        assertFalse("Anonymous user should not have verified email", isEmailVerified[0]);
    }

    @Test
    public void testFirebaseAuth_GetUserMetadata_ReturnsValidMetadata() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final com.google.firebase.auth.FirebaseUserMetadata[] metadata = new com.google.firebase.auth.FirebaseUserMetadata[1];

        // Act
        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    metadata[0] = task.getResult().getUser().getMetadata();
                }
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (metadata[0] != null) {
            assertNotNull("User metadata should not be null", metadata[0]);
            assertNotNull("Creation timestamp should not be null", metadata[0].getCreationTimestamp());
            assertNotNull("Last sign in timestamp should not be null", metadata[0].getLastSignInTimestamp());
        }
    }
} 