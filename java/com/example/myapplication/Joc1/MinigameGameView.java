package com.example.myapplication.Joc1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.graphics.DashPathEffect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinigameGameView extends SurfaceView implements SurfaceHolder.Callback {
    // Game object classes
    private class Player {
        public static final float SIZE = 60f;
        public float x, y;
        private float velocityX, velocityY;
        private static final float SPEED = 5f;

        public void update() {
            x += velocityX * SPEED;
            y += velocityY * SPEED;

            // Keep player within bounds
            x = Math.max(0, Math.min(x, screenWidth - SIZE));
            y = Math.max(0, Math.min(y, screenHeight - SIZE));
        }

        public void setVelocity(float vx, float vy) {
            velocityX = vx;
            velocityY = vy;
        }

        public void stop() {
            velocityX = 0;
            velocityY = 0;
        }

        public RectF getBounds() {
            return new RectF(x, y, x + SIZE, y + SIZE);
        }
    }

    private class CollectibleItem {
        public static final float SIZE = 40f;
        public float x, y;

        public CollectibleItem(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public RectF getBounds() {
            return new RectF(x, y, x + SIZE, y + SIZE);
        }
    }

    // Interface for accessing player data
    public static interface PlayerData {
        float getX();
        float getY();
        RectF getBounds();
    }

    private class GameThread extends Thread {
        private static final String TAG = "GameThread";
        private final SurfaceHolder surfaceHolder;
        private final MinigameGameView gameView;
        private boolean running = false;
        private final long targetFPS = 60;
        private final long targetFrameTime = 1000 / targetFPS;

        public GameThread(SurfaceHolder holder, MinigameGameView view) {
            this.surfaceHolder = holder;
            this.gameView = view;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            long startTime;
            long timeMillis;
            long waitTime;
            long frameCount = 0;
            long totalTime = 0;
            long targetTime = targetFrameTime;

            while (running) {
                startTime = System.nanoTime();
                Canvas canvas = null;

                try {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        synchronized (surfaceHolder) {
                            // Update game state
                            updateGameState();
                            // Draw frame
                            gameView.draw(canvas);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in game loop: " + e.getMessage());
                } finally {
                    if (canvas != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            Log.e(TAG, "Error unlocking canvas: " + e.getMessage());
                        }
                    }
                }

                timeMillis = (System.nanoTime() - startTime) / 1000000;
                waitTime = targetTime - timeMillis;

                try {
                    if (waitTime > 0) {
                        sleep(waitTime);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in thread sleep: " + e.getMessage());
                }

                totalTime += System.nanoTime() - startTime;
                frameCount++;

                if (frameCount == targetFPS) {
                    frameCount = 0;
                    totalTime = 0;
                }
            }
        }
    }

    private GameThread gameThread;
    private Paint paint;
    private Random random;
    private Drawable traditionalItemDrawable;
    private Drawable playerCharacterDrawable;
    private Drawable backgroundDrawable;
    private Drawable romaniaMapDrawable;

    // Game objects
    private Player player;
    private List<CollectibleItem> items;
    private List<NPC> npcs;
    private Mission currentMission;
    private int currentLevel = 1;
    private int score = 0;
    // Listener interfaces
    public interface OnScoreChangeListener {
        void onScoreChanged(int newScore);
    }

    public interface OnMissionChangeListener {
        void onMissionChanged(String newMission);
    }

    public interface OnLevelChangeListener {
        void onLevelChanged(int newLevel);
    }

    public interface OnNPCInteractionListener {
        void onNPCNearby(NPC npc);
    }

    private OnScoreChangeListener scoreListener;
    private OnMissionChangeListener missionListener;
    private OnLevelChangeListener levelListener;
    private OnNPCInteractionListener npcInteractionListener;

    // Setter methods for listeners
    public void setOnScoreChangeListener(OnScoreChangeListener listener) {
        this.scoreListener = listener;
    }

    public void setOnMissionChangeListener(OnMissionChangeListener listener) {
        this.missionListener = listener;
    }

    public void setOnLevelChangeListener(OnLevelChangeListener listener) {
        this.levelListener = listener;
    }

    public void setOnNPCInteractionListener(OnNPCInteractionListener listener) {
        this.npcInteractionListener = listener;
    }

    // Arrow guidance
    private Paint arrowPaint;
    private float[] arrowPath;

    private void drawGuidanceArrow(Canvas canvas) {
        if (currentMission == null || items.isEmpty()) return;

        // Find nearest collectible item
        float playerCenterX = player.x + Player.SIZE/2;
        float playerCenterY = player.y + Player.SIZE/2;
        float minDistance = Float.MAX_VALUE;
        CollectibleItem nearestItem = null;

        for (CollectibleItem item : items) {
            float distance = calculateDistance(
                    playerCenterX,
                    playerCenterY,
                    item.x + CollectibleItem.SIZE/2,
                    item.y + CollectibleItem.SIZE/2
            );
            if (distance < minDistance) {
                minDistance = distance;
                nearestItem = item;
            }
        }

        if (nearestItem != null) {
            // Calculate arrow direction
            float dx = (nearestItem.x + CollectibleItem.SIZE/2) - playerCenterX;
            float dy = (nearestItem.y + CollectibleItem.SIZE/2) - playerCenterY;
            float angle = (float) Math.atan2(dy, dx);

            // Draw arrow line
            float arrowLength = 100;
            float endX = playerCenterX + (float) Math.cos(angle) * arrowLength;
            float endY = playerCenterY + (float) Math.sin(angle) * arrowLength;

            canvas.drawLine(playerCenterX, playerCenterY, endX, endY, arrowPaint);

            // Draw arrow head
            float arrowHeadLength = 20;
            float arrowHeadAngle = 0.5f;
            double angle1 = angle + Math.PI - arrowHeadAngle;
            double angle2 = angle + Math.PI + arrowHeadAngle;

            float arrowHead1X = endX + arrowHeadLength * (float) Math.cos(angle1);
            float arrowHead1Y = endY + arrowHeadLength * (float) Math.sin(angle1);
            float arrowHead2X = endX + arrowHeadLength * (float) Math.cos(angle2);
            float arrowHead2Y = endY + arrowHeadLength * (float) Math.sin(angle2);

            canvas.drawLine(endX, endY, arrowHead1X, arrowHead1Y, arrowPaint);
            canvas.drawLine(endX, endY, arrowHead2X, arrowHead2Y, arrowPaint);
        }
    }

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    // Background elements
    private class BackgroundElement {
        float x, y;
        float alpha;
        float scale;
        float velocity;
        float angle;

        public BackgroundElement(float x, float y) {
            this.x = x;
            this.y = y;
            this.alpha = random.nextFloat() * 0.5f + 0.2f; // 0.2 to 0.7
            this.scale = random.nextFloat() * 0.5f + 0.5f; // 0.5 to 1.0
            this.velocity = random.nextFloat() * 0.5f + 0.5f; // 0.5 to 1.0
            this.angle = random.nextFloat() * (float)Math.PI * 2;
        }

        public void update() {
            // Move in a circular pattern
            float radius = 10 * scale;
            x += Math.cos(angle) * velocity;
            y += Math.sin(angle) * velocity;
            angle += 0.02f;

            // Wrap around screen edges
            if (x < -radius) x = screenWidth + radius;
            if (x > screenWidth + radius) x = -radius;
            if (y < -radius) y = screenHeight + radius;
            if (y > screenHeight + radius) y = -radius;

            // Pulse alpha
            alpha = 0.2f + (float)(Math.sin(angle) + 1) * 0.25f; // 0.2 to 0.7
        }
    }

    private List<BackgroundElement> backgroundElements;

    private void createBackgroundElements() {
        backgroundElements.clear();
        int numElements = 20;

        // Create decorative elements at fixed positions
        float[] positions = {
                0.1f, 0.1f,    // Top-left
                0.1f, 0.9f,    // Bottom-left
                0.9f, 0.1f,    // Top-right
                0.9f, 0.9f,    // Bottom-right
                0.5f, 0.5f     // Center
        };

        for (int i = 0; i < positions.length; i += 2) {
            float x = positions[i] * screenWidth;
            float y = positions[i + 1] * screenHeight;
            backgroundElements.add(new BackgroundElement(x, y));
        }

        // Add random elements
        for (int i = 0; i < numElements - (positions.length / 2); i++) {
            float x = random.nextFloat() * screenWidth;
            float y = random.nextFloat() * screenHeight;
            backgroundElements.add(new BackgroundElement(x, y));
        }
    }

    public MinigameGameView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        getHolder().addCallback(this);
        init();
    }

    public MinigameGameView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        getHolder().addCallback(this);
        init();
    }

    public MinigameGameView(Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        getHolder().addCallback(this);
        init();
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private boolean checkCollision(RectF rect1, RectF rect2) {
        return RectF.intersects(rect1, rect2);
    }

    private void updateGameState() {
        // Update background elements
        for (BackgroundElement element : backgroundElements) {
            element.update();
        }

        // Update player position based on movement
        if (player != null) {
            player.update();
        }

        // Check collisions with items
        if (items != null) {
            for (CollectibleItem item : new ArrayList<>(items)) {
                if (checkCollision(player.getBounds(), item.getBounds())) {
                    items.remove(item);
                    score += 10;
                    if (scoreListener != null) {
                        scoreListener.onScoreChanged(score);
                    }

                    // Update mission progress
                    if (currentMission != null && !currentMission.isCompleted()) {
                        currentMission.incrementCollectedItems();
                        if (missionListener != null) {
                            missionListener.onMissionChanged(currentMission.getDescription());
                        }

                        // Check if mission is completed
                        if (currentMission.checkCompletion(new PlayerData() {
                            @Override
                            public float getX() {
                                return player.x;
                            }

                            @Override
                            public float getY() {
                                return player.y;
                            }

                            @Override
                            public RectF getBounds() {
                                return player.getBounds();
                            }
                        })) {
                            // Level up and create new mission
                            currentLevel++;
                            if (levelListener != null) {
                                levelListener.onLevelChanged(currentLevel);
                            }
                            createItems();
                            createInitialMission();
                        }
                    }
                }
            }
        }

        // Check NPC interactions
        if (npcs != null) {
            for (NPC npc : npcs) {
                if (!npc.isInteracted()) {
                    RectF npcBounds = npc.getBounds();
                    float distance = calculateDistance(
                            player.x + Player.SIZE/2,
                            player.y + Player.SIZE/2,
                            npcBounds.centerX(),
                            npcBounds.centerY()
                    );
                    if (distance < 100) { // Interaction radius
                        if (npcInteractionListener != null) {
                            npcInteractionListener.onNPCNearby(npc);
                        }

                        // Check if this NPC has a quest
                        Mission npcQuest = npc.getNpcQuest();
                        if (npcQuest != null && !npcQuest.isCompleted()) {
                            if (npcQuest.checkCompletion(new PlayerData() {
                                @Override
                                public float getX() { return player.x; }
                                @Override
                                public float getY() { return player.y; }
                                @Override
                                public RectF getBounds() { return player.getBounds(); }
                            })) {
                                // Quest completed
                                score += npcQuest.getRewardPoints();
                                if (scoreListener != null) {
                                    scoreListener.onScoreChanged(score);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void movePlayer(float dx, float dy) {
        if (player != null) {
            player.setVelocity(dx, dy);
        }
    }

    public void stopPlayer() {
        if (player != null) {
            player.stop();
        }
    }

    private void createItems() {
        items.clear();
        int numItems = 5 + currentLevel * 2; // More items per level
        for (int i = 0; i < numItems; i++) {
            float x = random.nextFloat() * (screenWidth - CollectibleItem.SIZE);
            float y = random.nextFloat() * (screenHeight - CollectibleItem.SIZE);
            items.add(new CollectibleItem(x, y));
        }
    }

    private void createNPCs() {
        npcs.clear();

        // Create NPCs with proper drawable and color filter
        Drawable npcDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_player_character);
        if (npcDrawable != null) {
            // Add Guide NPC
            NPC guide = new NPC(
                    screenWidth * 0.2f,
                    screenHeight * 0.2f,
                    "Guide",
                    npcDrawable,
                    Color.BLUE
            );
            guide.addDialogue("Welcome to the game! Collect traditional items to complete missions.");
            guide.setNpcQuest(new Mission("Meet the Guide", 1, 10, Mission.MissionType.INTERACT_NPC));
            npcs.add(guide);

            // Add Merchant NPC
            NPC merchant = new NPC(
                    screenWidth * 0.8f,
                    screenHeight * 0.8f,
                    "Merchant",
                    npcDrawable,
                    Color.GREEN
            );
            merchant.addDialogue("I have some special items for trade!");
            merchant.setNpcQuest(new Mission("Trade Items", 5, 20, Mission.MissionType.COLLECT_ITEMS));
            npcs.add(merchant);
        }
    }

    private void createInitialMission() {
        currentMission = new Mission("Collect Traditional Items", 3, 10, Mission.MissionType.COLLECT_ITEMS);
        if (missionListener != null) {
            missionListener.onMissionChanged(currentMission.getDescription());
        }
    }

    private void init() {
        try {
            // Initialize collections
            paint = new Paint();
            random = new Random();
            items = new ArrayList<>();
            npcs = new ArrayList<>();
            backgroundElements = new ArrayList<>();

            // Initialize arrow paint
            arrowPaint = new Paint();
            arrowPaint.setColor(Color.YELLOW);
            arrowPaint.setStyle(Paint.Style.STROKE);
            arrowPaint.setStrokeWidth(5);
            arrowPaint.setPathEffect(new DashPathEffect(new float[] {10, 20}, 0));
            arrowPath = new float[8];

            // Load drawables
            traditionalItemDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_traditional_item);
            playerCharacterDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_player_character);
            backgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.minigame_background);
            romaniaMapDrawable = ContextCompat.getDrawable(getContext(), R.drawable.romania_map);

            if (traditionalItemDrawable == null || playerCharacterDrawable == null ||
                    backgroundDrawable == null || romaniaMapDrawable == null) {
                throw new IllegalStateException("Failed to load one or more drawables");
            }

            // Initialize game objects
            player = new Player();
            player.x = screenWidth / 2 - Player.SIZE / 2;
            player.y = screenHeight / 2 - Player.SIZE / 2;

            // Initialize game state
            score = 0;
            currentLevel = 1;

            // Create game elements
            createItems();
            createBackgroundElements();
            createNPCs();
            createInitialMission();

            // Notify initial state
            if (scoreListener != null) {
                scoreListener.onScoreChanged(score);
            }
            if (levelListener != null) {
                levelListener.onLevelChanged(currentLevel);
            }
        } catch (Exception e) {
            Log.e("MinigameGameView", "Error initializing game: " + e.getMessage());
            paint.setColor(Color.parseColor("#FFF5E6")); // Fallback color
            throw new RuntimeException("Failed to initialize game", e);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        // Initialize player position
        player.x = screenWidth / 2 - Player.SIZE / 2;
        player.y = screenHeight / 2 - Player.SIZE / 2;

        // Set up background
        if (backgroundDrawable != null) {
            backgroundDrawable.setBounds(0, 0, screenWidth, screenHeight);
        }
        if (romaniaMapDrawable != null) {
            romaniaMapDrawable.setBounds(0, 0, screenWidth, screenHeight);
        }

        // Create background elements and NPCs
        createBackgroundElements();
        createNPCs();

        // Create and start game thread
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread(holder, this);
            gameThread.setRunning(true);
            gameThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;

        // Update drawable bounds
        if (backgroundDrawable != null) {
            backgroundDrawable.setBounds(0, 0, width, height);
        }
        if (romaniaMapDrawable != null) {
            romaniaMapDrawable.setBounds(0, 0, width, height);
        }

        // Recreate elements for new dimensions
        createBackgroundElements();
        createNPCs();
        createItems();

        // Center player
        if (player != null) {
            player.x = screenWidth / 2 - Player.SIZE / 2;
            player.y = screenHeight / 2 - Player.SIZE / 2;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Stop game thread
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                Log.e("MinigameGameView", "Error stopping game thread: " + e.getMessage());
            }
        }

        // Clean up resources
        if (backgroundDrawable != null) {
            backgroundDrawable.setCallback(null);
        }
        if (romaniaMapDrawable != null) {
            romaniaMapDrawable.setCallback(null);
        }
        if (traditionalItemDrawable != null) {
            traditionalItemDrawable.setCallback(null);
        }
        if (playerCharacterDrawable != null) {
            playerCharacterDrawable.setCallback(null);
        }

        // Clear collections
        items.clear();
        npcs.clear();
        backgroundElements.clear();
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas == null) return;

        try {
            super.draw(canvas);

            // Draw base background
            if (backgroundDrawable != null) {
                backgroundDrawable.draw(canvas);
            } else {
                canvas.drawColor(Color.parseColor("#FFF5E6"));
            }

            // Draw background elements
            Paint elementPaint = new Paint();
            elementPaint.setStyle(Paint.Style.FILL);
            elementPaint.setColor(Color.WHITE);

            for (BackgroundElement element : backgroundElements) {
                elementPaint.setAlpha((int)(element.alpha * 255));
                float size = 20 * element.scale;
                canvas.drawCircle(element.x, element.y, size, elementPaint);
            }

            // Draw Romania map with transparency
            if (romaniaMapDrawable != null) {
                Paint alphaPaint = new Paint();
                alphaPaint.setAlpha(128);
                int saveCount = canvas.save();
                romaniaMapDrawable.setAlpha(128);
                romaniaMapDrawable.draw(canvas);
                romaniaMapDrawable.setAlpha(255);
                canvas.restoreToCount(saveCount);
            }

            // Draw NPCs
            for (NPC npc : npcs) {
                npc.render(canvas);
            }

            // Draw guidance arrow
            if (currentMission != null && !currentMission.isCompleted()) {
                drawGuidanceArrow(canvas);
            }

            // Draw player
            if (playerCharacterDrawable != null) {
                playerCharacterDrawable.setBounds(
                        (int)player.x,
                        (int)player.y,
                        (int)(player.x + Player.SIZE),
                        (int)(player.y + Player.SIZE)
                );
                playerCharacterDrawable.draw(canvas);
            }

            // Draw collectible items
            if (traditionalItemDrawable != null) {
                for (CollectibleItem item : items) {
                    traditionalItemDrawable.setBounds(
                            (int)item.x,
                            (int)item.y,
                            (int)(item.x + CollectibleItem.SIZE),
                            (int)(item.y + CollectibleItem.SIZE)
                    );
                    traditionalItemDrawable.draw(canvas);
                }
            }
        } catch (Exception e) {
            Log.e("MinigameGameView", "Error during draw: " + e.getMessage());
        }
    }

    // Rest of the class implementation remains the same...
    // (Player class, CollectibleItem class, BackgroundElement class, etc.)
}
