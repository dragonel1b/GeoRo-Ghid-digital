package com.example.myapplication.Joc1;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class RomQuestChallengeActivity extends AppCompatActivity {
    private TextView missionTitle;
    private TextView missionDescription;
    private LinearLayout objectivesContainer;
    private TextView challengePrompt;
    private TextInputLayout answerInputLayout;
    private MaterialButton submitButton;
    private MaterialButton cancelButton;

    private String[] objectives;
    private String correctAnswer;
    private int missionId;

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

        // Set up challenge based on mission
        setupChallenge(title);

        initializeViews();
        setupToolbar(title);
        populateMissionDetails(title, description);
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
    }

    private void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    private void populateMissionDetails(String title, String description) {
        missionTitle.setText(title);
        missionDescription.setText(description);

        // Add objectives
        if (objectives != null) {
            for (String objective : objectives) {
                TextView objectiveView = new TextView(this);
                objectiveView.setText("• " + objective);
                objectiveView.setTextAppearance(R.style.RomTextBody);
                objectiveView.setPadding(0, 4, 0, 4);
                objectivesContainer.addView(objectiveView);
            }
        }
    }

    private void setupChallenge(String missionTitle) {
        // Set challenge details based on mission
        if (missionTitle.contains("Sibiu")) {
            challengePrompt.setText("Pentru a completa această misiune, răspunde la următoarea întrebare:\n\n" +
                    "Care este povestea care a dat numele Podului Minciunilor? (Hint: Are legătură cu comercianții)");
            correctAnswer = "comercianți mințeau";
        } else if (missionTitle.contains("Brașov")) {
            challengePrompt.setText("Pentru a debloca această misiune, rezolvă următorul puzzle:\n\n" +
                    "Sunt neagră, dar nu din naștere\nFocul m-a întunecat\nDar credința am păstrat\nCe sunt eu?");
            correctAnswer = "biserica neagră";
        } else if (missionTitle.contains("Cluj")) {
            challengePrompt.setText("Rezolvă următorul mister pentru a continua:\n\n" +
                    "Rege drept am fost numit\nÎn Cluj m-am născut\nDreptatea am împărțit\nCine sunt eu?");
            correctAnswer = "matei corvin";
        } else if (missionTitle.contains("București")) {
            challengePrompt.setText("Ghicește ghicitoarea capitalei:\n\n" +
                    "În perioada interbelică\nCu Paris m-au comparat\nȘi un nume mi-au dat\nCare-i porecla mea istorică?");
            correctAnswer = "micul paris";
        } else if (missionTitle.contains("Iași")) {
            challengePrompt.setText("Descoperă secretul Mănăstirii Trei Ierarhi:\n\n" +
                    "Din Orient au venit\nPe ziduri au sculptat\nDin piatră au creat\nCe meșteri au lucrat?");
            correctAnswer = "meșteri armeni";
        } else if (missionTitle.contains("Timișoara")) {
            challengePrompt.setText("Rezolvă enigma luminii:\n\n" +
                    "Prima în Europa am fost\nCând străzile s-au luminat\nCu becuri electrice-am strălucit\nÎn ce an s-a întâmplat?");
            correctAnswer = "1884";
        } else {
            // Default challenge
            challengePrompt.setText("Pentru a completa această misiune, demonstrează-ți cunoștințele despre locație răspunzând la întrebarea de mai sus.");
            correctAnswer = "default";
        }
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

        if (answer.contains(correctAnswer)) {
            // Success!
            Intent resultIntent = new Intent();
            resultIntent.putExtra("mission_id", missionId);
            setResult(RESULT_OK, resultIntent);

            // Show success animation/message
            Toast.makeText(this, "Felicitări! Ai completat provocarea!", Toast.LENGTH_SHORT).show();

            // Finish with slight delay to show the success message
            submitButton.postDelayed(this::finish, 1000);
        } else {
            // Wrong answer
            answerInputLayout.setError("Răspuns incorect. Încearcă din nou!");
            shakeView(answerInputLayout);
        }
    }

    private void shakeView(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
