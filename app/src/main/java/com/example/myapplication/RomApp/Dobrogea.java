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
import com.example.myapplication.dobrogeausage.CasinoStoryActivity;
import com.example.myapplication.dobrogeausage.DobrogeaGameActivity;
import com.example.myapplication.dobrogeausage.DobrogeaMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.DobrogeaViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class Dobrogea extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "dobrogea";
    private MaterialButton casinoButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private DobrogeaViewModel viewModel;

    @Override
    protected String getIntroductionText() {
        return "Dobrogea este o regiune istorică situată în sud-estul României, între fluviul Dunărea și țărmul Mării Negre. " +
               "Este renumită pentru litoralul său spectaculos, stațiunile turistice și Delta Dunării – una dintre cele mai bine conservate " +
               "deltă din Europa și rezervație a biosferei UNESCO.";
    }

    protected String getHistoryText() {
        return "Dobrogea a fost locuită încă din antichitate, fiind o regiune cu o istorie bogată și o diversitate culturală remarcabilă. A fost influențată de civilizații grecești, romane, bizantine, otomane și românești.\n\n" +

               "Perioada antică:\n" +
               "- Fondarea coloniilor grecești (secolele VII–VI î.Hr.): Histria, Tomis (Constanța), Callatis (Mangalia)\n" +
               "- Dobrogea devine parte a provinciei romane Moesia Inferior (secolul I d.Hr.)\n" +
               "- Fortificații romane importante: Durostorum, Axiopolis, Capidava, Noviodunum\n" +
               "- Exilul poetului roman Ovidiu la Tomis (8–17 d.Hr.), autor al operelor \"Tristele\" și \"Ponticele\"\n\n" +

               "Evul Mediu:\n" +
               "- Dobrogea sub stăpânirea Imperiului Bizantin, cunoscută ca Sciția Minor\n" +
               "- Răspândirea creștinismului timpuriu și dezvoltarea centrelor religioase\n" +
               "- Formarea statului dobrogean independent sub Balică și Dobrotici (secolul XIV)\n" +
               "- Cucerirea de către otomani în 1417 și stăpânirea acestora până în 1878\n" +
               "- Amestec etnic și cultural: români, bulgari, turci, tătari, greci\n\n" +

               "Perioada modernă:\n" +
               "- Dobrogea devine parte a României prin Tratatul de la Berlin (1878)\n" +
               "- Administrația românească se instalează la Constanța, care devine principal port maritim\n" +
               "- Construirea Podului Regele Carol I la Cernavodă (1895)\n" +
               "- Dezvoltarea litoralului: Mamaia, Eforie, Mangalia, Neptun\n" +
               "- Construirea Canalului Dunăre–Marea Neagră (finalizat în 1984), important pentru transport și comerț";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
               "- Regiunea este delimitată de Dunăre la vest și nord și de Marea Neagră la est\n" +
               "- Munții Măcinului – cei mai vechi munți din România, cu altitudini modeste\n" +
               "- Câmpii întinse, dealuri domoale și podișuri cu sol fertil\n" +
               "- Clima temperat-continentală cu influențe maritime: veri călduroase, ierni blânde\n" +
               "- Delta Dunării – rezervație a biosferei UNESCO, bogată în biodiversitate\n" +
               "- Litoralul cu plaje de nisip fin și faleze spectaculoase\n\n" +

               "Geografie umană și economică:\n" +
               "- Activități economice principale: agricultură, pescuit, turism, industrie portuară\n" +
               "- Portul Constanța – cel mai mare port din România și unul dintre cele mai importante din Europa\n" +
               "- Agricultura este bine dezvoltată, cu culturi de cereale, viță de vie și legume\n" +
               "- Energia eoliană este în plină dezvoltare, Dobrogea având cel mai mare potențial eolian din țară\n\n" +

               "Biodiversitate și zone protejate:\n" +
               "- Delta Dunării este habitat pentru peste 300 de specii de păsări și 45 de specii de pești\n" +
               "- Specii rare: pelicanul creț, sturionul, vidra europeană\n" +
               "- Zone protejate: Pădurea Letea, Pădurea Caraorman, Insula Popina\n" +
               "- Dobrogea este un paradis pentru ornitologi, ecoturiști și iubitorii de natură";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Dobrogea este un veritabil mozaic cultural, locuită de români, lipoveni, turci, tătari, aromâni, greci, bulgari, ucraineni și alte comunități etnice. " +
               "Această diversitate se reflectă în portul popular, obiceiuri, gastronomie și muzica tradițională, fiecare grup contribuind " +
               "cu propriile valori și tradiții la identitatea unică a regiunii.";
    }

    @Override
    protected String getAttractionsText() {
        return "Dobrogea oferă o varietate de atracții turistice remarcabile, printre care se numără:\n\n" +
               "- **Delta Dunării**: cea mai bine conservată deltă din Europa, inclusă în patrimoniul mondial UNESCO din 1991, recunoscută pentru biodiversitatea sa excepțională, cu peste 300 de specii de păsări și 45 de specii de pești.\n\n" +
               "- **Stațiunea Mamaia**: cea mai mare stațiune de pe litoralul românesc al Mării Negre, situată între mare și Lacul Siutghiol, renumită pentru plajele sale întinse cu nisip fin și facilitățile moderne pentru sporturi nautice.\n\n" +
               "- **Constanța și ruinele anticului Tomis**: oraș cu o istorie de peste 2.500 de ani, fondat de coloniștii greci din Milet, unde se pot vizita vestigii arheologice precum zidurile cetății și ruinele portului antic.\n\n" +
               "- **Cetatea Histria**: cel mai vechi oraș atestat de pe teritoriul actual al României, întemeiat în secolul VII î.Hr. de coloniștii greci din Milet, cu ruine impresionante care oferă o incursiune în istoria antică a regiunii.";
    }

    @Override
    protected String getGastronomyText() {
        return "Bucătăria dobrogeană este un melanj savuros de influențe românești, turco-tătare, lipovenești și balcanice, reflectând diversitatea culturală a regiunii. " +
               "Peștele joacă un rol central, preparate precum borșul de pește, storceagul de sturion și saramura de crap fiind emblematice. " +
               "Carnea de oaie este de asemenea populară, cu preparate precum cârnații de oaie și mielul la proțap. " +
               "Deserturile tradiționale, precum baclavaua și sarailiile, aduc o notă dulce orientală, completând astfel paleta gastronomică bogată a Dobrogei.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
               "- **Ovidiu**: poetul roman exilat la Tomis (actuala Constanța) în anul 8 d.Hr., unde a creat opere precum \"Tristele\" și \"Ponticele\".\n" +
               "- **Mihai Eminescu**: a petrecut o perioadă de convalescență în Constanța în 1882, cazat la fostul Hotel D'Angleterre, unde a scris scrisori către Veronica Micle și a descris orașul ca fiind \"mic, dar îngrijit\".\n\n" +
               "Evenimente culturale:\n" +
               "- **Festivalul Callatis**: unul dintre cele mai mari festivaluri de muzică și cultură din România, desfășurat în Mangalia, care include spectacole de muzică, teatru, dans, lansări de carte și expoziții.\n" +
               "- **Festivalul Antic Tomis**: eveniment anual organizat în Constanța, care reconstituie atmosfera antică prin parade militare, demonstrații de luptă, ateliere de meșteșuguri și spectacole de teatru antic.";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
               "- **Delta Dunării** deține cea mai mare suprafață compactă de stuf din lume, acoperind aproximativ 1.750 km², și adăpostește peste 5.500 de specii de plante și animale, dintre care peste 300 de specii de păsări?\n" +
               "- **Timișoara** a devenit, în 1884, primul oraș din Europa cu iluminat public electric, având 731 de lămpi incandescente pe o rețea de 60 km de străzi.";
    }

    @Override
    protected String getRegionName() {
        return "Dobrogea";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            cityImages.add("constanta");
            cityImages.add("tulcea");
            cityImages.add("mamaia");
            cityImages.add("histria");
            cityImages.add("sulina");
        }
        return cityImages;
    }

    private final String[] cityDescriptions = {
            "Constanța este principalul oraș al Dobrogei și al doilea ca mărime din România. " +
            "Orașul găzduiește Portul Constanța, cel mai mare port al Mării Negre, și numeroase " +
            "atracții turistice precum Casino-ul Constanța, Catedrala Sfinții Apostoli Petru și Pavel " +
            "și Muzeul de Istorie Națională și Arheologie.",

            "Tulcea este considerată poarta de intrare în Delta Dunării. Orașul este un important " +
            "centru cultural și economic, găzduind Muzeul Deltei Dunării și numeroase monumente " +
            "istorice. Este punctul de plecare pentru excursiile în Delta Dunării.",

            "Mamaia este cea mai mare stațiune de pe litoralul românesc, cunoscută pentru plajele " +
            "sale de nisip fin și viața de noapte animată. Stațiunea oferă multiple facilități " +
            "de cazare și agrement pentru turiști.",

            "Histria este cel mai vechi oraș atestat pe teritoriul României, fondat de greci în " +
            "secolul al VII-lea î.Hr. Ruinele antice includ temple, băi publice și un muzeu.",

            "Sulina este orașul de la vărsarea Dunării în Marea Neagră. Cunoscută pentru farul " +
            "sau istoric și pentru comunitatea lipovenească, Sulina oferă peisaje unice din Delta Dunării."
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
        setContentView(R.layout.activity_dobrogea);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize viewModel
        viewModel = new DobrogeaViewModel();

        // Initialize views
        pointsText = findViewById(R.id.pointsText);
        casinoButton = findViewById(R.id.buttonGoToCasinoStory);
        gameButton = findViewById(R.id.buttonGoToDobrogeaGame);
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
            com.example.myapplication.adapter.ImageCarouselAdapter adapter = 
                new com.example.myapplication.adapter.ImageCarouselAdapter(this, images);
            viewPager.setAdapter(adapter);
            
            com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.imageIndicator);
            new com.google.android.material.tabs.TabLayoutMediator(tabLayout, viewPager, 
                (tab, position) -> {}).attach();
        }
    }

    private void setupNavigationButtons() {
        citiesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CityListActivity.class);
            intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Dobrogea");
            startActivity(intent);
        });

        casinoButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 100) {
                Intent intent = new Intent(this, CasinoStoryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Casino Story!", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        gameButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 50) {
                Intent intent = new Intent(this, DobrogeaGameActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Dobrogea Game!", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        mapButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 25) {
                startMapActivity();
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 25 de puncte pentru a accesa harta!", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePointsDisplay();
        
        // Get the game score from the intent
        int gameScore = getIntent().getIntExtra("GAME_SCORE", 0);
        if (gameScore > 0) {
            // Add the game score to the total points
            pointsManager.addPoints(this, "dobrogea", gameScore);
            // Clear the score from the intent to avoid adding it multiple times
            getIntent().removeExtra("GAME_SCORE");
            // Update the points display
            updatePointsDisplay();
        }
    }

    private void updatePointsDisplay() {
        int points = pointsManager.getPoints(this);
        
        // Update the textBalance TextView
        if (pointsText != null) {
            pointsText.setText(String.valueOf(points));
        }
        
        // Also update the pointsTextView if it exists (used by EnhancedCityActivity)
        TextView pointsTextView = findViewById(R.id.pointsTextView);
        if (pointsTextView != null) {
            pointsTextView.setText(String.valueOf(points));
        }
    }

    private String getCurrentUserId() {
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return userPrefs.getString("current_user_id", "default");
    }

    private void loadCheckboxStates() {
        String userId = getCurrentUserId();
        // Remove references to undefined checkboxes
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
        }
    }

    public void showPopup1(View view) {
        showPopup("Constanța", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Constanța");
        intent.putExtra("city_lat", 44.1733);
        intent.putExtra("city_lng", 28.6383);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Tulcea", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Tulcea");
        intent.putExtra("city_lat", 45.1792);
        intent.putExtra("city_lng", 28.7969);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Mamaia", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Mamaia");
        intent.putExtra("city_lat", 44.2500);
        intent.putExtra("city_lng", 28.6333);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Histria", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Histria");
        intent.putExtra("city_lat", 44.5469);
        intent.putExtra("city_lng", 28.7750);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Sulina", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Sulina");
        intent.putExtra("city_lat", 45.1556);
        intent.putExtra("city_lng", 29.6539);
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
        
        // Remove references to undefined checkboxes
        editor.apply();
    }

    public void startMapActivity() {
        Intent intent = new Intent(this, DobrogeaMapActivity.class);
        startActivity(intent);
    }

    public void handleLocationClick(int locationId) {
        switch (locationId) {
            case 1: // Delta
                startActivity(DobrogeaGameActivity.class);
                break;
            case 2: // Casino
                startActivity(CasinoStoryActivity.class);
                break;
            case 3: // Histria
                Toast.makeText(this, "Această funcționalitate nu este disponibilă momentan", Toast.LENGTH_SHORT).show();
                break;
            case 4: // Beach
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
