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

public class Banat extends AppCompatActivity {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "banat";

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
    }

    public void showPopup2(View view) {
        showPopup("Reșița", cityDescriptions[1]);
    }

    public void showPopup3(View view) {
        showPopup("Lugoj", cityDescriptions[2]);
    }

    public void showPopup4(View view) {
        showPopup("Caransebeș", cityDescriptions[3]);
    }

    public void showPopup5(View view) {
        showPopup("Oravița", cityDescriptions[4]);
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
