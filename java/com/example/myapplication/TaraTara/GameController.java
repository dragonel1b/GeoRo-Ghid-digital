package com.example.myapplication.TaraTara;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.HapticFeedbackConstants;

import com.example.myapplication.R;

public class GameController {
    private final Context context;
    private final GameUIController uiController;
    private final SoundController soundController;
    private Team playerTeam;
    private Team playerTeam2;
    private Team enemyTeam;
    private boolean isFleeingMode = false;
    private boolean isQTEActive = false;
    private boolean isQTESuccessful = false;
    private long qteStartTime = 0;
    private static final long QTE_DURATION = 3000; // 3 seconds for QTE window
    private static final long QTE_SUCCESS_WINDOW_START = 2000; // Start of success window at 2 seconds
    private static final long QTE_SUCCESS_WINDOW_END = 2500; // End of success window at 2.5 seconds

    public GameController(Context context, GameUIController uiController, SoundController soundController) {
        this.context = context;
        this.uiController = uiController;
        this.soundController = soundController;
    }

    public void handleShout() {
        if (!isFleeingMode) {
            soundController.playSound(SoundController.SoundType.SHOUT);
            soundController.vibrate(SoundController.VibrationPattern.BUTTON_PRESS);

            uiController.showGameMessage(R.string.team_call);
            uiController.setButtonStates(false, true);

            isFleeingMode = true;

            android.util.Log.d("TaraTaraVremOstasi", "Shout button pressed");

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                uiController.getShoutButton().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            }
        }
    }

    public void handleAnswer() {
        if (isFleeingMode) {
            soundController.playSound(SoundController.SoundType.ANSWER);
            uiController.showGameMessage(R.string.team_response);

            // Show selection instruction after team response
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                uiController.showGameMessage(R.string.select_enemy_soldier);
                // Enable avatar selection with click listener
                uiController.enableAvatarSelection(enemyTeam.getSoldierCount(), v -> enemyAvatarSelected());
            }, 1000);

            android.util.Log.d("TaraTaraVremOstasi", "Answer button pressed");

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                uiController.getAnswerButton().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            }
        }
    }

    /**
     * Called when an enemy avatar is selected
     * Triggers the Quick Time Event
     */
    public void enemyAvatarSelected() {
        if (enemyTeam == null || enemyTeam.getSoldierCount() == 0) {
            android.util.Log.e("GameController", "Enemy team is null or has no soldiers");
            return;
        }

        android.util.Log.d("TaraTaraVremOstasi", "Enemy avatar selected, activating QTE");

        // Provide haptic feedback for selection
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            uiController.getAnswerButton().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }

        // Activate the Quick Time Event
        activateQTE();
    }

    public void handleQTEClick(View v) {
        if (!isQTEActive) return;

        long clickTime = System.currentTimeMillis() - qteStartTime;
        boolean isSuccessful = clickTime >= QTE_SUCCESS_WINDOW_START &&
                clickTime <= QTE_SUCCESS_WINDOW_END;

        android.util.Log.d("GameController", "QTE Click - Time: " + clickTime +
                "ms, Success Window: " + QTE_SUCCESS_WINDOW_START + "-" + QTE_SUCCESS_WINDOW_END +
                "ms, Success: " + isSuccessful);

        soundController.vibrate(isSuccessful ? SoundController.VibrationPattern.SUCCESS : SoundController.VibrationPattern.FAILURE);
        soundController.playSound(isSuccessful ? SoundController.SoundType.SUCCESS : SoundController.SoundType.FAILURE);

        endQTE(isSuccessful);
    }

    public void activateQTE() {
        android.util.Log.d("TaraTaraVremOstasi", "Activating QTE");
        isQTEActive = true;
        isQTESuccessful = false;
        qteStartTime = System.currentTimeMillis();

        uiController.showQTEElements(v -> handleQTEClick(v));

        // End QTE after duration if not clicked
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isQTEActive) {
                android.util.Log.d("GameController", "QTE timeout - no click detected");
                endQTE(false);
            }
        }, QTE_DURATION);
    }

    private void endQTE(boolean successful) {
        isQTEActive = false;
        isQTESuccessful = successful;

        soundController.playSound(successful ? SoundController.SoundType.SUCCESS : SoundController.SoundType.FAILURE);
        uiController.handleQTEEnd(successful, () -> {
            isFleeingMode = false;
            updateTeamStates(successful);

            // Only end the game if one team has no soldiers left
            if (playerTeam.getSoldierCount() == 0 || enemyTeam.getSoldierCount() == 0) {
                uiController.showGameOver(enemyTeam.getSoldierCount() == 0);
            } else {
                // Continue the game by updating UI for the next round
                uiController.updateControlButtons(true);
                uiController.updateAvatarViews(playerTeam.getSoldierCount(), enemyTeam.getSoldierCount());
                android.util.Log.d("GameController", "Round complete, continuing game");
            }
        });
    }

    private void updateTeamStates(boolean qteSuccessful) {
        if (playerTeam == null || enemyTeam == null) {
            android.util.Log.e("GameController", "Team data not initialized.");
            return;
        }

        // Handle soldier transfer based on QTE result
        if (qteSuccessful) {
            if (enemyTeam.getSoldierCount() > 0) {
                Soldier soldier = enemyTeam.getRandomSoldier();
                if (soldier != null) {
                    enemyTeam.removeSoldier(soldier);
                    playerTeam.addSoldier(soldier.getX(), soldier.getY());
                    android.util.Log.d("GameController", "Soldier transferred to player team");
                } else {
                    android.util.Log.w("GameController", "getRandomSoldier() returned null.");
                }
            }
        } else {
            if (playerTeam.getSoldierCount() > 0) {
                Soldier soldier = playerTeam.getRandomSoldier();
                if (soldier != null) {
                    playerTeam.removeSoldier(soldier);
                    enemyTeam.addSoldier(soldier.getX(), soldier.getY());
                    android.util.Log.d("GameController", "Soldier transferred to enemy team");
                } else {
                    android.util.Log.w("GameController", "getRandomSoldier() returned null.");
                }
            }
        }

        // Update UI with null for playerTeam2 (removed from game)
        uiController.updateTeamUI(playerTeam, enemyTeam, null, isFleeingMode);
        android.util.Log.d("GameController", "Teams updated - Player: " + playerTeam.getSoldierCount() +
                ", Enemy: " + enemyTeam.getSoldierCount());
    }

    public void setTeams(Team playerTeam1, Team enemyTeam, Team playerTeam2) {
        if (playerTeam1 == null || enemyTeam == null) {
            android.util.Log.e("GameController", "Cannot initialize with null teams");
            return;
        }
        this.playerTeam = playerTeam1;
        this.enemyTeam = enemyTeam;
        // playerTeam2 is not used in 5v5 mode
        this.playerTeam2 = null;
        android.util.Log.d("GameController", "Teams initialized for 5v5 mode - Player: " +
                playerTeam.getSoldierCount() + ", Enemy: " + enemyTeam.getSoldierCount());

        // Initial UI update
        uiController.updateTeamUI(playerTeam, enemyTeam, null, false);
    }

    public boolean isFleeingMode() {
        return isFleeingMode;
    }

    public Team getPlayerTeam() {
        return playerTeam;
    }

    public Team getEnemyTeam() {
        return enemyTeam;
    }
}
