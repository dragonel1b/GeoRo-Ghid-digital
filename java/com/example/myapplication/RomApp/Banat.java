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
import com.example.myapplication.banatusage.BanatStoryActivity;
import com.example.myapplication.banatusage.BanatGameActivity;
import com.example.myapplication.banatusage.BanatMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.BanatViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import android.widget.LinearLayout;
import com.google.android.material.appbar.MaterialToolbar;

public class Banat extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "banat";
    private MaterialButton casinoButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private BanatViewModel viewModel;

    @Override
    protected String getRegionName() {
        return "Banat";
    }

    @Override
    protected String getCityName() {
        return "Regiunea Banat";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("timisoara");
        images.add("resita");
        images.add("lugoj");
        images.add("caransebes");
        images.add("herculane");
        return images;
    }



    protected String getIntroductionText() {
        return "Banatul este o regiune istorică situată în sud-vestul României, învecinată cu Serbia și Ungaria. " +
               "Este renumită pentru diversitatea culturală, tradițiile bogate, arhitectura specifică și pentru peisajele naturale spectaculoase, incluzând Munții Banatului și Dunăre.";
    }

    protected String getHistoryText() {
        return "Banatul are o istorie complexă, marcată de influențe multiple și perioade de dezvoltare distincte:\n\n" +

               "Perioada antică:\n" +
               "- A fost locuit de triburi dacice și a făcut parte din regatul lui Burebista și Decebal\n" +
               "- A fost cucerit de romani și integrat în provincia Dacia\n" +
               "- A cunoscut procesul de romanizare intensă\n\n" +

               "Evul Mediu:\n" +
               "- După retragerea aureliană, regiunea a fost afectată de migrația popoarelor\n" +
               "- A fost integrată în Regatul Ungariei în secolul XI\n" +
               "- A reprezentat un important spațiu de frontieră în fața Imperiului Otoman\n\n" +

               "Perioada modernă:\n" +
               "- A fost eliberat de sub dominația otomană după Pacea de la Passarowitz (1718)\n" +
               "- A devenit parte a Imperiului Habsburgic sub numele de Banatul Timișoarei\n" +
               "- A cunoscut o colonizare intensă cu populații germane, sârbe, maghiare și de alte etnii\n" +
               "- După Primul Război Mondial, a fost împărțit între România, Serbia și Ungaria\n" +
               "- Partea românească a Banatului s-a unit cu România la 1 Decembrie 1918";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
               "- Relief variat: zona montană (Munții Banatului), dealuri (Dealurile Lipovei), câmpie (Câmpia Banatului)\n" +
               "- Rețea hidrografică bogată: fluviul Dunărea, râurile Timiș, Bega, Caraș, Nera\n" +
               "- Climat temperat-continental cu influențe mediteraneene\n" +
               "- Resurse naturale diverse: cărbune, minereuri, ape termale\n\n" +

               "Geografie umană și economică:\n" +
               "- Centre urbane importante: Timișoara, Reșița, Lugoj, Caransebeș\n" +
               "- Economia diversificată: industrie, agricultură, turism\n" +
               "- Infrastructură bine dezvoltată, fiind o regiune de tranzit între Europa Centrală și Peninsula Balcanică\n\n" +

               "Biodiversitate și zone protejate:\n" +
               "- Parcul Național Cheile Nerei-Beușnița\n" +
               "- Parcul Natural Porțile de Fier\n" +
               "- Rezervații naturale importante\n" +
               "- Biodiversitate bogată, cu specii endemice";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Banatul se remarcă printr-un mozaic cultural unic, fiind locuită istoric de români, germani (șvabi), sârbi, maghiari, bulgari și alte etnii. " +
               "Această diversitate a generat un patrimoniu cultural specific, cu influențe multiple, vizibile în arhitectură, port popular, gastronomie, obiceiuri și sărbători tradiționale. " +
               "Multiculturalismul bănățean este considerat un model de conviețuire armonioasă între diferite grupuri etnice.";
    }

    @Override
    protected String getAttractionsText() {
        return "Banatul oferă o diversitate de atracții turistice, printre care:\n\n" +
               "- **Timișoara**: Capitala Banatului, Capitală Culturală Europeană 2023, cunoscută pentru arhitectura sa eclectică, spațiile verzi și efervescența culturală\n\n" +
               "- **Rezervația Cheile Nerei-Beușnița**: Un paradis natural cu cascade spectaculoase, lacuri turcoaz și peisaje impresionante\n\n" +
               "- **Băile Herculane**: Una dintre cele mai vechi stațiuni balneare din Europa, cu izvoare termale cunoscute încă din perioada romană\n\n" +
               "- **Parcul Natural Porțile de Fier**: Spectaculosul defileu al Dunării, cu peisaje unice și vestigii istorice importante";
    }

    @Override
    protected String getGastronomyText() {
        return "Gastronomia bănățeană reflectă influențele multiculturale ale regiunii, combinând elemente românești cu influențe germane, sârbești, maghiare și de alte origini. " +
               "Preparatele tradiționale include: papricaș bănățean, tăiței cu nucă (după rețeta șvăbească), sarmale în foi de viță, plăcintă bănățeană, supă de găluște, cârnaț bănățean și pită cu maia. " +
               "Produsele locale precum brânzeturile, carnea și vinul din podgoriile Banatului sunt apreciate pentru calitatea lor deosebită.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
               "- **Traian Vuia**: Inventator și pionier al aviației, născut lângă Lugoj, care a realizat primul zbor autopropulsat cu un aparat mai greu decât aerul\n" +
               "- **Johnny Weissmuller**: Sportiv olimpic și actor, născut la Timișoara, cunoscut pentru rolul lui Tarzan\n\n" +
               "Evenimente culturale:\n" +
               "- **Festivalul JazzTM**: Unul dintre cele mai importante festivaluri de jazz din România\n" +
               "- **Festivalul Plai**: Festival multicultural care celebrează diversitatea Banatului";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
               "- **Timișoara** a fost primul oraș european cu iluminat stradal electric permanent, în 1884?\n" +
               "- **Revoluția Română din 1989** a început la Timișoara?";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication_Light);
        }
        
        // Call super.onCreate but we'll override the layout next
        super.onCreate(savedInstanceState);
        
        // Set our specific layout
        setContentView(R.layout.activity_banat);
        
        // Find and hide any unwanted elements from parent layouts
        hideUnwantedParentElements();

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

    /**
     * This method hides any unwanted UI elements inherited from parent classes
     */
    private void hideUnwantedParentElements() {
        // Hide any AppBarLayout or Toolbar that might be inherited from parent classes
        View appBarLayout = findViewById(R.id.appbar);
        if (appBarLayout != null) {
            appBarLayout.setVisibility(View.GONE);
        }
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
        }
        
        // Hide the FloatingActionButton from EnhancedCityActivity
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab);
        if (fab != null) {
            fab.setVisibility(View.GONE);
        }
        
        // Hide the default viewCitiesButton from EnhancedCityActivity if we have our own
        View viewCitiesButton = findViewById(R.id.viewCitiesButton);
        if (viewCitiesButton != null && citiesButton != null) {
            viewCitiesButton.setVisibility(View.GONE);
        }
        
        // Ensure our header layout is visible and on top
        View headerLayout = findViewById(R.id.headerLayout);
        if (headerLayout != null) {
            headerLayout.bringToFront();
        }
    }

    private void initializeComponents() {
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize viewModel
        viewModel = new BanatViewModel();

        // Find views
        pointsText = findViewById(R.id.pointsText);
        gameButton = findViewById(R.id.buttonGoToDobrogeaGame);
        citiesButton = findViewById(R.id.buttonGoToCities);
        mapButton = findViewById(R.id.buttonGoToMap);
        casinoButton = findViewById(R.id.buttonGoToCasinoStory);
        
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
        if (citiesButton != null) {
            citiesButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, CityListActivity.class);
                intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Banat");
                startActivity(intent);
            });
        }

        // Casino/Story button
        if (casinoButton != null) {
            casinoButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 100) {
                    Intent intent = new Intent(this, BanatStoryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Banat Story!", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Game button
        if (gameButton != null) {
            gameButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 50) {
                    Intent intent = new Intent(this, BanatGameActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Banat Game!", 
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
            pointsManager.addPoints(this, "banat", gameScore);
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
        // Implementation for loading checkbox states from SharedPreferences
    }

    public void onCheckboxClicked(View view) {
        // Implementation for handling checkbox clicks
    }

    private void saveCheckboxStates() {
        // Implementation for saving checkbox states to SharedPreferences
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
            Intent intent = new Intent(this, BanatMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Nu s-a putut deschide harta Banatului: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            
            // Log error details for debugging
            android.util.Log.e("Banat", errorMessage);
            
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
            case 1: // Timișoara
                startActivity(BanatGameActivity.class);
                break;
            case 2: // Reșița
                startActivity(BanatStoryActivity.class);
                break;
            case 3: // Lugoj
                Intent intent = new Intent(this, RomCityActivity.class);
                intent.putExtra("CITY_NAME", "Lugoj");
                intent.putExtra("city_lat", 45.6909);
                intent.putExtra("city_lng", 21.9031);
                startActivity(intent);
                break;
            case 4: // Caransebeș
                Intent intent2 = new Intent(this, RomCityActivity.class);
                intent2.putExtra("CITY_NAME", "Caransebeș");
                intent2.putExtra("city_lat", 45.4177);
                intent2.putExtra("city_lng", 22.2192);
                startActivity(intent2);
                break;
            case 5: // Băile Herculane
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
