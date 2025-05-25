package com.example.myapplication.TaraTara;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;

public class TaraMinigameGameView extends View {
    private Team playerTeam1;
    private Team playerTeam2;
    private Team enemyTeam;
    private Paint paint;
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint glowPaint;
    private OnTeamUpdateListener teamUpdateListener;
    private boolean teamsInitialized = false;
    
    // Game field parameters
    private float fieldCenterX;
    private float fieldCenterY;
    private float fieldRadius;
    
    // Team positioning
    private float team1CenterX, team1CenterY;
    private float team2CenterX, team2CenterY;
    private float enemyCenterX, enemyCenterY;

    public TaraMinigameGameView(Context context) {
        super(context);
        initializePaints();
    }
    
    private void initializePaints() {
        // Main paint for soldiers
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        // Background paint with subtle texture
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#F5F5F5"));
        
        // Border paint for soldiers
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        
        // Glow paint for effects
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
    }

    public void startGame(Team playerTeam1, Team enemyTeam, Team playerTeam2) {
        this.playerTeam1 = playerTeam1;
        this.enemyTeam = enemyTeam;
        this.playerTeam2 = playerTeam2;
        
        // Only initialize teams if dimensions are valid
        if (!teamsInitialized && getWidth() > 0 && getHeight() > 0) {
            calculateGameField();
            initializeTeams();
        }
        
        invalidate();
    }
    
    private void calculateGameField() {
        // Calculate the game field dimensions
        fieldCenterX = getWidth() / 2f;
        fieldCenterY = getHeight() / 2f;
        fieldRadius = Math.min(getWidth(), getHeight()) * 0.45f;
        
        // Calculate team positions
        // Player 1 team at bottom left
        team1CenterX = getWidth() * 0.2f;
        team1CenterY = getHeight() * 0.7f;
        
        // Player 2 team at bottom right
        team2CenterX = getWidth() * 0.8f;
        team2CenterY = getHeight() * 0.7f;
        
        // Enemy team at top center
        enemyCenterX = getWidth() * 0.5f;
        enemyCenterY = getHeight() * 0.3f;
    }
    
    private void initializeTeams() {
        if (playerTeam1 == null || enemyTeam == null) {
            Log.e("TaraMinigameGameView", "Cannot initialize null teams");
            return;
        }
        
        int initialSoldiers = 5;
        
        // Initialize player team 1 with its center position
        playerTeam1.setTeamCenter(team1CenterX, team1CenterY);
        playerTeam1.initializeTeam(getWidth(), getHeight(), initialSoldiers);
        
        // Initialize enemy team with its center position
        enemyTeam.setTeamCenter(enemyCenterX, enemyCenterY);
        enemyTeam.initializeTeam(getWidth(), getHeight(), initialSoldiers);
        
        if (playerTeam2 != null) {
            // Initialize player team 2 with its center position
            playerTeam2.setTeamCenter(team2CenterX, team2CenterY);
            playerTeam2.initializeTeam(getWidth(), getHeight(), initialSoldiers);
        }
        
        teamsInitialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the game background with field
        drawBackground(canvas);

        // Draw team areas
        drawTeamAreas(canvas);
        
        // Draw teams if initialized
        if (playerTeam1 != null && enemyTeam != null) {
            drawTeam(canvas, playerTeam1);
            drawTeam(canvas, enemyTeam);
        }
        if (playerTeam2 != null) {
            drawTeam(canvas, playerTeam2);
        }
    }
    
    private void drawBackground(Canvas canvas) {
        // Fill the canvas with a light background
        canvas.drawColor(Color.parseColor("#F5F5F5"));
        
        // Draw a subtle pattern or texture if desired
        // For now, just draw a light grid
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(1);
        
        float spacing = 50f;
        for (float x = 0; x < getWidth(); x += spacing) {
            canvas.drawLine(x, 0, x, getHeight(), gridPaint);
        }
        
        for (float y = 0; y < getHeight(); y += spacing) {
            canvas.drawLine(0, y, getWidth(), y, gridPaint);
        }
    }
    
    private void drawTeamAreas(Canvas canvas) {
        // Draw circular areas for each team with semi-transparent colors
        if (playerTeam1 != null) {
            Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            int teamColor = playerTeam1.getColor();
            areaPaint.setColor(Color.argb(30, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)));
            canvas.drawCircle(team1CenterX, team1CenterY, 150, areaPaint);
        }
        
        if (playerTeam2 != null) {
            Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            int teamColor = playerTeam2.getColor();
            areaPaint.setColor(Color.argb(30, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)));
            canvas.drawCircle(team2CenterX, team2CenterY, 150, areaPaint);
        }
        
        if (enemyTeam != null) {
            Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            int teamColor = enemyTeam.getColor();
            areaPaint.setColor(Color.argb(30, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)));
            canvas.drawCircle(enemyCenterX, enemyCenterY, 150, areaPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Recalculate the game field when size changes
        calculateGameField();
        
        // Reset the initialized flag because dimensions have changed
        teamsInitialized = false;
        
        // If teams exist, reinitialize them with new dimensions
        if (playerTeam1 != null && enemyTeam != null) {
            initializeTeams();
        }
    }

    private void drawTeam(Canvas canvas, Team team) {
        int teamColor = team.getColor();
        
        paint.setColor(teamColor);
        paint.setAlpha(200);
        
        borderPaint.setColor(Color.WHITE);
        
        int soldierSize = 35;

        for (Soldier soldier : team.getSoldiers()) {
            // Draw the soldier with a glow effect
            drawSoldierWithEffects(canvas, soldier, soldierSize, teamColor);
        }
    }
    
    private void drawSoldierWithEffects(Canvas canvas, Soldier soldier, int size, int teamColor) {
        float x = soldier.getX();
        float y = soldier.getY();
        
        // Create a glow effect with radial gradient
        RadialGradient gradient = new RadialGradient(
            x, y, size * 1.5f,
            Color.argb(100, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)),
            Color.argb(0, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)),
            Shader.TileMode.CLAMP
        );
        glowPaint.setShader(gradient);
        
        // Draw the glow
        canvas.drawCircle(x, y, size * 1.5f, glowPaint);
        
        // Draw the soldier body
        canvas.drawCircle(x, y, size, paint);
        
        // Draw white border
        canvas.drawCircle(x, y, size, borderPaint);
        
        // Draw the soldier drawable
        soldier.getDrawable().setBounds(
            (int)x - size, 
            (int)y - size, 
            (int)x + size, 
            (int)y + size
        );
        soldier.getDrawable().setAlpha(255);
        soldier.getDrawable().draw(canvas);
        
        // Reset shader
        glowPaint.setShader(null);
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

    public boolean contains(float x, float y, Soldier soldier) {
        float distance = (float) Math.sqrt(
            Math.pow(x - soldier.getX(), 2) + 
            Math.pow(y - soldier.getY(), 2)
        );
        return distance <= 40; // Slightly larger hitbox for better touch detection
    }
    
    public boolean contains(float x, float y) {
        if (playerTeam1 != null) {
            for (Soldier soldier : playerTeam1.getSoldiers()) {
                if (contains(x, y, soldier)) {
                    return true;
                }
            }
        }
        
        if (enemyTeam != null) {
            for (Soldier soldier : enemyTeam.getSoldiers()) {
                if (contains(x, y, soldier)) {
                    return true;
                }
            }
        }
        
        if (playerTeam2 != null) {
            for (Soldier soldier : playerTeam2.getSoldiers()) {
                if (contains(x, y, soldier)) {
                    return true;
                }
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
