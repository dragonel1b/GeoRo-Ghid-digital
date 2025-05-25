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
import com.example.myapplication.maramuresusage.MaramuresGameActivity;
import com.example.myapplication.maramuresusage.MaramuresMapActivity;
import com.example.myapplication.maramuresusage.MaramuresStoryActivity;
import com.example.myapplication.viewmodel.CityListActivity;
import com.example.myapplication.viewmodel.MaramuresViewModel;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;

public class Maramures extends RegionTemplate {
    private PointsManager pointsManager;
    private SharedPreferences sharedPreferences;
    private static final String REGION = "Maramures";
    private MaterialButton gameButton;
    private MaterialButton citiesButton;
    private MaterialButton mapButton;
    private MaterialButton storyButton;
    private TextView pointsText;
    private MaramuresViewModel viewModel;

    @Override
    protected String getIntroductionText() {
        return "Maramureșul este o regiune istorică situată în nordul României, renumită pentru peisajele sale spectaculoase, arhitectura tradițională din lemn și obiceiurile populare bine conservate. " +
               "Cunoscută drept 'țara lemnului', Maramureșul reprezintă una dintre cele mai autentice zone etno-folclorice din Europa, unde tradițiile străvechi sunt încă vii.";
    }

    protected String getHistoryText() {
        return "Maramureșul are o istorie bogată, cu rădăcini ce se întind până în antichitate, fiind locuit inițial de daci, apoi influențat de prezența romană și ulterior dezvoltându-se ca o entitate distinctă în Evul Mediu.\n\n" +

               "Perioada medievală:\n" +
               "- Primele atestări documentare ale Maramureșului datează din secolul al XIII-lea\n" +
               "- În secolul al XIV-lea, Maramureșul deține statutul de voievodat român autonom în cadrul Regatului Ungariei\n" +
               "- Descălecarea lui Dragoș și Bogdan, conducători maramureșeni, care au întemeiat statul medieval Moldova\n" +
               "- Construirea bisericilor de lemn, multe dintre ele fiind astăzi incluse în patrimoniul UNESCO\n\n" +

               "Perioada modernă:\n" +
               "- În 1920, prin Tratatul de la Trianon, Maramureșul istoric este împărțit între România și Cehoslovacia (astăzi Ucraina)\n" +
               "- Rezistența anticomunistă din munții Maramureșului în perioada 1945-1970\n" +
               "- Conservarea extraordinară a tradițiilor, arhitecturii și meșteșugurilor populare în ciuda modernizării\n" +
               "- Dezvoltarea turismului rural și conservarea patrimoniului cultural după 1990";
    }

    protected String getGeographyText() {
        return "Geografie fizică:\n" +
               "- Regiunea este dominată de Munții Maramureșului, Munții Rodnei și Munții Gutâi\n" +
               "- Văi pitorești precum Valea Izei, Valea Marei și Valea Vișeului\n" +
               "- Râuri principale: Tisa (la granița cu Ucraina), Vișeu, Iza și Mara\n" +
               "- Clima este temperat-continentală cu influențe montane: veri răcoroase și ierni lungi cu zăpadă abundentă\n" +
               "- Parcul Natural Munții Maramureșului - cel mai mare parc natural din România\n" +
               "- Rezervații naturale: Creasta Cocoșului, Pietrosul Rodnei, Rezervația Piatra Rea\n\n" +

               "Geografie umană și economică:\n" +
               "- Sate tradiționale răspândite pe văi și dealuri, cu gospodării specifice și porți maramureșene sculptate\n" +
               "- Orașe principale: Baia Mare (reședința județului), Sighetu Marmației, Borșa, Vișeu de Sus\n" +
               "- Activități economice tradiționale: prelucrarea lemnului, agricultura de subzistență, păstorit, pomicultură\n" +
               "- Exploatări miniere istorice, în special aur, argint și cupru (multe închise în prezent)\n\n" +

               "Biodiversitate și zone protejate:\n" +
               "- Parcul Național Munții Rodnei - Rezervație a Biosferei UNESCO\n" +
               "- Peste 1400 de specii de plante, inclusiv specii rare precum floarea de colț și gențiana\n" +
               "- Faună bogată: urși, lupi, râși, cerbi, capre negre, cocoși de munte\n" +
               "- Păduri seculare de fag și molid, unele incluse în patrimoniul UNESCO";
    }

    @Override
    protected String getHistoryGeographyText() {
        return getHistoryText() + "\n\n" + getGeographyText();
    }

    @Override
    protected String getCultureTraditionsText() {
        return "Maramureșul este un tezaur viu de cultură populară românească, unde tradițiile și obiceiurile străvechi sunt păstrate și transmise din generație în generație.\n\n" +
               "- Portul popular maramureșean este unul dintre cele mai spectaculoase din România, cu elemente distinctive precum clop (pălărie), zadii (fuste populare), pieptare și cămăși bogat ornamentate\n\n" +
               "- Artizanatul este dominat de prelucrarea lemnului, maramureșenii fiind renumiți pentru măiestria cu care sculptează porți monumentale, troițe, mobilier și obiecte de uz gospodăresc\n\n" +
               "- Muzica tradițională se cântă cu instrumente precum țambal, vioară și zongoră (tobă), iar horile maramureșene au un stil vocal specific\n\n" +
               "- Obiceiuri populare unice: Festivalul de Iarnă de la Sighetu Marmației, Tânjaua de pe Mara, horele satului, șezătorile și nunțile tradiționale";
    }

    @Override
    protected String getAttractionsText() {
        return "Maramureșul oferă vizitatorilor experiențe autentice și atracții unice:\n\n" +
               "- **Bisericile de lemn UNESCO**: opt biserici incluse în patrimoniul mondial, printre care Bârsana, Budești, Desești și Ieud, capodopere ale arhitecturii tradiționale în lemn\n\n" +
               "- **Cimitirul Vesel de la Săpânța**: unic în lume prin crucile colorate și epitafurile în versuri care descriu cu umor viața defunctului\n\n" +
               "- **Mocănița de pe Valea Vaserului**: una dintre ultimele căi ferate forestiere cu locomotive cu aburi funcționale din Europa\n\n" +
               "- **Memorialul Victimelor Comunismului de la Sighetu Marmației**: fostă închisoare politică transformată în muzeu memorial\n\n" +
               "- **Complexul Muzeal Maramureș**: include Muzeul Satului Maramureșean în aer liber cu gospodării tradiționale\n\n" +
               "- **Porțile Maramureșene**: simboluri identitare remarcabile prin dimensiuni și ornamentică bogată";
    }

    @Override
    protected String getGastronomyText() {
        return "Bucătăria tradițională maramureșeană este robustă, gustoasă și bazată pe ingrediente naturale locale:\n\n" +
               "- **Horinca (pălinca) de Maramureș**: băutură spirtoasă tradițională din prune sau mere, dublu sau triplu distilată, esențială în cultura locală\n\n" +
               "- **Balmoșul**: mămăligă preparată cu brânză de burduf, smântână și unt, servită adesea cu jumări\n\n" +
               "- **Sarmale în foi de varză acră**: umplute cu carne de porc și orez, servite cu smântână și ardei iute\n\n" +
               "- **Cozonacul cu nucă și mac**: desert tradițional pregătit la sărbători\n\n" +
               "- **Plăcinta cu brânză de oaie**: coaptă în cuptor țărănesc și servită caldă\n\n" +
               "- **Tocană de miel cu mămăligă**: preparată în special de Paște";
    }

    @Override
    protected String getPersonalitiesEventsText() {
        return "Personalități marcante:\n" +
               "- **Elie Wiesel**: supraviețuitor al Holocaustului și laureat al Premiului Nobel pentru Pace, născut în Sighetu Marmației\n\n" +
               "- **Ioan Mihai Cochinescu**: scriitor și fotograf cunoscut pentru volumele sale despre Maramureș\n\n" +
               "- **Gheorghe Vida**: sculptor contemporan cu lucrări inspirate din mitologia și tradițiile Maramureșului\n\n" +
               "Evenimente culturale:\n" +
               "- **Festivalul Datinilor și Obiceiurilor de Iarnă**: eveniment major în Sighetu Marmației care atrage grupuri folclorice din întreaga regiune\n\n" +
               "- **Hora la Prislop**: sărbătoare tradițională ce are loc anual pe Vârful Prislop\n\n" +
               "- **Festivalul Internațional de Poezie de la Sighetu Marmației**: eveniment literar de prestigiu";
    }

    @Override
    protected String getCuriositiesText() {
        return "Știați că:\n" +
               "- **Cimitirul Vesel din Săpânța** este considerat unul dintre cele mai neobișnuite cimitire din lume, renumit pentru crucile multicolore și epitafurile pline de umor care narează viața defunctului?\n\n" +
               "- În Maramureș se află cel mai înalt turn de lemn din Europa - turnul bisericii din Șurdești, care măsoară 72 de metri?\n\n" +
               "- Mocănița de pe Valea Vaserului este singura cale ferată forestieră din Europa care funcționează în scop comercial, transportând atât turiști, cât și material lemnos?";
    }

    @Override
    protected String getRegionName() {
        return "Maramureș";
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("baia_mare");
        images.add("sighet");
        images.add("sapanta");
        images.add("barsana");
        images.add("borsa");
        return images;
    }

    private final String[] cityDescriptions = {
            "Baia Mare este reședința județului Maramureș și principalul centru urban al regiunii. " +
            "Fostă așezare minieră cu o istorie de peste 650 de ani, orașul impresionează prin " +
            "centrul vechi bine conservat, cu Turnul Ștefan, Biserica Sfântul Anton și case în stil baroc. " +
            "Muzeul Județean de Artă găzduiește o valoroasă colecție de pictură.",

            "Sighetu Marmației este al doilea oraș ca mărime din Maramureș, situat la granița cu Ucraina. " +
            "Cunoscut pentru Memorialul Victimelor Comunismului, Muzeul Satului Maramureșean și Casa Memorială " +
            "Elie Wiesel. A fost un important centru cultural și comercial al regiunii istorice.",

            "Săpânța este faimoasă în întreaga lume pentru Cimitirul Vesel, o adevărată galerie de artă în aer liber " +
            "cu crucile sale multicolore și epitafurile în versuri care descriu cu umor viața defuncților. " +
            "Satul păstrează și astăzi o atmosferă tradițională autentică.",

            "Bârsana este cunoscută pentru splendida sa mănăstire de maici, un ansamblu monastic impresionant " +
            "construit în stil maramureșean, cu biserica din lemn inclusă în patrimoniul UNESCO. Complexul monastic " +
            "include una dintre cele mai înalte construcții din lemn din Europa.",

            "Borșa este o stațiune montană situată la poalele Munților Rodnei, un important centru pentru " +
            "sporturile de iarnă și turismul montan. Oferă acces către Rezervația Naturală Pietrosul Rodnei " +
            "și peisaje montane spectaculoase."
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
        setContentView(R.layout.activity_maramures);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", MODE_PRIVATE);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize viewModel
        viewModel = new MaramuresViewModel();

        // Initialize views - using correct IDs from the layout
        pointsText = findViewById(R.id.textBalance);
        gameButton = findViewById(R.id.buttonGoToMaramuresGame);
        citiesButton = findViewById(R.id.buttonGoToCities);
        mapButton = findViewById(R.id.buttonGoToMap);
        storyButton = findViewById(R.id.buttonGoToMaramuresStory);

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
                    intent.putExtra(CityListActivity.EXTRA_REGION_NAME, "Maramureș");
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
                    Intent intent = new Intent(this, MaramuresStoryActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 100 de puncte pentru a explora Povestea Maramureșului!", 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (gameButton != null) {
            gameButton.setOnClickListener(v -> {
                if (pointsManager.getPoints(this) >= 50) {
                    Intent intent = new Intent(this, MaramuresGameActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Ai nevoie de cel puțin 50 de puncte pentru a juca Maramureș Game!", 
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
            pointsManager.addPoints(this, REGION, gameScore);
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
        showPopup("Baia Mare", cityDescriptions[0]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra(RomCityActivity.CITY_NAME, "Baia Mare");
        intent.putExtra(RomCityActivity.CITY_REGION, "Maramureș");
        intent.putExtra(RomCityActivity.CITY_DESC, "Baia Mare este municipiul reședință al județului Maramureș, și un important centru economic, cultural și universitar din nordul României.");
        startActivity(intent);
    }

    public void showPopup2(View view) {
        showPopup("Sighetu Marmației", cityDescriptions[1]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Sighetu Marmației");
        intent.putExtra("city_lat", 47.9287);
        intent.putExtra("city_lng", 23.8915);
        startActivity(intent);
    }

    public void showPopup3(View view) {
        showPopup("Săpânța", cityDescriptions[2]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Săpânța");
        intent.putExtra("city_lat", 47.9831);
        intent.putExtra("city_lng", 23.6967);
        startActivity(intent);
    }

    public void showPopup4(View view) {
        showPopup("Bârsana", cityDescriptions[3]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Bârsana");
        intent.putExtra("city_lat", 47.8036);
        intent.putExtra("city_lng", 24.0631);
        startActivity(intent);
    }

    public void showPopup5(View view) {
        showPopup("Borșa", cityDescriptions[4]);
        Intent intent = new Intent(this, RomCityActivity.class);
        intent.putExtra("CITY_NAME", "Borșa");
        intent.putExtra("city_lat", 47.6599);
        intent.putExtra("city_lng", 24.6577);
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
        
        editor.apply();
    }

    public void startMapActivity() {
        try {
            // Explicitly use the full class name to ensure we load the correct one
            Class<?> mapActivityClass = Class.forName("com.example.myapplication.maramuresusage.MaramuresMapActivity");
            Intent intent = new Intent(this, mapActivityClass);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Nu s-a putut deschide harta Maramureșului", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleLocationClick(int locationId) {
        switch (locationId) {
            case 1: // Baia Mare
                startActivity(MaramuresGameActivity.class);
                break;
            case 2: // Sighetu Marmației
                startActivity(MaramuresStoryActivity.class);
                break;
            case 3: // Săpânța
                if (pointsManager.getPoints(this) >= 20) {
                    Intent cityIntent = new Intent(this, RomCityActivity.class);
                    startActivity(cityIntent);
                } else {
                    Toast.makeText(this, "Ai nevoie de 20 de puncte pentru a debloca acest oraș!", Toast.LENGTH_SHORT).show();
                }
                break;
            case 4: // Bârsana
                Toast.makeText(this, "Această funcționalitate nu este disponibilă momentan", Toast.LENGTH_SHORT).show();
                break;
            case 5: // Borșa
                Toast.makeText(this, "Această funcționalitate nu este disponibilă momentan", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    public void updateProgress(int locationId, boolean completed) {
        viewModel.setLocationCompleted(this, locationId, completed);
    }

    public boolean isLocationCompleted(int locationId) {
        return viewModel.isLocationCompleted(this, locationId);
    }

    public int getCompletedLocationsCount() {
        return viewModel.getCompletedLocationsCount(this);
    }
}
