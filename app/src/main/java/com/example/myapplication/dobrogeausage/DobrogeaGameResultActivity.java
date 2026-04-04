package com.example.myapplication.dobrogeausage;

import android.content.Intent;
import com.example.myapplication.core.domain.model.BaseGameResultActivity;
import com.example.myapplication.R;

/**
 * Activitate rezultate pentru regiunea Dobrogea
 * Extinde BaseGameResultActivity cu tema și funcționalitățile specifice Dobrogei
 * Design îmbunătățit cu tema maritimă și deltei
 */
public class DobrogeaGameResultActivity extends BaseGameResultActivity {
    
    @Override
    protected String getRegionName() {
        return "Dobrogea";
    }
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, DobrogeaGameActivity.class);
    }
    
    @Override
    protected RegionTheme getRegionTheme() {
        // Tema maritimă pentru Dobrogea cu culori inspirate din Marea Neagră și Delta Dunării
        return new RegionTheme(
            R.color.dobrogeaResult_primary,        // Primary: albastru ocean pentru tema maritimă
            R.color.dobrogeaResult_primary_dark,   // Primary Dark: albastru ocean închis
            R.color.dobrogeaResult_accent,         // Accent: galben-auriu pentru plajele nisipoase
            R.color.dobrogeaResult_background,     // Background: verde deschis pentru deltă
            R.color.dobrogeaResult_card_bg,        // Card Background: alb pur
            R.color.dobrogeaResult_text            // Text: negru pentru contrast maxim
        );
    }
} 
