package com.example.myapplication.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.QuestionAdapter;
import com.example.myapplication.core.domain.model.FirestoreQuestionModel;
import com.example.myapplication.core.domain.model.CuriosityModel;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.Intent;
import com.example.myapplication.utils.FirebaseCrashlyticsManager;

/**
 * Activitate pentru gestionarea întrebărilor în Firebase Firestore
 * Permite adăugarea, editarea și ștergerea întrebărilor pentru fiecare regiune și joc
 */
public class FirebaseQuizManagerActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseQuizManager";

    // Repository pentru accesul la Firestore
    private FirestoreQuestionRepository questionRepository;
    
    // UI Components
    private Spinner regionSpinner;
    private Spinner gameTypeSpinner;
    private RecyclerView questionsRecyclerView;
    private FloatingActionButton addQuestionFab;
    private Button migrateButton;
    private Button uploadOlteniaSampleButton;
    private Button aiGenerateQuestionButton;
    private Button aiChatbotButton;
    private Button localAIButton;

    
    // Data
    private List<FirestoreQuestionModel> questionsList = new ArrayList<>();
    private QuestionAdapter adapter;
    private String selectedRegion = "transilvania";
    private String selectedGameType = "quiz";
    
    // Regiuni și tipuri de joc
    private final String[] regions = {
        "transilvania", "muntenia", "oltenia", "moldova", 
        "dobrogea", "banat", "crisana", "maramures", "bucovina"
    };
    
    private final String[] gameTypes = {
        "quiz", "memory", "puzzle", "matching"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_quiz_manager);
        
        // Inițializăm repository-ul
        questionRepository = FirestoreQuestionRepository.getInstance();
        
        // Inițializăm UI components
        initializeViews();
        setupSpinners();
        setupRecyclerView();
        
        // Încărcăm întrebările pentru selecția inițială
        loadQuestions();
        
        // Setup listeners
        addQuestionFab.setOnClickListener(v -> showAddQuestionDialog());
        migrateButton.setOnClickListener(v -> showMigrateDialog());
        uploadOlteniaSampleButton.setOnClickListener(v -> uploadSampleOlteniaQuestion());
        aiGenerateQuestionButton.setOnClickListener(v -> showAIGenerateQuestionDialog());
        aiChatbotButton.setOnClickListener(v -> showAIChatbotDialog());
        localAIButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocalAIActivity.class);
            startActivity(intent);
        });

        
        // Adăugăm un buton pentru testarea Firebase
        Button testFirebaseButton = findViewById(R.id.testFirebaseButton);
        if (testFirebaseButton != null) {
            testFirebaseButton.setOnClickListener(v -> testFirebaseConnection());
        }
    }
    
    private void initializeViews() {
        regionSpinner = findViewById(R.id.regionSpinner);
        gameTypeSpinner = findViewById(R.id.gameTypeSpinner);
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        addQuestionFab = findViewById(R.id.addQuestionFab);
        migrateButton = findViewById(R.id.migrateButton);
        uploadOlteniaSampleButton = findViewById(R.id.uploadOlteniaSampleButton);
        aiGenerateQuestionButton = findViewById(R.id.aiGenerateQuestionButton);
        aiChatbotButton = findViewById(R.id.aiChatbotButton);
        localAIButton = findViewById(R.id.localAIButton);
        
        // Verifică dacă toate butoanele au fost găsite
        Log.d(TAG, "Initializing views...");
        Log.d(TAG, "migrateButton: " + (migrateButton != null ? "OK" : "NULL"));
        Log.d(TAG, "uploadOlteniaSampleButton: " + (uploadOlteniaSampleButton != null ? "OK" : "NULL"));
        Log.d(TAG, "aiGenerateQuestionButton: " + (aiGenerateQuestionButton != null ? "OK" : "NULL"));
        Log.d(TAG, "aiChatbotButton: " + (aiChatbotButton != null ? "OK" : "NULL"));
        Log.d(TAG, "localAIButton: " + (localAIButton != null ? "OK" : "NULL"));

        Log.d(TAG, "addQuestionFab: " + (addQuestionFab != null ? "OK" : "NULL"));
        

    }
    
    private void setupSpinners() {
        // Setup region spinner
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, regions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
        
        // Setup game type spinner
        ArrayAdapter<String> gameTypeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, gameTypes);
        gameTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameTypeSpinner.setAdapter(gameTypeAdapter);
        
        // Setup listeners
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRegion = regions[position];
                loadQuestions();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGameType = gameTypes[position];
                loadQuestions();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void setupRecyclerView() {
        adapter = new QuestionAdapter(
            questionsList, 
            this::showEditQuestionDialog, 
            this::deleteQuestion
        );
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setAdapter(adapter);
    }
    
    private void loadQuestions() {
        Log.d(TAG, "Loading questions for region: " + selectedRegion + ", gameType: " + selectedGameType);
        
        // Arătăm un progress indicator
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        
        // Golim lista curentă
        questionsList.clear();
        adapter.notifyDataSetChanged();
        
        // Încărcăm întrebările pentru regiunea și jocul selectat
        questionRepository.getQuestions(selectedRegion, selectedGameType)
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Ascundem progress indicator
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                
                Log.d(TAG, "Query successful. Found " + queryDocumentSnapshots.size() + " documents");
                
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Log.d(TAG, "Processing document: " + document.getId());
                    FirestoreQuestionModel question = document.toObject(FirestoreQuestionModel.class);
                    if (question != null) {
                        // Setăm ID-ul documentului
                        question.setId(document.getId());
                        questionsList.add(question);
                        Log.d(TAG, "Added question: " + question.getQuestion());
                    } else {
                        Log.w(TAG, "Failed to convert document to FirestoreQuestionModel: " + document.getId());
                    }
                }
                
                Log.d(TAG, "Total questions loaded: " + questionsList.size());
                
                // Actualizăm RecyclerView
                adapter.notifyDataSetChanged();
                
                // Actualizăm UI în funcție de rezultate
                updateEmptyState();
            })
            .addOnFailureListener(e -> {
                // Ascundem progress indicator
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                
                // Afișăm eroarea
                Log.e(TAG, "Error loading questions", e);
                Toast.makeText(this, "Eroare la încărcarea întrebărilor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                
                // Actualizăm UI pentru starea goală
                updateEmptyState();
            });
    }
    
    private void updateEmptyState() {
        if (questionsList.isEmpty()) {
            findViewById(R.id.emptyStateLayout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.emptyStateLayout).setVisibility(View.GONE);
        }
    }
    
    private void showAddQuestionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_question, null);
        
        // Inițializăm câmpurile din dialog
        EditText questionEditText = dialogView.findViewById(R.id.questionEditText);
        EditText correctAnswerEditText = dialogView.findViewById(R.id.correctAnswerEditText);
        EditText incorrectAnswer1EditText = dialogView.findViewById(R.id.incorrectAnswer1EditText);
        EditText incorrectAnswer2EditText = dialogView.findViewById(R.id.incorrectAnswer2EditText);
        EditText incorrectAnswer3EditText = dialogView.findViewById(R.id.incorrectAnswer3EditText);
        EditText factEditText = dialogView.findViewById(R.id.factEditText);
        EditText hintEditText = dialogView.findViewById(R.id.hintEditText);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Adaugă întrebare nouă")
            .setView(dialogView)
            .setPositiveButton("Adaugă", (dialog, which) -> {
                // Validăm datele
                String question = questionEditText.getText().toString().trim();
                String correctAnswer = correctAnswerEditText.getText().toString().trim();
                String incorrectAnswer1 = incorrectAnswer1EditText.getText().toString().trim();
                String incorrectAnswer2 = incorrectAnswer2EditText.getText().toString().trim();
                String incorrectAnswer3 = incorrectAnswer3EditText.getText().toString().trim();
                String fact = factEditText.getText().toString().trim();
                String hint = hintEditText.getText().toString().trim();
                
                if (question.isEmpty() || correctAnswer.isEmpty() || incorrectAnswer1.isEmpty()) {
                    Toast.makeText(this, "Completați cel puțin întrebarea, răspunsul corect și un răspuns incorect", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Creăm lista de răspunsuri incorecte
                List<String> incorrectAnswers = new ArrayList<>();
                incorrectAnswers.add(incorrectAnswer1);
                
                if (!incorrectAnswer2.isEmpty()) {
                    incorrectAnswers.add(incorrectAnswer2);
                }
                
                if (!incorrectAnswer3.isEmpty()) {
                    incorrectAnswers.add(incorrectAnswer3);
                }
                
                // Creăm modelul întrebării
                FirestoreQuestionModel newQuestion = new FirestoreQuestionModel(
                    question, correctAnswer, incorrectAnswers, fact, hint, "", selectedRegion, selectedGameType
                );
                
                // Adăugăm întrebarea în Firestore
                addQuestion(newQuestion);
            })
            .setNegativeButton("Anulează", null)
            .show();
    }
    
    private void showEditQuestionDialog(FirestoreQuestionModel question) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_question, null);
        
        // Inițializăm câmpurile din dialog cu valorile existente
        EditText questionEditText = dialogView.findViewById(R.id.questionEditText);
        EditText correctAnswerEditText = dialogView.findViewById(R.id.correctAnswerEditText);
        EditText incorrectAnswer1EditText = dialogView.findViewById(R.id.incorrectAnswer1EditText);
        EditText incorrectAnswer2EditText = dialogView.findViewById(R.id.incorrectAnswer2EditText);
        EditText incorrectAnswer3EditText = dialogView.findViewById(R.id.incorrectAnswer3EditText);
        EditText factEditText = dialogView.findViewById(R.id.factEditText);
        EditText hintEditText = dialogView.findViewById(R.id.hintEditText);
        
        // Populăm câmpurile cu datele existente
        questionEditText.setText(question.getQuestion());
        correctAnswerEditText.setText(question.getCorrectAnswer());
        
        List<String> incorrectAnswers = question.getIncorrectAnswers();
        if (incorrectAnswers.size() > 0) {
            incorrectAnswer1EditText.setText(incorrectAnswers.get(0));
        }
        if (incorrectAnswers.size() > 1) {
            incorrectAnswer2EditText.setText(incorrectAnswers.get(1));
        }
        if (incorrectAnswers.size() > 2) {
            incorrectAnswer3EditText.setText(incorrectAnswers.get(2));
        }
        
        factEditText.setText(question.getFact());
        hintEditText.setText(question.getHint());
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Editează întrebarea")
            .setView(dialogView)
            .setPositiveButton("Salvează", (dialog, which) -> {
                // Validăm datele
                String questionText = questionEditText.getText().toString().trim();
                String correctAnswer = correctAnswerEditText.getText().toString().trim();
                String incorrectAnswer1 = incorrectAnswer1EditText.getText().toString().trim();
                String incorrectAnswer2 = incorrectAnswer2EditText.getText().toString().trim();
                String incorrectAnswer3 = incorrectAnswer3EditText.getText().toString().trim();
                String fact = factEditText.getText().toString().trim();
                String hint = hintEditText.getText().toString().trim();
                
                if (questionText.isEmpty() || correctAnswer.isEmpty() || incorrectAnswer1.isEmpty()) {
                    Toast.makeText(this, "Completați cel puțin întrebarea, răspunsul corect și un răspuns incorect", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Creăm lista de răspunsuri incorecte
                List<String> updatedIncorrectAnswers = new ArrayList<>();
                updatedIncorrectAnswers.add(incorrectAnswer1);
                
                if (!incorrectAnswer2.isEmpty()) {
                    updatedIncorrectAnswers.add(incorrectAnswer2);
                }
                
                if (!incorrectAnswer3.isEmpty()) {
                    updatedIncorrectAnswers.add(incorrectAnswer3);
                }
                
                // Actualizăm modelul întrebării
                question.setQuestion(questionText);
                question.setCorrectAnswer(correctAnswer);
                question.setIncorrectAnswers(updatedIncorrectAnswers);
                question.setFact(fact);
                question.setHint(hint);
                
                // Actualizăm întrebarea în Firestore
                updateQuestion(question);
            })
            .setNegativeButton("Anulează", null)
            .show();
    }
    
    private void addQuestion(FirestoreQuestionModel question) {
        // Arătăm un progress indicator
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        
        questionRepository.addQuestion(question)
            .addOnSuccessListener(documentReference -> {
                // Ascundem progress indicator
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                
                // Setăm ID-ul documentului și adăugăm în lista locală
                question.setId(documentReference.getId());
                questionsList.add(question);
                adapter.notifyItemInserted(questionsList.size() - 1);
                
                // Actualizăm UI
                updateEmptyState();
                
                Toast.makeText(this, "Întrebare adăugată cu succes", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                // Ascundem progress indicator
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                
                Log.e(TAG, "Error adding question", e);
                Toast.makeText(this, "Eroare la adăugarea întrebării: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateQuestion(FirestoreQuestionModel question) {
        // Arătăm un progress indicator
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        
        questionRepository.updateQuestion(question)
            .addOnSuccessListener(aVoid -> {
                // Ascundem progress indicator
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                
                // Actualizăm RecyclerView
                int position = questionsList.indexOf(question);
                if (position != -1) {
                    adapter.notifyItemChanged(position);
                }
                
                Toast.makeText(this, "Întrebare actualizată cu succes", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                // Ascundem progress indicator
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                
                Log.e(TAG, "Error updating question", e);
                Toast.makeText(this, "Eroare la actualizarea întrebării: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void deleteQuestion(FirestoreQuestionModel question) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Șterge întrebarea")
            .setMessage("Ești sigur că vrei să ștergi această întrebare?")
            .setPositiveButton("Șterge", (dialog, which) -> {
                // Arătăm un progress indicator
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                
                questionRepository.deleteQuestion(question.getId(), selectedRegion, selectedGameType)
                    .addOnSuccessListener(aVoid -> {
                        // Ascundem progress indicator
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        
                        // Eliminăm din lista locală
                        int position = questionsList.indexOf(question);
                        if (position != -1) {
                            questionsList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                        
                        // Actualizăm UI
                        updateEmptyState();
                        
                        Toast.makeText(this, "Întrebare ștearsă cu succes", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Ascundem progress indicator
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        
                        Log.e(TAG, "Error deleting question", e);
                        Toast.makeText(this, "Eroare la ștergerea întrebării: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Anulează", null)
            .show();
    }
    
    private void showMigrateDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Migrare întrebări")
            .setMessage("Această acțiune va încerca să migreze întrebările din codul sursă în Firebase Firestore. Continuați?")
            .setPositiveButton("Da", (dialog, which) -> migrateQuestions())
            .setNegativeButton("Nu", null)
            .show();
    }
    
    private void migrateQuestions() {
        // Arătăm un progress indicator
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        
        // Obținem întrebările din activitatea selectată
        Object[] questionsArray = questionRepository.getQuestionsFromActivity(selectedRegion);
        
        if (questionsArray != null && questionsArray.length > 0) {
            // Migrăm întrebările
            questionRepository.migrateQuestionsFromSource(questionsArray, selectedRegion, selectedGameType)
                .thenAccept(v -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        
                        Toast.makeText(this, "Migrare completă", Toast.LENGTH_SHORT).show();
                        // Reîncărcăm întrebările
                        loadQuestions();
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        
                        Log.e(TAG, "Error migrating questions", e);
                        Toast.makeText(this, "Eroare la migrarea întrebărilor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
        } else {
            // Ascundem progress indicator
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            
            Toast.makeText(this, "Nu s-au găsit întrebări de migrat pentru " + selectedRegion, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uploadSampleOlteniaQuestion() {
        if ("dobrogea".equals(selectedRegion)) {
            uploadSampleDobrogeaQuestions();
            return;
        }
        if ("moldova".equals(selectedRegion)) {
            uploadSampleMoldovaQuestions();
            return;
        }
        if ("muntenia".equals(selectedRegion)) {
            uploadSampleMunteniaQuestions();
            return;
        }
        if ("banat".equals(selectedRegion)) {
            uploadSampleBanatQuestions();
            return;
        }
        if ("crisana".equals(selectedRegion)) {
            uploadSampleCrisanaQuestions();
            return;
        }
        if ("maramures".equals(selectedRegion)) {
            uploadSampleMaramuresQuestions();
            return;
        }
        if ("bucovina".equals(selectedRegion)) {
            uploadSampleBucovinaQuestions();
            return;
        }
        // Întrebările hardcodate pentru toate regiunile au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void uploadSampleDobrogeaQuestions() {
        // Întrebările hardcodate pentru Dobrogea au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void uploadSampleMoldovaQuestions() {
        // Întrebările hardcodate pentru Moldova au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void uploadSampleMunteniaQuestions() {
        // Întrebările hardcodate pentru Muntenia au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void uploadSampleBanatQuestions() {
        // Întrebările hardcodate pentru Banat au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void uploadSampleCrisanaQuestions() {
        // Întrebările hardcodate pentru Crișana au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void uploadSampleMaramuresQuestions() {
        // Întrebările hardcodate pentru Maramureș au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void uploadSampleBucovinaQuestions() {
        // Întrebările hardcodate pentru Bucovina au fost eliminate pentru a preveni adăugarea accidentală.
        Toast.makeText(this, "Nu există întrebări predefinite pentru upload rapid!", Toast.LENGTH_LONG).show();
    }

    private void showAIGenerateQuestionDialog() {
        final EditText input = new EditText(this);
        input.setHint("Subiect întrebare (ex: Bucovina, istorie, mâncare)");
        new MaterialAlertDialogBuilder(this)
            .setTitle("Generează întrebare AI")
            .setView(input)
            .setPositiveButton("Generează", (dialog, which) -> {
                String topic = input.getText().toString().trim();
                if (!topic.isEmpty()) {
                    showProgressBar();
                    com.example.myapplication.utils.OpenAIHelper.generateQuizQuestion(topic, response -> runOnUiThread(() -> {
                        hideProgressBar();
                        showAIResultDialogWithSave("Întrebare generată", response, topic);
                    }));
                }
            })
            .setNegativeButton("Anulează", null)
            .show();
    }

    // Dialog cu opțiune de salvare în Firestore
    private void showAIResultDialogWithSave(String title, String message, String topic) {
        if (message == null || message.trim().isEmpty()) {
            showAIResultDialog(title, message);
            return;
        }
        // Precompletăm câmpurile pe baza răspunsului AI
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_question, null);
        EditText questionEditText = dialogView.findViewById(R.id.questionEditText);
        EditText correctAnswerEditText = dialogView.findViewById(R.id.correctAnswerEditText);
        EditText incorrectAnswer1EditText = dialogView.findViewById(R.id.incorrectAnswer1EditText);
        EditText incorrectAnswer2EditText = dialogView.findViewById(R.id.incorrectAnswer2EditText);
        EditText incorrectAnswer3EditText = dialogView.findViewById(R.id.incorrectAnswer3EditText);
        EditText factEditText = dialogView.findViewById(R.id.factEditText);
        EditText hintEditText = dialogView.findViewById(R.id.hintEditText);
        // Parsez mesajul AI pentru a extrage întrebare și răspunsuri (simplu, pe linii)
        String[] lines = message.split("\n");
        String question = "";
        String correct = "";
        List<String> incorrects = new ArrayList<>();
        for (String line : lines) {
            if (line.toLowerCase().contains("întrebare") || line.toLowerCase().contains("question")) {
                question = line.replaceFirst("(?i)întrebare: ", "").replaceFirst("(?i)question: ", "").trim();
            } else if (line.toLowerCase().contains("corect") || line.toLowerCase().contains("correct")) {
                correct = line.replaceFirst("(?i)răspuns corect: ", "").replaceFirst("(?i)correct answer: ", "").trim();
            } else if (line.toLowerCase().contains("variantă") || line.toLowerCase().contains("incorect")) {
                String ans = line.replaceFirst("(?i)variantă incorectă: ", "").replaceFirst("(?i)incorrect answer: ", "").trim();
                if (!ans.isEmpty()) incorrects.add(ans);
            } else if (line.matches("^[A-Da-d][\\).].*")) {
                // Format tip A) B) C) D)
                String ans = line.substring(2).trim();
                if (line.toLowerCase().contains("corect")) correct = ans;
                else incorrects.add(ans);
            }
        }
        // Fallback: dacă nu am găsit, pun tot mesajul ca întrebare
        if (question.isEmpty()) question = message;
        questionEditText.setText(question);
        correctAnswerEditText.setText(correct);
        if (incorrects.size() > 0) incorrectAnswer1EditText.setText(incorrects.get(0));
        if (incorrects.size() > 1) incorrectAnswer2EditText.setText(incorrects.get(1));
        if (incorrects.size() > 2) incorrectAnswer3EditText.setText(incorrects.get(2));
        // Fact și hint lăsate goale
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Salvează în Firestore", (dialog, which) -> {
                String q = questionEditText.getText().toString().trim();
                String c = correctAnswerEditText.getText().toString().trim();
                List<String> inc = new ArrayList<>();
                if (!incorrectAnswer1EditText.getText().toString().trim().isEmpty())
                    inc.add(incorrectAnswer1EditText.getText().toString().trim());
                if (!incorrectAnswer2EditText.getText().toString().trim().isEmpty())
                    inc.add(incorrectAnswer2EditText.getText().toString().trim());
                if (!incorrectAnswer3EditText.getText().toString().trim().isEmpty())
                    inc.add(incorrectAnswer3EditText.getText().toString().trim());
                String fact = factEditText.getText().toString().trim();
                String hint = hintEditText.getText().toString().trim();
                if (q.isEmpty() || c.isEmpty() || inc.isEmpty()) {
                    Toast.makeText(this, "Completează cel puțin întrebare, răspuns corect și un răspuns incorect!", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirestoreQuestionModel newQuestion = new FirestoreQuestionModel(
                    q, c, inc, fact, hint, "", selectedRegion, selectedGameType
                );
                showProgressBar();
                questionRepository.addQuestion(newQuestion)
                    .addOnSuccessListener(documentReference -> {
                        hideProgressBar();
                        Toast.makeText(this, "Întrebare salvată cu succes!", Toast.LENGTH_SHORT).show();
                        loadQuestions();
                    })
                    .addOnFailureListener(e -> {
                        hideProgressBar();
                        Toast.makeText(this, "Eroare la salvare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Anulează", null)
            .setNeutralButton("Doar vizualizează", (dialog, which) -> showAIResultDialog(title, message))
            .show();
    }

    private void showAIChatbotDialog() {
        final EditText input = new EditText(this);
        input.setHint("Întrebare pentru AI (ex: Spune-mi o curiozitate)");
        new MaterialAlertDialogBuilder(this)
            .setTitle("Chatbot AI")
            .setView(input)
            .setPositiveButton("Trimite", (dialog, which) -> {
                String message = input.getText().toString().trim();
                if (!message.isEmpty()) {
                    showProgressBar();
                    com.example.myapplication.utils.OpenAIHelper.sendChatMessage(message, response -> runOnUiThread(() -> {
                        hideProgressBar();
                        showAIChatbotResultDialogWithSave("Răspuns AI", response, message);
                    }));
                }
            })
            .setNegativeButton("Anulează", null)
            .show();
    }

    // Dialog cu opțiune de salvare a răspunsului AI ca feedback/curiozitate
    private void showAIChatbotResultDialogWithSave(String title, String message, String userQuestion) {
        if (message == null || message.trim().isEmpty()) {
            showAIResultDialog(title, message);
            return;
        }
        final EditText editText = new EditText(this);
        editText.setText(message);
        editText.setHint("Editează răspunsul AI dacă vrei");
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Salvează ca feedback/curiozitate", (dialog, which) -> {
                String textToSave = editText.getText().toString().trim();
                if (textToSave.isEmpty()) {
                    Toast.makeText(this, "Nu poți salva un răspuns gol!", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveAIChatbotFeedback(userQuestion, textToSave);
            })
            .setNegativeButton("Anulează", null)
            .setNeutralButton("Doar vizualizează", (dialog, which) -> showAIResultDialog(title, message))
            .show();
    }

    // Exemplu de salvare în Firestore (poți adapta structura după nevoie)
    private void saveAIChatbotFeedback(String userQuestion, String aiResponse) {
        String region = selectedRegion;
        String type = "curiozitate";
        String source = "AI";
        Timestamp timestamp = Timestamp.now();
        CuriosityModel curiosity = new CuriosityModel(userQuestion, aiResponse, region, type, timestamp, source);
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("ai_curiosities")
            .add(curiosity)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Curiozitate AI salvată cu succes!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Eroare la salvare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showAIResultDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message == null ? "Eroare la generare sau răspuns gol." : message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showProgressBar() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }
    private void hideProgressBar() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }


    
    private void testFirebaseConnection() {
        Log.d(TAG, "Testing Firebase connection...");
        
        // Testăm dacă putem accesa Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("test")
            .document("test")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Log.d(TAG, "Firebase connection successful");
                Toast.makeText(this, "Firebase connection OK", Toast.LENGTH_SHORT).show();
                
                // Testăm și structura pentru întrebări
                testQuestionsStructure();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firebase connection failed", e);
                Toast.makeText(this, "Firebase connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void testQuestionsStructure() {
        Log.d(TAG, "Testing questions structure...");
        
        // Testăm dacă există colecția regions
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("regions")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d(TAG, "Regions collection exists with " + querySnapshot.size() + " documents");
                
                // Testăm dacă există documentul pentru transilvania
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("regions")
                    .document("transilvania")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Transilvania document exists");
                            
                            // Testăm dacă există colecția games
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("regions")
                                .document("transilvania")
                                .collection("games")
                                .get()
                                .addOnSuccessListener(gamesSnapshot -> {
                                    Log.d(TAG, "Games collection exists with " + gamesSnapshot.size() + " documents");
                                    
                                    // Testăm dacă există documentul pentru quiz
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("regions")
                                        .document("transilvania")
                                        .collection("games")
                                        .document("quiz")
                                        .get()
                                        .addOnSuccessListener(quizSnapshot -> {
                                            if (quizSnapshot.exists()) {
                                                Log.d(TAG, "Quiz document exists");
                                                
                                                // Testăm dacă există colecția questions
                                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                    .collection("regions")
                                                    .document("transilvania")
                                                    .collection("games")
                                                    .document("quiz")
                                                    .collection("questions")
                                                    .get()
                                                    .addOnSuccessListener(questionsSnapshot -> {
                                                        Log.d(TAG, "Questions collection exists with " + questionsSnapshot.size() + " documents");
                                                        Toast.makeText(this, "Found " + questionsSnapshot.size() + " questions in Firebase", Toast.LENGTH_LONG).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Questions collection access failed", e);
                                                        Toast.makeText(this, "Questions collection access failed", Toast.LENGTH_LONG).show();
                                                    });
                                            } else {
                                                Log.w(TAG, "Quiz document does not exist");
                                                Toast.makeText(this, "Quiz document does not exist", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Quiz document access failed", e);
                                            Toast.makeText(this, "Quiz document access failed", Toast.LENGTH_LONG).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Games collection access failed", e);
                                Toast.makeText(this, "Games collection access failed", Toast.LENGTH_LONG).show();
                            });
                        } else {
                            Log.w(TAG, "Transilvania document does not exist");
                            Toast.makeText(this, "Transilvania document does not exist", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Transilvania document access failed", e);
                        Toast.makeText(this, "Transilvania document access failed", Toast.LENGTH_LONG).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Regions collection access failed", e);
                Toast.makeText(this, "Regions collection access failed", Toast.LENGTH_LONG).show();
            });
    }
} 