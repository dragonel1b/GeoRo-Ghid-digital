package com.example.myapplication.repository;

import android.util.Log;
import com.example.myapplication.models.FirestoreQuestionModel;
import com.example.myapplication.models.QuestionModel;
import com.example.myapplication.model.QuizResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Repository pentru gestionarea întrebărilor în Firestore
 */
public class FirestoreQuestionRepository {
    private static final String TAG = "FirestoreQuestionRepo";
    private static final String COLLECTION_REGIONS = "regions";
    private static final String COLLECTION_GAMES = "games";
    private static final String COLLECTION_QUESTIONS = "questions";
    private static final String COLLECTION_QUIZ_RESULTS = "quiz_results";
    
    private final FirebaseFirestore db;
    private static FirestoreQuestionRepository instance;
    
    private FirestoreQuestionRepository() {
        db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Obține instanța singleton a repository-ului
     */
    public static synchronized FirestoreQuestionRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreQuestionRepository();
        }
        return instance;
    }
    
    /**
     * Adaugă o întrebare în Firestore
     * @param question Întrebarea de adăugat
     * @return Task pentru monitorizarea operației
     */
    public Task<DocumentReference> addQuestion(FirestoreQuestionModel question) {
        return db.collection(COLLECTION_REGIONS)
                .document(question.getRegion())
                .collection(COLLECTION_GAMES)
                .document(question.getGameType())
                .collection(COLLECTION_QUESTIONS)
                .add(question);
    }
    
    /**
     * Adaugă o întrebare folosind un model local QuestionModel
     * @param questionModel Întrebarea locală
     * @param region Regiunea întrebării
     * @param gameType Tipul jocului
     * @return Task pentru monitorizarea operației
     */
    public Task<DocumentReference> addQuestionFromModel(QuestionModel questionModel, String region, String gameType) {
        FirestoreQuestionModel firestoreQuestion = FirestoreQuestionModel.fromQuestionModel(questionModel, region, gameType);
        return addQuestion(firestoreQuestion);
    }
    
    /**
     * Adaugă o listă de întrebări în Firestore
     * @param questions Lista de întrebări
     * @param region Regiunea întrebărilor
     * @param gameType Tipul jocului
     * @return CompletableFuture pentru monitorizarea operației
     */
    public CompletableFuture<Void> addQuestions(List<QuestionModel> questions, String region, String gameType) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        List<Task<DocumentReference>> tasks = new ArrayList<>();
        for (QuestionModel question : questions) {
            tasks.add(addQuestionFromModel(question, region, gameType));
        }
        
        // Monitorizăm toate task-urile
        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
            .addOnSuccessListener(taskList -> {
                Log.d(TAG, "Toate întrebările au fost adăugate cu succes");
                future.complete(null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Eroare la adăugarea întrebărilor", e);
                future.completeExceptionally(e);
            });
        
        return future;
    }
    
    /**
     * Obține toate întrebările pentru o regiune și un tip de joc
     * @param region Regiunea
     * @param gameType Tipul jocului
     * @return Task pentru monitorizarea operației
     */
    public Task<QuerySnapshot> getQuestions(String region, String gameType) {
        Log.d(TAG, "Getting questions for region: " + region + ", gameType: " + gameType);
        
        Task<QuerySnapshot> task = db.collection(COLLECTION_REGIONS)
                .document(region)
                .collection(COLLECTION_GAMES)
                .document(gameType)
                .collection(COLLECTION_QUESTIONS)
                .get();
        
        task.addOnSuccessListener(querySnapshot -> {
            Log.d(TAG, "Query successful. Found " + querySnapshot.size() + " questions");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Query failed for region: " + region + ", gameType: " + gameType, e);
        });
        
        return task;
    }
    
    /**
     * Obține întrebările pentru o regiune și un tip de joc și le convertește în modele locale
     * @param region Regiunea
     * @param gameType Tipul jocului
     * @return CompletableFuture cu lista de întrebări locale
     */
    public CompletableFuture<List<QuestionModel>> getQuestionsAsModels(String region, String gameType) {
        CompletableFuture<List<QuestionModel>> future = new CompletableFuture<>();
        
        getQuestions(region, gameType)
            .addOnSuccessListener(querySnapshot -> {
                List<QuestionModel> questionModels = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    FirestoreQuestionModel firestoreQuestion = doc.toObject(FirestoreQuestionModel.class);
                    if (firestoreQuestion != null) {
                        questionModels.add(firestoreQuestion.toQuestionModel());
                    }
                }
                future.complete(questionModels);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Eroare la obținerea întrebărilor", e);
                future.completeExceptionally(e);
            });
        
        return future;
    }
    
    /**
     * Actualizează o întrebare existentă
     * @param question Întrebarea actualizată
     * @return Task pentru monitorizarea operației
     */
    public Task<Void> updateQuestion(FirestoreQuestionModel question) {
        if (question.getId() == null) {
            throw new IllegalArgumentException("ID-ul întrebării nu poate fi null pentru actualizare");
        }
        
        return db.collection(COLLECTION_REGIONS)
                .document(question.getRegion())
                .collection(COLLECTION_GAMES)
                .document(question.getGameType())
                .collection(COLLECTION_QUESTIONS)
                .document(question.getId())
                .set(question);
    }
    
    /**
     * Șterge o întrebare
     * @param questionId ID-ul întrebării
     * @param region Regiunea
     * @param gameType Tipul jocului
     * @return Task pentru monitorizarea operației
     */
    public Task<Void> deleteQuestion(String questionId, String region, String gameType) {
        return db.collection(COLLECTION_REGIONS)
                .document(region)
                .collection(COLLECTION_GAMES)
                .document(gameType)
                .collection(COLLECTION_QUESTIONS)
                .document(questionId)
                .delete();
    }
    
    /**
     * Verifică dacă există întrebări pentru o regiune și un tip de joc
     * @param region Regiunea
     * @param gameType Tipul jocului
     * @return CompletableFuture cu rezultatul verificării
     */
    public CompletableFuture<Boolean> hasQuestions(String region, String gameType) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        db.collection(COLLECTION_REGIONS)
            .document(region)
            .collection(COLLECTION_GAMES)
            .document(gameType)
            .collection(COLLECTION_QUESTIONS)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                future.complete(!querySnapshot.isEmpty());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Eroare la verificarea existenței întrebărilor", e);
                future.completeExceptionally(e);
            });
        
        return future;
    }
    
    /**
     * Migrează întrebări din codul sursă în Firestore
     * Exemplu de utilizare pentru întrebările din TransilvaniaGameActivity
     * @param questions Array de întrebări din clasa Question internă
     * @param region Regiunea
     * @param gameType Tipul jocului
     * @return CompletableFuture pentru monitorizarea operației
     */
    public CompletableFuture<Void> migrateQuestionsFromSource(Object[] questions, String region, String gameType) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // Verificăm mai întâi dacă există deja întrebări pentru această regiune și joc
        hasQuestions(region, gameType)
            .thenAccept(hasExistingQuestions -> {
                if (hasExistingQuestions) {
                    Log.d(TAG, "Există deja întrebări pentru " + region + "/" + gameType + ". Migrarea este ignorată.");
                    future.complete(null);
                    return;
                }
                
                List<Task<DocumentReference>> tasks = new ArrayList<>();
                
                for (Object q : questions) {
                    try {
                        // Extragem câmpurile din obiectul Question folosind reflection
                        java.lang.reflect.Field questionField = q.getClass().getDeclaredField("question");
                        java.lang.reflect.Field answersField = q.getClass().getDeclaredField("answers");
                        java.lang.reflect.Field correctAnswerIndexField = q.getClass().getDeclaredField("correctAnswerIndex");
                        java.lang.reflect.Field factField = q.getClass().getDeclaredField("fact");
                        
                        questionField.setAccessible(true);
                        answersField.setAccessible(true);
                        correctAnswerIndexField.setAccessible(true);
                        factField.setAccessible(true);
                        
                        String questionText = (String) questionField.get(q);
                        String[] answers = (String[]) answersField.get(q);
                        int correctIndex = (int) correctAnswerIndexField.get(q);
                        String fact = (String) factField.get(q);
                        
                        // Creăm modelul Firestore
                        String correctAnswer = answers[correctIndex];
                        List<String> incorrectAnswers = new ArrayList<>();
                        for (int i = 0; i < answers.length; i++) {
                            if (i != correctIndex) {
                                incorrectAnswers.add(answers[i]);
                            }
                        }
                        
                        FirestoreQuestionModel firestoreQuestion = new FirestoreQuestionModel(
                            questionText,
                            correctAnswer,
                            incorrectAnswers,
                            fact,
                            "", // hint
                            "", // imageUrl
                            region,
                            gameType
                        );
                        
                        tasks.add(addQuestion(firestoreQuestion));
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Eroare la extragerea datelor din întrebare", e);
                    }
                }
                
                // Monitorizăm toate task-urile
                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener(taskList -> {
                        Log.d(TAG, "Migrare completă: " + tasks.size() + " întrebări adăugate");
                        future.complete(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Eroare la migrarea întrebărilor", e);
                        future.completeExceptionally(e);
                    });
            })
            .exceptionally(e -> {
                Log.e(TAG, "Eroare la verificarea existenței întrebărilor", e);
                future.completeExceptionally(e);
                return null;
            });
        
        return future;
    }
    
    /**
     * Obține întrebările din activitatea specifică unei regiuni
     * Folosește reflection pentru a găsi clasa corespunzătoare și a extrage întrebările
     * @param region Regiunea pentru care se doresc întrebările
     * @return Array de obiecte Question sau null dacă nu s-au găsit
     */
    public Object[] getQuestionsFromActivity(String region) {
        try {
            // Construim numele clasei în funcție de regiune
            String className = "com.example.myapplication." + region + "usage." + 
                              region.substring(0, 1).toUpperCase() + region.substring(1) + "GameActivity";
            
            // Încercăm să încărcăm clasa
            Class<?> activityClass = Class.forName(className);
            
            // Încercăm să găsim câmpul 'questions' în clasă
            java.lang.reflect.Field questionsField = activityClass.getDeclaredField("questions");
            questionsField.setAccessible(true);
            
            // Creăm o instanță a clasei (poate fi null pentru câmpuri statice)
            Object instance = null;
            if (!java.lang.reflect.Modifier.isStatic(questionsField.getModifiers())) {
                try {
                    instance = activityClass.newInstance();
                } catch (Exception e) {
                    Log.e(TAG, "Nu s-a putut crea o instanță a clasei " + className, e);
                    return null;
                }
            }
            
            // Încercăm să obținem lista de întrebări
            Object questionsList = questionsField.get(instance);
            
            // Verificăm dacă avem o listă și o convertim în array
            if (questionsList instanceof List) {
                return ((List<?>) questionsList).toArray();
            } else if (questionsList instanceof Object[]) {
                return (Object[]) questionsList;
            }
            
            Log.e(TAG, "Câmpul 'questions' nu este o listă sau un array în " + className);
            return null;
            
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Nu s-a găsit clasa pentru regiunea " + region, e);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Nu s-a găsit câmpul 'questions' în clasa pentru regiunea " + region, e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Nu s-a putut accesa câmpul 'questions' în clasa pentru regiunea " + region, e);
        } catch (Exception e) {
            Log.e(TAG, "Eroare la obținerea întrebărilor pentru regiunea " + region, e);
        }
        
        return null;
    }
    
    /**
     * Salvează rezultatul unui quiz în Firestore
     * @param quizResult Rezultatul quiz-ului
     * @return Task pentru monitorizarea operației
     */
    public Task<DocumentReference> saveQuizResult(QuizResult quizResult) {
        return db.collection(COLLECTION_QUIZ_RESULTS)
                .add(quizResult)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Quiz result saved with ID: " + docRef.getId());
                    
                    // Actualizăm clasamentul direct aici
                    updateLeaderboardEntry(quizResult);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error saving quiz result", e));
    }
    
    /**
     * Actualizează clasamentul cu rezultatul unui quiz
     * @param quizResult Rezultatul quiz-ului
     */
    private void updateLeaderboardEntry(QuizResult quizResult) {
        // Implementare simplificată pentru actualizarea clasamentului
        String leaderboardId = quizResult.getRegion() + "_" + quizResult.getGameType();
        String userId = quizResult.getUserId();
        
        if (userId == null) {
            Log.w(TAG, "User ID is null, cannot update leaderboard");
            return;
        }
        
        // Verificăm dacă utilizatorul are deja o intrare în clasament
        db.collection("leaderboards")
                .document(leaderboardId)
                .collection("entries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Utilizatorul nu are o intrare în clasament, adăugăm una nouă
                        createNewLeaderboardEntry(quizResult, leaderboardId);
                    } else {
                        // Utilizatorul are deja o intrare în clasament, o actualizăm doar dacă scorul nou este mai mare
                        updateExistingLeaderboardEntry(quizResult, queryDocumentSnapshots.getDocuments().get(0).getReference());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking existing leaderboard entry", e));
    }
    
    private void createNewLeaderboardEntry(QuizResult quizResult, String leaderboardId) {
        // Obținem datele utilizatorului din Firebase Auth
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "User not authenticated, cannot create leaderboard entry");
            return;
        }
        
        // Creăm o intrare nouă în clasament
        Map<String, Object> entry = new java.util.HashMap<>();
        entry.put("userId", quizResult.getUserId());
        entry.put("username", currentUser.getEmail()); // Folosim email-ul ca username implicit
        entry.put("displayName", currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : "User " + currentUser.getUid().substring(0, 5));
        entry.put("profileImageUrl", currentUser.getPhotoUrl() != null ? 
                currentUser.getPhotoUrl().toString() : "");
        entry.put("score", quizResult.getScore());
        entry.put("region", quizResult.getRegion());
        entry.put("gameType", quizResult.getGameType());
        entry.put("achievedAt", quizResult.getCompletedAt());
        
        // Verificăm mai întâi dacă utilizatorul are un profil în colecția users
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Utilizăm datele din profilul utilizatorului
                        String username = documentSnapshot.getString("username");
                        String displayName = documentSnapshot.getString("displayName");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        
                        if (username != null) entry.put("username", username);
                        if (displayName != null) entry.put("displayName", displayName);
                        if (profileImageUrl != null) entry.put("profileImageUrl", profileImageUrl);
                    }
                    
                    // Adăugăm intrarea în clasament folosind userId ca ID pentru document
                    db.collection("leaderboards")
                            .document(leaderboardId)
                            .collection("entries")
                            .document(currentUser.getUid())
                            .set(entry)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Leaderboard entry created for user: " + currentUser.getUid()))
                            .addOnFailureListener(e -> Log.e(TAG, "Error creating leaderboard entry", e));
                })
                .addOnFailureListener(e -> {
                    // În caz de eroare, adăugăm intrarea cu datele implicite
                    db.collection("leaderboards")
                            .document(leaderboardId)
                            .collection("entries")
                            .document(currentUser.getUid())
                            .set(entry)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Leaderboard entry created for user: " + currentUser.getUid()))
                            .addOnFailureListener(e2 -> Log.e(TAG, "Error creating leaderboard entry", e2));
                });
    }
    
    private void updateExistingLeaderboardEntry(QuizResult quizResult, DocumentReference entryRef) {
        // Obținem intrarea existentă
        entryRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long existingScore = documentSnapshot.getLong("score");
                    if (existingScore == null || quizResult.getScore() > existingScore) {
                        // Actualizăm scorul doar dacă este mai mare
                        entryRef.update("score", quizResult.getScore(), "achievedAt", quizResult.getCompletedAt())
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Leaderboard entry updated successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating leaderboard entry", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting existing leaderboard entry", e));
    }
} 