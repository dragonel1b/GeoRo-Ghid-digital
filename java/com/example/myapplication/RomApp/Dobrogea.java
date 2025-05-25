package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.Joc1.RomCityActivity;
import com.example.myapplication.R;

import java.util.ArrayList;

public class Dobrogea extends RegionTemplate {

    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "dobrogea";

    @Override
    protected String getIntroductionText() {
        return "Dobrogea este o regiune istorică în sud-estul României, cunoscută pentru " +
               "litoralul său pe Marea Neagr și Delta Dunării, un ecosistem unic în Europa.";
    }

    @Override
    protected String getHistoryGeographyText() {
        return "Dobrogea a fost locuită încă din antichitate, cu influențe grecești, romane " +
               "și otomane. Regiunea este străbătută de Dunăre și are o climă moderat-continentală.";
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Dobrogea este un mozaic cultural cu comunități de români, lipoveni, turci " +
               "și tătari. Portul popular și muzica tradițională reflectă această diversitate.";
    }

    @Override
    protected String getAttractionsText() {
        return "Principalele atracții includ Delta Dunării (rezervație biosferei UNESCO), " +
               "Mamaia, Constanța (cu ruinele Tomis) și Histria (cel mai vechi oraș atestat pe teritoriul României).";
    }

    @Override
    protected String getGastronomyText() {
        return "Specificul culinar include plăcinte lipovenești, saramură de crap, " +
               "icre de scrumbie și alte preparate cu influențe orientale și balcanice.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități: Ovidiu, Mihai Eminescu (a trăit în exil la Constanța).\n" +
               "Evenimente: Festivalul Callatis, Zilele Tomisului.";
    }

    @Override
    protected String getCuriositiesText() {
        return "Delta Dunării este cea mai mare rezervație de stuf din lume și găzduiește " +
               "peste 300 de specii de păsări. Constanța a fost primul oraș electrificat din România (1882).";
    }

    @Override
    protected String getRegionName() {
        return "Dobrogea";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("constanta");
        images.add("tulcea");
        images.add("mamaia");
        images.add("histria");
        images.add("sulina");
        return images;
    }

    private final String[] cityDescriptions = {
            "Constanța este principalul oraș al Dobrogei și al doilea ca mărime din România. " +
            "Orașul găzduiește Portul Constanța, cel mai mare port al Mării Negre, și numeroase " +
            "atracții turistice precum Casino-ul Constanța, Catedrala Sfinții Apostoli Petru și Pavel " +
            "și Muzeul de Istorie Națională și Arheologie.",

            "Tulcea este considerată poarta de intrare în Delta Dunării. Orașul este un important " +
            "centru cultural și economic, găzduind Muzeul Deltei Dunării și numeroase monumente " +
            "istorice. Este punctul de plecare pentru excursiile în Delta Dunării.",

            "Mamaia este cea mai mare stațiune de pe litoralul românesc, cunoscută pentru plajele " +
            "sale de nisip fin și viața de noapte animată. Stațiunea oferă multiple facilități " +
            "de cazare și agrement pentru turiști.",

            "Histria este cel mai vechi oraș atestat pe teritoriul României, fondat de greci în " +
            "secolul al VII-lea î.Hr. Ruinele antice includ temple, băi publice și un muzeu.",

            "Sulina este orașul de la vărsarea Dunării în Marea Neagră. Cunoscută pentru farul " +
            "sau istoric și pentru comunitatea lipovenească, Sulina oferă peisaje unice din Delta Dunării."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Set up image carousel
        ArrayList<String> images = getCityImages();
        if (images != null && !images.isEmpty()) {
            androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.imageCarousel);
            com.example.myapplication.adapter.ImageCarouselAdapter adapter = 
                new com.example.myapplication.adapter.ImageCarouselAdapter(this, images);
            viewPager.setAdapter(adapter);
            
            // Connect with tab indicator
            com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
            new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager, 
                (tab, position) -> {}).attach();
        }

        // Initialize content sections
        initializeSpecificContent();

        // Set up navigation buttons
        findViewById(R.id.buttonGoToCasinoStory).setOnClickListener(this::goToCasinoStory);
        findViewById(R.id.buttonGoToDobrogeaGame).setOnClickListener(this::goToDobrogeaGame);
        
        // Load saved states
        loadCheckboxStates();
    }

    private String getCurrentUserId() {
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return userPrefs.getString("current_user_id", "default");
    }

    private void loadCheckboxStates() {
        String userId = getCurrentUserId();
        CheckBox checkBox1 = findViewById(R.id.checkBox1);
        CheckBox checkBox2 = findViewById(R.id.checkBox2);
        CheckBox checkBox3 = findViewById(R.id.checkBox3);
        CheckBox checkBox4 = findViewById(R.id.checkBox4);
        CheckBox checkBox5 = findViewById(R.id.checkBox5);

        if (checkBox1 != null) checkBox1.setChecked(sharedPreferences.getBoolean(userId + "_checkBox1_" + REGION, false));
        if (checkBox2 != null) checkBox2.setChecked(sharedPreferences.getBoolean(userId + "_checkBox2_" + REGION, false));
        if (checkBox3 != null) checkBox3.setChecked(sharedPreferences.getBoolean(userId + "_checkBox3_" + REGION, false));
        if (checkBox4 != null) checkBox4.setChecked(sharedPreferences.getBoolean(userId + "_checkBox4_" + REGION, false));
        if (checkBox5 != null) checkBox5.setChecked(sharedPreferences.getBoolean(userId + "_checkBox5_" + REGION, false));
    }

    public void onCheckboxClicked(View view) {
        if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            pointsManager.updateLandmarkStatus(this, REGION, checkBox.isChecked());

            // Save state with user ID
            String userId = getCurrentUserId();
            String checkBoxId = getResources().getResourceEntryName(view.getId());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(userId + "_" + checkBoxId + "_" + REGION, checkBox.isChecked());
            editor.apply();
        }
    }

    public void showPopup1(View view) {
        showPopup("Constanța", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Constanța");
        intent.putExtra("city_lat", 44.1733);
        intent.putExtra("city_lng", 28.6383);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Tulcea", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Tulcea");
        intent.putExtra("city_lat", 45.1792);
        intent.putExtra("city_lng", 28.7969);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Mamaia", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Mamaia");
        intent.putExtra("city_lat", 44.2500);
        intent.putExtra("city_lng", 28.6333);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Histria", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Histria");
        intent.putExtra("city_lat", 44.5469);
        intent.putExtra("city_lng", 28.7750);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Sulina", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Sulina");
        intent.putExtra("city_lat", 45.1556);
        intent.putExtra("city_lng", 29.6539);
        startActivity(intent);
    }

    private void showPopup(String title, String description) {
        if (!isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(description)
                    .setPositiveButton("Închide", null)
                    .show();
        }
    }

    public void goBack(View view) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCheckboxStates();
    }

    // Navigation methods
    public void goToCasinoStory(View view) {
        Intent intent = new Intent(this, com.example.myapplication.dobrogeausage.CasinoStoryActivity.class);
        startActivity(intent);
    }

    public void goToDobrogeaGame(View view) {
        // DobrogeaGame is a model class, not an Activity
        // Either implement a proper game activity or show a message
        new AlertDialog.Builder(this)
            .setTitle("Joc Dobrogea")
            .setMessage("Funcționalitatea jocului va fi implementată în versiuni viitoare.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void saveCheckboxStates() {
        String userId = getCurrentUserId();
        CheckBox checkBox1 = findViewById(R.id.checkBox1);
        CheckBox checkBox2 = findViewById(R.id.checkBox2);
        CheckBox checkBox3 = findViewById(R.id.checkBox3);
        CheckBox checkBox4 = findViewById(R.id.checkBox4);
        CheckBox checkBox5 = findViewById(R.id.checkBox5);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (checkBox1 != null) editor.putBoolean(userId + "_checkBox1_" + REGION, checkBox1.isChecked());
        if (checkBox2 != null) editor.putBoolean(userId + "_checkBox2_" + REGION, checkBox2.isChecked());
        if (checkBox3 != null) editor.putBoolean(userId + "_checkBox3_" + REGION, checkBox3.isChecked());
        if (checkBox4 != null) editor.putBoolean(userId + "_checkBox4_" + REGION, checkBox4.isChecked());
        if (checkBox5 != null) editor.putBoolean(userId + "_checkBox5_" + REGION, checkBox5.isChecked());

        editor.apply();
    }
}
