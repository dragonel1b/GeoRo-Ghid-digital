package com.example.myapplication.Joc1.Culinary;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Import ModernCulinaryActivity class
import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity;

public class RecipeStepByStepActivity extends AppCompatActivity {
    private ModernCulinaryActivity.Recipe recipe;
    private List<RecipeStep> recipeSteps;
    private int currentStepIndex = 0;
    private boolean isTimerRunning = false;
    private CountDownTimer currentTimer;
    private long timerTimeRemaining = 0;
    private long overallTimerStartTime;
    private Handler overallTimerHandler;
    private Runnable overallTimerRunnable;
    
    // Voice recognition
    private SpeechRecognizer speechRecognizer;
    private boolean isVoiceCommandEnabled = false;
    private static final int SPEECH_REQUEST_CODE = 100;
    
    // UI components
    private TextView recipeTitle;
    private TextView recipeRegion;
    private TextView overallTimer;
    private ProgressBar recipeProgressBar;
    private TextView progressText;
    private RecyclerView interactiveStepsRecyclerView;
    private InteractiveStepAdapter stepAdapter;
    private TextView currentStepNumber;
    private TextView currentStepText;
    private Button previousStepButton;
    private Button nextStepButton;
    private Button markCompletedButton;
    private MaterialCardView currentStepCard;
    private MaterialCardView completionCard;
    private View timerContainer;
    private TextView timerTitle;
    private TextView timerText;
    private Button startPauseTimerButton;
    private Button resetTimerButton;
    private Button finishCookingButton;
    private Button shareResultButton;
    private SwitchCompat voiceCommandSwitch;
    private TextView voiceStatusText;
    private ImageButton micButton;
    private View videoTutorialContainer;
    private Button watchTutorialButton;
    
    // Timer sound
    private MediaPlayer timerAlarmSound;
    
    // Map to store video tutorials by technique name
    private Map<String, String> videoTutorials = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_step_by_step);
        
        // Initialize recipe data from intent
        if (getIntent().hasExtra("recipe_title") && getIntent().hasExtra("recipe_region")) {
            String title = getIntent().getStringExtra("recipe_title");
            String region = getIntent().getStringExtra("recipe_region");
            
            // Find the recipe details
            recipe = findRecipeByTitleAndRegion(title, region);
        }
        
        if (recipe == null) {
            Toast.makeText(this, "Nu s-au putut încărca detaliile rețetei", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize UI components
        initializeViews();
        setupToolbar();
        initializeVideoTutorials();
        
        // Setup recipe steps from the recipe
        processRecipeSteps();
        
        // Setup UI with recipe data
        populateRecipeDetails();
        setupInteractiveStepsList();
        updateCurrentStep();
        
        // Setup voice recognition
        setupVoiceRecognition();
        
        // Start the overall timer
        startOverallTimer();
    }
    
    private void initializeViews() {
        recipeTitle = findViewById(R.id.recipeTitle);
        recipeRegion = findViewById(R.id.recipeRegion);
        overallTimer = findViewById(R.id.overallTimer);
        recipeProgressBar = findViewById(R.id.recipeProgressBar);
        progressText = findViewById(R.id.progressText);
        interactiveStepsRecyclerView = findViewById(R.id.interactiveStepsRecyclerView);
        currentStepNumber = findViewById(R.id.currentStepNumber);
        currentStepText = findViewById(R.id.currentStepText);
        previousStepButton = findViewById(R.id.previousStepButton);
        nextStepButton = findViewById(R.id.nextStepButton);
        markCompletedButton = findViewById(R.id.markCompletedButton);
        currentStepCard = findViewById(R.id.currentStepCard);
        completionCard = findViewById(R.id.completionCard);
        timerContainer = findViewById(R.id.timerContainer);
        timerTitle = findViewById(R.id.timerTitle);
        timerText = findViewById(R.id.timerText);
        startPauseTimerButton = findViewById(R.id.startPauseTimerButton);
        resetTimerButton = findViewById(R.id.resetTimerButton);
        finishCookingButton = findViewById(R.id.finishCookingButton);
        shareResultButton = findViewById(R.id.shareResultButton);
        voiceCommandSwitch = findViewById(R.id.voiceCommandSwitch);
        voiceStatusText = findViewById(R.id.voiceStatusText);
        micButton = findViewById(R.id.micButton);
        videoTutorialContainer = findViewById(R.id.videoTutorialContainer);
        watchTutorialButton = findViewById(R.id.watchTutorialButton);
        
        setupButtonListeners();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Preparare pas cu pas");
        }
    }
    
    private void initializeVideoTutorials() {
        // Initialize video tutorials for common cooking techniques
        // These could come from a database in a real app
        videoTutorials.put("călire", "https://www.youtube.com/watch?v=example_saute");
        videoTutorials.put("fierbere", "https://www.youtube.com/watch?v=example_boil");
        videoTutorials.put("coacere", "https://www.youtube.com/watch?v=example_bake");
        videoTutorials.put("rumenire", "https://www.youtube.com/watch?v=example_browning");
        videoTutorials.put("prăjire", "https://www.youtube.com/watch?v=example_fry");
        videoTutorials.put("marinare", "https://www.youtube.com/watch?v=example_marinate");
        videoTutorials.put("tocare", "https://www.youtube.com/watch?v=example_chop");
        videoTutorials.put("amestec", "https://www.youtube.com/watch?v=example_mix");
        videoTutorials.put("frământare", "https://www.youtube.com/watch?v=example_knead");
        videoTutorials.put("dospire", "https://www.youtube.com/watch?v=example_rise");
    }
    
    private void processRecipeSteps() {
        recipeSteps = new ArrayList<>();
        
        if (recipe.getSteps() != null) {
            for (int i = 0; i < recipe.getSteps().length; i++) {
                String stepText = recipe.getSteps()[i];
                RecipeStep step = new RecipeStep(i + 1, stepText);
                
                // Check for cooking timer in the step
                extractCookingTimer(step);
                
                // Check for cooking techniques that have video tutorials
                checkForVideoTutorial(step);
                
                recipeSteps.add(step);
            }
        }
        
        // Set progress bar max value
        recipeProgressBar.setMax(recipeSteps.size());
        updateProgressText();
    }
    
    private void extractCookingTimer(RecipeStep step) {
        // Look for time specifications in the step description
        String stepText = step.getDescription().toLowerCase();
        
        // Pattern to match common time formats like "5 minute", "10 min", "2 ore"
        Pattern timePattern = Pattern.compile("(\\d+)\\s*(minut[e]*|min|or[eă]|h)");
        Matcher matcher = timePattern.matcher(stepText);
        
        if (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            // Convert to milliseconds
            long milliseconds;
            if (unit.startsWith("or") || unit.equals("h")) {
                milliseconds = amount * 60 * 60 * 1000; // hours to ms
                step.setTimerDuration(milliseconds);
                step.setTimerLabel(amount + " ore");
            } else {
                milliseconds = amount * 60 * 1000; // minutes to ms
                step.setTimerDuration(milliseconds);
                step.setTimerLabel(amount + " minute");
            }
        }
    }
    
    private void checkForVideoTutorial(RecipeStep step) {
        String stepText = step.getDescription().toLowerCase();
        
        for (Map.Entry<String, String> entry : videoTutorials.entrySet()) {
            String technique = entry.getKey();
            if (stepText.contains(technique)) {
                step.setHasVideoTutorial(true);
                step.setVideoTutorialUrl(entry.getValue());
                step.setVideoTutorialTechnique(technique);
                break;
            }
        }
    }
    
    private void populateRecipeDetails() {
        recipeTitle.setText(recipe.getTitle());
        recipeRegion.setText(recipe.getRegion());
    }
    
    private void setupInteractiveStepsList() {
        interactiveStepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepAdapter = new InteractiveStepAdapter(recipeSteps, this::onStepClicked);
        interactiveStepsRecyclerView.setAdapter(stepAdapter);
    }
    
    private void setupButtonListeners() {
        // Previous step button
        previousStepButton.setOnClickListener(v -> {
            goToPreviousStep();
        });
        
        // Next step button
        nextStepButton.setOnClickListener(v -> {
            goToNextStep();
        });
        
        // Mark as completed button
        markCompletedButton.setOnClickListener(v -> {
            completeCurrentStep();
        });
        
        // Timer buttons
        startPauseTimerButton.setOnClickListener(v -> {
            toggleTimer();
        });
        
        resetTimerButton.setOnClickListener(v -> {
            resetTimer();
        });
        
        // Finish cooking button
        finishCookingButton.setOnClickListener(v -> {
            finishCooking();
        });
        
        // Share result button
        shareResultButton.setOnClickListener(v -> {
            shareResult();
        });
        
        // Voice command switch
        voiceCommandSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleVoiceCommand(isChecked);
        });
        
        // Mic button for voice commands
        micButton.setOnClickListener(v -> {
            startVoiceRecognition();
        });
        
        // Watch tutorial button
        watchTutorialButton.setOnClickListener(v -> {
            openVideoTutorial();
        });
    }
    
    private void onStepClicked(int position) {
        currentStepIndex = position;
        updateCurrentStep();
        
        // Scroll to show the current step in the recycler view
        interactiveStepsRecyclerView.scrollToPosition(position);
    }
    
    private void updateCurrentStep() {
        if (currentStepIndex >= recipeSteps.size()) {
            // All steps are complete
            showCompletionCard();
            return;
        }
        
        RecipeStep step = recipeSteps.get(currentStepIndex);
        
        // Update step number and description
        currentStepNumber.setText("Pasul " + step.getStepNumber());
        currentStepText.setText(step.getDescription());
        
        // Update navigation buttons
        previousStepButton.setEnabled(currentStepIndex > 0);
        nextStepButton.setEnabled(currentStepIndex < recipeSteps.size() - 1);
        
        // Cancel any running timer when changing steps
        if (currentTimer != null) {
            currentTimer.cancel();
            isTimerRunning = false;
        }
        
        // Check if step has a timer
        if (step.hasTimer()) {
            timerContainer.setVisibility(View.VISIBLE);
            timerTitle.setText("Cronometru: " + step.getTimerLabel());
            timerTimeRemaining = step.getTimerDuration();
            updateTimerText(timerTimeRemaining);
            startPauseTimerButton.setText("Start");
        } else {
            timerContainer.setVisibility(View.GONE);
        }
        
        // Check if step has a video tutorial
        if (step.hasVideoTutorial()) {
            videoTutorialContainer.setVisibility(View.VISIBLE);
            watchTutorialButton.setText("Vizionează tutorial: " + capitalize(step.getVideoTutorialTechnique()));
        } else {
            videoTutorialContainer.setVisibility(View.GONE);
        }
        
        // Update completion status
        markCompletedButton.setText(step.isCompleted() ? "Resetează" : "Finalizat");
        
        // Highlight current step in the recycler view
        stepAdapter.setCurrentStep(currentStepIndex);
        stepAdapter.notifyDataSetChanged();
    }
    
    private void goToNextStep() {
        if (currentStepIndex < recipeSteps.size() - 1) {
            currentStepIndex++;
            updateCurrentStep();
        }
    }
    
    private void goToPreviousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            updateCurrentStep();
        }
    }
    
    private void completeCurrentStep() {
        if (currentStepIndex < recipeSteps.size()) {
            RecipeStep step = recipeSteps.get(currentStepIndex);
            
            // Toggle completion status
            step.setCompleted(!step.isCompleted());
            
            // Update UI
            markCompletedButton.setText(step.isCompleted() ? "Resetează" : "Finalizat");
            stepAdapter.notifyItemChanged(currentStepIndex);
            
            // Update progress
            updateProgressBar();
            
            // If completed, automatically move to next step after a short delay
            if (step.isCompleted() && currentStepIndex < recipeSteps.size() - 1) {
                new Handler().postDelayed(() -> {
                    goToNextStep();
                }, 500);
            }
        }
    }
    
    private void updateProgressBar() {
        int completedSteps = 0;
        for (RecipeStep step : recipeSteps) {
            if (step.isCompleted()) {
                completedSteps++;
            }
        }
        
        recipeProgressBar.setProgress(completedSteps);
        updateProgressText();
        
        // Check if all steps are completed
        if (completedSteps == recipeSteps.size()) {
            showCompletionCard();
        }
    }
    
    private void updateProgressText() {
        int completedSteps = 0;
        for (RecipeStep step : recipeSteps) {
            if (step.isCompleted()) {
                completedSteps++;
            }
        }
        
        progressText.setText(completedSteps + "/" + recipeSteps.size() + " pași completați");
    }
    
    private void showCompletionCard() {
        currentStepCard.setVisibility(View.GONE);
        completionCard.setVisibility(View.VISIBLE);
    }
    
    private void toggleTimer() {
        if (isTimerRunning) {
            // Pause timer
            if (currentTimer != null) {
                currentTimer.cancel();
            }
            isTimerRunning = false;
            startPauseTimerButton.setText("Start");
        } else {
            // Start timer
            startTimer(timerTimeRemaining);
            isTimerRunning = true;
            startPauseTimerButton.setText("Pauză");
        }
    }
    
    private void startTimer(long milliseconds) {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
        
        currentTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTimeRemaining = millisUntilFinished;
                updateTimerText(millisUntilFinished);
            }
            
            @Override
            public void onFinish() {
                timerTimeRemaining = 0;
                updateTimerText(0);
                isTimerRunning = false;
                startPauseTimerButton.setText("Start");
                
                // Play alarm sound
                playTimerAlarm();
                
                // Show timer completion notification
                Toast.makeText(RecipeStepByStepActivity.this, 
                        "Timpul a expirat pentru " + currentStepNumber.getText(), 
                        Toast.LENGTH_LONG).show();
            }
        }.start();
    }
    
    private void updateTimerText(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - 
                TimeUnit.MINUTES.toSeconds(minutes);
        
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }
    
    private void resetTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
        }
        
        RecipeStep step = recipeSteps.get(currentStepIndex);
        timerTimeRemaining = step.getTimerDuration();
        updateTimerText(timerTimeRemaining);
        isTimerRunning = false;
        startPauseTimerButton.setText("Start");
    }
    
    private void playTimerAlarm() {
        try {
            if (timerAlarmSound == null) {
                timerAlarmSound = MediaPlayer.create(this, R.raw.timer_alarm);
            }
            
            timerAlarmSound.setOnCompletionListener(mp -> {
                mp.release();
                timerAlarmSound = null;
            });
            
            timerAlarmSound.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startOverallTimer() {
        overallTimerStartTime = System.currentTimeMillis();
        overallTimerHandler = new Handler();
        overallTimerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - overallTimerStartTime;
                updateOverallTimerText(elapsedMillis);
                overallTimerHandler.postDelayed(this, 1000);
            }
        };
        
        overallTimerHandler.post(overallTimerRunnable);
    }
    
    private void updateOverallTimerText(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - 
                TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - 
                TimeUnit.MINUTES.toSeconds(minutes) - 
                TimeUnit.HOURS.toSeconds(hours);
        
        overallTimer.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
    }
    
    private void setupVoiceRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    voiceStatusText.setVisibility(View.VISIBLE);
                    voiceStatusText.setText("Ascult...");
                }
                
                @Override
                public void onBeginningOfSpeech() {}
                
                @Override
                public void onRmsChanged(float v) {}
                
                @Override
                public void onBufferReceived(byte[] bytes) {}
                
                @Override
                public void onEndOfSpeech() {
                    voiceStatusText.setVisibility(View.GONE);
                }
                
                @Override
                public void onError(int i) {
                    voiceStatusText.setVisibility(View.GONE);
                    Toast.makeText(RecipeStepByStepActivity.this, 
                            "Eroare la recunoașterea vocală. Încearcă din nou.", 
                            Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onResults(Bundle bundle) {
                    ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (results != null && !results.isEmpty()) {
                        String command = results.get(0).toLowerCase();
                        processVoiceCommand(command);
                    }
                    voiceStatusText.setVisibility(View.GONE);
                }
                
                @Override
                public void onPartialResults(Bundle bundle) {}
                
                @Override
                public void onEvent(int i, Bundle bundle) {}
            });
        } else {
            voiceCommandSwitch.setEnabled(false);
            Toast.makeText(this, "Recunoașterea vocală nu este disponibilă pe acest dispozitiv", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    private void toggleVoiceCommand(boolean enabled) {
        isVoiceCommandEnabled = enabled;
        
        if (enabled) {
            micButton.setVisibility(View.VISIBLE);
            voiceStatusText.setVisibility(View.VISIBLE);
            voiceStatusText.setText("Apasă pe microfon pentru a activa comanda vocală");
        } else {
            micButton.setVisibility(View.GONE);
            voiceStatusText.setVisibility(View.GONE);
        }
    }
    
    private void startVoiceRecognition() {
        if (!isVoiceCommandEnabled) return;
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Spune o comandă...");
        
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Recunoașterea vocală nu este disponibilă pe acest dispozitiv", 
                    Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String command = results.get(0).toLowerCase();
                processVoiceCommand(command);
            }
        }
    }
    
    private void processVoiceCommand(String command) {
        if (command.contains("următor") || command.contains("next")) {
            goToNextStep();
        } else if (command.contains("anterior") || command.contains("previous")) {
            goToPreviousStep();
        } else if (command.contains("finalizat") || command.contains("complete")) {
            completeCurrentStep();
        } else if (command.contains("start timer") || command.contains("pornește cronometru")) {
            if (!isTimerRunning && timerContainer.getVisibility() == View.VISIBLE) {
                toggleTimer();
            }
        } else if (command.contains("stop timer") || command.contains("oprește cronometru")) {
            if (isTimerRunning) {
                toggleTimer();
            }
        } else if (command.contains("reset timer") || command.contains("resetează cronometru")) {
            resetTimer();
        } else if (command.contains("arată video") || command.contains("tutorial")) {
            openVideoTutorial();
        } else {
            Toast.makeText(this, "Comandă nerecunoscută: " + command, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openVideoTutorial() {
        if (currentStepIndex < recipeSteps.size()) {
            RecipeStep step = recipeSteps.get(currentStepIndex);
            
            if (step.hasVideoTutorial() && step.getVideoTutorialUrl() != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(step.getVideoTutorialUrl()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Nu s-a putut deschide tutorialul video", 
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void finishCooking() {
        // Record cooking activity in user profile
        recordCookingActivity();
        
        // Finish activity
        setResult(RESULT_OK);
        finish();
    }
    
    private void recordCookingActivity() {
        // Save cooking activity to user profile or database
        SharedPreferences prefs = getSharedPreferences("recipe_history", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Update cook count for this recipe
        String recipeKey = recipe.getTitle() + "_" + recipe.getRegion() + "_count";
        int currentCount = prefs.getInt(recipeKey, 0);
        editor.putInt(recipeKey, currentCount + 1);
        
        // Save last cooked date
        String dateKey = recipe.getTitle() + "_" + recipe.getRegion() + "_lastCookedDate";
        editor.putLong(dateKey, System.currentTimeMillis());
        
        editor.apply();
    }
    
    private void shareResult() {
        String shareText = "Am gătit \"" + recipe.getTitle() + "\" din bucătăria regiunii " + 
                recipe.getRegion() + " folosind aplicația de Bucătărie Tradițională Românească!";
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Rețetă finalizată");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        startActivity(Intent.createChooser(shareIntent, "Distribuie prin"));
    }
    
    // Helper method to capitalize first letter
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button in toolbar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        // Show confirmation dialog if recipe is in progress
        int completedSteps = 0;
        for (RecipeStep step : recipeSteps) {
            if (step.isCompleted()) {
                completedSteps++;
            }
        }
        
        if (completedSteps > 0 && completedSteps < recipeSteps.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Părăsești prepararea")
                    .setMessage("Ești sigur că vrei să părăsești prepararea? Progresul tău va fi pierdut.")
                    .setPositiveButton("Da", (dialog, which) -> finish())
                    .setNegativeButton("Nu", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up resources
        if (currentTimer != null) {
            currentTimer.cancel();
        }
        
        if (overallTimerHandler != null && overallTimerRunnable != null) {
            overallTimerHandler.removeCallbacks(overallTimerRunnable);
        }
        
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        
        if (timerAlarmSound != null) {
            timerAlarmSound.release();
            timerAlarmSound = null;
        }
    }
    
    private ModernCulinaryActivity.Recipe findRecipeByTitleAndRegion(String title, String region) {
        // This should be replaced with database lookup in a real app
        List<ModernCulinaryActivity.Recipe> allRecipes = ModernCulinaryActivity.getRecipes();
        
        for (ModernCulinaryActivity.Recipe recipe : allRecipes) {
            if (recipe.getTitle().equals(title) && recipe.getRegion().equals(region)) {
                return recipe;
            }
        }
        
        return null;
    }

    /**
     * Model class for a recipe step with interactive features
     */
    public static class RecipeStep {
        private final int stepNumber;
        private final String description;
        private boolean completed;
        private long timerDuration; // in milliseconds
        private String timerLabel;
        private boolean hasVideoTutorial;
        private String videoTutorialUrl;
        private String videoTutorialTechnique;
        
        public RecipeStep(int stepNumber, String description) {
            this.stepNumber = stepNumber;
            this.description = description;
            this.completed = false;
            this.timerDuration = 0;
            this.hasVideoTutorial = false;
        }
        
        public int getStepNumber() {
            return stepNumber;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
        
        public boolean hasTimer() {
            return timerDuration > 0;
        }
        
        public long getTimerDuration() {
            return timerDuration;
        }
        
        public void setTimerDuration(long timerDuration) {
            this.timerDuration = timerDuration;
        }
        
        public String getTimerLabel() {
            return timerLabel;
        }
        
        public void setTimerLabel(String timerLabel) {
            this.timerLabel = timerLabel;
        }
        
        public boolean hasVideoTutorial() {
            return hasVideoTutorial;
        }
        
        public void setHasVideoTutorial(boolean hasVideoTutorial) {
            this.hasVideoTutorial = hasVideoTutorial;
        }
        
        public String getVideoTutorialUrl() {
            return videoTutorialUrl;
        }
        
        public void setVideoTutorialUrl(String videoTutorialUrl) {
            this.videoTutorialUrl = videoTutorialUrl;
        }
        
        public String getVideoTutorialTechnique() {
            return videoTutorialTechnique;
        }
        
        public void setVideoTutorialTechnique(String videoTutorialTechnique) {
            this.videoTutorialTechnique = videoTutorialTechnique;
        }
    }
    
    /**
     * Adapter for the interactive steps RecyclerView
     */
    class InteractiveStepAdapter extends RecyclerView.Adapter<InteractiveStepAdapter.StepViewHolder> {
        private final List<RecipeStep> steps;
        private final OnStepClickListener listener;
        private int currentStep = 0;
        
        interface OnStepClickListener {
            void onStepClick(int position);
        }
        
        public InteractiveStepAdapter(List<RecipeStep> steps, OnStepClickListener listener) {
            this.steps = steps;
            this.listener = listener;
        }
        
        public void setCurrentStep(int position) {
            this.currentStep = position;
        }
        
        @Override
        public StepViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_step_interactive, parent, false);
            return new StepViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(StepViewHolder holder, int position) {
            RecipeStep step = steps.get(position);
            
            // Set step number and description
            holder.stepNumberText.setText("Pasul " + step.getStepNumber());
            holder.stepDescriptionText.setText(step.getDescription());
            
            // Set checkbox state
            holder.stepCheckbox.setChecked(step.isCompleted());
            
            // Highlight current step
            if (position == currentStep) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(RecipeStepByStepActivity.this, 
                        android.R.color.holo_green_light));
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(RecipeStepByStepActivity.this, 
                        android.R.color.white));
            }
            
            // Show timer info if available
            if (step.hasTimer()) {
                holder.timerIcon.setVisibility(View.VISIBLE);
                holder.timerInfoText.setVisibility(View.VISIBLE);
                holder.timerInfoText.setText(step.getTimerLabel());
            } else {
                holder.timerIcon.setVisibility(View.GONE);
                holder.timerInfoText.setVisibility(View.GONE);
            }
            
            // Show video tutorial info if available
            if (step.hasVideoTutorial()) {
                holder.videoIcon.setVisibility(View.VISIBLE);
                holder.videoAvailableText.setVisibility(View.VISIBLE);
            } else {
                holder.videoIcon.setVisibility(View.GONE);
                holder.videoAvailableText.setVisibility(View.GONE);
            }
            
            // Set click listeners
            holder.stepCheckbox.setOnClickListener(v -> {
                step.setCompleted(holder.stepCheckbox.isChecked());
                updateProgressBar();
                notifyItemChanged(position);
                
                // Also update current step view if this is the current step
                if (position == currentStep) {
                    markCompletedButton.setText(step.isCompleted() ? "Resetează" : "Finalizat");
                }
            });
            
            holder.expandStepButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepClick(position);
                }
            });
            
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStepClick(position);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return steps.size();
        }
        
        class StepViewHolder extends RecyclerView.ViewHolder {
            CheckBox stepCheckbox;
            TextView stepNumberText;
            TextView stepDescriptionText;
            ImageButton expandStepButton;
            View timerIcon;
            TextView timerInfoText;
            View videoIcon;
            TextView videoAvailableText;
            
            StepViewHolder(View itemView) {
                super(itemView);
                stepCheckbox = itemView.findViewById(R.id.stepCheckbox);
                stepNumberText = itemView.findViewById(R.id.stepNumberText);
                stepDescriptionText = itemView.findViewById(R.id.stepDescriptionText);
                expandStepButton = itemView.findViewById(R.id.expandStepButton);
                timerIcon = itemView.findViewById(R.id.timerIcon);
                timerInfoText = itemView.findViewById(R.id.timerInfoText);
                videoIcon = itemView.findViewById(R.id.videoIcon);
                videoAvailableText = itemView.findViewById(R.id.videoAvailableText);
            }
        }
    }
} 