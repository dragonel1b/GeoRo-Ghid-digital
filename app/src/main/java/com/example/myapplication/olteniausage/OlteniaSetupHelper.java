package com.example.myapplication.olteniausage;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.util.concurrent.CompletableFuture;

/**
 * Helper class pentru configurarea rapidă a quiz-ului Oltenia
 * Poate fi utilizat pentru a încărca întrebărilor în Firebase cu o singură linie de cod
 */
public class OlteniaSetupHelper {
    private static final String TAG = "OlteniaSetupHelper";
    
    /**
     * Încarcă toate întrebările despre Oltenia în Firebase
     * Această metodă poate fi apelată din orice activitate pentru a configura quiz-ul
     * 
     * @param context Context-ul aplicației
     * @return CompletableFuture care se completează când încărcarea se termină
     */
    public static CompletableFuture<Void> setupOlteniaQuestions(Context context) {
        return setupOlteniaQuestions(context, true);
    }
    
    /**
     * Încarcă toate întrebările despre Oltenia în Firebase cu opțiune de afișare toast
     * 
     * @param context Context-ul aplicației
     * @param showToasts Dacă să afișeze toast-uri pentru progres
     * @return CompletableFuture care se completează când încărcarea se termină
     */
    public static CompletableFuture<Void> setupOlteniaQuestions(Context context, boolean showToasts) {
        Log.d(TAG, "🚀 Începe configurarea quiz-ului Oltenia...");
        
        if (showToasts) {
            Toast.makeText(context, "Se încarcă întrebările despre Oltenia...", Toast.LENGTH_SHORT).show();
        }
        
        OlteniaQuestionsUploader uploader = new OlteniaQuestionsUploader(context);
        
        return uploader.uploadAllQuestions()
            .thenRun(() -> {
                Log.d(TAG, "✅ Configurarea quiz-ului Oltenia completată cu succes!");
                if (showToasts) {
                    Toast.makeText(context, "Quiz-ul Oltenia este gata! 🎉", Toast.LENGTH_LONG).show();
                }
            })
            .exceptionally(throwable -> {
                Log.e(TAG, "❌ Eroare la configurarea quiz-ului Oltenia", throwable);
                if (showToasts) {
                    Toast.makeText(context, "Eroare la configurarea quiz-ului: " + throwable.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                }
                return null;
            });
    }
    
    /**
     * Verifică dacă întrebările despre Oltenia sunt deja încărcate în Firebase
     * Această metodă poate fi folosită pentru a evita încărcări duplicate
     * 
     * @param context Context-ul aplicației
     * @return CompletableFuture<Boolean> care returnează true dacă întrebările există
     */
    public static CompletableFuture<Boolean> areOlteniaQuestionsLoaded(Context context) {
        Log.d(TAG, "🔍 Verifică dacă întrebările Oltenia sunt încărcate...");
        
        com.example.myapplication.repository.FirestoreQuestionRepository repository = 
            com.example.myapplication.repository.FirestoreQuestionRepository.getInstance();
        
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        repository.getQuestions("oltenia", "quiz")
            .addOnSuccessListener(querySnapshot -> {
                boolean hasQuestions = !querySnapshot.isEmpty();
                int questionCount = querySnapshot.size();
                
                Log.d(TAG, "📊 Întrebări Oltenia găsite: " + questionCount);
                future.complete(hasQuestions);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Eroare la verificarea întrebărilor Oltenia", e);
                future.complete(false);
            });
        
        return future;
    }
    
    /**
     * Configurează quiz-ul Oltenia doar dacă nu este deja configurat
     * Această metodă este recomandată pentru utilizarea în producție
     * 
     * @param context Context-ul aplicației
     * @return CompletableFuture care se completează când verificarea și încărcarea se termină
     */
    public static CompletableFuture<Void> setupOlteniaQuestionsIfNeeded(Context context) {
        return setupOlteniaQuestionsIfNeeded(context, true);
    }
    
    /**
     * Configurează quiz-ul Oltenia doar dacă nu este deja configurat, cu opțiune de toast-uri
     * 
     * @param context Context-ul aplicației
     * @param showToasts Dacă să afișeze toast-uri pentru progres
     * @return CompletableFuture care se completează când verificarea și încărcarea se termină
     */
    public static CompletableFuture<Void> setupOlteniaQuestionsIfNeeded(Context context, boolean showToasts) {
        Log.d(TAG, "🔍 Verifică și configurează quiz-ul Oltenia dacă este necesar...");
        
        return areOlteniaQuestionsLoaded(context)
            .thenCompose(questionsExist -> {
                if (questionsExist) {
                    Log.d(TAG, "✅ Întrebările Oltenia sunt deja încărcate, nu este nevoie de configurare");
                    if (showToasts) {
                        Toast.makeText(context, "Quiz-ul Oltenia este deja configurat ✅", Toast.LENGTH_SHORT).show();
                    }
                    return CompletableFuture.completedFuture(null);
                } else {
                    Log.d(TAG, "📤 Întrebările Oltenia nu sunt încărcate, începe configurarea...");
                    if (showToasts) {
                        Toast.makeText(context, "Se configurează quiz-ul Oltenia pentru prima dată...", 
                                     Toast.LENGTH_SHORT).show();
                    }
                    return setupOlteniaQuestions(context, showToasts);
                }
            });
    }
    
    /**
     * Afișează informații despre configurarea quiz-ului Oltenia
     * Util pentru debugging sau pentru informarea utilizatorului
     * 
     * @param context Context-ul aplicației
     */
    public static void showOlteniaQuizInfo(Context context) {
        areOlteniaQuestionsLoaded(context)
            .thenAccept(questionsLoaded -> {
                if (questionsLoaded) {
                    Toast.makeText(context, 
                        "Quiz Oltenia: ✅ Configurat\n" +
                        "• 40 de întrebări\n" +
                        "• 8 categorii\n" +
                        "• Gata de utilizare!", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, 
                        "Quiz Oltenia: ❌ Nu este configurat\n" +
                        "Folosește OlteniaSetupHelper.setupOlteniaQuestions() pentru configurare", 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    /**
     * Exemple de utilizare pentru dezvoltatori
     */
    public static class UsageExamples {
        
        /**
         * Exemplu 1: Configurare simplă cu o linie
         */
        public static void example1_SimpleSetup(Context context) {
            OlteniaSetupHelper.setupOlteniaQuestions(context);
        }
        
        /**
         * Exemplu 2: Configurare inteligentă (evită duplicate)
         */
        public static void example2_SmartSetup(Context context) {
            OlteniaSetupHelper.setupOlteniaQuestionsIfNeeded(context);
        }
        
        /**
         * Exemplu 3: Configurare cu callback personalizat
         */
        public static void example3_SetupWithCallback(Context context) {
            OlteniaSetupHelper.setupOlteniaQuestions(context)
                .thenRun(() -> {
                    Log.d("MyApp", "Quiz-ul Oltenia este gata!");
                    // Aici poți adăuga logica ta personalizată
                })
                .exceptionally(throwable -> {
                    Log.e("MyApp", "Eroare la configurarea quiz-ului", throwable);
                    return null;
                });
        }
        
        /**
         * Exemplu 4: Verificare status fără încărcare
         */
        public static void example4_CheckStatus(Context context) {
            OlteniaSetupHelper.areOlteniaQuestionsLoaded(context)
                .thenAccept(loaded -> {
                    if (loaded) {
                        Log.d("MyApp", "Quiz-ul Oltenia este disponibil");
                    } else {
                        Log.d("MyApp", "Quiz-ul Oltenia nu este configurat");
                    }
                });
        }
    }
} 