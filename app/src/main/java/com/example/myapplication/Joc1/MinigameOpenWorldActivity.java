package com.example.myapplication.Joc1;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class MinigameOpenWorldActivity extends AppCompatActivity
        implements MinigameGameView.OnScoreChangeListener,
        MinigameGameView.OnMissionChangeListener,
        MinigameGameView.OnNPCInteractionListener {

    private static final String TAG = "MinigameOpenWorld";
    private MinigameGameView gameView;
    private TextView scoreText, missionText;
    private MaterialButton upButton, downButton, leftButton, rightButton, interactButton;
    private RomGameState gameState;
    
    // Save game state
    private int currentScore = 0;
    private String currentMission = "";
    private NPC currentNPC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame_openworld);

        gameState = RomGameState.getInstance();
        gameState.initialize(this);

        // Restore any saved instance state
        if (savedInstanceState != null) {
            currentScore = savedInstanceState.getInt("currentScore", 0);
            currentMission = savedInstanceState.getString("currentMission", "");
        }

        initializeViews();
        setupControls();
        showInstructions();
    }

    private void initializeViews() {
        try {
            gameView = findViewById(R.id.gameView);
            if (gameView == null) {
                Log.e(TAG, "Failed to initialize game view");
                finish();
                return;
            }

            // Set up listeners
            gameView.setOnScoreChangeListener(this);
            gameView.setOnMissionChangeListener(this);
            gameView.setOnNPCInteractionListener(this);

            // Find views
            scoreText = findViewById(R.id.scoreText);
            missionText = findViewById(R.id.missionText);
            upButton = findViewById(R.id.upButton);
            downButton = findViewById(R.id.downButton);
            leftButton = findViewById(R.id.leftButton);
            rightButton = findViewById(R.id.rightButton);
            interactButton = findViewById(R.id.interactButton);
            interactButton.setContentDescription(getString(R.string.minigame_interact));

            // Configure button icons and rotations
            MaterialButton[] buttons = {upButton, leftButton, rightButton, downButton};
            int[] rotations = {270, 0, 180, 90};

            for (int i = 0; i < buttons.length; i++) {
                MaterialButton button = buttons[i];
                button.setRotation(rotations[i]);
                button.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
                button.setIconResource(R.drawable.ic_back);
            }

            // Verify all required views are found
            if (scoreText == null || missionText == null ||
                    upButton == null || downButton == null || leftButton == null ||
                    rightButton == null || interactButton == null) {
                Log.e(TAG, "Failed to initialize required views");
                finish();
                return;
            }

            // Initialize UI with restored or default values
            scoreText.setText(getString(R.string.minigame_score, currentScore));
            if (!currentMission.isEmpty()) {
                missionText.setText(getString(R.string.minigame_mission, currentMission));
            } else {
                missionText.setText(getString(R.string.minigame_mission,
                        getString(R.string.minigame_collect_mission, 3)));
            }

            // Set up interact button
            interactButton.setOnClickListener(v -> {
                // Handle NPC interaction
                gameView.stopPlayer();
                showNPCDialog();
            });
            interactButton.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            finish();
        }
    }

    private void setupControls() {
        View.OnTouchListener buttonTouchListener = (v, event) -> {
            MaterialButton button = (MaterialButton) v;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (v == upButton) {
                        gameView.movePlayer(0, -1);
                    } else if (v == downButton) {
                        gameView.movePlayer(0, 1);
                    } else if (v == leftButton) {
                        gameView.movePlayer(-1, 0);
                    } else if (v == rightButton) {
                        gameView.movePlayer(1, 0);
                    }
                    button.setPressed(true);
                    button.setAlpha(0.7f);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    gameView.stopPlayer();
                    button.setPressed(false);
                    button.setAlpha(1.0f);
                    break;
            }
            return true;
        };

        upButton.setOnTouchListener(buttonTouchListener);
        downButton.setOnTouchListener(buttonTouchListener);
        leftButton.setOnTouchListener(buttonTouchListener);
        rightButton.setOnTouchListener(buttonTouchListener);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Configuration changed. Orientation: " + 
                (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait"));
        
        // The layout is handled automatically due to the layout-land resource directory
        // and the configChanges attribute in the AndroidManifest.xml
        
        // Preserve game state during orientation change
        if (gameView != null) {
            gameView.pauseGame();
            gameView.resumeGame();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save game state
        outState.putInt("currentScore", currentScore);
        outState.putString("currentMission", currentMission);
        Log.d(TAG, "Game state saved. Score: " + currentScore);
    }

    private void showInstructions() {
        Snackbar.make(gameView,
                getString(R.string.minigame_instructions),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onScoreChanged(int newScore) {
        runOnUiThread(() -> {
            currentScore = newScore;
            scoreText.setText(getString(R.string.minigame_score, newScore));

            // Award wisdom points for every 50 points scored
            if (newScore % 50 == 0) {
                gameState.addPuncteIntelepte(5, this);
                Snackbar.make(gameView,
                        getString(R.string.minigame_points_earned, 5),
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMissionChanged(String newMission) {
        runOnUiThread(() -> {
            currentMission = newMission;
            missionText.setText(getString(R.string.minigame_mission, newMission));
            Snackbar.make(gameView,
                    getString(R.string.minigame_new_mission, newMission),
                    Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onNPCNearby(NPC npc) {
        runOnUiThread(() -> {
            currentNPC = npc;
            interactButton.setVisibility(View.VISIBLE);
            Snackbar.make(gameView,
                    getString(R.string.minigame_npc_nearby, npc.getName()),
                    Snackbar.LENGTH_SHORT).show();
        });
    }

    private void showNPCDialog() {
        if (currentNPC == null) return;

        String dialogue = currentNPC.triggerDialogue();
        Mission quest = currentNPC.getNpcQuest();

        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_npc, null);
        TextView titleView = dialogView.findViewById(R.id.npcDialogTitle);
        TextView messageView = dialogView.findViewById(R.id.npcDialogMessage);
        TextView questView = dialogView.findViewById(R.id.npcDialogQuest);

        // Set dialog content
        titleView.setText(getString(R.string.minigame_npc_title, currentNPC.getName()));
        messageView.setText(dialogue);

        // Show quest info if available
        if (quest != null && !currentNPC.isInteracted()) {
            questView.setVisibility(View.VISIBLE);
            questView.setText(getString(R.string.minigame_npc_quest_info, quest.getDescription()));
        } else {
            questView.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // If NPC has an unaccepted quest, show accept button
        if (quest != null && !currentNPC.isInteracted()) {
            builder.setPositiveButton(getString(R.string.minigame_npc_quest_accept),
                    (dialog, which) -> {
                        currentNPC.setInteracted(true);
                        gameView.stopPlayer();
                        interactButton.setVisibility(View.GONE);

                        // Add quest points when accepting
                        gameState.addPuncteIntelepte(10, this);
                        Snackbar.make(gameView,
                                getString(R.string.minigame_points_earned, 10),
                                Snackbar.LENGTH_SHORT).show();
                    });
        }

        builder.setNegativeButton(getString(R.string.minigame_npc_quest_decline),
                (dialog, which) -> {
                    gameView.stopPlayer();
                    interactButton.setVisibility(View.GONE);
                });

        AlertDialog dialog = builder.create();
        dialog.show();
        currentNPC = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        if (gameView != null) {
            gameView.pauseGame();
        }
        
        // Save final score and award wisdom points
        String scoreStr = scoreText.getText().toString();
        try {
            int finalScore = Integer.parseInt(scoreStr.substring(scoreStr.lastIndexOf(" ") + 1));
            int wisdomPoints = finalScore / 10;
            if (wisdomPoints > 0) {
                gameState.addPuncteIntelepte(wisdomPoints, this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing score: " + e.getMessage());
        }
        setResult(RESULT_OK);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resumeGame();
        }
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
