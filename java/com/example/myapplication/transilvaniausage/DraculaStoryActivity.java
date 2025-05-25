package com.example.myapplication.transilvaniausage;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.model.TransilvaniaStoryNode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.util.Log;
import androidx.lifecycle.ViewModelProvider;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DraculaStoryActivity extends AppCompatActivity {
    private TextView storyText;
    private TextView storyTitle;
    private TextView storyContext;
    private ImageView storyImageView;
    private MaterialButton redButton;
    private MaterialButton blackButton;
    private MaterialButton nextButton;
    private MaterialButton continueButton;
    private MaterialButton storyButton;
    private MaterialButton exitButton;
    private ImageView headerBackButton;
    private ImageView soundToggleButton;
    private MaterialCardView feedbackCard;
    private MaterialCardView bettingCard;
    private MaterialCardView interactiveCardView;
    private TextView feedbackText;
    private TextView vampireTestTitle;
    private TextView vampireTestDesc;
    private MaterialCardView objectCard1;
    private MaterialCardView objectCard2;
    private MaterialCardView objectCard3;
    private TextInputEditText betAmountInput;
    private LinearProgressIndicator progressIndicator;
    private ImageView batAnimation;
    private PointsManager pointsManager;
    private int currentSceneIndex = 0;
    private Map<Integer, TransilvaniaStoryNode> storyNodes;
    private Random random;
    private boolean isStoryMode = false;
    private int artifactPieces = 0;
    private boolean hasMetDracula = false;
    private boolean hasFoundBlood = false;
    private boolean hasBeenBitten = false;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private int currentPoints = 0;
    private int totalStoryNodes = 0;
    private MediaPlayer backgroundMusic;
    private MediaPlayer soundEffect;
    private boolean isSoundEnabled = true;
    private Animation fadeInAnimation;
    private Animation batFlyAnimation;
    private Handler handler = new Handler();
    private MaterialButton finishButton;
    private DraculaStoryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transilvania_story);

        // Inițializare ViewModel
        viewModel = new ViewModelProvider(this).get(DraculaStoryViewModel.class);

        initializeViews();
        initializeAudio();
        initializeAnimations();
        setupButtonListeners();
        initializeTextToSpeech();
        observeViewModel();
    }

    private void initializeViews() {
        storyText = findViewById(R.id.storyText);
        storyTitle = findViewById(R.id.storyTitle);
        storyContext = findViewById(R.id.storyContext);
        storyImageView = findViewById(R.id.storyImageView);
        redButton = findViewById(R.id.redButton);
        blackButton = findViewById(R.id.blackButton);
        nextButton = findViewById(R.id.nextButton);
        continueButton = findViewById(R.id.continueButton);
        storyButton = findViewById(R.id.storyButton);
        exitButton = findViewById(R.id.exitButton);
        headerBackButton = findViewById(R.id.headerBackButton);
        soundToggleButton = findViewById(R.id.soundToggleButton);
        feedbackCard = findViewById(R.id.feedbackCard);
        bettingCard = findViewById(R.id.bettingCard);
        interactiveCardView = findViewById(R.id.interactiveCardView);
        feedbackText = findViewById(R.id.feedbackText);
        vampireTestTitle = findViewById(R.id.vampireTestTitle);
        vampireTestDesc = findViewById(R.id.vampireTestDesc);
        objectCard1 = findViewById(R.id.objectCard1);
        objectCard2 = findViewById(R.id.objectCard2);
        objectCard3 = findViewById(R.id.objectCard3);
        betAmountInput = findViewById(R.id.betAmountInput);
        progressIndicator = findViewById(R.id.progressIndicator);
        batAnimation = findViewById(R.id.batAnimation);
        
        // Log warning if progressIndicator is not found in the layout
        if (progressIndicator == null) {
            System.out.println("WARNING: LinearProgressIndicator with ID progressIndicator not found in layout");
        }
        
        pointsManager = PointsManager.getInstance(this);
        random = new Random();
        
        // Get current points
        currentPoints = pointsManager.getPoints(this);
        
        // Make sure all elements have proper initial visibility
        if (storyText != null) storyText.setVisibility(View.VISIBLE);
        if (storyTitle != null) storyTitle.setVisibility(View.VISIBLE);
        if (storyContext != null) storyContext.setVisibility(View.VISIBLE);
        if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        if (continueButton != null) continueButton.setVisibility(View.GONE);
        if (exitButton != null) exitButton.setVisibility(View.VISIBLE);
        if (storyButton != null) storyButton.setVisibility(View.VISIBLE);
        if (feedbackCard != null) feedbackCard.setVisibility(View.GONE);
        if (bettingCard != null) bettingCard.setVisibility(View.GONE);
        if (interactiveCardView != null) interactiveCardView.setVisibility(View.GONE);
        if (batAnimation != null) batAnimation.setVisibility(View.GONE);
        
        finishButton = findViewById(R.id.finishButton);
        if (finishButton != null) {
            finishButton.setVisibility(View.GONE);
        }
    }
    
    private void initializeAudio() {
        try {
        // Initialize background music
        backgroundMusic = MediaPlayer.create(this, R.raw.dark_ambient);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.2f, 0.2f);
            backgroundMusic.start();
            } else {
                Log.e("DraculaStoryActivity", "Failed to create background music player");
        }
        
        // Initialize sound effect player
        soundEffect = MediaPlayer.create(this, R.raw.thunder);
            if (soundEffect == null) {
                Log.e("DraculaStoryActivity", "Failed to create sound effect player");
            }
        } catch (Exception e) {
            Log.e("DraculaStoryActivity", "Error initializing audio: " + e.getMessage());
        }
    }
    
    private void initializeAnimations() {
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        batFlyAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_animation);
        
        fadeInAnimation.setDuration(1000);
        batFlyAnimation.setDuration(1500);
    }
    
    private void initializeTextToSpeech() {
        try {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("ro", "RO"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Limba română nu este suportată pentru citire", Toast.LENGTH_SHORT).show();
                        Log.w("DraculaStoryActivity", "Romanian language not supported for TTS");
                }
            } else {
                Toast.makeText(this, "Eroare la inițializarea text-to-speech", Toast.LENGTH_SHORT).show();
                    Log.e("DraculaStoryActivity", "Failed to initialize TTS, status: " + status);
            }
        });
        } catch (Exception e) {
            Log.e("DraculaStoryActivity", "Error initializing TTS: " + e.getMessage());
        }
    }
    
    private void setupButtonListeners() {
        // Add listener for story reading button
        if (storyButton != null) {
            storyButton.setOnClickListener(v -> toggleStoryReading());
        }
        
        // Add listener for exit button
        if (exitButton != null) {
            exitButton.setOnClickListener(v -> finish());
        }
        
        // Add listener for next button
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                currentSceneIndex++;
                animateSceneTransition();
            });
        }
        
        // Add listener for continue button
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                currentSceneIndex++;
                animateSceneTransition();
            });
        }
        
        // Back button in header
        if (headerBackButton != null) {
            headerBackButton.setOnClickListener(v -> finish());
        }
        
        // Sound toggle
        if (soundToggleButton != null) {
            soundToggleButton.setOnClickListener(v -> toggleSound());
        }
        
        // Interactive element cards
        if (objectCard1 != null) {
            objectCard1.setOnClickListener(v -> selectVampireTest(1));
        }
        if (objectCard2 != null) {
            objectCard2.setOnClickListener(v -> selectVampireTest(2));
        }
        if (objectCard3 != null) {
            objectCard3.setOnClickListener(v -> selectVampireTest(3));
        }
        
        // Adaugă listener pentru butonul de încheiere
        if (finishButton != null) {
            finishButton.setOnClickListener(v -> finish());
        }
    }
    
    private void toggleSound() {
        isSoundEnabled = !isSoundEnabled;
        
        if (isSoundEnabled) {
            soundToggleButton.setImageResource(R.drawable.ic_sound_on);
            if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                backgroundMusic.start();
            }
        } else {
            soundToggleButton.setImageResource(R.drawable.ic_sound_off);
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        }
    }

    private void toggleStoryReading() {
        if (isSpeaking) {
            textToSpeech.stop();
            if (storyButton != null) {
                storyButton.setText("Citește povestea");
            }
            isSpeaking = false;
        } else {
            StringBuilder textToRead = new StringBuilder();
            if (storyTitle != null && storyTitle.getVisibility() == View.VISIBLE) {
                textToRead.append(storyTitle.getText()).append(". ");
            }
            if (storyText != null) {
                textToRead.append(storyText.getText());
            }
            if (storyContext != null && storyContext.getVisibility() == View.VISIBLE) {
                textToRead.append(". ").append(storyContext.getText());
            }
            textToSpeech.speak(textToRead.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
            if (storyButton != null) {
                storyButton.setText("Oprește citirea");
            }
            isSpeaking = true;
        }
    }
    
    private void animateSceneTransition() {
        // Stop any ongoing TTS
        if (isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
            if (storyButton != null) {
                storyButton.setText("Citește povestea");
            }
        }
        
        // Play bat animation
        if (batAnimation != null) {
            batAnimation.setVisibility(View.VISIBLE);
            batAnimation.startAnimation(batFlyAnimation);
        }
        
        // Play sound effect if enabled
        playSoundEffect();
        
        // Fade out current content
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        if (storyText != null) storyText.startAnimation(fadeOut);
        if (storyTitle != null) storyTitle.startAnimation(fadeOut);
        if (storyContext != null) storyContext.startAnimation(fadeOut);
        
        // After animation, update the view using ViewModel
        handler.postDelayed(() -> {
            // În loc să apelăm showScene(), actualizăm scena prin ViewModel
            viewModel.moveToNextScene();
            
            if (batAnimation != null) {
                batAnimation.setVisibility(View.GONE);
            }
            
            // Fade in new content
            if (storyText != null) storyText.startAnimation(fadeInAnimation);
            if (storyTitle != null) storyTitle.startAnimation(fadeInAnimation);
            if (storyContext != null) storyContext.startAnimation(fadeInAnimation);
        }, 800);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
        
        if (soundEffect != null) {
            soundEffect.release();
            soundEffect = null;
        }
        
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying() && isSoundEnabled) {
            backgroundMusic.start();
        }
    }

    /**
     * Observă schimbările din ViewModel
     */
    private void observeViewModel() {
        // Observă nodul curent
        viewModel.getCurrentNode().observe(this, node -> {
            if (node != null) {
                updateStoryContent(node);
                updateSceneImage();
                setupChoiceButtons(node);
            }
        });
        
        // Observă mesajele de feedback
        viewModel.getFeedbackMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                showFeedback(message);
            }
        });
        
        // Observă dacă trebuie să arătăm testul vampirului
        viewModel.getShowVampireTest().observe(this, show -> {
            if (show != null && show) {
            showVampireTest();
            }
        });
        
        // Observă dacă trebuie să arătăm ecranul final
        viewModel.getShowFinalScreen().observe(this, show -> {
            if (show != null && show) {
                showFinalScreen();
            }
        });
        
        // Observă rezultatul final al poveștii
        viewModel.getFinalStoryOutcome().observe(this, outcome -> {
            if (outcome != null && !outcome.isEmpty()) {
                showFinalOutcome(outcome);
            }
        });
    }
    
    /**
     * Actualizează conținutul poveștii cu nodul curent
     */
    private void updateStoryContent(TransilvaniaStoryNode node) {
        if (storyTitle != null) storyTitle.setText(node.getTitle());
        if (storyText != null) storyText.setText(node.getContent());
        if (storyContext != null) storyContext.setText(node.getContext());
    }
    
    /**
     * Arată ecranul final cu butonul de încheiere
     */
    private void showFinalScreen() {
        // Ascunde toate elementele interactive
        hideAllInteractiveElements();
        
        // Ascunde butoanele normale
        if (nextButton != null) nextButton.setVisibility(View.GONE);
        if (storyButton != null) storyButton.setVisibility(View.GONE);
        
        // Arată butonul de încheiere
        if (finishButton != null) {
            finishButton.setVisibility(View.VISIBLE);
            finishButton.setText("Finalizează aventura");
        }
        
        // Arată feedback pentru punctele câștigate
        if (viewModel.getCurrentPoints().getValue() != null) {
            int totalPoints = viewModel.getCurrentPoints().getValue();
            showFeedback("Felicitări! Ai acumulat un total de " + totalPoints + " puncte în această aventură!");
        }
    }
    
    /**
     * Arată rezultatul final al poveștii
     */
    private void showFinalOutcome(String outcome) {
        if (storyContext != null && outcome != null) {
            storyContext.setText(outcome);
            
            // Afișează pentru un timp scurt, apoi arată ecranul de finalizare
            new Handler().postDelayed(() -> {
                viewModel.finishStory();
            }, 1500);
        }
    }
    
    /**
     * Gestionează selecția din testul vampirului
     */
    private void selectVampireTest(int choice) {
        viewModel.selectVampireTest(choice);
    }
    
    private void hideAllInteractiveElements() {
        if (bettingCard != null) {
            bettingCard.setVisibility(View.GONE);
        }
        if (feedbackCard != null) {
            feedbackCard.setVisibility(View.GONE);
        }
        if (interactiveCardView != null) {
            interactiveCardView.setVisibility(View.GONE);
        }
        if (continueButton != null) {
            continueButton.setVisibility(View.GONE);
        }
    }

    private String getSceneTitle(int sceneIndex) {
        TransilvaniaStoryNode node = storyNodes.get(sceneIndex);
        if (node != null && node.getTitle() != null && !node.getTitle().isEmpty()) {
            return node.getTitle();
        }
        
        // Default titles based on scene index
        switch (sceneIndex) {
            case 0:
                return "Bine ai venit în Transilvania!";
            case 1:
                return "Legendele locului";
            case 2:
                return "Întâlnirea cu Contele";
            case 3:
                return "Noaptea la han";
            case 6:
                return "Prizonier al Castelului";
            case 7:
                return "Invitația lui Dracula";
            case 10:
                return "Vizitator nocturn";
            case 12:
                return "Catacombele secrete";
            case 16:
                return "Mușcătura";
            case 25:
                return "Testul Vampirului";
            default:
                return "Aventura continuă...";
        }
    }

    private String getSceneContext(int sceneIndex) {
        TransilvaniaStoryNode node = storyNodes.get(sceneIndex);
        if (node != null && node.getContext() != null && !node.getContext().isEmpty()) {
            return node.getContext();
        }
        
        // Default context based on scene index
        switch (sceneIndex) {
            case 0:
                return "Fiecare alegere pe care o faci va influența parcursul poveștii și finalul acesteia.";
            case 25:
                return "Ce fel de creatură ai devenit? Testul îți va dezvălui adevărata natură.";
            default:
                return "Aventura ta prin Transilvania continuă, iar fiecare decizie te aduce mai aproape de adevăr.";
        }
    }

    private void setupChoiceButtons(TransilvaniaStoryNode node) {
        // Create a local variable for the container or use an existing one
        androidx.constraintlayout.widget.ConstraintLayout choicesContainer = findViewById(R.id.optionsContainer); // Using optionsContainer instead
        
        if (choicesContainer == null) {
            // If optionsContainer doesn't exist, log an error and return
            System.out.println("ERROR: Could not find options container");
            return;
        }
        
        choicesContainer.removeAllViews();
        
        if (node.getChoices() != null && node.getChoices().length > 0) {
            // Has choices - show choice buttons
            if (nextButton != null) nextButton.setVisibility(View.GONE);
            
            for (int i = 0; i < node.getChoices().length; i++) {
                String choice = node.getChoices()[i];
                
                MaterialButton choiceButton = new MaterialButton(this);
                choiceButton.setText(choice);
                choiceButton.setBackgroundTintList(getResources().getColorStateList(R.color.design_default_color_secondary));
                
                // Set margin and other properties
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                
                // Configurăm parametrii ConstraintLayout
                params.setMargins(0, 0, 0, 16);
                
                // Pentru primul buton, îl legăm la vârful containerului
                if (i == 0) {
                    params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                } else {
                    // Pentru restul butoanelor, le legăm de butonul anterior
                    params.topToBottom = choicesContainer.getChildAt(i-1).getId();
                }
                
                // Legăm la stânga și dreapta containerului
                params.leftToLeft = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                params.rightToRight = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                
                choiceButton.setLayoutParams(params);
                choiceButton.setId(View.generateViewId());
                
                // Add click listener
                final int choiceIndex = i;
                choiceButton.setOnClickListener(v -> {
                    viewModel.moveToNextScene(choiceIndex);
                    animateSceneTransition();
                });
                
                choicesContainer.addView(choiceButton);
            }
        } else {
            // No choices - show next button
            if (nextButton != null) {
            nextButton.setVisibility(View.VISIBLE);
                nextButton.setOnClickListener(v -> {
                    viewModel.moveToNextScene();
                    animateSceneTransition();
                });
            }
        }
    }
    
    private void showFeedback(String message) {
        if (feedbackCard != null) {
            feedbackCard.setVisibility(View.VISIBLE);
        }
        if (feedbackText != null) {
            feedbackText.setText(message);
        }
        
        // Auto-hide feedback after delay
        handler.postDelayed(() -> {
            if (feedbackCard != null) {
                feedbackCard.setVisibility(View.GONE);
            }
        }, 3000);
    }
    
    private void showVampireTest() {
        // Hide regular story elements
        if (storyTitle != null) {
            storyTitle.setText("Testul Vampirului");
        }
        if (storyText != null) {
            storyText.setText("După toate aventurile tale prin Transilvania, a venit momentul să descoperi ce impact au avut " +
                    "alegerile tale. Alegând unul dintre obiectele de mai jos, vei descoperi ce fel de creatură ai devenit după " +
                    "experiențele tale cu Contele Dracula.");
        }
        if (storyContext != null) {
            storyContext.setText("Fiecare obiect reprezintă o parte din tine. Care te atrage cel mai mult?");
        }
        
        // Show vampire test interface
        if (interactiveCardView != null) {
            interactiveCardView.setVisibility(View.VISIBLE);
        }
        if (nextButton != null) {
            nextButton.setVisibility(View.GONE);
        }
        
        // Set highlight based on previous choices
        if (hasBeenBitten && objectCard1 != null) {
            objectCard1.setStrokeColor(getResources().getColor(R.color.design_default_color_error));
            objectCard1.setStrokeWidth(5);
        }
        
        if (hasFoundBlood && objectCard3 != null) {
            objectCard3.setStrokeColor(getResources().getColor(R.color.design_default_color_error));
            objectCard3.setStrokeWidth(5);
        }
    }
    
    private void updateProgressIndicator() {
        // Only update if progressIndicator exists and totalStoryNodes is greater than 0
        if (progressIndicator != null && totalStoryNodes > 0) {
            int progress = (int) (((float) currentSceneIndex / totalStoryNodes) * 100);
            progressIndicator.setProgress(progress);
        }
    }
    
    private void updateSceneImage() {
        // Set different images based on the current scene
        int imageResource = R.drawable.castle_dracula; // Default image
        
        switch (currentSceneIndex) {
            case 0:
                imageResource = R.drawable.transylvania_village;
                break;
            case 1:
                imageResource = R.drawable.tavern;
                break;
            case 2:
            case 7:
            case 8:
                imageResource = R.drawable.dracula_portrait;
                break;
            case 3:
                imageResource = R.drawable.inn_room;
                break;
            case 6:
                imageResource = R.drawable.castle_gates;
                break;
            case 10:
            case 16:
                imageResource = R.drawable.vampire_bite;
                break;
            case 12:
                imageResource = R.drawable.catacombs;
                break;
            default:
                // Use default castle image
                break;
        }
        
        storyImageView.setImageResource(imageResource);
    }
    
    /**
     * Safely plays a sound effect, handling potential errors
     */
    private void playSoundEffect() {
        if (isSoundEnabled && soundEffect != null) {
            try {
                // Reset sound to start to allow replaying
                soundEffect.seekTo(0);
                soundEffect.start();
            } catch (Exception e) {
                Log.e("DraculaStoryActivity", "Error playing sound effect: " + e.getMessage());
            }
        }
    }
} 