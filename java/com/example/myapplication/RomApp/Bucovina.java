package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class Bucovina extends AppCompatActivity {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "bucovina";

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
                    "facilitățile de tratament sunt principalele puncte de interes."
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

        if (checkBox1 != null) checkBox1.setChecked(sharedPreferences.getBoolean(userId + "_checkBox1_" + REGION, false));
        if (checkBox2 != null) checkBox2.setChecked(sharedPreferences.getBoolean(userId + "_checkBox2_" + REGION, false));
        if (checkBox3 != null) checkBox3.setChecked(sharedPreferences.getBoolean(userId + "_checkBox3_" + REGION, false));
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
    }

    public void showPopup2(View view) {
        showPopup("Câmpulung Moldovenesc", cityDescriptions[1]);
    }

    public void showPopup3(View view) {
        showPopup("Vatra Dornei", cityDescriptions[2]);
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

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (checkBox1 != null) editor.putBoolean(userId + "_checkBox1_" + REGION, checkBox1.isChecked());
        if (checkBox2 != null) editor.putBoolean(userId + "_checkBox2_" + REGION, checkBox2.isChecked());
        if (checkBox3 != null) editor.putBoolean(userId + "_checkBox3_" + REGION, checkBox3.isChecked());

        editor.apply();
    }
}
