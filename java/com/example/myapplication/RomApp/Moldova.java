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

public class Moldova extends AppCompatActivity {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "moldova";

    private final String[] cityDescriptions = {
            "Iași este cel mai important centru cultural și istoric al Moldovei. " +
                    "Aici se află Palatul Culturii, simbol al orașului, Universitatea Alexandru Ioan Cuza, " +
                    "cea mai veche universitate din România, și numeroase biserici și mănăstiri istorice. " +
                    "Orașul este cunoscut și pentru Teatrul Național 'Vasile Alecsandri' și Grădina Botanică.",

            "Bacău este un important centru economic și cultural al Moldovei. " +
                    "Orașul găzduiește Casa Memorială 'George Bacovia', Observatorul Astronomic și " +
                    "numeroase instituții culturale. Este și un important nod de transport, având " +
                    "unul dintre cele mai mari aeroporturi din regiune.",

            "Piatra Neamț, supranumită 'Perla Moldovei', este situat într-un cadru natural " +
                    "spectaculos, la poalele munților. Telecabina și pârtia de schi, Curtea Domnească și " +
                    "Turnul lui Ștefan cel Mare sunt principalele atracții. Orașul este și punct de plecare " +
                    "spre mănăstirile din zonă.",

            "Botoșani este orașul natal al mai multor personalități culturale importante, " +
                    "precum Mihai Eminescu și George Enescu. Casa Memorială 'Nicolae Iorga', " +
                    "Muzeul Județean și centrul istoric sunt principalele puncte de interes. " +
                    "Orașul păstrează o arhitectură specifică secolului XIX.",

            "Galați este unul dintre cele mai importante porturi la Dunăre din România. " +
                    "Orașul găzduiește Universitatea 'Dunărea de Jos', Muzeul de Istorie și Complexul Muzeal " +
                    "de Științele Naturii. Faleza Dunării, Grădina Botanică și Biserica Fortificată Precista " +
                    "sunt printre principalele atracții turistice. Combinatul Siderurgic a fost mult timp " +
                    "simbolul industrial al orașului."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moldova);

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
        showPopup("Iași", cityDescriptions[0]);
    }

    public void showPopup2(View view) {
        showPopup("Bacău", cityDescriptions[1]);
    }

    public void showPopup3(View view) {
        showPopup("Piatra Neamț", cityDescriptions[2]);
    }

    public void showPopup4(View view) {
        showPopup("Botoșani", cityDescriptions[3]);
    }

    public void showPopup5(View view) {
        showPopup("Galați", cityDescriptions[4]);
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
