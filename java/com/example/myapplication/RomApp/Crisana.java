package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.Joc1.RomCityActivity;
import com.example.myapplication.R;

import java.util.ArrayList;

public class Crisana extends RegionTemplate {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "crisana";

    @Override
    protected String getIntroductionText() {
        return "Crișana este o regiune istorică în vestul României, cunoscută pentru peisajele sale de câmpie " +
               "și pentru orașele sale bogate în patrimoniu cultural și arhitectural.";
    }

    @Override
    protected String getHistoryGeographyText() {
        return "Crișana a fost locuită încă din epoca bronzului, cu influențe dacice, romane și maghiare.\n\n" +
               "Rețea hidrografică: Crișul Alb, Crișul Negru, Crișul Repede, Barcău";
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Crișana este un teritoriu multicultural, cu comunități de români, maghiari și germani.\n\n" +
               "Tradiții: Dansuri populare (brâul), portul popular crișean, meșteșuguri tradiționale";
    }

    @Override
    protected String getAttractionsText() {
        return "Principalele atracții:\n" +
               "- Cetatea Oradea\n" +
               "- Cetatea Arad\n" +
               "- Băile Felix\n" +
               "- Complexul Apiferă din Salonta\n" +
               "- Rezervația Naturală Pădurea Craiului";
    }

    @Override
    protected String getGastronomyText() {
        return "Specificul culinar:\n" +
               "- Ciorbă de burtă\n" +
               "- Gulaș crișean\n" +
               "- Salam de Sibiu\n" +
               "- Pâine de Pecica";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități:\n" +
               "- Ioan Slavici\n" +
               "- George Coșbuc\n" +
               "- Arany János\n\n" +
               "Evenimente:\n" +
               "- Festivalul Medieval de la Oradea\n" +
               "- Zilele Aradului";
    }

    @Override
    protected String getCuriositiesText() {
        return "Curiozități:\n" +
               "- Oradea are cea mai mare concentrație de clădiri Art Nouveau din România\n" +
               "- Aradul a fost primul oraș european cu iluminat stradal pe gaz\n" +
               "- Salonta este locul de naștere al poetului maghiar Arany János";
    }

    @Override
    protected String getRegionName() {
        return "Crișana";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("oradea");
        images.add("arad");
        images.add("salonta");
        images.add("ineu");
        images.add("chisineu_cris");
        return images;
    }

    private final String[] cityDescriptions = {
            "Oradea este cel mai important oraș al Crișanei. " +
                    "Centrul istoric, cu arhitectura sa Art Nouveau, este remarcabil. " +
                    "Cetatea Oradea, Palatul Episcopal Greco-Catolic și băile termale " +
                    "sunt principalele atracții turistice.",

            "Arad este un important centru cultural și economic. " +
                    "Cetatea Aradului și Palatul Cultural sunt obiective importante. " +
                    "Teatrul Clasic 'Ioan Slavici' și Filarmonica de Stat sunt " +
                    "instituții culturale de prestigiu.",

            "Salonta este cunoscută pentru turnul său medieval. " +
                    "Orașul natal al poetului Arany János păstrează numeroase " +
                    "monumente istorice și culturale. Muzeul Memorial 'Arany János' " +
                    "este principala atracție.",

            "Ineu este un important centru istoric. " +
                    "Cetatea Ineului, construită în secolul al XIII-lea, " +
                    "este principala atracție turistică. Orașul păstrează " +
                    "numeroase tradiții locale.",

            "Chișineu-Criș este un important centru agricol. " +
                    "Orașul este cunoscut pentru târgurile sale tradiționale " +
                    "și pentru pescuitul sportiv pe râul Criș. Biserica ortodoxă " +
                    "este un monument istoric important."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisana);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize UI elements
        textBalance = findViewById(R.id.textBalance);

        // Load saved states
        loadCheckboxStates();
        updatePointsDisplay();
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

            updatePointsDisplay();
        }
    }

    private void updatePointsDisplay() {
        if (textBalance != null) {
            int points = pointsManager.getTotalPoints(this);
            textBalance.setText("💰 Total Puncte: " + points);
        }
    }

    public void showPopup1(View view) {
        showPopup("Oradea", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Oradea");
        intent.putExtra("city_lat", 47.0722);
        intent.putExtra("city_lng", 21.9211);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Arad", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Arad");
        intent.putExtra("city_lat", 46.1866);
        intent.putExtra("city_lng", 21.3123);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Salonta", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Salonta");
        intent.putExtra("city_lat", 46.8000);
        intent.putExtra("city_lng", 21.6500);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Ineu", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Ineu");
        intent.putExtra("city_lat", 46.4333);
        intent.putExtra("city_lng", 21.8500);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Chișineu-Criș", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Chișineu-Criș");
        intent.putExtra("city_lat", 46.5167);
        intent.putExtra("city_lng", 21.5167);
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
