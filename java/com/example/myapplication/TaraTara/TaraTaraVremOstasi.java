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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tara_tara_vrem_ostasi);

        Log.d("TaraTaraVremOstasi", "onCreate started");

        try {
            // Initialize controllers
            uiController = new GameUIController(this, findViewById(android.R.id.content));
            soundController = new SoundController(this);
            gameController = new GameController(this, uiController, soundController);

            // Set up game view
            gameView = new TaraMinigameGameView(this);
            FrameLayout container = findViewById(R.id.gameViewContainer);
            container.addView(gameView);
            Log.d("TaraTaraVremOstasi", "Game view set up");

            // Initialize teams
            initializeTeams();
            Log.d("TaraTaraVremOstasi", "Teams initialized");

            // Set up button listeners
            setupButtonListeners();
            Log.d("TaraTaraVremOstasi", "Button listeners set up");

            // Set up game state listeners
            setupGameStateListeners();
            Log.d("TaraTaraVremOstasi", "Game state listeners set up");

            // Play welcome sound
            soundController.playSound(SoundController.SoundType.SHOUT);

            // Start the game
            startGame();
            Log.d("TaraTaraVremOstasi", "Game started");

        } catch (Exception e) {
            Log.e("TaraTaraVremOstasi", "Error in onCreate", e);
        }
    }

    private void initializeTeams() {
        // Initialize teams for two players
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

        // Add the teams to the game
        gameController.setTeams(playerTeam, enemyTeam, playerTeam2);
        uiController.updateAvatarViews(playerTeam.getSoldierCount() + playerTeam2.getSoldierCount(), enemyTeam.getSoldierCount());
    }

    private void setupButtonListeners() {
        View.OnClickListener shoutListener = v -> {
            Log.d("TaraTaraVremOstasi", "Shout button clicked");
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
            gameController.handleShout();
        };
        uiController.getShoutButton().setOnClickListener(shoutListener);

        View.OnClickListener answerListener = v -> {
            Log.d("TaraTaraVremOstasi", "Answer button clicked");
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
            gameController.handleAnswer();
        };
        uiController.getAnswerButton().setOnClickListener(answerListener);

        View.OnClickListener restartListener = v -> {
            Log.d("TaraTaraVremOstasi", "Restart button clicked");
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
            startGame();
        };
        findViewById(R.id.restartButton).setOnClickListener(restartListener);
    }

    private void setupGameStateListeners() {
        if (gameView != null) {
            gameView.setOnTeamUpdateListener(new TaraMinigameGameView.OnTeamUpdateListener() {
                @Override
                public void onTeamUpdate(Team team) {
                    uiController.updateTeamUI(playerTeam, enemyTeam, playerTeam2, gameController.isFleeingMode());
                }

                @Override
                public void onGameOver(boolean playerWon) {
                    uiController.showGameOver(playerWon);
                }
            });
        }
    }

    private void startGame() {
        Log.d("TaraTaraVremOstasi", "Starting new game");

        gameInProgress = true;

        // Initialize teams with soldiers (5v5)
        float screenWidth = gameView.getWidth();
        float screenHeight = gameView.getHeight();

        int initialSoldiers = 5;
        playerTeam.initializeTeam(screenWidth, screenHeight, initialSoldiers);
        enemyTeam.initializeTeam(screenWidth, screenHeight, initialSoldiers);

        // Reset UI elements and show tutorial
        uiController.updateTeamUI(playerTeam, enemyTeam, null, false);
        uiController.showGameMessage(R.string.tutorial_step1);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            uiController.showGameMessage(R.string.tutorial_step2);
        }, 2000);

        // Start the game in GameView
        if (gameView != null) {
            gameView.startGame(playerTeam, enemyTeam, playerTeam2);
            Log.d("TaraTaraVremOstasi", "Game started successfully");
        } else {
            Log.e("TaraTaraVremOstasi", "GameView is null");
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
        soundController.release();
    }
}
