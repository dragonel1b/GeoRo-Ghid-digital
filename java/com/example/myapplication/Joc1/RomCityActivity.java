package com.example.myapplication.Joc1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RomCityActivity extends AppCompatActivity {
    private RomGameState gameState;
    private String cityName;
    private TextView cityTitleText, cityDescriptionText;
    private ImageView cityImageView;
    private MaterialCardView infoCard;
    private RecyclerView attractionsRecyclerView;
    private MaterialButton quizButton, exploreButton;

    // City information database
    private static final Map<String, CityInfo> CITY_DATABASE = new HashMap<String, CityInfo>() {{
        put("Sibiu", new CityInfo(
                "Sibiu",
                "Unul dintre cele mai frumoase și bine păstrate orașe medievale din România. " +
                        "Cunoscut pentru arhitectura sa gotică, Piața Mare, și Podul Minciunilor.",
                new String[] {
                        "Piața Mare - Centrul istoric al orașului",
                        "Podul Minciunilor - Primul pod din fontă din România",
                        "Muzeul Brukenthal - Cel mai vechi muzeu din România",
                        "Turnul Sfatului - Symbol al orașului medieval"
                }
        ));

        put("Cluj", new CityInfo(
                "Cluj-Napoca",
                "Capitala neoficială a Transilvaniei, un important centru cultural și universitar. " +
                        "Orașul îmbină perfect istoria medievală cu viața modernă și dinamică.",
                new String[] {
                        "Piața Unirii - Centrul istoric cu Biserica Sf. Mihail",
                        "Grădina Botanică - Una dintre cele mai mari din sud-estul Europei",
                        "Muzeul Etnografic al Transilvaniei",
                        "Cetățuia - Oferă o panoramă spectaculoasă asupra orașului"
                }
        ));

        put("Brașov", new CityInfo(
                "Brașov",
                "Oraș medieval fascinant, înconjurat de Munții Carpați. " +
                        "Biserica Neagră și zidurile cetății oferă o atmosferă autentică medievală.",
                new String[] {
                        "Biserica Neagră - Cel mai mare edificiu gotic din Europa de Est",
                        "Piața Sfatului - Centrul istoric medieval",
                        "Tampa - Muntele care oferă o vedere panoramică",
                        "Poarta Ecaterinei - Monument istoric din secolul XVI"
                }
        ));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_city);

        gameState = RomGameState.getInstance();

        // Get city name from intent
        cityName = getIntent().getStringExtra("CITY_NAME");
        if (cityName == null) {
            Toast.makeText(this, "Eroare la încărcarea orașului", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadCityData();
        applyAnimations();
    }

    private void initializeViews() {
        cityTitleText = findViewById(R.id.cityTitle);
        cityDescriptionText = findViewById(R.id.cityDescription);
        cityImageView = findViewById(R.id.cityImage);
        infoCard = findViewById(R.id.infoCard);
        attractionsRecyclerView = findViewById(R.id.attractionsRecyclerView);
        quizButton = findViewById(R.id.quizButton);
        exploreButton = findViewById(R.id.exploreButton);

        // Set up RecyclerView
        attractionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up button listeners
        quizButton.setOnClickListener(v -> startQuiz());
        exploreButton.setOnClickListener(v -> exploreCity());
    }

    private void loadCityData() {
        CityInfo cityInfo = CITY_DATABASE.get(cityName);
        if (cityInfo == null) {
            Toast.makeText(this, "Informații indisponibile pentru acest oraș", Toast.LENGTH_SHORT).show();
            return;
        }

        cityTitleText.setText(cityInfo.name);
        cityDescriptionText.setText(cityInfo.description);

        // Load city image
        int imageResource = getResources().getIdentifier(
                cityName.toLowerCase().replace(" ", "_"),
                "drawable",
                getPackageName()
        );
        if (imageResource != 0) {
            cityImageView.setImageResource(imageResource);
        }

        // Set up attractions list
        List<String> attractionsList = new ArrayList<>(List.of(cityInfo.attractions));
        attractionsRecyclerView.setAdapter(new RomAttractionAdapter(attractionsList));
    }

    private void applyAnimations() {
        infoCard.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        cityImageView.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));
    }

    private void startQuiz() {
        Intent intent = new Intent(this, RomQuizActivity.class);
        intent.putExtra("CITY_NAME", cityName);
        startActivity(intent);
    }

    private void exploreCity() {
        // Simulate exploration and resource consumption
        float explorationCost = 50.0f; // RON
        float foodConsumption = 0.5f; // kg

        if (gameState.canAffordPurchase(explorationCost) && gameState.hasEnoughFood(1)) {
            gameState.updateMoney(-explorationCost);
            gameState.updateFood(-foodConsumption);
            gameState.addPuncteIntelepte(10, this);

            String message = "Ai explorat orașul și ai dobândit 10 Puncte Înțelepte!";

            // Check if Cultural Explorer achievement is unlocked
            if (!gameState.isAchievementUnlocked(RomGameState.ACHIEVEMENT_CALATOR_LEGENDAR)) {
                message += "\nAi deblocat realizarea 'Explorator Cultural'!";
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    "Nu ai suficiente resurse pentru explorare!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void goBack(View view) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Inner class for city information
    private static class CityInfo {
        String name;
        String description;
        String[] attractions;

        CityInfo(String name, String description, String[] attractions) {
            this.name = name;
            this.description = description;
            this.attractions = attractions;
        }
    }
}
