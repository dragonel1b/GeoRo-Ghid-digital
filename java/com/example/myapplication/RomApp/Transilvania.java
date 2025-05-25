package com.example.myapplication.RomApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.myapplication.Joc1.RomCityActivity;
import com.example.myapplication.R;
import com.example.myapplication.transilvaniausage.DraculaStoryActivity;
import com.example.myapplication.transilvaniausage.TransilvaniaGameActivity;
import com.example.myapplication.transilvaniausage.TransilvaniaMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.TransilvaniaViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import android.widget.LinearLayout;

public class Transilvania extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "transilvania";
    private MaterialButton casinoButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private TransilvaniaViewModel viewModel;

    @Override
    protected String getIntroductionText() {
        return "Transilvania este o regiune istorică situată în centrul României, înconjurată de Carpați. " +
               "Este renumită pentru peisajele montane spectaculoase, orașele medievale bine conservate, " +
               "tradițiile folclorice bogate și legendele asociate cu Dracula.";
    }

    protected String getHistoryText() {
        return "Transilvania are o istorie bogată ce se întinde pe mai multe milenii, fiind modelată de diverse popoare și civilizații care au stăpânit sau tranzitat regiunea.\n\n" +

               "Perioada antică:\n" +
               "- Locuită de triburi dacice care au creat o civilizație avansată în zonă\n" +
               "- Parte a regatului dac condus de Burebista și apoi Decebal\n" +
               "- Cucerită de romani în 106 d.Hr. și transformată în provincia Dacia\n" +
               "- Colonizată intens de romani, rezultând în procesul de romanizare\n\n" +

               "Evul Mediu:\n" +
               "- După retragerea aureliană (271-275 d.Hr.), regiunea a cunoscut migrația popoarelor\n" +
               "- Formarea primelor cnezate și voievodate românești medievale\n" +
               "- Integrarea în Regatul Ungariei începând cu secolul al XI-lea\n" +
               "- Înființarea Voievodatului Transilvaniei, având autonomie în cadrul regatului maghiar\n" +
               "- Colonizarea sașilor (germani) în secolul al XII-lea și a secuilor\n\n" +

               "Perioada modernă:\n" +
               "- Principat autonom sub suzeranitate otomană (secolul XVI-XVII)\n" +
               "- Integrarea în Imperiul Habsburgic (1699) și apoi Austro-Ungar (1867-1918)\n" +
               "- Unirea cu România la 1 Decembrie 1918 prin hotărârea Marii Adunări Naționale de la Alba Iulia\n" +
               "- Dezvoltarea economică, culturală și socială ca parte a României moderne";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
               "- Regiunea este delimitată natural de lanțul Carpaților\n" +
               "- Relief variat: munți (Carpații Orientali, Meridionali și Occidentali), Podișul Transilvaniei, văi fertile\n" +
               "- Rețea hidrografică bogată: râurile Mureș, Olt, Someș, Crișuri\n" +
               "- Climat temperat-continental cu ierni reci și veri plăcute\n" +
               "- Ecosisteme diverse, de la zone alpine la păduri de foioase și conifere\n\n" +

               "Geografie umană și economică:\n" +
               "- Activități economice principale: agricultura, silvicultura, industria, turismul\n" +
               "- Centre urbane importante: Cluj-Napoca, Brașov, Sibiu, Târgu Mureș, Alba Iulia\n" +
               "- Bogății ale subsolului: aur, argint, sare, gaz natural\n" +
               "- Agricultura tradițională în zonele rurale, păstrând metode ancestrale\n\n" +

               "Biodiversitate și zone protejate:\n" +
               "- Parcuri naționale: Parcul Național Retezat, Parcul Național Apuseni, Piatra Craiului\n" +
               "- Specii rare: ursul brun, lupul, râsul, acvila de munte\n" +
               "- Rezervații naturale și peisaje carstice spectaculoase\n" +
               "- Păduri seculare care adăpostesc una dintre cele mai bogate faune din Europa";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Transilvania este un tezaur de tradiții culturale diverse, fiind locuită de-a lungul istoriei de români, maghiari, sași (germani), romi și alte comunități etnice. " +
               "Această diversitate se reflectă în arhitectura variată, portul popular colorat, obiceiurile, festivalurile și gastronomia regiunii. " +
               "Fiecare grup etnic a contribuit cu propriile tradiții, creând un patrimoniu cultural unic în Europa.";
    }

    @Override
    protected String getAttractionsText() {
        return "Transilvania oferă o varietate de atracții turistice remarcabile, printre care se numără:\n\n" +
               "- **Castelul Bran**: adesea asociat cu legenda lui Dracula, este una dintre cele mai vizitate atracții din România, impresionând prin arhitectura sa medievală și poziția sa spectaculoasă pe stâncă.\n\n" +
               "- **Cetatea Sighișoara**: singura cetate medievală locuită din Europa de Est, parte a patrimoniului UNESCO, cu un centru istoric bine conservat și turnul cu ceas emblematic.\n\n" +
               "- **Cetatea Alba Carolina**: cea mai mare cetate de tip Vauban din România, situată în Alba Iulia, recent restaurată și transformată într-un complex cultural și turistic impresionant.\n\n" +
               "- **Biserica Neagră din Brașov**: cel mai mare edificiu gotic din sud-estul Europei, renumită pentru arhitectura sa impunătoare și colecția de covoare orientale.";
    }

    @Override
    protected String getGastronomyText() {
        return "Bucătăria transilvăneană reprezintă un amestec delicios de influențe românești, maghiare, săsești și rome, oferind o experiență culinară diversă și autentică. " +
               "Preparatele tradiționale includ: ciorbă de burtă, gulaș, sarmale, slănină afumată, varză à la Cluj, cozonac secuiesc (kürtőskalács), plăcintă cu brânză și mărar. " +
               "Produsele locale precum brânzeturile, carnea afumată și păinea de casă sunt esențiale în gastronomia regiunii, iar vinurile și pălinca (țuica) din Transilvania sunt apreciate pentru calitatea lor deosebită.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
               "- **Avram Iancu**: revoluționar și lider în timpul Revoluției de la 1848, supranumit \"Crăișorul Munților\", care a luptat pentru drepturile românilor din Transilvania.\n" +
               "- **Lucian Blaga**: unul dintre cei mai importanți poeți și filozofi români, născut în Lancrăm, ale cărui opere reflectă profund spiritualitatea transilvăneană.\n\n" +
               "Evenimente culturale:\n" +
               "- **Festivalul Internațional de Film Transilvania (TIFF)**: cel mai important festival de film din România, organizat anual la Cluj-Napoca, care atrage cineaști și spectatori din întreaga lume.\n" +
               "- **Sighișoara Medieval Festival**: eveniment anual care recreează atmosfera medievală în cetatea Sighișoara, cu cavaleri, muzică medievală, meșteșuguri tradiționale și spectacole de stradă.";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
               "- **Salina Turda** din Transilvania a fost transformată într-un spectaculos parc de distracții subteran, fiind considerată una dintre cele mai frumoase mine de sare din lume, conform revistei Business Insider?\n" +
               "- **Cluj-Napoca** este unul dintre cele mai importante centre IT din Europa Centrală și de Est, fiind supranumit \"Silicon Valley al României\"?";
    }

    @Override
    protected String getRegionName() {
        return "Transilvania";
    }


    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            cityImages.add("cluj");
            cityImages.add("brasov");
            cityImages.add("sibiu");
            cityImages.add("sighisoara");
            cityImages.add("alba_iulia");
        }
        return cityImages;
    }
    private final String[] cityDescriptions = {
            "Cluj-Napoca este considerat capitala neoficială a Transilvaniei și un important centru " +
            "cultural, academic și economic. Orașul combină farmecul arhitecturii medievale cu " +
            "dinamismul unui centru universitar modern și un sector IT în plină dezvoltare.",

            "Brașov este unul dintre cele mai pitorești orașe din Transilvania, înconjurat de Munții Carpați. " +
            "Centrul său istoric medieval, Biserica Neagră și proximitatea față de stațiunile montane " +
            "și Castelul Bran îl fac extrem de atractiv pentru turiști.",

            "Sibiu, fostă Capitală Culturală Europeană, impresionează prin arhitectura saxonă, " +
            "piețele sale medievale și evenimentele culturale internaționale. Centrul istoric " +
            "excelent conservat îi conferă un farmec aparte.",

            "Sighișoara este singura cetate medievală locuită din Europa de Est, inclusă în patrimoniul UNESCO. " +
            "Turnul cu Ceas, casele colorate și străzile pietruite păstrează atmosfera autentică a Evului Mediu.",

            "Alba Iulia este cunoscută pentru importanța sa istorică, fiind locul unde s-a realizat " +
            "Marea Unire din 1918. Cetatea Alba Carolina, recent restaurată, reprezintă cea mai mare " +
            "fortificație de tip Vauban din România."
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
        setContentView(R.layout.activity_transilvania);

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
        viewModel = new TransilvaniaViewModel();

        // Find views
        pointsText = findViewById(R.id.pointsText);
        casinoButton = findViewById(R.id.buttonGoToCasinoStory);
        gameButton = findViewById(R.id.buttonGoToTransilvaniaGame);
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
        
        // Adăugăm secțiunile specifice care nu sunt gestionate de clasa părinte
        // Aceste secțiuni vor fi adăugate în cityContentContainer
        addSection(findViewById(R.id.cityContentContainer), "Gastronomie", getGastronomyText(), false);
        addSection(findViewById(R.id.cityContentContainer), "Personalități și Evenimente", getPersonalitiesEventsText(), false);
        addSection(findViewById(R.id.cityContentContainer), "Curiozități", getCuriositiesText(), false);
        
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
            intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Transilvania");
            startActivity(intent);
        });

        // Casino button
        casinoButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 100) {
                Intent intent = new Intent(this, DraculaStoryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Casino Story!", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Transilvania Game button
        gameButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 50) {
                Intent intent = new Intent(this, TransilvaniaGameActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Transilvania Game!", 
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
            pointsManager.addPoints(this, "transilvania", gameScore);
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
        // Nu mai facem nimic deoarece checkbox-urile au fost eliminate din layout
        // Lăsăm metoda goală pentru a evita erorile
    }

    private void saveCheckboxStates() {
        // Nu mai facem nimic deoarece checkbox-urile au fost eliminate din layout
        // Lăsăm metoda goală pentru a evita erorile
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
        if (cityDescriptions.length > 1) {
            showPopup("Brașov", cityDescriptions[1]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Brașov");
            intent.putExtra("city_lat", 45.6427);
            intent.putExtra("city_lng", 25.5887);
            startActivity(intent);
        }
    }

    public void showPopup3(View view) {
        if (cityDescriptions.length > 2) {
            showPopup("Sibiu", cityDescriptions[2]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Sibiu");
            intent.putExtra("city_lat", 45.7983);
            intent.putExtra("city_lng", 24.1256);
            startActivity(intent);
        }
    }

    public void showPopup4(View view) {
        if (cityDescriptions.length > 3) {
            showPopup("Sighișoara", cityDescriptions[3]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Sighișoara");
            intent.putExtra("city_lat", 46.2198);
            intent.putExtra("city_lng", 24.7965);
            startActivity(intent);
        }
    }

    public void showPopup5(View view) {
        if (cityDescriptions.length > 4) {
            showPopup("Alba Iulia", cityDescriptions[4]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Alba Iulia");
            intent.putExtra("city_lat", 46.0685);
            intent.putExtra("city_lng", 23.5709);
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
            Intent intent = new Intent(this, TransilvaniaMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Nu s-a putut deschide harta Transilvaniei: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            
            // Log error details for debugging
            android.util.Log.e("Transilvania", errorMessage);
            
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
            case 1: // Cluj-Napoca
                startActivity(TransilvaniaGameActivity.class);
                break;
            case 2: // Brașov
                startActivity(DraculaStoryActivity.class);
                break;
            case 3: // Sibiu
                Intent intent = new Intent(this, RomCityActivity.class);
                intent.putExtra("CITY_NAME", "Sibiu");
                intent.putExtra("city_lat", 45.7983);
                intent.putExtra("city_lng", 24.1256);
                startActivity(intent);
                break;
            case 4: // Sighișoara
                Intent intent2 = new Intent(this, RomCityActivity.class);
                intent2.putExtra("CITY_NAME", "Sighișoara");
                intent2.putExtra("city_lat", 46.2198);
                intent2.putExtra("city_lng", 24.7965);
                startActivity(intent2);
                break;
            case 5: // Alba Iulia
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
