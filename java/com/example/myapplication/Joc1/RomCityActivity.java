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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RomCityActivity extends AppCompatActivity {
    private RomGameState gameState;
    private String cityName;
    private LatLng cityLocation;
    private TextView cityTitleText, cityDescriptionText;
    private ImageView cityImageView;
    private MaterialCardView infoCard;
    private RecyclerView attractionsRecyclerView;
    private MaterialButton quizButton, exploreButton;

    // City information database
    private static final Map<String, CityInfo> CITY_DATABASE = new HashMap<String, CityInfo>() {{
        // Transilvania
        put("Sibiu", new CityInfo(
                "Sibiu",
                "Unul dintre cele mai frumoase și bine păstrate orașe medievale din România.",
                new String[] {
                        "Piața Mare", "Podul Minciunilor", "Muzeul Brukenthal", "Turnul Sfatului"
                }
        ));
        put("Cluj-Napoca", new CityInfo(
                "Cluj-Napoca",
                "Capitala neoficială a Transilvaniei, centru cultural și universitar.",
                new String[] {
                        "Piața Unirii", "Grădina Botanică", "Muzeul Etnografic", "Cetățuia"
                }
        ));
        put("Brașov", new CityInfo(
                "Brașov",
                "Oraș medieval înconjurat de Munții Carpați.",
                new String[] {
                        "Biserica Neagră", "Piața Sfatului", "Tampa", "Poarta Ecaterinei"
                }
        ));
        put("Târgu Mureș", new CityInfo(
                "Târgu Mureș",
                "Oraș multicultural cu arhitectură secession.",
                new String[] {
                        "Palatul Culturii", "Cetatea medievală", "Piața Trandafirilor"
                }
        ));

        // Banat
        put("Timișoara", new CityInfo(
                "Timișoara",
                "Primul oraș european cu iluminat electric.",
                new String[] {
                        "Piața Unirii", "Opera", "Catedrala Mitropolitană", "Bega"
                }
        ));

        // Crișana
        put("Oradea", new CityInfo(
                "Oradea",
                "Orașul art nouveau al României.",
                new String[] {
                        "Cetatea Oradea", "Piața Unirii", "Biserica cu Lună", "Str. Republicii"
                }
        ));

        // Maramureș
        put("Baia Mare", new CityInfo(
                "Baia Mare",
                "Poarta de intrare în Maramureș.",
                new String[] {
                        "Turnul lui Ștefan", "Piața Libertății", "Muzeul de Mineralogie"
                }
        ));

        // Bucovina
        put("Suceava", new CityInfo(
                "Suceava",
                "Fostă capitală a Moldovei medievale.",
                new String[] {
                        "Cetatea de Scaun", "Muzeul Satului Bucovinean", "Mănăstirea Zamca"
                }
        ));

        // Moldova
        put("Iași", new CityInfo(
                "Iași",
                "Orașul celor 7 coline, capitală culturală.",
                new String[] {
                        "Palatul Culturii", "Teatrul Național", "Copou", "Mănăstirea Trei Ierarhi"
                }
        ));

        // Oltenia
        put("Craiova", new CityInfo(
                "Craiova",
                "Orașul cu tradiție culturală și industrială.",
                new String[] {
                        "Parcul Nicolae Romanescu", "Muzeul Olteniei", "Teatrul Marin Sorescu"
                }
        ));

        // Dobrogea
        put("Constanța", new CityInfo(
                "Constanța",
                "Cel mai mare port al României la Marea Neagră.",
                new String[] {
                        "Cazinoul", "Farul Genovez", "Moscheea Carol I", "Plaja Modern"
                }
        ));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_city);

        gameState = RomGameState.getInstance();
        
        // Get city location from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cityLocation = new LatLng(
                extras.getDouble("city_lat"),
                extras.getDouble("city_lng")
            );
            gameState.addVisitedLocation(cityLocation, this);
        }

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
