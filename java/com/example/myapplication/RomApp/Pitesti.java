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

public class Pitesti extends EnhancedCityActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "PitestiPrefs";
    private static final String USER_IMAGES_KEY = "userImages_Pitesti";
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
    private static final String PITESTI_CITY_ID = "671492"; // Pitesti city ID
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + PITESTI_CITY_ID + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=ro";
    private boolean contentInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply muted theme styling
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#F7F3E9")); // Light cream background

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Încărcăm doar imaginile salvate pentru Pitesti
        loadSavedImages();

        recordVisit();
        loadVisitCount();

        specialFeaturesContainer = new LinearLayout(this);
        specialFeaturesContainer.setOrientation(LinearLayout.VERTICAL);
        specialFeaturesContainer.setPadding(32, 16, 32, 16);

        LinearLayout mainContainer = findViewById(R.id.cityContentContainer);
        if (mainContainer != null) {
            mainContainer.addView(specialFeaturesContainer);
            // Apply subtle gradient background
            mainContainer.setBackgroundResource(R.drawable.gradient_background);
        }

        // Adăugăm butonul de hartă interactivă imediat după caruselul de imagini
        Button mapButton = new Button(this);
        mapButton.setText("Harta Interactivă Pitești");
        mapButton.setBackgroundColor(Color.parseColor("#3F51B5"));
        mapButton.setTextColor(Color.WHITE);
        mapButton.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        mapButton.setLayoutParams(params);

        mapButton.setOnClickListener(v -> {
            // Open Google Maps with Pitesti location
            Uri gmmIntentUri = Uri.parse("geo:44.8560,24.8690?q=Pitesti,Romania");
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
        defaultImages.add("muntenia_pitesti_primarie");
        defaultImages.add("muntenia_pitesti_centru");
        defaultImages.add("muntenia_pitesti_parc");
        defaultImages.add("muntenia_pitesti_catedrala");
        defaultImages.add("muntenia_pitesti_trivale");

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
        if (contentInitialized) return;
        contentInitialized = true;

        // Adăugăm un card de vreme
        MaterialCardView weatherCard = new MaterialCardView(this);
        weatherCard.setCardElevation(4);
        weatherCard.setRadius(16);
        weatherCard.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 24);
        weatherCard.setLayoutParams(cardParams);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 16, 24, 16);

        TextView weatherTitle = new TextView(this);
        weatherTitle.setText("Vremea în Pitești");
        weatherTitle.setTextSize(18);
        weatherTitle.setTextColor(Color.parseColor("#1565C0"));
        weatherTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView weatherInfoView = new TextView(this);
        weatherInfoView.setText("Se încarcă datele meteo...");
        weatherInfoView.setTextSize(16);
        weatherInfoView.setPadding(0, 8, 0, 0);

        cardContent.addView(weatherTitle);
        cardContent.addView(weatherInfoView);
        weatherCard.addView(cardContent);
        specialFeaturesContainer.addView(weatherCard);

        // Încărcăm datele meteo
        fetchWeatherData(weatherInfoView);

        // Adăugăm un separator
        View separator = new View(this);
        separator.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        separatorParams.setMargins(0, 0, 0, 24);
        separator.setLayoutParams(separatorParams);
        specialFeaturesContainer.addView(separator);

        // Adăugăm un card de evenimente
        MaterialCardView eventsCard = new MaterialCardView(this);
        eventsCard.setCardElevation(4);
        eventsCard.setRadius(16);
        eventsCard.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
        eventsCard.setLayoutParams(cardParams);

        LinearLayout eventsContent = new LinearLayout(this);
        eventsContent.setOrientation(LinearLayout.VERTICAL);
        eventsContent.setPadding(24, 16, 24, 16);

        TextView eventsTitle = new TextView(this);
        eventsTitle.setText("Evenimente curente");
        eventsTitle.setTextSize(18);
        eventsTitle.setTextColor(Color.parseColor("#F57F17"));
        eventsTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        // Listă de evenimente
        final String[] events = {
                "Simfonia Lalelelor - 20-22 Aprilie 2024",
                "Zilele Orașului Pitești - 15-17 Mai 2024",
                "Festivalul Internațional de Folclor - 10-12 August 2024",
                "Festivalul Underground - 5-6 Iulie 2024",
                "Expoziție Auto Retro - 25-27 Septembrie 2024"
        };

        TextView eventListView = new TextView(this);
        StringBuilder eventList = new StringBuilder();
        for (String event : events) {
            eventList.append("• ").append(event).append("\n");
        }
        eventListView.setText(eventList.toString());
        eventListView.setTextSize(16);
        eventListView.setPadding(0, 8, 0, 8);

        Button showMoreButton = new Button(this);
        showMoreButton.setText("Vezi toate evenimentele");
        showMoreButton.setBackgroundColor(Color.parseColor("#F57F17"));
        showMoreButton.setTextColor(Color.WHITE);

        showMoreButton.setOnClickListener(v -> {
            // Show events dialog or activity
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Evenimente în Pitești");
            builder.setItems(events, (dialog, which) -> {
                Toast.makeText(this, "Eveniment selectat: " + events[which], Toast.LENGTH_SHORT).show();
                
                // Aici am putea deschide o activitate cu mai multe detalii despre eveniment
            });
            builder.show();
        });

        eventsContent.addView(eventsTitle);
        eventsContent.addView(eventListView);
        eventsContent.addView(showMoreButton);
        eventsCard.addView(eventsContent);
        specialFeaturesContainer.addView(eventsCard);

        // Adăugăm un separator
        View separator2 = new View(this);
        separator2.setBackgroundColor(Color.parseColor("#E0E0E0"));
        separator2.setLayoutParams(separatorParams);
        specialFeaturesContainer.addView(separator2);

        // Adăugăm secțiunea de sfaturi locale
        MaterialCardView tipsCard = new MaterialCardView(this);
        tipsCard.setCardElevation(4);
        tipsCard.setRadius(16);
        tipsCard.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
        tipsCard.setLayoutParams(cardParams);

        LinearLayout tipsContent = new LinearLayout(this);
        tipsContent.setOrientation(LinearLayout.VERTICAL);
        tipsContent.setPadding(24, 16, 24, 16);

        TextView tipsTitle = new TextView(this);
        tipsTitle.setText("Sfaturi Locale");
        tipsTitle.setTextSize(18);
        tipsTitle.setTextColor(Color.parseColor("#2E7D32"));
        tipsTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        // Lista de sfaturi pentru Pitești
        String[] tips = {
                "Vizitați Pădurea Trivale - un adevărat plămân verde al orașului, perfect pentru plimbări și picnic",
                "Nu ratați evenimentul anual 'Simfonia Lalelelor' în luna aprilie - un spectacol floral deosebit",
                "Centrul istoric oferă restaurante excelente cu preparate tradiționale argeșene",
                "Muzeul Județean Argeș găzduiește colecții impresionante de artă populară și istorie locală"
        };

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

        // Adăugăm un separator
        View separator3 = new View(this);
        separator3.setBackgroundColor(Color.parseColor("#E0E0E0"));
        separator3.setLayoutParams(separatorParams);
        specialFeaturesContainer.addView(separator3);

        // Adăugăm un card pentru provocarea foto
        MaterialCardView photoCard = new MaterialCardView(this);
        photoCard.setCardElevation(4);
        photoCard.setRadius(16);
        photoCard.setCardBackgroundColor(Color.parseColor("#FBE9E7"));
        photoCard.setLayoutParams(cardParams);

        LinearLayout photoContent = new LinearLayout(this);
        photoContent.setOrientation(LinearLayout.VERTICAL);
        photoContent.setPadding(24, 16, 24, 16);

        TextView photoTitle = new TextView(this);
        photoTitle.setText("Provocare Foto: Capturați Pitești");
        photoTitle.setTextSize(18);
        photoTitle.setTextColor(Color.parseColor("#FF8F00"));
        photoTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView photoDesc = new TextView(this);
        photoDesc.setText("Capturați cele mai frumoase locuri din Pitești și împărtășiți-le cu noi!");
        photoDesc.setTextSize(16);
        photoDesc.setPadding(0, 8, 0, 16);

        Button addPhotoButton = new Button(this);
        addPhotoButton.setText("Adaugă o fotografie");
        addPhotoButton.setBackgroundColor(Color.parseColor("#FF8F00"));
        addPhotoButton.setTextColor(Color.WHITE);

        addPhotoButton.setOnClickListener(v -> {
            // Check if we have permission to access storage
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }

            // Open gallery to pick image
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        photoContent.addView(photoTitle);
        photoContent.addView(photoDesc);
        photoContent.addView(addPhotoButton);
        photoCard.addView(photoContent);
        specialFeaturesContainer.addView(photoCard);

        // Afișăm un mesaj de bun venit dacă este cazul (prima vizită sau după o perioadă mai lungă)
        long lastVisit = sharedPreferences.getLong(LAST_VISIT_KEY, 0);
        long currentTime = System.currentTimeMillis();
        
        // Dacă este prima vizită sau au trecut mai mult de 7 zile
        if (lastVisit == 0 || (currentTime - lastVisit) > 7 * 24 * 60 * 60 * 1000) {
            addWelcomeBackMessage();
        }
    }

    private void fetchWeatherData(TextView weatherInfoView) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(WEATHER_API_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject main = jsonObject.getJSONObject("main");
                        JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);

                        double temp = main.getDouble("temp");
                        double feelsLike = main.getDouble("feels_like");
                        int humidity = main.getInt("humidity");
                        String description = weather.getString("description");

                        String weatherText = String.format(
                                "Temperatura: %.1f°C\nSe simte ca: %.1f°C\nUmiditate: %d%%\nCondiții: %s",
                                temp, feelsLike, humidity, description);

                        weatherInfoView.setText(weatherText);
                    } catch (Exception e) {
                        weatherInfoView.setText("Eroare la preluarea datelor meteo.");
                    }
                } else {
                    weatherInfoView.setText("Nu s-au putut încărca datele meteo. Verificați conexiunea la internet.");
                }
            }
        }.execute();
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
        welcomeText.setText("Bine ai revenit în Pitești! Aceasta este vizita ta #" + visitCount);
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

                    // Verificăm dacă avem deja 5 imagini pentru Pitești
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
            defaultImages.add("muntenia_pitesti_primarie");
            defaultImages.add("muntenia_pitesti_centru");
            defaultImages.add("muntenia_pitesti_parc");
            defaultImages.add("muntenia_pitesti_catedrala");
            defaultImages.add("muntenia_pitesti_trivale");

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

        setTitle("Pitești");

        /* Add sections
        addSection(
                findViewById(R.id.cityContentContainer),
                "Istorie și Tradiție",
                "Orașul Pitești, reședința județului Argeș, are o istorie bogată ce datează din secolele XIV-XV. Prima atestare documentară a localității datează din 20 mai 1388, într-un act emis de domnitorul Mircea cel Bătrân. De-a lungul istoriei, Piteștiul a fost un important centru comercial și meșteșugăresc, beneficiind de poziția strategică pe drumul ce lega capitala Țării Românești de Transilvania.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Geografie",
                "Piteștiul este situat în partea central-sudică a României, în zona de contact dintre Subcarpații Getici și Câmpia Română, pe malurile râului Argeș. Cu o suprafață de aproximativ 40 km², orașul se află la o altitudine medie de 250-300 m. Clima este temperat-continentală, cu veri calde și ierni relativ moderate, influențată de poziția sa în zona de adăpost a dealurilor subcarpatice.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Atracții Turistice",
                "Principalele atracții din Pitești includ Centrul Civic cu Primăria și fântânile sale muzicale, Parcul Ștrand, Muzeul Județean Argeș, Galeria de Artă și Catedrala 'Sfântul Gheorghe'. În afara orașului, turiștii pot vizita Pădurea Trivale, o zonă naturală de recreere cu trasee pentru drumeții și sporturi în aer liber. La aproximativ 30 km de Pitești se află renumita Mănăstire Curtea de Argeș, un monument istoric și arhitectural de excepție.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Cultură",
                "Piteștiul oferă un peisaj cultural variat, cu instituții precum Teatrul 'Alexandru Davila', Filarmonica Pitești și Centrul Cultural Județean Argeș. Evenimentul cultural emblematic al orașului este 'Simfonia Lalelelor', un festival internațional de floricultură organizat anual din 1978, când orașul se transformă într-o adevărată expoziție florală în aer liber. De asemenea, Festivalul Internațional de Folclor și Zilele Municipiului Pitești sunt evenimente importante în agenda culturală a orașului.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Gastronomie",
                "Bucătăria tradițională din zona Piteștiului oferă specialități culinare specifice Munteniei, îmbogățite cu influențe locale argeșene. Printre preparatele reprezentative se numără plachia de pește, sarmalele în foi de viță, tochitură argeșeană și ciorba de bureți. Zona este renumită pentru livezile sale de pomi fructiferi și podgoriile din împrejurimi, în special cele de la Ștefănești, care produc vinuri de calitate superioară precum Feteasca Regală și Tămâioasa Românească.",
                false
        );
*/
        // Add attractions using the AttractionHelper
        LinearLayout container = findViewById(R.id.cityContentContainer);

        // Add Primaria attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Primăria Pitești",
                R.drawable.primaria_pitesti, // Make sure this resource exists
                "Părerea ta despre Primăria Pitești"
        );

        // Add Parcul Strand attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Parcul Ștrand",
                R.drawable.parcul_strand, // Make sure this resource exists
                "Părerea ta despre Parcul Ștrand"
        );
    }

    @Override
    protected void addSection(LinearLayout container, String title, String content, boolean isHighlighted) {
        // Utilizăm implementarea din clasa părinte pentru a crea secțiunea
        super.addSection(container, title, content, isHighlighted);

        // Obținem ultima secțiune adăugată (cea creată acum)
        View sectionView = container.getChildAt(container.getChildCount() - 1);

        // Adăugăm listener pentru click pe secțiune pentru a deschide SectionPreviewActivity
        sectionView.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.myapplication.viewmodel.SectionPreviewActivity.class);
            intent.putExtra("SECTION_TITLE", title);
            intent.putExtra("SECTION_CONTENT", content);
            intent.putExtra("CITY_NAME", "Pitești");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });
    }

    @Override
    protected String getRegionName() {
        return "Muntenia";
    }
}
