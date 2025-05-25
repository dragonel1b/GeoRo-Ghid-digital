package com.example.myapplication.Joc1;

import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RomMapActivity extends AppCompatActivity {
    private RomGameState gameState;
    private ConstraintLayout markerContainer;
    private MaterialCardView regionInfoCard;
    private TextView regionNameText;
    private TextView regionDescriptionText;
    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;
    private String currentRegion = null;

    // Region data class
    private static class RegionData {
        final String name;
        final String description;
        final int color;
        final String id;

        RegionData(String name, String description, int color, String id) {
            this.name = name;
            this.description = description;
            this.color = color;
            this.id = id;
        }
    }

    // Region data mapping
    private final RegionData[] REGIONS = {
            new RegionData("Banat", "Regiunea istorică Banat, cunoscută pentru multiculturalismul său și arhitectura specifică.",
                    android.graphics.Color.parseColor("#FFD700"), "banatButton"),
            new RegionData("Crișana", "Crișana, o regiune cu tradiții bogate și peisaje pitorești.",
                    android.graphics.Color.parseColor("#FF0000"), "crisanaButton"),
            new RegionData("Maramureș", "Maramureșul istoric, faimos pentru porțile sale sculptate și bisericile din lemn.",
                    android.graphics.Color.parseColor("#8B0000"), "maramuresButton"),
            new RegionData("Bucovina", "Bucovina, tărâmul mănăstirilor pictate și al tradițiilor păstrate.",
                    android.graphics.Color.parseColor("#FF69B4"), "bucovinaButton"),
            new RegionData("Transilvania", "Transilvania, inima României, cu cetăți medievale și diversitate culturală.",
                    android.graphics.Color.parseColor("#008080"), "transilvaniaButton"),
            new RegionData("Moldova", "Moldova, regiune cu mănăstiri istorice și tradiții viticole.",
                    android.graphics.Color.parseColor("#FF00FF"), "moldovaButton"),
            new RegionData("Oltenia", "Oltenia, cunoscută pentru artă populară și arhitectură tradițională.",
                    android.graphics.Color.parseColor("#ADD8E6"), "olteniaButton"),
            new RegionData("Muntenia", "Muntenia, regiune cu contrast între modernitate și tradiție.",
                    android.graphics.Color.parseColor("#8B4513"), "munteniaButton"),
            new RegionData("Dobrogea", "Dobrogea, unde Dunărea întâlnește Marea Neagră.",
                    android.graphics.Color.parseColor("#FFA500"), "dobrogeaButton")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_map);

        gameState = RomGameState.getInstance();
        initializeViews();
        setupRegionButtons();
        setupCityMarkers();
        applyEntryAnimations();
    }

    private void initializeViews() {
        markerContainer = findViewById(R.id.markerContainer);
        regionInfoCard = findViewById(R.id.regionInfoCard);
        regionNameText = findViewById(R.id.regionNameText);
        regionDescriptionText = findViewById(R.id.regionDescriptionText);

        bottomSheetBehavior = BottomSheetBehavior.from(regionInfoCard);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        FloatingActionButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupRegionButtons() {
        for (RegionData region : REGIONS) {
            ImageView marker = findViewById(getResources().getIdentifier(region.id, "id", getPackageName()));
            if (marker != null) {
                // Get the drawable and start animation
                Drawable drawable = marker.getDrawable();
                if (drawable instanceof Animatable) {
                    ((Animatable) drawable).start();
                }

                // Apply color filter to the drawable
                marker.setColorFilter(region.color, PorterDuff.Mode.MULTIPLY);

                // Set up ripple effect for touch feedback
                TypedValue outValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                marker.setBackgroundResource(outValue.resourceId);

                // Set click listener
                marker.setOnClickListener(v -> showRegionInfo(region));
            }
        }
    }

    private void showRegionInfo(RegionData region) {
        if (currentRegion != null && currentRegion.equals(region.id)) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            currentRegion = null;
        } else {
            regionNameText.setText(region.name);
            regionDescriptionText.setText(region.description);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            currentRegion = region.id;
        }
    }

    public void onRegionClick(View view) {
        String buttonId = getResources().getResourceEntryName(view.getId());
        for (RegionData region : REGIONS) {
            if (region.id.equals(buttonId)) {
                showRegionInfo(region);
                break;
            }
        }
    }

    private void setupCityMarkers() {
        // City markers setup code remains unchanged
    }

    private void applyEntryAnimations() {
        // Animation code remains unchanged
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setupRegionButtons();
        }
    }
}
