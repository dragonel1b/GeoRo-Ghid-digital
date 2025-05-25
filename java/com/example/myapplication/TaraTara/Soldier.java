package com.example.myapplication.TaraTara;

import android.graphics.drawable.Drawable;
import android.graphics.RectF;
import android.graphics.Canvas;

public class Soldier {
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

    public boolean contains(float x, float y) {
        return (x >= this.x - 25 && x <= this.x + 25) && (y >= this.y - 25 && y <= this.y + 25);
    }

    public RectF getBounds() {
        return new RectF(x - 25, y - 25, x + 25, y + 25);
    }

    public void update() {
        // Update soldier's position or state if needed
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
        drawable.setBounds(
                (int)x - 25,
                (int)y - 25,
                (int)x + 25,
                (int)y + 25
        );
        drawable.draw(canvas);
    }
}
