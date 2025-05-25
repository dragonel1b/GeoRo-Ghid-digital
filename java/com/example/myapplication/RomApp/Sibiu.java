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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sibiu extends EnhancedCityActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "SibiuPrefs";
    private static final String USER_IMAGES_KEY = "userImages_Sibiu";
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
    private static final String SIBIU_CITY_ID = "667268";
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + SIBIU_CITY_ID + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=ro";
    private boolean contentInitialized = false;
    private FloatingActionButton addPhotoFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply muted theme styling - third color set for Sibiu
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#F8F4F9")); // Light lavender background

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Încărcăm doar imaginile salvate pentru Sibiu
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
        mapButton.setText("Harta Interactivă Sibiu");
        mapButton.setBackgroundColor(Color.parseColor("#9A8C98")); // Muted purple
        mapButton.setTextColor(Color.WHITE);
        mapButton.setPadding(32, 16, 32, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        mapButton.setLayoutParams(params);

        mapButton.setOnClickListener(v -> {
            // Open Google Maps with Sibiu location
            Uri gmmIntentUri = Uri.parse("geo:45.7983,24.1469?q=Sibiu,Romania");
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
        
        // Inițializăm FloatingActionButton înainte de a seta parametrii
        addPhotoFab = new FloatingActionButton(this);
        addPhotoFab.setImageResource(android.R.drawable.ic_input_add);
        addPhotoFab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF8F00")));
        addPhotoFab.setSize(FloatingActionButton.SIZE_NORMAL);
        
        // Setăm layoutParams pentru a poziționa butonul în colțul din dreapta, mai sus
        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams fabParams = 
                new androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        fabParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        fabParams.setMargins(0, 0, 32, 300);  // Marginea în dreapta și mult mai sus (300dp de la partea de jos)
        addPhotoFab.setLayoutParams(fabParams);
        
        // Adăugăm listener pentru deschiderea galeriei
        addPhotoFab.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        
        // Obținem CoordinatorLayout-ul principal corect, fără cast periculos
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        if (rootView != null && rootView.getChildCount() > 0) {
            View firstChild = rootView.getChildAt(0);
            if (firstChild instanceof androidx.coordinatorlayout.widget.CoordinatorLayout) {
                // Dacă primul copil este CoordinatorLayout, adăugăm FAB-ul direct
                ((androidx.coordinatorlayout.widget.CoordinatorLayout) firstChild).addView(addPhotoFab);
            } else {
                // Dacă nu găsim CoordinatorLayout, adăugăm un buton alternativ în container-ul principal
                Button addPhotoButton = new Button(this);
                addPhotoButton.setText("Adaugă Fotografie");
                addPhotoButton.setBackgroundColor(Color.parseColor("#FF8F00"));
                addPhotoButton.setTextColor(Color.WHITE);
                
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                buttonParams.gravity = android.view.Gravity.END;
                buttonParams.setMargins(0, 16, 16, 16);
                addPhotoButton.setLayoutParams(buttonParams);
                
                addPhotoButton.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                });
                
                if (mainContainer != null) {
                    mainContainer.addView(addPhotoButton, 0);
                }
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
        defaultImages.add("oltenia_craiova_1");
        defaultImages.add("oltenia_craiova_parc");
        defaultImages.add("oltenia_craiova_centru");
        defaultImages.add("oltenia_craiova_universitate");
        defaultImages.add("oltenia_craiova_teatru");

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
            // Debugging - afișăm imaginile care vor fi afișate
            Toast.makeText(this, "Se afișează " + images.size() + " imagini în carusel", Toast.LENGTH_SHORT).show();
            
            androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.imageCarousel);
            if (viewPager != null) {
                com.example.myapplication.adapter.ImageCarouselAdapter adapter =
                        new com.example.myapplication.adapter.ImageCarouselAdapter(this, images);

                adapter.setOnImageAddedListener(uri -> {
                    String imageUri = uri.toString();
                    userImages.add(imageUri);
                    saveImages();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Fotografie adăugată cu succes prin listener!", Toast.LENGTH_SHORT).show();
                });

                viewPager.setAdapter(adapter);

                com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
                if (tabLayout != null) {
                    new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager,
                            (tab, position) -> {}).attach();
                }
            } else {
                Toast.makeText(this, "ViewPager nu a fost găsit!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Nu există imagini pentru carusel!", Toast.LENGTH_LONG).show();
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
        if (savedImages != null && !savedImages.isEmpty()) {
            userImages.addAll(savedImages);
            Toast.makeText(this, "S-au încărcat " + userImages.size() + " imagini salvate", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nu există imagini salvate", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImages() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> imageSet = new HashSet<>(userImages);
        editor.putStringSet(USER_IMAGES_KEY, imageSet);
        boolean success = editor.commit(); // Use commit instead of apply to get immediate result
        
        // Debugging toast to check if images were saved successfully
        if (success) {
            Toast.makeText(this, "Imagini salvate: " + userImages.size(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Eroare la salvarea imaginilor!", Toast.LENGTH_LONG).show();
        }
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
                "Festivalul Internațional de Teatru - Iunie 2024",
                "Târgul de Crăciun - Decembrie 2024",
                "Astra Film Festival - Octombrie 2024",
                "Sibiu Jazz Festival - Mai 2024"
        };

        String[] tips = {
                "Piața Mare este mai puțin aglomerată dimineața devreme",
                "Încercați brânzeturile tradiționale din Piața Mică",
                "Podul Minciunilor este locul perfect pentru fotografii la apus",
                "Muzeul ASTRA este cel mai bine vizitat în a doua jumătate a zilei"
        };

        // Adăugăm componentele direct în containerul principal, în ordinea corectă

        // 1. Widget-ul de vreme
        MaterialCardView weatherWidget = new MaterialCardView(this);
        weatherWidget.setTag("weather_widget");
        weatherWidget.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        weatherWidget.setCardBackgroundColor(Color.parseColor("#E8E6F0")); // Pale lavender
        weatherWidget.setRadius(16);
        weatherWidget.setCardElevation(4);
        weatherWidget.setContentPadding(16, 16, 16, 16);

        LinearLayout weatherContent = new LinearLayout(this);
        weatherContent.setOrientation(LinearLayout.VERTICAL);

        TextView weatherTitle = new TextView(this);
        weatherTitle.setText("Vremea în Sibiu");
        weatherTitle.setTextSize(18);
        weatherTitle.setTextColor(Color.parseColor("#4A4E69")); // Deep blue-purple
        weatherTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView weatherInfo = new TextView(this);
        weatherInfo.setText("Se încarcă informații despre vreme...");
        weatherInfo.setTextSize(16);

        Button refreshWeatherBtn = new Button(this);
        refreshWeatherBtn.setText("Actualizează");
        refreshWeatherBtn.setBackgroundColor(Color.parseColor("#9A8C98")); // Muted purple
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
        eventsButton.setBackgroundColor(Color.parseColor("#C9ADA7")); // Dusty rose
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
            builder.setTitle("Evenimente în Sibiu");
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
        tipsCard.setCardBackgroundColor(Color.parseColor("#F0EDF5")); // Very light lavender
        tipsCard.setRadius(16);
        tipsCard.setCardElevation(4);
        tipsCard.setContentPadding(16, 16, 16, 16);

        LinearLayout tipsContent = new LinearLayout(this);
        tipsContent.setOrientation(LinearLayout.VERTICAL);

        TextView tipsTitle = new TextView(this);
        tipsTitle.setText("Sfaturi Locale");
        tipsTitle.setTextSize(18);
        tipsTitle.setTextColor(Color.parseColor("#4A4E69")); // Deep blue-purple
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

        // 4. Provocarea foto
        MaterialCardView photoCard = new MaterialCardView(this);
        photoCard.setTag("photo_challenge");
        photoCard.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        photoCard.setCardBackgroundColor(Color.parseColor("#F2E9E4")); // Light pink-beige
        photoCard.setRadius(16);
        photoCard.setCardElevation(4);
        photoCard.setContentPadding(16, 16, 16, 16);

        LinearLayout photoContent = new LinearLayout(this);
        photoContent.setOrientation(LinearLayout.VERTICAL);

        TextView photoTitle = new TextView(this);
        photoTitle.setText("Provocare Foto: Descoperă Sibiu");
        photoTitle.setTextSize(18);
        photoTitle.setTextColor(Color.parseColor("#4A4E69")); // Deep blue-purple
        photoTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView photoDesc = new TextView(this);
        photoDesc.setText("Imortalizează cele mai frumoase locuri din Sibiu și adaugă-le în colecția ta personală!");
        photoDesc.setTextSize(16);
        photoDesc.setPadding(0, 8, 0, 16);

        Button addPhotoBtn = new Button(this);
        addPhotoBtn.setText("Adaugă Fotografie");
        addPhotoBtn.setBackgroundColor(Color.parseColor("#C9ADA7")); // Dusty rose
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
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            // Cod executat în background
            String result = null;
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

                result = response.toString();
            } catch (Exception e) {
                result = null;
            }
            
            // Rezultatul final pentru a fi utilizat în Handler
            final String finalResult = result;
            
            // Actualizare UI pe thread-ul principal
            handler.post(() -> {
                // Cod identic cu onPostExecute original
                if (finalResult != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(finalResult);
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
            });
        });
    }

    private void addWelcomeBackMessage() {
        MaterialCardView welcomeCard = new MaterialCardView(this);
        welcomeCard.setCardElevation(4);
        welcomeCard.setRadius(16);
        welcomeCard.setCardBackgroundColor(Color.parseColor("#E8E6F0")); // Pale lavender to match weather widget

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(24, 16, 24, 16);

        android.widget.TextView welcomeText = new android.widget.TextView(this);
        welcomeText.setText("Bine ai revenit în Sibiu! Aceasta este vizita ta #" + visitCount);
        welcomeText.setTextSize(18);
        welcomeText.setTextColor(Color.parseColor("#4A4E69")); // Deep blue-purple to match other text

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
                    // Obținem permisiune persistentă pentru URI, dacă e disponibilă
                    try {
                        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);
                        Toast.makeText(this, "Permisiune persistentă obținută", Toast.LENGTH_SHORT).show();
                    } catch (SecurityException se) {
                        // În caz că nu putem obține permisiune persistentă, continuăm oricum
                        Toast.makeText(this, "Nu s-a putut obține permisiune persistentă, dar continuăm", Toast.LENGTH_SHORT).show();
                    }

                    String imageUri = selectedImageUri.toString();
                    
                    // Debugging toast to confirm URI was captured
                    Toast.makeText(this, "URI imagine: " + imageUri, Toast.LENGTH_SHORT).show();

                    // Verificăm dacă avem deja 5 imagini pentru Sibiu
                    if (userImages.size() >= 5) {
                        // Eliminăm cea mai veche imagine
                        userImages.remove(0);
                    }

                    // Adăugăm noua imagine
                    userImages.add(imageUri);
                    
                    // Debugging - afișăm câte imagini avem acum
                    Toast.makeText(this, "Total imagini: " + userImages.size(), Toast.LENGTH_SHORT).show();
                    
                    // Salvăm imaginile
                    saveImages();

                    // Reconfigurăm caruselul pentru a include noua imagine
                    setupImageCarousel();

                    Snackbar.make(findViewById(android.R.id.content),
                            "Imaginea a fost adăugată cu succes în carusel!",
                            Snackbar.LENGTH_LONG).show();
                } catch (SecurityException e) {
                    Toast.makeText(this, "Nu s-a putut accesa imaginea: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "URI imagine null", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            Toast.makeText(this, "Selectare imagine anulată sau eșuată", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();

        // Add user images if available
        if (userImages != null && !userImages.isEmpty()) {
            int startIndex = Math.max(0, userImages.size() - 5);
            images.addAll(userImages.subList(startIndex, userImages.size()));
            // Debugging - afișăm câte imagini ale utilizatorului sunt folosite
            Toast.makeText(this, "Se folosesc " + images.size() + " imagini ale utilizatorului", Toast.LENGTH_SHORT).show();
        }

        // Add default images if needed
        if (images.size() < 5) {
            ArrayList<String> defaultImages = new ArrayList<>();
            defaultImages.add("oltenia_craiova_1");
            defaultImages.add("oltenia_craiova_parc");
            defaultImages.add("oltenia_craiova_centru");
            defaultImages.add("oltenia_craiova_universitate");
            defaultImages.add("oltenia_craiova_teatru");

            // Add only as many default images as needed to reach 5
            int remainingSlots = 5 - images.size();
            for (int i = 0; i < remainingSlots && i < defaultImages.size(); i++) {
                images.add(defaultImages.get(i));
            }
            
            // Debugging - afișăm câte imagini implicite sunt folosite
            Toast.makeText(this, "Se adaugă " + remainingSlots + " imagini implicite", Toast.LENGTH_SHORT).show();
        }

        return images;
    }

    @Override
    protected void initializeSpecificContent() {
        super.initializeSpecificContent();

        setTitle("Sibiu");

        // Add sections
        addSection(
                findViewById(R.id.cityContentContainer),
                "Istorie și Tradiție",
                "Sibiu, unul dintre cele mai vechi orașe din România, își are originile în așezarea dacică Pelendava. A fost menționată documentar pentru prima dată în 1475, devenind reședință a boierilor Craioveşti în secolul al XV-lea. În secolul al XIX-lea a fost un important centru comercial și cultural al Olteniei.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Geografie",
                "Sibiu este situată în sud-vestul României, pe malul stâng al râului Jiu, în centrul Olteniei. Orașul se află la aproximativ 227 km de București și are o suprafață de aproximativ 81,41 km². Clima este temperat-continentală cu influențe mediteraneene, cu veri calde și ierni relativ blânde.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Atracții Turistice",
                "Parcul Nicolae Romanescu (al treilea ca mărime din Europa), Centrul Vechi, Muzeul de Artă (cu opere ale lui Constantin Brâncuși), Casa Băniei și Grădina Botanică sunt principalele atracții ale Sibiului. Teatrul Național Marin Sorescu este, de asemenea, un important reper cultural și arhitectural.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Cultură",
                "Sibiu este un important centru cultural, cu instituții precum Filarmonica Oltenia, Teatrul Național Marin Sorescu și Teatrul pentru Copii și Tineret Colibri. Orașul găzduiește anual Festivalul Internațional Shakespeare și festivalul de muzică IntenCity, și are o bogată tradiție literară, fiind orașul natal al lui Marin Sorescu și Ion D. Sîrbu.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Gastronomie",
                "Bucătăria oltenească din Sibiu se remarcă prin preparate tradiționale precum sarmale oltenești cu afumătură, ciorbă de potroace, cârnați de Pleșcoi și celebra dovleac copt cu brânză. Restaurantele din Centrul Vechi oferă o experiență culinară autentică, iar crama Domeniile Coroanei oferă degustări de vinuri locale.",
                false
        );

        // Add attractions using the AttractionHelper
        LinearLayout container = findViewById(R.id.cityContentContainer);

        // Add Parcul Romanescu attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Parcul Cetății",
                R.drawable.parcul_cetatii, // Make sure this resource exists
                "Părerea ta despre Parcul Cetății"
        );

        // Add Muzeul de Artă attraction
        AttractionHelper.addAttraction(
                this,
                container,
                "Turnurile Cetății",
                R.drawable.turn_cetatii, // Make sure this resource exists
                "Părerea ta despre Turnurile Cetății"
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
            intent.putExtra("CITY_NAME", "Sibiu");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });
    }

    @Override
    protected String getRegionName() {
        return "Oltenia";
    }
}
