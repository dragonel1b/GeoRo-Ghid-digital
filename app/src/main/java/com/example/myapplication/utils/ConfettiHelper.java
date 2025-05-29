package com.example.myapplication.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A helper class to display beautiful confetti animations on achievement completion
 */
public class ConfettiHelper {
    
    private static final int DEFAULT_DURATION = 3000; // 3 seconds for a more satisfying effect
    private static final int DEFAULT_PIECES = 150; // More pieces for denser effect
    
    // Enhanced color palette with Romanian flag colors and more vibrant colors
    private static final int[] COLORS = {
            Color.parseColor("#002B7F"), // Romanian blue
            Color.parseColor("#FCD116"), // Romanian yellow
            Color.parseColor("#CE1126"), // Romanian red
            Color.parseColor("#FF1493"), // Deep pink
            Color.parseColor("#00BFFF"), // Deep sky blue
            Color.parseColor("#7CFC00"), // Lawn green
            Color.parseColor("#FF8C00"), // Dark orange
            Color.parseColor("#9932CC"), // Dark orchid
            Color.parseColor("#00FF7F")  // Spring green
    };
    
    /**
     * Shows a confetti animation in the given parent view
     * 
     * @param context The context
     * @param parent The parent view to add the confetti to
     */
    public static void showConfetti(Context context, ViewGroup parent) {
        showConfetti(context, parent, DEFAULT_PIECES, DEFAULT_DURATION);
    }
    
    /**
     * Shows a confetti explosion from the center of the screen
     */
    public static void showCenterExplosion(Context context, ViewGroup parent) {
        EnhancedConfettiView confettiView = new EnhancedConfettiView(context, DEFAULT_PIECES);
        confettiView.setCenterExplosion(true);
        
        // Add confetti view to parent
        parent.addView(confettiView);
        
        // Start animation
        confettiView.startAnimation(DEFAULT_DURATION);
    }
    
    /**
     * Shows confetti raining from the top of the screen
     */
    public static void showConfettiRain(Context context, ViewGroup parent) {
        EnhancedConfettiView confettiView = new EnhancedConfettiView(context, DEFAULT_PIECES * 2);
        confettiView.setRainMode(true);
        
        // Add confetti view to parent
        parent.addView(confettiView);
        
        // Start animation with longer duration for rain effect
        confettiView.startAnimation(DEFAULT_DURATION * 2);
    }
    
    /**
     * Shows a confetti animation in the given parent view with custom parameters
     * 
     * @param context The context
     * @param parent The parent view to add the confetti to
     * @param pieces Number of confetti pieces
     * @param duration Duration of the animation in milliseconds
     */
    public static void showConfetti(Context context, ViewGroup parent, int pieces, int duration) {
        EnhancedConfettiView confettiView = new EnhancedConfettiView(context, pieces);
        
        // Add confetti view to parent
        parent.addView(confettiView);
        
        // Start animation
        confettiView.startAnimation(duration);
    }
    
    /**
     * The enhanced view that draws the confetti with improved physics and visuals
     */
    private static class EnhancedConfettiView extends View {
        private final List<ConfettiPiece> confettiPieces = new ArrayList<>();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random random = new Random();
        private final int width;
        private final int height;
        private boolean centerExplosion = false;
        private boolean rainMode = false;
        
        public EnhancedConfettiView(Context context, int pieces) {
            super(context);
            
            width = context.getResources().getDisplayMetrics().widthPixels;
            height = context.getResources().getDisplayMetrics().heightPixels;
            
            // Set view attributes
            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            setZ(1000); // Ensure it's drawn on top
        }
        
        public void setCenterExplosion(boolean centerExplosion) {
            this.centerExplosion = centerExplosion;
        }
        
        public void setRainMode(boolean rainMode) {
            this.rainMode = rainMode;
        }
        
        private void initializeConfetti(int pieces) {
            confettiPieces.clear();
            
            // Initialize confetti pieces
            for (int i = 0; i < pieces; i++) {
                confettiPieces.add(createConfettiPiece());
            }
        }
        
        private ConfettiPiece createConfettiPiece() {
            float x, y, speedX, speedY;
            
            if (centerExplosion) {
                // For explosion, start all pieces from center
                x = width / 2f;
                y = height / 2f;
                
                // Random direction for explosion
                double angle = random.nextDouble() * Math.PI * 2; // 0 to 2π
                float speed = 10f + random.nextFloat() * 15f;
                speedX = (float) (Math.cos(angle) * speed);
                speedY = (float) (Math.sin(angle) * speed);
            } else if (rainMode) {
                // For rain, start from top at random positions
                x = random.nextFloat() * width;
                y = -random.nextInt(height) - 20; // Start above screen with varied heights
                
                // Mostly downward, with slight side movement
                speedX = -2f + random.nextFloat() * 4f;
                speedY = 3f + random.nextFloat() * 7f;
            } else {
                // Standard confetti - start from top
                x = random.nextFloat() * width;
                y = -random.nextInt(height / 3) - 20; // Start above the screen with variation
                
                // Random speeds
                speedX = -7f + random.nextFloat() * 14f; // More horizontal variety
                speedY = 7f + random.nextFloat() * 8f; // Falling speed
            }
            
            // Common properties
            float size = 10f + random.nextFloat() * 25f; // Size between 10 and 35
            float alpha = 0.6f + random.nextFloat() * 0.4f; // Semi-transparency
            int color = COLORS[random.nextInt(COLORS.length)];
            
            // Apply alpha to color
            int alphaInt = (int) (alpha * 255);
            color = Color.argb(alphaInt, Color.red(color), Color.green(color), Color.blue(color));
            
            float rotation = random.nextFloat() * 360;
            float rotationSpeed = -9f + random.nextFloat() * 18f;
            
            // Create different shapes with more variety
            int type = random.nextInt(5); // Increased to 5 types
            
            return new ConfettiPiece(x, y, size, color, rotation, speedX, speedY, rotationSpeed, type);
        }
        
        public void startAnimation(int duration) {
            // Initialize confetti on animation start
            initializeConfetti(confettiPieces.size());
            
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(duration);
            
            if (rainMode) {
                animator.setInterpolator(new LinearInterpolator());
            } else {
                animator.setInterpolator(new DecelerateInterpolator(0.8f));
            }
            
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                
                // Fade out near the end
                float alpha = value < 0.8f ? 1.0f : (1.0f - (value - 0.8f) * 5f);
                
                // Move confetti with improved physics
                for (ConfettiPiece piece : confettiPieces) {
                    // Set piece alpha for fade-out
                    piece.alpha = alpha;
                    
                    if (centerExplosion) {
                        // Slow down explosion over time
                        piece.speedX *= 0.98f;
                        piece.speedY *= 0.98f;
                        
                        // Add gravity
                        piece.speedY += 0.2f;
                    } else {
                        // Standard gravity
                        piece.speedY += 0.15f; 
                        
                        // Air resistance
                        piece.speedX *= 0.99f;
                        
                        // Maximum speed cap
                        if (piece.speedY > 15) {
                            piece.speedY = 15;
                        }
                    }
                    
                    // Update positions
                    piece.x += piece.speedX;
                    piece.y += piece.speedY;
                    
                    // More realistic rotation (affected by speed)
                    float rotationFactor = piece.type == 1 ? 0.8f : 1.5f; // Rectangles rotate slower
                    piece.rotation += piece.rotationSpeed * rotationFactor;
                    
                    // Add oscillation for a more natural floating effect
                    piece.x += Math.sin(piece.y / 40 + piece.offset) * (0.5f + piece.size / 30f);
                }
                
                // Redraw view
                invalidate();
            });
            
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Remove view after animation
                    post(() -> {
                        ViewGroup parent = (ViewGroup) getParent();
                        if (parent != null) {
                            parent.removeView(EnhancedConfettiView.this);
                        }
                    });
                }
            });
            
            animator.start();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            // Draw each piece
            for (ConfettiPiece piece : confettiPieces) {
                // Skip if out of bounds
                if (piece.y > height + 100 || piece.x < -100 || piece.x > width + 100) {
                    continue;
                }
                
                // Apply fade-out
                paint.setAlpha((int) (255 * piece.alpha));
                
                // Save canvas state
                canvas.save();
                
                // Move to piece position and rotate
                canvas.translate(piece.x, piece.y);
                canvas.rotate(piece.rotation);
                
                // Set paint color
                paint.setColor(piece.color);
                paint.setStyle(Paint.Style.FILL);
                
                // Draw shape based on type with improved rendering
                drawConfettiShape(canvas, piece, paint);
                
                // Restore canvas
                canvas.restore();
            }
        }
        
        private void drawConfettiShape(Canvas canvas, ConfettiPiece piece, Paint paint) {
            switch (piece.type) {
                case 0: // Circle
                    canvas.drawCircle(0, 0, piece.size / 2, paint);
                    break;
                    
                case 1: // Rectangle
                    float rectWidth = piece.size;
                    float rectHeight = piece.size / 2.5f;
                    canvas.drawRect(-rectWidth / 2, -rectHeight / 2, 
                                   rectWidth / 2, rectHeight / 2, paint);
                    
                    // Add shine effect
                    paint.setColor(Color.WHITE);
                    paint.setAlpha(60);
                    canvas.drawLine(-rectWidth / 3, -rectHeight / 3, 
                                   rectWidth / 3, rectHeight / 3, paint);
                    paint.setColor(piece.color);
                    break;
                    
                case 2: // Triangle
                    Path trianglePath = new Path();
                    trianglePath.moveTo(0, -piece.size / 2);
                    trianglePath.lineTo(-piece.size / 2, piece.size / 2);
                    trianglePath.lineTo(piece.size / 2, piece.size / 2);
                    trianglePath.close();
                    canvas.drawPath(trianglePath, paint);
                    break;
                    
                case 3: // Star
                    drawStar(canvas, 0, 0, piece.size / 2, paint);
                    break;
                    
                case 4: // Heart
                    drawHeart(canvas, 0, 0, piece.size / 1.5f, paint);
                    break;
            }
        }
        
        private void drawStar(Canvas canvas, float x, float y, float size, Paint paint) {
            Path path = new Path();
            
            float outerRadius = size;
            float innerRadius = size / 2.5f;
            int numPoints = 5;
            
            for (int i = 0; i < numPoints * 2; i++) {
                float radius = (i % 2 == 0) ? outerRadius : innerRadius;
                double angle = Math.PI * i / numPoints - Math.PI / 2;
                
                float pointX = (float) (x + radius * Math.cos(angle));
                float pointY = (float) (y + radius * Math.sin(angle));
                
                if (i == 0) {
                    path.moveTo(pointX, pointY);
                } else {
                    path.lineTo(pointX, pointY);
                }
            }
            
            path.close();
            canvas.drawPath(path, paint);
        }
        
        private void drawHeart(Canvas canvas, float x, float y, float size, Paint paint) {
            Path path = new Path();
            
            // Starting point
            path.moveTo(x, y + size / 4);
            
            // Left curve
            RectF leftArc = new RectF(
                    x - size, y - size / 2,
                    x, y + size / 2);
            path.arcTo(leftArc, 180, 180, false);
            
            // Right curve
            RectF rightArc = new RectF(
                    x, y - size / 2,
                    x + size, y + size / 2);
            path.arcTo(rightArc, 180, 180, false);
            
            // Bottom point
            path.lineTo(x, y + size);
            path.close();
            
            canvas.drawPath(path, paint);
        }
    }
    
    /**
     * Enhanced confetti piece with more properties
     */
    private static class ConfettiPiece {
        public float x, y;
        public float size;
        public int color;
        public float rotation;
        public float speedX, speedY;
        public float rotationSpeed;
        public int type; // 0=circle, 1=rectangle, 2=triangle, 3=star, 4=heart
        public float alpha = 1.0f;
        public float offset;
        
        public ConfettiPiece(float x, float y, float size, int color, float rotation,
                            float speedX, float speedY, float rotationSpeed, int type) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.rotation = rotation;
            this.speedX = speedX;
            this.speedY = speedY;
            this.rotationSpeed = rotationSpeed;
            this.type = type;
            
            // Random offset for oscillation
            this.offset = new Random().nextFloat() * 10;
        }
    }
} 