package com.example.myapplication.Joc1;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.TaraTara.TaraTaraVremOstasi;
import com.google.android.material.card.MaterialCardView;

public class RomMainActivity extends AppCompatActivity {
    private TextView fuelText, moneyText, foodText;
    private MaterialCardView resourcePanel;
    private GridLayout destinationGrid;
    private RomGameState gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_main);

        // Initialize game state
        gameState = RomGameState.getInstance();

        // Initialize views
        initializeViews();

        // Set up destination cards
        setupDestinationCards();

        // Load and display resources
        updateResourceDisplay();

        // Apply animations
        applyEntryAnimations();
    }

    private void initializeViews() {
        fuelText = findViewById(R.id.fuelText);
        moneyText = findViewById(R.id.moneyText);
        foodText = findViewById(R.id.foodText);
        resourcePanel = findViewById(R.id.resourcePanel);
        destinationGrid = findViewById(R.id.destinationGrid);
    }

    private void setupDestinationCards() {
        String[] cities = {"Sibiu", "Cluj", "Brașov", "București", "Iași", "Timișoara"};

        for (String city : cities) {
            MaterialCardView card = new MaterialCardView(this);

            // Set card layout parameters
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                    getResources().getDisplayMetrics());
            params.setMargins(margin, margin, margin, margin);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            card.setLayoutParams(params);

            // Style the card
            float elevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                    getResources().getDisplayMetrics());
            float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                    getResources().getDisplayMetrics());
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                    getResources().getDisplayMetrics());

            card.setCardElevation(elevation);
            card.setRadius(radius);
            card.setContentPadding(padding, padding, padding, padding);
            card.setCardBackgroundColor(getResources().getColor(R.color.rom_card_background, getTheme()));
            card.setClickable(true);
            card.setFocusable(true);

            // Add ripple effect
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            card.setForeground(getDrawable(outValue.resourceId));

            // Create and style the city name text
            TextView cityName = new TextView(this);
            cityName.setText(city);
            cityName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            cityName.setTextAppearance(R.style.RomTextBody);

            // Add text to card with proper layout
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.gravity = Gravity.CENTER;
            cityName.setLayoutParams(textParams);
            card.addView(cityName);

            // Set click listener
            card.setOnClickListener(v -> openCityActivity(city));

            // Add card to grid
            destinationGrid.addView(card);
        }
    }

    private void updateResourceDisplay() {
        try {
            fuelText.setText(getString(R.string.rom_fuel_label, (int) gameState.getEsentaCalatoriei()));
            moneyText.setText(getString(R.string.rom_money_label, (int) gameState.getMonedeDacice()));
            foodText.setText(getString(R.string.rom_food_label, (int) gameState.getMerinde()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyEntryAnimations() {
        resourcePanel.startAnimation(
                AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        for (int i = 0; i < destinationGrid.getChildCount(); i++) {
            View child = destinationGrid.getChildAt(i);
            child.startAnimation(
                    AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
        }
    }

    public void openMapActivity(View view) {
        Intent intent = new Intent(this, RomMapActivity.class);
        startActivity(intent);
    }

    public void openCityActivity(String cityName) {
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", cityName);
        startActivity(intent);
    }

    public void startQuiz(View view) {
        Intent intent = new Intent(this, RomQuizActivity.class);
        startActivity(intent);
    }

    public void openTarataravremostasiActivity(View view) {
        Intent intent = new Intent(this, TaraTaraVremOstasi.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void startQuestMode(View view) {
        Intent intent = new Intent(this, RomQuestActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void startCulinaryMode(View view) {
        Intent intent = new Intent(this, RomCulinaryActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void startMinigame(View view) {
        try {
            Intent intent = new Intent(this, MinigameOpenWorldActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Could not start minigame. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e("RomMainActivity", "Error starting minigame", e);
        } catch (Exception e) {
            Toast.makeText(this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
            Log.e("RomMainActivity", "Unexpected error starting minigame", e);
        }
    }

    public void openAchievements() {
        Intent intent = new Intent(this, RomAchievementsActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateResourceDisplay();
    }
}
