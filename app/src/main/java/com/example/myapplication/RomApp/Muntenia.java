package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.ui.ComposeEntryActivity;
import com.example.myapplication.R;
import com.example.myapplication.munteniausage.MunteniaGameActivity;
import com.example.myapplication.munteniausage.MunteniaTourActivity;
import com.example.myapplication.munteniausage.MunteniaMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.MunteniaViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import android.widget.LinearLayout;
import java.util.Timer;

public class Muntenia extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "muntenia";
    private MaterialButton casinoButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private MunteniaViewModel viewModel;
    private Timer timer;

    @Override
    protected String getIntroductionText() {
        return "Muntenia, cunoscută și sub numele de Țara Românească, este o regiune istorică situată în sudul României. " +
                "Cuprinde câmpii fertile, dealuri line și este străbătută de râuri importante precum Argeș, Ialomița și Dâmbovița. " +
                "Regiunea include București, capitala țării, și este bogată în istorie, cultură și tradiții autohtone.";
    }

    protected String getHistoryText() {
        return "Muntenia are o istorie bogată ce se întinde pe mai multe milenii, fiind una dintre regiunile care au stat la baza formării statului român.\n\n" +

                "Perioada antică:\n" +
                "- Locuită de triburi geto-dacice în antichitate\n" +
                "- Parte a provinciei romane Moesia Inferior după cucerirea romană\n" +
                "- Zona a cunoscut influența civilizației romane și bizantine\n\n" +

                "Evul Mediu:\n" +
                "- Formarea primelor formațiuni politice românești în secolul al XIII-lea\n" +
                "- Întemeierea Țării Românești de către Basarab I în secolul al XIV-lea\n" +
                "- Perioada de glorie sub domniile lui Mircea cel Bătrân și Vlad Țepeș\n" +
                "- Rezistența în fața expansiunii Imperiului Otoman\n\n" +

                "Perioada modernă:\n" +
                "- Dezvoltarea economică și culturală sub domniile fanariote\n" +
                "- Revoluția de la 1848 condusă de Nicolae Bălcescu\n" +
                "- Unirea Principatelor Române din 1859 sub Alexandru Ioan Cuza\n" +
                "- Obținerea independenței în 1877 și formarea României moderne";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
                "- Relief variat: predomină Câmpia Română, cu dealuri spre nord (Subcarpații Munteniei)\n" +
                "- Traversată de râuri importante: Olt, Argeș, Ialomița, Dâmbovița\n" +
                "- Fluviul Dunărea formează granița sudică a regiunii\n" +
                "- Climat temperat-continental cu veri calde și ierni reci\n" +
                "- Sol fertil favorabil agriculturii, în special în Câmpia Română\n\n" +

                "Geografie umană și economică:\n" +
                "- Capitala București domină regiunea ca principal centru economic și cultural\n" +
                "- Alte orașe importante: Ploiești, Târgoviște, Pitești, Buzău\n" +
                "- Economia bazată pe agricultură, industria petrolieră, producție industrială\n" +
                "- Zonă Ploiești - important centru al industriei petroliere din România\n\n" +

                "Biodiversitate și zone protejate:\n" +
                "- Parcul Natural Comana, una dintre cele mai importante zone umede din sudul țării\n" +
                "- Rezervația naturală Lacul Snagov și pădurile înconjurătoare\n" +
                "- Delta Neajlovului, habitat pentru numeroase specii protejate\n" +
                "- Ecosisteme diverse, de la lunci de râuri la păduri de stejar";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Muntenia este un tezaur de tradiții culturale românești, cu o bogată moștenire folclorică. " +
                "Portul popular muntenesc se distinge prin cămăși albe brodate, fote și catrințe cu modele geometrice viu colorate. " +
                "Dansurile tradiționale precum Hora, Sârba și Călușul sunt emblematice pentru această regiune. " +
                "Meșteșugurile tradiționale includ olăritul, țesutul, prelucrarea lemnului și confecționarea măștilor populare.";
    }

    @Override
    protected String getAttractionsText() {
        return "Muntenia oferă o varietate de atracții turistice remarcabile, printre care se numără:\n\n" +
                "- **București**: capitala României, impresionează prin amestecul unic de arhitectură veche și modernă, muzee de renume și viață culturală intensă.\n\n" +
                "- **Palatul Parlamentului**: a doua cea mai mare clădire administrativă din lume, un monument impresionant al arhitecturii și istoriei recente.\n\n" +
                "- **Castelul Peleș din Sinaia**: una dintre cele mai frumoase reședințe regale din Europa, cu o arhitectură neo-renascentistă spectaculoasă și colecții de artă valoroase.\n\n" +
                "- **Curtea Domnească din Târgoviște**: fostă capitală a Țării Românești, cu un important ansamblu de monumente medievale, inclusiv Turnul Chindiei.";
    }

    @Override
    protected String getGastronomyText() {
        return "Bucătăria din Muntenia se distinge prin preparate tradiționale savuroase, influențate de moștenirea rurală și urbană. " +
                "Preparatele emblematice includ: ciorbă de potroace, tochitură muntenească, musaca de cartofi, mămăligă cu brânză și smântână, cozonac. " +
                "Vinurile din podgoriile Dealu Mare sunt recunoscute pentru calitatea lor, iar țuica de prune este băutura tradițională a regiunii. " +
                "În București se regăsește o bucătărie rafinată, cu influențe balcanice și occidentale, reflectând statutul de capitală cosmopolită.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
                "- **Nicolae Grigorescu**: unul dintre cei mai importanți pictori români, născut la Pitaru, Dâmbovița, cunoscut pentru peisajele și scenele rurale românești.\n" +
                "- **Ion Luca Caragiale**: dramaturg și scriitor satiric, născut la Haimanale (astăzi I.L. Caragiale), Dâmbovița, a creat opere care surprind esența societății românești.\n\n" +
                "Evenimente culturale:\n" +
                "- **Festivalul George Enescu**: unul dintre cele mai prestigioase festivaluri de muzică clasică din Europa, organizat în București.\n" +
                "- **Festivalul Medieval de la Târgoviște**: eveniment anual care recreează atmosfera medievală a fostei capitale, cu turniruri, meșteșuguri tradiționale și spectacole istorice.";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
                "- **Palatul Parlamentului** din București deține mai multe recorduri mondiale, fiind a doua cea mai mare clădire administrativă din lume după Pentagon și cea mai grea clădire din lume?\n" +
                "- **Calea Victoriei** din București este una dintre cele mai vechi artere ale capitalei, fiind construită în 1692 la ordinul domnitorului Constantin Brâncoveanu?";
    }

    @Override
    protected String getRegionName() {
        return "Muntenia";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            cityImages.add("bucuresti");
            cityImages.add("ploiesti");
            cityImages.add("targoviste");
            cityImages.add("sinaia");
            cityImages.add("curtea_de_arges");
        }
        return cityImages;
    }

    private final String[] cityDescriptions = {
            "București este capitala României și cel mai important centru urban al Munteniei, " +
                    "combinând arhitectura de la sfârșitul secolului XIX cu edificii moderne. " +
                    "Oferă o viață culturală intensă, parcuri impresionante și o gamă variată de atracții turistice și culinare.",

            "Ploiești este cunoscut ca centrul industriei petroliere românești, " +
                    "cu o istorie bogată legată de exploatarea și rafinarea petrolului. " +
                    "Orașul oferă muzee interesante, inclusiv unicul Muzeu Național al Petrolului din România.",

            "Târgoviște, fosta capitală a Țării Românești, impresionează prin Complexul Muzeal " +
                    "și emblematicul Turn al Chindiei. Aici se poate descoperi istoria medievală a Munteniei " +
                    "și importanța orașului în formarea statului român.",

            "Sinaia, cunoscută ca Perla Carpaților, este o stațiune montană elegantă " +
                    "ce găzduiește Castelul Peleș, fosta reședință de vară a familiei regale române. " +
                    "Arhitectura specifică și împrejurimile montane oferă un farmec aparte acestei localități.",

            "Curtea de Argeș este un important centru istoric și religios, " +
                    "faimos pentru Mănăstirea Curtea de Argeș, o capodoperă arhitecturală " +
                    "și necropolă regală. Legenda Meșterului Manole este strâns legată de construcția acestui edificiu."
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
        setContentView(R.layout.activity_muntenia);

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
        sharedPreferences = getSharedPreferences("MunteniaPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize viewModel
        viewModel = new MunteniaViewModel(this, REGION);

        // Find views
        pointsText = findViewById(R.id.pointsText);
        casinoButton = findViewById(R.id.buttonGoToCasinoStory);
        gameButton = findViewById(R.id.buttonGoToDobrogeaGame);
        citiesButton = findViewById(R.id.buttonGoToCities);
        mapButton = findViewById(R.id.buttonGoToMap);

        // Set up map button listener
        if (mapButton != null) {
            mapButton.setOnClickListener(v -> startMapActivity());
        } else {
            Toast.makeText(this, "Eroare: Butonul pentru hartă nu a fost găsit.", Toast.LENGTH_SHORT).show();
        }
        
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
                        Intent intent = new Intent(this, com.example.myapplication.viewmodel.SectionPreviewActivity.class);
                        intent.putExtra(com.example.myapplication.viewmodel.SectionPreviewActivity.EXTRA_TITLE, title);
                        intent.putExtra(com.example.myapplication.viewmodel.SectionPreviewActivity.EXTRA_CONTENT, content);
                        startActivity(intent);
                        // Aplicăm animație la tranziție
                        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
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
        citiesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CityListActivity.class);
            intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Muntenia");
            startActivity(intent);
        });

        // Muntenia Story button
        casinoButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 100) {
                Intent intent = new Intent(this, MunteniaTourActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Muntenia Story!", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Muntenia Game button
        gameButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 50) {
                Intent intent = new Intent(this, MunteniaGameActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Muntenia Game!", 
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
            pointsManager.addPoints(this, REGION, gameScore);
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
        // In a real app, this would get the current logged-in user
        return "default_user";
    }

    private void loadCheckboxStates() {
        // Nu mai facem nimic deoarece checkbox-urile au fost eliminate din layout
        // Lăsăm metoda goală pentru a evita erorile
    }

    public void onCheckboxClicked(View view) {
        // Nu mai facem nimic deoarece checkbox-urile au fost eliminate din layout
        // Lăsăm metoda goală pentru a evita erorile
    }

    private void saveCheckboxStates() {
        // Nu mai facem nimic deoarece checkbox-urile au fost eliminate din layout
        // Lăsăm metoda goală pentru a evita erorile
    }

    public void showPopup1(View view) {
        showPopup("București", cityDescriptions[0]);
        Intent intent = new Intent(this, ComposeEntryActivity.class);
        intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "bucuresti");
        startActivity(intent);
    }

    public void showPopup2(View view) {
        if (cityDescriptions.length > 1) {
            showPopup("Ploiești", cityDescriptions[1]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "ploiesti");
            startActivity(intent);
        }
    }

    public void showPopup3(View view) {
        if (cityDescriptions.length > 2) {
            showPopup("Târgoviște", cityDescriptions[2]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "targoviste");
            startActivity(intent);
        }
    }

    public void showPopup4(View view) {
        if (cityDescriptions.length > 3) {
            showPopup("Sinaia", cityDescriptions[3]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "sinaia");
            startActivity(intent);
        }
    }

    public void showPopup5(View view) {
        if (cityDescriptions.length > 4) {
            showPopup("Curtea de Argeș", cityDescriptions[4]);
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "curteadearges");
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
            Intent intent = new Intent(this, MunteniaMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Nu s-a putut deschide harta Munteniei: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            
            // Log error details for debugging
            android.util.Log.e("Muntenia", errorMessage);
            
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
            case 1: // București
                startActivity(MunteniaGameActivity.class);
                break;
            case 2: // Ploiești
                startActivity(MunteniaTourActivity.class);
                break;
            case 3: // Târgoviște
                Intent intent = new Intent(this, ComposeEntryActivity.class);
                intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "targoviste");
                startActivity(intent);
                break;
            case 4: // Sinaia
                if (pointsManager.getPoints(this) >= 20) {
                    Intent intent2 = new Intent(this, ComposeEntryActivity.class);
                    intent2.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, "sinaia");
                    startActivity(intent2);
                }
                break;
            case 5: // Curtea de Argeș
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

    @Override
    public void onBackPressed() {
        // Afișăm dialogul de confirmare
        showExitConfirmation();
    }

    private void showExitConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ieșire");
        builder.setMessage("Ești sigur că vrei să părăsești această regiune?");
        builder.setPositiveButton("Da", (dialog, which) -> {
            if (timer != null) {
                timer.cancel();
            }
            finish();
        });
        builder.setNegativeButton("Nu", null);
        builder.show();
    }
}
