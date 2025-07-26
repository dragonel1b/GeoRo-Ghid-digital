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
import com.example.myapplication.models.FirestoreQuestionModel;
import com.example.myapplication.repository.FirestoreQuestionRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    }
    
    private void initializeViews() {
        regionSpinner = findViewById(R.id.regionSpinner);
        gameTypeSpinner = findViewById(R.id.gameTypeSpinner);
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        addQuestionFab = findViewById(R.id.addQuestionFab);
        migrateButton = findViewById(R.id.migrateButton);
        uploadOlteniaSampleButton = findViewById(R.id.uploadOlteniaSampleButton);
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
                
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    FirestoreQuestionModel question = document.toObject(FirestoreQuestionModel.class);
                    if (question != null) {
                        // Setăm ID-ul documentului
                        question.setId(document.getId());
                        questionsList.add(question);
                    }
                }
                
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
} 