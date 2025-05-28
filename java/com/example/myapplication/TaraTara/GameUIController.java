package com.example.myapplication.TaraTara;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.HapticFeedbackConstants;
import android.animation.ObjectAnimator;
import androidx.core.view.GestureDetectorCompat;
import android.util.Log;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;
import android.graphics.drawable.AnimationDrawable;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class GameUIController {
    private final Context context;
    private final TextView playerSoldierCount;
    private final TextView playerMorale;
    private final TextView enemySoldierCount;
    private final TextView enemyMorale;
    private final ProgressBar playerMoraleBar;
    private final ProgressBar enemyMoraleBar;
    private final Button shoutButton;
    private final Button answerButton;
    private final LinearLayout blueTeamAvatars;
    private final LinearLayout redTeamAvatars;
    private final View breakZoneIndicator;
    private final RelativeLayout gameOverlay;
    private final TextView overlayMessage;
    private final TextView gameMessageText;
    private final Button restartButton;
    private final Animation fadeIn;
    private final Animation fadeOut;
    private final Animation slideUp;
    private final Animation scaleIn;
    private final Animation pulse;

    private Team currentPlayerTeam;
    private Team currentEnemyTeam;

    private ViewGroup swipeChaseContainer;
    private ImageView swipeArrowIndicator;

    private GestureDetectorCompat gestureDetector;
    private List<SwipeDirection> targetSwipePattern;
    private List<SwipeDirection> currentSwipePattern;
    private Consumer<Boolean> reportOutcomeCallback;

    private enum SwipeDirection { UP, DOWN, LEFT, RIGHT }

    private final Random random = new Random();
    private static final int MAX_AVATARS = 10; // Maximum number of avatars to display

    private WallBreakMinigame wallBreakMinigame;
    private TextView gameInstructionText;
    private AnimationDrawable targetedEffect;

    public GameUIController(Context context, View rootView) {
        this.context = context;

        playerSoldierCount = rootView.findViewById(R.id.playerSoldierCount);
        playerMorale = rootView.findViewById(R.id.playerMorale);
        enemySoldierCount = rootView.findViewById(R.id.enemySoldierCount);
        enemyMorale = rootView.findViewById(R.id.enemyMorale);
        playerMoraleBar = rootView.findViewById(R.id.playerMoraleBar);
        enemyMoraleBar = rootView.findViewById(R.id.enemyMoraleBar);
        shoutButton = rootView.findViewById(R.id.shoutButton);
        answerButton = rootView.findViewById(R.id.answerButton);
        blueTeamAvatars = rootView.findViewById(R.id.blueTeamAvatars);
        redTeamAvatars = rootView.findViewById(R.id.redTeamAvatars);
        breakZoneIndicator = rootView.findViewById(R.id.breakZoneIndicator);
        gameOverlay = rootView.findViewById(R.id.gameOverlay);
        overlayMessage = rootView.findViewById(R.id.overlayMessage);
        gameMessageText = rootView.findViewById(R.id.gameMessageText);
        restartButton = rootView.findViewById(R.id.restartButton);

        swipeChaseContainer = rootView.findViewById(R.id.swipeChaseContainer);
        swipeArrowIndicator = rootView.findViewById(R.id.swipeArrowIndicator);
        if (swipeChaseContainer == null) {
            android.util.Log.w("GameUIController", "Swipe Chase Container not found! Ensure R.id.swipeChaseContainer exists in the layout.");
        }
        if (swipeArrowIndicator == null) {
            android.util.Log.w("GameUIController", "Swipe Arrow Indicator not found! Ensure R.id.swipeArrowIndicator exists in the layout.");
        }
        
        // Initialize or find the WallBreakMinigame component
        wallBreakMinigame = rootView.findViewById(R.id.wallBreakMinigame);
        if (wallBreakMinigame == null) {
            // Create and add the wall break minigame dynamically if not found in layout
            Log.d("GameUIController", "Creating WallBreakMinigame dynamically");
            wallBreakMinigame = new WallBreakMinigame(context);
            FrameLayout minigameContainer = rootView.findViewById(R.id.minigameContainer);
            if (minigameContainer == null) {
                // If the container doesn't exist, try to add to the main content
                ViewGroup root = rootView.findViewById(android.R.id.content);
                if (root == null) {
                    root = (ViewGroup) rootView;
                }
                // Create a container if needed
                minigameContainer = new FrameLayout(context);
                minigameContainer.setId(R.id.minigameContainer);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);
                root.addView(minigameContainer, params);
            }
            // Add the WallBreakMinigame to the container
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            minigameContainer.addView(wallBreakMinigame, params);
        } else {
            Log.d("GameUIController", "Found WallBreakMinigame in layout");
        }

        gameInstructionText = rootView.findViewById(R.id.gameInstructionText);
        if (gameInstructionText == null) {
            Log.w("GameUIController", "Game instruction text view not found");
        }

        fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        scaleIn = AnimationUtils.loadAnimation(context, R.anim.scale_in);
        pulse = AnimationUtils.loadAnimation(context, R.anim.pulse);

        gestureDetector = new GestureDetectorCompat(context, new SwipeGestureListener());
        targetSwipePattern = new ArrayList<>();
        currentSwipePattern = new ArrayList<>();
    }

    public void updateTeamUI(Team playerTeam, Team enemyTeam, boolean isPlayerTurn) {
        this.currentPlayerTeam = playerTeam;
        this.currentEnemyTeam = enemyTeam;

        if (playerTeam == null || enemyTeam == null) {
            android.util.Log.w("GameUIController", "updateTeamUI called with null teams.");
            return;
        }

        View playerStatusView = playerSoldierCount.getRootView().findViewById(R.id.playerTeamStatus);
        View enemyStatusView = enemySoldierCount.getRootView().findViewById(R.id.enemyTeamStatus);

        animateTextChange(playerSoldierCount, context.getString(R.string.soldiers_count, playerTeam.getSoldierCount()));
        animateTextChange(playerMorale, context.getString(R.string.morale_level, (int)playerTeam.getMorale()));
        animateProgressBar(playerMoraleBar, (int)playerTeam.getMorale());
        
        // Make sure avatar layout is visible and update with current count
        if (blueTeamAvatars != null) {
            blueTeamAvatars.setVisibility(View.VISIBLE);
            updateTeamAvatars(blueTeamAvatars, playerTeam.getSoldierCount(), false);
        }

        animateTextChange(enemySoldierCount, context.getString(R.string.soldiers_count, enemyTeam.getSoldierCount()));
        animateTextChange(enemyMorale, context.getString(R.string.resistance_level, (int)enemyTeam.getMorale()));
        animateProgressBar(enemyMoraleBar, (int)enemyTeam.getMorale());
        
        // Make sure avatar layout is visible and update with current count
        if (redTeamAvatars != null) {
            redTeamAvatars.setVisibility(View.VISIBLE);
            updateTeamAvatars(redTeamAvatars, enemyTeam.getSoldierCount(), false);
        }
        
        // Make sure break zone indicator is visible
        if (breakZoneIndicator != null) {
            breakZoneIndicator.setVisibility(View.VISIBLE);
        }
    }

    public void showGameOver(boolean playerWon) {
        if (currentPlayerTeam == null || currentEnemyTeam == null) return;

        gameOverlay.setAlpha(0f);
        gameOverlay.setVisibility(View.VISIBLE);

        gameOverlay.animate()
                .alpha(1f)
                .setDuration(500)
                .withEndAction(() -> {
                    overlayMessage.setText((CharSequence)(playerWon ? context.getString(R.string.victory) : context.getString(R.string.defeat)));
                    overlayMessage.setTextColor(playerWon ? Color.GREEN : Color.RED);
                    overlayMessage.startAnimation(scaleIn);

                    String finalScore = context.getString(R.string.soldiers_count,
                            playerWon ? currentPlayerTeam.getSoldierCount() : currentEnemyTeam.getSoldierCount());

                    restartButton.setEnabled(true);
                    restartButton.setVisibility(View.VISIBLE);
                    restartButton.startAnimation(slideUp);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        restartButton.performHapticFeedback(
                                playerWon ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.REJECT
                        );
                    }
                });
    }

    public void showGameMessage(int messageResId) {
        if (gameMessageText != null) {
            gameMessageText.setText(messageResId);
            gameMessageText.setVisibility(View.VISIBLE);
            gameMessageText.bringToFront();
            gameMessageText.requestLayout();
            gameMessageText.startAnimation(fadeIn);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                hideGameMessage();
            }, 2500);
        }
    }

    public void showGameMessage(String message) {
        if (gameMessageText != null) {
            gameMessageText.setText(message);
            gameMessageText.setVisibility(View.VISIBLE);
            gameMessageText.bringToFront();
            gameMessageText.requestLayout();
            gameMessageText.startAnimation(fadeIn);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                hideGameMessage();
            }, 2500);
        }
    }

    private void hideGameMessage() {
        if (gameMessageText != null && gameMessageText.getVisibility() == View.VISIBLE) {
            Animation fadeOutShort = AnimationUtils.loadAnimation(context, R.anim.fade_out);
            fadeOutShort.setDuration(500);
            fadeOutShort.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    if (gameMessageText != null) {
                        gameMessageText.setVisibility(View.GONE);
                    }
                }
            });
            gameMessageText.startAnimation(fadeOutShort);
        }
    }

    private void animateTextChange(TextView textView, String newText) {
        if (textView == null) return;
        if (textView.getText().toString().equals(newText)) {
            return;
        }
        textView.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    textView.setText(newText);
                    textView.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void animateProgressBar(ProgressBar progressBar, int newProgress) {
        if (progressBar == null) return;
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), newProgress)
                .setDuration(300)
                .start();
    }

    private void updateTeamAvatars(LinearLayout avatarContainer, int activeCount, boolean clickable) {
        if (avatarContainer == null) return;
        for (int i = 0; i < avatarContainer.getChildCount(); i++) {
            View child = avatarContainer.getChildAt(i);
            if (!(child instanceof ImageView)) continue;
            ImageView avatar = (ImageView) child;
            boolean isActive = i < activeCount;

            avatar.animate()
                    .alpha(isActive ? 1.0f : 0.3f)
                    .setDuration(300)
                    .start();

            avatar.setOnClickListener(null);
        }
    }

    public void setButtonStates(boolean shoutEnabled, boolean answerEnabled) {
        shoutButton.setEnabled(shoutEnabled);
        shoutButton.setAlpha(shoutEnabled ? 1.0f : 0.5f);
        answerButton.setEnabled(answerEnabled);
        answerButton.setAlpha(answerEnabled ? 1.0f : 0.5f);
    }

    public void enableAvatarSelection(int enemyCount, View.OnClickListener onAvatarClick) {
        if (redTeamAvatars == null) return;
        android.util.Log.d("GameUIController", "Enabling avatar selection for " + enemyCount + " enemies.");
        for (int i = 0; i < redTeamAvatars.getChildCount(); i++) {
            View child = redTeamAvatars.getChildAt(i);
            if (!(child instanceof ImageView)) continue;
            ImageView avatar = (ImageView) child;
            boolean isActive = i < enemyCount;
            avatar.setEnabled(isActive);
            avatar.setClickable(isActive);
            avatar.setFocusable(isActive);

            // Store the index in the tag so GameController knows which soldier was clicked
            avatar.setTag(i);

            if (isActive) {
                avatar.setOnClickListener(onAvatarClick);
                avatar.setBackgroundResource(R.drawable.avatar_selectable_background);
            } else {
                avatar.setOnClickListener(null);
                avatar.setBackground(null);
            }
        }
    }

    public void disableAvatarSelection() {
        if (redTeamAvatars == null) return;
        android.util.Log.d("GameUIController", "Disabling avatar selection.");
        for (int i = 0; i < redTeamAvatars.getChildCount(); i++) {
            View child = redTeamAvatars.getChildAt(i);
            if (!(child instanceof ImageView)) continue;
            ImageView avatar = (ImageView) child;
            avatar.setEnabled(false);
            avatar.setClickable(false);
            avatar.setOnClickListener(null);
            avatar.setBackground(null);
        }
    }

    public void showChaseElements(float difficulty, Consumer<Boolean> reportOutcomeCallback) {
        this.reportOutcomeCallback = reportOutcomeCallback;

        // Ensure container is visible
        if (swipeChaseContainer != null) {
            swipeChaseContainer.setVisibility(View.VISIBLE);
            swipeChaseContainer.startAnimation(fadeIn);
            
            // Set up gesture detector
            swipeChaseContainer.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return true;
            });
        }

        // Generate random swipe pattern based on difficulty
        int patternLength = 3 + Math.round(difficulty * 2); // 3-5 swipes based on difficulty
        patternLength = Math.min(patternLength, 5); // Cap at 5 swipes
        generateRandomSwipePattern(patternLength);
        
        // Reset current pattern
        currentSwipePattern.clear();
        
        // Show first arrow
        updateSwipeArrowIndicator();
        
        // Show game instructions
        if (gameInstructionText != null) {
            gameInstructionText.setText(R.string.swipe_instructions);
            gameInstructionText.setVisibility(View.VISIBLE);
            gameInstructionText.startAnimation(fadeIn);
        }
        
        // Haptic feedback
        if (swipeChaseContainer != null) {
            swipeChaseContainer.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }

    public void handleChaseEnd(boolean successful, Runnable onComplete) {
        Log.d("GameUIController", "Handling Chase End. Success: " + successful);
        if (swipeChaseContainer == null) {
            Log.e("GameUIController", "Cannot handle chase end, container is null.");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        swipeChaseContainer.setVisibility(View.GONE);
        swipeChaseContainer.setOnTouchListener(null);
        if (swipeArrowIndicator != null) {
            swipeArrowIndicator.setVisibility(View.GONE);
        }

        showGameMessage((String)(successful ? context.getString(R.string.chase_success) : context.getString(R.string.chase_failure)));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            swipeChaseContainer.performHapticFeedback(
                    successful ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.REJECT
            );
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (onComplete != null) {
                onComplete.run();
            }
        }, 1000);
    }

    private void generateRandomSwipePattern(int length) {
        targetSwipePattern.clear();
        SwipeDirection[] directions = SwipeDirection.values();
        
        for (int i = 0; i < length; i++) {
            SwipeDirection randomDirection = directions[random.nextInt(directions.length)];
            targetSwipePattern.add(randomDirection);
        }
        
        Log.d("GameUIController", "Generated swipe pattern of length: " + length);
    }

    private void updateSwipeArrowIndicator() {
        if (swipeArrowIndicator == null) return;
        
        // Get current expected direction
        int currentIndex = currentSwipePattern.size();
        if (currentIndex >= targetSwipePattern.size()) {
            // Pattern complete
            swipeArrowIndicator.setVisibility(View.INVISIBLE);
            return;
        }
        
        SwipeDirection nextDirection = targetSwipePattern.get(currentIndex);
        
        // Set arrow image based on direction
        switch (nextDirection) {
            case UP:
                swipeArrowIndicator.setRotation(270);
                break;
            case DOWN:
                swipeArrowIndicator.setRotation(90);
                break;
            case LEFT:
                swipeArrowIndicator.setRotation(180);
                break;
            case RIGHT:
                swipeArrowIndicator.setRotation(0);
                break;
        }
        
        swipeArrowIndicator.setVisibility(View.VISIBLE);
        swipeArrowIndicator.startAnimation(pulse);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        
        @Override
        public boolean onDown(MotionEvent e) {
            return true; // Required for onFling to work
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (e1 == null || e2 == null) return false;
                
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                
                // Check if the swipe was more horizontal or vertical
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // Horizontal swipe
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            handleSwipe(SwipeDirection.RIGHT);
                        } else {
                            handleSwipe(SwipeDirection.LEFT);
                        }
                        return true;
                    }
                } else {
                    // Vertical swipe
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            handleSwipe(SwipeDirection.DOWN);
                        } else {
                            handleSwipe(SwipeDirection.UP);
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e("GameUIController", "Error in swipe detection", e);
            }
            
            return false;
        }
    }
    
    private void handleSwipe(SwipeDirection swipe) {
        // Provide haptic feedback
        if (swipeChaseContainer != null) {
            swipeChaseContainer.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        
        // Get expected direction
        if (currentSwipePattern.size() >= targetSwipePattern.size()) {
            // Already completed pattern
            return;
        }
        
        SwipeDirection expectedDirection = targetSwipePattern.get(currentSwipePattern.size());
        boolean correctSwipe = (swipe == expectedDirection);
        
        // Add to current pattern regardless of correctness
        currentSwipePattern.add(swipe);
        
        if (correctSwipe) {
            // Show success feedback
            if (swipeArrowIndicator != null) {
                swipeArrowIndicator.startAnimation(pulse);
            }
            
            // Check if pattern is complete
            if (currentSwipePattern.size() >= targetSwipePattern.size()) {
                // Success! Pattern matched
                if (reportOutcomeCallback != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(
                        () -> reportOutcomeCallback.accept(true), 300);
                }
                hideChaseUI();
            } else {
                // Update to next direction
                updateSwipeArrowIndicator();
            }
        } else {
            // Wrong swipe - pattern failed
            if (swipeArrowIndicator != null) {
                // Show failure animation
                Animation shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake);
                swipeArrowIndicator.startAnimation(shakeAnimation);
            }
            
            // Notify callback of failure
            if (reportOutcomeCallback != null) {
                new Handler(Looper.getMainLooper()).postDelayed(
                    () -> reportOutcomeCallback.accept(false), 300);
            }
            hideChaseUI();
        }
    }

    public void updateAvatarViews(int playerCount, int enemyCount) {
        Log.d("GameUIController", "Updating avatar views explicitly (player: " + playerCount + ", enemy: " + enemyCount + ")");
        updateTeamAvatars(blueTeamAvatars, playerCount, false);
        updateTeamAvatars(redTeamAvatars, enemyCount, false);
    }

    public Button getShoutButton() {
        return shoutButton;
    }

    public Button getAnswerButton() {
        return answerButton;
    }

    public Button getRestartButton() {
        return restartButton;
    }

    public void highlightActiveTeam(int teamNumber) {
        View playerStatusView = playerSoldierCount.getRootView().findViewById(R.id.playerTeamStatus);
        View enemyStatusView = enemySoldierCount.getRootView().findViewById(R.id.enemyTeamStatus);

        if (teamNumber == 1) {
            playerStatusView.setBackgroundResource(R.drawable.rounded_status_highlighted);
            enemyStatusView.setBackgroundResource(R.drawable.rounded_status_normal);
        } else {
            playerStatusView.setBackgroundResource(R.drawable.rounded_status_normal);
            enemyStatusView.setBackgroundResource(R.drawable.rounded_status_highlighted);
        }
    }

    public void highlightPlayerSoldier(int soldierIndex) {
        if (blueTeamAvatars == null || soldierIndex >= blueTeamAvatars.getChildCount()) return;

        // Reset all avatars first
        for (int i = 0; i < blueTeamAvatars.getChildCount(); i++) {
            View child = blueTeamAvatars.getChildAt(i);
            if (child instanceof ImageView) {
                child.setBackgroundResource(R.drawable.avatar_background_normal);
            }
        }

        // Highlight the selected avatar
        View selectedAvatar = blueTeamAvatars.getChildAt(soldierIndex);
        if (selectedAvatar instanceof ImageView) {
            selectedAvatar.setBackgroundResource(R.drawable.avatar_background_selected);
            selectedAvatar.startAnimation(pulse);
        }
    }
    
    // Metoda pentru a evidenția un avatar de jucător
    public void highlightPlayerAvatar(int index, boolean highlight) {
        if (blueTeamAvatars == null || index >= blueTeamAvatars.getChildCount()) return;
        
        View avatar = blueTeamAvatars.getChildAt(index);
        if (avatar instanceof ImageView) {
            avatar.setBackgroundResource(highlight ? 
                R.drawable.avatar_background_selected : 
                R.drawable.avatar_background_normal);
            
            if (highlight) {
                avatar.startAnimation(pulse);
            }
        }
    }

    public void updateTeamCounts(int playerCount, int enemyCount) {
        if (playerSoldierCount != null) {
            playerSoldierCount.setText(context.getString(R.string.soldiers_count, playerCount));
        }
        if (enemySoldierCount != null) {
            enemySoldierCount.setText(context.getString(R.string.soldiers_count, enemyCount));
        }
        
        TextView roundCounter = playerSoldierCount.getRootView().findViewById(R.id.roundCounter);
        if (roundCounter != null) {
            roundCounter.setText(context.getString(R.string.round_counter, 0)); // Default round 0
        }
    }

    public void showRoundResult(boolean successful) {
        String message = successful ? 
            context.getString(R.string.round_success) : 
            context.getString(R.string.round_failure);
        showGameMessage(message);
    }

    public void updateRoundCounter(int roundCount) {
        TextView roundCounter = playerSoldierCount.getRootView().findViewById(R.id.roundCounter);
        if (roundCounter != null) {
            roundCounter.setText(context.getString(R.string.round_counter, roundCount));
        }
    }

    public void highlightAvatar(int index, boolean highlight) {
        if (index < 0) return;
        
        // Determine if this is a player or enemy avatar based on index
        LinearLayout targetLayout;
        if (index < blueTeamAvatars.getChildCount()) {
            targetLayout = blueTeamAvatars;
        } else {
            targetLayout = redTeamAvatars;
            index = index - blueTeamAvatars.getChildCount();
        }
        
        if (targetLayout == null || index >= targetLayout.getChildCount()) return;
        
        View avatar = targetLayout.getChildAt(index);
        if (avatar instanceof ImageView) {
            avatar.setBackgroundResource(highlight ? 
                R.drawable.avatar_background_selected : 
                R.drawable.avatar_background_normal);
            
            if (highlight) {
                avatar.startAnimation(pulse);
            }
        }
    }
    
    public void showChaseUI(boolean isPlayerChasing, Soldier targetSoldier) {
        // Implementare nouă folosește startWallBreakMinigame
        // Această metodă este păstrată pentru compatibilitate
        if (swipeChaseContainer != null) {
            swipeChaseContainer.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Porneste mini-jocul de "spargere a zidului"
     */
    public void startWallBreakMinigame(boolean isPlayerTurn, float difficulty, Consumer<Boolean> resultCallback) {
        Log.d("GameUIController", "Starting wall break minigame (player turn: " + isPlayerTurn + ", difficulty: " + difficulty + ")");

        // First ensure we have a valid component
        if (wallBreakMinigame == null) {
            Log.w("GameUIController", "Wall break minigame component is null, creating it dynamically");
            
            // Try to find the minigame container
            FrameLayout minigameContainer = (FrameLayout) playerSoldierCount.getRootView().findViewById(R.id.minigameContainer);
            if (minigameContainer == null) {
                Log.w("GameUIController", "Minigame container not found, creating it");
                
                // Get the root view and add a container
                ViewGroup rootView = (ViewGroup) playerSoldierCount.getRootView();
                minigameContainer = new FrameLayout(context);
                minigameContainer.setId(View.generateViewId());
                
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
                rootView.addView(minigameContainer, params);
            }
            
            // Create the minigame view
            wallBreakMinigame = new WallBreakMinigame(context);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
            minigameContainer.addView(wallBreakMinigame, params);
        }

        // Make sure container and minigame are visible
        ViewGroup minigameContainer = (ViewGroup) wallBreakMinigame.getParent();
        if (minigameContainer != null) {
            minigameContainer.setVisibility(View.VISIBLE);
            Log.d("GameUIController", "Minigame container is now visible");
        }
        
        wallBreakMinigame.setVisibility(View.VISIBLE);
        Log.d("GameUIController", "Wall break minigame is now visible");
        
        // Bring to front
        wallBreakMinigame.bringToFront();
        if (minigameContainer != null) {
            minigameContainer.bringToFront();
            minigameContainer.invalidate();
        }
        
        // Afișăm instrucțiuni adecvate
        String instructionText;
        if (isPlayerTurn) {
            instructionText = context.getString(R.string.chase_player_instruction);
        } else {
            instructionText = context.getString(R.string.chase_ai_instruction);
        }
        
        showGameMessage(instructionText);
        
        // Pornim mini-jocul
        wallBreakMinigame.startGame(isPlayerTurn, difficulty, resultCallback);
        
        // Animăm echipa care formează "zidul"
        animateWallTeam(isPlayerTurn);
    }
    
    /**
     * Animează echipa care formează "zidul"
     */
    private void animateWallTeam(boolean isPlayerAttacking) {
        LinearLayout wallTeam = isPlayerAttacking ? redTeamAvatars : blueTeamAvatars;
        
        if (wallTeam == null) return;
        
        // Crează un efect de pulsație pentru echipă
        for (int i = 0; i < wallTeam.getChildCount(); i++) {
            View avatar = wallTeam.getChildAt(i);
            if (avatar instanceof ImageView) {
                avatar.startAnimation(pulse);
                
                // Modifică temporar background-ul pentru a indica "zidul"
                avatar.setBackgroundResource(R.drawable.avatar_wall_background);
            }
        }
    }
    
    /**
     * Resetează animațiile și vizualul pentru echipe
     */
    private void resetTeamAnimations() {
        // Resetăm animațiile pentru ambele echipe
        resetTeamAnimation(blueTeamAvatars);
        resetTeamAnimation(redTeamAvatars);
    }
    
    private void resetTeamAnimation(LinearLayout teamLayout) {
        if (teamLayout == null) return;
        
        for (int i = 0; i < teamLayout.getChildCount(); i++) {
            View avatar = teamLayout.getChildAt(i);
            avatar.clearAnimation();
            avatar.setBackgroundResource(R.drawable.avatar_background_normal);
        }
    }
    
    public void resetAvatarHighlights() {
        // Reset highlights for player team
        if (blueTeamAvatars != null) {
            for (int i = 0; i < blueTeamAvatars.getChildCount(); i++) {
                View child = blueTeamAvatars.getChildAt(i);
                if (child instanceof ImageView) {
                    child.setBackgroundResource(R.drawable.avatar_background_normal);
                }
            }
        }
        
        // Reset highlights for enemy team
        if (redTeamAvatars != null) {
            for (int i = 0; i < redTeamAvatars.getChildCount(); i++) {
                View child = redTeamAvatars.getChildAt(i);
                if (child instanceof ImageView) {
                    child.setBackgroundResource(R.drawable.avatar_background_normal);
                }
            }
        }
    }

    /**
     * Animează îndepărtarea unui soldat din joc
     */
    public void animateSoldierRemoval(Soldier soldier) {
        // Animăm îndepărtarea soldatului
        if (soldier != null) {
            Team team = soldier.getTeam();
            if (team != null) {
                // Găsim avatarul corespunzător
                LinearLayout targetLayout = team.isPlayerTeam() ? blueTeamAvatars : redTeamAvatars;
                if (targetLayout != null) {
                    // Căutăm index-ul soldatului în echipă
                    int soldierIndex = team.getSoldiers().indexOf(soldier);
                    if (soldierIndex >= 0 && soldierIndex < targetLayout.getChildCount()) {
                        View avatar = targetLayout.getChildAt(soldierIndex);
                        if (avatar != null) {
                            // Animație de fade out pentru avatar
                            avatar.animate()
                                .alpha(0f)
                                .scaleX(0.5f)
                                .scaleY(0.5f)
                                .setDuration(500)
                                .withEndAction(() -> {
                                    // Actualizăm interfața după animație
                                    if (currentPlayerTeam != null && currentEnemyTeam != null) {
                                        updateTeamUI(currentPlayerTeam, currentEnemyTeam, true);
                                    }
                                })
                                .start();
                        }
                    }
                }
            }
        } else {
            // Fallback - actualizăm direct UI-ul
            if (currentPlayerTeam != null && currentEnemyTeam != null) {
                updateTeamUI(currentPlayerTeam, currentEnemyTeam, true);
            }
        }
    }

    /**
     * Actualizează barele de moral pentru echipe
     */
    public void updateTeamMorale(float playerMorale, float enemyMorale, float moraleChange) {
        // Actualizăm barele de progres cu animație
        animateProgressBar(playerMoraleBar, (int)playerMorale);
        animateProgressBar(enemyMoraleBar, (int)enemyMorale);
        
        // Actualizăm textele de moral
        animateTextChange(this.playerMorale, context.getString(R.string.morale_level, (int)playerMorale));
        animateTextChange(this.enemyMorale, context.getString(R.string.resistance_level, (int)enemyMorale));
        
        // Afișăm mesaj despre schimbarea de moral dacă este semnificativă
        if (Math.abs(moraleChange) > 5) {
            String message = moraleChange > 0 ? 
                context.getString(R.string.morale_up) : 
                context.getString(R.string.morale_down);
            showGameMessage(message);
        }
    }

    /**
     * Obține mesajul de rezultat pentru mini-jocul de chase
     */
    public String getChaseOutcomeMessage(boolean isPlayerTurn, boolean success) {
        if (isPlayerTurn) {
            return success ? context.getString(R.string.chase_success) : context.getString(R.string.chase_failure);
        } else {
            return success ? context.getString(R.string.enemy_caught_player) : context.getString(R.string.enemy_failed_catch);
        }
    }

    /**
     * Ascunde interfața mini-jocului și resetează starea vizuală
     */
    public void hideChaseUI() {
        // Hide swipe chase container
        if (swipeChaseContainer != null) {
            swipeChaseContainer.setVisibility(View.GONE);
            swipeChaseContainer.setOnTouchListener(null);
        }
        
        // Hide arrow indicator
        if (swipeArrowIndicator != null) {
            swipeArrowIndicator.clearAnimation();
            swipeArrowIndicator.setVisibility(View.GONE);
        }
        
        // Hide wall break minigame if it's visible
        if (wallBreakMinigame != null) {
            wallBreakMinigame.setVisibility(View.GONE);
            wallBreakMinigame.stop();
        }
        
        // Hide game instruction text
        if (gameInstructionText != null) {
            gameInstructionText.setVisibility(View.GONE);
        }
        
        // Clear any callbacks
        reportOutcomeCallback = null;
    }
    
    /**
     * Efect de fade out pentru un view
     */
    private void fadeOutView(View view) {
        view.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> view.setVisibility(View.GONE))
            .start();
    }
}
