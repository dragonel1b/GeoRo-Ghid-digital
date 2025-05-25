package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.EnhancedCityActivity;
import com.example.myapplication.viewmodel.CityImageAdapter;
import com.example.myapplication.viewmodel.CityFeaturesActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.widget.TextView;
import android.view.ViewGroup;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import com.example.myapplication.viewmodel.AttractionHelper;

public class Borsa extends EnhancedCityActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "BorsaPrefs";
    private static final String USER_IMAGES_KEY = "userImages_Borsa";
    private static final String LAST_VISIT_KEY = "lastVisit";
    private static final String FAVORITE_PLACES_KEY = "favoritePlaces";
    private static final String VISIT_COUNT_KEY = "visitCount";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ArrayList<String> userImages = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private LinearLayout specialFeaturesContainer;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    private int visitCount = 0;
    private static final String WEATHER_API_KEY = "1234567890abcdef1234567890abcdef";
    private static final String BORSA_CITY_ID = "684410"; // Borsa city ID
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + BORSA_CITY_ID + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=ro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Încărcăm doar imaginile salvate pentru Borsa
        loadSavedImages();

        recordVisit();
        loadVisitCount();

        specialFeaturesContainer = new LinearLayout(this);
        specialFeaturesContainer.setOrientation(LinearLayout.VERTICAL);
        specialFeaturesContainer.setPadding(32, 16, 32, 16);

        LinearLayout mainContainer = findViewById(R.id.cityContentContainer);
        if (mainContainer != null) {
            mainContainer.addView(specialFeaturesContainer);
        }

        // Adăugăm butonul de hartă interactivă imediat după caruselul de imagini
        Button mapButton = new Button(this);
        mapButton.setText("Harta Interactivă Borșa");
        mapButton.setBackgroundColor(Color.parseColor("#2196F3"));
        mapButton.setTextColor(Color.WHITE);
        mapButton.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        mapButton.setLayoutParams(params);

        mapButton.setOnClickListener(v -> {
            // Open Google Maps with Borsa location
            Uri gmmIntentUri = Uri.parse("geo:47.6551,24.6506?q=Borsa,Romania");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps nu este instalat", Toast.LENGTH_SHORT).show();
            }
        });

        // Adăugăm butonul după TabLayout, dar înainte ca AppBarLayout să se termine
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
        if (tabLayout != null) {
            ViewGroup parent = (ViewGroup) tabLayout.getParent();
            if (parent != null) {
                parent.addView(mapButton);
            }
        }

        // Apoi inițializăm restul funcționalităților
        setupSpecialFeatures();

        // Verificăm permisiunile înainte de a configura caruselul de imagini
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        // Verificăm dacă avem permisiunile necesare
        boolean hasPermission = false;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Pentru Android 13 și peste, avem nevoie de READ_MEDIA_IMAGES
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Pentru Android 6.0 până la 12, avem nevoie de READ_EXTERNAL_STORAGE
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Pentru versiuni mai vechi, permisiunile sunt acordate la instalare
            hasPermission = true;
        }

        if (hasPermission) {
            // Dacă avem permisiunea, configurăm caruselul de imagini
            setupImageCarousel();
        } else {
            // Dacă nu avem permisiunea, o solicităm
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisiune acordată, configurăm caruselul de imagini
                setupImageCarousel();
            } else {
                // Permisiune refuzată, afișăm un mesaj
                Toast.makeText(this, "Permisiunea de acces la imagini a fost refuzată", Toast.LENGTH_LONG).show();

                // Configurăm caruselul doar cu imaginile predefinite, fără a încărcăm imaginile utilizatorului
                setupImageCarouselWithoutUserImages();
            }
        }
    }

    private void setupImageCarouselWithoutUserImages() {
        // Configurăm caruselul doar cu imaginile predefinite
        ArrayList<String> defaultImages = new ArrayList<>();
        defaultImages.add("maramures_borsa_1");
        defaultImages.add("maramures_borsa_munte");
        defaultImages.add("maramures_borsa_statiune");
        defaultImages.add("maramures_borsa_partie");
        defaultImages.add("maramures_borsa_panorama");

        androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.imageCarousel);
        if (viewPager != null) {
            com.example.myapplication.adapter.ImageCarouselAdapter adapter =
                    new com.example.myapplication.adapter.ImageCarouselAdapter(this, defaultImages);

            viewPager.setAdapter(adapter);

            com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
            if (tabLayout != null) {
                new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager,
                        (tab, position) -> {}).attach();
            }
        }
    }

    private void setupImageCarousel() {
        ArrayList<String> images = getCityImages();
        if (images != null && !images.isEmpty()) {
            androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.imageCarousel);
            if (viewPager != null) {
                com.example.myapplication.adapter.ImageCarouselAdapter adapter =
                        new com.example.myapplication.adapter.ImageCarouselAdapter(this, images);

                adapter.setOnImageAddedListener(uri -> {
                    String imageUri = uri.toString();
                    userImages.add(imageUri);
                    saveImages();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Fotografie adăugată cu succes!", Toast.LENGTH_SHORT).show();
                });

                viewPager.setAdapter(adapter);

                com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
                if (tabLayout != null) {
                    new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager,
                            (tab, position) -> {}).attach();
                }
            }
        }
    }

    private void loadVisitCount() {
        visitCount = sharedPreferences.getInt(VISIT_COUNT_KEY, 0);
        visitCount++;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(VISIT_COUNT_KEY, visitCount);
        editor.apply();
    }

    private void recordVisit() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(LAST_VISIT_KEY, System.currentTimeMillis());
        editor.apply();
    }

    private void loadSavedImages() {
        Set<String> savedImages = sharedPreferences.getStringSet(USER_IMAGES_KEY, new HashSet<>());
        userImages.clear();
        userImages.addAll(savedImages);
    }

    private void saveImages() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(USER_IMAGES_KEY, new HashSet<>(userImages));
        editor.apply();
    }

    private void setupSpecialFeatures() {
        // Golim containerul pentru a fi siguri că nu există elemente reziduale
        specialFeaturesContainer.removeAllViews();

        // Verificăm dacă utilizatorul a mai fost aici
        if (visitCount > 1) {
            addWelcomeBackMessage();
        }

        addWeatherWidget();
        addPhotoChallenge();
        addTouristRoutes();
    }

    private void addWeatherWidget() {
        MaterialCardView weatherCard = new MaterialCardView(this);
        weatherCard.setCardElevation(4);
        weatherCard.setRadius(16);
        weatherCard.setCardBackgroundColor(Color.parseColor("#E1F5FE"));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 32);
        weatherCard.setLayoutParams(cardParams);

        LinearLayout weatherContent = new LinearLayout(this);
        weatherContent.setOrientation(LinearLayout.VERTICAL);
        weatherContent.setPadding(24, 16, 24, 16);

        TextView weatherTitle = new TextView(this);
        weatherTitle.setText("Vremea în Borșa");
        weatherTitle.setTextSize(18);
        weatherTitle.setTextColor(Color.parseColor("#0288D1"));
        weatherTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView weatherInfo = new TextView(this);
        weatherInfo.setText("Se încarcă informațiile despre vreme...");
        weatherInfo.setTextSize(16);
        weatherInfo.setPadding(0, 8, 0, 0);

        weatherContent.addView(weatherTitle);
        weatherContent.addView(weatherInfo);
        weatherCard.addView(weatherContent);

        specialFeaturesContainer.addView(weatherCard);

        // Aici am putea face o cerere reală la un API de vreme, dar folosim date simulate
        // pentru simplitate
        handler.postDelayed(() -> {
            int temp = 5 + random.nextInt(15);
            String condition = getRandomWeatherCondition();
            weatherInfo.setText("Temperatura: " + temp + "°C\nCondiții: " + condition);
        }, 1500);
    }

    private String getRandomWeatherCondition() {
        String[] conditions = {
                "Însorit", "Parțial noros", "Noros", "Ploaie ușoară", "Averse", "Ninsoare"
        };
        return conditions[random.nextInt(conditions.length)];
    }

    private void addTouristRoutes() {
        MaterialCardView routesCard = new MaterialCardView(this);
        routesCard.setCardElevation(4);
        routesCard.setRadius(16);
        routesCard.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 32);
        routesCard.setLayoutParams(cardParams);

        LinearLayout routesContent = new LinearLayout(this);
        routesContent.setOrientation(LinearLayout.VERTICAL);
        routesContent.setPadding(24, 16, 24, 16);

        TextView routesTitle = new TextView(this);
        routesTitle.setText("Trasee Turistice Populare");
        routesTitle.setTextSize(18);
        routesTitle.setTextColor(Color.parseColor("#33691E"));
        routesTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView routesInfo = new TextView(this);
        routesInfo.setText("1. Traseu spre Vârful Pietrosul Rodnei (2303m)\n" +
                "2. Complex Turistic Borșa - Pârtia de schi\n" +
                "3. Cascada Cailor\n" +
                "4. Rezervația Pietrosul Mare");
        routesInfo.setTextSize(16);
        routesInfo.setPadding(0, 16, 0, 16);

        Button routesButton = new Button(this);
        routesButton.setText("Explorează Traseele");
        routesButton.setBackgroundColor(Color.parseColor("#8BC34A"));
        routesButton.setTextColor(Color.BLACK);

        routesButton.setOnClickListener(v -> {
            Toast.makeText(this, "Vei fi redirecționat către harta turistică", Toast.LENGTH_SHORT).show();
            // Implementare viitoare pentru afișarea traseelor
        });

        routesContent.addView(routesTitle);
        routesContent.addView(routesInfo);
        routesContent.addView(routesButton);
        routesCard.addView(routesContent);

        specialFeaturesContainer.addView(routesCard);
    }

    private void addPhotoChallenge() {
        MaterialCardView photoCard = new MaterialCardView(this);
        photoCard.setCardElevation(4);
        photoCard.setRadius(16);
        photoCard.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 32);
        photoCard.setLayoutParams(cardParams);

        LinearLayout photoContent = new LinearLayout(this);
        photoContent.setOrientation(LinearLayout.VERTICAL);
        photoContent.setPadding(24, 16, 24, 16);

        TextView photoTitle = new TextView(this);
        photoTitle.setText("Provocare Foto: Capturați Borșa");
        photoTitle.setTextSize(18);
        photoTitle.setTextColor(Color.parseColor("#FF8F00"));
        photoTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView photoDesc = new TextView(this);
        photoDesc.setText("Capturați cele mai frumoase peisaje din Borșa și împărtășiți-le cu noi!");
        photoDesc.setTextSize(16);
        photoDesc.setPadding(0, 8, 0, 16);

        Button addPhotoBtn = new Button(this);
        addPhotoBtn.setText("Adaugă Fotografie");
        addPhotoBtn.setBackgroundColor(Color.parseColor("#FFC107"));
        addPhotoBtn.setTextColor(Color.BLACK);

        addPhotoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        photoContent.addView(photoTitle);
        photoContent.addView(photoDesc);
        photoContent.addView(addPhotoBtn);
        photoCard.addView(photoContent);

        specialFeaturesContainer.addView(photoCard);
    }

    private void addWelcomeBackMessage() {
        MaterialCardView welcomeCard = new MaterialCardView(this);
        welcomeCard.setCardElevation(4);
        welcomeCard.setRadius(16);
        welcomeCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"));

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 16, 24, 16);

        android.widget.TextView welcomeText = new android.widget.TextView(this);
        welcomeText.setText("Bine ai revenit în Borșa! Aceasta este vizita ta #" + visitCount);
        welcomeText.setTextSize(18);
        welcomeText.setTextColor(Color.parseColor("#1565C0"));

        cardContent.addView(welcomeText);
        welcomeCard.addView(cardContent);
        specialFeaturesContainer.addView(welcomeCard);

        // Auto-dismiss after 5 seconds
        handler.postDelayed(() -> {
            specialFeaturesContainer.removeView(welcomeCard);
        }, 5000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);

                    String imageUri = selectedImageUri.toString();

                    // Verificăm dacă avem deja 5 imagini pentru Borșa
                    if (userImages.size() >= 5) {
                        // Eliminăm cea mai veche imagine
                        userImages.remove(0);
                    }

                    // Adăugăm noua imagine
                    userImages.add(imageUri);
                    saveImages();

                    // Reconfigurăm caruselul pentru a include noua imagine
                    setupImageCarousel();

                    Toast.makeText(this, "Fotografie adăugată cu succes!", Toast.LENGTH_SHORT).show();

                    Snackbar.make(findViewById(android.R.id.content),
                            "Mulțumim pentru participarea la provocarea foto! Fotografia ta a fost adăugată cu succes.",
                            Snackbar.LENGTH_LONG).show();
                } catch (SecurityException e) {
                    Toast.makeText(this, "Nu s-a putut accesa imaginea: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();

        // Add user images if available
        if (userImages != null && !userImages.isEmpty()) {
            int startIndex = Math.max(0, userImages.size() - 5);
            images.addAll(userImages.subList(startIndex, userImages.size()));
        }

        // Add default images if needed
        if (images.size() < 5) {
            ArrayList<String> defaultImages = new ArrayList<>();
            defaultImages.add("maramures_borsa_1");
            defaultImages.add("maramures_borsa_munte");
            defaultImages.add("maramures_borsa_statiune");
            defaultImages.add("maramures_borsa_partie");
            defaultImages.add("maramures_borsa_panorama");

            // Add only as many default images as needed to reach 5
            int remainingSlots = 5 - images.size();
            for (int i = 0; i < remainingSlots && i < defaultImages.size(); i++) {
                images.add(defaultImages.get(i));
            }
        }

        return images;
    }

    @Override
    protected void initializeSpecificContent() {
        super.initializeSpecificContent();

        setTitle("Borșa");

        // Add sections
        addSection(
                findViewById(R.id.cityContentContainer),
                "Istorie și Tradiție",
                "Borșa este o localitate cu o istorie veche, cunoscută pentru tradițiile maramureșene bine păstrate. Așezarea a fost menționată documentar pentru prima dată în 1365, dezvoltându-se inițial ca un centru minier și pastoral. Locuitorii săi au continuat să păstreze obiceiurile și tradițiile specifice Maramureșului de-a lungul timpului.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Geografie",
                "Situată în partea de nord-est a județului Maramureș, la poalele Munților Rodnei, Borșa se află la o altitudine de aproximativ 850 m. Este străbătută de râul Vișeu și se întinde pe o lungime de aproximativ 20 km în partea de sud a Depresiunii Maramureșului. În apropiere se află cel mai înalt vârf din Carpații Orientali - Pietrosul Rodnei (2303 m).",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Atracții Turistice",
                "Printre atracțiile principale se numără Stațiunea Borșa, cu pârtiile sale de schi, Cascada Cailor (cea mai înaltă cascadă din România, cu o cădere de aproximativ 90 m), Rezervația Naturală Pietrosul Rodnei și Parcul Național Munții Rodnei. Arhitectura tradițională maramureșeană și bisericile de lemn completează peisajul turistic al zonei.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Cultură",
                "Viața culturală din Borșa este bogată în tradiții și obiceiuri locale, manifestate prin portul popular, dansuri tradiționale, muzică și meșteșuguri. Festivalurile locale, precum Hora la Prislop sau sărbătorile religioase, sunt momente importante în viața comunității, atrăgând vizitatori și menținând vii tradițiile străvechi.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Turism montan",
                "Borșa este un important centru turistic montan, cu facilități pentru sporturi de iarnă (pârtii de schi, teleschi) și un punct de plecare pentru drumeții montane în Munții Rodnei. Vara, zona devine un paradis pentru iubitorii de drumeții și natură sălbatică, oferind trasee de diferite grade de dificultate către peisaje spectaculoase.",
                false
        );

        // Add attractions using the AttractionHelper
        LinearLayout container = findViewById(R.id.cityContentContainer);

        // Add Statiunea Borsa attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Stațiunea Borșa",
                R.drawable.statiune_borsa, // Make sure this resource exists
                "Părerea ta despre Stațiunea Borșa"
        );

        // Add Cascada Cailor attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Cascada Cailor",
                R.drawable.bega_river, // Make sure this resource exists
                "Părerea ta despre Cascada Cailor"
        );
    }

    @Override
    protected String getCityName() {
        return "Borșa";
    }

    @Override
    protected String getRegionName() {
        return "Maramureș";
    }

    protected String getCityDescription() {
        return "Borșa, una dintre cele mai mari așezări din Maramureș, este o destinație turistică populară atât iarna, pentru sporturile de iarnă, cât și vara pentru drumeții montane, cu Pietrosul Rodnei și Cascada Cailor ca atracții principale.";
    }
}
