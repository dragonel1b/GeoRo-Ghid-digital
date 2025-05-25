package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.utils.ConfettiHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CookingGuideActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    
    private TextView recipeTitle;
    private TextView recipeRegion;
    private TextView recipeDescription;
    private TextView currentStepText;
    private TextView currentStepNumber;
    private ProgressBar cookingProgress;
    private Button nextStepButton;
    private Button prevStepButton;
    private CardView timerCard;
    private TextView timerText;
    private Button startTimerButton;
    private Button stopTimerButton;
    private RecyclerView ingredientsRecyclerView;
    private MaterialButton culturalNotesButton;
    private MaterialButton substitutionsButton;
    private MaterialButton cookingTipsButton;
    
    // Video demonstration views
    private CardView videoCard;
    private VideoView stepVideoView;
    private ImageButton replayVideoButton;
    
    // Voice guidance views
    private MaterialButton readStepButton;
    private android.widget.Switch autoReadSwitch;
    private TextToSpeech textToSpeech;
    private boolean autoReadEnabled = false;
    
    private String[] ingredients;
    private String[] steps;
    private String[] culturalNotes;
    private Map<String, String> ingredientSubstitutions;
    private String[] cookingTips;
    
    // Maps steps to video resources
    private Map<Integer, String> stepVideos;
    
    private int currentStep = 0;
    private boolean cookingCompleted = false;
    private CountDownTimer currentTimer = null;
    private long timerMillisRemaining = 0;
    private boolean timerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooking_guide);
        
        // Initialize views
        recipeTitle = findViewById(R.id.recipeTitleTextView);
        recipeRegion = findViewById(R.id.recipeCategoryTextView);
        recipeDescription = findViewById(R.id.recipeDescriptionTextView);
        currentStepText = findViewById(R.id.currentStepTextView);
        currentStepNumber = findViewById(R.id.currentStepNumberTextView);
        cookingProgress = findViewById(R.id.stepProgressBar);
        nextStepButton = findViewById(R.id.nextStepButton);
        prevStepButton = findViewById(R.id.prevStepButton);
        timerCard = findViewById(R.id.timerCard);
        timerText = findViewById(R.id.timerTextView);
        startTimerButton = findViewById(R.id.startTimerButton);
        stopTimerButton = findViewById(R.id.stopTimerButton);
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);
        culturalNotesButton = findViewById(R.id.culturalNotesButton);
        substitutionsButton = findViewById(R.id.substitutionsButton);
        cookingTipsButton = findViewById(R.id.tipsButton);
        
        // Initialize video demonstration views
        videoCard = findViewById(R.id.videoCard);
        stepVideoView = findViewById(R.id.recipeVideoView);
        replayVideoButton = findViewById(R.id.replayButton);
        
        // Initialize voice guidance views
        readStepButton = findViewById(R.id.readStepButton);
        autoReadSwitch = findViewById(R.id.autoReadSwitch);
        
        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ghid de gătit interactiv");
        
        // Get recipe data from intent
        getRecipeData();
        
        // Setup UI with recipe data
        setupUI();
        
        // Setup ingredient list
        setupIngredients();
        
        // Setup step navigation
        setupStepNavigation();
        
        // Setup timer
        setupTimer();
        
        // Setup extra buttons
        setupExtraButtons();
        
        // Setup video demonstration
        setupVideoDemo();
        
        // Setup voice guidance
        setupVoiceGuidance();
    }
    
    private void getRecipeData() {
        Intent intent = getIntent();
        if (intent != null) {
            recipeTitle.setText(intent.getStringExtra("RECIPE_TITLE"));
            recipeRegion.setText(intent.getStringExtra("RECIPE_REGION"));
            recipeDescription.setText(intent.getStringExtra("RECIPE_DESCRIPTION"));
            
            ingredients = intent.getStringArrayExtra("RECIPE_INGREDIENTS");
            steps = intent.getStringArrayExtra("RECIPE_STEPS");
            
            // Get cultural notes if available
            if (intent.hasExtra("RECIPE_CULTURAL_NOTES")) {
                culturalNotes = intent.getStringArrayExtra("RECIPE_CULTURAL_NOTES");
            } else {
                culturalNotes = new String[] {
                        "Această rețetă tradițională din " + intent.getStringExtra("RECIPE_REGION") + " are rădăcini adânci în cultura locală.",
                        "Se gătește de obicei în perioada sărbătorilor de iarnă sau la ocazii speciale.",
                        "Se servește tradițional cu pâine proaspătă și un pahar de vin local."
                };
            }
            
            // Setup default ingredient substitutions if not provided
            ingredientSubstitutions = new HashMap<>();
            if (ingredients != null) {
                for (String ingredient : ingredients) {
                    String mainIngredient = ingredient.split(" ")[0]; // Extract first word
                    
                    switch (mainIngredient.toLowerCase()) {
                        case "carne":
                            ingredientSubstitutions.put(ingredient, "Poți înlocui cu carne de vită sau de pui");
                            break;
                        case "lapte":
                            ingredientSubstitutions.put(ingredient, "Poți folosi lapte vegetal (de migdale sau soia)");
                            break;
                        case "smântână":
                            ingredientSubstitutions.put(ingredient, "Poți înlocui cu iaurt grecesc sau frișcă vegetală");
                            break;
                        case "unt":
                            ingredientSubstitutions.put(ingredient, "Poți folosi ulei de măsline sau margarină");
                            break;
                        case "făină":
                            ingredientSubstitutions.put(ingredient, "Poți folosi făină integrală sau făină fără gluten");
                            break;
                        default:
                            // No specific substitution
                            break;
                    }
                }
            }
            
            // Setup default cooking tips
            cookingTips = new String[] {
                    "Nu supraîncălzi uleiurile, acestea își pierd calitățile nutritive la temperaturi înalte.",
                    "Taie toate legumele de dimensiuni similare pentru a se găti uniform.",
                    "Gustă frecvent și ajustează condimentele treptat.",
                    "Lasă carnea să se odihnească 5-10 minute după gătire pentru a reține sucurile."
            };
            
            // Setup dummy step videos for demonstration
            setupStepVideos();
        }
    }
    
    private void setupUI() {
        // Set first step
        if (steps != null && steps.length > 0) {
            updateStepDisplay();
            
            // Setup progress bar
            cookingProgress.setMax(steps.length);
            cookingProgress.setProgress(currentStep + 1);
        }
    }
    
    private void setupIngredients() {
        if (ingredients != null && ingredients.length > 0 && ingredientsRecyclerView != null) {
            List<IngredientItem> ingredientItems = new ArrayList<>();
            for (String ingredient : ingredients) {
                ingredientItems.add(new IngredientItem(ingredient, false));
            }
            
            IngredientsAdapter adapter = new IngredientsAdapter(ingredientItems);
            ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            ingredientsRecyclerView.setAdapter(adapter);
        }
    }
    
    private void setupStepNavigation() {
        if (steps == null || steps.length == 0) {
            return;
        }
        
        // Disable prev button at first step
        prevStepButton.setEnabled(false);
        
        nextStepButton.setOnClickListener(v -> {
            if (currentStep < steps.length - 1) {
                currentStep++;
                updateStepDisplay();
                cookingProgress.setProgress(currentStep + 1);
                
                // Check if we're at the last step
                if (currentStep == steps.length - 1) {
                    nextStepButton.setText("Finalizează");
                }
                
                // Enable prev button after first step
                prevStepButton.setEnabled(true);
                
                // Reset timer for new step
                resetTimer();
                
                // Check if step contains timing information and suggest timer
                checkForTimingInfo(steps[currentStep]);
            } else {
                // Complete the cooking
                completeCooking();
            }
        });
        
        prevStepButton.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                updateStepDisplay();
                cookingProgress.setProgress(currentStep + 1);
                
                // Reset next button text
                nextStepButton.setText("Pasul următor");
                
                // Disable prev button at first step
                if (currentStep == 0) {
                    prevStepButton.setEnabled(false);
                }
                
                // Reset timer for new step
                resetTimer();
                
                // Check if step contains timing information
                checkForTimingInfo(steps[currentStep]);
            }
        });
    }
    
    private void updateStepDisplay() {
        if (steps != null && steps.length > 0) {
            currentStepText.setText(steps[currentStep]);
            currentStepNumber.setText(String.format(Locale.getDefault(), "Pasul %d din %d", currentStep + 1, steps.length));
            
            // Display video if available for this step
            updateVideoForCurrentStep();
            
            // Auto-read step if enabled
            if (autoReadEnabled) {
                readCurrentStep();
            }
        }
    }
    
    private void setupTimer() {
        startTimerButton.setOnClickListener(v -> {
            showTimerDialog();
        });
        
        stopTimerButton.setOnClickListener(v -> {
            if (currentTimer != null) {
                currentTimer.cancel();
                timerRunning = false;
                timerMillisRemaining = 0;
                stopTimerButton.setVisibility(View.GONE);
                startTimerButton.setEnabled(true);
                timerText.setText("00:00");
            }
        });
    }
    
    private void showTimerDialog() {
        final String[] times = {"1 minut", "5 minute", "10 minute", "15 minute", "30 minute", "Personalizat"};
        final long[] timeValues = {60000, 300000, 600000, 900000, 1800000, 0};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Setează un cronometru")
                .setItems(times, (dialog, which) -> {
                    long timeMs = timeValues[which];
                    
                    if (which == times.length - 1) {
                        // Custom time
                        showCustomTimerDialog();
                    } else {
                        startTimer(timeMs);
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    private void showCustomTimerDialog() {
        final View customView = getLayoutInflater().inflate(R.layout.dialog_custom_timer, null);
        final TextView minutesText = customView.findViewById(R.id.minutesText);
        final Button minusButton = customView.findViewById(R.id.minusButton);
        final Button plusButton = customView.findViewById(R.id.plusButton);
        
        // Initial value
        final int[] minutes = {10};
        minutesText.setText(String.valueOf(minutes[0]));
        
        // Setup buttons
        minusButton.setOnClickListener(v -> {
            if (minutes[0] > 1) {
                minutes[0]--;
                minutesText.setText(String.valueOf(minutes[0]));
            }
        });
        
        plusButton.setOnClickListener(v -> {
            if (minutes[0] < 120) {
                minutes[0]++;
                minutesText.setText(String.valueOf(minutes[0]));
            }
        });
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cronometru personalizat")
                .setView(customView)
                .setPositiveButton("Setează", (dialog, which) -> {
                    long timeMs = minutes[0] * 60000L;
                    startTimer(timeMs);
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    private void startTimer(long timeMs) {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
        
        timerCard.setVisibility(View.VISIBLE);
        stopTimerButton.setVisibility(View.VISIBLE);
        startTimerButton.setEnabled(false);
        timerMillisRemaining = timeMs;
        timerRunning = true;
        
        currentTimer = new CountDownTimer(timeMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerMillisRemaining = millisUntilFinished;
                updateTimerText(millisUntilFinished);
                
                // Change color when less than 30 seconds remain
                if (millisUntilFinished < 30000) {
                    timerText.setTextColor(Color.RED);
                }
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                timerRunning = false;
                timerMillisRemaining = 0;
                
                // Show notification
                Toast.makeText(CookingGuideActivity.this, "Timpul a expirat!", Toast.LENGTH_LONG).show();
                
                // Reset timer UI
                stopTimerButton.setVisibility(View.GONE);
                startTimerButton.setEnabled(true);
                timerText.setTextColor(Color.BLACK);
                
                // Vibrate and play sound
                vibrate();
            }
        }.start();
    }
    
    private void updateTimerText(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - 
                TimeUnit.MINUTES.toSeconds(minutes);
        
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }
    
    private void resetTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            timerRunning = false;
            timerMillisRemaining = 0;
            timerText.setText("00:00");
            timerText.setTextColor(Color.BLACK);
            stopTimerButton.setVisibility(View.GONE);
            startTimerButton.setEnabled(true);
        }
    }
    
    private void vibrate() {
        // Implement vibration if needed
    }
    
    private void checkForTimingInfo(String step) {
        String stepLower = step.toLowerCase();
        
        // Look for timing patterns like "X minutes", "X hours", etc.
        if (stepLower.contains("minut") || stepLower.contains("minute") || 
                stepLower.contains("ora") || stepLower.contains("ore")) {
            
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Sugestie de cronometrare")
                    .setMessage("Acest pas pare să necesite cronometrare. Vrei să setezi un cronometru?")
                    .setPositiveButton("Da", (dialog, which) -> showTimerDialog())
                    .setNegativeButton("Nu", null)
                    .show();
        }
    }
    
    private void setupExtraButtons() {
        // Cultural notes button
        culturalNotesButton.setOnClickListener(v -> {
            if (culturalNotes != null && culturalNotes.length > 0) {
                StringBuilder message = new StringBuilder();
                for (String note : culturalNotes) {
                    message.append("• ").append(note).append("\n\n");
                }
                
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Context cultural")
                        .setMessage(message.toString())
                        .setPositiveButton("Am înțeles", null)
                        .show();
            }
        });
        
        // Substitutions button
        substitutionsButton.setOnClickListener(v -> {
            if (ingredientSubstitutions != null && !ingredientSubstitutions.isEmpty()) {
                final String[] ingredientList = ingredientSubstitutions.keySet().toArray(new String[0]);
                
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Substituții de ingrediente")
                        .setItems(ingredientList, (dialog, which) -> {
                            String ingredient = ingredientList[which];
                            String substitution = ingredientSubstitutions.get(ingredient);
                            
                            new MaterialAlertDialogBuilder(CookingGuideActivity.this)
                                    .setTitle(ingredient)
                                    .setMessage(substitution)
                                    .setPositiveButton("Am înțeles", null)
                                    .show();
                        })
                        .setNegativeButton("Închide", null)
                        .show();
            } else {
                Toast.makeText(this, "Nu există substituții pentru această rețetă", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Cooking tips button
        cookingTipsButton.setOnClickListener(v -> {
            if (cookingTips != null && cookingTips.length > 0) {
                StringBuilder message = new StringBuilder();
                for (String tip : cookingTips) {
                    message.append("• ").append(tip).append("\n\n");
                }
                
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Sfaturi de gătit")
                        .setMessage(message.toString())
                        .setPositiveButton("Am înțeles", null)
                        .show();
            }
        });
    }
    
    private void setupStepVideos() {
        // This would normally load from a database or resources
        // For this demo, we'll map step indices to sample video URIs
        stepVideos = new HashMap<>();
        
        // Add some demo video mappings using sample videos from raw resources
        // These would typically be recipe-specific videos
        stepVideos.put(0, "android.resource://" + getPackageName() + "/" + R.raw.sample_cooking_1);
        stepVideos.put(2, "android.resource://" + getPackageName() + "/" + R.raw.sample_cooking_2);
        stepVideos.put(4, "android.resource://" + getPackageName() + "/" + R.raw.sample_cooking_3);
    }
    
    private void updateVideoForCurrentStep() {
        if (stepVideos != null && stepVideos.containsKey(currentStep)) {
            String videoUri = stepVideos.get(currentStep);
            if (videoUri != null && !videoUri.isEmpty()) {
                videoCard.setVisibility(View.VISIBLE);
                playVideo(videoUri);
            } else {
                videoCard.setVisibility(View.GONE);
            }
        } else {
            videoCard.setVisibility(View.GONE);
        }
    }
    
    private void setupVideoDemo() {
        replayVideoButton.setOnClickListener(v -> {
            if (stepVideos != null && stepVideos.containsKey(currentStep)) {
                String videoUri = stepVideos.get(currentStep);
                if (videoUri != null && !videoUri.isEmpty()) {
                    playVideo(videoUri);
                }
            }
        });
        
        // Handle video completion
        stepVideoView.setOnCompletionListener(mp -> {
            // Show replay button more prominently if needed
        });
    }
    
    private void playVideo(String videoUri) {
        try {
            // Reset the media player
            stepVideoView.stopPlayback();
            stepVideoView.setVideoURI(Uri.parse(videoUri));
            
            // Add loading indicator if needed
            stepVideoView.setOnPreparedListener(mp -> {
                // Adjust media player settings if needed
                mp.setLooping(false);
                stepVideoView.start();
            });
            
            stepVideoView.setOnErrorListener((mp, what, extra) -> {
                // Handle video playback errors
                Toast.makeText(CookingGuideActivity.this, 
                        "Eroare la redarea demonstrației video", Toast.LENGTH_SHORT).show();
                return true;
            });
            
        } catch (Exception e) {
            Log.e("CookingGuide", "Error playing video: " + e.getMessage());
            videoCard.setVisibility(View.GONE);
        }
    }
    
    private void setupVoiceGuidance() {
        readStepButton.setOnClickListener(v -> readCurrentStep());
        
        autoReadSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoReadEnabled = isChecked;
            
            if (autoReadEnabled) {
                readCurrentStep();
            } else if (textToSpeech != null && textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        });
    }
    
    private void readCurrentStep() {
        if (textToSpeech != null && !textToSpeech.isSpeaking() && steps != null && currentStep < steps.length) {
            // For Romanian localization, announce the step number first, then the content
            String textToRead = String.format("Pasul %d. %s", currentStep + 1, steps[currentStep]);
            textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "step_guidance");
        }
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set Romanian as preferred language
            int result = textToSpeech.setLanguage(new Locale("ro", "RO"));
            
            // Fall back to default language if Romanian not available
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(Locale.getDefault());
                Log.e("TextToSpeech", "Romanian language not supported, using default");
            }
            
            // Adjust speech rate slightly slower for cooking instructions
            textToSpeech.setSpeechRate(0.9f);
        } else {
            Log.e("TextToSpeech", "Initialization failed");
        }
    }
    
    @Override
    protected void onDestroy() {
        // Shut down TextToSpeech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        
        // Release MediaPlayer resources
        if (stepVideoView != null) {
            stepVideoView.stopPlayback();
        }
        
        super.onDestroy();
    }
    
    private void completeCooking() {
        if (cookingCompleted) {
            return;
        }
        
        cookingCompleted = true;
        
        // Stop any active TTS
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        
        // Show congratulations dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle("Felicitări!")
                .setMessage("Ai finalizat cu succes rețeta: " + recipeTitle.getText())
                .setPositiveButton("Finalizează", (dialog, which) -> {
                    // Show confetti
                    View rootView = findViewById(android.R.id.content);
                    if (rootView instanceof ViewGroup) {
                        ConfettiHelper.showCenterExplosion(this, (ViewGroup) rootView);
                    }
                    
                    // Return result
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("RECIPE_TITLE", recipeTitle.getText().toString());
                    resultIntent.putExtra("COOKING_COMPLETED", true);
                    setResult(RESULT_OK, resultIntent);
                    
                    // Delay finish to show confetti
                    rootView.postDelayed(this::finish, 2000);
                })
                .setCancelable(false)
                .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (cookingCompleted) {
            super.onBackPressed();
            return;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Părăsești rețeta?")
                .setMessage("Ești sigur că vrei să părăsești această rețetă? Progresul nu va fi salvat.")
                .setPositiveButton("Da", (dialog, which) -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("COOKING_COMPLETED", false);
                    setResult(RESULT_CANCELED, resultIntent);
                    finish();
                })
                .setNegativeButton("Nu", null)
                .show();
    }
    
    // Inner class for ingredient items
    static class IngredientItem {
        private final String name;
        private boolean checked;
        
        IngredientItem(String name, boolean checked) {
            this.name = name;
            this.checked = checked;
        }
        
        public String getName() {
            return name;
        }
        
        public boolean isChecked() {
            return checked;
        }
        
        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }
    
    // Adapter for ingredients
    class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {
        private final List<IngredientItem> ingredients;
        
        IngredientsAdapter(List<IngredientItem> ingredients) {
            this.ingredients = ingredients;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_ingredient_check, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            IngredientItem item = ingredients.get(position);
            holder.ingredientText.setText(item.getName());
            holder.checkImage.setVisibility(item.isChecked() ? View.VISIBLE : View.GONE);
            
            // Set click listener to toggle checked state
            holder.itemView.setOnClickListener(v -> {
                item.setChecked(!item.isChecked());
                holder.checkImage.setVisibility(item.isChecked() ? View.VISIBLE : View.GONE);
            });
        }
        
        @Override
        public int getItemCount() {
            return ingredients.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView ingredientText;
            ImageView checkImage;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ingredientText = itemView.findViewById(R.id.ingredientText);
                checkImage = itemView.findViewById(R.id.checkImage);
            }
        }
    }
} 