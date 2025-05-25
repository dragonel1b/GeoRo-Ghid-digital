package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

public class MinigameOpenWorldActivity extends AppCompatActivity
        implements MinigameGameView.OnScoreChangeListener,
        MinigameGameView.OnMissionChangeListener,
        MinigameGameView.OnLevelChangeListener,
        MinigameGameView.OnNPCInteractionListener {

    private static final String TAG = "MinigameOpenWorld";
    private MinigameGameView gameView;
    private TextView scoreText, levelText, missionText;
    private MaterialButton upButton, downButton, leftButton, rightButton, interactButton;
    private RomGameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame_openworld);

        gameState = RomGameState.getInstance();
        gameState.initialize(this);

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
            gameView.setOnLevelChangeListener(this);
            gameView.setOnNPCInteractionListener(this);

            // Find views
            scoreText = findViewById(R.id.scoreText);
            levelText = findViewById(R.id.levelText);
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
            if (scoreText == null || levelText == null || missionText == null ||
                    upButton == null || downButton == null || leftButton == null ||
                    rightButton == null || interactButton == null) {
                Log.e(TAG, "Failed to initialize required views");
                finish();
                return;
            }

            // Initialize UI
            scoreText.setText(getString(R.string.minigame_score, 0));
            levelText.setText(getString(R.string.minigame_level, 1));
            missionText.setText(getString(R.string.minigame_mission,
                    getString(R.string.minigame_collect_mission, 3)));

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

    private void showInstructions() {
        Snackbar.make(gameView,
                getString(R.string.minigame_instructions),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onScoreChanged(int newScore) {
        runOnUiThread(() -> {
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
            missionText.setText(getString(R.string.minigame_mission, newMission));
            Snackbar.make(gameView,
                    getString(R.string.minigame_new_mission, newMission),
                    Snackbar.LENGTH_LONG).show();
        });
    }

    @Override
    public void onLevelChanged(int newLevel) {
        runOnUiThread(() -> {
            levelText.setText(getString(R.string.minigame_level, newLevel));
            Snackbar.make(gameView,
                    getString(R.string.minigame_level_up, newLevel),
                    Snackbar.LENGTH_LONG).show();

            // Award extra wisdom points for level up
            gameState.addPuncteIntelepte(10 * newLevel, this);
        });
    }

    private NPC currentNPC;

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
        // Save final score and award wisdom points
        String scoreStr = scoreText.getText().toString();
        int finalScore = Integer.parseInt(scoreStr.substring(scoreStr.lastIndexOf(" ") + 1));
        int wisdomPoints = finalScore / 10;
        if (wisdomPoints > 0) {
            gameState.addPuncteIntelepte(wisdomPoints, this);
        }
        setResult(RESULT_OK);
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
