package com.example.myapplication.bucovinausage;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.Bucovina;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.core.domain.model.BucovinaStoryNode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.core.widget.NestedScrollView;

public class BucovinaStoryActivity extends AppCompatActivity {
    private static final String TAG = "BucovinaStoryActivity";
    
    private TextView storyText;
    private TextView storyTitle;
    private TextView storyContext;
    private ImageView storyImageView;
    private MaterialButton choiceButton1;
    private MaterialButton choiceButton2;
    private MaterialButton choiceButton3;
    private MaterialButton nextButton;
    private MaterialButton storyButton;
    private MaterialButton exitButton;
    private ImageView headerBackButton;
    private ImageView soundToggleButton;
    private MaterialCardView feedbackCard;
    private TextView feedbackText;
    private ProgressBar progressIndicator;
    private ImageView animationView;
    private PointsManager pointsManager;
    private int currentSceneIndex = 0;
    private Map<Integer, BucovinaStoryNode> storyNodes;
    private boolean isSoundEnabled = true;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private int currentPoints = 0;
    private int totalStoryNodes = 0;
    private MediaPlayer backgroundMusic;
    private MediaPlayer soundEffect;
    private Animation fadeInAnimation;
    private Handler handler = new Handler();
    private LinearLayout buttonContainer;
    private LinearLayout choiceContainer;
    private NestedScrollView scrollView;
    private MaterialCardView storyCardView;
    private MaterialCardView headerCardView;
    private Random random = new Random();
    private boolean isStoryMode = false;
    private int artifactPieces = 0;
    private boolean hasFoundTreasure = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucovina_story);
        
        initializeViews();
        initializeAudio();
        initializeAnimations();
        initializeStory();
        initializeTextToSpeech();
        setupButtonListeners();
        applyBucovinaTheme();
        showScene();
    }
    
    private void initializeViews() {
        storyText = findViewById(R.id.storyText);
        storyTitle = findViewById(R.id.storyTitle);
        storyContext = findViewById(R.id.storyContext);
        storyImageView = findViewById(R.id.storyImage);
        choiceButton1 = findViewById(R.id.choiceButton1);
        choiceButton2 = findViewById(R.id.choiceButton2);
        choiceButton3 = findViewById(R.id.choiceButton3);
        nextButton = findViewById(R.id.nextButton);
        storyButton = findViewById(R.id.storyButton);
        exitButton = findViewById(R.id.exitButton);
        headerBackButton = findViewById(R.id.headerBackButton);
        soundToggleButton = findViewById(R.id.soundToggleButton);
        feedbackCard = findViewById(R.id.feedbackCard);
        feedbackText = findViewById(R.id.feedbackText);
        progressIndicator = findViewById(R.id.progressIndicator);
        animationView = findViewById(R.id.animationView);
        buttonContainer = findViewById(R.id.buttonContainer);
        choiceContainer = findViewById(R.id.choiceButtonsContainer);
        scrollView = findViewById(R.id.story_scroll_view);
        storyCardView = findViewById(R.id.storyCardView);
        headerCardView = findViewById(R.id.headerCard);
        
        pointsManager = PointsManager.getInstance(this);
        
        // Get current points
        currentPoints = pointsManager.getPoints(this);
        
        // Make sure all elements have proper initial visibility
        if (storyText != null) storyText.setVisibility(View.VISIBLE);
        if (storyTitle != null) storyTitle.setVisibility(View.VISIBLE);
        if (storyContext != null) storyContext.setVisibility(View.VISIBLE);
        if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        if (exitButton != null) exitButton.setVisibility(View.VISIBLE);
        if (storyButton != null) storyButton.setVisibility(View.VISIBLE);
        if (feedbackCard != null) feedbackCard.setVisibility(View.GONE);
        if (animationView != null) animationView.setVisibility(View.GONE);
        if (choiceContainer != null) choiceContainer.setVisibility(View.GONE);
        
        // Add typeface to text views
        if (storyText != null) {
            storyText.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        }
        if (storyTitle != null) {
            storyTitle.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD));
        }
        if (storyContext != null) {
            storyContext.setTypeface(android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.ITALIC));
        }
    }
    
    private void applyBucovinaTheme() {
        // Apply Bucovina theme colors
        int primaryColor = ContextCompat.getColor(this, R.color.bucovina_primary);
        int primaryLightColor = ContextCompat.getColor(this, R.color.bucovina_primary_light);
        int accentColor = ContextCompat.getColor(this, R.color.bucovina_accent);
        int textColor = ContextCompat.getColor(this, R.color.bucovina_text);
        int backgroundColor = ContextCompat.getColor(this, R.color.bucovina_background);
        int cardBgColor = ContextCompat.getColor(this, R.color.bucovina_card_bg);
        
        // Apply colors to views
        getWindow().setStatusBarColor(primaryColor);
        
        // Apply to cards
        if (storyCardView != null) {
            storyCardView.setCardBackgroundColor(cardBgColor);
            storyCardView.setStrokeColor(primaryLightColor);
        }
        
        if (headerCardView != null) {
            headerCardView.setCardBackgroundColor(primaryColor);
        }
        
        // Apply to text
        if (storyTitle != null) {
            storyTitle.setTextColor(primaryColor);
        }
        if (storyContext != null) {
            storyContext.setTextColor(primaryLightColor);
        }
        if (storyText != null) {
            storyText.setTextColor(textColor);
        }
        
        // Apply to buttons
        if (nextButton != null) {
            nextButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
            nextButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
            nextButton.setStrokeColor(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
        }
        
        if (storyButton != null) {
            storyButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_secondary));
            storyButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
            storyButton.setStrokeColor(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
        }
        
        if (exitButton != null) {
            exitButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary_light));
            exitButton.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
            exitButton.setStrokeColor(ContextCompat.getColorStateList(this, R.color.bucovina_secondary));
        }
        
        // Apply to choice buttons
        if (choiceButton1 != null) {
            choiceButton1.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
            choiceButton1.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
            choiceButton1.setRippleColorResource(R.color.bucovina_primary_light);
        }
        
        if (choiceButton2 != null) {
            choiceButton2.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
            choiceButton2.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
            choiceButton2.setRippleColorResource(R.color.bucovina_primary_light);
        }
        
        if (choiceButton3 != null) {
            choiceButton3.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
            choiceButton3.setTextColor(ContextCompat.getColorStateList(this, R.color.white));
            choiceButton3.setRippleColorResource(R.color.bucovina_primary_light);
        }
        
        // Apply to progress bar
        if (progressIndicator != null) {
            progressIndicator.setProgressTintList(ContextCompat.getColorStateList(this, R.color.bucovina_primary));
        }
        
        // Apply color to feedback card
        if (feedbackCard != null) {
            feedbackCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bucovina_primary_light));
        }
        if (feedbackText != null) {
            feedbackText.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        
        // Adjust icons
        if (headerBackButton != null) {
            headerBackButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        }
        if (soundToggleButton != null) {
            soundToggleButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
        }
    }
    
    private void initializeAudio() {
        try {
            // Initialize background music with traditional Bucovina music
            backgroundMusic = MediaPlayer.create(this, R.raw.dark_ambient); // Replace with Bucovina music
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.2f, 0.2f);
                backgroundMusic.start();
            }
            
            // Initialize sound effect player
            soundEffect = MediaPlayer.create(this, R.raw.thunder); // Replace with appropriate sound effect
        } catch (Exception e) {
            // Handle possible errors safely
            Toast.makeText(this, "Eroare la inițializarea audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeAnimations() {
        try {
            fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            if (fadeInAnimation != null) {
                fadeInAnimation.setDuration(1000);
            }
        } catch (Exception e) {
            // Handle possible errors safely
            Toast.makeText(this, "Eroare la inițializarea animațiilor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initializeTextToSpeech() {
        try {
            textToSpeech = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(new Locale("ro", "RO"));
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(this, "Limba română nu este suportată pentru citire", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Eroare la inițializarea text-to-speech", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            // Handle possible errors safely
            Toast.makeText(this, "Eroare la inițializarea TTS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            textToSpeech = null;
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
        
        // Choice buttons
        if (choiceButton1 != null) {
            choiceButton1.setOnClickListener(v -> {
                BucovinaStoryNode currentNode = storyNodes.get(currentSceneIndex);
                if (currentNode != null && currentNode.getNextNodes() != null && currentNode.getNextNodes().length > 0) {
                    currentSceneIndex = currentNode.getNextNodes()[0];
                    animateSceneTransition();
                }
            });
        }
        
        if (choiceButton2 != null) {
            choiceButton2.setOnClickListener(v -> {
                BucovinaStoryNode currentNode = storyNodes.get(currentSceneIndex);
                if (currentNode != null && currentNode.getNextNodes() != null && currentNode.getNextNodes().length > 1) {
                    currentSceneIndex = currentNode.getNextNodes()[1];
                    animateSceneTransition();
                }
            });
        }
        
        if (choiceButton3 != null) {
            choiceButton3.setOnClickListener(v -> {
                BucovinaStoryNode currentNode = storyNodes.get(currentSceneIndex);
                if (currentNode != null && currentNode.getNextNodes() != null && currentNode.getNextNodes().length > 2) {
                    currentSceneIndex = currentNode.getNextNodes()[2];
                    animateSceneTransition();
                }
            });
        }
        
        if (headerBackButton != null) {
            headerBackButton.setOnClickListener(v -> finish());
        }
        
        if (soundToggleButton != null) {
            soundToggleButton.setOnClickListener(v -> toggleSound());
        }
    }
    
    private void toggleSound() {
        isSoundEnabled = !isSoundEnabled;
        
        if (soundToggleButton != null) {
            soundToggleButton.setImageResource(isSoundEnabled ? 
                android.R.drawable.ic_lock_silent_mode_off : 
                android.R.drawable.ic_lock_silent_mode);
        }
        
        if (isSoundEnabled) {
            if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                backgroundMusic.start();
            }
        } else {
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                textToSpeech.stop();
                isSpeaking = false;
            }
        }
    }
    
    private void toggleStoryReading() {
        if (textToSpeech == null || storyText == null) return;
        
        if (isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
            if (storyButton != null) {
                storyButton.setText("Citește");
            }
        } else {
            if (isSoundEnabled) {
                String text = storyText.getText().toString();
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                isSpeaking = true;
                if (storyButton != null) {
                    storyButton.setText("Oprește");
                }
            } else {
                Toast.makeText(this, "Sunetul este dezactivat", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void animateSceneTransition() {
        if (scrollView != null) {
            scrollView.setAlpha(0f);
            scrollView.animate().alpha(1f).setDuration(500).start();
        }
        
        // Stop speaking if changing scene
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
            isSpeaking = false;
            if (storyButton != null) {
                storyButton.setText("Citește");
            }
        }
        
        showScene();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
        if (soundEffect != null) {
            soundEffect.release();
            soundEffect = null;
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
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
    
    private void initializeStory() {
        try {
            storyNodes = new HashMap<>();
            
            // Create a simple initial story structure - can be expanded later
            storyNodes.put(0, new BucovinaStoryNode.Builder(0, "Bine ai venit în povestea interactivă a Bucovinei. Legendele din Țara Fagilor te așteaptă să le descoperi. Călătoria ta începe acum...")
                    .title("Legendele Bucovinei")
                    .context("O poveste veche din ținutul fagilor...")
                    .imageResource(R.drawable.story)
                    .nextNodes(new int[]{1})
                    .build());
            
            storyNodes.put(1, new BucovinaStoryNode.Builder(1, "Te afli la marginea unei păduri seculare de fagi. În fața ta se întind două drumuri. Unul duce către mănăstirea Voroneț, iar celălalt către muntele Rarău. Încotro vrei să mergi?")
                    .title("La răscruce de drumuri")
                    .context("Alege primul pas al călătoriei...")
                    .imageResource(R.drawable.drum_ras)
                    .choices(new String[]{"Spre mănăstirea Voroneț", "Către muntele Rarău"})
                    .nextNodes(new int[]{2, 3})
                    .build());
            
            // Voronet path
            storyNodes.put(2, new BucovinaStoryNode.Builder(2, "Ajungi la mănăstirea Voroneț, bijuteria Bucovinei. Picturile exterioare în faimosul \"albastru de Voroneț\" te lasă fără cuvinte. Un călugăr bătrân te invită să îl urmezi pentru a-ți arăta o taină a mănăstirii.")
                    .title("Albastrul de Voroneț")
                    .context("Culoarea care a impresionat lumea întreagă...")
                    .imageResource(R.drawable.manastire_voronet)
                    .choices(new String[]{"Urmezi călugărul", "Rămâi să admiri picturile"})
                    .nextNodes(new int[]{4, 5})
                    .build());
            
            // Rariu path
            storyNodes.put(3, new BucovinaStoryNode.Builder(3, "Urci pe poteca ce duce spre vârful Rarău. Peisajele sunt spectaculoase, iar aerul proaspăt de munte îți dă energie. În depărtare, formațiunile stâncoase Pietrele Doamnei par să ascundă o poveste veche.")
                    .title("Spre vârful Rarău")
                    .context("Muntele semeț al Bucovinei...")
                    .imageResource(R.drawable.varf_munte)
                    .choices(new String[]{"Explorezi Pietrele Doamnei", "Continui drumul spre vârf"})
                    .nextNodes(new int[]{6, 7})
                    .build());
            
            // End node with reward
            storyNodes.put(7, new BucovinaStoryNode.Builder(7, "Ai ajuns pe vârful Rarău! Panorama asupra Bucovinei este incredibilă. Poți vedea pădurile nesfârșite de fagi, satele tradiționale și mănăstirile pictate în depărtare. Te simți împlinit și mai bogat sufletește după această experiență.")
                    .title("Pe vârful muntelui")
                    .context("O priveliște de neuitat...")
                    .imageResource(R.drawable.varf_munte)
                    .isEndNode(true)
                    .rewardPoints(100)
                    .build());
            
            // Add more nodes to create a complete story...
            
            // Set the total number of story nodes
            totalStoryNodes = storyNodes.size();
            
        } catch (Exception e) {
            // Handle errors safely
            Toast.makeText(this, "Eroare la inițializarea poveștii: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Create at least one basic node to avoid null errors
            if (storyNodes == null) {
                storyNodes = new HashMap<>();
                storyNodes.put(0, new BucovinaStoryNode.Builder(0, "A apărut o eroare. Te rugăm să încerci mai târziu.")
                        .title("Eroare")
                        .build());
                totalStoryNodes = 1;
            }
        }
    }
    
    private void showScene() {
        BucovinaStoryNode currentNode = storyNodes.get(currentSceneIndex);
        if (currentNode == null) {
            return;
        }
        
        // Update text and image
        if (storyTitle != null) {
            storyTitle.setText(currentNode.getTitle());
        }
        if (storyContext != null) {
            storyContext.setText(currentNode.getContext());
        }
        if (storyText != null) {
            storyText.setText(currentNode.getText());
        }
        
        // Set image resource based on node type
        if (storyImageView != null) {
            int imageResource = getImageForNode(currentNode);
            if (imageResource != 0) {
                storyImageView.setImageResource(imageResource);
                storyImageView.setVisibility(View.VISIBLE);
            } else {
                storyImageView.setVisibility(View.GONE);
            }
        }
        
        // Show or hide buttons based on node type
        if (currentNode.hasChoices()) {
            setupChoiceButtons(currentNode);
            if (choiceContainer != null) choiceContainer.setVisibility(View.VISIBLE);
            if (buttonContainer != null) buttonContainer.setVisibility(View.GONE);
        } else {
            if (choiceContainer != null) choiceContainer.setVisibility(View.GONE);
            if (buttonContainer != null) buttonContainer.setVisibility(View.VISIBLE);
        }
        
        // Check if this is the end node
        if (currentNode.isEndNode()) {
            if (nextButton != null) nextButton.setVisibility(View.GONE);
            
            // Add rewards
            int rewardPoints = currentNode.getRewardPoints();
            if (rewardPoints > 0) {
                pointsManager.addPoints(this, "bucovina", rewardPoints);
                showFeedback("Ai câștigat " + rewardPoints + " puncte!");
            }
        } else {
            if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        }
        
        // Update progress
        updateProgressIndicator();
        
        // Auto-scroll to top
        if (scrollView != null) {
            scrollView.smoothScrollTo(0, 0);
        }
    }
    
    private void updateProgressIndicator() {
        if (progressIndicator != null && totalStoryNodes > 0) {
            int progress = (currentSceneIndex * 100) / totalStoryNodes;
            progressIndicator.setProgress(progress);
        }
    }
    
    private void setupChoiceButtons(BucovinaStoryNode node) {
        if (node == null || node.getChoices() == null) return;
        
        String[] choices = node.getChoices();
        
        // Hide all buttons first
        if (choiceButton1 != null) choiceButton1.setVisibility(View.GONE);
        if (choiceButton2 != null) choiceButton2.setVisibility(View.GONE);
        if (choiceButton3 != null) choiceButton3.setVisibility(View.GONE);
        
        // Show and set text for available choices
        if (choices.length > 0 && choiceButton1 != null) {
            choiceButton1.setText(choices[0]);
            choiceButton1.setVisibility(View.VISIBLE);
        }
        
        if (choices.length > 1 && choiceButton2 != null) {
            choiceButton2.setText(choices[1]);
            choiceButton2.setVisibility(View.VISIBLE);
        }
        
        if (choices.length > 2 && choiceButton3 != null) {
            choiceButton3.setText(choices[2]);
            choiceButton3.setVisibility(View.VISIBLE);
        }
    }
    
    private void showFeedback(String message) {
        if (feedbackText != null) {
            feedbackText.setText(message);
        }
        if (feedbackCard != null) {
            feedbackCard.setVisibility(View.VISIBLE);
        }
    }
    
    public void goBack(View view) {
        finish();
    }
    
    private int getImageForNode(BucovinaStoryNode node) {
        if (node == null) return 0;
        
        try {
            // Get appropriate image based on node type or content
            int imageResource = node.getImageResource();
            if (imageResource != 0) {
                return imageResource;
            }
            
            // Default to generic images based on node type if no specific image
            if (node.isEndNode()) {
                return R.drawable.app_logo; // Replace with appropriate image
            } else if (node.hasChoices()) {
                return R.drawable.app_logo; // Replace with appropriate image
            }
            
            // Default image
            return R.drawable.app_logo;
        } catch (Exception e) {
            // Safe default on error
            return R.drawable.app_logo;
        }
    }
} 