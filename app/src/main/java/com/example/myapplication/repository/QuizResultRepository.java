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
                        // Creăm un profil nou dacă nu există
                        UserProfile newProfile = new UserProfile();
                        newProfile.setUserId(currentUser.getUid());
                        newProfile.setEmail(currentUser.getEmail());
                        newProfile.setDisplayName(currentUser.getDisplayName() != null ? 
                                currentUser.getDisplayName() : "User " + currentUser.getUid().substring(0, 5));
                        newProfile.setUsername(currentUser.getEmail());
                        if (currentUser.getPhotoUrl() != null) {
                            newProfile.setProfileImageUrl(currentUser.getPhotoUrl().toString());
                        }
                        
                        // Salvăm profilul nou
                        db.collection(COLLECTION_USERS).document(currentUser.getUid())
                            .set(newProfile)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User profile created successfully");
                                // Continuăm cu actualizarea clasamentului
                                createOrUpdateLeaderboardEntry(
                                        newProfile, quizResult);
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error creating user profile", e));
                    } else {
                        // Continuăm cu actualizarea clasamentului
                        createOrUpdateLeaderboardEntry(userProfile, quizResult);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting user profile for leaderboard update", e));
    }
    
    private void createOrUpdateLeaderboardEntry(UserProfile userProfile, QuizResult quizResult) {
        // Creăm intrarea pentru clasament
        LeaderboardEntry entry = new LeaderboardEntry(
                userProfile.getUserId(),
                userProfile.getUsername() != null ? userProfile.getUsername() : userProfile.getEmail(),
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
                .whereEqualTo("userId", userProfile.getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Utilizatorul nu are o intrare în clasament, adăugăm una nouă
                        DocumentReference newEntryRef = db.collection(COLLECTION_LEADERBOARDS)
                                .document(leaderboardId)
                                .collection("entries")
                                .document(userProfile.getUserId());  // Folosim userId ca ID pentru document
                        batch.set(newEntryRef, entry);
                        Log.d(TAG, "Creating new leaderboard entry for user: " + userProfile.getUserId());
                    } else {
                        // Utilizatorul are deja o intrare în clasament, o actualizăm doar dacă scorul nou este mai mare
                        DocumentReference existingEntryRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        LeaderboardEntry existingEntry = queryDocumentSnapshots.getDocuments().get(0).toObject(LeaderboardEntry.class);
                        
                        if (existingEntry != null && quizResult.getScore() > existingEntry.getScore()) {
                            batch.set(existingEntryRef, entry);
                            Log.d(TAG, "Updating leaderboard entry for user: " + userProfile.getUserId() + 
                                    " with new score: " + quizResult.getScore());
                        } else if (existingEntry != null) {
                            Log.d(TAG, "Not updating leaderboard entry as existing score (" + 
                                    existingEntry.getScore() + ") is higher than new score (" + 
                                    quizResult.getScore() + ")");
                        }
                    }
                    
                    // Executăm batch-ul
                    batch.commit()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Leaderboard updated successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating leaderboard", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking existing leaderboard entry", e));
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
                        // Creăm un profil nou
                        UserProfile newProfile = new UserProfile();
                        newProfile.setUserId(currentUser.getUid());
                        newProfile.setEmail(currentUser.getEmail());
                        newProfile.setDisplayName(currentUser.getDisplayName() != null ? 
                                currentUser.getDisplayName() : "User " + currentUser.getUid().substring(0, 5));
                        newProfile.setUsername(currentUser.getEmail());
                        if (currentUser.getPhotoUrl() != null) {
                            newProfile.setProfileImageUrl(currentUser.getPhotoUrl().toString());
                        }
                        
                        // Inițializăm statisticile
                        newProfile.setQuizPoints(quizResult.getScore());
                        newProfile.setTotalQuizzesTaken(1);
                        newProfile.setTotalAnswers(quizResult.getTotalQuestions());
                        newProfile.setCorrectAnswers(quizResult.getCorrectAnswers());
                        
                        // Salvăm profilul nou
                        userRef.set(newProfile)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile created successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error creating user profile", e));
                    } else {
                        // Actualizăm statisticile
                        int newPoints = userProfile.getQuizPoints() + quizResult.getScore();
                        int newTotalQuizzes = userProfile.getTotalQuizzesTaken() + 1;
                        int newTotalAnswers = userProfile.getTotalAnswers() + quizResult.getTotalQuestions();
                        int newCorrectAnswers = userProfile.getCorrectAnswers() + quizResult.getCorrectAnswers();
                        
                        // Actualizăm profilul
                        userRef.update(
                                "quizPoints", newPoints,
                                "totalQuizzesTaken", newTotalQuizzes,
                                "totalAnswers", newTotalAnswers,
                                "correctAnswers", newCorrectAnswers
                        )
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile updated successfully with new points: " + newPoints))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating user profile", e));
                    }
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
        
        // Simplificăm interogarea pentru a evita probleme cu indexurile compuse
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
                    
                    // Extragem toate intrările
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        LeaderboardEntry entry = queryDocumentSnapshots.getDocuments().get(i).toObject(LeaderboardEntry.class);
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }
                    
                    // Sortăm intrările după scor (descrescător) și apoi după dată (crescător) dacă scorurile sunt egale
                    entries.sort((a, b) -> {
                        if (a.getScore() != b.getScore()) {
                            return Integer.compare(b.getScore(), a.getScore()); // Descrescător după scor
                        }
                        // Dacă scorurile sunt egale, sortăm după dată
                        if (a.getAchievedAt() != null && b.getAchievedAt() != null) {
                            return a.getAchievedAt().compareTo(b.getAchievedAt()); // Crescător după dată
                        }
                        return 0;
                    });
                    
                    // Calculăm rangurile
                    int currentRank = 1;
                    int previousScore = -1;
                    
                    for (int i = 0; i < entries.size(); i++) {
                        LeaderboardEntry entry = entries.get(i);
                        
                        // Primul element primește rangul 1
                        if (i == 0) {
                            entry.setRank(currentRank);
                            previousScore = entry.getScore();
                        } else {
                            // Dacă scorul este diferit de cel precedent, actualizăm rangul
                            if (entry.getScore() < previousScore) {
                                currentRank = i + 1;
                                previousScore = entry.getScore();
                            }
                            entry.setRank(currentRank);
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
        
        // Folosim metoda getLeaderboard pentru a obține toate intrările și a asigura consistența rangurilor
        getLeaderboard(region, gameType, 0)
            .thenAccept(entries -> {
                int userRank = -1;
                
                // Căutăm intrarea utilizatorului curent
                for (LeaderboardEntry entry : entries) {
                    if (entry.getUserId().equals(currentUser.getUid())) {
                        userRank = entry.getRank();
                        break;
                    }
                }
                
                future.complete(userRank);
            })
            .exceptionally(e -> {
                Log.e(TAG, "Error getting user rank", e);
                future.completeExceptionally(e);
                return null;
            });
        
        return future;
    }
    
    /**
     * Obține profilul unui utilizator
     * @param userId ID-ul utilizatorului
     * @return CompletableFuture care va conține profilul utilizatorului sau null dacă nu există
     */
    public CompletableFuture<UserProfile> getUserProfile(String userId) {
        CompletableFuture<UserProfile> future = new CompletableFuture<>();
        
        db.collection(COLLECTION_USERS).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                    future.complete(userProfile);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user profile", e);
                    future.completeExceptionally(e);
                });
        
        return future;
    }
    
    /**
     * Salvează profilul unui utilizator
     * @param userProfile Profilul utilizatorului de salvat
     * @return CompletableFuture care va conține true dacă salvarea a reușit, false altfel
     */
    public CompletableFuture<Boolean> saveUserProfile(UserProfile userProfile) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (userProfile == null || userProfile.getUserId() == null) {
            future.complete(false);
            return future;
        }
        
        db.collection(COLLECTION_USERS).document(userProfile.getUserId())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile saved successfully");
                    future.complete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile", e);
                    future.complete(false);
                });
        
        return future;
    }
    
    /**
     * Obține rezultatele quiz-urilor pentru un utilizator
     * @param userId ID-ul utilizatorului
     * @param limit Numărul maxim de rezultate de returnat (opțional)
     * @return CompletableFuture care va conține lista de rezultate
     */
    public CompletableFuture<List<QuizResult>> getUserQuizResults(String userId, int limit) {
        CompletableFuture<List<QuizResult>> future = new CompletableFuture<>();
        
        Query query = db.collection(COLLECTION_QUIZ_RESULTS)
                .whereEqualTo("userId", userId)
                .orderBy("completedAt", Query.Direction.DESCENDING);
        
        if (limit > 0) {
            query = query.limit(limit);
        }
        
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<QuizResult> results = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        QuizResult quizResult = document.toObject(QuizResult.class);
                        if (quizResult != null) {
                            results.add(quizResult);
                        }
                    }
                    future.complete(results);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user quiz results", e);
                    future.completeExceptionally(e);
                });
        
        return future;
    }
} 