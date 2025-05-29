package com.example.myapplication.viewmodel;

import com.example.myapplication.R;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AppShowcaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_showcase);

        // Setup animation demo
        ImageView animationDemo = findViewById(R.id.animationDemo);
        findViewById(R.id.playAnimationButton).setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.logo_rotate);
            animationDemo.startAnimation(anim);
            Toast.makeText(this, "Demonstrând animația de rotație", Toast.LENGTH_SHORT).show();
        });

        // Setup game mode buttons to show brief info
        setupGameModeButtons();
    }

    private void setupGameModeButtons() {
        int[] buttonIds = {
            R.id.showcaseMinigameButton,
            R.id.showcaseMapButton, 
            R.id.showcaseQuizButton,
            R.id.showcaseQuestButton,
            R.id.showcaseCulinaryButton,
            R.id.showcaseTraditionalButton
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this::showGameModeInfo);
        }
    }

    private void showGameModeInfo(View view) {
        String message = "";
        int id = view.getId();
        if (id == R.id.showcaseMinigameButton) {
            message = "Minijocuri interactive care te ajută să înveți despre România";
        } else if (id == R.id.showcaseMapButton) {
            message = "Explorează harta României și descoperă locații importante";
        } else if (id == R.id.showcaseQuizButton) {
            message = "Testează-ți cunoștințele despre România cu întrebări variate";
        } else if (id == R.id.showcaseQuestButton) {
            message = "Completează misiuni pentru a descoperi patrimoniul cultural";
        } else if (id == R.id.showcaseCulinaryButton) {
            message = "Descoperă bucătăria tradițională românească";
        } else if (id == R.id.showcaseTraditionalButton) {
            message = "Joacă jocuri tradiționale românești";
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
