package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.CityData;
import com.example.myapplication.core.domain.model.CityData.AttractionData;
import com.example.myapplication.core.domain.repository.CityRepository;
import com.example.myapplication.viewmodel.AttractionHelper;
import com.example.myapplication.viewmodel.EnhancedCityActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class CityDetailActivity extends EnhancedCityActivity {

    public static final String EXTRA_CITY_ID = "CITY_ID";
    private static final String WEATHER_API_KEY = "1234567890abcdef1234567890abcdef";
    private static final int PICK_IMAGE_REQUEST = 1;

    private CityData cityData;
    private ArrayList<String> userImages = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private int visitCount = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout specialFeaturesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String cityId = getIntent().getStringExtra(EXTRA_CITY_ID);
        if (cityId != null) {
            cityData = CityRepository.getInstance().getCityById(this, cityId);
        }
        
        if (cityData != null) {
            String prefsName = cityData.getId() + "Prefs";
            sharedPreferences = getSharedPreferences(prefsName, MODE_PRIVATE);
            loadSavedImages();
            recordVisit();
            loadVisitCount();
        }

        super.onCreate(savedInstanceState);

        // Setup BlurViews with RenderScriptBlur for real glassmorphism
        setupBlurViews();
    }

    private void setupBlurViews() {
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        android.graphics.drawable.Drawable windowBackground = decorView.getBackground();

        // Initialize every BlurView found in the layout
        BlurView pointsBadgeBlur = findViewById(R.id.pointsBlurView);
        if (pointsBadgeBlur != null) {
            pointsBadgeBlur.setupWith(rootView, new RenderScriptBlur(this))
                    .setBlurRadius(12f)
                    .setBlurAutoUpdate(true);
        }
    }

    @Override
    protected String getCityName() {
        return cityData != null ? cityData.getName() : "Oraș Necunoscut";
    }

    @Override
    protected String getRegionName() {
        return cityData != null ? cityData.getRegion() : "Regiune Necunoscută";
    }

    @Override
    protected String getCityDescription() {
        return (cityData != null && !cityData.getDescription().isEmpty()) ? cityData.getDescription() : super.getCityDescription();
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();

        if (userImages != null && !userImages.isEmpty()) {
            int startIndex = Math.max(0, userImages.size() - 5);
            images.addAll(userImages.subList(startIndex, userImages.size()));
        }

        if (images.size() < 5 && cityData != null && cityData.getDefaultImages() != null) {
            List<String> defaultImages = cityData.getDefaultImages();
            int remainingSlots = 5 - images.size();
            for (int i = 0; i < remainingSlots && i < defaultImages.size(); i++) {
                images.add(defaultImages.get(i));
            }
        }
        if (images.isEmpty()) {
            return super.getCityImages();
        }
        return images;
    }

    private void loadVisitCount() {
        visitCount = sharedPreferences.getInt("visitCount", 0);
        visitCount++;
        sharedPreferences.edit().putInt("visitCount", visitCount).apply();
    }

    private void recordVisit() {
        sharedPreferences.edit().putLong("lastVisit", System.currentTimeMillis()).apply();
    }

    private void loadSavedImages() {
        Set<String> savedImages = sharedPreferences.getStringSet("userImages", new HashSet<>());
        userImages.clear();
        userImages.addAll(savedImages);
    }

    private void saveImages() {
        sharedPreferences.edit().putStringSet("userImages", new HashSet<>(userImages)).apply();
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
                    if (userImages.size() >= 5) {
                        userImages.remove(0);
                    }
                    userImages.add(imageUri);
                    saveImages();
                    
                    // The carousel reloading is handled by EnhancedCityActivity 
                    // To force reload, we recreate or call setupImageCarousel again if accessible.
                    // For now displaying Toast.
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

    private void addWelcomeBackMessage() {
        LinearLayout welcomeCard = new LinearLayout(this);
        welcomeCard.setOrientation(LinearLayout.VERTICAL);
        welcomeCard.setBackgroundResource(R.drawable.bg_glass_card_rounded);
         welcomeCard.setPadding(48, 36, 48, 36);

        TextView welcomeText = new TextView(this);
        welcomeText.setText("\uD83D\uDC4B Bine ai revenit în " + getCityName() + "!");
        welcomeText.setTextSize(20);
        welcomeText.setTypeface(null, android.graphics.Typeface.BOLD);
        welcomeText.setTextColor(Color.WHITE);

        TextView subText = new TextView(this);
        subText.setText("Aceasta este vizita ta #" + visitCount + ". Ne bucurăm să te revedem!");
        subText.setTextSize(14);
        subText.setTextColor(Color.parseColor("#9CA3AF"));
        subText.setPadding(0, 8, 0, 0);

        welcomeCard.addView(welcomeText);
        welcomeCard.addView(subText);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 24);
        welcomeCard.setLayoutParams(params);
        
        specialFeaturesContainer.addView(welcomeCard);

        handler.postDelayed(() -> {
            specialFeaturesContainer.removeView(welcomeCard);
        }, 5000);
    }

    private void fetchWeatherData(TextView weatherInfoView, String weatherId) {
        if (weatherId == null || weatherId.isEmpty()) return;
        String urlString = "https://api.openweathermap.org/data/2.5/weather?id=" + weatherId + "&appid=" + WEATHER_API_KEY + "&units=metric&lang=ro";
        
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();
                    return response.toString();
                } catch (Exception e) { return null; }
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
                        String weatherText = String.format("Temperatura: %.1f°C\nSe simte ca: %.1f°C\nUmiditate: %d%%\nCondiții: %s", temp, feelsLike, humidity, description);
                        weatherInfoView.setText(weatherText);
                    } catch (Exception e) {
                        weatherInfoView.setText("Eroare la preluarea datelor meteo.");
                    }
                } else {
                    weatherInfoView.setText("Nu s-au putut încărca datele meteo.");
                }
            }
        }.execute();
    }

    @Override
    protected void initializeSpecificContent() {
        if (cityData == null) {
            addSection(findViewById(R.id.cityContentContainer), "Eroare", "Datele orașului nu au putut fi încărcate.", false);
            return;
        }

        LinearLayout container = findViewById(R.id.cityContentContainer);
        if (container == null) return;

        // Container Features
        specialFeaturesContainer = new LinearLayout(this);
        specialFeaturesContainer.setOrientation(LinearLayout.VERTICAL);
        specialFeaturesContainer.setPadding(32, 16, 32, 16);
        container.addView(specialFeaturesContainer);

        // Map Button
        if (cityData.getMapCoords() != null && !cityData.getMapCoords().isEmpty()) {
            com.google.android.material.button.MaterialButton mapButton = new com.google.android.material.button.MaterialButton(this);
            mapButton.setText("Harta Interactivă " + getCityName());
            mapButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F97316")));
            mapButton.setTextColor(Color.WHITE);
            mapButton.setCornerRadius(24);
            mapButton.setIconResource(R.drawable.ic_map); // Assumes ic_map exists or will fallback
            mapButton.setIconGravity(com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START);
            mapButton.setPadding(32, 24, 32, 24);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 24, 16, 24);
            mapButton.setLayoutParams(params);
            
            mapButton.setOnClickListener(v -> {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                Uri gmmIntentUri = Uri.parse("geo:" + cityData.getMapCoords() + "?q=" + getCityName() + ",Romania");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Ensure we handle the package visibility for Android 11+
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    // Fallback to any map app
                    mapIntent.setPackage(null);
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(this, "Nicio aplicație de hărți instalată", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
            if (tabLayout != null) {
                // Better to add it inside the main container to avoid breaking AppBarLayout
                specialFeaturesContainer.addView(mapButton, 0); 
            }
        }

        if (visitCount > 1) {
            addWelcomeBackMessage();
        }

        // Weather Widget
        if (cityData.getWeatherId() != null && !cityData.getWeatherId().isEmpty()) {
            LinearLayout weatherWidget = new LinearLayout(this);
            weatherWidget.setOrientation(LinearLayout.VERTICAL);
            weatherWidget.setBackgroundResource(R.drawable.bg_braided_border);
            weatherWidget.setPadding(40, 32, 40, 32);
            weatherWidget.setTag("weather_widget");
            
            LinearLayout.LayoutParams widgetParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            widgetParams.setMargins(0, 16, 0, 24);
            weatherWidget.setLayoutParams(widgetParams);

            TextView weatherTitle = new TextView(this);
            weatherTitle.setText("Vremea în " + getCityName());
            weatherTitle.setTextSize(18);
            weatherTitle.setTextColor(Color.parseColor("#FFFFFF"));
            weatherTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            weatherTitle.setPadding(0, 0, 0, 16);

            TextView weatherInfo = new TextView(this);
            weatherInfo.setText("Se încarcă informații despre vreme...");
            weatherInfo.setTextSize(15);
            weatherInfo.setTextColor(Color.parseColor("#9CA3AF"));
            weatherInfo.setLineSpacing(0, 1.2f);

            com.google.android.material.button.MaterialButton refreshWeatherBtn = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            refreshWeatherBtn.setText("Actualizează");
            refreshWeatherBtn.setTextColor(Color.parseColor("#F97316"));
            refreshWeatherBtn.setStrokeColorResource(android.R.color.transparent);
            refreshWeatherBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1AFFFFFF")));
            refreshWeatherBtn.setCornerRadius(16);
            refreshWeatherBtn.setIconResource(android.R.drawable.ic_popup_sync);
            refreshWeatherBtn.setIconTint(android.content.res.ColorStateList.valueOf(Color.parseColor("#F97316")));
            refreshWeatherBtn.setOnClickListener(v -> fetchWeatherData(weatherInfo, cityData.getWeatherId()));

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(0, 16, 0, 0);
            refreshWeatherBtn.setLayoutParams(btnParams);

            weatherWidget.addView(weatherTitle);
            weatherWidget.addView(weatherInfo);
            weatherWidget.addView(refreshWeatherBtn);
            specialFeaturesContainer.addView(weatherWidget);
            fetchWeatherData(weatherInfo, cityData.getWeatherId());
        }

        // Events Button
        if (cityData.getEvents() != null && !cityData.getEvents().isEmpty()) {
            com.google.android.material.button.MaterialButton eventsButton = new com.google.android.material.button.MaterialButton(this);
            eventsButton.setText("Evenimente Locale");
            eventsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F97316")));
            eventsButton.setTextColor(Color.WHITE);
            eventsButton.setPadding(32, 24, 32, 24);
            eventsButton.setCornerRadius(24);
            LinearLayout.LayoutParams eventsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            eventsParams.setMargins(0, 8, 0, 24);
            eventsButton.setLayoutParams(eventsParams);

            eventsButton.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Evenimente în " + getCityName());
                String[] evs = cityData.getEvents().toArray(new String[0]);
                builder.setItems(evs, (dialog, which) -> {
                    Toast.makeText(this, "Eveniment selectat: " + evs[which], Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
            specialFeaturesContainer.addView(eventsButton);
        }

        // Tips Card
        if (cityData.getTips() != null && !cityData.getTips().isEmpty()) {
            LinearLayout tipsCard = new LinearLayout(this);
            tipsCard.setOrientation(LinearLayout.VERTICAL);
            tipsCard.setBackgroundResource(R.drawable.bg_glass_card);
            tipsCard.setPadding(40, 32, 40, 32);
            
            LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tipsParams.setMargins(0, 16, 0, 24);
            tipsCard.setLayoutParams(tipsParams);
            
            TextView tipsTitle = new TextView(this);
            tipsTitle.setText("Sfaturi Locale");
            tipsTitle.setTextSize(18);
            tipsTitle.setTextColor(Color.parseColor("#FFFFFF"));
            tipsTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            tipsTitle.setPadding(0, 0, 0, 16);
            tipsCard.addView(tipsTitle);
            
            for (String tip : cityData.getTips()) {
                TextView tipView = new TextView(this);
                tipView.setText("\uD83D\uDCA1 " + tip);
                tipView.setTextSize(15);
                tipView.setTextColor(Color.parseColor("#9CA3AF"));
                tipView.setPadding(0, 8, 0, 8);
                tipView.setLineSpacing(0, 1.4f);
                tipsCard.addView(tipView);
            }
            specialFeaturesContainer.addView(tipsCard);
        }

        // Photo Challenge
        LinearLayout photoCard = new LinearLayout(this);
        photoCard.setOrientation(LinearLayout.VERTICAL);
        photoCard.setBackgroundResource(R.drawable.bg_glass_card);
        photoCard.setPadding(40, 32, 40, 32);
        
        LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        photoParams.setMargins(0, 16, 0, 24);
        photoCard.setLayoutParams(photoParams);

        TextView photoTitle = new TextView(this);
        photoTitle.setText("\uD83D\uDCF8 Provocare Foto: " + getCityName());
        photoTitle.setTextSize(18);
        photoTitle.setTextColor(Color.parseColor("#FFFFFF"));
        photoTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView photoDesc = new TextView(this);
        photoDesc.setText("Imortalizează cele mai frumoase locuri din " + getCityName() + " și adaugă-le în colecția ta personală!");
        photoDesc.setTextSize(15);
        photoDesc.setTextColor(Color.parseColor("#9CA3AF"));
        photoDesc.setPadding(0, 12, 0, 20);
        photoDesc.setLineSpacing(0, 1.4f);

        com.google.android.material.button.MaterialButton addPhotoBtn = new com.google.android.material.button.MaterialButton(this);
        addPhotoBtn.setText("Adaugă Fotografie");
        addPhotoBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F97316")));
        addPhotoBtn.setTextColor(Color.WHITE);
        addPhotoBtn.setCornerRadius(24);
        addPhotoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        photoCard.addView(photoTitle);
        photoCard.addView(photoDesc);
        photoCard.addView(addPhotoBtn);
        specialFeaturesContainer.addView(photoCard);

        // Sections - rich content migrated from EnhancedCityActivity
        if (cityData.getSections() != null && !cityData.getSections().isEmpty()) {
            for (CityData.SectionData sec : cityData.getSections()) {
                addSection(container, sec.getTitle(), sec.getContent(), sec.isHighlighted());
            }
        } else if (cityData.getDescription() != null && !cityData.getDescription().isEmpty()) {
            // Fallback: show simple description if no sections available
            addSection(container, "Prezentare Generală", cityData.getDescription(), true);
        }

        // Attractions
        if (cityData.getAttractions() != null && !cityData.getAttractions().isEmpty()) {
            for (AttractionData attr : cityData.getAttractions()) {
                int resId = getResources().getIdentifier(attr.getImageRes().replace("R.drawable.", ""), "drawable", getPackageName());
                if(resId == 0) resId = R.drawable.city_image_placeholder; // fallback
                AttractionHelper.addAttraction(
                        this,
                        container,
                        attr.getName(),
                        resId,
                        attr.getPrompt() != null ? attr.getPrompt() : "Părerea ta"
                );
            }
        }

    }

    @Override
    protected void addSection(LinearLayout container, String title, String content, boolean isHighlighted) {
        super.addSection(container, title, content, isHighlighted);
        
        // Deschide SectionPreviewActivity la apăsare
        if (container.getChildCount() > 0) {
            View sectionView = container.getChildAt(container.getChildCount() - 1);
            sectionView.setOnClickListener(v -> {
                Intent intent = new Intent(this, com.example.myapplication.viewmodel.SectionPreviewActivity.class);
                intent.putExtra("SECTION_TITLE", title);
                intent.putExtra("SECTION_CONTENT", content);
                intent.putExtra("CITY_NAME", getCityName());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            });
        }
    }
}
