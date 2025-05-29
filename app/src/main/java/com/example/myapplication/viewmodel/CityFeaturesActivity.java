package com.example.myapplication.viewmodel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ImageCarouselAdapter;
import com.example.myapplication.model.CityImage;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class CityFeaturesActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "CityFeaturesPrefs";
    private static final String USER_IMAGES_KEY = "userImages";
    private static final String LAST_VISIT_KEY = "lastVisit";
    private static final String FAVORITE_PLACES_KEY = "favoritePlaces";
    private static final String VISIT_COUNT_KEY = "visitCount";

    protected ArrayList<String> userImages = new ArrayList<>();
    protected SharedPreferences sharedPreferences;
    protected LinearLayout featuresContainer;
    protected Random random = new Random();
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected int visitCount = 0;
    protected MediaPlayer soundEffect;
    protected KonfettiView konfettiView;
    protected ViewPager2 imageCarousel;
    protected TabLayout imageIndicator;
    protected FloatingActionButton fab;
    protected List<CityImage> images = new ArrayList<>();
    protected ActivityResultLauncher<Intent> imagePickerLauncher;
    protected ActivityResultLauncher<Intent> cameraLauncher;
    protected String cityName = "Constanța";
    protected String regionName = "Dobrogea";
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_features);
        
        // Get city and region name from intent (default to Constanța if not provided)
        if (getIntent().hasExtra("CITY_NAME")) {
            cityName = getIntent().getStringExtra("CITY_NAME");
        }
        if (getIntent().hasExtra("REGION_NAME")) {
            regionName = getIntent().getStringExtra("REGION_NAME");
        }
        
        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(cityName);
            }
        }
        
        // Initialize components
        sharedPreferences = getSharedPreferences(PREFS_NAME + "_" + cityName, MODE_PRIVATE);
        featuresContainer = findViewById(R.id.featuresContainer);
        konfettiView = findViewById(R.id.confetti_view);
        fab = findViewById(R.id.fab);
        imageCarousel = findViewById(R.id.imageCarousel);
        imageIndicator = findViewById(R.id.imageIndicator);
        
        // Load saved data
        loadSavedImages();
        loadVisitCount();
        recordVisit();
        
        // Setup components
        setupImageCarousel();
        setupImagePickers();
        setupSoundEffects();
        setupFloatingActionButton();
        setupConfetti();
        
        // Initialize features
        setupSpecialFeatures();
    }
    
    protected void loadVisitCount() {
        visitCount = sharedPreferences.getInt(VISIT_COUNT_KEY, 0);
        visitCount++;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VISIT_COUNT_KEY, visitCount);
        editor.apply();
    }

    protected void recordVisit() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(LAST_VISIT_KEY, System.currentTimeMillis());
        editor.apply();
    }

    protected void loadSavedImages() {
        Set<String> savedImages = sharedPreferences.getStringSet(USER_IMAGES_KEY, new HashSet<>());
        userImages.clear();
        userImages.addAll(savedImages);
    }

    protected void saveImages() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(USER_IMAGES_KEY, new HashSet<>(userImages));
        editor.apply();
    }

    protected void setupImageCarousel() {
        ArrayList<String> images = getCityImages();
        if (images != null && !images.isEmpty()) {
            ImageCarouselAdapter adapter = new ImageCarouselAdapter(this, images);
            
            adapter.setOnImageAddedListener(uri -> {
                String imageUri = uri.toString();
                userImages.add(imageUri);
                saveImages();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Fotografie adăugată cu succes!", Toast.LENGTH_SHORT).show();
            });
            
            imageCarousel.setAdapter(adapter);
            
            new TabLayoutMediator(imageIndicator, imageCarousel, 
                (tab, position) -> {}).attach();
        }
    }

    protected void setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        addImageToCarousel(imageUri, true);
                        
                        // Show a special message for photo challenge participants
                        Snackbar.make(findViewById(android.R.id.content), 
                            "Mulțumim pentru participarea la provocarea foto! Fotografia ta a fost adăugată cu succes.", 
                            Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.containsKey("data")) {
                        Uri imageUri = Uri.parse(extras.getString("data"));
                        addImageToCarousel(imageUri, true);
                    }
                }
            }
        );
    }

    protected void setupFloatingActionButton() {
        fab.setOnClickListener(v -> {
            showCityInfoBottomSheet();
        });
    }

    protected void showCityInfoBottomSheet() {
        CityInfoBottomSheet bottomSheet = new CityInfoBottomSheet();
        Bundle args = new Bundle();
        args.putString(CityInfoBottomSheet.ARG_CITY_NAME, cityName);
        args.putString(CityInfoBottomSheet.ARG_REGION_NAME, regionName);
        bottomSheet.setArguments(args);
        bottomSheet.show(getSupportFragmentManager(), "CityInfoBottomSheet");
    }

    protected void setupSoundEffects() {
        soundEffect = MediaPlayer.create(this, R.raw.click_sound);
    }

    protected void setupConfetti() {
        konfettiView = findViewById(R.id.confetti_view);
    }

    protected void showConfetti() {
        EmitterConfig emitterConfig = new Emitter(5, TimeUnit.SECONDS).perSecond(50);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(1f, 5f)
                .timeToLive(2000L)
                .colors(java.util.Arrays.asList(
                    getResources().getColor(R.color.colorPrimary),
                    getResources().getColor(R.color.colorAccent),
                    getResources().getColor(R.color.colorPrimaryDark)))
                .position(0.5f, 0.3f, 1f, 0.7f)
                .build();

        konfettiView.start(party);
    }

    protected void addImageToCarousel(Uri imageUri, boolean isUserManaged) {
        String imageUriString = imageUri.toString();
        userImages.add(imageUriString);
        saveImages();
        
        if (imageCarousel != null && imageCarousel.getAdapter() != null) {
            imageCarousel.getAdapter().notifyDataSetChanged();
        }
        
        Toast.makeText(this, "Fotografie adăugată cu succes!", Toast.LENGTH_SHORT).show();
    }

    protected void setupSpecialFeatures() {
        // Add elements in the correct order

        // 1. Add local events button (primul element sub carusel)
        addLocalEventsButton();

        // 2. Add weather widget (al doilea element)
        addWeatherWidget();

        // 3. Add photo challenge (al treilea element)
        addPhotoChallenge();

        // 4. Add local tips section (al patrulea element)
        addLocalTipsSection();

        // 5. Add welcome message for returning visitors (se afișează temporar)
        if (visitCount > 1) {
            addWelcomeBackMessage();
        }

        // Nu mai adăugăm butonul de hartă interactivă
        // addInteractiveMapButton();
    }
    
    protected void addWelcomeBackMessage() {
        MaterialCardView welcomeCard = new MaterialCardView(this);
        welcomeCard.setCardElevation(4);
        welcomeCard.setRadius(16);
        welcomeCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 16, 24, 16);
        
        TextView welcomeText = new TextView(this);
        welcomeText.setText("Bine ai revenit în " + cityName + "! Aceasta este vizita ta #" + visitCount);
        welcomeText.setTextSize(18);
        welcomeText.setTextColor(Color.parseColor("#1565C0"));
        
        cardContent.addView(welcomeText);
        welcomeCard.addView(cardContent);
        featuresContainer.addView(welcomeCard);
        
        // Auto-dismiss after 5 seconds
        handler.postDelayed(() -> {
            featuresContainer.removeView(welcomeCard);
        }, 5000);
    }
    
    protected void addInteractiveMapButton() {
        Button mapButton = new Button(this);
        mapButton.setText("Harta Interactivă " + cityName);
        mapButton.setBackgroundColor(Color.parseColor("#2196F3"));
        mapButton.setTextColor(Color.WHITE);
        mapButton.setPadding(32, 16, 32, 16);
        mapButton.setTag("mapButton");
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        mapButton.setLayoutParams(params);
        
        mapButton.setOnClickListener(v -> {
            // Open Google Maps with Constanța's coordinates by default
            Uri gmmIntentUri = Uri.parse("geo:44.1598,28.6348?q=" + cityName + "," + regionName);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps nu este instalat", Toast.LENGTH_SHORT).show();
            }
        });
        
        featuresContainer.addView(mapButton);
    }
    
    protected void addWeatherWidget() {
        MaterialCardView weatherCard = new MaterialCardView(this);
        weatherCard.setCardElevation(4);
        weatherCard.setRadius(16);
        
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 16, 24, 16);
        
        TextView weatherTitle = new TextView(this);
        weatherTitle.setText("Vremea în " + cityName);
        weatherTitle.setTextSize(18);
        weatherTitle.setTextColor(Color.parseColor("#1565C0"));
        
        TextView weatherInfo = new TextView(this);
        weatherInfo.setText("Temperatura: 22°C\nCondiții: Însorit\nUmiditate: 65%");
        weatherInfo.setTextSize(16);
        weatherInfo.setPadding(0, 8, 0, 0);
        
        Button refreshButton = new Button(this);
        refreshButton.setText("Actualizează");
        refreshButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
        refreshButton.setTextColor(Color.BLACK);
        
        refreshButton.setOnClickListener(v -> {
            // Simulate weather update
            int temp = 20 + random.nextInt(10);
            String[] conditions = {"Însorit", "Parcial noros", "Noros", "Ploaie ușoară"};
            String condition = conditions[random.nextInt(conditions.length)];
            int humidity = 50 + random.nextInt(30);
            
            weatherInfo.setText("Temperatura: " + temp + "°C\nCondiții: " + condition + "\nUmiditate: " + humidity + "%");
            
            Snackbar.make(v, "Informațiile meteo au fost actualizate", Snackbar.LENGTH_SHORT).show();
        });
        
        cardContent.addView(weatherTitle);
        cardContent.addView(weatherInfo);
        cardContent.addView(refreshButton);
        weatherCard.addView(cardContent);
        
        featuresContainer.addView(weatherCard);
    }
    
    protected void addLocalEventsButton() {
        Button eventsButton = new Button(this);
        eventsButton.setText("Evenimente Locale");
        eventsButton.setBackgroundColor(Color.parseColor("#4CAF50"));
        eventsButton.setTextColor(Color.WHITE);
        eventsButton.setPadding(32, 16, 32, 16);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        eventsButton.setLayoutParams(params);
        
        eventsButton.setOnClickListener(v -> {
            // Show local events specific to Constanța
            String[] events = {
                "Festivalul Tomis - 15-17 Iulie",
                "Concert la Cazinou - 22 Iulie",
                "Expoziție de Artă - 5-20 August",
                "Festivalul Mării - 10-12 August"
            };
            
            StringBuilder message = new StringBuilder("Evenimente în " + cityName + ":\n\n");
            for (String event : events) {
                message.append("• ").append(event).append("\n");
            }
            
            new android.app.AlertDialog.Builder(this)
                .setTitle("Evenimente Locale")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
        });
        
        featuresContainer.addView(eventsButton);
    }
    
    protected void addPhotoChallenge() {
        MaterialCardView challengeCard = new MaterialCardView(this);
        challengeCard.setCardElevation(4);
        challengeCard.setRadius(16);
        challengeCard.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
        
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 16, 24, 16);
        
        TextView challengeTitle = new TextView(this);
        challengeTitle.setText("Provocare Foto: Capturați " + cityName);
        challengeTitle.setTextSize(18);
        challengeTitle.setTextColor(Color.parseColor("#FF8F00"));
        
        TextView challengeDesc = new TextView(this);
        challengeDesc.setText("Capturați cele mai frumoase locuri din " + cityName + " și împărtășiți-le cu noi!");
        challengeDesc.setTextSize(16);
        challengeDesc.setPadding(0, 8, 0, 16);
        
        Button addPhotoButton = new Button(this);
        addPhotoButton.setText("Adaugă Fotografie");
        addPhotoButton.setBackgroundColor(Color.parseColor("#FFC107"));
        addPhotoButton.setTextColor(Color.BLACK);
        
        addPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
        
        cardContent.addView(challengeTitle);
        cardContent.addView(challengeDesc);
        cardContent.addView(addPhotoButton);
        challengeCard.addView(cardContent);
        
        featuresContainer.addView(challengeCard);
    }
    
    protected void addLocalTipsSection() {
        MaterialCardView tipsCard = new MaterialCardView(this);
        tipsCard.setCardElevation(4);
        tipsCard.setRadius(16);
        tipsCard.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
        
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 16, 24, 16);
        
        TextView tipsTitle = new TextView(this);
        tipsTitle.setText("Sfaturi Locale");
        tipsTitle.setTextSize(18);
        tipsTitle.setTextColor(Color.parseColor("#2E7D32"));
        
        String[] tips = {
            "Vizitați Cazinoul la apus pentru cea mai frumoasă priveliște",
            "Cea mai bună plăcintă dobrogeneană se găsește la 'La Doi Frați'",
            "O plimbare pe Faleză dimineața devreme este o experiență de neuitat",
            "Muzeul de Istorie este gratuit în prima duminică a lunii"
        };
        
        for (String tip : tips) {
            TextView tipText = new TextView(this);
            tipText.setText("• " + tip);
            tipText.setTextSize(16);
            tipText.setPadding(0, 8, 0, 8);
            cardContent.addView(tipText);
        }
        
        tipsCard.addView(cardContent);
        featuresContainer.addView(tipsCard);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String imageUri = selectedImageUri.toString();
                userImages.add(imageUri);
                saveImages();
                
                if (imageCarousel != null && imageCarousel.getAdapter() != null) {
                    imageCarousel.getAdapter().notifyDataSetChanged();
                }
                
                Toast.makeText(this, "Fotografie adăugată cu succes!", Toast.LENGTH_SHORT).show();
                
                // Show a special message for photo challenge participants
                Snackbar.make(findViewById(android.R.id.content), 
                    "Mulțumim pentru participarea la provocarea foto! Fotografia ta a fost adăugată cu succes.", 
                    Snackbar.LENGTH_LONG).show();
            }
        }
    }

    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        
        if (userImages != null && !userImages.isEmpty()) {
            images.addAll(userImages);
        }
        
        // Add default images for Constanța
        images.add("dobrogea_constanta_1");
        images.add("dobrogea_constanta_casino");
        images.add("dobrogea_constanta_plaja");
        images.add("dobrogea_constanta_port");
        images.add("dobrogea_constanta_moschee");
        images.add("dobrogea_constanta_piata_ovidiu");
        images.add("dobrogea_constanta_faleza");
        images.add("dobrogea_constanta_centru");
        
        return images;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundEffect != null) {
            soundEffect.release();
            soundEffect = null;
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 