package com.example.myapplication.TaraTara;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaraMinigameGameView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "TaraMinigameGameView";
    
    // Teams
    private Team playerTeam1;
    private Team enemyTeam;
    
    // Drawing tools
    private Paint paint;
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint glowPaint;
    private Path teamAreaPath;
    
    // Game rendering
    private GameThread gameThread;
    private SurfaceHolder surfaceHolder;
    private boolean isRunning = false;
    private boolean teamsInitialized = false;
    
    // Cache for drawable tinting
    private LruCache<String, Drawable> drawableCache;
    
    // Game field parameters
    private float fieldCenterX;
    private float fieldCenterY;
    private float fieldRadius;
    
    // Team positioning
    private float team1CenterX, team1CenterY;
    private float team2CenterX, team2CenterY;
    private float enemyCenterX, enemyCenterY;
    
    // Listener for team updates
    private OnTeamUpdateListener teamUpdateListener;
    
    // List to track animations
    private CopyOnWriteArrayList<AnimationEffect> activeAnimations = new CopyOnWriteArrayList<>();
    
    /**
     * Animation effect class to handle visual effects
     */
    private static class AnimationEffect {
        float x, y;
        float radius;
        int color;
        long startTime;
        long duration;
        
        AnimationEffect(float x, float y, float radius, int color, long duration) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
            this.startTime = System.currentTimeMillis();
            this.duration = duration;
        }
        
        boolean isAlive() {
            return System.currentTimeMillis() - startTime < duration;
        }
        
        float getProgress() {
            return Math.min(1.0f, (System.currentTimeMillis() - startTime) / (float) duration);
        }
    }

    public TaraMinigameGameView(Context context) {
        super(context);
        
        // Initialize drawing tools
        initializePaints();
        
        // Initialize surface holder
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        
        // Initialize drawable cache
        int cacheSize = 20; // Number of drawables to cache
        drawableCache = new LruCache<>(cacheSize);
        
        // Initialize path for team areas
        teamAreaPath = new Path();
        
        // Set focusable to receive events
        setFocusable(true);
    }
    
    private void initializePaints() {
        // Main paint for soldiers - reused for all soldier drawings
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Start game thread when surface is created
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread();
            isRunning = true;
            gameThread.start();
        }
        
        // Calculate game field on surface creation
        calculateGameField();
        
        // Initialize teams if data is available
        if (playerTeam1 != null && enemyTeam != null && getWidth() > 0 && getHeight() > 0) {
            initializeTeams();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Recalculate game field when surface dimensions change
        calculateGameField();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Stop game thread when surface is destroyed
        isRunning = false;
        try {
            if (gameThread != null) {
                gameThread.join();
                gameThread = null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Error stopping game thread", e);
        }
    }

    public void startGame(Team playerTeam1, Team enemyTeam) {
        Log.d(TAG, "startGame called with teams: " + 
              (playerTeam1 != null ? "Player OK" : "Player NULL") + ", " + 
              (enemyTeam != null ? "Enemy OK" : "Enemy NULL"));
              
        this.playerTeam1 = playerTeam1;
        this.enemyTeam = enemyTeam;
        
        // Reset initialization flag
        teamsInitialized = false;
        
        // Clear animations
        activeAnimations.clear();
        
        // Only initialize teams if dimensions are valid
        if (getWidth() > 0 && getHeight() > 0) {
            calculateGameField();
            initializeTeams();
            teamsInitialized = true;
        }
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
        
        Log.d(TAG, "Game field calculated: center(" + fieldCenterX + "," + fieldCenterY + 
              "), radius=" + fieldRadius + ", dimensions: " + getWidth() + "x" + getHeight());
    }
    
    private void initializeTeams() {
        if (playerTeam1 == null || enemyTeam == null) {
            Log.e(TAG, "Cannot initialize null teams");
            return;
        }
        
        int initialSoldiers = 5;
        
        Log.d(TAG, "Initializing teams with " + initialSoldiers + " soldiers each");
        
        try {
            // Initialize player team 1 with its center position
            playerTeam1.setTeamCenter(team1CenterX, team1CenterY);
            playerTeam1.initializeTeam(getWidth(), getHeight(), initialSoldiers);
            
            // Initialize enemy team with its center position
            enemyTeam.setTeamCenter(enemyCenterX, enemyCenterY);
            enemyTeam.initializeTeam(getWidth(), getHeight(), initialSoldiers);
            
            teamsInitialized = true;
            
            // Notify that teams have been initialized
            if (teamUpdateListener != null) {
                teamUpdateListener.onTeamUpdate(playerTeam1);
                teamUpdateListener.onTeamUpdate(enemyTeam);
            }
            
            Log.d(TAG, "Teams initialized successfully - Player1: " + 
                  playerTeam1.getSoldierCount() + 
                  ", Enemy: " + enemyTeam.getSoldierCount());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing teams", e);
        }
    }
    
    /**
     * Game thread that handles the rendering loop
     */
    private class GameThread extends Thread {
        private static final int TARGET_FPS = 60;
        private static final long FRAME_PERIOD_MS = 1000 / TARGET_FPS;
        
        @Override
        public void run() {
            long beginTime;
            long timeDiff;
            long sleepTime;
            
            while (isRunning) {
                Canvas canvas = null;
                beginTime = System.currentTimeMillis();
                
                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            render(canvas);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in game loop", e);
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            Log.e(TAG, "Error unlocking canvas", e);
                        }
                    }
                }
                
                // Calculate sleep time to maintain target FPS
                timeDiff = System.currentTimeMillis() - beginTime;
                sleepTime = Math.max(0, FRAME_PERIOD_MS - timeDiff);
                
                try {
                    if (sleepTime > 0) {
                        sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread sleep interrupted", e);
                }
            }
        }
    }
    
    /**
     * Main render method that draws the game state
     */
    private void render(Canvas canvas) {
        if (canvas == null) return;
        
        try {
            // Clear the canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            
            // Draw the game background with field
            drawBackground(canvas);
    
            // Draw team areas
            drawTeamAreas(canvas);
            
            // Draw teams if initialized
            if (teamsInitialized && playerTeam1 != null && enemyTeam != null) {
                drawTeam(canvas, playerTeam1);
                drawTeam(canvas, enemyTeam);
            } else if (!teamsInitialized && getWidth() > 0 && getHeight() > 0 && 
                      playerTeam1 != null && enemyTeam != null) {
                // Try to initialize if not done yet but dimensions are available
                calculateGameField();
                initializeTeams();
            }
            
            // Draw active animations
            drawAnimations(canvas);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in render", e);
        }
    }
    
    private void drawBackground(Canvas canvas) {
        // Fill the canvas with a light background to resemble paper
        canvas.drawColor(Color.parseColor("#F8F9FA"));
        
        // Draw a more pronounced grid pattern to resemble a math notebook
        Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#CCCCCC")); // Darker grid lines
        gridPaint.setStrokeWidth(1);
        
        // Draw vertical light blue lines (primary grid)
        float primaryGridSize = 80f; // Larger grid squares for math notebook look
        Paint primaryGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        primaryGridPaint.setColor(Color.parseColor("#3F51B5"));
        primaryGridPaint.setAlpha(50); // Semi-transparent
        primaryGridPaint.setStrokeWidth(1);
        
        for (float x = 0; x < getWidth(); x += primaryGridSize) {
            canvas.drawLine(x, 0, x, getHeight(), primaryGridPaint);
        }
        
        // Draw horizontal light blue lines (primary grid)
        for (float y = 0; y < getHeight(); y += primaryGridSize) {
            canvas.drawLine(0, y, getWidth(), y, primaryGridPaint);
        }
        
        // Draw smaller grid lines between the primary ones
        Paint secondaryGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondaryGridPaint.setColor(Color.parseColor("#CCCCCC")); // Light gray for secondary lines
        secondaryGridPaint.setStrokeWidth(0.5f);
        
        float secondaryGridSize = primaryGridSize / 4; // 4 small squares per large square
        
        // Only draw secondary lines if the view is large enough
        if (getWidth() > 400) { // Skip on very small screens
            for (float x = 0; x < getWidth(); x += secondaryGridSize) {
                // Skip if this is already a primary line
                if (Math.abs(x % primaryGridSize) > 1) {
                    canvas.drawLine(x, 0, x, getHeight(), secondaryGridPaint);
                }
            }
            
            for (float y = 0; y < getHeight(); y += secondaryGridSize) {
                // Skip if this is already a primary line
                if (Math.abs(y % primaryGridSize) > 1) {
                    canvas.drawLine(0, y, getWidth(), y, secondaryGridPaint);
                }
            }
        }
        
        // Add red margin line on the left to simulate a notebook margin
        Paint marginPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        marginPaint.setColor(Color.parseColor("#FF5252"));
        marginPaint.setStrokeWidth(2);
        float marginPosition = primaryGridSize * 1.5f;
        canvas.drawLine(marginPosition, 0, marginPosition, getHeight(), marginPaint);
        
        // Draw a subtle border around the game field
        Paint fieldBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fieldBorderPaint.setStyle(Paint.Style.STROKE);
        fieldBorderPaint.setColor(Color.parseColor("#3F51B5"));
        fieldBorderPaint.setStrokeWidth(3);
        canvas.drawRect(0, 0, getWidth(), getHeight(), fieldBorderPaint);
    }
    
    private void drawTeamAreas(Canvas canvas) {
        if (playerTeam1 == null || enemyTeam == null) return;
        
        // Draw player team 1 area
        drawTeamArea(canvas, team1CenterX, team1CenterY, playerTeam1.getTeamColor());
        
        // Draw enemy team area
        drawTeamArea(canvas, enemyCenterX, enemyCenterY, enemyTeam.getTeamColor());
    }
    
    private void drawTeamArea(Canvas canvas, float centerX, float centerY, int teamColor) {
        float areaRadius = 60f;
        Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        areaPaint.setStyle(Paint.Style.FILL);
        areaPaint.setColor(Color.argb(40, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)));
        
        // Draw team area circle
        canvas.drawCircle(centerX, centerY, areaRadius, areaPaint);
        
        // Draw team area border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setColor(teamColor);
        canvas.drawCircle(centerX, centerY, areaRadius, borderPaint);
    }
    
    private void drawTeam(Canvas canvas, Team team) {
        if (team == null) return;
        
        int teamColor = team.getTeamColor();
        List<Soldier> soldiers = team.getSoldiers();
        
        for (Soldier soldier : soldiers) {
            if (soldier != null) {
                drawSoldierWithEffects(canvas, soldier, 40, teamColor);
            }
        }
    }
    
    private void drawSoldierWithEffects(Canvas canvas, Soldier soldier, int size, int teamColor) {
        if (soldier == null) return;
        
        float x = soldier.getX();
        float y = soldier.getY();
        
        // Create glow effect for active soldiers
        if (soldier.isActive()) {
            RadialGradient gradient = new RadialGradient(
                    x, y, size * 1.5f,
                    Color.argb(100, Color.red(teamColor), Color.green(teamColor), Color.blue(teamColor)),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP);
            
            glowPaint.setShader(gradient);
            canvas.drawCircle(x, y, size * 1.5f, glowPaint);
            
            // Add animation effect for active soldier
            if (Math.random() < 0.05) { // 5% chance to add effect
                addAnimationEffect(x, y, teamColor);
            }
        }
        
        // Draw soldier using cached drawable
        Drawable soldierDrawable = getSoldierDrawable(soldier, teamColor);
        if (soldierDrawable != null) {
            soldierDrawable.setBounds(
                    (int)(x - size/2), 
                    (int)(y - size/2), 
                    (int)(x + size/2), 
                    (int)(y + size/2));
            soldierDrawable.draw(canvas);
        }
        
        // Draw health indicator if damaged
        if (soldier.getHealth() < 100) {
            float healthBarWidth = size * 0.8f;
            float healthBarHeight = 4f;
            float healthBarX = x - healthBarWidth / 2;
            float healthBarY = y + size / 2 + 6;
            
            // Background
            Paint healthBgPaint = new Paint();
            healthBgPaint.setColor(Color.GRAY);
            canvas.drawRect(healthBarX, healthBarY, healthBarX + healthBarWidth, healthBarY + healthBarHeight, healthBgPaint);
            
            // Health level
            Paint healthPaint = new Paint();
            healthPaint.setColor(Color.GREEN);
            float healthWidth = healthBarWidth * soldier.getHealth() / 100f;
            canvas.drawRect(healthBarX, healthBarY, healthBarX + healthWidth, healthBarY + healthBarHeight, healthPaint);
        }
    }
    
    private Drawable getSoldierDrawable(Soldier soldier, int teamColor) {
        String cacheKey = soldier.getId() + "_" + teamColor;
        Drawable cachedDrawable = drawableCache.get(cacheKey);
        
        if (cachedDrawable != null) {
            return cachedDrawable;
        }
        
        Drawable originalDrawable = soldier.getDrawable();
        if (originalDrawable == null) {
            return null;
        }
        
        // Create a mutable copy of the drawable
        Drawable newDrawable = originalDrawable.getConstantState().newDrawable().mutate();
        
        // Tint the drawable with team color
        // For API 21+, you can use setTint directly
        newDrawable.setTint(teamColor);
        
        // Cache the tinted drawable
        drawableCache.put(cacheKey, newDrawable);
        
        return newDrawable;
    }
    
    private void drawAnimations(Canvas canvas) {
        List<AnimationEffect> expiredEffects = new ArrayList<>();
        
        for (AnimationEffect effect : activeAnimations) {
            if (effect.isAlive()) {
                float progress = effect.getProgress();
                float currentRadius = effect.radius * progress;
                int alpha = (int)(255 * (1 - progress));
                Paint effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                effectPaint.setStyle(Paint.Style.STROKE);
                effectPaint.setStrokeWidth(3f);
                effectPaint.setColor(Color.argb(alpha, Color.red(effect.color), Color.green(effect.color), Color.blue(effect.color)));
                
                canvas.drawCircle(effect.x, effect.y, currentRadius, effectPaint);
            } else {
                expiredEffects.add(effect);
            }
        }
        
        // Remove expired effects
        activeAnimations.removeAll(expiredEffects);
    }
    
    public void addAnimationEffect(float x, float y, int color) {
        AnimationEffect effect = new AnimationEffect(x, y, 60f, color, 800);
        activeAnimations.add(effect);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        Log.d(TAG, "onSizeChanged: " + w + "x" + h);
        
        // Recalculate game field when size changes
        calculateGameField();
        
        // Try to initialize teams if not done yet
        if (!teamsInitialized && playerTeam1 != null && enemyTeam != null) {
            initializeTeams();
        }
        
        // Force redraw
        invalidate();
    }
    
    public void pause() {
        isRunning = false;
    }
    
    public void resume() {
        isRunning = true;
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread();
            gameThread.start();
        }
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
            // Post to UI thread to avoid threading issues
            new Handler(Looper.getMainLooper()).post(() -> teamUpdateListener.onTeamUpdate(team));
        }
    }
    
    protected void notifyGameOver(boolean playerWon) {
        if (teamUpdateListener != null) {
            // Post to UI thread to avoid threading issues
            new Handler(Looper.getMainLooper()).post(() -> teamUpdateListener.onGameOver(playerWon));
        }
    }
    
    public boolean contains(float x, float y, Soldier soldier) {
        if (soldier == null) return false;
        
        float distance = (float) Math.sqrt(
                Math.pow(x - soldier.getX(), 2) + 
                Math.pow(y - soldier.getY(), 2));
        
        return distance <= 30; // Soldier hit radius
    }
    
    public boolean contains(float x, float y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }
    
    public Soldier getRandomSoldier(Team team) {
        if (team == null || team.getSoldiers().isEmpty()) {
            return null;
        }
        
        List<Soldier> soldiers = team.getSoldiers();
        if (soldiers.isEmpty()) {
            return null;
        }
        
        int index = (int) (Math.random() * soldiers.size());
        return soldiers.get(index);
    }
    
    public void updateSoldier(Soldier soldier) {
        // Force redraw when soldier state changes
        invalidate();
        
        // Notify listeners of team updates
        if (soldier != null && soldier.getTeam() != null) {
            notifyTeamUpdate(soldier.getTeam());
        }
    }
    
    public void checkGameStatus() {
        // Check if game is over
        if (playerTeam1 != null && enemyTeam != null) {
            int totalPlayerSoldiers = playerTeam1.getSoldierCount();
                                      
            if (totalPlayerSoldiers == 0) {
                notifyGameOver(false); // Player lost
            } else if (enemyTeam.getSoldierCount() == 0) {
                notifyGameOver(true); // Player won
            }
        }
    }
}
