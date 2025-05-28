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
import java.util.Random;

public class GameController {
    private static final String TAG = "GameController";
    
    // Dependencies
    private final Context context;
    private final GameUIController uiController;
    private final SoundController soundController;
    
    // Game data
    private Team playerTeam;
    private Team enemyTeam;
    
    // Game state
    private GameState currentState;
    private boolean isPlayerTurn = true;
    private int roundCount = 0;
    private Soldier currentTargetSoldier = null;
    
    // Chase mechanic parameters
    private boolean isActionActive = false;
    private boolean isActionSuccessful = false;
    private long actionStartTime = 0;
    private static final long CHASE_DURATION = 6000; // 6 seconds for chase
    
    // Random for more controlled randomness
    private final Random random = new Random();

    /**
     * GameState pattern implementation for better state management
     */
    public interface GameState {
        void enter();
        void exit();
        boolean canTransitionTo(GameState nextState);
        String getName();
    }
    
    // Base state with common functionality
    private abstract class BaseGameState implements GameState {
        @Override
        public void exit() {
            // Default implementation - can be overridden by specific states
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            // Default implementation allows transitions to specific next states
            // Override in concrete states
            return true;
        }
        
        @Override
        public String getName() {
            return this.getClass().getSimpleName();
        }
    }
    
    // Concrete game states
    private final GameState playerTurnStart = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering PlayerTurnStart state");
            uiController.showGameMessage(R.string.player_turn);
            uiController.setButtonStates(true, false);
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == playerShouted;
        }
    };
    
    private final GameState playerShouted = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering PlayerShouted state");
            uiController.showGameMessage(R.string.team_call);
            uiController.setButtonStates(false, false);
            
            // Simulate enemy response after a delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                simulateEnemyAnswer();
            }, 1000);
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == playerSelectingEnemy;
        }
    };
    
    private final GameState playerSelectingEnemy = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering PlayerSelectingEnemy state");
            uiController.showGameMessage(R.string.select_enemy_soldier);
            uiController.enableAvatarSelection(enemyTeam.getSoldierCount(), v -> enemyAvatarSelected(v));
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == playerChasing;
        }
    };
    
    private final GameState playerChasing = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering PlayerChasing state");
            isActionActive = true;
            actionStartTime = System.currentTimeMillis();
            
            // Informăm controllerul UI să afișeze interfața mini-jocului
            uiController.showGameMessage(R.string.chase_prompt);
            uiController.showChaseUI(true, currentTargetSoldier);
            
            // Activăm mecanica de chase
            activateChaseMechanic();
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == roundOver || nextState == gameOver;
        }
    };
    
    private final GameState enemyTurnStart = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering EnemyTurnStart state");
            uiController.showGameMessage(R.string.enemy_turn);
            uiController.setButtonStates(false, false);
            initiateEnemyTurn();
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == enemyShouted;
        }
    };
    
    private final GameState enemyShouted = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering EnemyShouted state");
            uiController.showGameMessage(R.string.enemy_team_call);
            uiController.setButtonStates(false, true);
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == enemySelectingPlayer;
        }
    };
    
    private final GameState enemySelectingPlayer = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering EnemySelectingPlayer state");
            uiController.showGameMessage(R.string.enemy_selecting);
            
            // Enemy selects a player after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                enemySelectPlayer();
            }, 1500);
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == enemyChasing;
        }
    };
    
    private final GameState enemyChasing = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering EnemyChasing state");
            activateChaseMechanic();
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == roundOver || nextState == gameOver;
        }
    };
    
    private final GameState roundOver = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering RoundOver state");
            roundCount++;
            switchActivePlayerTeam();
            
            // Update UI
            uiController.updateRoundCounter(roundCount);
            
            // Short pause before starting next turn
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isPlayerTurn = !isPlayerTurn;
                transitionToState(isPlayerTurn ? playerTurnStart : enemyTurnStart);
            }, 2000);
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            return nextState == playerTurnStart || nextState == enemyTurnStart;
        }
    };
    
    private final GameState gameOver = new BaseGameState() {
        @Override
        public void enter() {
            Log.d(TAG, "Entering GameOver state");
            boolean playerWon = enemyTeam.getSoldierCount() == 0;
            uiController.showGameOver(playerWon);
            soundController.playSound(playerWon ? 
                SoundController.SoundType.SUCCESS : 
                SoundController.SoundType.FAILURE);
        }
        
        @Override
        public boolean canTransitionTo(GameState nextState) {
            // No valid transitions from game over
            return false;
        }
    };

    public GameController(Context context, GameUIController uiController, SoundController soundController) {
        this.context = context;
        this.uiController = uiController;
        this.soundController = soundController;
        
        Log.d(TAG, "GameController initialized");
        
        // Set initial state
        this.currentState = playerTurnStart;
    }

    // --- Game Flow Control ---

    public void startGame(Team playerTeam, Team enemyTeam) {
        if (playerTeam == null || enemyTeam == null) {
            Log.e(TAG, "Cannot start game with null teams");
            return;
        }
        
        this.playerTeam = playerTeam;
        this.enemyTeam = enemyTeam;
        this.roundCount = 0;
        
        // Reset game state
        isPlayerTurn = true;
        isActionActive = false;
        isActionSuccessful = false;
        currentTargetSoldier = null;
        
        Log.d(TAG, "Game started - Player: " + playerTeam.getSoldierCount() + 
              ", Enemy: " + enemyTeam.getSoldierCount());

        // Update UI
        if (uiController != null) {
            uiController.updateRoundCounter(roundCount);
            uiController.updateTeamUI(playerTeam, enemyTeam, isPlayerTurn);
            
            // Make sure buttons are in correct state
            uiController.setButtonStates(true, false);
        } else {
            Log.e(TAG, "UI Controller is null during game start");
        }
        
        // Transition to initial state
        transitionToState(playerTurnStart);
    }

    /**
     * Transitions to a new game state if the transition is valid
     */
    private void transitionToState(GameState newState) {
        if (currentState == newState) {
            Log.d(TAG, "Already in state " + newState.getName());
            return;
        }
        
        if (currentState != null && !currentState.canTransitionTo(newState)) {
            Log.e(TAG, "Invalid state transition from " + currentState.getName() + " to " + newState.getName());
            return;
        }
        
        Log.d(TAG, "Transitioning from " + (currentState != null ? currentState.getName() : "null") + " to " + newState.getName());
        
        // Exit current state
        if (currentState != null) {
            currentState.exit();
        }
        
        // Update current state
        currentState = newState;
        
        // Enter new state
        currentState.enter();
        
        // Update UI based on current state
        updateUIForState();
    }

    private void updateUIForState() {
        // Update UI with active player team
        if (uiController != null && playerTeam != null && enemyTeam != null) {
            uiController.updateTeamUI(playerTeam, enemyTeam, isPlayerTurn);
        } else {
            Log.e(TAG, "Cannot update UI, controllers or teams are null");
        }
    }

    // Simplificăm metoda switchActivePlayerTeam deoarece nu mai avem a doua echipă
    private void switchActivePlayerTeam() {
        // Update isPlayerTurn flag to switch active player
        isPlayerTurn = !isPlayerTurn;
        Log.d(TAG, "Active player switched, isPlayerTurn = " + isPlayerTurn);
    }

    // --- Player Actions ---

    public void handleShout() {
        if (currentState == playerTurnStart) {
            Log.d(TAG, "Player Shout button pressed");
            soundController.playSound(SoundController.SoundType.SHOUT);
            soundController.vibrate(SoundController.VibrationPattern.BUTTON_PRESS);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                View shoutButton = uiController.getShoutButton();
                if (shoutButton != null) {
                    shoutButton.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                }
            }
            
            transitionToState(playerShouted);
        } else {
            Log.w(TAG, "Shout ignored, current state: " + currentState.getName());
        }
    }

    public void handleAnswer() {
        if (currentState == enemyShouted) {
            Log.d(TAG, "Player Answer button pressed");
            soundController.playSound(SoundController.SoundType.ANSWER);
            uiController.showGameMessage(R.string.player_response);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                View answerButton = uiController.getAnswerButton();
                if (answerButton != null) {
                    answerButton.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                }
            }
            
            transitionToState(enemySelectingPlayer);
        } else {
            Log.w(TAG, "Answer ignored, current state: " + currentState.getName());
        }
    }

    private void simulateEnemyAnswer() {
        // 80% chance to answer (can be adjusted for difficulty)
        boolean enemyAnswers = random.nextFloat() < 0.8f;
        
        if (enemyAnswers) {
            Log.d(TAG, "Enemy answered");
            soundController.playSound(SoundController.SoundType.ANSWER);
            transitionToState(playerSelectingEnemy);
        } else {
            Log.d(TAG, "Enemy failed to answer");
            uiController.showGameMessage(R.string.enemy_failed_response);
            
            // End round if enemy doesn't answer
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                transitionToState(roundOver);
            }, 1500);
        }
    }

    public void enemyAvatarSelected(View v) {
        if (currentState != playerSelectingEnemy) {
            Log.w(TAG, "Enemy avatar selection ignored, wrong state: " + currentState.getName());
            return;
        }
        
        // Get the index of the selected avatar
        int index = -1;
        if (v.getTag() instanceof Integer) {
            index = (Integer) v.getTag();
        }
        
        Log.d(TAG, "Enemy avatar selected with index: " + index);
        
        if (index >= 0 && index < enemyTeam.getSoldierCount()) {
            // Get the selected soldier
            List<Soldier> enemySoldiers = new ArrayList<>(enemyTeam.getSoldiers());
            if (!enemySoldiers.isEmpty() && index < enemySoldiers.size()) {
                currentTargetSoldier = enemySoldiers.get(index);
                
                // Highlight the selected avatar
                uiController.highlightAvatar(index, true);
                
                // Play selection sound and vibration
                soundController.playSound(SoundController.SoundType.SELECT);
                soundController.vibrate(SoundController.VibrationPattern.SELECT);
                
                // Transition to chase state after a short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    transitionToState(playerChasing);
                }, 500);
            } else {
                Log.w(TAG, "Enemy avatar index out of bounds: " + index + " vs size " + enemySoldiers.size());
            }
        } else {
            Log.w(TAG, "Invalid enemy avatar index: " + index + " vs count " + enemyTeam.getSoldierCount());
        }
    }

    private void initiateEnemyTurn() {
        // Simulate enemy shouting after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            soundController.playSound(SoundController.SoundType.SHOUT);
            transitionToState(enemyShouted);
        }, 1500);
    }

    private void enemySelectPlayer() {
        // Enemy selects a random player soldier
        if (playerTeam.getSoldierCount() == 0) {
            // No player soldiers left to select
            transitionToState(gameOver);
            return;
        }
        
        // Select a random soldier
        List<Soldier> playerSoldiers = new ArrayList<>(playerTeam.getSoldiers());
        int randomIndex = random.nextInt(playerSoldiers.size());
        currentTargetSoldier = playerSoldiers.get(randomIndex);
        
        // Highlight the selected avatar in the UI
        uiController.highlightPlayerAvatar(randomIndex, true);
        
        // Play selection sound
        soundController.playSound(SoundController.SoundType.SELECT);
        
        // Show selection message
        String targetTeamName = context.getString(R.string.player_team_1);
            
        uiController.showGameMessage(context.getString(R.string.enemy_selected_soldier, targetTeamName));
        
        // Transition to chase state after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            transitionToState(enemyChasing);
        }, 1500);
    }

    /**
     * Activează mecanica de mini-joc pentru "spargerea zidului"
     * Aceasta va fi fie controlată de jucător, fie simulată de AI
     */
    public void activateChaseMechanic() {
        Log.d(TAG, "Activating chase mechanic");
        isActionActive = true;
        actionStartTime = System.currentTimeMillis();
        
        boolean isPlayerChasing = currentState == playerChasing;
        float difficulty = calculateChaseDifficulty();
        
        if (isPlayerChasing) {
            // Ensure UI for player chasing is shown
            uiController.showChaseElements(difficulty, this::reportChaseOutcome);
            soundController.playSound(SoundController.SoundType.CHASE);
        } else {
            // Enemy chasing logic
            uiController.showChaseUI(false, currentTargetSoldier);
            
            // Simulate enemy chase result after a random delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Calculate chance of success based on difficulty
                boolean enemySuccess = random.nextFloat() < (0.5f + difficulty * 0.3f);
                endChase(enemySuccess);
            }, 2000 + random.nextInt(1000)); // Random delay between 2-3 seconds
        }
        
        // Start chase timeout
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isActionActive) {
                // Chase timed out
                Log.d(TAG, "Chase timeout");
                endChase(false);
            }
        }, CHASE_DURATION);
    }
    
    /**
     * Calculează dificultatea mini-jocului bazată pe starea echipelor
     * @return Valoare între 0 (ușor) și 1 (dificil)
     */
    private float calculateChaseDifficulty() {
        // Bază: dificultate medie
        float baseDifficulty = 0.5f;
        
        // În funcție de moralul echipelor
        Team chasingTeam = isPlayerTurn ? playerTeam : enemyTeam;
        Team targetTeam = isPlayerTurn ? enemyTeam : playerTeam;
        
        // Moralul echipei influențează dificultatea
        // Moral ridicat = mai ușor pentru echipa respectivă
        if (isPlayerTurn) {
            // Pentru jucător, dificultatea scade cu moralul mare
            return baseDifficulty * (1 - (chasingTeam.getMorale() / 200f)) + (targetTeam.getMorale() / 200f);
        } else {
            // Pentru AI, dificultatea crește cu moralul mare
            return baseDifficulty * (1 + (chasingTeam.getMorale() / 200f)) - (targetTeam.getMorale() / 200f);
        }
    }

    /**
     * Primește rezultatul mini-jocului și procesează consecințele
     */
    public void reportChaseOutcome(boolean successful) {
        Log.d(TAG, "Chase outcome: " + (successful ? "SUCCESS" : "FAILURE"));
        
        // Marcăm rezultatul acțiunii
        isActionSuccessful = successful;
        isActionActive = false;
        
        // Notificăm UI-ul să ascundă elementele mini-jocului
        uiController.hideChaseUI();
        
        // Gestionăm rezultatul și efectele acestuia
        endChase(successful);
    }
    
    /**
     * Finalizează faza de chase și aplică rezultatele
     */
    private void endChase(boolean successful) {
        // Prevent double execution
        if (!isActionActive) return;
        isActionActive = false;
        
        // Determine which team is chasing and which is the target
        boolean isPlayerChasing = currentState == playerChasing;
        Team chasingTeam = isPlayerChasing ? playerTeam : enemyTeam;
        Team targetTeam = isPlayerChasing ? enemyTeam : playerTeam;
        
        Log.d(TAG, "Chase ended. Player chasing: " + isPlayerChasing + ", Result: " + (successful ? "success" : "failure"));
        
        // UI feedback
        String outcomeMessage = getChaseOutcomeMessage(isPlayerChasing, successful);
        uiController.showGameMessage(outcomeMessage);
        
        // Play sound effect based on outcome
        if (successful) {
            soundController.playSound(SoundController.SoundType.SUCCESS);
        } else {
            soundController.playSound(SoundController.SoundType.FAILURE);
        }
        
        // Update team states based on outcome
        updateTeamStates(chasingTeam, targetTeam, successful);
        
        // Hide chase UI
        uiController.hideChaseUI();
        
        // Complete the round and check for game over
        if (checkGameOver()) {
            transitionToState(gameOver);
        } else {
            transitionToState(roundOver);
        }
    }

    /**
     * Obține mesajul pentru rezultatul mini-jocului de chase
     */
    private String getChaseOutcomeMessage(boolean isPlayerChasing, boolean successful) {
        if (isPlayerChasing) {
            return successful ? 
                context.getString(R.string.player_caught_enemy) : 
                context.getString(R.string.player_failed_catch);
        } else {
            return successful ? 
                context.getString(R.string.enemy_caught_player) : 
                context.getString(R.string.enemy_failed_catch);
        }
    }

    private int getTotalPlayerSoldierCount() {
        return playerTeam != null ? playerTeam.getSoldierCount() : 0;
    }

    private void updateTeamStates(Team chasingTeam, Team targetTeam, boolean chaseSuccessful) {
        if (currentTargetSoldier == null) {
            Log.e(TAG, "No target soldier selected for state update");
            return;
        }
        
        if (chaseSuccessful) {
            // If chase successful, the target loses a soldier
            if (currentTargetSoldier.getTeam() == targetTeam) {
                targetTeam.removeSoldier(currentTargetSoldier);
                
                // Update UI
                uiController.updateTeamCounts(
                    playerTeam.getSoldierCount(),
                    enemyTeam.getSoldierCount()
                );
                
                // Animation effect for removed soldier
                uiController.animateSoldierRemoval(currentTargetSoldier);
            }
        } else {
            // If chase failed, morale penalties could be applied
            // For example, reduce morale of the chasing team
            chasingTeam.applyMoralePenalty(10);
            
            // Update UI to show morale changes
            uiController.updateTeamMorale(
                playerTeam.getMorale(),
                enemyTeam.getMorale(),
                -10 // moraleChange negative pentru penalizare
            );
        }
        
        // Reset the current target
        currentTargetSoldier = null;
        
        // Update the avatars display to match current counts
        uiController.updateAvatarViews(
            getTotalPlayerSoldierCount(),
            enemyTeam.getSoldierCount()
        );
    }

    private boolean checkGameOver() {
        if (getTotalPlayerSoldierCount() == 0 || enemyTeam.getSoldierCount() == 0) {
            return true;
        }
        return false;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public Team getPlayerTeam() {
        return playerTeam;
    }

    public Team getEnemyTeam() {
        return enemyTeam;
    }

    public Team getActivePlayerTeam() {
        return playerTeam;  // Întotdeauna playerTeam este activ
    }
}


