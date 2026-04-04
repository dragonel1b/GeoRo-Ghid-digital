package com.example.myapplication.moldovausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.BaseGameResultActivity;
import com.example.myapplication.utils.GameResultLauncher;
import com.google.android.material.button.MaterialButton;

/**
 * Activitate pentru afișarea rezultatelor quiz-ului Moldova
 * Similar cu cel din Transilvania pentru consistență
 */
public class MoldovaGameResultActivity extends BaseGameResultActivity {
    
    @Override
    protected String getRegionName() {
        return "Moldova";
    }
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, GameModeSelectionActivity.class);
    }
    
    @Override
    protected RegionTheme getRegionTheme() {
        return new RegionTheme(
            R.color.moldova_primary,
            R.color.moldova_secondary,
            R.color.moldova_accent,
            R.color.moldova_background,
            R.color.moldova_card_bg,
            R.color.moldova_text
        );
    }
    
    // Metodele specifice pentru Moldova pot fi adăugate aici dacă este necesar
} 
