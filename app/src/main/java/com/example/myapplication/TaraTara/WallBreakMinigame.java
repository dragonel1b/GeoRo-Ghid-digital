package com.example.myapplication.TaraTara;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Mini-joc pentru "Spargerea Zidului" în cadrul jocului Țară, Țară, Vrem Ostași
 * Implementează swipe-uri pentru a ghida un cursor printr-un "culoar" îngust între doi apărători
 */
public class WallBreakMinigame extends FrameLayout {

    private static final String TAG = "WallBreakMinigame";
    private static final int CORRIDOR_SEGMENTS = 5; // Numărul de segmente ale culoarului
    private static final float CORRIDOR_WIDTH_MIN = 80; // Lățimea minimă a culoarului (pixeli)
    private static final float CORRIDOR_WIDTH_MAX = 150; // Lățimea maximă a culoarului (pixeli)
    private static final float PLAYER_RADIUS = 30f; // Dimensiunea "ostașului" care încearcă să treacă
    private static final long GAME_DURATION = 5000; // 5 secunde pentru mini-joc
    
    // Culorile pentru elementele vizuale
    private static final int CORRIDOR_COLOR = Color.parseColor("#80CCCCCC");
    private static final int WALL_COLOR = Color.parseColor("#884444FF");
    private static final int PLAYER_COLOR = Color.parseColor("#FFAA44");
    private static final int LINE_COLOR = Color.parseColor("#AAFFFFFF");
    
    // Stări ale jocului
    private enum GameState {
        READY,      // Așteptând începerea
        PLAYING,    // În desfășurare
        SUCCESS,    // Reușit (a trecut prin zid)
        FAILURE     // Eșuat (a atins zidul)
    }
    
    // Variabile pentru starea jocului
    private GameState gameState = GameState.READY;
    private float playerX = 0; // Poziția curentă X a jucătorului
    private float playerY = 0; // Poziția curentă Y a jucătorului
    private float startY = 0; // Poziția de start
    private float endY = 0; // Poziția finală
    private long startTime = 0; // Momentul începerii jocului
    private List<CorridorSegment> corridorSegments = new ArrayList<>();
    private Path corridorPath = new Path();
    private boolean isPlayerTurn = true; // Dacă este rândul jucătorului sau AI
    private float difficulty = 0.5f; // Dificultate între 0 (ușor) și 1 (greu)
    private Consumer<Boolean> onResultListener; // Callback pentru rezultat
    private boolean isActive = false; // Flag pentru a urmări dacă jocul este activ
    private boolean gameStarted = false; // Flag pentru a verifica dacă jocul a început
    
    // Instrumente de desenare și animație
    private Paint playerPaint;
    private Paint corridorPaint;
    private Paint wallPaint;
    private Paint linePaint;
    private Animation shakeAnimation;
    private GestureDetectorCompat gestureDetector;
    private Random random = new Random();
    private Handler gameHandler = new Handler();
    private List<ValueAnimator> brickAnimators = new ArrayList<>();
    
    /**
     * Segment al culoarului cu margini la stânga și dreapta
     */
    private static class CorridorSegment {
        float y; // Poziția Y a segmentului
        float leftX; // Marginea stângă
        float rightX; // Marginea dreaptă
        
        CorridorSegment(float y, float leftX, float rightX) {
            this.y = y;
            this.leftX = leftX;
            this.rightX = rightX;
        }
    }
    
    /**
     * Constructor pentru crearea view-ului din XML
     */
    public WallBreakMinigame(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * Constructor pentru crearea view-ului din cod
     */
    public WallBreakMinigame(Context context) {
        super(context);
        init();
    }
    
    /**
     * Inițializare vizuală și animații
     */
    private void init() {
        // Configurăm stilurile de desenare
        playerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerPaint.setColor(PLAYER_COLOR);
        playerPaint.setStyle(Paint.Style.FILL);
        
        corridorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        corridorPaint.setColor(CORRIDOR_COLOR);
        corridorPaint.setStyle(Paint.Style.FILL);
        
        wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaint.setColor(WALL_COLOR);
        wallPaint.setStyle(Paint.Style.FILL);
        
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(LINE_COLOR);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f);
        
        // Încărcăm animația de "shake" pentru eșec
        try {
            shakeAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        } catch (Exception e) {
            Log.e(TAG, "Nu s-a putut încărca animația: " + e.getMessage());
        }
        
        // Configurăm detectorul de gesturi
        gestureDetector = new GestureDetectorCompat(getContext(), new SwipeGestureListener());
        
        // Setăm listener pentru touch events
        setOnTouchListener((v, event) -> {
            if (gameState == GameState.PLAYING && isPlayerTurn) {
                gestureDetector.onTouchEvent(event);
                
                // Pentru a muta imediat jucătorul la X-ul atins
                if (event.getAction() == MotionEvent.ACTION_DOWN || 
                    event.getAction() == MotionEvent.ACTION_MOVE) {
                    playerX = event.getX();
                    checkCollision();
                    invalidate();
                }
            }
            return true;
        });
    }
    
    /**
     * Pornește mini-jocul
     */
    public void startGame(boolean isPlayerTurn, float difficulty, Consumer<Boolean> resultCallback) {
        this.isPlayerTurn = isPlayerTurn;
        this.difficulty = difficulty;
        this.onResultListener = resultCallback;
        
        // Resetăm starea jocului
        gameState = GameState.PLAYING;
        generateCorridor();
        startTime = System.currentTimeMillis();
        
        // Inițializăm poziția jucătorului
        playerX = getWidth() / 2f;
        playerY = startY;
        
        // Pornim jocul
        postInvalidate();
        
        if (!isPlayerTurn) {
            // Dacă este rândul AI-ului, simulăm un parcurs
            simulateAI();
        }
        
        // Setăm un timer pentru durata max a jocului
        gameHandler.postDelayed(this::timeUp, GAME_DURATION);
    }
    
    /**
     * Generează un coridor aleatoriu cu un traseu sinuos
     */
    private void generateCorridor() {
        corridorSegments.clear();
        corridorPath.reset();
        
        // Dimensiunile ecranului
        int width = getWidth();
        int height = getHeight();
        
        // Poziția de start și final
        startY = height * 0.1f;
        endY = height * 0.9f;
        
        float segmentHeight = (endY - startY) / CORRIDOR_SEGMENTS;
        float centerX = width / 2f;
        float corridorHalfWidth;
        
        // Generăm segmentele de coridor cu lățimi variabile
        for (int i = 0; i <= CORRIDOR_SEGMENTS; i++) {
            float segmentY = startY + i * segmentHeight;
            
            // Variăm lățimea bazată pe dificultate
            corridorHalfWidth = lerp(
                CORRIDOR_WIDTH_MAX / 2, 
                CORRIDOR_WIDTH_MIN / 2, 
                difficulty + random.nextFloat() * 0.3f
            );
            
            // Variăm poziția orizontală pentru a crea un traseu sinuos
            float offset = (random.nextFloat() * width * 0.5f) - width * 0.25f;
            float segmentCenterX = centerX + offset;
            
            // Ajustăm ca să nu iasă din ecran
            segmentCenterX = Math.max(corridorHalfWidth, Math.min(width - corridorHalfWidth, segmentCenterX));
            
            corridorSegments.add(new CorridorSegment(
                segmentY, 
                segmentCenterX - corridorHalfWidth,
                segmentCenterX + corridorHalfWidth
            ));
        }
        
        // Construim path-ul pentru desen
        buildCorridorPath();
    }
    
    /**
     * Construiește path-ul pentru coridor bazat pe segmente
     */
    private void buildCorridorPath() {
        if (corridorSegments.isEmpty()) return;
        
        corridorPath.reset();
        
        // Latura stângă
        CorridorSegment first = corridorSegments.get(0);
        corridorPath.moveTo(first.leftX, first.y);
        
        for (int i = 1; i < corridorSegments.size(); i++) {
            CorridorSegment segment = corridorSegments.get(i);
            corridorPath.lineTo(segment.leftX, segment.y);
        }
        
        // Latura dreaptă (în sens invers)
        for (int i = corridorSegments.size() - 1; i >= 0; i--) {
            CorridorSegment segment = corridorSegments.get(i);
            corridorPath.lineTo(segment.rightX, segment.y);
        }
        
        corridorPath.close();
    }
    
    /**
     * Verifică ciocnirile cu pereții coridorului
     */
    private void checkCollision() {
        if (gameState != GameState.PLAYING) return;
        
        // Găsim segmentele coridorului deasupra și sub jucător
        CorridorSegment above = null;
        CorridorSegment below = null;
        
        for (CorridorSegment segment : corridorSegments) {
            if (segment.y <= playerY) {
                above = segment;
            }
            if (segment.y >= playerY && below == null) {
                below = segment;
            }
        }
        
        // Dacă nu găsim două segmente pentru interpolare, nu putem verifica
        if (above == null || below == null) return;
        
        // Interpolăm limitele coridorului la Y-ul actual
        float ratio = 0;
        if (below.y != above.y) {
            ratio = (playerY - above.y) / (below.y - above.y);
        }
        
        float leftBound = lerp(above.leftX, below.leftX, ratio);
        float rightBound = lerp(above.rightX, below.rightX, ratio);
        
        // Verificăm dacă jucătorul e în afara coridorului
        if (playerX - PLAYER_RADIUS < leftBound || playerX + PLAYER_RADIUS > rightBound) {
            // Coliziune cu peretele - eșec
            handleResult(false);
        }
        
        // Verificăm dacă jucătorul a ajuns la final
        if (playerY >= endY - PLAYER_RADIUS) {
            // Succes - a parcurs tot culoarul
            handleResult(true);
        }
    }
    
    /**
     * Simulează comportamentul AI-ului
     */
    private void simulateAI() {
        // Implementare basată pe dificultate
        // La dificultate mică, AI-ul va reuși mai des
        float successChance = 1.0f - difficulty;
        
        gameHandler.postDelayed(() -> {
            // Simulează un parcurs al AI-ului
            animateAIPath(() -> {
                boolean aiSucceeds = random.nextFloat() < successChance;
                handleResult(aiSucceeds);
            });
        }, 1000); // Delay pentru a arăta că AI-ul "se gândește"
    }
    
    /**
     * Animează parcursul AI-ului
     */
    private void animateAIPath(Runnable onComplete) {
        playerY = startY;
        
        // Calculăm un drum prin coridor
        List<Float> aiPathX = new ArrayList<>();
        
        for (CorridorSegment segment : corridorSegments) {
            // Calculăm un X între marginile coridorului, cu variații bazate pe dificultate
            float centerX = (segment.leftX + segment.rightX) / 2;
            float maxOffset = (segment.rightX - segment.leftX) * 0.4f;
            
            // La dificultate mare, AI-ul merge mai aproape de margini
            float offset = maxOffset * random.nextFloat() * difficulty;
            if (random.nextBoolean()) offset = -offset;
            
            aiPathX.add(centerX + offset);
        }
        
        // Animăm mișcarea AI-ului prin coridor
        final int[] pathIndex = {0};
        final long stepDuration = GAME_DURATION / corridorSegments.size();
        
        Runnable animateStep = new Runnable() {
            @Override
            public void run() {
                if (pathIndex[0] >= corridorSegments.size()) {
                    // Am ajuns la final
                    if (onComplete != null) onComplete.run();
                    return;
                }
                
                // Actualizăm poziția
                CorridorSegment segment = corridorSegments.get(pathIndex[0]);
                playerY = segment.y;
                playerX = aiPathX.get(pathIndex[0]);
                
                invalidate();
                pathIndex[0]++;
                
                // Programăm următorul pas
                gameHandler.postDelayed(this, stepDuration);
            }
        };
        
        // Începem animația
        gameHandler.post(animateStep);
    }
    
    /**
     * Se apelează când timpul a expirat
     */
    private void timeUp() {
        if (gameState == GameState.PLAYING) {
            handleResult(false); // Eșec dacă s-a terminat timpul
        }
    }
    
    /**
     * Gestionează rezultatul mini-jocului
     */
    private void handleResult(boolean success) {
        if (gameState != GameState.PLAYING) return;
        
        gameState = success ? GameState.SUCCESS : GameState.FAILURE;
        
        // Oprim handler-ul pentru a preveni executări întârziate
        gameHandler.removeCallbacksAndMessages(null);
        
        // Efecte vizuale în funcție de rezultat
        if (success) {
            // Animație de succes
            playerPaint.setColor(Color.GREEN);
        } else {
            // Animație de eșec
            playerPaint.setColor(Color.RED);
            if (shakeAnimation != null) {
                startAnimation(shakeAnimation);
            }
        }
        invalidate();
        
        // Notificăm listener-ul cu rezultatul
        gameHandler.postDelayed(() -> {
            if (onResultListener != null) {
                onResultListener.accept(success);
            }
        }, 1000); // Dăm timp să se vadă rezultatul
    }
    
    /**
     * Actualizează poziția jucătorului când se face swipe
     */
    private void updatePlayerPosition(float deltaX, float deltaY) {
        if (gameState != GameState.PLAYING) return;
        
        // Actualizăm poziția pe axa X doar dacă jucătorul controlează
        if (isPlayerTurn) {
            playerX += deltaX;
            // Limităm la ecran
            playerX = Math.max(PLAYER_RADIUS, Math.min(getWidth() - PLAYER_RADIUS, playerX));
        }
        
        // Actualizăm poziția Y indiferent, dar controlat
        playerY += Math.abs(deltaY) * 0.5f; // Înaintăm mai încet decât swipe-ul
        playerY = Math.min(endY, playerY); // Nu depășim limita
        
        checkCollision();
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Desenăm fundalul (zidul)
        canvas.drawRect(0, 0, getWidth(), getHeight(), wallPaint);
        
        // Desenăm coridorul
        canvas.drawPath(corridorPath, corridorPaint);
        
        // Desenăm liniile coridorului
        for (int i = 0; i < corridorSegments.size() - 1; i++) {
            CorridorSegment current = corridorSegments.get(i);
            CorridorSegment next = corridorSegments.get(i + 1);
            
            canvas.drawLine(current.leftX, current.y, next.leftX, next.y, linePaint);
            canvas.drawLine(current.rightX, current.y, next.rightX, next.y, linePaint);
        }
        
        // Desenăm jucătorul (ostașul)
        canvas.drawCircle(playerX, playerY, PLAYER_RADIUS, playerPaint);
        
        // Desenăm liniile de start și finish
        canvas.drawLine(0, startY, getWidth(), startY, linePaint);
        canvas.drawLine(0, endY, getWidth(), endY, linePaint);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Regenerăm coridorul la schimbarea dimensiunii
        if (w > 0 && h > 0 && gameState == GameState.PLAYING) {
            generateCorridor();
        }
    }
    
    /**
     * Interpolează linear între două valori
     */
    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }
    
    /**
     * Detector de gesturi swipe
     */
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;
        
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (gameState == GameState.PLAYING && isPlayerTurn) {
                updatePlayerPosition(-distanceX, -distanceY);
                return true;
            }
            return false;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (gameState != GameState.PLAYING || !isPlayerTurn) return false;
            
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            
            // Dacă mișcarea e în principal în jos (pentru "spargerea" zidului)
            if (Math.abs(diffY) > Math.abs(diffX) && 
                diffY > SWIPE_THRESHOLD && 
                Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                
                // Aplică o mișcare proporțională cu intensitatea swipe-ului
                float speedFactor = Math.min(Math.abs(velocityY) / 2000, 1.5f);
                updatePlayerPosition(diffX * 0.2f, diffY * speedFactor);
                return true;
            }
            return false;
        }
    }

    /**
     * Oprește jocul și eliberează resursele
     */
    public void stop() {
        // Stop any running animations
        if (brickAnimators != null) {
            for (ValueAnimator animator : brickAnimators) {
                if (animator != null && animator.isRunning()) {
                    animator.cancel();
                }
            }
            brickAnimators.clear();
        }
        
        // Reset state
        isActive = false;
        gameStarted = false;
        
        // Clear handlers
        if (gameHandler != null) {
            gameHandler.removeCallbacksAndMessages(null);
        }
        
        // Reset UI
        invalidate();
    }
} 