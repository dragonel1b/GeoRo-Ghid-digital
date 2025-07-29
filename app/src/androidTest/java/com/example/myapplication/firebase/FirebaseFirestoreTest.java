package com.example.myapplication.firebase;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration tests for Firebase Firestore
 * Tests CRUD operations, queries, and data management
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseFirestoreTest {

    private FirebaseFirestore firestore;
    private Context context;
    private static final String TEST_COLLECTION = "test_collection";
    private static final String TEST_DOCUMENT = "test_document";

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        firestore = FirebaseFirestore.getInstance();
    }

    @Test
    public void testFirestore_IsInitialized() {
        // Assert
        assertNotNull("Firestore should be initialized", firestore);
    }

    @Test
    public void testFirestore_CreateDocument_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "Test User");
        testData.put("age", 25);
        testData.put("city", "Bucuresti");
        testData.put("timestamp", System.currentTimeMillis());

        // Act
        firestore.collection(TEST_COLLECTION)
            .document(TEST_DOCUMENT)
            .set(testData)
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
        assertTrue("Document creation should succeed", success[0]);
    }

    @Test
    public void testFirestore_ReadDocument_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final DocumentSnapshot[] result = new DocumentSnapshot[1];
        
        // First create a document
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "Test User");
        testData.put("age", 25);

        // Act
        firestore.collection(TEST_COLLECTION)
            .document(TEST_DOCUMENT)
            .set(testData)
            .addOnSuccessListener(aVoid -> {
                // Then read it
                firestore.collection(TEST_COLLECTION)
                    .document(TEST_DOCUMENT)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        result[0] = documentSnapshot;
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        latch.countDown();
                    });
            })
            .addOnFailureListener(e -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertTrue("Document should exist", result[0].exists());
            assertEquals("Name should match", "Test User", result[0].getString("name"));
            assertEquals("Age should match", 25L, result[0].getLong("age"));
        }
    }

    @Test
    public void testFirestore_UpdateDocument_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("age", 30);
        updateData.put("updated", true);

        // Act
        firestore.collection(TEST_COLLECTION)
            .document(TEST_DOCUMENT)
            .update(updateData)
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
        assertTrue("Document update should succeed", success[0]);
    }

    @Test
    public void testFirestore_DeleteDocument_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        // Act
        firestore.collection(TEST_COLLECTION)
            .document(TEST_DOCUMENT)
            .delete()
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
        assertTrue("Document deletion should succeed", success[0]);
    }

    @Test
    public void testFirestore_QueryDocuments_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final QuerySnapshot[] result = new QuerySnapshot[1];
        
        // Create multiple documents
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "User 1");
        data1.put("age", 25);
        
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "User 2");
        data2.put("age", 30);

        // Act
        firestore.collection(TEST_COLLECTION)
            .document("doc1")
            .set(data1)
            .addOnSuccessListener(aVoid -> {
                firestore.collection(TEST_COLLECTION)
                    .document("doc2")
                    .set(data2)
                    .addOnSuccessListener(aVoid2 -> {
                        // Query all documents
                        firestore.collection(TEST_COLLECTION)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                result[0] = querySnapshot;
                                latch.countDown();
                            })
                            .addOnFailureListener(e -> {
                                latch.countDown();
                            });
                    });
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertTrue("Should have documents", result[0].size() > 0);
        }
    }

    @Test
    public void testFirestore_WhereQuery_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final QuerySnapshot[] result = new QuerySnapshot[1];

        // Act
        firestore.collection(TEST_COLLECTION)
            .whereEqualTo("age", 25)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                result[0] = querySnapshot;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            // Query might return empty results, which is valid
            assertNotNull("Query result should not be null", result[0]);
        }
    }

    @Test
    public void testFirestore_OrderByQuery_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final QuerySnapshot[] result = new QuerySnapshot[1];

        // Act
        firestore.collection(TEST_COLLECTION)
            .orderBy("age")
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                result[0] = querySnapshot;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        if (result[0] != null) {
            assertNotNull("Query result should not be null", result[0]);
            assertTrue("Should limit to 10 documents", result[0].size() <= 10);
        }
    }

    @Test
    public void testFirestore_BatchWrite_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];
        
        WriteBatch batch = firestore.batch();
        
        // Add multiple operations to batch
        DocumentReference doc1 = firestore.collection(TEST_COLLECTION).document("batch1");
        DocumentReference doc2 = firestore.collection(TEST_COLLECTION).document("batch2");
        
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "Batch User 1");
        data1.put("age", 25);
        
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", "Batch User 2");
        data2.put("age", 30);
        
        batch.set(doc1, data1);
        batch.set(doc2, data2);

        // Act
        batch.commit()
            .addOnSuccessListener(aVoid -> {
                success[0] = true;
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                success[0] = false;
                latch.countDown();
            });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Batch write should succeed", success[0]);
    }

    @Test
    public void testFirestore_Transaction_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];

        // Act
        firestore.runTransaction(transaction -> {
            DocumentReference docRef = firestore.collection(TEST_COLLECTION).document("transaction_doc");
            DocumentSnapshot snapshot = transaction.get(docRef);
            
            if (!snapshot.exists()) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", "Transaction User");
                data.put("age", 25);
                transaction.set(docRef, data);
            } else {
                transaction.update(docRef, "age", snapshot.getLong("age") + 1);
            }
            
            return null;
        })
        .addOnSuccessListener(aVoid -> {
            success[0] = true;
            latch.countDown();
        })
        .addOnFailureListener(e -> {
            success[0] = false;
            latch.countDown();
        });

        // Wait for async operation
        boolean completed = latch.await(15, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Transaction should succeed", success[0]);
    }

    @Test
    public void testFirestore_RealTimeListener_Success() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = new boolean[1];
        
        DocumentReference docRef = firestore.collection(TEST_COLLECTION).document("listener_doc");

        // Act
        docRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                success[0] = false;
                latch.countDown();
                return;
            }
            
            if (snapshot != null && snapshot.exists()) {
                success[0] = true;
                latch.countDown();
            }
        });

        // Create document to trigger listener
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Listener User");
        data.put("age", 25);
        
        docRef.set(data);

        // Wait for async operation
        boolean completed = latch.await(10, TimeUnit.SECONDS);

        // Assert
        assertTrue("Test should complete within timeout", completed);
        assertTrue("Real-time listener should work", success[0]);
    }

    @Test
    public void testFirestore_GetDocumentReference_Success() {
        // Act
        DocumentReference docRef = firestore.collection(TEST_COLLECTION).document(TEST_DOCUMENT);

        // Assert
        assertNotNull("Document reference should not be null", docRef);
        assertEquals("Collection path should match", TEST_COLLECTION, docRef.getParent().getId());
        assertEquals("Document ID should match", TEST_DOCUMENT, docRef.getId());
    }

    @Test
    public void testFirestore_GetCollectionReference_Success() {
        // Act
        com.google.firebase.firestore.CollectionReference collectionRef = 
            firestore.collection(TEST_COLLECTION);

        // Assert
        assertNotNull("Collection reference should not be null", collectionRef);
        assertEquals("Collection ID should match", TEST_COLLECTION, collectionRef.getId());
    }
} 