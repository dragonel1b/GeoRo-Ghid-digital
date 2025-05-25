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

public class Bucovina extends RegionTemplate {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "bucovina";

    @Override
    protected String getIntroductionText() {
        return "Bucovina este o regiune istorică în nordul României, cunoscută pentru mănăstirile sale " +
               "pictate (patrimoniu UNESCO), peisajele montane și tradițiile păstrate vii.";
    }

    @Override
    protected String getHistoryGeographyText() {
        return "Bucovina a fost locuită încă din epoca bronzului, cu influențe dacice și medievale.\n\n" +
               "Rețea hidrografică: Siret, Moldova, Suceava, Bistrița";
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Bucovina păstrează tradiții unice:\n\n" +
               "- Pictura murală pe mănăstiri\n" +
               "- Portul popular cu motive florale\n" +
               "- Meșteșuguri tradiționale (ouă încondeiate)";
    }

    @Override
    protected String getAttractionsText() {
        return "Principalele atracții:\n" +
               "- Mănăstirile pictate (UNESCO)\n" +
               "- Cetatea de Scaun a Sucevei\n" +
               "- Parcul Național Călimani\n" +
               "- Stațiunea Vatra Dornei\n" +
               "- Muzeul Satului Bucovinean";
    }

    @Override
    protected String getGastronomyText() {
        return "Specificul culinar:\n" +
               "- Balmoș bucovinean\n" +
               "- Plăcinte cu brânză de oi\n" +
               "- Ciorba de potroace\n" +
               "- Colaci cu nucă";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități:\n" +
               "- Ștefan cel Mare\n" +
               "- Nicolae Labiș\n" +
               "- Ciprian Porumbescu\n\n" +
               "Evenimente:\n" +
               "- Festivalul Medieval de la Suceava\n" +
               "- Târgul de Toamnă de la Câmpulung";
    }

    @Override
    protected String getCuriositiesText() {
        return "Curiozități:\n" +
               "- Mănăstirile pictate sunt pe lista patrimoniului UNESCO\n" +
               "- Cel mai vechi stejar din România (500+ ani) se află la Putna\n" +
               "- Tradiția ouălor încondeiate este unică în lume";
    }

    @Override
    protected String getRegionName() {
        return "Bucovina";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("suceava");
        images.add("campulung");
        images.add("vatra_dornei");
        images.add("gura_humorului");
        images.add("putna");
        return images;
    }

    private final String[] cityDescriptions = {
            "Suceava, fosta capitală a Moldovei, este un important centru istoric și cultural. " +
                    "Cetatea de Scaun a Sucevei, construită în secolul al XIV-lea, este principalul punct de atracție. " +
                    "Orașul găzduiește numeroase muzee și mănăstiri istorice, precum Mănăstirea Sfântul Ioan cel Nou. " +
                    "Centrul vechi al orașului păstrează arhitectura tradițională bucovineană.",

            "Câmpulung Moldovenesc, situat în inima Bucovinei, este un important centru turistic. " +
                    "Orașul este înconjurat de munți pitorești și oferă multiple oportunități pentru drumeții și schi. " +
                    "Muzeul Lemnului și arhitectura tradițională bucovineană sunt atracții importante. " +
                    "Festivalurile locale păstrează vie cultura și tradițiile zonei.",

            "Vatra Dornei este o renumită stațiune balneoclimaterică. " +
                    "Cunoscută pentru apele minerale cu proprietăți curative și pentru pârtiile de schi, " +
                    "orașul atrage turiști în toate anotimpurile. Cazinoul istoric, parcurile și " +
                    "facilitățile de tratament sunt principalele puncte de interes.",

            "Gura Humorului este poarta de intrare în Bucovina. " +
                    "Orașul este cunoscut pentru Mănăstirea Voroneț și pentru tradițiile locale. " +
                    "Muzeul de Etnografie și Artă Populară prezintă bogăția culturală a zonei.",

            "Putna este un important centru spiritual. " +
                    "Mănăstirea Putna, ctitoria lui Ștefan cel Mare, este un simbol al ortodoxiei românești. " +
                    "Zona este cunoscută și pentru peisajele sale montane spectaculoase."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucovina);

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
        showPopup("Suceava", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Suceava");
        intent.putExtra("city_lat", 47.6333);
        intent.putExtra("city_lng", 26.2500);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Câmpulung Moldovenesc", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Câmpulung Moldovenesc");
        intent.putExtra("city_lat", 47.5333);
        intent.putExtra("city_lng", 25.5500);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Vatra Dornei", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Vatra Dornei");
        intent.putExtra("city_lat", 47.3500);
        intent.putExtra("city_lng", 25.3667);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Gura Humorului", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Gura Humorului");
        intent.putExtra("city_lat", 47.5500);
        intent.putExtra("city_lng", 25.9000);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Putna", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Putna");
        intent.putExtra("city_lat", 47.8667);
        intent.putExtra("city_lng", 25.6167);
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
        Intent intent = new Intent(this, UserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
