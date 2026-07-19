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
import com.example.myapplication.ui.ComposeEntryActivity;
import com.example.myapplication.R;
import com.example.myapplication.crisanausage.CrisanaStoryActivity;
import com.example.myapplication.crisanausage.CrisanaGameActivity;
import com.example.myapplication.crisanausage.CrisanaMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.CrisanaViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class Crisana extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "Crisana";
    private MaterialButton storyButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private CrisanaViewModel viewModel;

    @Override
    protected String getIntroductionText() {
        return "Crișana este o regiune istorică și culturală situată în vestul României, la granița cu Ungaria. " +
               "Regiunea este numită după râul Crișul care o străbate și este formată din părți ale județelor Bihor, " +
               "Arad, Satu Mare și Sălaj. Crișana se remarcă prin peisaje diverse, de la câmpii fertile la dealuri și munți, " +
               "precum și prin bogăția patrimoniului cultural și istoric.";
    }

    protected String getHistoryText() {
        return "Istoria Crișanei este marcată de diversitatea etnică și culturală, cu influențe românești, maghiare și germane. " +
               "Teritoriul a făcut parte din Voievodatul Transilvaniei, apoi din Imperiul Habsburgic și Austro-Ungar până la " +
               "Marea Unire din 1918. Regiunea a fost martora unor evenimente importante precum Răscoala lui Horea, Cloșca și Crișan din 1784. " +
               "Numele regiunii provine chiar de la unul dintre conducătorii acestei răscoale, Crișan. " +
               "De-a lungul timpului, orașele din Crișana au cunoscut o dezvoltare economică și culturală semnificativă, " +
               "devenind centre importante pentru comerț, meșteșuguri și învățământ.";
    }

    protected String getGeographyText() {
        return "Din punct de vedere geografic, Crișana cuprinde atât zone de câmpie (Câmpia de Vest), cât și zone deluroase " +
               "(Dealurile de Vest) și zone montane (Munții Apuseni). Regiunea este străbătută de râurile Crișul Repede, " +
               "Crișul Negru și Crișul Alb, care își au izvoarele în Munții Apuseni și se varsă în Tisa. " +
               "Clima este temperat-continentală cu influențe oceanice, favorabilă agriculturii și viticulturii. " +
               "Peisajul natural include și zone protejate precum Parcul Natural Apuseni, cu peșteri spectaculoase, " +
               "chei și defileuri. Izvoarele termale din regiune au favorizat dezvoltarea stațiunilor balneoclimaterice " +
               "precum Băile Felix și Băile 1 Mai.";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Cultura și tradițiile din Crișana reflectă caracterul multicultural al regiunii. Portul popular românesc " +
               "din zonă se remarcă prin eleganță și cromatică bogată, cu motive florale și geometrice. " +
               "Arhitectura tradițională păstrează elemente specifice, cu case din lemn și porți sculptate în stil maramureșean. " +
               "Muzica și dansurile populare precum \"Roata\", \"Ardeleana\" și \"Mânânțălu\" sunt practicate și astăzi la sărbători. " +
               "Tradițiile legate de ciclul vieții (nuntă, botez, înmormântare) și de sărbătorile religioase păstrează " +
               "elemente arhaice și ritualuri specifice. Meșteșugurile tradiționale precum olăritul, prelucrarea lemnului, " +
               "țesutul și broderia sunt încă practicate în satele din regiune.";
    }

    @Override
    protected String getAttractionsText() {
        return "Printre atracțiile turistice ale Crișanei se numără:\n\n" +
               "• Orașul Oradea, cu centrul său istoric baroc și Art Nouveau, Cetatea Oradea și Palatul Episcopal\n" +
               "• Băile Felix, cea mai mare stațiune balneară din România\n" +
               "• Peșterile din Munții Apuseni: Peștera Urșilor, Peștera Vântului, Peștera Meziad\n" +
               "• Cetățile medievale: Șiria, Dezna, Șoimoș\n" +
               "• Parcul Natural Apuseni cu circuitul carstic Cetățile Ponorului\n" +
               "• Cheile Crișului Repede și Defileul Crișului Repede\n" +
               "• Mănăstirile din Bihor: Beiuș, Izbuc, Voivozi\n" +
               "• Castele și conace: Castelul de la Șiria, Castelul Károlyi din Carei\n" +
               "• Trasee de drumeție și ciclism montan în Munții Apuseni și Munții Codru-Moma";
    }

    @Override
    protected String getGastronomyText() {
        return "Gastronomia din Crișana îmbină influențe românești, maghiare și germane, rezultând preparate specifice " +
               "zonei. Printre specialitățile culinare se numără:\n\n" +
               "• Pâinea \"Piparkă\" cu boia de ardei\n" +
               "• Gulaș bihorean cu carne de vită și legume\n" +
               "• Ciorba de fasole cu ciolan afumat servită în pâine\n" +
               "• Sarmale cu păsat (mălai măcinat grosier)\n" +
               "• Plăcinta \"Bodag\" cu brânză, cartofi sau varză\n" +
               "• Țuica de prune din Bihor, vestitele vinuri de Miniș-Măderat și Diosig\n" +
               "• Preparate din pește de apă dulce precum crapul umplut și șalăul la grătar\n" +
               "• Dulciuri tradiționale precum \"Gomboți\" (găluște cu prune) și \"Reteș\" (ștrudel cu diverse umpluturi)";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante din Crișana:\n\n" +
               "• Iosif Vulcan - scriitor și publicist, fondatorul revistei \"Familia\"\n" +
               "• Iuliu Maniu - om politic, prim-ministru al României\n" +
               "• Aurel Vlaicu - pionier al aviației românești\n" +
               "• Ioan Slavici - scriitor și publicist\n" +
               "• Emanuil Gojdu - jurist și filantrop\n" +
               "• Ady Endre - poet maghiar modernist\n\n" +
               "Evenimente culturale importante:\n\n" +
               "• Festivalul Internațional de Teatru Oradea\n" +
               "• Zilele Culturale Maghiare din Oradea\n" +
               "• Festivalul Folcloric \"Cântecele Munților\" din Apuseni\n" +
               "• Târgul de Ceramică Tradițională \"Vasul de Lut\" de la Crișcior\n" +
               "• Festivalul de Datini și Obiceiuri de Iarnă din Beiuș";
    }

    @Override
    protected String getCuriositiesText() {
        return "Curiozități despre Crișana:\n\n" +
               "• Oradea este cunoscută drept \"orașul Art Nouveau\" datorită numeroaselor clădiri în acest stil arhitectural\n" +
               "• Peștera Urșilor a fost descoperită accidental în 1975, în urma unei detonări într-o carieră de marmură\n" +
               "• În Crișana se află una dintre cele mai moderne grădini zoologice din România, Zoo Oradea\n" +
               "• Izvoarele termale de la Băile Felix au temperaturi constante de aproximativ 40°C tot timpul anului\n" +
               "• În apele termale de la Băile Felix trăiește nufărul termal și melcul Melanopsis parreyssi, specii rare protejate\n" +
               "• Vinurile de Miniș-Măderat sunt printre cele mai vechi și apreciate din România, cu o tradiție de peste 800 de ani\n" +
               "• În Crișana se află patru dintre cele 18 poduri acoperite din România, construite din lemn după tehnici tradiționale";
    }

    @Override
    protected String getRegionName() {
        return "Crișana";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            cityImages.add("oradea");
            cityImages.add("arad");
            cityImages.add("salonta");
            cityImages.add("beius");
            cityImages.add("baile_felix");
        }
        return cityImages;
    }

    private final String[] cityDescriptions = {
            "Oradea este cel mai mare oraș din Crișana și un important centru cultural și economic. " +
                    "Este renumit pentru arhitectura sa Art Nouveau, Cetatea Oradea, băile termale și pentru " +
                    "atmosfera sa cosmopolită.",

            "Arad este al doilea oraș ca mărime din Crișana, un important centru industrial și cultural. " +
                    "Cetatea Aradului, Teatrul Clasic Ioan Slavici și Palatul Cultural sunt doar câteva dintre " +
                    "atracțiile orașului.",

            "Salonta este un oraș cu bogate tradiții culturale, locul de naștere al poetului maghiar " +
                    "Arany János. Turnul Ciunt, simbol al orașului, datează din secolul al XVII-lea.",

            "Beiuș este un centru istoric și etnografic important din Țara Beiușului, cu un muzeu " +
                    "etnografic valoros și biserici istorice.",

            "Băile Felix este cea mai mare stațiune balneoclimaterică permanentă din România, " +
                    "renumită pentru apele termale cu proprietăți terapeutice și pentru nufărul termal unic."
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
        setContentView(R.layout.activity_crisana);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize viewModel
        viewModel = new CrisanaViewModel();

        // Initialize views - using correct IDs from the layout
        pointsText = findViewById(R.id.pointsText);
        storyButton = findViewById(R.id.buttonGoToCrisanaStory);
        gameButton = findViewById(R.id.buttonGoToCrisanaGame);
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
                    intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Crișana");
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
                    Intent intent = new Intent(this, CrisanaStoryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Crisana Story!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (gameButton != null) {
            gameButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 50) {
                    Intent intent = new Intent(this, CrisanaGameActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Crisana Game!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (mapButton != null) {
            mapButton.setOnClickListener(v -> startMapActivity());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePointsDisplay();
    }

    private void updatePointsDisplay() {
        if (pointsText != null) {
            int points = pointsManager.getPoints(this);
            pointsText.setText(String.valueOf(points));
        }
    }

    private String getCurrentUserId() {
        return "default_user"; // Replace with actual user ID if you have user management
    }

    private void loadCheckboxStates() {
        // Loading checkbox states from shared preferences
        for (int i = 1; i <= 5; i++) {
            String key = REGION + "_checkbox_" + i + "_" + getCurrentUserId();
            boolean isChecked = sharedPreferences.getBoolean(key, false);
            
            int checkboxId = getResources().getIdentifier("checkbox" + i, "id", getPackageName());
            CheckBox checkbox = findViewById(checkboxId);
            if (checkbox != null) {
                checkbox.setChecked(isChecked);
            }
        }
    }

    public void onCheckboxClicked(View view) {
        if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            boolean isChecked = checkBox.isChecked();
            
        String checkboxId = getResources().getResourceEntryName(view.getId());
        String checkboxNumber = checkboxId.replace("checkbox", "");
            String key = REGION.toLowerCase() + "_checkbox_" + checkboxNumber + "_" + getCurrentUserId();
        
            // Salvăm starea în SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, isChecked);
        editor.apply();
        
            // Adaugă puncte când este bifat
            if (isChecked) {
                pointsManager.addPoints(this, REGION.toLowerCase(), 10);
                // Actualizează și statusul landmark-ului
                pointsManager.updateLandmarkStatus(this, REGION.toLowerCase(), true);
            } else {
                // Actualizează statusul landmark-ului
                pointsManager.updateLandmarkStatus(this, REGION.toLowerCase(), false);
            }
            
            // Actualizează afișarea punctelor imediat
            updatePointsDisplay();
        }
    }

    public void showPopup1(View view) {
        showPopup("Oradea", cityDescriptions[0]);
        Intent intent = new Intent(this, ComposeEntryActivity.class);
        intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "oradea");
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Arad", cityDescriptions[1]);
        Intent intent = new Intent(this, ComposeEntryActivity.class);
        intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "arad");
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Salonta", cityDescriptions[2]);
        Intent intent = new Intent(this, ComposeEntryActivity.class);
        intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "salonta");
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Băile Felix", cityDescriptions[4]);
        Intent intent = new Intent(this, ComposeEntryActivity.class);
        intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "bailefelix");
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Peștera Urșilor", "Peștera Urșilor este una dintre cele mai spectaculoase peșteri din România, cu formațiuni carstice impresionante.");
        // This is a landmark, maybe not a city dashboard yet, but we'll use city dashboard if available or just the popup
    }

    private void showPopup(String title, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
               .setMessage(description)
               .setPositiveButton("Închide", null)
               .create()
               .show();
    }

    public void goBack(View view) {
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCheckboxStates();
    }

    private void saveCheckboxStates() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        for (int i = 1; i <= 5; i++) {
            int checkboxId = getResources().getIdentifier("checkbox" + i, "id", getPackageName());
            CheckBox checkbox = findViewById(checkboxId);
            
            if (checkbox != null) {
                String key = REGION + "_checkbox_" + i + "_" + getCurrentUserId();
                editor.putBoolean(key, checkbox.isChecked());
            }
        }
        
        editor.apply();
    }

    public void startMapActivity() {
        try {
            Intent intent = new Intent(this, CrisanaMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la deschiderea hărții: " + e.getMessage(), 
                           Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void handleLocationClick(int locationId) {
        switch (locationId) {
            case 1: // Oradea
                startActivity(CrisanaGameActivity.class);
                break;
            case 2: // Arad
                startActivity(CrisanaStoryActivity.class);
                break;
            case 3: // Salonta
                Toast.makeText(this, "Această funcționalitate nu este disponibilă momentan", Toast.LENGTH_SHORT).show();
                break;
            case 4: // Beius
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
