package com.example.myapplication.TaraTara;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.HapticFeedbackConstants;
import android.animation.ObjectAnimator;

import com.example.myapplication.R;

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

    public GameUIController(Context context, View rootView) {
        this.context = context;

        // Initialize UI elements
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

        // Load animations
        fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        scaleIn = AnimationUtils.loadAnimation(context, R.anim.scale_in);
        pulse = AnimationUtils.loadAnimation(context, R.anim.pulse);
    }

    public void updateTeamUI(Team playerTeam1, Team enemyTeam, Team playerTeam2, boolean isFleeingMode) {
        this.currentPlayerTeam = playerTeam1;
        this.currentEnemyTeam = enemyTeam;

        View playerStatusView = playerSoldierCount.getRootView().findViewById(R.id.playerTeamStatus);
        View enemyStatusView = enemySoldierCount.getRootView().findViewById(R.id.enemyTeamStatus);

        Animation updateAnim = AnimationUtils.loadAnimation(context, R.anim.pulse);
        updateAnim.setDuration(300);
        playerStatusView.startAnimation(updateAnim);
        enemyStatusView.startAnimation(updateAnim);

        // Calculate total player soldiers with null check for playerTeam2
        int totalPlayerSoldiers = (playerTeam1 != null ? playerTeam1.getSoldierCount() : 0) +
                (playerTeam2 != null ? playerTeam2.getSoldierCount() : 0);

        if (playerTeam1 != null) {
            animateTextChange(playerSoldierCount, context.getString(R.string.soldiers_count, totalPlayerSoldiers));
            animateTextChange(playerMorale, context.getString(R.string.morale_level, (int)playerTeam1.getMorale()));
            animateProgressBar(playerMoraleBar, (int)playerTeam1.getMorale());
            updateTeamAvatars(blueTeamAvatars, totalPlayerSoldiers, !isFleeingMode);
        }

        if (enemyTeam != null) {
            animateTextChange(enemySoldierCount, context.getString(R.string.soldiers_count, enemyTeam.getSoldierCount()));
            animateTextChange(enemyMorale, context.getString(R.string.resistance_level, (int)enemyTeam.getMorale()));
            animateProgressBar(enemyMoraleBar, (int)enemyTeam.getMorale());
            updateTeamAvatars(redTeamAvatars, enemyTeam.getSoldierCount(), isFleeingMode);
        }
    }

    public void showGameOver(boolean playerWon) {
        if (currentPlayerTeam == null || currentEnemyTeam == null) return;

        gameOverlay.setAlpha(0f);
        gameOverlay.setVisibility(View.VISIBLE);

        View overlayEffect = new View(context);
        overlayEffect.setBackgroundColor(playerWon ? Color.GREEN : Color.RED);
        overlayEffect.setAlpha(0.3f);
        ((ViewGroup)gameOverlay.getParent()).addView(overlayEffect);

        overlayEffect.animate()
                .alpha(0f)
                .setDuration(1000)
                .withEndAction(() -> {
                    ((ViewGroup)gameOverlay.getParent()).removeView(overlayEffect);
                    gameOverlay.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .withEndAction(() -> {
                                overlayMessage.setText(playerWon ? R.string.victory : R.string.defeat);
                                overlayMessage.setTextColor(playerWon ? Color.GREEN : Color.RED);
                                overlayMessage.startAnimation(scaleIn);

                                String finalScore = context.getString(R.string.soldiers_count,
                                        playerWon ? currentPlayerTeam.getSoldierCount() : currentEnemyTeam.getSoldierCount());
                                showGameMessage(finalScore);

                                restartButton.setEnabled(true);
                                restartButton.setVisibility(View.VISIBLE);
                                restartButton.startAnimation(slideUp);

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    restartButton.performHapticFeedback(
                                            playerWon ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.REJECT
                                    );
                                }
                            });
                });
    }

    // ... (rest of the methods remain the same)

    public void showGameMessage(int messageResId) {
        if (gameMessageText != null) {
            gameMessageText.setText(messageResId);
            gameMessageText.setVisibility(View.VISIBLE);
            gameMessageText.bringToFront();
            gameMessageText.invalidate();
            gameMessageText.startAnimation(fadeIn);

            if (messageResId != R.string.tutorial_step5) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (gameMessageText != null) {
                        gameMessageText.startAnimation(fadeOut);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (gameMessageText != null) {
                                    gameMessageText.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                    }
                }, 2000);
            }
        }
    }

    public void showGameMessage(String message) {
        if (gameMessageText != null) {
            gameMessageText.setText(message);
            gameMessageText.setVisibility(View.VISIBLE);
            gameMessageText.bringToFront();
            gameMessageText.invalidate();
            gameMessageText.startAnimation(fadeIn);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (gameMessageText != null) {
                    gameMessageText.startAnimation(fadeOut);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (gameMessageText != null) {
                                gameMessageText.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                }
            }, 2000);
        }
    }

    private void animateTextChange(TextView textView, String newText) {
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
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), newProgress)
                .setDuration(300)
                .start();
    }

    private void updateTeamAvatars(LinearLayout avatarContainer, int activeCount, boolean enabled) {
        for (int i = 0; i < avatarContainer.getChildCount(); i++) {
            ImageView avatar = (ImageView) avatarContainer.getChildAt(i);
            boolean isActive = i < activeCount;

            avatar.animate()
                    .alpha(isActive ? 1.0f : 0.3f)
                    .setDuration(300)
                    .start();

            avatar.setEnabled(isActive && enabled);
            avatar.setClickable(isActive && enabled);
        }
    }

    public void setButtonStates(boolean shoutEnabled, boolean answerEnabled) {
        shoutButton.setEnabled(shoutEnabled);
        answerButton.setEnabled(answerEnabled);
    }

    /**
     * Enable avatar selection with a click listener
     * @param enemyCount Number of enemy avatars to enable
     * @param onAvatarClick Click listener to be called when an avatar is selected
     */
    public void enableAvatarSelection(int enemyCount, View.OnClickListener onAvatarClick) {
        for (int i = 0; i < redTeamAvatars.getChildCount(); i++) {
            ImageView avatar = (ImageView) redTeamAvatars.getChildAt(i);
            boolean isActive = i < enemyCount;

            if (isActive) {
                avatar.setEnabled(true);
                avatar.setClickable(true);
                avatar.setOnClickListener(v -> {
                    // Disable all avatars after one is selected
                    disableAllAvatars();
                    // Start pulse animation
                    v.startAnimation(pulse);
                    // Call the click listener
                    onAvatarClick.onClick(v);
                });
                avatar.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            avatar.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(200)
                                    .start();
                        })
                        .start();
            }
        }
    }

    /**
     * Enable avatar selection without click listener (for backward compatibility)
     * @param enemyCount Number of enemy avatars to enable
     */
    public void enableAvatarSelection(int enemyCount) {
        enableAvatarSelection(enemyCount, v -> {});
    }

    /**
     * Disable all avatar selections
     */
    private void disableAllAvatars() {
        for (int i = 0; i < redTeamAvatars.getChildCount(); i++) {
            ImageView avatar = (ImageView) redTeamAvatars.getChildAt(i);
            avatar.setEnabled(false);
            avatar.setClickable(false);
            avatar.setOnClickListener(null);
        }
    }

    public void showQTEElements(View.OnClickListener qteClickListener) {
        if (gameMessageText != null) {
            gameMessageText.setText(R.string.quick_time_event);
            gameMessageText.setVisibility(View.VISIBLE);
            gameMessageText.bringToFront();
            gameMessageText.invalidate();
            gameMessageText.startAnimation(fadeIn);
        }

        // Show and setup the break zone indicator
        breakZoneIndicator.setVisibility(View.VISIBLE);
        breakZoneIndicator.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        breakZoneIndicator.setAlpha(0.3f);
        breakZoneIndicator.setClickable(true);
        breakZoneIndicator.setOnClickListener(qteClickListener);

        // Initial color animation (0-2000ms: Blue to Red)
        ObjectAnimator initialFade = ObjectAnimator.ofArgb(
                breakZoneIndicator,
                "backgroundColor",
                context.getResources().getColor(R.color.colorPrimary),
                Color.RED
        );
        initialFade.setDuration(2000); // Match QTE_SUCCESS_WINDOW_START
        initialFade.start();

        // Success window color animation (2000-2500ms: Red to Green)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (breakZoneIndicator.isClickable()) {
                ObjectAnimator successFade = ObjectAnimator.ofArgb(
                        breakZoneIndicator,
                        "backgroundColor",
                        Color.RED,
                        Color.GREEN
                );
                successFade.setDuration(500); // Success window duration
                successFade.start();

                // Increase visibility during success window
                breakZoneIndicator.setAlpha(0.8f);
                breakZoneIndicator.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(500)
                        .start();
            }
        }, 2000);

        // Add pulsing effect that gets faster during success window
        Animation pulseAnim = AnimationUtils.loadAnimation(context, R.anim.pulse);
        pulseAnim.setRepeatCount(Animation.INFINITE);
        breakZoneIndicator.startAnimation(pulseAnim);

        // Speed up pulse animation during success window
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (breakZoneIndicator.isClickable()) {
                Animation fastPulse = AnimationUtils.loadAnimation(context, R.anim.pulse);
                fastPulse.setDuration(fastPulse.getDuration() / 2); // Faster pulse
                fastPulse.setRepeatCount(Animation.INFINITE);
                breakZoneIndicator.startAnimation(fastPulse);
            }
        }, 2000);
    }

    public void handleQTEEnd(boolean successful, Runnable onComplete) {
        breakZoneIndicator.setClickable(false);
        breakZoneIndicator.setOnClickListener(null);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            breakZoneIndicator.performHapticFeedback(
                    successful ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.REJECT
            );
        }

        if (successful) {
            ObjectAnimator.ofArgb(
                    breakZoneIndicator,
                    "backgroundColor",
                    Color.TRANSPARENT,
                    Color.GREEN,
                    Color.TRANSPARENT
            ).setDuration(500).start();
        }

        breakZoneIndicator.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                breakZoneIndicator.setVisibility(View.GONE);
                showGameMessage(successful ? R.string.formation_broken : R.string.formation_held);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    onComplete.run();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        showGameMessage(successful ? R.string.tutorial_step7 : R.string.tutorial_step8);

                        // Re-enable the shout button for the next round
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            setButtonStates(true, false);
                            shoutButton.startAnimation(scaleIn);
                        }, 2000);
                    }, 2000);
                }, 2000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    public void updateControlButtons(boolean enabled) {
        shoutButton.setEnabled(enabled);
        answerButton.setEnabled(enabled);

        if (enabled) {
            Animation buttonAnim = scaleIn;
            buttonAnim.setStartOffset(100);
            shoutButton.startAnimation(buttonAnim);
            buttonAnim.setStartOffset(200);
            answerButton.startAnimation(buttonAnim);
            buttonAnim.setStartOffset(0);
        }
    }

    public Button getShoutButton() {
        return shoutButton;
    }

    public Button getAnswerButton() {
        return answerButton;
    }

    public void updateAvatarViews(int playerCount, int enemyCount) {
        for (int i = 0; i < blueTeamAvatars.getChildCount(); i++) {
            ImageView avatar = (ImageView) blueTeamAvatars.getChildAt(i);
            boolean isActive = i < playerCount;
            avatar.setAlpha(isActive ? 1.0f : 0.3f);
            avatar.setEnabled(isActive);
        }

        for (int i = 0; i < redTeamAvatars.getChildCount(); i++) {
            ImageView avatar = (ImageView) redTeamAvatars.getChildAt(i);
            boolean isActive = i < enemyCount;
            avatar.setAlpha(isActive ? 1.0f : 0.3f);
            avatar.setEnabled(isActive);
        }
    }
}
