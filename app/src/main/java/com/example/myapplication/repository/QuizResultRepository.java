package com.example.myapplication.repository;

import android.util.Log;
import com.example.myapplication.model.LeaderboardEntry;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.model.UserProfile;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repository pentru gestionarea rezultatelor quiz-urilor în Firestore
 */
public class QuizResultRepository {
    private static final String TAG = "QuizResultRepository";
    private static final String COLLECTION_QUIZ_RESULTS = "quiz_results";
    private static final String COLLECTION_LEADERBOARDS = "leaderboards";
    private static final String COLLECTION_USERS = "users";
    
    private final FirebaseFirestore db;
    private static QuizResultRepository instance;
    
    private QuizResultRepository() {
        db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Obține instanța singleton a repository-ului
     */
    public static synchronized QuizResultRepository getInstance() {
        if (instance == null) {
            instance = new QuizResultRepository();
        }
        return instance;
    }
    
    /**
     * Salvează rezultatul unui quiz și actualizează clasamentul
     * @param quizResult Rezultatul quiz-ului
     * @return Task pentru monitorizarea operației
     */
    public Task<DocumentReference> saveQuizResult(QuizResult quizResult) {
        // Verificăm dacă utilizatorul este autentificat
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Dacă utilizatorul nu este autentificat, salvăm doar rezultatul local
            return db.collection(COLLECTION_QUIZ_RESULTS)
                    .add(quizResult)
                    .addOnSuccessListener(docRef -> {
                        Log.d(TAG, "Quiz result saved with ID: " + docRef.getId());
                        // Nu actualizăm clasamentul sau profilul utilizatorului
                    });
        }
        
        // Dacă utilizatorul este autentificat, salvăm rezultatul și actualizăm clasamentul
        return db.collection(COLLECTION_QUIZ_RESULTS)
                .add(quizResult)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Quiz result saved with ID: " + docRef.getId());
                    
                    // Actualizăm clasamentul
                    updateLeaderboard(quizResult);
                    
                    // Actualizăm profilul utilizatorului
                    updateUserProfile(quizResult);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error saving quiz result", e));
    }
    
    /**
     * Actualizează clasamentul cu rezultatul unui quiz
     * @param quizResult Rezultatul quiz-ului
     */
    private void updateLeaderboard(QuizResult quizResult) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        // Obținem profilul utilizatorului pentru a avea username și displayName
        db.collection(COLLECTION_USERS).document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    if (userProfile == null) {
                        Log.w(TAG, "User profile not found for updating leaderboard");
                        return;
                    }
                    
                    // Creăm intrarea pentru clasament
                    LeaderboardEntry entry = new LeaderboardEntry(
                            currentUser.getUid(),
                            userProfile.getUsername(),
                            userProfile.getDisplayName(),
                            userProfile.getProfileImageUrl(),
                            quizResult.getScore(),
                            quizResult.getRegion(),
                            quizResult.getGameType()
                    );
                    
                    // Verificăm dacă utilizatorul are deja o intrare în clasament pentru această regiune și joc
                    String leaderboardId = quizResult.getRegion() + "_" + quizResult.getGameType();
                    db.collection(COLLECTION_LEADERBOARDS)
                            .document(leaderboardId)
                            .collection("entries")
                            .whereEqualTo("userId", currentUser.getUid())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                WriteBatch batch = db.batch();
                                
                                if (queryDocumentSnapshots.isEmpty()) {
                                    // Utilizatorul nu are o intrare în clasament, adăugăm una nouă
                                    DocumentReference newEntryRef = db.collection(COLLECTION_LEADERBOARDS)
                                            .document(leaderboardId)
                                            .collection("entries")
                                            .document();
                                    batch.set(newEntryRef, entry);
                                } else {
                                    // Utilizatorul are deja o intrare în clasament, o actualizăm doar dacă scorul nou este mai mare
                                    DocumentReference existingEntryRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                                    LeaderboardEntry existingEntry = queryDocumentSnapshots.getDocuments().get(0).toObject(LeaderboardEntry.class);
                                    
                                    if (existingEntry != null && quizResult.getScore() > existingEntry.getScore()) {
                                        batch.set(existingEntryRef, entry);
                                    }
                                }
                                
                                // Executăm batch-ul
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Leaderboard updated successfully"))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error updating leaderboard", e));
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error checking existing leaderboard entry", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting user profile for leaderboard update", e));
    }
    
    /**
     * Actualizează profilul utilizatorului cu rezultatul unui quiz
     * @param quizResult Rezultatul quiz-ului
     */
    private void updateUserProfile(QuizResult quizResult) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        DocumentReference userRef = db.collection(COLLECTION_USERS).document(currentUser.getUid());
        
        // Actualizăm profilul utilizatorului
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    if (userProfile == null) {
                        Log.w(TAG, "User profile not found for updating");
                        return;
                    }
                    
                    // Actualizăm punctele și alte statistici
                    // Aici putem adăuga logica pentru a actualiza statisticile utilizatorului
                    // De exemplu, numărul total de quiz-uri completate, scorul total, etc.
                    
                    userRef.update("quizPoints", userProfile.getQuizPoints() + quizResult.getScore())
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating user profile", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting user profile for update", e));
    }
    
    /**
     * Obține rezultatele quiz-urilor pentru un utilizator specific
     * @param userId ID-ul utilizatorului
     * @return Task pentru monitorizarea operației
     */
    public Task<QuerySnapshot> getQuizResultsForUser(String userId) {
        return db.collection(COLLECTION_QUIZ_RESULTS)
                .whereEqualTo("userId", userId)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get();
    }
    
    /**
     * Obține rezultatele quiz-urilor pentru utilizatorul curent
     * @return Task pentru monitorizarea operației sau null dacă utilizatorul nu este autentificat
     */
    public Task<QuerySnapshot> getQuizResultsForCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return null;
        
        return getQuizResultsForUser(currentUser.getUid());
    }
    
    /**
     * Obține clasamentul pentru o regiune și un tip de joc specifice
     * @param region Regiunea
     * @param gameType Tipul jocului
     * @param limit Numărul maxim de intrări (opțional)
     * @return CompletableFuture pentru monitorizarea operației
     */
    public CompletableFuture<List<LeaderboardEntry>> getLeaderboard(String region, String gameType, int limit) {
        CompletableFuture<List<LeaderboardEntry>> future = new CompletableFuture<>();
        
        String leaderboardId = region + "_" + gameType;
        Query query = db.collection(COLLECTION_LEADERBOARDS)
                .document(leaderboardId)
                .collection("entries")
                .orderBy("score", Query.Direction.DESCENDING);
        
        if (limit > 0) {
            query = query.limit(limit);
        }
        
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    int rank = 1;
                    
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        LeaderboardEntry entry = queryDocumentSnapshots.getDocuments().get(i).toObject(LeaderboardEntry.class);
                        if (entry != null) {
                            // Setăm rangul
                            if (i > 0) {
                                LeaderboardEntry previousEntry = entries.get(i - 1);
                                if (entry.getScore() < previousEntry.getScore()) {
                                    rank = i + 1;
                                }
                            }
                            entry.setRank(rank);
                            entries.add(entry);
                        }
                    }
                    
                    future.complete(entries);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting leaderboard", e);
                    future.completeExceptionally(e);
                });
        
        return future;
    }
    
    /**
     * Obține poziția utilizatorului curent în clasament
     * @param region Regiunea
     * @param gameType Tipul jocului
     * @return CompletableFuture pentru monitorizarea operației
     */
    public CompletableFuture<Integer> getCurrentUserRank(String region, String gameType) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            future.complete(-1); // Utilizatorul nu este autentificat
            return future;
        }
        
        String leaderboardId = region + "_" + gameType;
        db.collection(COLLECTION_LEADERBOARDS)
                .document(leaderboardId)
                .collection("entries")
                .orderBy("score", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int rank = -1;
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        LeaderboardEntry entry = queryDocumentSnapshots.getDocuments().get(i).toObject(LeaderboardEntry.class);
                        if (entry != null && entry.getUserId().equals(currentUser.getUid())) {
                            rank = i + 1;
                            break;
                        }
                    }
                    future.complete(rank);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user rank", e);
                    future.completeExceptionally(e);
                });
        
        return future;
    }
} 