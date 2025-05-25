package com.example.myapplication.TaraTara;

import android.graphics.drawable.Drawable;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.util.Log;
import android.graphics.Paint;

public class Soldier {
    private static final String TAG = "Soldier";
    private Team team;
    private float x;
    private float y;
    private float targetX;
    private float targetY;
    private float speed;
    private boolean isMoving;
    private int color;
    private Drawable drawable;
    private float health;
    private float strength;

    public Soldier(float x, float y, int color, Drawable drawable) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.speed = 5.0f;
        this.isMoving = false;
        this.color = color;
        this.drawable = drawable;
        this.health = 100.0f; // Default health
        this.strength = 10.0f; // Default strength
        
        if (drawable == null) {
            Log.w(TAG, "Created soldier with null drawable at position " + x + "," + y);
        }
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getTargetX() {
        return targetX;
    }

    public void setTargetX(float targetX) {
        this.targetX = targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public void setTargetY(float targetY) {
        this.targetY = targetY;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public int getColor() {
        return color;
    }

    public Drawable getDrawable() {
        return drawable;
    }
    
    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public boolean contains(float x, float y) {
        return (x >= this.x - 25 && x <= this.x + 25) && (y >= this.y - 25 && y <= this.y + 25);
    }

    public RectF getBounds() {
        return new RectF(x - 25, y - 25, x + 25, y + 25);
    }

    public void update() {
        // Implementare pentru mișcarea soldatului către țintă
        if (isMoving) {
            float dx = targetX - x;
            float dy = targetY - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > speed) {
                // Normalizare și scalare cu viteza
                x += dx / distance * speed;
                y += dy / distance * speed;
            } else {
                // Soldatul a ajuns la destinație
                x = targetX;
                y = targetY;
                isMoving = false;
            }
            
            // Make sure soldier stays within reasonable bounds
            if (team != null) {
                // Get screen dimensions from team
                float screenWidth = 0;
                float screenHeight = 0;
                
                try {
                    // Try to get screen dimensions from team center positions
                    float teamCenterX = team.getTeamCenterX();
                    float teamCenterY = team.getTeamCenterY();
                    
                    // Estimate screen dimensions (assuming team centers are placed reasonably)
                    if (teamCenterX > 0 && teamCenterY > 0) {
                        screenWidth = teamCenterX * 2.5f;
                        screenHeight = teamCenterY * 2.5f;
                    }
                    
                    // Apply bounds
                    if (screenWidth > 0 && screenHeight > 0) {
                        x = Math.max(50, Math.min(screenWidth - 50, x));
                        y = Math.max(50, Math.min(screenHeight - 50, y));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error applying bounds to soldier position", e);
                }
            }
        }
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            // Handle soldier death
            health = 0;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public float getStrength() {
        return strength;
    }

    public float getHealth() {
        return health;
    }

    public void draw(Canvas canvas) {
        try {
            if (drawable != null) {
                drawable.setBounds(
                        (int)x - 25,
                        (int)y - 25,
                        (int)x + 25,
                        (int)y + 25
                );
                drawable.draw(canvas);
            } else {
                Log.w(TAG, "Attempted to draw soldier with null drawable at position " + x + "," + y);
                
                // Draw a fallback circle if drawable is null
                Paint paint = new Paint();
                paint.setColor(color);
                canvas.drawCircle(x, y, 25, paint);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error drawing soldier at " + x + "," + y, e);
            
            // Simple fallback
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawCircle(x, y, 25, paint);
        }
    }
}
