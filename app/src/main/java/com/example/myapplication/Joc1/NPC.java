package com.example.myapplication.Joc1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.ArrayList;

/**
 * Clasa pentru personaje non-jucător (NPC) cu care utilizatorul poate interacționa
 */
public class NPC {
    private float x, y;
    private String name;
    private String description;
    private int imageResourceId;
    private List<String> dialogs;
    private int currentDialogIndex;
    private boolean isInteracted;
    private static final float SIZE = 60;
    private Paint paint;
    private Drawable npcDrawable;
    private String id;
    private String region;
    private Mission npcQuest;
    private int colorFilter;
    private boolean isQuestGiver;

    /**
     * Constructor pentru personaj NPC
     * 
     * @param name Numele personajului
     * @param description O scurtă descriere a personajului
     * @param imageResourceId ID-ul resursei drawable pentru imaginea personajului
     */
    public NPC(String name, String description, int imageResourceId) {
        this.name = name;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.dialogs = new ArrayList<>();
        this.currentDialogIndex = 0;
        this.isQuestGiver = false;

        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
    }

    /**
     * Constructor pentru NPC cu id specificat.
     *
     * @param id ID-ul personajului
     * @param name Numele personajului
     * @param description Descrierea personajului
     * @param imageResourceId Resursa imaginii pentru personaj
     */
    public NPC(String id, String name, String description, int imageResourceId) {
        this(name, description, imageResourceId);
        this.id = id;
    }

    /**
     * Constructor pentru NPC cu poziție și culoare.
     *
     * @param x Poziția x pe ecran
     * @param y Poziția y pe ecran
     * @param name Numele personajului
     * @param npcDrawable Drawable-ul pentru personaj
     * @param colorFilter Filtrul de culoare pentru a distinge NPC-ul
     */
    public NPC(float x, float y, String name, Drawable npcDrawable, int colorFilter) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.npcDrawable = npcDrawable;
        this.colorFilter = colorFilter;
        this.dialogs = new ArrayList<>();
        this.currentDialogIndex = 0;
        this.isQuestGiver = false;

        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
    }

    /**
     * Adaugă o replică de dialog pentru personaj
     */
    public void addDialog(String dialogText) {
        dialogs.add(dialogText);
    }
    
    /**
     * Metodă de compatibilitate - alias pentru addDialog
     */
    public void addDialogue(String dialogText) {
        addDialog(dialogText);
    }
    
    /**
     * Metodă de compatibilitate - obține dialogul curent
     * @return Textul dialogului curent
     */
    public String triggerDialogue() {
        return getCurrentDialog();
    }

    /**
     * Getter pentru id.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter pentru id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter pentru nume.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter pentru nume.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter pentru descriere.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter pentru descriere.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter pentru resursa imaginii.
     */
    public int getImageResourceId() {
        return imageResourceId;
    }

    /**
     * Setter pentru resursa imaginii.
     */
    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    /**
     * Getter pentru lista de dialoguri.
     */
    public List<String> getDialogs() {
        return dialogs;
    }

    /**
     * Getter pentru indexul dialogului curent.
     */
    public int getCurrentDialogIndex() {
        return currentDialogIndex;
    }

    /**
     * Setter pentru indexul dialogului curent.
     */
    public void setCurrentDialogIndex(int index) {
        if (index >= 0 && index < dialogs.size()) {
            this.currentDialogIndex = index;
        }
    }

    /**
     * Obține replica curentă de dialog
     */
    public String getCurrentDialog() {
        if (dialogs.isEmpty()) {
            return "...";
        }
        return dialogs.get(currentDialogIndex);
    }

    /**
     * Avansează la următoarea replică de dialog, dacă există
     * 
     * @return true dacă există o replică următoare, false dacă am ajuns la final
     */
    public boolean nextDialog() {
        if (currentDialogIndex < dialogs.size() - 1) {
            currentDialogIndex++;
            return true;
        }
        return false;
    }

    /**
     * Verifică dacă personajul are mai multe replici de dialog
     */
    public boolean hasMoreDialogs() {
        return currentDialogIndex < dialogs.size() - 1;
    }

    /**
     * Resetează dialogul la prima replică
     */
    public void resetDialog() {
        currentDialogIndex = 0;
    }

    /**
     * Getter pentru isQuestGiver.
     */
    public boolean isQuestGiver() {
        return isQuestGiver;
    }

    /**
     * Setter pentru isQuestGiver.
     */
    public void setQuestGiver(boolean questGiver) {
        isQuestGiver = questGiver;
    }

    /**
     * Getter pentru regiunea asociată NPC-ului.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Setter pentru regiunea asociată NPC-ului.
     */
    public void setRegion(String region) {
        this.region = region;
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
    public boolean isInteracted() { return isInteracted; }
    public void setInteracted(boolean interacted) { isInteracted = interacted; }
}
