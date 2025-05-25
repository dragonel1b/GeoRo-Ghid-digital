package com.example.myapplication.dobrogeausage;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class HistriaPuzzleActivity extends AppCompatActivity {

    private ImageView[] puzzlePieces = new ImageView[4];
    private int[] targetAreas = new int[4];
    private int completedPieces = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histria_puzzle);

        // Initialize puzzle pieces
        puzzlePieces[0] = findViewById(R.id.piece1);
        puzzlePieces[1] = findViewById(R.id.piece2);
        puzzlePieces[2] = findViewById(R.id.piece3);
        puzzlePieces[3] = findViewById(R.id.piece4);

        // Initialize target areas
        targetAreas[0] = R.id.target1;
        targetAreas[1] = R.id.target2;
        targetAreas[2] = R.id.target3;
        targetAreas[3] = R.id.target4;

        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        for (ImageView piece : puzzlePieces) {
            piece.setOnLongClickListener(v -> {
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(null, shadowBuilder, v, 0);
                return true;
            });
        }

        for (int targetId : targetAreas) {
            findViewById(targetId).setOnDragListener((v, event) -> {
                switch(event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        View draggedView = (View) event.getLocalState();
                        ImageView droppedPiece = (ImageView) draggedView;
                        ImageView targetArea = (ImageView) v;

                        if (isCorrectPosition(droppedPiece, targetArea)) {
                            droppedPiece.setX(v.getX());
                            droppedPiece.setY(v.getY());
                            completedPieces++;
                            checkCompletion();
                        }
                        break;
                }
                return true;
            });
        }
    }

    private boolean isCorrectPosition(ImageView piece, ImageView target) {
        // Implement position checking logic
        return true; // Simplified for example
    }

    private void checkCompletion() {
        if (completedPieces == puzzlePieces.length) {
            Toast.makeText(this, "Felicitări! Ai reconstruit cetatea!", Toast.LENGTH_LONG).show();
        }
    }
}
