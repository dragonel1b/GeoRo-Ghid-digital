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

public class Oltenia extends AppCompatActivity {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "oltenia";

    private final String[] cityDescriptions = {
            "Craiova este cel mai important oraș al Olteniei. " +
                    "Centrul istoric, Parcul Nicolae Romanescu și Muzeul de Artă sunt atracții importante. " +
                    "Universitatea din Craiova și teatrul Național 'Marin Sorescu' sunt centre culturale semnificative. " +
                    "Orașul este și un important centru industrial și comercial.",

            "Râmnicu Vâlcea, reședința județului Vâlcea, este un important centru turistic. " +
                    "Stațiunile balneare din apropiere și mănăstirile din zonă atrag numeroși vizitatori. " +
                    "Parcul Zăvoi și Muzeul Județean sunt atracții locale importante.",

            "Drobeta-Turnu Severin păstrează importante vestigii istorice. " +
                    "Ruinele podului lui Traian și Castrul Roman sunt mărturii ale istoriei antice. " +
                    "Muzeul Porților de Fier și Castelul de Apă sunt obiective turistice importante.",

            "Târgu Jiu este orașul lui Constantin Brâncuși. " +
                    "Ansamblul monumental (Poarta Sărutului, Coloana Infinitului, Masa Tăcerii) " +
                    "reprezintă cea mai importantă operă de artă publică din România.",

            "Slatina este un important centru industrial. " +
                    "Parcul Esplanada și centrul istoric sunt principalele atracții. " +
                    "Industria aluminiului și cea alimentară sunt bine dezvoltate aici."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia);

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
        showPopup("Craiova", cityDescriptions[0]);
    }

    public void openCraiova(View view) {
        Intent intent = new Intent(this, Craiova.class);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Râmnicu Vâlcea", cityDescriptions[1]);
    }

    public void showPopup3(View view) {
        showPopup("Drobeta-Turnu Severin", cityDescriptions[2]);
    }

    public void openDrobeta(View view) {
        Intent intent = new Intent(this, Drobetaturnuseverin.class);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Slatina", cityDescriptions[4]);
    }

    public void openTarguJiu(View view) {
        Intent intent = new Intent(this, TarguJiu.class);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Râmnicu Vâlcea", cityDescriptions[1]);
    }

    public void openSlatina(View view) {
        Intent intent = new Intent(this, Slatina.class);
        startActivity(intent);
    }

    public void openValcea(View view) {
        Intent intent = new Intent(this, Valcea.class);
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
