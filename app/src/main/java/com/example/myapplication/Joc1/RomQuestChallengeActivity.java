package com.example.myapplication.Joc1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

public class RomQuestChallengeActivity extends AppCompatActivity {
    private TextView missionTitle;
    private TextView missionDescription;
    private LinearLayout objectivesContainer;
    private TextView challengePrompt;
    private TextInputLayout answerInputLayout;
    private MaterialButton submitButton;
    private MaterialButton cancelButton;
    private MaterialCardView challengeCard;
    private ImageView typeIcon;
    private TextView hintText;

    private String[] objectives;
    private String correctAnswer;
    private int missionId;
    private int attemptCount = 0;
    private String missionType;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_quest_challenge);

        // Get mission details from intent
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("mission_title")) {
            Toast.makeText(this, "Eroare la încărcarea misiunii", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = intent.getStringExtra("mission_title");
        String description = intent.getStringExtra("mission_description");
        objectives = intent.getStringArrayExtra("mission_objectives");
        missionId = intent.getIntExtra("mission_id", -1);
        correctAnswer = intent.getStringExtra("correct_answer");
        missionType = intent.getStringExtra("mission_type");

        initializeViews();
        setupToolbar(title);
        populateMissionDetails(title, description);
        setupChallenge();
        setupButtons();
    }

    private void initializeViews() {
        missionTitle = findViewById(R.id.missionTitle);
        missionDescription = findViewById(R.id.missionDescription);
        objectivesContainer = findViewById(R.id.objectivesContainer);
        challengePrompt = findViewById(R.id.challengePrompt);
        answerInputLayout = findViewById(R.id.answerInputLayout);
        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);
        challengeCard = findViewById(R.id.challengeCard);
        typeIcon = findViewById(R.id.typeIcon);
        hintText = findViewById(R.id.hintText);
        
        // Set input animation
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        answerInputLayout.startAnimation(pulse);
        
        // Add text watcher to reset error when typing
        answerInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                answerInputLayout.setError(null);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    private void populateMissionDetails(String title, String description) {
        missionTitle.setText(title);
        missionDescription.setText(description);

        // Set type-specific UI elements
        if (missionType != null) {
            setupTypeSpecificUI(missionType);
        }

        // Add objectives
        if (objectives != null) {
            for (String objective : objectives) {
                TextView objectiveView = new TextView(this);
                objectiveView.setText("• " + objective);
                objectiveView.setTextSize(14);
                objectiveView.setPadding(0, 8, 0, 8);
                objectivesContainer.addView(objectiveView);
            }
        }
    }
    
    private void setupTypeSpecificUI(String type) {
        int iconRes = R.drawable.ic_explore;
        int colorRes = R.color.rom_primary;
        String promptText = "Răspunde la întrebare pentru a completa misiunea:";
        
        switch (type) {
            case "cultura":
                iconRes = R.drawable.ic_explore;
                colorRes = R.color.purple_500;
                promptText = "Ce regiune istorică a României este cunoscută pentru cultura sa diversă?";
                break;
            case "istorie":
                iconRes = R.drawable.ic_explore;
                colorRes = R.color.rom_primary;
                promptText = "În ce an s-a înfăptuit Marea Unire?";
                break;
            case "culinara":
                iconRes = R.drawable.ic_explore;
                colorRes = R.color.rom_accent;
                promptText = "Care este mâncarea tradițională românească făcută din carne și varză?";
                break;
            case "explorare":
                iconRes = R.drawable.ic_explore;
                colorRes = R.color.rom_primary;
                promptText = "Ce fluviu formează granița de sud a României?";
                break;
            case "provocare":
                iconRes = R.drawable.ic_challenge;
                colorRes = R.color.rom_accent;
                promptText = "Ce domnitor român a construit mănăstirea Hurezi (acum în patrimoniul UNESCO)?";
                break;
        }
        
        typeIcon.setImageResource(iconRes);
        challengeCard.setCardBackgroundColor(ContextCompat.getColor(this, colorRes));
        challengePrompt.setText(promptText);
        answerInputLayout.setHint("Răspunsul tău");
        
        // Set hint based on mission type
        String hintString = "Indiciu: Numele conține " + correctAnswer.length() + " litere";
        hintText.setText(hintString);
    }

    private void setupChallenge() {
        // Setup challenge specific UI based on mission type
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        challengeCard.startAnimation(slideIn);
    }

    private void setupButtons() {
        submitButton.setOnClickListener(v -> validateAnswer());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void validateAnswer() {
        String answer = answerInputLayout.getEditText().getText().toString().trim().toLowerCase();

        if (answer.isEmpty()) {
            answerInputLayout.setError("Te rugăm să introduci un răspuns");
            shakeView(answerInputLayout);
            return;
        }
        
        attemptCount++;

        if (answer.contains(correctAnswer) || correctAnswer.contains(answer)) {
            // Success!
            Intent resultIntent = new Intent();
            resultIntent.putExtra("mission_id", missionId);
            setResult(RESULT_OK, resultIntent);

            // Show success animation
            showSuccessAnimation();
        } else {
            // Wrong answer
            if (attemptCount < 3) {
                answerInputLayout.setError("Răspuns incorect. Mai încearcă! (" + (3 - attemptCount) + " încercări rămase)");
                shakeView(answerInputLayout);
                
                // Give more hints for later attempts
                if (attemptCount == 1) {
                    String hintString = "Indiciu: Începe cu litera '" + correctAnswer.charAt(0) + "'";
                    hintText.setText(hintString);
                } else if (attemptCount == 2) {
                    String hintString = "Indiciu: '" + correctAnswer.substring(0, Math.min(3, correctAnswer.length())) + "...'";
                    hintText.setText(hintString);
                    hintText.setTextColor(Color.RED);
                }
            } else {
                // After 3 failed attempts, show the correct answer
                answerInputLayout.setError("Răspuns incorect. Răspunsul corect este: " + correctAnswer);
                
                // After showing the correct answer, allow to proceed
                submitButton.setText("Continuă");
                submitButton.setOnClickListener(v -> {
                    setResult(RESULT_CANCELED);
                    finish();
                });
            }
        }
    }
    
    private void showSuccessAnimation() {
        // Animate success
        View successOverlay = findViewById(R.id.successOverlay);
        ImageView successIcon = findViewById(R.id.successIcon);
        
        successOverlay.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        successOverlay.startAnimation(fadeIn);
        
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        successIcon.startAnimation(pulse);
        
        // Show success message
        Toast.makeText(this, "Felicitări! Ai completat provocarea!", Toast.LENGTH_SHORT).show();
        
        // Finish with slight delay to show the success animation
        handler.postDelayed(() -> finish(), 2000);
    }

    private void shakeView(View view) {
        Animation shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        view.startAnimation(shakeAnimation);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear any pending callbacks
        handler.removeCallbacksAndMessages(null);
    }
}
