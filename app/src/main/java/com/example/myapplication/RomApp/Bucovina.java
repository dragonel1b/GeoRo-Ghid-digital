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
import com.example.myapplication.bucovinausage.BucovinaStoryActivity;
import com.example.myapplication.bucovinausage.BucovinaGameActivity;
import com.example.myapplication.bucovinausage.BucovinaMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.BucovinaViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import android.widget.LinearLayout;

public class Bucovina extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "bucovina";
    private MaterialButton casinoButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private BucovinaViewModel viewModel;

    @Override
    protected String getIntroductionText() {
        return "Bucovina este o regiune istorică situată în nordul României, cunoscută pentru mănăstirile sale pictate, " +
                "peisajele naturale spectaculoase, și tradițiile populare bine conservate. " +
                "Regiunea are o istorie bogată și o cultură diversă, influențată de vecinătatea cu Ucraina.";
    }

    protected String getHistoryText() {
        return "Bucovina are o istorie complexă, fiind o regiune cu o importanță strategică deosebită de-a lungul timpului.\n\n" +

                "Perioada medievală:\n" +
                "- Parte integrantă a Moldovei medievale începând cu secolul XIV\n" +
                "- Importantă pentru dezvoltarea culturală și spirituală sub domnia lui Ștefan cel Mare\n" +
                "- Locul construirii unor mănăstiri celebre, unele incluse astăzi în patrimoniul UNESCO\n\n" +

                "Perioada modernă:\n" +
                "- Anexată de Imperiul Habsburgic în 1775\n" +
                "- Sub administrație austriacă până în 1918\n" +
                "- Dezvoltarea economică și modernizarea sub administrația austriacă\n" +
                "- Unirea cu România la sfârșitul Primului Război Mondial (1918)\n" +
                "- Divizarea regiunii după Al Doilea Război Mondial între România și Ucraina";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
                "- Situată în partea de nord a României, cu o parte în Ucraina\n" +
                "- Relief variat: munți (Carpații Orientali), dealuri, podișuri și văi\n" +
                "- Rețea hidrografică bogată: râurile Suceava, Moldova, Siret\n" +
                "- Climat temperat-continental cu ierni reci și veri moderate\n" +
                "- Păduri extinse care acoperă o parte semnificativă a regiunii\n\n" +

                "Geografie umană și economică:\n" +
                "- Activități economice principale: turism, agricultură, silvicultură, industria lemnului\n" +
                "- Centre urbane importante: Suceava, Câmpulung Moldovenesc, Rădăuți, Gura Humorului\n" +
                "- Zone rurale cu sate tradiționale bine conservate\n\n" +

                "Biodiversitate și zone protejate:\n" +
                "- Parcul Național Călimani\n" +
                "- Rezervații naturale cu specii rare de plante și animale\n" +
                "- Peisaje naturale deosebite: chei, cascade, masive muntoase";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Bucovina este un tezaur cultural, celebră pentru:\n\n" +
                "- Mănăstirile pictate, unele incluse în patrimoniul UNESCO, precum Voroneț, Humor, Moldovița, Sucevița\n" +
                "- Arta încondeierii ouălor, o tradiție unică și elaborată\n" +
                "- Meșteșuguri tradiționale: olărit, țesut, prelucrarea lemnului\n" +
                "- Costume populare deosebit de elaborate și colorate\n" +
                "- Obiceiuri și datini ancestrale, păstrate cu sfințenie până în prezent";
    }

    @Override
    protected String getAttractionsText() {
        return "Bucovina oferă o varietate de atracții turistice remarcabile, printre care se numără:\n\n" +
                "- **Mănăstirile pictate**: Voroneț (cunoscută pentru \"albastrul de Voroneț\"), Humor, Moldovița, Sucevița și Arbore, toate parte din patrimoniul UNESCO\n\n" +
                "- **Cetatea de Scaun a Sucevei**: fostă reședință a domnitorilor Moldovei, o impunătoare fortificație medievală\n\n" +
                "- **Muzeul Satului Bucovinean**: un muzeu în aer liber care prezintă arhitectura tradițională și modul de viață rural\n\n" +
                "- **Munții Rarău și Stânca Pietrele Doamnei**: formațiuni stâncoase spectaculoase și trasee montane pitorești";
    }

    @Override
    protected String getGastronomyText() {
        return "Bucătăria din Bucovina este renumită pentru autenticitatea și savoarea preparatelor tradiționale:\n\n" +
                "- **Tocana bucovineană**: un fel de mâncare consistent cu carne și legume\n" +
                "- **Sarmale în foi de varză sau viță**: preparate după rețete tradiționale\n" +
                "- **Ciorba rădăuțeană**: o specialitate locală pe bază de smântână și carne de pasăre\n" +
                "- **Plăcintele poale-n brâu**: desert tradițional umplut cu brânză dulce sau brânză sărată cu mărar\n" +
                "- **Afinata și Vișinata**: băuturi tradiționale preparate din fructe de pădure";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
                "- **Ciprian Porumbescu**: compozitor și violonist, autor al celebrei \"Balade\"\n" +
                "- **Mihai Eminescu**: poetul național al României, care a petrecut o parte din viață în această regiune\n\n" +
                "Evenimente culturale:\n" +
                "- **Festivalul de Artă Medievală de la Suceava**: reconstituie atmosfera medievală cu cavaleri, meșteșugari și muzică de epocă\n" +
                "- **Festivalul Ouălor Încondeiate**: celebrează arta tradițională a încondeierii ouălor";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
                "- **Albastrul de Voroneț** este o nuanță unică de albastru folosită la Mănăstirea Voroneț, a cărei compoziție exactă rămâne un mister și încă rezistă după secole?\n" +
                "- **Bucovina** a fost numită astfel de către austrieci, numele însemnând \"Țara fagilor\" (Buchenland în germană), datorită pădurilor extinse de fag din regiune?";
    }

    @Override
    protected String getRegionName() {
        return "Bucovina";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            cityImages.add("suceava");
            cityImages.add("gura_humorului");
            cityImages.add("radauti");
            cityImages.add("campulung");
            cityImages.add("vatra_dornei");
        }
        return cityImages;
    }

    private final String[] cityDescriptions = {
            "Suceava este fostă capitală a Moldovei și centrul administrativ al Bucovinei. " +
                    "Orașul impresionează prin Cetatea de Scaun, bisericile medievale și atmosfera istorică. " +
                    "Este un important centru cultural și poartă de intrare spre mănăstirile pictate din regiune.",

            "Gura Humorului este un orășel pitoresc, situat într-o zonă de o frumusețe naturală deosebită. " +
                    "Reprezintă un important punct de plecare spre mănăstirile Humor și Voroneț și spre " +
                    "atracțiile naturale din împrejurimi.",

            "Rădăuți este unul dintre cele mai vechi orașe din Moldova, cu o bogată tradiție culturală. " +
                    "Este cunoscut pentru hergheliile de cai, meșteșugurile tradiționale și gastronomia locală, " +
                    "în special pentru celebra \"ciorbă rădăuțeană\".",

            "Câmpulung Moldovenesc, situat în inima Bucovinei, este înconjurat de munți și păduri impresionante. " +
                    "Orașul găzduiește Muzeul Lemnului și este cunoscut pentru tradițiile forestiere și peisajele " +
                    "naturale spectaculoase din împrejurimi.",

            "Vatra Dornei este o renumită stațiune balneoclimaterică, situată într-o depresiune pitorească. " +
                    "Este apreciată pentru apele minerale terapeutice, pârtiile de schi, aerul curat și " +
                    "posibilitățile de tratament și recreere în toate anotimpurile."
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
        setContentView(R.layout.activity_bucovina);

        // Initialize components
        initializeComponents();
        
        // Set up image carousel
        setupImageCarousel();
        
        // Initialize content sections
        initializeSections();
        
        // Initialize city content
        initializeSpecificContent();
        
        // Update points display
        updatePointsDisplay();
        
        // Load saved states
        loadCheckboxStates();
    }

    private void initializeComponents() {
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize viewModel
        viewModel = new BucovinaViewModel();

        // Find views - updated IDs based on new layout
        pointsText = findViewById(R.id.pointsText);
        casinoButton = findViewById(R.id.buttonGoToCasinoStory);
        gameButton = findViewById(R.id.buttonGoToDobrogeaGame);
        citiesButton = findViewById(R.id.buttonGoToCities);
        mapButton = findViewById(R.id.buttonGoToMap);
        
        // Back button is handled via onClick attribute in XML
        
        // Set up navigation buttons
        setupNavigationButtons();
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

    private void initializeSections() {
        // Nu mai facem nimic deoarece sectionsContainer a fost eliminat din layout
        // Toate secțiunile sunt acum gestionate de clasa părinte RegionTemplate
    }

    @Override
    protected void initializeSpecificContent() {
        // Ne bazăm pe implementarea clasei părinte pentru afișarea conținutului
        super.initializeSpecificContent();
        
        // Toate secțiunile sunt deja adăugate de clasa părinte RegionTemplate
        // Nu mai avem nevoie să adăugăm secțiuni duplicate aici
        
        // Adăugăm listeneri pentru deschiderea activității de recenzie
        addSectionReviewListeners();
    }

    private void addSectionReviewListeners() {
        LinearLayout container = findViewById(R.id.cityContentContainer);
        if (container == null) return;

        // Parcurgem toate vederile din container
        for (int i = 0; i < container.getChildCount(); i++) {
            View childView = container.getChildAt(i);
            if (childView != null) {
                // Verificăm dacă avem un titlu și conținut în această secțiune
                TextView titleView = childView.findViewById(R.id.sectionTitle);
                TextView contentView = childView.findViewById(R.id.sectionContent);

                if (titleView != null && contentView != null) {
                    final String title = titleView.getText().toString();
                    final String content = contentView.getText().toString();

                    // Adăugăm listener pentru click pe secțiune pentru a deschide SectionPreviewActivity
                    childView.setOnClickListener(v -> {
                        try {
                            Intent intent = new Intent(this, com.example.myapplication.viewmodel.SectionPreviewActivity.class);
                            intent.putExtra(com.example.myapplication.viewmodel.SectionPreviewActivity.EXTRA_TITLE, title);
                            intent.putExtra(com.example.myapplication.viewmodel.SectionPreviewActivity.EXTRA_CONTENT, content);
                            startActivity(intent);
                            // Aplicăm animație la tranziție
                            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
                        } catch (Exception e) {
                            // Handle exception in case SectionPreviewActivity doesn't exist
                            Toast.makeText(Bucovina.this, "Nu se poate deschide secțiunea: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Adăugăm un efect de hover pentru a indica că secțiunea este clickabilă
                    childView.setOnTouchListener((v, event) -> {
                        switch (event.getAction()) {
                            case android.view.MotionEvent.ACTION_DOWN:
                                v.setAlpha(0.8f);
                                v.setScaleX(0.98f);
                                v.setScaleY(0.98f);
                                break;
                            case android.view.MotionEvent.ACTION_UP:
                            case android.view.MotionEvent.ACTION_CANCEL:
                                v.setAlpha(1.0f);
                                v.setScaleX(1.0f);
                                v.setScaleY(1.0f);
                                break;
                        }
                        return false;
                    });
                }
            }
        }
    }

    private void setupNavigationButtons() {
        // Cities button
        if (citiesButton != null) {
            citiesButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, CityListActivity.class);
                intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Bucovina");
                startActivity(intent);
            });
        }

        // Map button
        if (mapButton != null) {
            mapButton.setOnClickListener(v -> startMapActivity());
        }

        // Casino button
        if (casinoButton != null) {
            casinoButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 100) {
                    Intent intent = new Intent(this, BucovinaStoryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Casino Story!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bucovina Game button
        if (gameButton != null) {
            gameButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 50) {
                    Intent intent = new Intent(this, BucovinaGameActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Bucovina Game!",
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
            pointsManager.addPoints(this, "bucovina", gameScore);
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
        // In a real app, this would get the current logged-in user
        return "default_user";
    }

    private void loadCheckboxStates() {
        // Nu mai facem nimic deoarece checkbox-urile au fost eliminate din layout
        // Lăsăm metoda goală pentru a evita erorile
    }

    public void onCheckboxClicked(View view) {
        if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            boolean isChecked = checkBox.isChecked();
            
            // Adaugă puncte când este bifat
            if (isChecked) {
                pointsManager.addPoints(this, REGION.toLowerCase(), 10);
            }
            
            pointsManager.updateLandmarkStatus(this, REGION.toLowerCase(), isChecked);

            // Salvăm starea checkbox-ului
            String userId = getCurrentUserId();
            String checkBoxId = getResources().getResourceEntryName(view.getId());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(userId + "_" + checkBoxId + "_" + REGION, isChecked);
            editor.apply();
            
            // Actualizăm afișarea punctelor imediat
            updatePointsDisplay();
        }
    }

    private void saveCheckboxStates() {
        // Nu mai facem nimic deoarece checkbox-urile au fost eliminate din layout
        // Lăsăm metoda goală pentru a evita erorile
    }

    public void showPopup1(View view) {
        showPopup("Suceava", cityDescriptions[0]);
        Intent intent = new Intent(this, ComposeEntryActivity.class);
        intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "suceava");
        intent.putExtra("city_lat", 47.6635);
        intent.putExtra("city_lng", 26.2732);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        if (cityDescriptions.length > 1) {
            showPopup("Gura Humorului", cityDescriptions[1]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "gurahumorului");
            intent.putExtra("city_lat", 47.5533);
            intent.putExtra("city_lng", 25.8902);
            startActivity(intent);
        }
    }

    public void showPopup3(View view) {
        if (cityDescriptions.length > 2) {
            showPopup("Rădăuți", cityDescriptions[2]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "radauti");
            intent.putExtra("city_lat", 47.8428);
            intent.putExtra("city_lng", 25.9209);
            startActivity(intent);
        }
    }

    public void showPopup4(View view) {
        if (cityDescriptions.length > 3) {
            showPopup("Câmpulung Moldovenesc", cityDescriptions[3]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "campulungmoldovenesc");
            intent.putExtra("city_lat", 47.5285);
            intent.putExtra("city_lng", 25.5649);
            startActivity(intent);
        }
    }

    public void showPopup5(View view) {
        if (cityDescriptions.length > 4) {
            showPopup("Vatra Dornei", cityDescriptions[4]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "vatradornei");
            intent.putExtra("city_lat", 47.3526);
            intent.putExtra("city_lng", 25.3597);
            startActivity(intent);
        }
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

    public void startMapActivity() {
        try {
            Intent intent = new Intent(this, BucovinaMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Nu s-a putut deschide harta Bucovinei: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

            // Log error details for debugging
            android.util.Log.e("Bucovina", errorMessage);

            // Show detailed error dialog for developers
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Eroare hartă interactivă")
                    .setMessage("Detalii eroare:\n" + e.toString() + "\n\nCauza: " + (e.getCause() != null ? e.getCause().toString() : "Necunoscută"))
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    public void handleLocationClick(int locationId) {
        switch (locationId) {
            case 1: // Suceava
                Intent intentSuceava = new Intent(this, ComposeEntryActivity.class);
                intentSuceava.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "suceava");
                intentSuceava.putExtra("city_lat", 47.6635);
                intentSuceava.putExtra("city_lng", 26.2732);
                startActivity(intentSuceava);
                break;
            case 2: // Gura Humorului
                Intent intentGura = new Intent(this, ComposeEntryActivity.class);
                intentGura.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "gurahumorului");
                intentGura.putExtra("city_lat", 47.5533);
                intentGura.putExtra("city_lng", 25.8902);
                startActivity(intentGura);
                break;
            case 3: // Rădăuți
                Intent intentRadauti = new Intent(this, ComposeEntryActivity.class);
                intentRadauti.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "radauti");
                intentRadauti.putExtra("city_lat", 47.8428);
                intentRadauti.putExtra("city_lng", 25.9209);
                startActivity(intentRadauti);
                break;
            case 4: // Câmpulung Moldovenesc
                Intent intentCampulung = new Intent(this, ComposeEntryActivity.class);
                intentCampulung.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "campulungmoldovenesc");
                intentCampulung.putExtra("city_lat", 47.5285);
                intentCampulung.putExtra("city_lng", 25.5649);
                startActivity(intentCampulung);
                break;
            case 5: // Vatra Dornei
                Intent intentVatra = new Intent(this, ComposeEntryActivity.class);
                intentVatra.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "vatradornei");
                intentVatra.putExtra("city_lat", 47.3526);
                intentVatra.putExtra("city_lng", 25.3597);
                startActivity(intentVatra);
                break;
            case 6: // Bucovina Game
                startActivity(BucovinaGameActivity.class);
                break;
            case 7: // Bucovina Story
                startActivity(BucovinaStoryActivity.class);
                break;
            default:
                Toast.makeText(this, "Această locație nu este disponibilă momentan", Toast.LENGTH_SHORT).show();
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
