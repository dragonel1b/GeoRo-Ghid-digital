package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Joc1.RomCityActivity;
import com.example.myapplication.R;
import java.util.ArrayList;

public class Banat extends RegionTemplate {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "banat";

    @Override
    protected String getIntroductionText() {
        return "Banatul este o regiune istorică în vestul României, cunoscută pentru diversitatea sa culturală " +
               "și peisajele sale variate, de la câmpii la zone montane.";
    }

    @Override
    protected String getHistoryGeographyText() {
        return "Banatul a fost locuit încă din antichitate, cu influențe romane, otomane și habsburgice. " +
               "Regiunea este străbătută de râurile Timiș, Bega și Caraș, cu o climă temperat-continentală.";
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Banatul este un adevărat mozaic cultural, cu comunități de români, sârbi, maghiari, germani " +
               "și alte minorități. Portul popular și muzica tradițională sunt foarte variate.";
    }

    @Override
    protected String getAttractionsText() {
        return "Principalele atracții includ Castelul Huniade din Timișoara, Cheile Nerei-Beușnița, " +
               "Stațiunea Băile Herculane și Parcul Național Semenic-Cheile Carașului.";
    }

    @Override
    protected String getGastronomyText() {
        return "Specificul culinar include ciorba bănățeană, gulaș, salam de Sibiu și alte preparate " +
               "cu influențe centrale-europene.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități: Nicolae Bălcescu, Ion Dragalina, Johnny Weissmuller.\n" +
               "Evenimente: Festivalul Timișoara European Capital of Culture, JazzTM.";
    }

    @Override
    protected String getCuriositiesText() {
        return "Timișoara a fost primul oraș european cu iluminat public electric (1884). " +
               "Banatul are cel mai mare procent de locuitori care vorbesc două limbi străine.";
    }

    @Override
    protected String getRegionName() {
        return "Banat";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("timisoara");
        images.add("resita");
        images.add("lugoj");
        images.add("caransebes");
        images.add("oravita");
        return images;
    }

    private final String[] cityDescriptions = {
            "Timișoara este cel mai important oraș al Banatului. " +
                    "Primul oraș european cu iluminat stradal electric, Timișoara este un centru cultural și economic major. " +
                    "Opera, teatrele și muzeele sale sunt renumite în toată țara. " +
                    "Piața Victoriei și Catedrala Metropolitană sunt simboluri ale orașului.",

            "Reșița este un important centru industrial. " +
                    "Orașul are o lungă tradiție în metalurgie și construcții de mașini. " +
                    "Muzeul de Locomotive cu Abur și Parcul Tricolorului sunt atracții importante.",

            "Lugoj este cunoscut pentru tradițiile sale culturale. " +
                    "Biserica Romano-Catolică și podul de fier sunt simboluri ale orașului. " +
                    "Festivalul de Operă și Operetă este un eveniment cultural important.",

            "Caransebeș, situat la confluența Timișului cu Sebeșul, " +
                    "este un important centru istoric și cultural. Muzeul Județean de Etnografie, " +
                    "Catedrala Ortodoxă și centrul vechi sunt principalele atracții.",

            "Oravița găzduiește cel mai vechi teatru din România. " +
                    "Teatrul Vechi, construit în 1817, este principala atracție. " +
                    "Zona este cunoscută și pentru calea ferată montană istorică."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banat);

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
        showPopup("Timișoara", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Timișoara");
        intent.putExtra("city_lat", 45.7597);
        intent.putExtra("city_lng", 21.2300);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Reșița", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Reșița");
        intent.putExtra("city_lat", 45.3000);
        intent.putExtra("city_lng", 21.8900);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Lugoj", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Lugoj");
        intent.putExtra("city_lat", 45.6861);
        intent.putExtra("city_lng", 21.9000);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Caransebeș", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Caransebeș");
        intent.putExtra("city_lat", 45.4167);
        intent.putExtra("city_lng", 22.2167);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Oravița", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Oravița");
        intent.putExtra("city_lat", 45.0333);
        intent.putExtra("city_lng", 21.6833);
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
