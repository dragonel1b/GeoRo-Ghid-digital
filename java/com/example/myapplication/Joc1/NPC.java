package com.example.myapplication.Joc1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.ArrayList;

public class NPC {
    private float x, y;
    private List<String> dialogues;
    private boolean isInteracted;
    private static final float SIZE = 60;
    private Paint paint;
    private Drawable npcDrawable;
    private String name;
    private Mission npcQuest;
    private int colorFilter;

    public NPC(float x, float y, String name, Drawable npcDrawable, int colorFilter) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.npcDrawable = npcDrawable;
        this.dialogues = new ArrayList<>();
        this.isInteracted = false;
        this.colorFilter = colorFilter;

        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
    }

    public void addDialogue(String dialogue) {
        dialogues.add(dialogue);
    }

    public String triggerDialogue() {
        if (dialogues.isEmpty()) return "...";
        isInteracted = true;
        // Return a random dialogue
        int randomIndex = (int)(Math.random() * dialogues.size());
        return dialogues.get(randomIndex);
    }

    public void setNpcQuest(Mission quest) {
        this.npcQuest = quest;
    }

    public Mission getNpcQuest() {
        return npcQuest;
    }

    public void render(Canvas canvas) {
        if (npcDrawable != null) {
            npcDrawable.setBounds(
                    (int)x,
                    (int)y,
                    (int)(x + SIZE),
                    (int)(y + SIZE)
            );

            // Apply color filter to make each NPC look different
            npcDrawable.setColorFilter(colorFilter, android.graphics.PorterDuff.Mode.MULTIPLY);
            npcDrawable.draw(canvas);
            // Reset color filter after drawing
            npcDrawable.clearColorFilter();

            // Draw interaction indicator if not interacted
            if (!isInteracted) {
                canvas.drawCircle(x + SIZE/2, y - 10, 5, paint);
            }

            // Draw quest indicator if has quest
            if (npcQuest != null && !isInteracted) {
                paint.setColor(Color.GREEN);
                canvas.drawCircle(x + SIZE/2, y - 20, 5, paint);
                paint.setColor(Color.YELLOW);
            }
        }
    }

    public boolean isNearby(float playerX, float playerY) {
        float distance = (float) Math.sqrt(
                Math.pow(playerX - (x + SIZE/2), 2) +
                        Math.pow(playerY - (y + SIZE/2), 2)
        );
        return distance < 100; // 100 pixels interaction radius
    }

    public RectF getBounds() {
        return new RectF(x, y, x + SIZE, y + SIZE);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public String getName() { return name; }
    public boolean isInteracted() { return isInteracted; }
    public void setInteracted(boolean interacted) { isInteracted = interacted; }
}
