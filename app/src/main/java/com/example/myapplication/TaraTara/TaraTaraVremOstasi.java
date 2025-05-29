package com.example.myapplication.TaraTara;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main activity for the "Țară, Țară, Vrem Ostași!" traditional Romanian children's game.
 * This activity manages the gameplay, UI state, and player interactions.
 */
public class TaraTaraVremOstasi extends Activity {
    private static final String TAG = "TaraTaraVremOstasi";
    private static final int INITIAL_SOLDIER_COUNT = 5;
    private static final int GAME_START_DELAY = 500; // ms
    private static final int TUTORIAL_DELAY = 2000; // ms
    
    // Game components
    private TaraMinigameGameView gameView;
    private GameController gameController;
    private GameUIController uiController;
    private SoundController soundController;
    
    // Teams
    private Team playerTeam;
    private Team enemyTeam;
    
    // Game state
    private boolean gameInProgress = false;
    private boolean resourcesInitialized = false;
    
    // Background task executor
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // UI components
    private TextView roundCounterText;
    private Button restartButton;
    private FrameLayout gameViewContainer;
    private LinearLayout avatarLinesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tara_tara_vrem_ostasi);
        Log.d(TAG, "onCreate started");
        
        try {
            // Initialize UI references
            initializeUIComponents();
            
            // Initialize controllers
            initializeControllers();
            
            // Ensure vital UI containers are visible
            if (gameViewContainer != null) {
                gameViewContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "Game view container is now visible");
            }
            
            if (avatarLinesContainer != null) {
                avatarLinesContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "Avatar lines container is now visible");
            }
            
            // Initialize teams and game state in background
            backgroundExecutor.execute(this::initializeGameResourcesAsync);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "A apărut o eroare la inițializarea jocului", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Initialize UI component references
     */
    private void initializeUIComponents() {
        roundCounterText = findViewById(R.id.roundCounter);
        restartButton = findViewById(R.id.restartButton);
        gameViewContainer = findViewById(R.id.gameViewContainer);
        avatarLinesContainer = findViewById(R.id.avatarLinesContainer);
        
        if (roundCounterText == null) Log.e(TAG, "roundCounterText not found");
        if (restartButton == null) Log.e(TAG, "restartButton not found");
        if (gameViewContainer == null) Log.e(TAG, "gameViewContainer not found");
        if (avatarLinesContainer == null) Log.e(TAG, "avatarLinesContainer not found");
    }
    
    /**
     * Initialize controllers needed for the game
     */
    private void initializeControllers() {
        try {
            // Create UI Controller first
            View rootView = findViewById(android.R.id.content);
            uiController = new GameUIController(this, rootView);
            
            // Create Sound Controller
            soundController = new SoundController(this);
            
            // Create Game Controller (requires the other controllers)
            gameController = new GameController(this, uiController, soundController);
            
            // Set up game view container
            setupGameView();
            
            // Set up button listeners after creating controllers
            setupButtonListeners();
            
            Log.d(TAG, "Controllers initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing controllers", e);
            throw e; // Rethrow to be caught by the outer try-catch
        }
    }
    
    /**
     * Initialize game resources asynchronously
     */
    private void initializeGameResourcesAsync() {
        try {
            // Load drawable resources
            Drawable soldierDrawable = getResources().getDrawable(R.drawable.ic_player_character1);
            
            // Initialize teams with proper colors and names
            playerTeam = new Team(
                    "player_team_1",
                    getString(R.string.player_team_1),
                    getResources().getColor(R.color.teamBlue),
                    true,
                    soldierDrawable
            );

            enemyTeam = new Team(
                    "enemy_team",
                    getString(R.string.enemy_team),
                    getResources().getColor(R.color.teamRed),
                    false,
                    soldierDrawable
            );

            Log.d(TAG, "Teams created successfully");
            
            // Back to main thread to finish initialization
            mainHandler.post(() -> {
                resourcesInitialized = true;
                
                // Set up game listeners
                setupGameStateListeners();
                
                // Update UI with initial team counts
                updateTeamCountsUI();
                
                // Play welcome sound
                soundController.playSound(SoundController.SoundType.SHOUT);
                
                // Start the game after a delay
                mainHandler.postDelayed(this::startGame, GAME_START_DELAY);
                
                Log.d(TAG, "Game resources initialized and game start scheduled");
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing game resources", e);
            mainHandler.post(() -> {
                Toast.makeText(this, "Eroare la încărcarea resurselor de joc", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    /**
     * Update UI with current team counts and ensure UI elements are visible
     */
    private void updateTeamCountsUI() {
        // Make sure the avatar container is visible
        if (avatarLinesContainer != null) {
            avatarLinesContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "Avatar lines container set to visible");
        }
        
        // Make sure the game view container is visible
        if (gameViewContainer != null) {
            gameViewContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "Game view container set to visible");
        }
    
        // Update UI with team counts
        if (uiController != null) {
            uiController.updateAvatarViews(
                playerTeam != null ? playerTeam.getSoldierCount() : 0, 
                enemyTeam != null ? enemyTeam.getSoldierCount() : 0
            );
            
            // Also make sure the UI controller updates the full team UI
            uiController.updateTeamUI(playerTeam, enemyTeam, true);
        } else {
            Log.e(TAG, "Cannot update team counts, UI controller is null");
        }
    }
    
    /**
     * Setup the game view for rendering the game field
     */
    private void setupGameView() {
        // Create game view
        gameView = new TaraMinigameGameView(this);
        
        // Add to container
        if (gameViewContainer != null) {
            gameViewContainer.removeAllViews(); // Clear any existing views
            gameViewContainer.addView(gameView);
            
            // Ensure the container is visible
            gameViewContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "Game view added to container");
        } else {
            Log.e(TAG, "Game container not found!");
            throw new IllegalStateException("Game container view not found");
        }
    }

    /**
     * Setup button click listeners
     */
    private void setupButtonListeners() {
        try {
            // Shout button
            Button shoutButton = uiController.getShoutButton();
            if (shoutButton != null) {
                shoutButton.setOnClickListener(v -> {
                    Log.d(TAG, "Shout button clicked");
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
                    gameController.handleShout();
                });
            }

            // Answer button
            Button answerButton = uiController.getAnswerButton();
            if (answerButton != null) {
                answerButton.setOnClickListener(v -> {
                    Log.d(TAG, "Answer button clicked");
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
                    gameController.handleAnswer();
                });
            }

            // Restart button
            if (restartButton != null) {
                restartButton.setOnClickListener(v -> {
                    Log.d(TAG, "Restart button clicked");
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
                    startGame();
                });
            }
            
            Log.d(TAG, "Button listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up button listeners", e);
            throw e;
        }
    }

    /**
     * Setup game state change listeners
     */
    private void setupGameStateListeners() {
        if (gameView == null) {
            Log.e(TAG, "Game view is null when setting up listeners");
            return;
        }
        
        gameView.setOnTeamUpdateListener(new TaraMinigameGameView.OnTeamUpdateListener() {
            @Override
            public void onTeamUpdate(Team team) {
                // Update UI when team changes
                mainHandler.post(() -> {
                    if (uiController != null && playerTeam != null && enemyTeam != null) {
                        uiController.updateTeamUI(playerTeam, enemyTeam, true);
                    }
                });
            }

            @Override
            public void onGameOver(boolean playerWon) {
                // Show game over UI on the main thread
                mainHandler.post(() -> {
                    if (uiController != null) {
                        uiController.showGameOver(playerWon);
                    }
                    gameInProgress = false;
                });
            }
        });
        
        Log.d(TAG, "Game state listeners set up");
    }

    /**
     * Start or restart the game
     */
    private void startGame() {
        if (!resourcesInitialized) {
            Log.w(TAG, "Attempting to start game before resources are initialized");
            Toast.makeText(this, "Se încarcă resursele jocului...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Starting new game");
        gameInProgress = true;

        // Reset UI elements and show tutorial
        if (uiController != null) {
            uiController.updateTeamUI(playerTeam, enemyTeam, true);
            uiController.showGameMessage(R.string.tutorial_step1);
        }
        
        // Show second tutorial step after delay
        mainHandler.postDelayed(() -> {
            if (isFinishing()) return;
            if (uiController != null) {
                uiController.showGameMessage(R.string.tutorial_step2);
            }
        }, TUTORIAL_DELAY);

        // Reset round counter
        updateRoundCounter(0);
        
        // Reset teams to initial state
        resetTeams();

        // Add a delay to ensure the view is fully laid out before starting
        mainHandler.postDelayed(() -> {
            if (isFinishing()) return;
            
            // Start the game in GameView
            if (gameView != null) {
                Log.d(TAG, "Starting game in GameView");
                gameView.startGame(playerTeam, enemyTeam);
                
                // Add another delay before starting the game controller
                mainHandler.postDelayed(() -> {
                    if (isFinishing()) return;
                    
                    // Start the game in GameController
                    if (gameController != null) {
                        Log.d(TAG, "Starting game in GameController");
                        gameController.startGame(playerTeam, enemyTeam);
                        Log.d(TAG, "Game started successfully");
                    } else {
                        Log.e(TAG, "GameController is null, cannot start game");
                    }
                }, 500);
            } else {
                Log.e(TAG, "GameView is null, cannot start game");
                
                // Try to recreate the game view
                setupGameView();
                
                if (gameView != null) {
                    setupGameStateListeners();
                    mainHandler.postDelayed(this::startGame, 500);
                }
            }
        }, 1000);
    }

    /**
     * Reset teams to initial state
     */
    private void resetTeams() {
        try {
            // Reset team counts to initial values
            int initialSoldiers = INITIAL_SOLDIER_COUNT;
            
            // Ensure game view dimensions are available
            int width = 0;
            int height = 0;
            
            if (gameView != null) {
                width = gameView.getWidth();
                height = gameView.getHeight();
            }
            
            // If dimensions are not available yet, use screen dimensions
            if (width <= 0 || height <= 0) {
                width = getResources().getDisplayMetrics().widthPixels;
                height = getResources().getDisplayMetrics().heightPixels;
            }
            
            // Initialize teams with proper dimensions
            if (playerTeam != null) {
                playerTeam.initializeTeam(width, height, initialSoldiers);
            }
            
            if (enemyTeam != null) {
                enemyTeam.initializeTeam(width, height, initialSoldiers);
            }
            
            // Update UI with initial team counts
            if (uiController != null) {
                uiController.updateTeamCounts(
                    playerTeam != null ? playerTeam.getSoldierCount() : 0,
                    enemyTeam != null ? enemyTeam.getSoldierCount() : 0
                );
            }
            
            Log.d(TAG, "Teams reset to initial state with dimensions: " + width + "x" + height);
        } catch (Exception e) {
            Log.e(TAG, "Error resetting teams", e);
        }
    }
    
    /**
     * Update the round counter in the UI
     */
    private void updateRoundCounter(int round) {
        if (roundCounterText != null) {
            roundCounterText.setText(getString(R.string.round_counter_format, round));
        }
        
        if (uiController != null) {
            uiController.updateRoundCounter(round);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause game components
        if (gameView != null) {
            gameView.pause();
        }
        
        if (soundController != null) {
            soundController.pauseSounds();
        }
        
        Log.d(TAG, "Activity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume game components
        if (gameView != null) {
            gameView.resume();
        }
        
        if (soundController != null) {
            soundController.resumeSounds();
        }
        
        Log.d(TAG, "Activity resumed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources
        if (soundController != null) {
            soundController.release();
        }
        
        // Cancel any pending handlers
        mainHandler.removeCallbacksAndMessages(null);
        
        Log.d(TAG, "Activity destroyed");
    }
    
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save game state if needed
        outState.putBoolean("gameInProgress", gameInProgress);
        Log.d(TAG, "Game state saved");
    }
    
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore game state if needed
        gameInProgress = savedInstanceState.getBoolean("gameInProgress", false);
        Log.d(TAG, "Game state restored");
    }
}
