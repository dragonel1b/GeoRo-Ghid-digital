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
import com.example.myapplication.moldovausage.MoldovaStoryActivity;
import com.example.myapplication.moldovausage.MoldovaGameActivity;
import com.example.myapplication.moldovausage.MoldovaMapActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.MoldovaViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import android.widget.LinearLayout;

public class Moldova extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "moldova";
    private MaterialButton casinoButton;
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private TextView pointsText;
    private MoldovaViewModel viewModel;

    @Override
    protected String getIntroductionText() {
        return "Moldova este o regiune istorică situată în estul României, cu un peisaj diversificat și o istorie bogată. " +
               "Este cunoscută pentru ospitalitatea locuitorilor, mănăstirile pictate, dealurile line acoperite de vii renumite " +
               "și tradiții folclorice autentice păstrate de secole.";
    }

    protected String getHistoryText() {
        return "Moldova are o istorie complexă ce reflectă evoluția identității românești de-a lungul secolelor.\n\n" +

               "Perioada antică:\n" +
               "- Teritoriul a fost locuit de triburi geto-dacice\n" +
               "- Partea de Est a făcut parte din Regatul lui Burebista și apoi al lui Decebal\n" +
               "- În perioada romană, doar partea de sud a fost inclusă în provincia Dacia\n\n" +

               "Evul Mediu:\n" +
               "- Formarea Principatului Moldovei în secolul al XIV-lea\n" +
               "- Perioada de glorie sub domnia lui Ștefan cel Mare (1457-1504)\n" +
               "- Rezistența față de Imperiul Otoman și păstrarea autonomiei\n" +
               "- Dezvoltarea culturală și religioasă prin ctitorirea de mănăstiri și biserici\n\n" +

               "Perioada modernă:\n" +
               "- Unirea cu Țara Românească în 1859 prin dubla alegere a lui Alexandru Ioan Cuza\n" +
               "- Rolul important în Primul Război Mondial și realizarea României Mari\n" +
               "- Divizarea regiunii după al Doilea Război Mondial, cu partea de est devenind RSS Moldovenească\n" +
               "- După 1989, partea vestică rămâne în România, iar partea estică devine Republica Moldova";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
               "- Delimitată de Carpații Orientali la vest și râul Prut la est\n" +
               "- Relief variat: munți (Carpații Orientali), dealuri (Podișul Moldovei), câmpii (lunca Prutului și Siretului)\n" +
               "- Râuri principale: Siret, Prut, Bistrița, Moldova\n" +
               "- Climat temperat-continental cu influențe est-europene\n" +
               "- Vegetație diversificată, de la păduri de foioase la stepe și lunci\n\n" +

               "Geografie umană și economică:\n" +
               "- Centre urbane importante: Iași, Suceava, Bacău, Piatra Neamț, Botoșani\n" +
               "- Economia bazată pe agricultură (cereale, sfeclă de zahăr, vii), industrie și turism\n" +
               "- Regiunea viticolă Cotnari, cu soiuri autohtone renumite (Grasă de Cotnari, Fetească)\n" +
               "- Zone rurale cu un bogat patrimoniu etnografic și meșteșuguri tradiționale\n\n" +

               "Biodiversitate și zone protejate:\n" +
               "- Parcuri naturale: Vânători-Neamț, Ceahlău\n" +
               "- Rezervații naturale: Cheile Bicazului-Hășmaș, Lacul Roșu\n" +
               "- Faună diversă ce include specii rare precum zimbrul reintegrat în Parcul Vânători-Neamț\n" +
               "- Ecosisteme unice formate la confluența influențelor central-europene și est-europene";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Moldova păstrează un tezaur cultural deosebit, cu tradiții și obiceiuri ce reflectă sufletul neamului românesc. " +
               "Portul popular moldovenesc se distinge prin eleganță și cromatică, cu ii brodate manual în modele specifice zonei. " +
               "Tradițiile folclorice includ dansuri precum Hora, Bătuta și Corăgheasca, iar muzica este dominată de instrumente precum cobza, fluierul și vioara. " +
               "Obiceiurile de iarnă sunt deosebit de spectaculoase, cu măști, jocuri cu urși și capre, colinde și teatru popular (Irozii). " +
               "Ceramica de Horezu și covoarele țesute manual sunt parte din patrimoniul meșteșugăresc încă viu în satele moldovenești.";
    }

    @Override
    protected String getAttractionsText() {
        return "Moldova oferă o varietate de atracții turistice remarcabile, printre care se numără:\n\n" +
               "- **Mănăstirile pictate din Bucovina**: patrimoniu UNESCO, aceste bijuterii arhitecturale precum Voroneț, Sucevița, Moldovița și Humor sunt faimoase pentru frescele exterioare de o frumusețe unică.\n\n" +
               "- **Iași - capitala culturală a Moldovei**: găzduiește Palatul Culturii, Biserica Trei Ierarhi, Universitatea Alexandru Ioan Cuza (cea mai veche din România) și numeroase muzee și grădini.\n\n" +
               "- **Cetatea Neamț**: una dintre cele mai impresionante fortificații medievale din România, construită în timpul domniei lui Petru I și consolidată de Ștefan cel Mare.\n\n" +
               "- **Cheile Bicazului și Lacul Roșu**: formațiuni geologice spectaculoase și peisaje naturale impresionante, ideale pentru drumeții și activități în aer liber.";
    }

    @Override
    protected String getGastronomyText() {
        return "Gastronomia moldovenească este renumită pentru savoarea, diversitatea și autenticitatea sa, reflectând bogăția regiunii și influențele istorice diverse. " +
               "Preparatele tradiționale includ: borș moldovenesc, sarmale în foi de viță, tochitură moldovenească cu mămăligă și brânză, plăcinte poale-n brâu, alivenci. " +
               "Regiunea este celebră pentru vinurile sale de calitate din podgoriile Cotnari, Huși și Iași, cu soiuri autohtone precum Grasă de Cotnari, Fetească Albă și Busuioacă de Bohotin. " +
               "Dulciurile tradiționale precum pârjoalele dulci, papanașii și plăcinta cu mere completează experiența culinară autentică a Moldovei.";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
               "- **Mihai Eminescu**: cel mai mare poet român, născut la Botoșani, a cărui operă reprezintă un pilon al literaturii române.\n" +
               "- **Alexandru Ioan Cuza**: domnitor al Principatelor Unite, artizan al unirii din 1859 și al reformelor fundamentale pentru statul român modern.\n" +
               "- **Ion Creangă**: marele povestitor român, născut în Humulești, a cărui operă reflectă spiritul și viața satului moldovenesc.\n" +
               "- **George Enescu**: unul dintre cei mai importanți muzicieni români, compozitor și violonist de renume mondial, născut în Liveni, județul Botoșani.\n\n" +
               "Evenimente culturale:\n" +
               "- **Festivalul Internațional de Teatru de la Iași**: unul dintre cele mai importante evenimente teatrale din România.\n" +
               "- **Festivalul Internațional George Enescu** (cu extensii la Iași): celebrează moștenirea marelui compozitor moldovean.\n" +
               "- **Zilele Recoltei**: sărbători tradiționale organizate toamna în diverse localități moldovenești pentru a marca încheierea sezonului agricol.";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
               "- **Albastrul de Voroneț**, culoarea predominantă a frescelor exterioare ale Mănăstirii Voroneț, este unică în lume datorită compoziției sale naturale și rezistenței extraordinare la trecerea timpului?\n" +
               "- **Iașiul** a fost prima capitală a României moderne între 1859-1862, în timpul domniei lui Alexandru Ioan Cuza?\n" +
               "- **Cetatea Neamț** nu a fost niciodată cucerită prin luptă directă, rezistând inclusiv asediului otoman din 1476 condus de Mahomed al II-lea, cuceritorul Constantinopolului?";
    }

    @Override
    protected String getRegionName() {
        return "Moldova";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            cityImages.add("iasi");
            cityImages.add("suceava");
            cityImages.add("piatra_neamt");
            cityImages.add("bacau");
            cityImages.add("botosani");
        }
        return cityImages;
    }

    private final String[] cityDescriptions = {
            "Iași este considerat capitala culturală a Moldovei și unul dintre cele mai importante " +
            "centre academice și spirituale din România. Orașul反感 prin patrimoniul " +
            "arhitectural bogat, muzee de renume și parcuri spectaculoase.",

            "Bacău este un important centru industrial și cultural al Moldovei. Cunoscut pentru " +
            "muzeele sale, inclusiv Casa Memorială \"Vasile Alecsandri\" și pentru patrimoniul " +
            "său cultural, orașul este și un important nod de transport.",

            "Piatra Neamț, supranumit \"Perla Moldovei\", este înconjurat de munți și oferă un " +
            "peisaj urban pitoresc. Telecabina duce vizitatorii către vârful Cozla, de unde se " +
            "poate admira panorama superbă a orașului.",

            "Botoșani este un oraș plin de istorie, fiind locul de naștere al unor mari personalități " +
            "precum Mihai Eminescu și George Enescu. Centrul istoric bine conservat și muzeele " +
            "locale fac din acest oraș o destinație culturală de neratat.",

            "Galați este un important centru industrial și portuar la Dunăre. Orașul oferă atracții " +
            "precum Grădina Botanică, Muzeul de Istorie și faleza Dunării, unde vizitatorii se pot " +
            "bucura de priveliști impresionante ale fluviului."
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
        setContentView(R.layout.activity_moldova);

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
        viewModel = new MoldovaViewModel();

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
            intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Moldova");
            startActivity(intent);
        });

        // Moldova Story button
        casinoButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 100) {
                Intent intent = new Intent(this, com.example.myapplication.moldovausage.MoldovaStoryActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a juca Moldova Story!", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Moldova Game button
        gameButton.setOnClickListener(v -> {
            if (pointsManager.getPoints(this) >= 50) {
                Intent intent = new Intent(this, com.example.myapplication.moldovausage.GameModeSelectionActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Moldova Game!", 
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
            pointsManager.addPoints(this, "moldova", gameScore);
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
        showPopup("Iași", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Iași");
        intent.putExtra("city_lat", 47.1585);
        intent.putExtra("city_lng", 27.6014);
        startActivity(intent);
    }

    public void showPopup2(View view) {
        if (cityDescriptions.length > 1) {
            showPopup("Suceava", cityDescriptions[1]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Suceava");
            intent.putExtra("city_lat", 47.6635);
            intent.putExtra("city_lng", 26.2732);
            startActivity(intent);
        }
    }

    public void showPopup3(View view) {
        if (cityDescriptions.length > 2) {
        showPopup("Piatra Neamț", cityDescriptions[2]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Piatra Neamț");
            intent.putExtra("city_lat", 46.9258);
            intent.putExtra("city_lng", 26.3725);
            startActivity(intent);
        }
    }

    public void showPopup4(View view) {
        if (cityDescriptions.length > 3) {
            showPopup("Bacău", cityDescriptions[3]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Bacău");
            intent.putExtra("city_lat", 46.5670);
            intent.putExtra("city_lng", 26.9146);
            startActivity(intent);
        }
    }

    public void showPopup5(View view) {
        if (cityDescriptions.length > 4) {
            showPopup("Botoșani", cityDescriptions[4]);
            Intent intent = new Intent(this, RomCityActivity.class);
            intent.putExtra("CITY_NAME", "Botoșani");
            intent.putExtra("city_lat", 47.7487);
            intent.putExtra("city_lng", 26.6698);
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
            Intent intent = new Intent(this, MoldovaMapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Nu s-a putut deschide harta Moldovei: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            
            // Log error details for debugging
            android.util.Log.e("Moldova", errorMessage);
            
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
            case 1: // Iași
                startActivity(MoldovaGameActivity.class);
                break;
            case 2: // Suceava
                startActivity(MoldovaStoryActivity.class);
                break;
            case 3: // Piatra Neamț
                Intent intent = new Intent(this, RomCityActivity.class);
                intent.putExtra("CITY_NAME", "Piatra Neamț");
                intent.putExtra("city_lat", 46.9258);
                intent.putExtra("city_lng", 26.3725);
                startActivity(intent);
                break;
            case 4: // Bacău
                Intent intent2 = new Intent(this, RomCityActivity.class);
                intent2.putExtra("CITY_NAME", "Bacău");
                intent2.putExtra("city_lat", 46.5670);
                intent2.putExtra("city_lng", 26.9146);
                startActivity(intent2);
                break;
            case 5: // Botoșani
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
