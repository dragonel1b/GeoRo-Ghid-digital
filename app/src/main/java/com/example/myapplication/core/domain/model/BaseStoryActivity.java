package com.example.myapplication.core.domain.model;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.BaseStoryNode;
import com.example.myapplication.utils.PointsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Clasă de bază pentru toate activitățile de poveste din diferite regiuni
 * Implementează funcționalitatea comună pentru navigarea prin nodurile poveștii
 */
public abstract class BaseStoryActivity extends AppCompatActivity {
    
    // Variabile pentru UI
    protected TextView titleText;
    protected TextView storyText;
    protected TextView contextText;
    protected Button[] choiceButtons;
    protected CardView[] choiceCards;
    protected ImageView storyImage;
    protected ProgressBar progressBar;
    
    // Variabile pentru logică
    protected int currentNodeId = 0;
    protected int totalPoints = 0;
    protected List<BaseStoryNode> storyNodes;
    protected Map<Integer, BaseStoryNode> storyNodesMap;
    protected String region;
    
    // UI Components
    protected TextView storyTitle;
    protected TextView storyContext;
    protected TextView factTextView;
    protected Button nextButton;
    protected Button backButton;
    protected ImageView soundButton;
    
    // Story state
    protected PointsManager pointsManager;
    protected TextToSpeech textToSpeech;
    protected boolean isSoundEnabled = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fiecare subclasă trebuie să seteze propriul layout
        
        // Inițializarea este făcută în subclase
        
        // Subclasele trebuie să apeleze initializeCommonViews() și initializeStory()
        
        // Inițializăm managerul de puncte
        pointsManager = PointsManager.getInstance(this);
        
        // Inițializăm TextToSpeech
        initializeTextToSpeech();
    }
    
    /**
     * Inițializează elementele comune de UI
     */
    protected void initializeCommonViews() {
        titleText = findViewById(R.id.storyTitle);
        storyText = findViewById(R.id.storyText);
        
        // Verificăm dacă există contextText în layout
        int contextTextId = getResources().getIdentifier("contextText", "id", getPackageName());
        if (contextTextId != 0) {
            contextText = findViewById(contextTextId);
        }
        
        storyImage = findViewById(R.id.storyImage);
        progressBar = findViewById(R.id.progressBar);
        
        storyTitle = findViewById(R.id.storyTitle);
        storyContext = findViewById(R.id.storyContext);
        factTextView = findViewById(R.id.factTextView);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);
        
        // Verificăm dacă există soundButton în layout
        int soundButtonId = getResources().getIdentifier("soundButton", "id", getPackageName());
        if (soundButtonId != 0) {
            soundButton = findViewById(soundButtonId);
            
            if (soundButton != null) {
                soundButton.setOnClickListener(v -> toggleSound());
            }
        }
        
        // Configurăm butoanele
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> handleNextButton());
        }
        
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
        
        // Inițializarea butoanelor pentru alegeri trebuie făcută în subclase
        // deoarece ID-urile pot varia
    }
    
    /**
     * Inițializează nodurile poveștii
     * Trebuie suprascrisă în subclase pentru a adăuga nodurile specifice regiunii
     */
    protected abstract void initializeStory();
    
    /**
     * Pregătește nodurile poveștii pentru navigare rapidă
     */
    protected void prepareStoryNodes() {
        storyNodesMap = new HashMap<>();
        for (BaseStoryNode node : storyNodes) {
            storyNodesMap.put(node.getNodeId(), node);
        }
    }
    
    /**
     * Afișează nodul curent al poveștii
     */
    protected void displayCurrentNode() {
        BaseStoryNode currentNode = storyNodesMap.get(currentNodeId);
        if (currentNode == null) {
            // Nodul nu a fost găsit, terminăm povestea
            finishStory();
            return;
        }
        
        // Afișăm titlul și textul poveștii
        if (currentNode.getTitle() != null && !currentNode.getTitle().isEmpty()) {
            titleText.setText(currentNode.getTitle());
            titleText.setVisibility(View.VISIBLE);
        } else {
            titleText.setVisibility(View.GONE);
        }
        
        storyText.setText(currentNode.getStoryText());
        
        // Afișăm contextul dacă există
        if (contextText != null && currentNode.getContext() != null && !currentNode.getContext().isEmpty()) {
            contextText.setText(currentNode.getContext());
            contextText.setVisibility(View.VISIBLE);
        } else if (contextText != null) {
            contextText.setVisibility(View.GONE);
        }
        
        // Afișăm imaginea dacă există
        if (currentNode.getImageResourceId() != 0) {
            storyImage.setImageResource(currentNode.getImageResourceId());
            storyImage.setVisibility(View.VISIBLE);
        } else {
            storyImage.setVisibility(View.GONE);
        }
        
        // Afișăm faptul interesant dacă există
        if (factTextView != null && currentNode.hasFact()) {
            factTextView.setText(currentNode.getFact());
            factTextView.setVisibility(View.VISIBLE);
        } else if (factTextView != null) {
            factTextView.setVisibility(View.GONE);
        }
        
        // Afișăm opțiunile
        String[] choices = currentNode.getChoices();
        for (int i = 0; i < choiceButtons.length && i < choices.length; i++) {
            choiceButtons[i].setText(choices[i]);
            choiceCards[i].setVisibility(View.VISIBLE);
            
            final int choiceIndex = i;
            choiceButtons[i].setOnClickListener(v -> handleChoice(choiceIndex));
        }
        
        // Ascundem opțiunile neutilizate
        for (int i = choices.length; i < choiceButtons.length; i++) {
            choiceCards[i].setVisibility(View.GONE);
        }
        
        // Adăugăm punctele pentru acest nod
        totalPoints += currentNode.getPointsReward();
        
        // Actualizăm progress bar-ul dacă există
        if (progressBar != null) {
            // Calculăm progresul ca procent din numărul total de noduri
            int progress = (int) ((float) currentNodeId / storyNodes.size() * 100);
            progressBar.setProgress(progress);
        }
        
        // Gestionăm nodurile speciale
        handleSpecialNodeTypes(currentNode);
        
        // Citim textul dacă sunetul este activat
        if (isSoundEnabled && textToSpeech != null) {
            speakText(currentNode.getStoryText());
        }
    }
    
    /**
     * Gestionează tipurile speciale de noduri
     */
    protected void handleSpecialNodeTypes(BaseStoryNode node) {
        if (node.isQuizNode()) {
            // Implementarea specifică pentru noduri de tip quiz
            // Subclasele pot suprascrie această metodă pentru comportament specific
        } else if (node.isInteractiveNode()) {
            // Implementarea specifică pentru noduri interactive
            // Subclasele pot suprascrie această metodă pentru comportament specific
        }
    }
    
    /**
     * Gestionează alegerea utilizatorului
     */
    protected void handleChoice(int choiceIndex) {
        BaseStoryNode currentNode = storyNodesMap.get(currentNodeId);
        if (currentNode == null) return;
        
        // Animație pentru butonul ales
        animateChoiceButton(choiceIndex);
        
        // Determinăm următorul nod
        int nextNodeId = currentNode.getNextNodeForChoice(choiceIndex);
        
        // Trecem la următorul nod după o scurtă pauză
        choiceButtons[choiceIndex].postDelayed(() -> {
            currentNodeId = nextNodeId;
            displayCurrentNode();
        }, 500);
    }
    
    /**
     * Animație pentru butonul ales
     */
    protected void animateChoiceButton(int index) {
        // Animație de apăsare pentru butonul ales
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(choiceCards[index], "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(choiceCards[index], "scaleY", 1f, 0.95f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * Termină povestea și navighează către următoarea activitate
     */
    protected void finishStory() {
        // Subclasele trebuie să implementeze navigarea către următoarea activitate
        // Exemplu:
        // Intent intent = new Intent(this, NextActivity.class);
        // intent.putExtra("totalPoints", totalPoints);
        // intent.putExtra("region", region);
        // startActivity(intent);
        // finish();
    }
    
    /**
     * Salvează progresul poveștii
     */
    protected void saveProgress() {
        // Subclasele pot implementa salvarea progresului
        // Exemplu:
        // SharedPreferences prefs = getSharedPreferences("StoryProgress", MODE_PRIVATE);
        // SharedPreferences.Editor editor = prefs.edit();
        // editor.putInt(region + "_progress", currentNodeId);
        // editor.putInt(region + "_points", totalPoints);
        // editor.apply();
        
        if (pointsManager != null && region != null && !region.isEmpty()) {
            pointsManager.setStoryCompleted(this, region.toLowerCase(), true);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveProgress();
    }
    
    /**
     * Inițializează motorul TextToSpeech pentru citirea textului
     */
    protected void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("ro", "RO"));
                
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Dacă limba română nu este disponibilă, folosim engleza
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
    }
    
    /**
     * Activează sau dezactivează citirea textului
     */
    protected void toggleSound() {
        isSoundEnabled = !isSoundEnabled;
        
        if (soundButton != null) {
            soundButton.setImageResource(isSoundEnabled ? 
                    R.drawable.ic_volume_up : R.drawable.ic_volume_off);
        }
        
        if (isSoundEnabled && textToSpeech != null && storyText != null) {
            speakText(storyText.getText().toString());
        } else if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Citește un text folosind TextToSpeech
     * @param text Textul de citit
     */
    protected void speakText(String text) {
        if (textToSpeech != null && text != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "story_text");
        }
    }
    
    /**
     * Gestionează apăsarea butonului Next
     */
    protected void handleNextButton() {
        BaseStoryNode currentNode = storyNodesMap.get(currentNodeId);
        if (currentNode == null) return;
        
        // Verificăm dacă este un nod de final
        if (currentNode.isEndNode()) {
            finishStory();
            return;
        }
        
        // Trecem la următorul nod
        int nextNodeId = currentNode.getNextNodeForChoice(0);
        currentNodeId = nextNodeId;
        displayCurrentNode();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
} 