package com.example.myapplication.TaraTara;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class TaraMinigameGameView extends View {
    private Team playerTeam1;
    private Team playerTeam2;
    private Team enemyTeam;
    private Paint paint;
    private OnTeamUpdateListener teamUpdateListener;

    public TaraMinigameGameView(Context context) {
        super(context);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void startGame(Team playerTeam1, Team enemyTeam, Team playerTeam2) {
        this.playerTeam1 = playerTeam1;
        this.enemyTeam = enemyTeam;
        this.playerTeam2 = playerTeam2;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear the canvas with a background color
        canvas.drawColor(Color.WHITE);

        // Draw teams if initialized
        if (playerTeam1 != null && enemyTeam != null) {
            drawTeam(canvas, playerTeam1);
            drawTeam(canvas, enemyTeam);
        }
        if (playerTeam2 != null) {
            drawTeam(canvas, playerTeam2);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Reinitialize teams with new dimensions if they exist
        if (playerTeam1 != null && playerTeam2 != null && enemyTeam != null) {
            float screenWidth = w;
            float screenHeight = h;
            int initialSoldiers = 5;

            playerTeam1.initializeTeam(screenWidth, screenHeight, initialSoldiers);
            playerTeam2.initializeTeam(screenWidth, screenHeight, initialSoldiers);
            enemyTeam.initializeTeam(screenWidth, screenHeight, initialSoldiers);
        }
    }

    private void drawTeam(Canvas canvas, Team team) {
        paint.setColor(team.getColor());
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(180); // Make soldiers semi-transparent

        int soldierSize = 40; // Increased size for better visibility

        for (Soldier soldier : team.getSoldiers()) {
            // Draw a background circle
            canvas.drawCircle(
                    soldier.getX(),
                    soldier.getY(),
                    soldierSize,
                    paint
            );

            // Draw the soldier drawable
            soldier.getDrawable().setBounds(
                    (int)soldier.getX() - soldierSize,
                    (int)soldier.getY() - soldierSize,
                    (int)soldier.getX() + soldierSize,
                    (int)soldier.getY() + soldierSize
            );
            soldier.getDrawable().setAlpha(255); // Make drawable fully opaque
            soldier.getDrawable().draw(canvas);
        }
    }

    public void pause() {
        // Pause game logic
    }

    public void resume() {
        // Resume game logic
    }

    public interface OnTeamUpdateListener {
        void onTeamUpdate(Team team);
        void onGameOver(boolean playerWon);
    }

    public void setOnTeamUpdateListener(OnTeamUpdateListener listener) {
        this.teamUpdateListener = listener;
    }

    protected void notifyTeamUpdate(Team team) {
        if (teamUpdateListener != null) {
            teamUpdateListener.onTeamUpdate(team);
        }
    }

    protected void notifyGameOver(boolean playerWon) {
        if (teamUpdateListener != null) {
            teamUpdateListener.onGameOver(playerWon);
        }
    }

    public boolean contains(float x, float y) {
        for (Soldier soldier : playerTeam1.getSoldiers()) {
            if (soldier.contains(x, y)) {
                return true;
            }
        }
        for (Soldier soldier : enemyTeam.getSoldiers()) {
            if (soldier.contains(x, y)) {
                return true;
            }
        }
        for (Soldier soldier : playerTeam2.getSoldiers()) {
            if (soldier.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public Soldier getRandomSoldier(Team team) {
        if (team == null) {
            Log.e("TaraMinigameGameView", "Team is not initialized.");
            return null;
        }
        return team.getRandomSoldier();
    }

    public void updateSoldier(Soldier soldier) {
        soldier.update();
    }

    public void drawSoldier(Canvas canvas, Soldier soldier) {
        soldier.getDrawable().setBounds(
                (int)soldier.getX() - 25,
                (int)soldier.getY() - 25,
                (int)soldier.getX() + 25,
                (int)soldier.getY() + 25
        );
        soldier.getDrawable().draw(canvas);
    }
}
