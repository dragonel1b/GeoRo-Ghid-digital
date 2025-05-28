package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.Joc1.RomCityActivity;
import com.example.myapplication.R;
import com.example.myapplication.olteniausage.OlteniaStoryActivity;
import com.example.myapplication.olteniausage.OlteniaGameActivity;
import com.example.myapplication.olteniausage.OlteniaMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.OlteniaViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class Oltenia extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "Oltenia";
    private MaterialButton storyButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private OlteniaViewModel viewModel;

    @Override
    protected String getIntroductionText() {
        return "Oltenia este o regiune istorică situată în sud-vestul României, între Carpați la nord, " +
               "Dunărea la sud și vest, și Olt la est. Este cunoscută pentru peisajele sale spectaculoase, " +
               "monumentele istorice și tradițiile sale bogate.";
    }

    protected String getHistoryText() {
        return "Oltenia are o istorie bogată care se întinde pe milenii:\n\n" +

               "Perioada antică:\n" +
               "- Teritoriul a fost locuit de daci, cu capitala la Sarmizegetusa Regia\n" +
               "- Cucerirea romană în 106 d.Hr. sub împăratul Traian\n" +
               "- Formarea provinciei Dacia Inferior\n" +
               "- Dezvoltarea orașelor romane: Drobeta, Romula, Sucidava\n\n" +

               "Evul Mediu:\n" +
               "- Formarea voievodatului Oltenia în secolul XIII\n" +
               "- Stăpânirea Basarabilor și a Craioveștilor\n" +
               "- Băniei Craiovești – una dintre cele mai importante instituții medievale\n" +
               "- Luptele cu otomanii și perioada de autonomie sub Banii Craiovești\n\n" +

               "Perioada modernă:\n" +
               "- Unirea cu Țara Românească în 1859\n" +
               "- Dezvoltarea economică și culturală în secolul XIX\n" +
               "- Construirea căilor ferate și dezvoltarea industriei\n" +
               "- Modernizarea orașelor și a infrastructurii";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
               "- Carpații Meridionali la nord, cu vârfuri impresionante\n" +
               "- Câmpia Olteniei în sud, fertilă și bine irigată\n" +
               "- Râul Olt, care formează granița naturală cu Muntenia\n" +
               "- Dunărea, care formează granița cu Serbia și Bulgaria\n" +
               "- Clima temperat-continentală cu veri călduroase\n\n" +

               "Geografie umană și economică:\n" +
               "- Agricultura dezvoltată în câmpie\n" +
               "- Industria extractivă în zona montană\n" +
               "- Centru important pentru producția de energie hidroelectrică\n" +
               "- Turism bazat pe monumentele istorice și peisajele naturale\n\n" +

               "Resurse naturale și zone protejate:\n" +
               "- Parcul Național Buila-Vânturară\n" +
               "- Cheile Oltului și Cheile Cozia\n" +
               "- Păduri seculare și rezervații naturale\n" +
               "- Zone protejate pentru biodiversitate";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Oltenia păstrează tradiții autentice românești, cu obiceiuri și obiceiuri specifice. " +
               "Portul popular oltenesc este cunoscut pentru culorile vii și motivele geometrice. " +
               "Muzica populară oltenească, cu doinele și cântecele de dragoste, este una dintre cele mai " +
               "bogate din România. Meșteșugurile tradiționale, precum țesătoria și olăritul, sunt încă " +
               "păstrate și transmise din generație în generație.";
    }

    @Override
    protected String getAttractionsText() {
        return "Oltenia oferă numeroase atracții turistice deosebite:\n\n" +
               "- **Mănăstirea Cozia**: fondată în 1388 de Mircea cel Bătrân, este una dintre cele mai " +
               "importante mănăstiri medievale din România, cu arhitectură bizantină și pictură murală " +
               "excepțională.\n\n" +
               "- **Cheile Oltului**: o zonă spectaculoasă unde râul Olt străbate Carpații, oferind " +
               "peisaje naturale impresionante și posibilități de drumeție.\n\n" +
               "- **Craiova**: orașul cel mai mare din Oltenia, cu numeroase monumente istorice, " +
               "muzee și parcuri. Este renumit pentru Grădina Botanică și Muzeul de Artă.\n\n" +
               "- **Mănăstirea Horezu**: inclusă în patrimoniul UNESCO, este renumită pentru arhitectura " +
               "sa și școala de pictură murală și iconițe.";
    }

    @Override
    protected String getGastronomyText() {
        return "Bucătăria oltenească este cunoscută pentru preparatele sale tradiționale și gustoase. " +
               "Printre cele mai cunoscute se numără ciorba de burtă, sarmalele în foi de viță, " +
               "mămăliguta cu brânză și smântână, și piftia. Carnea de porc este foarte populară, " +
               "preparată în diverse moduri, iar cozonacul oltenesc este renumit pentru gustul său " +
               "deosebit. Vinurile din Oltenia, în special cele din Dealurile Craiovei, sunt apreciate " +
               "pentru calitatea lor.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
               "- **Tudor Vladimirescu**: lider al revoluției din 1821, născut în Oltenia\n" +
               "- **Ion Minulescu**: poet și scriitor modern, născut în Craiova\n" +
               "- **Nicolae Titulescu**: diplomat și politician de seamă\n\n" +
               "Evenimente culturale:\n" +
               "- **Festivalul Oltenia**: cel mai mare festival folcloric din regiune\n" +
               "- **Târgul de Toamnă din Craiova**: tradițional târg cu meșteșuguri și produse locale\n" +
               "- **Festivalul Internațional de Teatru din Craiova**: unul dintre cele mai importante " +
               "festivaluri de teatru din România";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
               "- **Mănăstirea Horezu** a fost fondată în 1690 de Constantin Brâncoveanu și găzduiește " +
               "cea mai mare clopot din România?\n" +
               "- **Craiova** a fost capitala Olteniei și a găzduit primul teatru din România, fondat în 1850?\n" +
               "- **Cheile Oltului** sunt considerate una dintre cele mai spectaculoase defileuri din România, " +
               "cu o lungime de peste 60 de kilometri?";
    }

    @Override
    protected String getRegionName() {
        return "Oltenia";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("craiova");
        images.add("drobeta");
        images.add("targujiu");
        images.add("calimanesti");
        images.add("valcea");
        return images;
    }

    private final String[] cityDescriptions = {
            "Craiova este cel mai mare oraș din Oltenia și al șaselea ca mărime din România. " +
            "Este un important centru cultural și economic, găzduind numeroase monumente istorice, " +
            "muzee și parcuri. Orașul este renumit pentru Grădina Botanică și Muzeul de Artă.",

            "Drobeta-Turnu Severin este un oraș istoric situat pe malul Dunării. Este cunoscut pentru " +
            "ruinele cetății medievale și pentru podul lui Traian, cel mai vechi pod peste Dunăre.",

            "Târgu Jiu este renumit pentru ansamblul sculptural Constantin Brâncuși, care include " +
            "Masa Tăcerii, Poarta Sărutului și Coloana Infinitului. Orașul este un important centru " +
            "cultural și economic.",

            "Călimănești-Căciulata este o stațiune balneoclimaterică cunoscută pentru apele sale " +
            "minerale și pentru peisajele spectaculoase din Cheile Oltului.",

            "Râmnicu Vâlcea este un oraș istoric situat în inima Olteniei, cu numeroase monumente " +
            "istorice și o bogăție culturală remarcabilă."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize viewModel
        viewModel = new OlteniaViewModel();

        // Initialize views - using correct IDs from the layout
        pointsText = findViewById(R.id.pointsText);
        storyButton = findViewById(R.id.buttonGoToOlteniaStory);
        gameButton = findViewById(R.id.buttonGoToOlteniaGame);
        citiesButton = findViewById(R.id.buttonGoToCities);
        mapButton = findViewById(R.id.buttonGoToMap);

        // Set up image carousel
        setupImageCarousel();

        // Initialize content sections
        initializeSpecificContent();

        // Set up navigation buttons
        setupNavigationButtons();

        // Load saved states
        loadCheckboxStates();

        // Update points display
        updatePointsDisplay();
    }

    private void setupImageCarousel() {
        ArrayList<String> images = getCityImages();
        if (images != null && !images.isEmpty()) {
            androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.imageCarousel);
            if (viewPager != null) {
                com.example.myapplication.adapter.ImageCarouselAdapter adapter = 
                    new com.example.myapplication.adapter.ImageCarouselAdapter(this, images);
                viewPager.setAdapter(adapter);
                
                com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
                if (tabLayout != null) {
                    new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager, 
                        (tab, position) -> {}).attach();
                }
            }
        }
    }

    private void setupNavigationButtons() {
        if (citiesButton != null) {
            citiesButton.setOnClickListener(v -> {
                // Debug Log
                System.out.println("DEBUG: Cities button clicked");
                
                try {
                    // Trying with exact path to activity
                    Intent intent = new Intent(this, com.example.myapplication.viewmodel.CityListActivity.class);
                    intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Oltenia");
                    startActivity(intent);
                } catch (Exception e) {
                    System.out.println("DEBUG: Error starting CityListActivity: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Eroare la deschiderea listei de orașe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            System.out.println("DEBUG: citiesButton is null");
        }

        if (storyButton != null) {
            storyButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 100) {
                    Intent intent = new Intent(this, OlteniaStoryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Oltenia Story!", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (gameButton != null) {
            gameButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 50) {
                    Intent intent = new Intent(this, OlteniaGameActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Oltenia Game!", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (mapButton != null) {
            mapButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 25) {
                    startMapActivity();
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 25 de puncte pentru a accesa harta!", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePointsDisplay();
        
        // Get the game score from the intent
        int gameScore = getIntent().getIntExtra("GAME_SCORE", 0);
        if (gameScore > 0) {
            // Add the game score to the total points
            pointsManager.addPoints(this, "oltenia", gameScore);
            // Clear the score from the intent to avoid adding it multiple times
            getIntent().removeExtra("GAME_SCORE");
            // Update the points display
            updatePointsDisplay();
        }
    }

    private void updatePointsDisplay() {
        if (pointsText != null) {
            int points = pointsManager.getPoints(this);
            pointsText.setText(String.valueOf(points));
        }
    }

    private String getCurrentUserId() {
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return userPrefs.getString("current_user_id", "default");
    }

    private void loadCheckboxStates() {
        String userId = getCurrentUserId();
        
        // Iterate through all checkboxes in the layout
        for (int i = 1; i <= 5; i++) {
            int checkboxId = getResources().getIdentifier("checkbox" + i, "id", getPackageName());
            if (checkboxId != 0) {  // The checkbox exists in the layout
                CheckBox checkbox = findViewById(checkboxId);
                if (checkbox != null) {
                    // The key format is: userId_checkboxId_REGION
                    String key = userId + "_" + getResources().getResourceEntryName(checkboxId) + "_" + REGION;
                    boolean isChecked = sharedPreferences.getBoolean(key, false);
                    checkbox.setChecked(isChecked);
                }
            }
        }
        
        // Update points display after loading checkboxes
        updatePointsDisplay();
    }

    public void onCheckboxClicked(View view) {
        if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            boolean isChecked = checkBox.isChecked();
            
            // Folosim doar updateLandmarkStatus pentru a gestiona punctele
            pointsManager.updateLandmarkStatus(this, REGION.toLowerCase(), isChecked);

            // Save state with user ID
            String userId = getCurrentUserId();
            String checkBoxId = getResources().getResourceEntryName(view.getId());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(userId + "_" + checkBoxId + "_" + REGION, isChecked);
            editor.apply();
            
            // Actualizează afișarea punctelor imediat
            updatePointsDisplay();
        }
    }

    public void showPopup1(View view) {
        showPopup("Craiova", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Craiova");
        intent.putExtra("city_lat", 44.3190);
        intent.putExtra("city_lng", 23.7967);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Drobeta-Turnu Severin", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Drobeta-Turnu Severin");
        intent.putExtra("city_lat", 44.6253);
        intent.putExtra("city_lng", 22.6599);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Târgu Jiu", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Târgu Jiu");
        intent.putExtra("city_lat", 45.0364);
        intent.putExtra("city_lng", 23.2747);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Călimănești", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Călimănești");
        intent.putExtra("city_lat", 45.2392);
        intent.putExtra("city_lng", 24.3374);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Râmnicu Vâlcea", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Râmnicu Vâlcea");
        intent.putExtra("city_lat", 45.1006);
        intent.putExtra("city_lng", 24.3671);
        startActivity(intent);
    }

    private void showPopup(String title, String description) {
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title)
                    .setMessage(description)
                    .setPositiveButton("Închide", null)
                   .setCancelable(true);
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
            // Set text colors for dark mode
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                getResources().getColor(R.color.rom_primary));
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // Iterate through all checkboxes in the layout
        for (int i = 1; i <= 5; i++) {
            int checkboxId = getResources().getIdentifier("checkbox" + i, "id", getPackageName());
            if (checkboxId != 0) {  // The checkbox exists in the layout
                CheckBox checkbox = findViewById(checkboxId);
                if (checkbox != null) {
                    // The key format is: userId_checkboxId_REGION
                    String key = userId + "_" + getResources().getResourceEntryName(checkboxId) + "_" + REGION;
                    editor.putBoolean(key, checkbox.isChecked());
                }
            }
        }
        
        editor.apply();
    }

    public void startMapActivity() {
        try {
            // Explicitly use the full class name to ensure we load the correct one
            Class<?> mapActivityClass = Class.forName("com.example.myapplication.olteniausage.OlteniaMapActivity");
            Intent intent = new Intent(this, mapActivityClass);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Nu s-a putut deschide harta Olteniei", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleLocationClick(int locationId) {
        switch (locationId) {
            case 1: // Craiova
                startActivity(OlteniaGameActivity.class);
                break;
            case 2: // Targu Jiu
                startActivity(OlteniaStoryActivity.class);
                break;
            case 3: // Drobeta
                Toast.makeText(this, "Această funcționalitate nu este disponibilă momentan", Toast.LENGTH_SHORT).show();
                break;
            case 4: // Valcea
                Toast.makeText(this, "Această funcționalitate nu este disponibilă momentan", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    public void updateProgress(int locationId, boolean completed) {
        viewModel.updateLocationProgress(locationId, completed);
    }

    public boolean isLocationCompleted(int locationId) {
        return viewModel.isLocationCompleted(locationId);
    }

    public int getCompletedLocationsCount() {
        return viewModel.getCompletedLocationsCount();
    }
}
