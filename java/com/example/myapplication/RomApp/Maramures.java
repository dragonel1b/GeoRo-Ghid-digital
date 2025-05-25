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

public class Maramures extends RegionTemplate {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "maramures";

    @Override
    protected String getIntroductionText() {
        return "Maramureșul este o regiune istorică în nordul României, cunoscută pentru tradițiile sale " +
               "păstrate vii, arhitectura de lemn unică și peisajele montane spectaculoase.";
    }

    @Override
    protected String getHistoryGeographyText() {
        return "Maramureșul a fost locuit încă din epoca bronzului, cu influențe dacice și medievale.\n\n" +
               "Rețea hidrografică: Tisa, Vișeu, Iza, Mara, Cosău";
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Maramureșul păstrează tradiții unice în Europa:\n\n" +
               "- Portul popular cu motive geometrice\n" +
               "- Dansuri populare (brâul bătrânesc)\n" +
               "- Obiceiuri de nuntă și sărbători tradiționale";
    }

    @Override
    protected String getAttractionsText() {
        return "Principalele atracții:\n" +
               "- Mocănița de la Vișeu de Sus\n" +
               "- Bisericile de lemn din Maramureș (UNESCO)\n" +
               "- Cimitirul Vesel de la Săpânța\n" +
               "- Stațiunea Borșa\n" +
               "- Rezervația Naturală Creasta Cocoșului";
    }

    @Override
    protected String getGastronomyText() {
        return "Specificul culinar:\n" +
               "- Balmoș\n" +
               "- Brânză de burduf\n" +
               "- Ciolan afumat\n" +
               "- Plăcinte cu varză și brânză";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități:\n" +
               "- Ion Țuculescu\n" +
               "- Grigore Leșe\n" +
               "- Ilie Ilaș\n\n" +
               "Evenimente:\n" +
               "- Târgul de Fete pe Muntele Găina\n" +
               "- Festivalul de la Săpânța";
    }

    @Override
    protected String getCuriositiesText() {
        return "Curiozități:\n" +
               "- Cel mai înalt turn de lemn din lume (Biserica din Șurdești - 72m)\n" +
               "- Singurul cimitir vesel din lume (Săpânța)\n" +
               "- Tradiția măștilor de iarnă (Turca, Capra)";
    }

    @Override
    protected String getRegionName() {
        return "Maramureș";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("baia_mare");
        images.add("sighet");
        images.add("borsa");
        images.add("viseu");
        images.add("targu_lapus");
        return images;
    }

    private final String[] cityDescriptions = {
            "Baia Mare este reședința județului Maramureș. " +
                    "Turnul Ștefan și centrul istoric medieval sunt principalele atracții. " +
                    "Muzeul Județean de Mineralogie și Muzeul de Artă sunt instituții " +
                    "culturale importante.",

            "Sighetu Marmației este un important centru cultural. " +
                    "Memorialul Victimelor Comunismului și Casa Memorială Elie Wiesel " +
                    "sunt obiective importante. Muzeul Maramureșului păstrează " +
                    "tradițiile locale.",

            "Borșa este o importantă stațiune montană. " +
                    "Complexul turistic și pârtiile de schi atrag vizitatori " +
                    "în toate anotimpurile. Zona oferă oportunități pentru " +
                    "drumeții și sporturi de iarnă.",

            "Vișeu de Sus este cunoscut pentru Mocănița. " +
                    "Calea ferată forestieră cu locomotive cu abur este o " +
                    "atracție turistică unică în Europa. Orașul este și un " +
                    "punct de plecare pentru excursii montane.",

            "Târgu Lăpuș păstrează tradiții populare autentice. " +
                    "Biserica de lemn și arhitectura tradițională sunt " +
                    "principalele atracții. Festivalurile locale păstrează " +
                    "vie cultura maramureșeană."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maramures);

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
        showPopup("Baia Mare", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Baia Mare");
        intent.putExtra("city_lat", 47.6667);
        intent.putExtra("city_lng", 23.5833);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Sighetu Marmației", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Sighetu Marmației");
        intent.putExtra("city_lat", 47.9333);
        intent.putExtra("city_lng", 23.8833);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Borșa", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Borșa");
        intent.putExtra("city_lat", 47.6500);
        intent.putExtra("city_lng", 24.6667);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Vișeu de Sus", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Vișeu de Sus");
        intent.putExtra("city_lat", 47.7167);
        intent.putExtra("city_lng", 24.4333);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Târgu Lăpuș", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Târgu Lăpuș");
        intent.putExtra("city_lat", 47.4500);
        intent.putExtra("city_lng", 23.8667);
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
