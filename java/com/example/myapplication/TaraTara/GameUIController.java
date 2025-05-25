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
        updateTeamAvatars(blueTeamAvatars, playerTeam.getSoldierCount(), false);

        animateTextChange(enemySoldierCount, context.getString(R.string.soldiers_count, enemyTeam.getSoldierCount()));
        animateTextChange(enemyMorale, context.getString(R.string.resistance_level, (int)enemyTeam.getMorale()));
        animateProgressBar(enemyMoraleBar, (int)enemyTeam.getMorale());
        updateTeamAvatars(redTeamAvatars, enemyTeam.getSoldierCount(), false);
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
        
        // Set up the swipe chase UI
        if (swipeChaseContainer != null) {
            swipeChaseContainer.setVisibility(View.VISIBLE);
            swipeChaseContainer.bringToFront();
            
            // Generate pattern based on difficulty (1-5 gestures)
            int patternLength = Math.max(1, Math.min(5, Math.round(difficulty * 3)));
            generateRandomSwipePattern(patternLength);
            currentSwipePattern.clear();
            
            updateSwipeArrowIndicator();
        } else {
            // If container is not available, auto-succeed
            if (reportOutcomeCallback != null) {
                reportOutcomeCallback.accept(true);
            }
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
            targetSwipePattern.add(directions[random.nextInt(directions.length)]);
        }
    }

    private void updateSwipeArrowIndicator() {
        if (swipeArrowIndicator == null || targetSwipePattern.isEmpty() || currentSwipePattern.size() >= targetSwipePattern.size()) {
            if (swipeArrowIndicator != null) swipeArrowIndicator.setVisibility(View.GONE);
            return;
        }

        SwipeDirection nextSwipe = targetSwipePattern.get(currentSwipePattern.size());
        Log.d("GameUIController", "Next required swipe: " + nextSwipe);
        swipeArrowIndicator.setVisibility(View.VISIBLE);

        switch (nextSwipe) {
            case UP:
                swipeArrowIndicator.setImageResource(R.drawable.ic_arrow_up);
                break;
            case DOWN:
                swipeArrowIndicator.setImageResource(R.drawable.ic_arrow_down);
                break;
            case LEFT:
                swipeArrowIndicator.setImageResource(R.drawable.ic_arrow_left);
                break;
            case RIGHT:
                swipeArrowIndicator.setImageResource(R.drawable.ic_arrow_right);
                break;
        }
        swipeArrowIndicator.startAnimation(fadeIn);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (reportOutcomeCallback == null) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            SwipeDirection detectedSwipe = null;

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        detectedSwipe = SwipeDirection.RIGHT;
                    } else {
                        detectedSwipe = SwipeDirection.LEFT;
                    }
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        detectedSwipe = SwipeDirection.DOWN;
                    } else {
                        detectedSwipe = SwipeDirection.UP;
                    }
                }
            }

            if (detectedSwipe != null) {
                handleSwipe(detectedSwipe);
            }
            return true;
        }
    }

    private void handleSwipe(SwipeDirection swipe) {
        Log.d("GameUIController", "Swipe detected: " + swipe);
        if (currentSwipePattern.size() < targetSwipePattern.size()) {
            SwipeDirection expectedSwipe = targetSwipePattern.get(currentSwipePattern.size());
            if (swipe == expectedSwipe) {
                currentSwipePattern.add(swipe);
                Log.d("GameUIController", "Correct swipe! Pattern: " + currentSwipePattern);
                if (currentSwipePattern.size() == targetSwipePattern.size()) {
                    Log.d("GameUIController", "Swipe pattern complete!");
                    reportOutcomeCallback.accept(true);
                    reportOutcomeCallback = null;
                } else {
                    updateSwipeArrowIndicator();
                }
            } else {
                Log.d("GameUIController", "Incorrect swipe! Expected " + expectedSwipe + ", got " + swipe);
                reportOutcomeCallback.accept(false);
                reportOutcomeCallback = null;
            }
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

    public void highlightPlayerSoldier(int teamNumber, int soldierIndex) {
        LinearLayout targetLayout = teamNumber == 1 ? blueTeamAvatars : redTeamAvatars;
        if (targetLayout == null || soldierIndex >= targetLayout.getChildCount()) return;

        // Reset all avatars first
        for (int i = 0; i < targetLayout.getChildCount(); i++) {
            View child = targetLayout.getChildAt(i);
            if (child instanceof ImageView) {
                child.setBackgroundResource(R.drawable.avatar_background_normal);
            }
        }

        // Highlight the selected avatar
        View selectedAvatar = targetLayout.getChildAt(soldierIndex);
        if (selectedAvatar instanceof ImageView) {
            selectedAvatar.setBackgroundResource(R.drawable.avatar_background_selected);
            selectedAvatar.startAnimation(pulse);
        }
    }

    public void updateTeamCounts(int playerCount, int enemyCount, int round) {
        if (playerSoldierCount != null) {
            playerSoldierCount.setText(context.getString(R.string.soldiers_count, playerCount));
        }
        if (enemySoldierCount != null) {
            enemySoldierCount.setText(context.getString(R.string.soldiers_count, enemyCount));
        }
        
        TextView roundCounter = playerSoldierCount.getRootView().findViewById(R.id.roundCounter);
        if (roundCounter != null) {
            roundCounter.setText(context.getString(R.string.round_counter, round));
        }
    }

    public void showRoundResult(boolean successful) {
        String message = successful ? 
            context.getString(R.string.round_success) : 
            context.getString(R.string.round_failure);
        showGameMessage(message);
    }
}
