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

public class Muntenia extends AppCompatActivity {

    private TextView textBalance;
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "muntenia";

    private final String[] cityDescriptions = {
            "București, capitala României, este cel mai mare oraș și centrul cultural, financiar și economic al țării. " +
                    "Palatul Parlamentului, a doua cea mai mare clădire administrativă din lume, domină peisajul urban. " +
                    "Orașul găzduiește numeroase muzee, teatre, parcuri și centre culturale, precum Ateneul Român, " +
                    "Muzeul Național de Artă și Centrul Vechi istoric.",

            "Pitești este un important centru industrial și cultural al Munteniei. " +
                    "Orașul este cunoscut pentru industria auto, fiind sediul uzinei Dacia. " +
                    "Centrul istoric, Parcul Ștrand și Catedrala Sfântul Gheorghe sunt atracții importante. " +
                    "Grădina Botanică și Muzeul Județean sunt destinații culturale populare.",

            "Târgoviște, fosta capitală a Țării Românești, păstrează importante monumente istorice. " +
                    "Complexul Muzeal Curtea Domnească, Turnul Chindiei și Muzeul Tiparului sunt principalele atracții. " +
                    "Orașul are o bogată istorie culturală și a fost reședința mai multor domnitori importanți.",

            "Curtea de Argeș este un important centru spiritual și istoric. " +
                    "Mănăstirea Curtea de Argeș, necropola regală a României, este principalul punct de atracție. " +
                    "Biserica Domnească și ruinele Curții Domnești sunt mărturii ale istoriei medievale românești.",

            "Giurgiu este cel mai important port dunărean al Munteniei. " +
                    "Podul Prieteniei peste Dunăre leagă România de Bulgaria. Turnul Ceasornicului și " +
                    "Muzeul Județean de Istorie sunt atracții importante. Orașul are un rol strategic în " +
                    "transportul fluvial și comerțul internațional.",

            "Alexandria, reședința județului Teleorman, este un important centru agricol. " +
                    "Catedrala Sfântul Alexandru, Parcul Pădurea Vedea și Muzeul Județean sunt principalele " +
                    "atracții. Orașul este cunoscut pentru agricultura dezvoltată și industria alimentară."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muntenia);

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
        CheckBox checkBox6 = findViewById(R.id.checkBox6);

        if (checkBox1 != null) checkBox1.setChecked(sharedPreferences.getBoolean(userId + "_checkBox1_" + REGION, false));
        if (checkBox2 != null) checkBox2.setChecked(sharedPreferences.getBoolean(userId + "_checkBox2_" + REGION, false));
        if (checkBox3 != null) checkBox3.setChecked(sharedPreferences.getBoolean(userId + "_checkBox3_" + REGION, false));
        if (checkBox4 != null) checkBox4.setChecked(sharedPreferences.getBoolean(userId + "_checkBox4_" + REGION, false));
        if (checkBox5 != null) checkBox5.setChecked(sharedPreferences.getBoolean(userId + "_checkBox5_" + REGION, false));
        if (checkBox6 != null) checkBox6.setChecked(sharedPreferences.getBoolean(userId + "_checkBox6_" + REGION, false));
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
        showPopup("București 👑", cityDescriptions[0]);
    }

    public void showPopup2(View view) {
        showPopup("Pitești", cityDescriptions[1]);
    }

    public void showPopup3(View view) {
        showPopup("Târgoviște", cityDescriptions[2]);
    }

    public void showPopup4(View view) {
        showPopup("Curtea de Argeș", cityDescriptions[3]);
    }

    public void showPopup5(View view) {
        showPopup("Giurgiu", cityDescriptions[4]);
    }

    public void showPopup6(View view) {
        showPopup("Alexandria", cityDescriptions[5]);
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
        CheckBox checkBox6 = findViewById(R.id.checkBox6);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (checkBox1 != null) editor.putBoolean(userId + "_checkBox1_" + REGION, checkBox1.isChecked());
        if (checkBox2 != null) editor.putBoolean(userId + "_checkBox2_" + REGION, checkBox2.isChecked());
        if (checkBox3 != null) editor.putBoolean(userId + "_checkBox3_" + REGION, checkBox3.isChecked());
        if (checkBox4 != null) editor.putBoolean(userId + "_checkBox4_" + REGION, checkBox4.isChecked());
        if (checkBox5 != null) editor.putBoolean(userId + "_checkBox5_" + REGION, checkBox5.isChecked());
        if (checkBox6 != null) editor.putBoolean(userId + "_checkBox6_" + REGION, checkBox6.isChecked());

        editor.apply();
    }
}
