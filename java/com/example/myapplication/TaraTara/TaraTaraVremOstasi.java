package com.example.myapplication.TaraTara;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.example.myapplication.R;

public class TaraTaraVremOstasi extends Activity {
    private TaraMinigameGameView gameView;
    private GameController gameController;
    private GameUIController uiController;
    private SoundController soundController;
    private Team playerTeam;
    private Team playerTeam2;
    private Team enemyTeam;
    private boolean gameInProgress = false;
    private static final String TAG = "TaraTaraVremOstasi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tara_tara_vrem_ostasi);

        Log.d(TAG, "onCreate started");

        try {
            // Initialize controllers
            uiController = new GameUIController(this, findViewById(android.R.id.content));
            soundController = new SoundController(this);
            gameController = new GameController(this, uiController, soundController);

            // Set up game view
            setupGameView();
            Log.d(TAG, "Game view set up");

            // Initialize teams
            initializeTeams();
            Log.d(TAG, "Teams initialized");

            // Set up button listeners
            setupButtonListeners();
            Log.d(TAG, "Button listeners set up");

            // Set up game state listeners
            setupGameStateListeners();
            Log.d(TAG, "Game state listeners set up");

            // Play welcome sound
            soundController.playSound(SoundController.SoundType.SHOUT);

            // Start the game after a short delay to ensure view is ready
            new Handler(Looper.getMainLooper()).postDelayed(this::startGame, 500);
            Log.d(TAG, "Game start scheduled");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }
    
    private void setupGameView() {
        // Create game view
        gameView = new TaraMinigameGameView(this);
        
        // Add to container
        FrameLayout container = findViewById(R.id.gameViewContainer);
        if (container != null) {
            container.removeAllViews(); // Clear any existing views
            container.addView(gameView);
            Log.d(TAG, "Game view added to container");
        } else {
            Log.e(TAG, "Game container not found!");
        }
    }

    private void initializeTeams() {
        // Initialize teams for two players
        try {
            Drawable soldierDrawable = getResources().getDrawable(R.drawable.ic_player_character1);

            playerTeam = new Team(
                    "player_team_1",
                    getString(R.string.player_team_1),
                    getResources().getColor(R.color.teamBlue),
                    true,
                    soldierDrawable
            );

            playerTeam2 = new Team(
                    "player_team_2",
                    getString(R.string.player_team_2),
                    getResources().getColor(R.color.teamGreen),
                    true,
                    soldierDrawable
            );

            enemyTeam = new Team("enemy_team",
                    getString(R.string.enemy_team),
                    getResources().getColor(R.color.teamRed),
                    false,
                    soldierDrawable);

            Log.d(TAG, "Teams created successfully");
            
            // Update UI with initial team counts
            uiController.updateAvatarViews(
                (playerTeam != null ? playerTeam.getSoldierCount() : 0) + 
                (playerTeam2 != null ? playerTeam2.getSoldierCount() : 0), 
                enemyTeam != null ? enemyTeam.getSoldierCount() : 0
            );
        } catch (Exception e) {
            Log.e(TAG, "Error initializing teams", e);
        }
    }

    private void setupButtonListeners() {
        try {
            View.OnClickListener shoutListener = v -> {
                Log.d(TAG, "Shout button clicked");
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
                gameController.handleShout();
            };
            uiController.getShoutButton().setOnClickListener(shoutListener);

            View.OnClickListener answerListener = v -> {
                Log.d(TAG, "Answer button clicked");
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
                gameController.handleAnswer();
            };
            uiController.getAnswerButton().setOnClickListener(answerListener);

            View.OnClickListener restartListener = v -> {
                Log.d(TAG, "Restart button clicked");
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
                startGame();
            };
            findViewById(R.id.restartButton).setOnClickListener(restartListener);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up button listeners", e);
        }
    }

    private void setupGameStateListeners() {
        if (gameView != null) {
            gameView.setOnTeamUpdateListener(new TaraMinigameGameView.OnTeamUpdateListener() {
                @Override
                public void onTeamUpdate(Team team) {
                    uiController.updateTeamUI(playerTeam, enemyTeam, true);
                }

                @Override
                public void onGameOver(boolean playerWon) {
                    uiController.showGameOver(playerWon);
                }
            });
        } else {
            Log.e(TAG, "Game view is null when setting up listeners");
        }
    }

    private void startGame() {
        Log.d(TAG, "Starting new game");

        gameInProgress = true;

        // Reset UI elements and show tutorial
        uiController.updateTeamUI(playerTeam, enemyTeam, false);
        uiController.showGameMessage(R.string.tutorial_step1);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            uiController.showGameMessage(R.string.tutorial_step2);
        }, 2000);

        // Make sure teams are properly initialized before starting
        if (playerTeam == null || enemyTeam == null) {
            Log.e(TAG, "Teams not initialized, recreating them");
            initializeTeams();
        }
        
        // Reset team counts to initial values
        resetTeams();

        // Add a delay to ensure the view is fully laid out before starting
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start the game in GameView
            if (gameView != null) {
                Log.d(TAG, "Starting game in GameView");
                gameView.startGame(playerTeam, enemyTeam, playerTeam2);
                
                // Add another delay before starting the game controller to ensure view is ready
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Start the game in GameController with both player teams
                    Log.d(TAG, "Starting game in GameController");
                    gameController.startGame(playerTeam, playerTeam2, enemyTeam);
                    
                    Log.d(TAG, "Game started successfully");
                }, 500);
            } else {
                Log.e(TAG, "GameView is null, cannot start game");
                
                // Try to recreate the game view
                setupGameView();
                
                if (gameView != null) {
                    setupGameStateListeners();
                    new Handler(Looper.getMainLooper()).postDelayed(this::startGame, 500);
                }
            }
        }, 1000);
    }

    private void resetTeams() {
        try {
            // Reset team counts to initial values (5 soldiers each)
            int initialSoldiers = 5;
            
            if (playerTeam != null) {
                playerTeam.initializeTeam(gameView.getWidth(), gameView.getHeight(), initialSoldiers);
            }
            
            if (playerTeam2 != null) {
                playerTeam2.initializeTeam(gameView.getWidth(), gameView.getHeight(), initialSoldiers);
            }
            
            if (enemyTeam != null) {
                enemyTeam.initializeTeam(gameView.getWidth(), gameView.getHeight(), initialSoldiers);
            }
            
            // Update UI with initial team counts
            uiController.updateTeamCounts(
                playerTeam != null ? playerTeam.getSoldierCount() : 0,
                playerTeam2 != null ? playerTeam2.getSoldierCount() : 0,
                enemyTeam != null ? enemyTeam.getSoldierCount() : 0
            );
            
            Log.d(TAG, "Teams reset to initial state");
        } catch (Exception e) {
            Log.e(TAG, "Error resetting teams", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundController != null) {
            soundController.release();
        }
    }
}
