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

public class Slatina extends EnhancedCityActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "SlatinaPrefs";
    private static final String USER_IMAGES_KEY = "userImages_Slatina";
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
    private static final String SLATINA_CITY_ID = "666767";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + SLATINA_CITY_ID + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=ro";
    private boolean contentInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Încărcăm doar imaginile salvate pentru Slatina
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
        mapButton.setText("Harta Interactivă Slatina");
        mapButton.setBackgroundColor(Color.parseColor("#3F51B5"));
        mapButton.setTextColor(Color.WHITE);
        mapButton.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        mapButton.setLayoutParams(params);

        mapButton.setOnClickListener(v -> {
            // Open Google Maps with Slatina location
            Uri gmmIntentUri = Uri.parse("geo:44.1500,24.3833?q=Slatina,Romania");
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
        defaultImages.add("oltenia_slatina_1");
        defaultImages.add("oltenia_slatina_centru");
        defaultImages.add("oltenia_slatina_pod");
        defaultImages.add("oltenia_slatina_alro");
        defaultImages.add("oltenia_slatina_muzeu");

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

        // 1. Adăugăm mesajul de bun venit pentru vizitatorii recurenți (temporar)
        if (visitCount > 1) {
            addWelcomeBackMessage();
        }

        // Pregătire date pentru funcționalitățile orașului
        String[] events = {
                "Zilele Municipiului Slatina - Ianuarie 2024",
                "Festivalul de Umor \"Oltenii & Restu' Lumii\" - Mai 2024",
                "Târgul Meșterilor Populari - August 2024",
                "Festivalul Național de Colinde și Obiceiuri de Iarnă - Decembrie 2024"
        };

        String[] tips = {
                "Dealul Grădiște oferă o panoramă frumoasă asupra orașului și a râului Olt",
                "Încercați halvița și braga de la cofetăria \"Atletul Albanez\", una dintre cele mai vechi din țară",
                "Muzeul Județean Olt are colecții interesante de arheologie și etnografie",
                "Podul peste Olt este un loc bun pentru fotografii, mai ales la apus"
        };

        // Adăugăm componentele direct în containerul principal, în ordinea corectă

        // 1. Widget-ul de vreme - adăugăm un tag pentru identificare ușoară
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
        weatherTitle.setText("Vremea în Slatina");
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

        refreshWeatherBtn.setOnClickListener(v -> {
            fetchWeatherData(weatherInfo);
        });

        weatherContent.addView(weatherTitle);
        weatherContent.addView(weatherInfo);
        weatherContent.addView(refreshWeatherBtn);
        weatherWidget.addView(weatherContent);

        specialFeaturesContainer.addView(weatherWidget);

        // Inițializăm datele meteo
        fetchWeatherData(weatherInfo);

        // 2. Butonul de evenimente locale
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
            // Arată dialog cu evenimente
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Evenimente în Slatina");
            builder.setItems(events, (dialog, which) -> {
                Toast.makeText(this, "Eveniment selectat: " + events[which], Toast.LENGTH_SHORT).show();
            });
            builder.show();
        });

        specialFeaturesContainer.addView(eventsButton);

        // 3. Secțiunea de sfaturi locale
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

        // 4. Provocarea foto - adăugăm un tag pentru identificare ușoară
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
        photoTitle.setText("Provocare Foto: Descoperă Slatina");
        photoTitle.setTextSize(18);
        photoTitle.setTextColor(Color.parseColor("#FF8F00"));
        photoTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView photoDesc = new TextView(this);
        photoDesc.setText("Surprinde farmecul industrial și peisajele râului Olt din Slatina!");
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
        welcomeText.setText("Bine ai revenit în Slatina! Aceasta este vizita ta #" + visitCount);
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

                    // Verificăm dacă avem deja 5 imagini pentru Slatina
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
            defaultImages.add("oltenia_slatina_1");
            defaultImages.add("oltenia_slatina_centru");
            defaultImages.add("oltenia_slatina_pod");
            defaultImages.add("oltenia_slatina_alro");
            defaultImages.add("oltenia_slatina_muzeu");

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

        setTitle("Slatina");

        // Add sections
        addSection(
                findViewById(R.id.cityContentContainer),
                "Istorie și Tradiție",
                "Slatina, menționată documentar prima dată în 1368, este unul dintre cele mai vechi orașe din Muntenia. Numele său provine din slavonă („slam tina” - pământ sărat). A fost un important centru meșteșugăresc și comercial, cunoscut pentru târgurile sale. Orașul a cunoscut o dezvoltare industrială masivă în perioada comunistă, devenind un centru major al producției de aluminiu.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Geografie",
                "Situată pe malul stâng al râului Olt, Slatina este reședința județului Olt. Orașul se află în Câmpia Boianului, la contactul cu Podișul Getic. Relieful este predominant de câmpie, cu dealul Grădiște dominând peisajul. Clima este temperat-continentală.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Atracții Turistice",
                "Principalele atracții includ Centrul Vechi (cu clădiri istorice), Muzeul Județean Olt, Catedrala Episcopală, Podul peste Olt (un reper ingineresc important) și Dealul Grădiște, care oferă o panoramă asupra orașului și loc de agrement (Parcul Tineretului).",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Cultură și Economie",
                "Slatina este cunoscută în special pentru uzina ALRO, cel mai mare producător de aluminiu din Europa Centrală și de Est (excluzând Rusia). Orașul are și o viață culturală activă, cu Centrul Cultural \"Eugen Ionescu\", Biblioteca Județeană \"Ion Minulescu\" și diverse festivaluri, precum cel de umor \"Oltenii & Restu' Lumii\".",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Gastronomie",
                "Gastronomia locală include preparate tradiționale muntenești. O specialitate renumită este halvița și braga produse de cofetăria \"Atletul Albanez\", una dintre cele mai vechi din România, fondată în 1912. De asemenea, se pot degusta plăcinte și produse de patiserie locale.",
                false
        );

        // Add attractions using the AttractionHelper
        LinearLayout container = findViewById(R.id.cityContentContainer);

        // Add Muzeul Județean Olt attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Centrul Vechi",
                R.drawable.centru_vechi_slatina,
                "Părerea ta despre Centrul Vechi"
        );

        // Add Dealul Grădiște / Parcul Tineretului attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Strada Lipscani",
                R.drawable.str_lipscan,
                "Părerea ta despre Strada Lipscani"
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
            intent.putExtra("CITY_NAME", "Slatina");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });
    }

    @Override
    protected String getRegionName() {
        return "Oltenia";
    }
}
