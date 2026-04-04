package com.example.myapplication.banatusage;

import android.content.Intent;
import com.example.myapplication.core.domain.model.BaseGameResultActivity;
import com.example.myapplication.R;

/**
 * Activitate rezultate pentru regiunea Banat
 * Extinde BaseGameResultActivity cu tema și funcționalitățile specifice Banatului
 * Design modular cu culori contrastante și compatibilitate completă cu baza de date
 */
public class BanatGameResultActivity extends BaseGameResultActivity {
    
    @Override
    protected String getRegionName() {
        return "Banat";
    }
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, BanatGameActivity.class);
    }
    
    @Override
    protected RegionTheme getRegionTheme() {
        // Tema pentru Banat cu culori calde maro-aurii
        return new RegionTheme(
            R.color.banat_primary,          // Maro plăcut pentru păduri
            R.color.banat_secondary,        // Maro închis pentru accent
            R.color.banat_accent,           // Auriu pentru patrimoniu
            R.color.backgroundLight,        // Background deschis
            R.color.white,                  // Card background alb
            R.color.text_primary            // Text negru pentru contrast
        );
    }
} 
