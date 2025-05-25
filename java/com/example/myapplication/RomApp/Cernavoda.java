package com.example.myapplication.RomApp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.HapticFeedbackConstants;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.viewmodel.EnhancedCityActivity;
import com.example.myapplication.viewmodel.AttractionHelper;
import com.example.myapplication.viewmodel.CityListActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Cernavoda extends EnhancedCityActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "CernavodaPrefs";
    private static final String USER_IMAGES_KEY = "userImages_Cernavoda";
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
    
    // Adăugăm constante pentru API-ul de vreme
    private static final String WEATHER_API_KEY = "1234567890abcdef1234567890abcdef";
    private static final String CERNAVODA_CITY_ID = "681799";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + CERNAVODA_CITY_ID + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=ro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Încărcăm doar imaginile salvate pentru Cernavoda
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
            Log.e("Cernavoda", "viewCitiesButton not found in layout");
        }

        specialFeaturesContainer = new LinearLayout(this);
        specialFeaturesContainer.setOrientation(LinearLayout.VERTICAL);
        specialFeaturesContainer.setPadding(32, 16, 32, 16);

        LinearLayout mainContainer = findViewById(R.id.cityContentContainer);
        if (mainContainer != null) {
            mainContainer.addView(specialFeaturesContainer);
        }

        Button mapButton = new Button(this);
        mapButton.setText("Harta Interactivă Cernavoda");
        mapButton.setBackgroundColor(Color.parseColor("#2196F3"));
        mapButton.setTextColor(Color.WHITE);
        mapButton.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        mapButton.setLayoutParams(params);

        mapButton.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:44.3436,28.0362?q=Cernavoda,Romania");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps nu este instalat", Toast.LENGTH_SHORT).show();
            }
        });

        TabLayout tabLayout = findViewById(R.id.imageIndicator);
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

    @Override
    protected String getRegionName() {
        return "Dobrogea";
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
        
        // Adăugăm widget pentru vreme
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
        weatherTitle.setText("Vremea în Cernavoda");
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

        MaterialCardView localGuidesCard = new MaterialCardView(this);
        localGuidesCard.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        localGuidesCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        localGuidesCard.setRadius(16);
        localGuidesCard.setCardElevation(4);
        localGuidesCard.setContentPadding(16, 16, 16, 16);

        LinearLayout guidesContent = new LinearLayout(this);
        guidesContent.setOrientation(LinearLayout.VERTICAL);

        TextView guidesTitle = new TextView(this);
        guidesTitle.setText("Ghiduri Locale Cernavoda");
        guidesTitle.setTextSize(18);
        guidesTitle.setTextColor(Color.parseColor("#1565C0"));
        guidesTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView guidesInfo = new TextView(this);
        guidesInfo.setText("Descoperă Cernavoda cu ajutorul ghizilor locali!");
        guidesInfo.setTextSize(16);

        Button contactGuidesBtn = new Button(this);
        contactGuidesBtn.setText("Contact Ghizi");
        contactGuidesBtn.setBackgroundColor(Color.parseColor("#2196F3"));
        contactGuidesBtn.setTextColor(Color.WHITE);

        guidesContent.addView(guidesTitle);
        guidesContent.addView(guidesInfo);
        guidesContent.addView(contactGuidesBtn);
        localGuidesCard.addView(guidesContent);

        specialFeaturesContainer.addView(localGuidesCard);

        contactGuidesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0723456789"));
            startActivity(intent);
        });

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
        photoTitle.setText("Provocare Foto: Capturați Cernavoda");
        photoTitle.setTextSize(18);
        photoTitle.setTextColor(Color.parseColor("#FF8F00"));
        photoTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView photoDesc = new TextView(this);
        photoDesc.setText("Capturați cele mai frumoase locuri din Cernavoda și împărtășiți-le cu noi!");
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
                "Festivalul Dunării - 15-17 Iulie 2024",
                "Concert în Cernavoda - 22 Iulie 2024",
                "Expoziție de Fotografie Locală - 5-20 August 2024",
                "Festivalul Centrală Nucleară - 25-27 August 2024"
        };

        String[] tips = {
                "Vizitați Podul lui Anghel Saligny pentru o priveliște panoramică",
                "Explorați canalul Dunăre-Marea Neagră",
                "Încercați produsele locale în piața din centrul orașului",
                "Faceți o excursie la Muzeul Daciei Romane"
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
            builder.setTitle("Evenimente în Cernavoda");
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
    
    // Implementăm metoda pentru obținerea datelor meteo
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

        TextView welcomeText = new TextView(this);
        welcomeText.setText("Bine ai revenit în Cernavoda! Aceasta este vizita ta #" + visitCount);
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
        defaultImages.add("dobrogea_cernavoda_pod");
        defaultImages.add("dobrogea_cernavoda_muzeu");
        defaultImages.add("dobrogea_cernavoda_biserica");
        defaultImages.add("dobrogea_cernavoda_faleza");
        defaultImages.add("dobrogea_cernavoda_canal");
        
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
                Log.e("Cernavoda", "ViewPager2 not found in layout");
            }
        } else {
            Log.e("Cernavoda", "No images available for carousel");
        }
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();

        // Adăugăm doar ultimele 5 imagini din userImages pentru Cernavoda
        if (userImages != null && !userImages.isEmpty()) {
            int startIndex = Math.max(0, userImages.size() - 5);
            images.addAll(userImages.subList(startIndex, userImages.size()));
        }

        // Adăugăm imaginile predefinite doar dacă nu avem 5 imagini de utilizator
        if (images.size() < 5) {
            ArrayList<String> defaultImages = new ArrayList<>();
            defaultImages.add("dobrogea_cernavoda_pod");
            defaultImages.add("dobrogea_cernavoda_muzeu");
            defaultImages.add("dobrogea_cernavoda_biserica");
            defaultImages.add("dobrogea_cernavoda_faleza");
            defaultImages.add("dobrogea_cernavoda_canal");

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

        setTitle("Cernavoda");

        addSection(
                findViewById(R.id.cityContentContainer),
                "Istorie și Patrimoniu",
                "Cernavoda este un oraș cu o istorie bogată, situat la intersecția Dunării cu Canalul Dunăre-Marea Neagră. Încă din antichitate, zona a fost locuită de traci și apoi de romani, dovadă fiind numeroasele artefacte descoperite în regiune, inclusiv vestigiile cetății Axiopolis. Orașul modern a căpătat importanță odată cu construirea podului lui Anghel Saligny (1895), o capodoperă a ingineriei din acea vreme.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Podul lui Anghel Saligny",
                "Podul peste Dunăre, proiectat de inginerul Anghel Saligny și finalizat în 1895, a fost la momentul construirii cel mai lung pod din Europa și al treilea din lume. Cu o lungime de 4088 metri, acesta a reprezentat o realizare inginerească remarcabilă pentru acea vreme. Astăzi, podul vechi este monument istoric și stă mărturie măiestriei inginerești românești.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Muzeul Daco-Roman",
                "Muzeul din Cernavoda adăpostește o colecție impresionantă de artefacte preistorice și romane, inclusiv vestigii ale culturii Hamangia, renumită pentru statuetele 'Gânditorul' și 'Femeia șezând'. Colecția include și numeroase obiecte descoperite în fostul oraș roman Axiopolis, oferind o perspectivă fascinantă asupra istoriei locale.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Centrala Nucleară",
                "Cernavoda găzduiește singura centrală nucleară din România, un complex industrial important pentru economia țării. Centrala produce aproximativ 20% din electricitatea României și este considerată una dintre cele mai sigure din lume, fiind construită cu tehnologie CANDU (Canadian Deuterium Uranium).",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Canalul Dunăre-Marea Neagră",
                "Canalul Dunăre-Marea Neagră, a cărui construcție a fost finalizată în 1984, traversează orașul Cernavoda. Cu o lungime de 64 km, canalul scurtează cu aproape 400 km ruta navelor comerciale de la Marea Neagră spre Europa Centrală, reprezentând o cale navigabilă importantă pentru transportul de mărfuri.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Gastronomie Locală",
                "Fiind situat la confluența Dunării cu canalul spre Marea Neagră, Cernavoda oferă o gastronomie bogată în preparate din pește. Specialitățile locale includ ciorba de pește, saramura de crap, plachie dobrogean și peștele la grătar preparat în stil tradițional dobrogean. Aceste delicii culinare pot fi savurate în restaurantele locale de pe malul Dunării.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Evenimente Locale",
                "Zilele Orașului Cernavoda - August\n\nFestivalul Pescăresc - Iulie\n\nFestivalul Multicultural Dobrogean - Septembrie\n\nRegata Dunării - Iunie",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Sfaturi pentru Vizitatori",
                "Cea mai bună perioadă pentru vizitarea orașului este între mai și septembrie.\n\nAlocați cel puțin o jumătate de zi pentru Muzeul Daco-Roman.\n\nFaceți o plimbare pe faleza Dunării, în special la apus.\n\nLa Cernavoda puteți găsi cazare mai accesibilă față de localitățile de pe litoral, fiind la doar 40 km de Constanța.",
                false
        );
        
        // Add attractions using the AttractionHelper
        LinearLayout container = findViewById(R.id.cityContentContainer);
        
        // Add Podul Cernavoda attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Podul Anghel Saligny",
                R.drawable.podul_anghel, // Using existing drawable
                "Părerea ta despre Podul Cernavoda"
        );
        
        // Add Muzeul Daciei attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Muzeul Daco-Roman",
                R.drawable.muzeu_draco_roman, // Using existing drawable
                "Părerea ta despre Muzeul Daciei"
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
                    
                    // Verificăm dacă avem deja 5 imagini pentru Cernavoda
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
}