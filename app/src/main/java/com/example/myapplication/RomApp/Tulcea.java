package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.view.HapticFeedbackConstants;
import android.widget.TextView;
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
import com.example.myapplication.viewmodel.CityListActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import com.example.myapplication.viewmodel.AttractionHelper;

public class Tulcea extends EnhancedCityActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "TulceaPrefs";
    private static final String USER_IMAGES_KEY = "userImages_Tulcea";
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
    private static final String TULCEA_CITY_ID = "683627";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + TULCEA_CITY_ID + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=ro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Încărcăm doar imaginile salvate pentru Tulcea
        loadSavedImages();

        recordVisit();
        loadVisitCount();
        
        // Configurăm explicit butonul viewCities pentru a ne asigura că funcționează corect
        View viewCitiesButton = findViewById(R.id.viewCitiesButton);
        if (viewCitiesButton != null) {
            viewCitiesButton.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent intent = new Intent(this, CityListActivity.class);
                intent.putExtra(CityListActivity.EXTRA_REGION_NAME, getRegionName());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        } else {
            Log.e("Tulcea", "viewCitiesButton not found in layout");
        }

        specialFeaturesContainer = new LinearLayout(this);
        specialFeaturesContainer.setOrientation(LinearLayout.VERTICAL);
        specialFeaturesContainer.setPadding(32, 16, 32, 16);

        LinearLayout mainContainer = findViewById(R.id.cityContentContainer);
        if (mainContainer != null) {
            mainContainer.addView(specialFeaturesContainer);
        }

        Button mapButton = new Button(this);
        mapButton.setText("Harta Interactivă Tulcea");
        mapButton.setBackgroundColor(Color.parseColor("#2196F3"));
        mapButton.setTextColor(Color.WHITE);
        mapButton.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        mapButton.setLayoutParams(params);

        mapButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:45.1667,28.8?q=Tulcea,Romania");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps nu este instalat", Toast.LENGTH_SHORT).show();
            }
        });

        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
        if (tabLayout != null) {
            ViewGroup parent = (ViewGroup) tabLayout.getParent();
            if (parent != null) {
                parent.addView(mapButton);
            }
        }

        setupSpecialFeatures();
        
        // Verificăm permisiunile înainte de a configura caruselul de imagini
        checkAndRequestPermissions();
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
        specialFeaturesContainer.removeAllViews();

        if (visitCount > 1) {
            addWelcomeBackMessage();
        }

        MaterialCardView weatherWidget = new MaterialCardView(this);
        weatherWidget.setTag("weather_widget");
        weatherWidget.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        weatherWidget.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        weatherWidget.setRadius(16);
        weatherWidget.setCardElevation(4);
        weatherWidget.setContentPadding(16, 16, 16, 16);

        LinearLayout weatherContent = new LinearLayout(this);
        weatherContent.setOrientation(LinearLayout.VERTICAL);

        TextView weatherTitle = new TextView(this);
        weatherTitle.setText("Vremea în Tulcea");
        weatherTitle.setTextSize(18);
        weatherTitle.setTextColor(Color.parseColor("#1565C0"));
        weatherTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView weatherInfo = new TextView(this);
        weatherInfo.setText("Se încarcă informații despre vreme...");
        weatherInfo.setTextSize(16);

        Button refreshWeatherBtn = new Button(this);
        refreshWeatherBtn.setText("Actualizează");
        refreshWeatherBtn.setBackgroundColor(Color.parseColor("#2196F3"));
        refreshWeatherBtn.setTextColor(Color.WHITE);

        weatherContent.addView(weatherTitle);
        weatherContent.addView(weatherInfo);
        weatherContent.addView(refreshWeatherBtn);
        weatherWidget.addView(weatherContent);

        specialFeaturesContainer.addView(weatherWidget);

        // Add click listener to refresh weather button
        refreshWeatherBtn.setOnClickListener(v -> {
            fetchWeatherData(weatherInfo);
        });

        // Fetch weather data initially
        fetchWeatherData(weatherInfo);

        MaterialCardView photoCard = new MaterialCardView(this);
        photoCard.setTag("photo_challenge");
        photoCard.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        photoCard.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
        photoCard.setRadius(16);
        photoCard.setCardElevation(4);
        photoCard.setContentPadding(16, 16, 16, 16);

        LinearLayout photoContent = new LinearLayout(this);
        photoContent.setOrientation(LinearLayout.VERTICAL);

        TextView photoTitle = new TextView(this);
        photoTitle.setText("Provocare Foto: Capturați Tulcea");
        photoTitle.setTextSize(18);
        photoTitle.setTextColor(Color.parseColor("#FF8F00"));
        photoTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView photoDesc = new TextView(this);
        photoDesc.setText("Capturați cele mai frumoase locuri din Tulcea și împărtășiți-le cu noi!");
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

        String[] events = {
                "Festivalul Deltei - 10-12 Iulie 2024",
                "Concert la Tulcea - 18 Iulie 2024",
                "Expoziție de Artă - 1-15 August 2024",
                "Festivalul Pescăresc - 20-22 August 2024"
        };

        String[] tips = {
                "Vizitați Muzeul Deltei Dunării pentru o experiență unică",
                "Faceți o plimbare cu barca pe canalele Deltei",
                "Explorați rezervațiile naturale din apropiere",
                "Încercați preparatele locale din pește proaspăt"
        };

        Button eventsButton = new Button(this);
        eventsButton.setText("Evenimente Locale");
        eventsButton.setBackgroundColor(Color.parseColor("#4CAF50"));
        eventsButton.setTextColor(Color.WHITE);
        eventsButton.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams eventsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        eventsParams.setMargins(0, 8, 0, 8);
        eventsButton.setLayoutParams(eventsParams);

        eventsButton.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Evenimente în Tulcea");
            builder.setItems(events, (dialog, which) -> {
                Toast.makeText(this, "Eveniment selectat: " + events[which], Toast.LENGTH_SHORT).show();
            });
            builder.show();
        });

        specialFeaturesContainer.addView(eventsButton);

        MaterialCardView tipsCard = new MaterialCardView(this);
        tipsCard.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tipsCard.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
        tipsCard.setRadius(16);
        tipsCard.setCardElevation(4);
        tipsCard.setContentPadding(16, 16, 16, 16);

        LinearLayout tipsContent = new LinearLayout(this);
        tipsContent.setOrientation(LinearLayout.VERTICAL);

        TextView tipsTitle = new TextView(this);
        tipsTitle.setText("Sfaturi Locale");
        tipsTitle.setTextSize(18);
        tipsTitle.setTextColor(Color.parseColor("#2E7D32"));
        tipsTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        tipsContent.addView(tipsTitle);

        for (String tip : tips) {
            TextView tipView = new TextView(this);
            tipView.setText("• " + tip);
            tipView.setTextSize(16);
            tipView.setPadding(0, 8, 0, 0);
            tipsContent.addView(tipView);
        }

        tipsCard.addView(tipsContent);
        specialFeaturesContainer.addView(tipsCard);
    }

    private void fetchWeatherData(TextView weatherInfo) {
        // Show loading message
        weatherInfo.setText("Se încarcă informații despre vreme...");
        
        // Create a new thread to perform network operations
        new Thread(() -> {
            try {
                // Create URL object
                java.net.URL url = new java.net.URL(WEATHER_API_URL);
                
                // Open connection
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                // Get response code
                int responseCode = connection.getResponseCode();
                
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    // Read the response
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    // Parse JSON response
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                    
                    // Extract weather information
                    org.json.JSONObject main = jsonResponse.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    int humidity = main.getInt("humidity");
                    
                    org.json.JSONArray weatherArray = jsonResponse.getJSONArray("weather");
                    org.json.JSONObject weather = weatherArray.getJSONObject(0);
                    String description = weather.getString("description");
                    
                    // Format weather information
                    final String weatherText = String.format("Temperatura: %.1f°C\nUmiditate: %d%%\nCondiții: %s",
                            temperature, humidity, description);
                    
                    // Update UI on the main thread
                    handler.post(() -> {
                        weatherInfo.setText(weatherText);
                    });
                } else {
                    // Handle error
                    handler.post(() -> {
                        weatherInfo.setText("Eroare la încărcarea datelor despre vreme. Cod: " + responseCode);
                    });
                }
            } catch (Exception e) {
                // Handle exceptions
                handler.post(() -> {
                    weatherInfo.setText("Eroare: " + e.getMessage());
                });
            }
        }).start();
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
        welcomeText.setText("Bine ai revenit în Tulcea! Aceasta este vizita ta #" + visitCount);
        welcomeText.setTextSize(18);
        welcomeText.setTextColor(Color.parseColor("#1565C0"));

        cardContent.addView(welcomeText);
        welcomeCard.addView(cardContent);
        specialFeaturesContainer.addView(welcomeCard);

        handler.postDelayed(() -> {
            specialFeaturesContainer.removeView(welcomeCard);
        }, 5000);
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
        defaultImages.add("dobrogea_tulcea_1");
        defaultImages.add("dobrogea_tulcea_delta");
        defaultImages.add("dobrogea_tulcea_muzeu");
        defaultImages.add("dobrogea_tulcea_cimitir");
        defaultImages.add("dobrogea_tulcea_biserica");
        
        ViewPager2 viewPager = findViewById(R.id.imageCarousel);
        if (viewPager != null) {
            com.example.myapplication.adapter.ImageCarouselAdapter adapter =
                    new com.example.myapplication.adapter.ImageCarouselAdapter(this, defaultImages);
            
            viewPager.setAdapter(adapter);
            
            TabLayout tabLayout = findViewById(R.id.imageIndicator);
            if (tabLayout != null) {
                new TabLayoutMediator(tabLayout, viewPager,
                        (tab, position) -> {}).attach();
            }
        }
    }

    private void setupImageCarousel() {
        ArrayList<String> images = getCityImages();
        if (images != null && !images.isEmpty()) {
            ViewPager2 viewPager = findViewById(R.id.imageCarousel);
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

                TabLayout tabLayout = findViewById(R.id.imageIndicator);
                if (tabLayout != null) {
                    new TabLayoutMediator(tabLayout, viewPager,
                            (tab, position) -> {}).attach();
                }
            } else {
                Log.e("Tulcea", "ViewPager2 not found in layout");
            }
        } else {
            Log.e("Tulcea", "No images available for carousel");
        }
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();

        // Adăugăm doar ultimele 5 imagini din userImages pentru Tulcea
        if (userImages != null && !userImages.isEmpty()) {
            int startIndex = Math.max(0, userImages.size() - 5);
            images.addAll(userImages.subList(startIndex, userImages.size()));
        }

        // Adăugăm imaginile predefinite doar dacă nu avem 5 imagini de utilizator
        if (images.size() < 5) {
            ArrayList<String> defaultImages = new ArrayList<>();
            defaultImages.add("dobrogea_tulcea_1");
            defaultImages.add("dobrogea_tulcea_delta");
            defaultImages.add("dobrogea_tulcea_muzeu");
            defaultImages.add("dobrogea_tulcea_cimitir");
            defaultImages.add("dobrogea_tulcea_biserica");

            // Adăugăm doar câte imagini predefinite sunt necesare pentru a ajunge la 5
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

        setTitle("Tulcea");
/*
        addSection(
                findViewById(R.id.cityContentContainer),
                "Istorie și Tradiție",
                "Tulcea este unul dintre cele mai vechi orașe din România, cu origini datând din perioada greacă și romană, cunoscut în antichitate sub numele de Aegyssus. A fost un important centru militar și comercial în timpul Imperiului Roman și Bizantin. Orașul are o moștenire culturală diversă, influențată de populațiile turce, grecești, române și lipovene.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Delta Dunării",
                "Tulcea este poarta de intrare în Delta Dunării, un paradis natural aflat în patrimoniul mondial UNESCO. De aici pleacă excursii cu barca spre canale, lacuri și colonii de păsări unice. Delta oferă peisaje spectaculoase și o biodiversitate remarcabilă, fiind o atracție majoră pentru ecoturism.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Atracții Turistice",
                "Printre cele mai cunoscute atracții din Tulcea se numără: Muzeul Deltei Dunării, Acvariul, Monumentul Eroilor Independenței, Cetatea Aegyssus și Catedrala \"Sfântul Nicolae\". Orașul oferă și o promenadă modernă pe faleza Dunării, ideală pentru plimbări și activități recreative.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Tradiții Multietnice",
                "Tulcea este un exemplu de conviețuire multietnică. Comunitățile de ruși lipoveni, ucraineni, turci, tătari și aromâni păstrează tradițiile și obiceiurile lor, vizibile în arhitectură, gastronomie și sărbători locale. Festivalurile etnice aduc în prim-plan dansuri, muzică și costume tradiționale.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Plimbări și Natură",
                "În apropierea orașului se află dealurile Tulcei, Pădurea Babadag și rezervațiile naturale din zona Razim-Sinoe. Se pot face drumeții ușoare sau excursii foto în natură. Tulcea este și un loc excelent pentru birdwatching, în special în lunile de primăvară și vară.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Gastronomie Locală",
                "Bucătăria tulceană este puternic influențată de cultura pescărească și de mozaicul etnic al regiunii. Delicatesele locale includ storceagul de sturion, saramura de crap, plachia de pește, chiftele de pește lipovenești, dar și baclavale și sarailii din tradiția orientală.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Evenimente Locale Sugestive",
                "Festivalul Peștelui și al Vinului - Septembrie\n\nZilele Orașului Tulcea - Iunie\n\nFestivalul Minorităților \"Serbările Deltei\" - August\n\nFestivalul Internațional al Dunării și Deltei - Iulie",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Sfaturi Locale",
                "Vizitați Muzeul Deltei înainte de excursiile în natură - oferă o imagine clară a florei și faunei din zonă.\n\nRezervați din timp excursiile cu barca în deltă - locurile bune se ocupă repede, mai ales vara.\n\nPlimbați-vă pe faleza Dunării la apus - atmosfera este liniștită și panoramică.\n\nÎncercați storceagul preparat de localnici în satele din deltă pentru gustul autentic.",
                false
        );
        */
        // Add attractions using the AttractionHelper
        LinearLayout container = findViewById(R.id.cityContentContainer);
        
        // Add Delta Dunării attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Delta Dunării",
                R.drawable.delta_dunarii, // Using existing drawable
                "Părerea ta despre Delta Dunării"
        );
        
        // Add Muzeul Deltei attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Muzeul Deltei Dunării",
                R.drawable.muzeu_delta_dunarii, // Using existing drawable
                "Părerea ta despre Muzeul Deltei"
        );
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
                    
                    // Verificăm dacă avem deja 5 imagini pentru Tulcea
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
    protected String getRegionName() {
        return "Dobrogea";
    }
}
