package com.example.myapplication.TaraTara;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.HapticFeedbackConstants;
import android.util.Log;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private final Context context;
    private final GameUIController uiController;
    private final SoundController soundController;
    private Team playerTeam;
    private Team playerTeam2;
    private Team enemyTeam;
    private Team activePlayerTeam; // Currently active player team

    // --- Game States ---
    private enum GameState {
        PLAYER_TURN_START,
        PLAYER_SHOUTED,
        PLAYER_SELECTING_ENEMY,
        PLAYER_CHASING,
        ENEMY_TURN_START,
        ENEMY_SHOUTED,
        ENEMY_SELECTING_PLAYER,
        ENEMY_CHASING,
        ROUND_OVER,
        GAME_OVER
    }

    private GameState currentState = GameState.PLAYER_TURN_START;
    private boolean isPlayerTurn = true;
    private int roundCount = 0;
    private Soldier currentTargetSoldier = null;

    // --- Chase mechanic parameters ---
    private boolean isActionActive = false;
    private boolean isActionSuccessful = false;
    private long actionStartTime = 0;
    private static final long CHASE_DURATION = 6000; // 6 seconds for chase

    public GameController(Context context, GameUIController uiController, SoundController soundController) {
        this.context = context;
        this.uiController = uiController;
        this.soundController = soundController;
    }

    // --- Game Flow Control ---

    public void startGame(Team playerTeam, Team enemyTeam) {
        startGame(playerTeam, null, enemyTeam);
    }
    
    public void startGame(Team playerTeam, Team playerTeam2, Team enemyTeam) {
        if (playerTeam == null || enemyTeam == null) {
            Log.e("GameController", "Cannot start game with null teams");
            return;
        }
        this.playerTeam = playerTeam;
        this.playerTeam2 = playerTeam2;
        this.enemyTeam = enemyTeam;
        this.activePlayerTeam = playerTeam; // Start with first player team
        this.roundCount = 0;
        
        Log.d("GameController", "Game started - Player1: " + playerTeam.getSoldierCount() + 
              (playerTeam2 != null ? ", Player2: " + playerTeam2.getSoldierCount() : "") + 
              ", Enemy: " + enemyTeam.getSoldierCount());

        isPlayerTurn = true;
        setGameState(GameState.PLAYER_TURN_START);
    }

    private void setGameState(GameState newState) {
        Log.d("GameController", "Changing state from " + currentState + " to " + newState);
        currentState = newState;
        updateUIForState();

        switch (newState) {
            case PLAYER_TURN_START:
                uiController.showGameMessage(R.string.player_turn);
                uiController.setButtonStates(true, false);
                // Highlight active player team
                if (playerTeam2 != null) {
                    uiController.highlightActiveTeam(activePlayerTeam == playerTeam ? 1 : 2);
                }
                break;
                
            case ENEMY_TURN_START:
                uiController.showGameMessage(R.string.enemy_turn);
                uiController.setButtonStates(false, false);
                initiateEnemyTurn();
                break;
                
            case PLAYER_SHOUTED:
                uiController.showGameMessage(R.string.team_call);
                new Handler(Looper.getMainLooper()).postDelayed(this::simulateEnemyAnswer, 1000);
                break;
                
            case ENEMY_SHOUTED:
                uiController.showGameMessage(R.string.enemy_team_call);
                uiController.setButtonStates(false, true);
                break;
                
            case PLAYER_SELECTING_ENEMY:
                uiController.showGameMessage(R.string.select_enemy_soldier);
                uiController.enableAvatarSelection(enemyTeam.getSoldierCount(), v -> enemyAvatarSelected(v));
                break;
                
            case ENEMY_SELECTING_PLAYER:
                uiController.showGameMessage(R.string.enemy_selecting);
                new Handler(Looper.getMainLooper()).postDelayed(this::enemySelectPlayer, 1500);
                break;
                
            case PLAYER_CHASING:
            case ENEMY_CHASING:
                activateChaseMechanic();
                break;
                
            case ROUND_OVER:
                roundCount++;
                switchActivePlayerTeam(); // Switch between player teams if available
                
                // Short pause before starting next turn
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isPlayerTurn = !isPlayerTurn;
                    setGameState(isPlayerTurn ? GameState.PLAYER_TURN_START : GameState.ENEMY_TURN_START);
                }, 2000);
                break;
                
            case GAME_OVER:
                boolean playerWon = enemyTeam.getSoldierCount() == 0;
                uiController.showGameOver(playerWon);
                soundController.playSound(playerWon ? 
                    SoundController.SoundType.SUCCESS : 
                    SoundController.SoundType.FAILURE);
                break;
        }
    }

    private void updateUIForState() {
        // Update UI with active player team instead of always playerTeam
        uiController.updateTeamUI(activePlayerTeam, enemyTeam, isPlayerTurn);
    }

    // Switch between player teams if playerTeam2 exists
    private void switchActivePlayerTeam() {
        if (playerTeam2 != null && playerTeam2.getSoldierCount() > 0) {
            if (activePlayerTeam == playerTeam && playerTeam.getSoldierCount() > 0) {
                activePlayerTeam = playerTeam2;
                Log.d("GameController", "Switched to player team 2");
            } else if (activePlayerTeam == playerTeam2 && playerTeam2.getSoldierCount() > 0) {
                activePlayerTeam = playerTeam;
                Log.d("GameController", "Switched to player team 1");
            }
            // If active team has no soldiers, stay with the team that has soldiers
            else if (playerTeam.getSoldierCount() == 0) {
                activePlayerTeam = playerTeam2;
            } else {
                activePlayerTeam = playerTeam;
            }
        }
    }

    // --- Player Actions ---

    public void handleShout() {
        if (currentState == GameState.PLAYER_TURN_START) {
            Log.d("TaraTaraVremOstasi", "Player Shout button pressed");
            soundController.playSound(SoundController.SoundType.SHOUT);
            soundController.vibrate(SoundController.VibrationPattern.BUTTON_PRESS);
            uiController.setButtonStates(false, false);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                uiController.getShoutButton().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            }
            
            setGameState(GameState.PLAYER_SHOUTED);
        } else {
            Log.w("GameController", "Shout ignored, current state: " + currentState);
        }
    }

    public void handleAnswer() {
        if (currentState == GameState.ENEMY_SHOUTED) {
            Log.d("TaraTaraVremOstasi", "Player Answer button pressed");
            soundController.playSound(SoundController.SoundType.ANSWER);
            uiController.showGameMessage(R.string.player_response);
            uiController.setButtonStates(false, false);

            setGameState(GameState.ENEMY_SELECTING_PLAYER);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                uiController.getAnswerButton().performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            }
        } else {
            Log.w("GameController", "Answer ignored, current state: " + currentState);
        }
    }

    private void simulateEnemyAnswer() {
        if (currentState == GameState.PLAYER_SHOUTED) {
            Log.d("TaraTaraVremOstasi", "Simulating Enemy Answer");
            soundController.playSound(SoundController.SoundType.ANSWER);
            uiController.showGameMessage(R.string.team_response);

            setGameState(GameState.PLAYER_SELECTING_ENEMY);
        }
    }

    public void enemyAvatarSelected(View v) {
        if (currentState != GameState.PLAYER_SELECTING_ENEMY) return;

        if (enemyTeam == null || enemyTeam.getSoldierCount() == 0) {
            Log.e("GameController", "Enemy team is null or has no soldiers during selection");
            return;
        }

        // Get the selected soldier
        currentTargetSoldier = null;
        Object tag = v.getTag();
        if (tag instanceof Integer) {
            int index = (Integer) tag;
            List<Soldier> enemySoldiers = enemyTeam.getSoldiers();
            if (index >= 0 && index < enemySoldiers.size()) {
                currentTargetSoldier = enemySoldiers.get(index);
                Log.d("TaraTaraVremOstasi", "Enemy avatar selected by player: Index " + index + 
                    " (Soldier Health: " + currentTargetSoldier.getHealth() + ")");
            } else {
                Log.e("GameController", "Invalid index (" + index + ") from avatar tag. Enemy count: " + 
                    enemySoldiers.size());
            }
        } else {
            Log.e("GameController", "Avatar tag is not an Integer or is null.");
        }

        uiController.disableAvatarSelection();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && v != null) {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }

        setGameState(GameState.PLAYER_CHASING);
    }

    // --- Enemy Actions ---

    private void initiateEnemyTurn() {
        Log.d("GameController", "Initiating Enemy Turn");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentState == GameState.ENEMY_TURN_START) {
                Log.d("TaraTaraVremOstasi", "Enemy AI Shouting");
                soundController.playSound(SoundController.SoundType.SHOUT);
                setGameState(GameState.ENEMY_SHOUTED);
            }
        }, 1500);
    }

    private void enemySelectPlayer() {
        if (currentState != GameState.ENEMY_SELECTING_PLAYER) return;

        // Check if active player team has soldiers
        if (activePlayerTeam == null || activePlayerTeam.getSoldierCount() == 0) {
            Log.e("GameController", "Active player team is null or has no soldiers for enemy selection");
            
            // Try to switch to other player team if it exists and has soldiers
            if (playerTeam2 != null && activePlayerTeam == playerTeam && playerTeam2.getSoldierCount() > 0) {
                activePlayerTeam = playerTeam2;
            } else if (activePlayerTeam == playerTeam2 && playerTeam.getSoldierCount() > 0) {
                activePlayerTeam = playerTeam;
            } else {
                endRound(false); // Enemy fails if no player to choose
                return;
            }
        }
        
        Log.d("TaraTaraVremOstasi", "Enemy AI selecting player soldier...");
        currentTargetSoldier = null;

        // AI Strategy: Target lowest health soldier
        List<Soldier> playerSoldiers = activePlayerTeam.getSoldiers();
        Soldier targetSoldier = null;
        float minHealth = Float.MAX_VALUE;

        for (Soldier soldier : playerSoldiers) {
            if (soldier.getHealth() < minHealth) {
                minHealth = soldier.getHealth();
                targetSoldier = soldier;
            }
        }

        // Fallback to random if needed
        if (targetSoldier == null) {
            Log.w("GameController", "AI couldn't find lowest health soldier, falling back to random.");
            targetSoldier = activePlayerTeam.getRandomSoldier();
        }
        
        currentTargetSoldier = targetSoldier;

        if (currentTargetSoldier != null) {
            Log.d("GameController", "Enemy AI selected player soldier (Health: " + 
                currentTargetSoldier.getHealth() + ")");
            uiController.highlightPlayerSoldier(activePlayerTeam == playerTeam ? 1 : 2, 
                playerSoldiers.indexOf(currentTargetSoldier));
            setGameState(GameState.ENEMY_CHASING);
        } else {
            Log.e("GameController", "Enemy AI failed to select a player soldier (targetSoldier is null).");
            endRound(false);
        }
    }

    // --- Chase Mechanic ---

    public void activateChaseMechanic() {
        Log.d("TaraTaraVremOstasi", "Activating Chase Mechanic (State: " + currentState + ")");
        isActionActive = true;
        isActionSuccessful = false;
        actionStartTime = System.currentTimeMillis();

        // Difficulty increases with round count
        float difficulty = Math.min(0.8f, 0.4f + (roundCount * 0.05f));
        
        uiController.showChaseElements(difficulty, this::reportChaseOutcome);

        // Timeout handler
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isActionActive) {
                Log.d("GameController", "Chase timeout - " + (isPlayerTurn ? "Player" : "Enemy") + " failed.");
                reportChaseOutcome(false);
            }
        }, CHASE_DURATION);
    }

    public void reportChaseOutcome(boolean successful) {
        if (!isActionActive) return;
        isActionActive = false;

        Log.d("GameController", "Chase outcome reported: " + successful);
        soundController.vibrate(successful ? 
            SoundController.VibrationPattern.SUCCESS : 
            SoundController.VibrationPattern.FAILURE);
        soundController.playSound(successful ? 
            SoundController.SoundType.SUCCESS : 
            SoundController.SoundType.FAILURE);

        endChase(successful);
    }

    private void endChase(boolean successful) {
        isActionSuccessful = successful;
        Log.d("GameController", "Ending Chase Sequence. Success: " + successful + " by " + 
            (isPlayerTurn ? "Player" : "Enemy"));

        uiController.handleChaseEnd(successful, () -> {
            if (isPlayerTurn) {
                updateTeamStates(activePlayerTeam, enemyTeam, successful);
            } else {
                updateTeamStates(enemyTeam, activePlayerTeam, successful);
            }

            // Check for game over
            if (getTotalPlayerSoldierCount() == 0 || enemyTeam.getSoldierCount() == 0) {
                setGameState(GameState.GAME_OVER);
            } else {
                setGameState(GameState.ROUND_OVER);
            }
        });
    }

    // Get total count of soldiers from both player teams
    private int getTotalPlayerSoldierCount() {
        int count = playerTeam.getSoldierCount();
        if (playerTeam2 != null) {
            count += playerTeam2.getSoldierCount();
        }
        return count;
    }

    // --- Team Management & Round End ---

    private void updateTeamStates(Team chasingTeam, Team targetTeam, boolean chaseSuccessful) {
        if (chasingTeam == null || targetTeam == null) {
            Log.e("GameController", "Team data not initialized during update.");
            return;
        }

        Team winnerTeam = chaseSuccessful ? chasingTeam : targetTeam;
        Team loserTeam = chaseSuccessful ? targetTeam : chasingTeam;
        Soldier transferredSoldier = null;

        if (loserTeam.getSoldierCount() > 0) {
            // Use tracked target soldier if possible
            if (currentTargetSoldier != null && loserTeam.getSoldiers().contains(currentTargetSoldier)) {
                transferredSoldier = currentTargetSoldier;
                Log.d("GameController", "Using specific target soldier for transfer.");
            } else {
                Log.w("GameController", "Target soldier invalid or not found. Falling back to random transfer.");
                transferredSoldier = loserTeam.getRandomSoldier();
            }

            if (transferredSoldier != null) {
                loserTeam.removeSoldier(transferredSoldier);
                winnerTeam.addSoldier();
                Log.d("GameController", "Soldier transferred from " +
                        getTeamName(loserTeam) + " to " +
                        getTeamName(winnerTeam) + " team.");
            } else {
                Log.w("GameController", "getRandomSoldier() returned null from loser team (" + 
                    getTeamName(loserTeam) + ").");
            }
        } else {
            Log.w("GameController", "Loser team has no soldiers left to transfer.");
        }

        currentTargetSoldier = null;
        uiController.updateTeamUI(activePlayerTeam, enemyTeam, isPlayerTurn);
        
        // Update UI count for both player teams if playerTeam2 exists
        if (playerTeam2 != null) {
            uiController.updateTeamCounts(playerTeam.getSoldierCount(), playerTeam2.getSoldierCount(), 
                enemyTeam.getSoldierCount());
        }
        
        Log.d("GameController", "Teams updated - Player1: " + playerTeam.getSoldierCount() + 
            (playerTeam2 != null ? ", Player2: " + playerTeam2.getSoldierCount() : "") + 
            ", Enemy: " + enemyTeam.getSoldierCount());
    }

    private String getTeamName(Team team) {
        if (team == playerTeam) return "Player1";
        if (team == playerTeam2) return "Player2";
        if (team == enemyTeam) return "Enemy";
        return "Unknown";
    }

    private void endRound(boolean successful) {
        Log.d("GameController", "Round over: " + (successful ? "Success" : "Failure"));
        uiController.showRoundResult(successful);
         
        // Check for game over
        if (enemyTeam.getSoldierCount() == 0) {
            setGameState(GameState.GAME_OVER);
        } else if (getTotalPlayerSoldierCount() == 0) {
            setGameState(GameState.GAME_OVER);
        } else {
            setGameState(GameState.ROUND_OVER);
        }
    }

    // --- Getters ---

    public GameState getCurrentState() {
        return currentState;
    }

    public Team getPlayerTeam() {
        return playerTeam;
    }
    
    public Team getPlayerTeam2() {
        return playerTeam2;
    }

    public Team getEnemyTeam() {
        return enemyTeam;
    }
    
    public Team getActivePlayerTeam() {
        return activePlayerTeam;
    }
}
