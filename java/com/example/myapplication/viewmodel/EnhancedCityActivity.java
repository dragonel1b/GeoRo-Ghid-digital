package com.example.myapplication.viewmodel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.RomApp.PointsManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.myapplication.R;
import com.example.myapplication.model.CityImage;
import com.example.myapplication.model.CityImageAdapter;
import com.example.myapplication.viewmodel.CityViewModel;
import com.example.myapplication.viewmodel.CityInfoBottomSheet;
import com.example.myapplication.viewmodel.SectionPreviewActivity;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EnhancedCityActivity extends BaseCityActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    protected ArrayList<String> cityImages;
    protected CityImageAdapter imageAdapter;
    private CityViewModel viewModel;
    private MediaPlayer soundEffect;
    private KonfettiView konfettiView;
    private ViewPager2 imageCarousel;
    private TabLayout imageIndicator;
    private FloatingActionButton fab;
    private View viewCitiesButton;
    private List<CityImage> images = new ArrayList<>();
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_city);

        viewModel = new ViewModelProvider(this).get(CityViewModel.class);
        setupViews();
        setupImageCarousel();
        setupImagePickers();
        initializeSpecificContent();
        setupSoundEffects();
        setupFloatingActionButton();
        setupConfetti();
        updatePointsDisplay(); // Actualizăm afișarea punctelor la pornirea activității
        
        // Adăugăm butonul flotant pentru încărcarea imaginilor în toate activitățile
        FloatingButtonHelper.addPhotoButton(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkAllImageResources(); // Adăugat pentru depanare
        updatePointsDisplay(); // Actualizăm punctele când utilizatorul revine la activitate
    }
    
    // Metodă pentru actualizarea afișării punctelor în bara de sus
    private void updatePointsDisplay() {
        // Mai întâi obținem PointsManager
        PointsManager pointsManager = PointsManager.getInstance(this);
        
        // Obținem numele regiunii curente (standardizat)
        String region = pointsManager.standardizeRegionName(getRegionName().toLowerCase());
        
        // Obținem punctele
        int totalPoints = pointsManager.getTotalPoints(this);
        int regionPoints = pointsManager.getRegionPoints(this, region);
        
        // Actualizăm TextView-ul din bara de sus (dacă există)
        TextView pointsTextView = findViewById(R.id.pointsTextView);
        if (pointsTextView != null) {
            pointsTextView.setText("Puncte: " + totalPoints + " (" + region + ": " + regionPoints + ")");
            
            // Aplicăm animație pentru a atrage atenția la schimbare
            Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.button_bounce);
            if (bounceAnim != null) {
                pointsTextView.startAnimation(bounceAnim);
            }
        }
        
        // Actualizăm și TextView-ul cu iconiță (dacă există)
        TextView iconPointsTextView = findViewById(R.id.pointsText);
        if (iconPointsTextView != null) {
            iconPointsTextView.setText(totalPoints + " Puncte");
            
            // Aplicăm animație și aici
            Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.button_bounce);
            if (bounceAnim != null) {
                iconPointsTextView.startAnimation(bounceAnim);
            }
        }
        
        // Dacă niciunul dintre TextView-uri nu există, afișăm un mesaj Toast
        if (pointsTextView == null && iconPointsTextView == null) {
            Toast.makeText(this, "Puncte totale: " + totalPoints + " (" + region + ": " + regionPoints + ")", 
                           Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViews() {
        konfettiView = findViewById(R.id.confetti_view);
        fab = findViewById(R.id.fab);
        viewCitiesButton = findViewById(R.id.viewCitiesButton);

        if (viewCitiesButton != null) {
            viewCitiesButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, CityListActivity.class);
                startActivity(intent);
            });
        }

        if (fab != null) {
            fab.setOnClickListener(v -> {
                CityInfoBottomSheet bottomSheet = new CityInfoBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            });
        }

        // Setup View Cities button with enhanced animation
        findViewById(R.id.viewCitiesButton).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            Intent intent = new Intent(this, CityListActivity.class);
            intent.putExtra(CityListActivity.EXTRA_REGION_NAME, getRegionName());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Apply enhanced elastic scroll with gradient background
        LinearLayout mainContainer = findViewById(R.id.cityContentContainer);
        mainContainer.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        mainContainer.setBackgroundResource(R.drawable.gradient_background);
    }

    private void setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        addImageToCarousel(imageUri, true);
                        // Salvăm imaginea adăugată de utilizator
                        saveUserImage(imageUri.toString());
                    }
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.containsKey("data")) {
                        // Pentru imagini capturate cu camera, primim un Bitmap
                        android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
                        
                        // Salvăm bitmap-ul ca fișier temporar și obținem URI-ul
                        Uri imageUri = saveImageToCache(imageBitmap);
                        if (imageUri != null) {
                        addImageToCarousel(imageUri, true);
                            // Salvăm imaginea adăugată de utilizator
                            saveUserImage(imageUri.toString());
                        }
                    }
                }
            }
        );
    }

    // Metodă pentru salvarea bitmap-ului ca fișier temporar
    private Uri saveImageToCache(android.graphics.Bitmap bitmap) {
        try {
            String fileName = "city_image_" + System.currentTimeMillis() + ".jpg";
            java.io.File cacheDir = new java.io.File(getCacheDir(), "city_images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            java.io.File outputFile = new java.io.File(cacheDir, fileName);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            
            // Returnăm URI-ul fișierului
            return Uri.fromFile(outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Eroare la salvarea imaginii: " + e.getMessage(), 
                          Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // Metodă pentru salvarea URI-ului imaginii în SharedPreferences
    private void saveUserImage(String imageUriString) {
        try {
            SharedPreferences prefs = getSharedPreferences("UserImagesPrefs", MODE_PRIVATE);
            String cityKey = "city_images_" + getCityName().toLowerCase().replace(" ", "_");
            
            // Obținem lista curentă de imagini
            String currentImages = prefs.getString(cityKey, "");
            ArrayList<String> imagesList = new ArrayList<>();
            
            if (!currentImages.isEmpty()) {
                imagesList.addAll(Arrays.asList(currentImages.split(",")));
            }
            
            // Adăugăm noua imagine dacă nu există deja
            if (!imagesList.contains(imageUriString)) {
                imagesList.add(imageUriString);
                
                // Salvăm lista actualizată
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(cityKey, TextUtils.join(",", imagesList));
                editor.apply();
                
                Toast.makeText(this, "Imagine salvată pentru " + getCityName(), 
                              Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFloatingActionButton() {
        fab.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showCityInfoBottomSheet();
        });
        
        // Add rotation animation
        fab.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            v.animate().rotationBy(360).setDuration(500).start();
            return true;
        });
    }

    private void showCityInfoBottomSheet() {
        CityInfoBottomSheet bottomSheet = new CityInfoBottomSheet();
        Bundle args = new Bundle();
        args.putString(CityInfoBottomSheet.ARG_CITY_NAME, getCityName());
        args.putString(CityInfoBottomSheet.ARG_REGION_NAME, getRegionName());
        args.putInt(CityInfoBottomSheet.ARG_POINTS, 100); // Default points for visiting
        
        // Get image resource for the city
        int imageResId = R.drawable.casa_baniei; // Default image
        String cityName = getCityName();
        if (cityName.equals("Brașov")) {
            imageResId = R.drawable.brasov;
        } else if (cityName.equals("Cluj-Napoca")) {
            imageResId = R.drawable.cluj;
        } else if (cityName.equals("Sibiu")) {
            imageResId = R.drawable.sibiu;
        }
        
        args.putInt("imageResId", imageResId);
        args.putString("description", getCityDescription());
        
        bottomSheet.setArguments(args);
        bottomSheet.show(getSupportFragmentManager(), "CityInfoBottomSheet");
    }

    private void setupSoundEffects() {
        soundEffect = MediaPlayer.create(this, R.raw.click_sound);
    }

    private void setupConfetti() {
        konfettiView = findViewById(R.id.confetti_view);
    }

    private void showConfetti() {
        EmitterConfig emitterConfig = new Emitter(5, TimeUnit.SECONDS).perSecond(50);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(1f, 5f)
                .timeToLive(2000L)
                .colors(Arrays.asList(
                    getResources().getColor(R.color.colorPrimary),
                    getResources().getColor(R.color.colorAccent),
                    getResources().getColor(R.color.colorPrimaryDark)))
                .position(0.5f, 0.3f, 1f, 0.7f)
                .build();

        konfettiView.start(party);
    }

    private void setupCard(int cardId, int titleId, int checkId, int imageId, 
                          int opinionId, int recommendationId) {
        ConstraintLayout card = findViewById(cardId);
        TextView title = findViewById(titleId);
        ImageView check = findViewById(checkId);
        ImageView image = findViewById(imageId);

        card.setOnClickListener(v -> {
            if (check.getVisibility() == View.GONE) {
                check.setVisibility(View.VISIBLE);
                showConfetti();
                playSuccessSound();
            } else {
                check.setVisibility(View.GONE);
            }
        });

        image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    private void playSuccessSound() {
        if (soundEffect != null) {
            soundEffect.start();
        }
    }

    @Override
    protected String getCityName() {
        // Determinăm numele orașului bazat pe clasa actuală
        String className = getClass().getSimpleName();
        
        if (className.contains("Constanta")) {
            return "Constanța";
        } else if (className.contains("Oltenia")) {
            return "Craiova"; // Principalul oraș din Oltenia
        } else if (className.contains("Moldova")) {
            return "Iași"; // Principalul oraș din Moldova
        } else if (className.contains("Transilvania")) {
            return "Cluj-Napoca"; // Principalul oraș din Transilvania
        } else if (className.contains("Muntenia")) {
            return "București"; // Principalul oraș din Muntenia
        } else if (className.contains("Banat")) {
            return "Timișoara"; // Principalul oraș din Banat
        } else if (className.contains("Sibiu")) {
            return "Sibiu"; // Oraș istoric din Transilvania cu arhitectură saxonă
        } else if (className.contains("Brasov")) {
            return "Brașov"; // Oraș turistic din Transilvania cu Cetate medievală
        } else if (className.contains("ClujNapoca")) {
            return "Cluj-Napoca"; // Centru cultural și universitar în Transilvania
        }
        
        // Valoare implicită generică
        return "Oraș necunoscut";
    }

    protected String getRegionName() {
        // Get region name from the class name of the actual activity
        String className = getClass().getSimpleName();
        
        // Determine region for the direct city classes
        if (className.equals("Brasov") || className.equals("ClujNapoca") || className.equals("Sibiu")) {
            return "Transilvania";
        }
        
        // For region and other classes
        if (className.contains("Oltenia")) {
            return "Oltenia";
        } else if (className.contains("Moldova")) {
            return "Moldova";
        } else if (className.contains("Transilvania")) {
            return "Transilvania";
        } else if (className.contains("Muntenia")) {
            return "Muntenia";
        } else if (className.contains("Dobrogea")) {
            return "Dobrogea";
        } else if (className.contains("Banat")) {
            return "Banat";
        } else if (className.contains("Crisana")) {
            return "Crișana";
        } else if (className.contains("Maramures")) {
            return "Maramureș";
        } else if (className.contains("Bucovina")) {
            return "Bucovina";
        }
        
        return "Regiune necunoscută";
    }

    @Override
    protected void initializeSpecificContent() {
        // Determină regiunea bazată pe clasa actuală
        String className = getClass().getSimpleName();
        LinearLayout container = findViewById(R.id.cityContentContainer);
        
        if (className.contains("Constanta") || className.contains("Dobrogea")) {
            // Conținut specific pentru Constanța/Dobrogea
            addSection(container, "Introducere", "Constanța, cunoscută în antichitate sub numele de Tomis, este cel mai vechi oraș atestat de pe teritoriul României. Situată pe coasta Mării Negre, este un important centru economic, cultural și turistic al țării.", true);
            addSection(container, "Geografie", "Constanța este situată în sud-estul României, pe coasta Mării Negre. Orașul se întinde pe o suprafață de aproximativ 124 km² și include numeroase lacuri precum Siutghiol și Tăbăcărie. Clima este temperat-continentală cu influențe maritime, cu veri călduroase și ierni blânde.", false);
            addSection(container, "Istorie", "Fondată în secolul al VI-lea î.Hr. de coloniștii greci din Milet, Constanța a fost cunoscută inițial sub numele de Tomis. A fost un important centru comercial și cultural în perioada romană, iar mai târziu a devenit parte a Imperiului Bizantin. În perioada modernă, orașul a cunoscut o dezvoltare rapidă, devenind principalul port al României.", false);
            addSection(container, "Atracții Turistice", "Cazinoul, Moscheea Carol I, Portul Tomis și plajele moderne sunt doar câteva dintre atracțiile care fac din Constanța o destinație turistică de top.", false);
            addSection(container, "Cultură", "Un oraș multicultural unde se împletesc influențele române, grecești, turcești și tătare, creând un mozaic cultural unic.", false);
        } else if (className.contains("Oradea")) {
            // Conținut specific pentru Oradea
            addSection(container, "Introducere", "Oradea este un oraș cu o bogată istorie și un important centru cultural, economic și administrativ din vestul României. Situat pe râul Crișul Repede, în apropierea graniței cu Ungaria, orașul impresionează prin arhitectura Art Nouveau și baroc, precum și prin băile termale renumite.", true);
            addSection(container, "Geografie", "Oradea este situată în câmpia de vest a României, pe malurile râului Crișul Repede. Orașul beneficiază de un climat temperat-continental cu influențe oceanice, fiind ferit de extreme climatice. Relieful predominant de câmpie și dealuri line face din Oradea un oraș prietenos pentru bicicliști și pietoni.", false);
            addSection(container, "Istorie", "Prima atestare documentară a Oradiei datează din 1113, când a fost menționată sub numele de Varadinum. De-a lungul secolelor, orașul a fost sub diverse stăpâniri: maghiară, otomană, habsburgică și română (după 1918). În perioada austro-ungară, Oradea a cunoscut o dezvoltare urbanistică impresionantă, care se reflectă în arhitectura sa eclectică de astăzi.", false);
            addSection(container, "Arhitectură", "Oradea este renumită pentru ansamblul arhitectural Art Nouveau, unul dintre cele mai mari și mai bine conservate din Europa. Strada Republicii, Piața Unirii, Palatul Vulturul Negru, Palatul Moskovits, Palatul Apollo, Palatul Stern și Casa Darvas-La Roche sunt doar câteva dintre capodoperele arhitecturale ce pot fi admirate în Oradea.", false);
            addSection(container, "Atracții Turistice", "Cetatea Oradea (una dintre cele mai mari cetăți de tip pentagonal din Europa Centrală), Catedrala Romano-Catolică (cea mai mare biserică barocă din România), Sinagoga Neologă Sion, Palatul Episcopal Baroc, Muzeul Țării Crișurilor, Parcul 1 Decembrie și Băile Felix (la doar 8 km de oraș) sunt principalele obiective turistice.", false);
            addSection(container, "Cultură", "Orașul are o viață culturală vibrantă, cu instituții precum Teatrul de Stat, Filarmonica de Stat, Teatrul Regina Maria și Muzeul Țării Crișurilor. Festivalul Internațional de Teatru Scurt, Festivalul de Jazz și Festivalul Medieval al Cetății sunt evenimente culturale importante care atrag anual numeroși vizitatori.", false);
            addSection(container, "Gastronomie", "Bucătăria din Oradea reflectă caracterul multicultural al regiunii, cu influențe românești, maghiare și austriece. Specialități locale precum goulașul, ciorbele acrișoare, langalăul, cozonacul secuiesc și faimoasele prăjituri Gerbeaud pot fi savurate în restaurantele tradiționale din oraș.", false);
        } else if (className.contains("Crisana")) {
            // Conținut specific pentru regiunea Crișana
            addSection(container, "Introducere", "Crișana este o regiune istorică situată în vestul României, la granița cu Ungaria. Este o zonă cu un bogat patrimoniu cultural, arhitectură diversă și peisaje naturale spectaculoase, ce include câmpii fertile, dealuri line și zone montane.", true);
            addSection(container, "Geografie", "Regiunea este situată în nord-vestul României, între Munții Apuseni la est și granița cu Ungaria la vest. Este străbătută de râurile Crișul Alb, Crișul Negru și Crișul Repede, care i-au dat și numele. Relieful variat include câmpii, dealuri și zone montane, oferind peisaje naturale diverse și spectaculoase.", false);
            addSection(container, "Istorie", "Crișana are o istorie îndelungată și complexă, fiind locuită încă din antichitate. A făcut parte din Dacia și ulterior din Imperiul Roman. În Evul Mediu, a fost disputată între Regatul Ungariei și Imperiul Otoman. După Primul Război Mondial, a devenit parte a României Mari, cu excepția extremității vestice care a rămas Ungariei.", false);
            addSection(container, "Orașe Principale", "Oradea este cel mai important oraș al regiunii, urmat de Arad. Alte localități importante sunt Salonta, Beiuș, Ineu și Chișineu-Criș. Fiecare dintre aceste orașe are propriul farmec și obiective turistice specifice, reflectând istoria bogată a regiunii.", false);
            addSection(container, "Cultură și Tradiții", "Crișana este o regiune multiculturală, unde conviețuiesc români, maghiari, sârbi, slovaci și alte etnii. Această diversitate se reflectă în folclor, tradiții, arhitectură și gastronomie. Portul popular, dansurile și muzica tradițională păstrează specificul local, iar meșteșugurile tradiționale sunt încă practicate în multe sate.", false);
            addSection(container, "Atracții Turistice", "Regiunea oferă o varietate de atracții turistice: arhitectura Art Nouveau din Oradea, Cetatea Aradului, stațiunile balneare (Băile Felix, Băile 1 Mai, Moneasa), peșterile și formațiunile carstice din Munții Apuseni, bisericile de lemn din Țara Beiușului și multe altele. Turismul rural se dezvoltă tot mai mult, oferind vizitatorilor experiențe autentice în satele tradiționale.", false);
            addSection(container, "Gastronomie", "Bucătăria regiunii Crișana combină influențe românești, maghiare și central-europene. Specialitățile locale includ gulaș, langoși, sarmale în foi de varză, ciorbe acrișoare, plăcinte tradiționale, cozonac secuiesc și preparate din pește de apă dulce. Vinurile de Miniș-Măderat și pălinca de fructe sunt băuturile tradiționale ale regiunii.", false);
        } else if (className.contains("Arad")) {
            // Conținut specific pentru Arad
            addSection(container, "Introducere", "Arad este un important centru economic, cultural și universitar din vestul României, situat pe malul Mureșului. Cu o arhitectură impresionantă și o istorie bogată, orașul este o poartă de intrare în țară dinspre Europa Centrală.", true);
            addSection(container, "Geografie", "Orașul este așezat în câmpia Aradului, pe malul râului Mureș, la doar 20 km de granița cu Ungaria. Clima temperat-continentală cu influențe mediteraneene oferă veri călduroase și ierni blânde. Poziția strategică a făcut din Arad un important nod de transport și comunicații.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1028. De-a lungul istoriei, Aradul a fost sub stăpânire otomană, habsburgică și austro-ungară, înainte de a deveni parte a României în 1918. Orașul a jucat un rol important în Revoluția Română din 1848-1849 și în evenimentele din decembrie 1989.", false);
            addSection(container, "Atracții Turistice", "Cetatea Aradului (impresionantă fortificație în stil Vauban din secolul al XVIII-lea), Palatul Administrativ (clădire emblematică în stil neo-renascentist), Palatul Cultural, Teatrul Clasic „Ioan Slavici, Biserica Roșie (biserica evanghelică-luterană), Turnul de Apă și podurile istorice peste Mureș sunt obiective turistice de neratat.", false);
            addSection(container, "Cultură", "Aradul are o viață culturală bogată, cu instituții precum Filarmonica de Stat, Teatrul Clasic „Ioan Slavici, Muzeul de Artă și Muzeul Județean. Festivalul Internațional de Teatru Clasic, Zilele Culturale ale Aradului și Târgul de Carte „Arad sunt evenimente importante în calendarul cultural al orașului.", false);
            addSection(container, "Personalități", "Aradul a dat țării personalități marcante precum: Ioan Slavici (scriitor), Vasile Goldiș (om politic, membru al guvernului care a realizat Unirea de la 1918), Ștefan Augustin Doinaș (poet), Iuliu Maniu (om politic) și mulți alții care au contribuit la patrimoniul cultural și istoric național.", false);
            addSection(container, "Gastronomie", "Bucătăria arădeană îmbină armonios influențe românești, maghiare, germane și sârbești. Specialități precum șnițelul arădean, gulașul, langoșii, plăcinta cu dovleac și vinurile din podgoriile Miniș-Măderat sunt delicii culinare ce pot fi savurate în restaurantele tradiționale din oraș.", false);
        } else if (className.contains("Tulcea")) {
            // Conținut specific pentru Tulcea
            addSection(container, "Introducere", "Tulcea este un important oraș-port situat la poarta de intrare în Delta Dunării, unul dintre cele mai frumoase și biodiversificate ecosisteme naturale din Europa. Este un punct de plecare pentru explorarea minunilor Deltei și un centru cultural și istoric al regiunii Dobrogea.", true);
            addSection(container, "Geografie", "Tulcea este situată în partea de nord a Dobrogei, pe malul drept al Dunării, înainte ca fluviul să se împartă în cele trei brațe care formează Delta. Orașul se întinde pe dealuri domoale care oferă panorame impresionante asupra Dunării și a zonelor umede înconjurătoare.", false);
            addSection(container, "Istorie", "Atestată documentar sub numele de Aegyssus în sec. VII î.Hr., Tulcea a fost pe rând cetate getică, colonie grecească, castru roman și cetate bizantină. De-a lungul secolelor, orașul a fost influențat de numeroase culturi, inclusiv otomană, fiind integrat României moderne în 1878, după Războiul de Independență.", false);
            addSection(container, "Atracții Turistice", "Monumentul Independenței de pe Colnicul Hora, Acvariul și Complexul Muzeal de Științele Naturii Delta Dunării, Muzeul de Artă, Muzeul de Etnografie și Artă Populară, Moscheea Azzizie și numeroasele biserici ortodoxe precum Catedrala Sf. Nicolae sunt doar câteva din atracțiile culturale ale orașului.", false);
            addSection(container, "Cultură", "Tulcea este un oraș multicultural, unde tradițiile românești se împletesc cu cele ale minorităților etnice (lipoveni, turci, tătari, ucraineni). Festivalul Internațional al Dunării, Sărbătoarea Borsului Lipovenesc și alte evenimente culturale anuale reflectă diversitatea orașului.", false);
            addSection(container, "Gastronomie", "Bucătăria tulceană este dominată de preparate din pește de apă dulce și fructe de mare, specifice Deltei Dunării. Borsul lipovenesc, saramura de crap, storceagul și plachiile sunt delicii culinare ce pot fi savurate în restaurantele tradiționale din oraș.", false);
        } else if (className.contains("Cernavoda")) {
            // Conținut specific pentru Cernavodă
            addSection(container, "Introducere", "Cernavodă este un important oraș-port de pe Dunăre, situat în județul Constanța, cunoscut pentru centrala nuclearo-electrică și pentru poziția sa strategică, la capătul vestic al Canalului Dunăre-Marea Neagră.", true);
            addSection(container, "Geografie", "Orașul Cernavodă este situat în partea de vest a județului Constanța, pe malul drept al Dunării, la intersecția acesteia cu Canalul Dunăre-Marea Neagră. Peisajul este dominat de platoul dobrogean și de valea Dunării, oferind panorame spectaculoase asupra fluviului.", false);
            addSection(container, "Istorie", "Zona Cernavodă a fost locuită încă din neolitic, fiind cunoscută pentru cultura Hamangia (descoperirea celebrelor statuete 'Gânditorul' și 'Femeie șezând'). În antichitate, aici se afla cetatea Axiopolis, un important punct strategic și comercial la granița Imperiului Roman. Orașul modern s-a dezvoltat în sec. XIX, odată cu construcția podului peste Dunăre de către inginerul Anghel Saligny (1895), o realizare inginerească remarcabilă pentru acea vreme.", false);
            addSection(container, "Atracții Turistice", "Cernavodă oferă vizitatorilor numeroase puncte de interes: Muzeul de Istorie Națională și Arheologie, unde pot fi admirate artefacte din cultura Hamangia, vechiul pod al lui Saligny (monument istoric), portul comercial, precum și priveliști impresionante asupra Dunării și a Canalului. La 20 km se află situl arheologic Tropaeum Traiani din Adamclisi, un alt obiectiv turistic major din zonă.", false);
            addSection(container, "Cultură", "Viața culturală a Cernavodei este influențată de poziția sa ca centru industrial și energetic, dar păstrează elemente tradiționale dobrogene. Festivalul 'Zilele Cernavodei' este principalul eveniment cultural anual, când orașul găzduiește concerte, expoziții și alte manifestări artistice.", false);
            addSection(container, "Economie", "Economia orașului este dominată de Centrala Nuclearo-Electrică (CNE) Cernavodă, prima și singura centrală nucleară din România, care furnizează aproximativ 20% din electricitatea țării. Portul comercial, industria materialelor de construcții și turismul sunt alte sectoare economice importante.", false);
        } else if (className.contains("Craiova")) {
            // Conținut specific pentru Craiova
            addSection(container, "Introducere", "Craiova, cunoscută și ca 'Orașul Băniei', este cel mai important centru urban din Oltenia și unul dintre cele mai mari orașe din România. Cu o bogată istorie, cultură vibrantă și arhitectură impresionantă, Craiova reprezintă inima economică și culturală a sud-vestului țării.", true);
            addSection(container, "Geografie", "Situată în câmpia Olteniei, pe malul stâng al râului Jiu, Craiova se bucură de un climat temperat-continental cu influențe mediteraneene. Orașul se află la o altitudine medie de 100 m deasupra nivelului mării și este înconjurat de terenuri agricole fertile, fiind un important centru agricol al țării.", false);
            addSection(container, "Istorie", "Originile Craiovei datează din antichitate, zona fiind locuită încă din perioada dacică (Pelendava). Prima mențiune documentară datează din 1475, iar numele orașului este legat de boierii Craioveşti, care au avut un rol important în istoria Țării Românești. În secolele XVIII-XIX, Craiova a fost un important centru comercial și cultural, fiind pentru scurt timp chiar capitala Olteniei sub ocupația austriacă (1718-1739).", false);
            addSection(container, "Atracții Turistice", "Centrul istoric al Craiovei impresionează prin clădirile în stil neoclasic și Art Nouveau. Printre cele mai importante atracții se numără: Parcul Nicolae Romanescu (unul dintre cele mai mari parcuri naturale urbane din Europa), Muzeul de Artă (care adăpostește opere ale lui Constantin Brâncuși), Teatrul Național 'Marin Sorescu', Palatul Jean Mihail (Muzeul de Artă), Casa Băniei (cea mai veche clădire din oraș), Biserica Sf. Dumitru și Grădina Botanică.", false);
            addSection(container, "Cultură", "Craiova este un important centru cultural cu instituții precum Filarmonica 'Oltenia', Teatrul Național 'Marin Sorescu', Teatrul Liric 'Elena Teodorini' și Opera Română Craiova. Orașul găzduiește anual Festivalul Shakespeare și are o bogată tradiție literară, fiind legat de nume precum: Marin Sorescu, Ion D. Sîrbu, Traian Demetrescu și Elena Farago.", false);
            addSection(container, "Gastronomie", "Bucătăria oltenească din Craiova se remarcă prin preparate consistente și gustoase precum: ciorba de potroace, sarmalele oltenești în foi de viță, prazul cu măsline, plachia de fasole, pârjoalele cu mujdei și celebra plăcintă cu brânză (specialitatea locală). Acestea pot fi însoțite de vinuri din podgoriile Segarcea și Drăgășani.", false);
            addSection(container, "Dezvoltare Economică", "În ultimii ani, Craiova a cunoscut o dezvoltare economică semnificativă, fiind un important centru industrial (fabrica Ford), educațional (Universitatea din Craiova) și de servicii. Parcurile industriale din zonă și buna conectivitate cu restul țării au transformat orașul într-un hub economic regional.", false);
        } else if (className.contains("TarguJiu")) {
            // Conținut specific pentru Târgu Jiu
            addSection(container, "Introducere", "Târgu Jiu este reședința județului Gorj, un oraș cu profunde semnificații istorice și culturale, cunoscut în întreaga lume datorită ansamblului sculptural realizat de Constantin Brâncuși. Situat în partea de nord a Olteniei, orașul păstrează un farmec aparte, îmbinând armonios tradițiile locale cu modernitatea.", true);
            addSection(container, "Geografie", "Orașul este așezat pe malurile râului Jiu, la poalele Carpaților Meridionali, într-o zonă de dealuri, având un cadru natural pitoresc. Clima este temperat-continentală cu influențe submediteraneene, oferind veri călduroase și ierni blânde, propice pentru turism pe tot parcursul anului.", false);
            addSection(container, "Istorie", "Târgu Jiu a fost menționat documentar pentru prima dată în 1406, ca târg și punct vamal. În octombrie 1916, aici s-a desfășurat o importantă bătălie din Primul Război Mondial, când forțele române, sub comanda Ecaterinei Teodoroiu, au oprit înaintarea armatei germane, salvând orașul de la ocupație.", false);
            addSection(container, "Ansamblul Brâncuși", "Principala atracție a orașului este Ansamblul Monumental \"Calea Eroilor\" realizat de Constantin Brâncuși, care include: Masa Tăcerii, Poarta Sărutului, Aleea Scaunelor și Coloana Infinitului (Coloana fără Sfârșit). Aceste capodopere ale artei moderne, realizate între 1937-1938, au transformat Târgu Jiu într-un important centru cultural și un punct de atracție pentru turiștii din întreaga lume.", false);
            addSection(container, "Alte Atracții Turistice", "Pe lângă Ansamblul Brâncuși, orașul oferă și alte obiective turistice importante: Muzeul Județean Gorj \"Alexandru Ștefulescu\", Casa Memorială Ecaterina Teodoroiu, Biserica \"Sfinții Apostoli\", Parcul Central, Grădina Publică, precum și numeroase monumente istorice și arhitectonice care reflectă istoria bogată a zonei.", false);
            addSection(container, "Cultură și Tradiții", "Târgu Jiu este un important centru cultural al Olteniei, cu instituții precum Teatrul Dramatic \"Elvira Godeanu\", Școala Populară de Artă, Biblioteca Județeană \"Christian Tell\" și Centrul de Cercetare, Documentare și Promovare \"Constantin Brâncuși\". Tradițiile populare gorjenești sunt păstrate cu sfințenie, fiind reflectate în port, muzică, dans și artizanat.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională gorjenească oferă preparate delicioase precum: ciorbă de fasole cu ciolan afumat, sarmale în foi de viță cu afumătură, plăcintă cu brânză și mărar, păstrăv la grătar cu mămăliguță și diverse preparate din carne de vânat. Vinurile și țuica de prune completează oferta gastronomică locală.", false);
        } else if (className.contains("Bucovina")) {
            // Conținut specific pentru Bucovina
            addSection(container, "Introducere", "Bucovina este o regiune istorică de o frumusețe aparte, situată în nord-estul României, renumită pentru mănăstirile sale pictate, incluse în patrimoniul UNESCO, pentru peisajele montane spectaculoase, dar și pentru tradițiile și obiceiurile populare bine conservate.", true);
            addSection(container, "Geografie", "Situată în nord-estul României, Bucovina este străjuită la vest de Munții Carpați, având un relief predominant muntos și deluros, cu văi pitorești străbătute de râuri cristaline. Peisajul natural impresionează prin frumusețe, puritate și diversitate: păduri de conifere, pășuni alpine, chei și stânci spectaculoase.", false);
            addSection(container, "Istorie", "Numele regiunii provine din limba germană - 'Buchenland' (Țara Fagilor). Bucovina a fost parte a Principatului Moldovei până în 1775, când a fost anexată de Imperiul Habsburgic. După Primul Război Mondial, partea de sud a intrat în componența României, iar după Al Doilea Război Mondial, partea de nord a fost încorporată în Ucraina. Astăzi, termenul 'Bucovina' se referă de obicei la Bucovina de Sud, care aparține României.", false);
            addSection(container, "Mănăstirile Pictate", "Cele mai valoroase comori ale Bucovinei sunt mănăstirile pictate, construite în secolele XV-XVI: Voroneț (cunoscută pentru 'albastrul de Voroneț'), Sucevița, Moldovița, Humor, Arbore și Putna. Aceste capodopere de arhitectură medievală sunt decorate cu fresce exterioare spectaculoase, reprezentând scene biblice și fiind incluse în patrimoniul mondial UNESCO din 1993.", false);
            addSection(container, "Orașe Principale", "Principalele orașe din Bucovina românească sunt Suceava (fosta capitală a Moldovei), Câmpulung Moldovenesc, Gura Humorului, Rădăuți și Vatra Dornei. Fiecare dintre aceste localități are farmecul său specific și obiective turistice importante: cetăți medievale, muzee etnografice, biserici istorice și arhitectură tradițională.", false);
            addSection(container, "Tradiții și Obiceiuri", "Bucovina este una dintre regiunile României unde tradițiile și obiceiurile populare sunt cel mai bine conservate: ouăle încondeiate (realizate prin tehnica încondeierii cu ceară), costumele populare specifice, ceramica neagră de Marginea, lemnul sculptat și țesăturile tradiționale. Sărbătorile religioase, hramurile mănăstirilor și obiceiurile de iarnă (colinde, uratul, jocul caprei, jocul ursului) sunt celebrate cu fast.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională bucovineană este bogată și savuroasă, reflectând influențele diverse din regiune: ciorbe acrișoare, tochitura bucovineană, sarmalele în foi de varză acră, plăcintele poale-n brâu, alivencile (plăcinte cu brânză dulce), cozonaci și produse tradiționale din lapte și carne. Afinata și țuica de prune sunt băuturile tradiționale ale zonei.", false);
            addSection(container, "Turism și Recreere", "Pe lângă turismul cultural și religios, Bucovina oferă multiple posibilități pentru activități recreative: drumeții montane în Munții Rarău și Giumalău, sporturi de iarnă la Gura Humorului și Vatra Dornei, turism rural și agroturism în satele tradiționale, tratamente balneare în stațiunea Vatra Dornei, precum și posibilitatea de a participa la ateliere meșteșugărești și evenimente culturale tradiționale.", false);
        } else if (className.contains("Gura-Humorului")) {
            // Conținut specific pentru Gura Humorului
            addSection(container, "Introducere", "Gura Humorului este un orășel pitoresc din Bucovina, situat într-un cadru natural spectaculos, la poalele Obcinilor Bucovinei. Este renumit ca poartă de intrare către mănăstirile pictate din zonă și ca o stațiune turistică în plină dezvoltare.", true);
            addSection(container, "Geografie", "Situat în județul Suceava, la confluența râurilor Humor și Moldova, orașul Gura Humorului se află la o altitudine de aproximativ 470 m, într-o depresiune înconjurată de dealuri împădurite. Poziția sa geografică privilegiată, între Obcinele Bucovinei și Podișul Sucevei, îi conferă un microclimat plăcut și peisaje naturale deosebite.", false);
            addSection(container, "Istorie", "Prima atestare documentară a localității datează din 1490. În perioada administrației austro-ungare (1775-1918), Gura Humorului a cunoscut o dezvoltare semnificativă, devenind un important centru administrativ și comercial. În 1904 a primit statutul de oraș și s-a dezvoltat ca o stațiune balneo-climaterică frecventată de aristocrația Imperiului.", false);
            addSection(container, "Atracții Turistice", "Principalele atracții din Gura Humorului includ: Muzeul Obiceiurilor Populare din Bucovina, Casa Memorială \"Nicolae Labiș\", arhitectuara specifică perioadei habsburgice, Parcul Ariniș cu pârtia de schi și tiroliana, precum și rezervația naturală Piatra Pinului. La doar câțiva kilometri se află Mănăstirea Humor, cu fresce exterioare spectaculoase, inclusă în patrimoniul UNESCO.", false);
            addSection(container, "Activități Recreative", "Orașul oferă numeroase posibilități pentru petrecerea timpului liber: sporturi de iarnă pe pârtia Șoimul din Parcul Ariniș, aventură la tiroliana Voronețul Fly (una dintre cele mai lungi din Europa), drumeții pe trasee montane marcate, plimbări cu bicicleta, călărie și pescuit sportiv pe râul Moldova.", false);
            addSection(container, "Cultură și Evenimente", "Viața culturală a orașului este animată de multiple evenimente anuale: Festivalul Internațional de Film, Umor și Satiră \"Humor... la Gura Humorului\", Festivalul de Datini și Obiceiuri de Iarnă, Zilele Orașului și diverse expoziții și spectacole organizate la Casa de Cultură și Muzeul Obiceiurilor Populare.", false);
            addSection(container, "Gastronomie", "În restaurantele și pensiunile din Gura Humorului se pot savura specialități bucovinene autentice: tochitura bucovineană, ciorba rădăuțeană, sarmalele în foi de varză, pastrama de oaie cu mămăligă, bulzul (brânză la ceaun cu mămăligă) și diverse dulciuri traditionale precum alivencile și cozonacii. Producția locală de afine și fructe de pădure se regăsește în gemuri, siropuri și afinată tradițională.", false);
        } else if (className.contains("Vatra-Dornei")) {
            // Conținut specific pentru Vatra Dornei
            addSection(container, "Introducere", "Vatra Dornei este o renumită stațiune balneo-climaterică din sudul Bucovinei, cunoscută pentru apele minerale cu proprietăți terapeutice, aerul curat și peisajele montane spectaculoase. Numită și \"Perla Bucovinei\", stațiunea combină facilitățile de tratament cu multiple oportunități pentru turism activ și sportiv.", true);
            addSection(container, "Geografie", "Situată în județul Suceava, la confluența râurilor Dorna și Bistrița Aurie, la o altitudine de aproximativ 800 m, Vatra Dornei este înconjurată de masivele muntoase Suhard, Giumalău, Călimanilor și Bistriței. Această poziționare într-o depresiune intramontană îi conferă un climat subalpin cu proprietăți terapeutice, caracterizat prin aer pur, bogat în aerosoli și ionizat negativ.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1592. Dezvoltarea localității ca stațiune balneară a început în perioada administrației austro-ungare (1775-1918), când Bucovina făcea parte din Imperiul Habsburgic. În această perioadă au fost construite primele stabilimente balneare, hoteluri și vile în stil arhitectonic specific, care dau și astăzi farmec orașului. Vatra Dornei a devenit una dintre cele mai elegante stațiuni din Imperiul Austro-Ungar, frecventată de aristocrația vremii.", false);
            addSection(container, "Turism Balnear", "Stațiunea este renumită pentru apele minerale carbogazoase, feruginoase, bicarbonatate, sodice, calcice și magneziene, precum și pentru nămolul de turbă, utilizate în tratarea afecțiunilor cardiovasculare, digestive, endocrine și reumatismale. Baza de tratament dispune de facilități moderne pentru băi carbogazoase, mofete, împachetări cu nămol, electroterapie, hidroterapie și kinetoterapie.", false);
            addSection(container, "Sporturi de Iarnă", "Vatra Dornei este o importantă stațiune montană pentru sporturi de iarnă, cu pârtii de schi pentru toate categoriile de schiori: pârtia Parc, pârtia Dealul Negru și pârtia Veverița. Stațiunea dispune de instalații de transport pe cablu moderne, școli de schi și snowboard, precum și trasee pentru schi fond și săniuș. În sezonul rece, Vatra Dornei găzduiește competiții naționale și internaționale de sporturi de iarnă.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Cazinoul (clădire emblematică în stil baroc, construită în 1898), Muzeul Etnografic \"Gheorghe Șmira\", vechile vile și hoteluri în stil austro-ungar, Biserica Romano-Catolică (1905), Parcul Central cu foișorul de muzică și izvoarele minerale amenajate. În împrejurimi se află rezervațiile naturale Tinovul Mare și Parcul Național Călimani.", false);
            addSection(container, "Activități în Aer Liber", "Pe lângă sporturile de iarnă, Vatra Dornei oferă multiple posibilități pentru activități recreative: drumeții montane pe trasee marcate către vârfurile din jur, mountain biking, echitație, pescuit sportiv pe râurile Dorna și Bistrița, rafting și canyoning. Pădurile din împrejurimi sunt ideale pentru observarea faunei sălbatice și culesul ciupercilor și fructelor de pădure.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională dorneană oferă specialități locale precum: tochitura bucovineană cu mămăligă, ciorba rădăuțeană, sarmalele în foi de varză, pastrama de oaie, bulzul, afinata și diverse preparate din trufe, ciuperci și fructe de pădure. Brânzeturile locale (cașcaval, urdă, caș) și produsele apicole (miere, polen, propolis) sunt recunoscute pentru calitatea lor deosebită.", false);
        } else if (className.contains("Campulung-Moldovenesc")) {
            // Conținut specific pentru Câmpulung Moldovenesc
            addSection(container, "Introducere", "Câmpulung Moldovenesc este un orășel pitoresc din Bucovina, situat într-o depresiune de-a lungul văii râului Moldova, înconjurat de păduri de conifere și dealuri domoale. Este un important centru etnografic și cultural, păstrător al tradițiilor și meșteșugurilor bucovinene.", true);
            addSection(container, "Geografie", "Situat în județul Suceava, în partea de sud a Bucovinei, Câmpulung Moldovenesc se află la o altitudine de aproximativ 630 m, într-o depresiune intramontană între masivele Rarău și Giumalău. Poziția geografică și relieful variat, cu munți, dealuri și valea largă a Moldovei, oferă peisaje naturale spectaculoase și multiple oportunități pentru activități în aer liber.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1411, într-un document emis de domnitorul Alexandru cel Bun. Numele localității provine din configuraţia văii râului Moldova, care formează aici un \"câmp lung\". În perioada administrației austro-ungare (1775-1918), Câmpulung a cunoscut o dezvoltare semnificativă, devenind un important centru administrativ, meșteșugăresc și comercial al Bucovinei.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Muzeul \"Arta Lemnului\" (unic în România prin colecțiile sale de unelte tradiționale și obiecte din lemn), Muzeul Etnografic \"Ioan Grămadă\", Biserica Romano-Catolică (1864), Biserica Adormirea Maicii Domnului, vechile clădiri administrative și vile în stil austriac, precum și Parcul Central cu arhitectura sa specifică perioadei habsburgice.", false);
            addSection(container, "Masivul Rarău", "La aproximativ 15 km de Câmpulung se află Masivul Rarău, o destinație excepțională pentru iubitorii naturii. Aici se găsesc formațiuni geologice spectaculoase precum Pietrele Doamnei, rezervații naturale importante, o biodiversitate remarcabilă și trasee de drumeție pentru toate nivelurile de dificultate. Mănăstirea Rarău, situată la altitudine, oferă o priveliște panoramică impresionantă.", false);
            addSection(container, "Tradiții și Meșteșuguri", "Câmpulungul este cunoscut ca un important centru al meșteșugurilor tradiționale bucovinene: prelucrarea artistică a lemnului, țesutul, broderia, înconjoarea ouălor și prelucrarea fibrelor textile. Muzeul \"Arta Lemnului\" și atelierele meșterilor populari locali păstrează și promovează aceste tradiții seculare. Festivaluri precum \"Târgul Lăptarilor\" și \"Bujorul de Munte\" celebrează tradițiile pastorale și folclorice ale zonei.", false);
            addSection(container, "Turism Activ", "Zona oferă multiple posibilități pentru activități în aer liber: drumeții montane pe trasee marcate în Masivul Rarău, Munții Giumalău și Obcinele Bucovinei, sporturi de iarnă la pârtia Rarău, mountain biking, echitație, pescuit sportiv pe râul Moldova și parapantă de pe vârful Rarău. Peșterile din zonă, precum Peștera Liliecilor, sunt destinații apreciate de speologi.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională din Câmpulung Moldovenesc oferă delicii culinare precum: tochitura bucovineană, ciorba rădăuțeană, sarmalele moldovenești, cozonacul bucovinean, alivencile (plăcinte cu brânză dulce) și preparate din produse locale (ciuperci, fructe de pădure, brânzeturi de la stânele din Rarău). Afinata și țuica de prune sunt băuturile tradiționale locale.", false);
        } else if (className.contains("Radauti")) {
            // Conținut specific pentru Rădăuți
            addSection(container, "Introducere", "Rădăuți este unul dintre cele mai vechi orașe din Bucovina, cu o bogată istorie și tradiție. Situat într-o zonă de câmpie, orașul este renumit pentru hergheliile sale de cai, pentru mănăstirea medievală și pentru tradițiile culturale bine conservate.", true);
            addSection(container, "Geografie", "Situat în nordul județului Suceava, la doar 12 km de granița cu Ucraina, Rădăuți se află într-o zonă de câmpie înaltă, străbătută de râul Topliţa. Poziția geografică a favorizat dezvoltarea orașului ca un important centru comercial și meșteșugăresc al zonei, cu acces facil către mănăstirile pictate din Bucovina.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1392, când este menționată Mănăstirea Bogdana (actuala Biserică Sf. Nicolae), ctitorită de Bogdan I, primul domn al Moldovei. Rădăuți a fost prima reședință episcopală a Moldovei, având o importanță religioasă și culturală deosebită. În perioada administrației austro-ungare (1775-1918), orașul a cunoscut o dezvoltare semnificativă, mai ales datorită hergheliei imperiale înființate aici.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Biserica \"Sf. Nicolae\" Bogdana (cea mai veche construcție religioasă din piatră din Moldova, unde sunt înmormântați primii voievozi moldoveni), Muzeul Etnografic \"Samuil și Eugenia Ioneţ\", Muzeul Tehnicii Populare, Sinagoga Mare, Templul Evreiesc și Herghelia Rădăuți (una dintre cele mai vechi din România, fondată în 1792 de habsburgi).", false);
            addSection(container, "Tradiții și Cultură", "Rădăuți este cunoscut ca un important centru al tradițiilor populare bucovinene: ceramica, lemnul sculptat, ouăle încondeiate, țesăturile și portul popular specific. \"Udeștii bătrâni\", una dintre cele mai vechi și valoroase forme de dans popular bucovinean, își are originea în această zonă. Festivaluri precum \"Arcanul\" și \"Zilele Rădăuțiului\" celebrează aceste tradiții bogate.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională din Rădăuți este renumită în toată țara prin \"ciorba rădăuțeană\" - o specialitate locală pe bază de legume, carne de pui și smântână, cu un gust unic datorat sosului de usturoi. Alte delicii locale includ tochitura bucovineană, sarmalele în foi de varză, plăcintele poale-n brâu, cozonacul bucovinean și diverse preparate din produse locale.", false);
            addSection(container, "Comunitatea Evreiască", "Înainte de Al Doilea Război Mondial, Rădăuți avea o comunitate evreiască înfloritoare, care a contribuit semnificativ la dezvoltarea economică și culturală a orașului. Patrimoniul evreiesc este reprezentat de Sinagoga Mare (construită în 1883), Templul Evreiesc, cimitirul evreiesc și diverse clădiri cu arhitectură specifică. Astăzi, comunitatea evreiască este foarte redusă ca număr, dar memoria sa este păstrată prin diverse proiecte culturale și evenimente comemorative.", false);
            addSection(container, "Împrejurimi", "Rădăuți este un punct de plecare ideal pentru explorarea mănăstirilor pictate din Bucovina (Putna, Sucevița, Moldovița, Arbore), toate situate la mai puțin de o oră de mers cu mașina. De asemenea, în apropiere se află rezervații naturale precum Codrii Seculari de la Slătioara și atracții turistice precum Cetatea de Scaun a Sucevei și Marginea (cunoscută pentru ceramica neagră).", false);
        } else if (className.contains("DrobetaTurnuSeverin") || className.contains("Drobeta")) {
            // Conținut specific pentru Drobeta-Turnu Severin
            addSection(container, "Introducere", "Drobeta-Turnu Severin, reședința județului Mehedinți, este un important oraș-port la Dunăre, cu o istorie de peste 1900 de ani. Situat la granița cu Serbia, orașul reprezintă un important punct strategic și comercial, fiind și o poartă de intrare în România.", true);
            addSection(container, "Geografie", "Așezat pe malul stâng al Dunării, în extremitatea sud-vestică a României, orașul beneficiază de un climat blând, submediteranean. Dunărea, care formează aici defileul Porțile de Fier, oferă peisaje spectaculoase și oportunități pentru activități nautice și de agrement.", false);
            addSection(container, "Istorie Antică", "Istoria orașului începe în anul 105 d.Hr., când împăratul Traian a construit aici podul peste Dunăre, proiectat de arhitectul Apolodor din Damasc, pentru a facilita cucerirea Daciei. Castrul roman Drobeta, construit pentru apărarea podului, a evoluat într-un important centru urban și militar al Imperiului Roman.", false);
            addSection(container, "Evoluție Istorică", "După perioada romană, localitatea a continuat să existe sub diverse nume. În Evul Mediu, regele maghiar Ladislau I a construit aici o cetate numită Severin (de unde și partea a doua a numelui actual). Orașul modern s-a dezvoltat după 1833, când a devenit port liber, cunoscând o expansiune economică importantă în perioada următoare.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: ruinele Podului lui Traian și Castrul Roman, Muzeul Regiunii Porților de Fier (unul dintre cele mai mari din România), Cetatea Medievală a Severinului, Castelul de Apă, Parcul Arheologic și Palatul Cultural \"Teodor Costescu\". La câțiva kilometri se află spectaculosul defileu Porțile de Fier și Hidrocentrala omonimă.", false);
            addSection(container, "Cultură", "Viața culturală a orașului este reprezentată de instituții precum Palatul Culturii \"Teodor Costescu\", Teatrul \"Teodor Costescu\", Biblioteca Județeană \"I.G. Bibicescu\" și Casa de Cultură a Sindicatelor. Festivalurile locale celebrează atât istoria romană a orașului (Festivalul Antic Drobeta), cât și tradițiile populare mehedințene.", false);
            addSection(container, "Economie", "Economia orașului se bazează pe industria navală (Șantierul Naval Drobeta), producția de energie (Hidrocentrala Porțile de Fier I), industria materialelor de construcții, industria ușoară și turism. Portul Drobeta-Turnu Severin este un important punct de tranzit pentru mărfuri pe Dunăre.", false);
        } else if (className.contains("RamnicuValcea") || className.contains("Valcea")) {
            // Conținut specific pentru Râmnicu Vâlcea
            addSection(container, "Introducere", "Râmnicu Vâlcea, cunoscut și ca \"orașul dintre vii\", este reședința județului Vâlcea și un important centru cultural, economic și turistic din nordul Olteniei. Așezat într-un cadru natural deosebit, la poalele Carpaților Meridionali, orașul oferă o îmbinare armonioasă între tradiție și modernitate.", true);
            addSection(container, "Geografie", "Orașul este situat pe malul drept al râului Olt, la confluența acestuia cu râul Olănești, într-o depresiune înconjurată de dealuri acoperite cu livezi și vii. Poziția sa geografică privilegiată, la intersecția unor importante căi de comunicație, a contribuit la dezvoltarea sa ca centru comercial și cultural.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1388, într-un document emis de voievodul Mircea cel Bătrân. De-a lungul secolelor, Râmnicu Vâlcea a jucat un rol important în istoria Țării Românești, fiind menționat ca un centru comercial și meșteșugăresc important, precum și un loc unde s-au tipărit cărți bisericești de valoare.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Parcul Zăvoi (unul dintre cele mai vechi parcuri naturale din România), Muzeul Județean de Istorie, Muzeul de Artă \"Casa Simian\", Complexul Muzeal Etnografic Bujoreni, Filarmonica \"Ion Dumitrescu\", Episcopia Râmnicului cu Catedrala Episcopală și biblioteca sa cu cărți rare. În apropiere se află stațiunile balneoclimaterice Călimănești-Căciulata, Băile Olănești și Băile Govora.", false);
            addSection(container, "Cultură", "Orașul are o bogată tradiție culturală, fiind un important centru tipografic în secolele XVIII-XIX. Astăzi, viața culturală este reprezentată de instituții precum Teatrul Municipal \"Ariel\", Filarmonica \"Ion Dumitrescu\", Biblioteca Județeană \"Antim Ivireanul\" și Casa de Cultură a Sindicatelor. Festivalul Internațional de Folclor \"Cântecele Oltului\" și Festivalul de Teatru \"Ariel Inter Fest\" sunt evenimente culturale importante.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională vâlceană se remarcă prin preparate precum: ciorbă de potroace, pastramă cu mămăliguță, sarmale în foi de viță, brânzeturi și produse lactate din zonele montane, precum și vinurile din podgoriile locale (Drăgășani). Dulcețurile și gemurile naturale, precum și pălinca de fructe, sunt alte specialități locale.", false);
            addSection(container, "Economie", "Economia județului Vâlcea și implicit a municipiului Râmnicu Vâlcea se bazează pe industria chimică, industria alimentară, exploatarea și prelucrarea lemnului, turism și servicii. Orașul este și un important nod rutier, fiind situat pe coridorul european care leagă București de Transilvania și Europa Centrală.", false);
        } else if (className.contains("Slatina")) {
            // Conținut specific pentru Slatina
            addSection(container, "Introducere", "Slatina, reședința județului Olt, este un oraș cu o bogată istorie și tradiție, situat în sudul Olteniei. Cunoscut pentru industria aluminiului și pentru peisajul pitoresc oferit de râul Olt care traversează orașul, Slatina îmbină armonios patrimoniul istoric cu dezvoltarea industrială modernă.", true);
            addSection(container, "Geografie", "Orașul este așezat pe malul stâng al râului Olt, în zona de câmpie a Olteniei. Relieful predominant de câmpie este întrerupt de terasele Oltului, care oferă panorame deosebite asupra râului și împrejurimilor. Clima temperat-continentală, cu influențe submediteraneene, oferă veri călduroase și ierni relativ blânde.", false);
            addSection(container, "Istorie", "Prima atestare documentară a Slatinei datează din 20 ianuarie 1368, într-un hrisov emis de Vladislav I Vlaicu. De-a lungul secolelor, orașul s-a dezvoltat ca un important centru comercial și meșteșugăresc. În epoca modernă, Slatina a devenit un centru industrial important, mai ales după construirea Uzinei de Aluminiu în anii 1960-1970.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Centrul Istoric cu arhitectura sa specifică din secolele XIX-XX, Muzeul Județean Olt, Casa Memorială \"Eugen Ionescu\" (părinții dramaturgului s-au născut în Slatina), Parcul \"Eugen Ionescu\", Catedrala \"Sf. Gheorghe\", Podul Metalic peste Olt (monument de arhitectură industrială), precum și malurile amenajate ale râului Olt.", false);
            addSection(container, "Cultură", "Viața culturală a orașului gravitează în jurul instituțiilor precum Centrul Cultural \"Eugen Ionescu\", Biblioteca Județeană \"Ion Minulescu\", Muzeul Județean Olt și Școala Populară de Arte și Meserii. Festivaluri precum \"Oltenii și Restu' Lumii\" și \"Eugen Ionescu\" atrag anual numeroși participanți și turiști, celebrând tradițiile locale și personalitățile născute în zonă.", false);
            addSection(container, "Personalități", "Din Slatina sau din județul Olt s-au ridicat personalități importante ale culturii române: Eugen Ionescu (părinții săi), poetul Ion Minulescu, istoricul și criticul literar Petre Pandrea, scriitorul Dumitru Caracostea, filozoful Petre Țuțea și mulți alții, care au contribuit la patrimoniul cultural național.", false);
            addSection(container, "Economie", "Economia orașului este dominată de industria aluminiului, Slatina fiind cunoscută drept \"Orașul Aluminiului\" datorită ALRO - cel mai mare producător de aluminiu din Europa Centrală și de Est. Alte sectoare importante sunt industria construcțiilor de mașini, industria textilă, industria alimentară și comerțul.", false);
        } else if (className.contains("Oltenia")) {
            // Conținut specific pentru Oltenia
            addSection(container, "Introducere", "Oltenia este o regiune istorică situată în sud-vestul României, între Carpați la nord, Dunărea la sud și vest, și Olt la est. Este cunoscută pentru peisajele sale spectaculoase, monumentele istorice și tradițiile sale bogate.", true);
            addSection(container, "Geografie", "Oltenia are un relief variat, de la Carpații Meridionali la nord, cu vârfuri impresionante precum Parângul Mare (2.519 m), până la Câmpia Olteniei în sud. Principalele râuri care străbat regiunea sunt Oltul și Jiul.", false);
            addSection(container, "Istorie", "Oltenia are o istorie bogată, fiind parte din Dacia, apoi provincia romană Dacia Inferior. În perioada medievală a fost parte din Țara Românească, iar între 1718-1739 a fost sub ocupație austriacă, fiind cunoscută sub numele de 'Oltenia imperială'.", false);
            addSection(container, "Atracții Turistice", "Ansamblul sculptural Brâncuși din Târgu Jiu, Mănăstirea Tismana, Mănăstirea Horezu (patrimoniu UNESCO), Cheile Oltețului și stațiunile balneo-climaterice Călimănești-Căciulata și Băile Olănești sunt destinații populare.", false);
            addSection(container, "Cultură", "Olăritul de Horezu, muzica și dansurile populare oltenești, costumele populare viu colorate și bucătăria specifică sunt elemente reprezentative ale culturii oltenești.", false);
        } else if (className.contains("Moldova")) {
            // Conținut specific pentru Moldova
            addSection(container, "Introducere", "Moldova este o regiune istorică situată în nord-estul României, cu o identitate culturală distinctă și peisaje naturale impresionante.", true);
        } else if (className.contains("Suceava")) {
            // Conținut specific pentru Suceava
            addSection(container, "Introducere", "Suceava, fosta capitală a Moldovei medievale, este un oraș cu o bogată istorie și un important centru cultural în nordul României. Cetatea de Scaun, mănăstirile pictate din împrejurimi și arhitectura tradițională bucovineană fac din această zonă o destinație turistică deosebită.", true);
            addSection(container, "Geografie", "Orașul Suceava este situat în nord-estul României, pe malurile râului Suceava, într-o zonă deluroasă, la poalele Obcinilor Bucovinei. Poziția sa geografică, la intersecția unor importante căi comerciale istorice, a contribuit la dezvoltarea sa ca centru administrativ și cultural al regiunii.", false);
            addSection(container, "Istorie", "Suceava a fost capitala Principatului Moldovei între secolele XIV-XVI, cunoscând apogeul sub domnia lui Ștefan cel Mare (1457-1504). Cetatea de Scaun a Sucevei, fortificată de Ștefan cel Mare, a fost principala reședință domnească și simbolul puterii Moldovei medievale. După mutarea capitalei la Iași în secolul XVI, orașul și-a păstrat importanța ca centru comercial și cultural.", false);
            addSection(container, "Atracții Turistice", "Cetatea de Scaun a Sucevei, unul dintre cele mai importante monumente istorice din România, Biserica Sfântul Gheorghe (patrimoniu UNESCO), Muzeul Național al Bucovinei, Muzeul Satului Bucovinean, Hanul Domnesc și centrul istoric cu arhitectura sa specifică sunt principalele atracții ale orașului. În împrejurimi se află mănăstirile pictate din Bucovina (patrimoniu UNESCO): Voroneț, Sucevița, Moldovița, Humor și Arbore.", false);
            addSection(container, "Cultură", "Suceava este un important centru cultural, cu instituții precum Universitatea 'Ștefan cel Mare', Teatrul Municipal 'Matei Vișniec', Muzeul Național al Bucovinei și Biblioteca Bucovinei 'I.G. Sbiera'. Festivalul de Artă Medievală din Cetatea Sucevei și numeroase alte evenimente culturale păstrează vie tradiția istorică a orașului.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională bucovineană oferă preparate delicioase precum: borș de pui cu tăiței de casă, tochitura bucovineană, balmoș (mămăligă cu brânză și smântână), sarmale în foi de varză acră și diverse prăjituri tradiționale precum poale-n brâu. Produsele locale, precum brânzeturile de la stânele bucovinene și mierea de albine, completează oferta gastronomică a zonei.", false);
        } else if (className.contains("Bacau")) {
            // Conținut specific pentru Bacău
            addSection(container, "Introducere", "Bacău este un important centru urban și industrial din regiunea Moldovei, situat pe râul Bistrița. Cu o istorie de peste șase secole și o economie diversificată, orașul reprezintă un nod important de transport și un centru cultural pentru zona centrală a Moldovei.", true);
            addSection(container, "Geografie", "Orașul Bacău este situat în partea central-vestică a Moldovei, pe malul drept al râului Bistrița, la confluența acestuia cu Siretul. Așezat la contactul dintre Podișul Moldovei și Subcarpații Orientali, orașul se bucură de un cadru natural variat, cu dealuri line, lunci fertile și păduri în împrejurimi.", false);
            addSection(container, "Istorie", "Prima atestare documentară a Bacăului datează din 6 octombrie 1408, într-un document emis de Alexandru cel Bun. De-a lungul secolelor, orașul s-a dezvoltat ca un important centru comercial la intersecția drumurilor care legau Transilvania de Moldova. În perioada modernă, începând cu a doua jumătate a secolului XIX, Bacăul a cunoscut o dezvoltare industrială și economică semnificativă.", false);
            addSection(container, "Atracții Turistice", "Printre obiectivele turistice importante se numără: Casa Memorială 'Vasile Alecsandri' din Mircești (la aproximativ 30 km), Casa Memorială 'George Bacovia', Observatorul Astronomic 'Victor Anestin', Muzeul de Științe ale Naturii 'Ion Borcea', Muzeul de Artă Contemporană 'George Apostu', Muzeul de Istorie și Arheologie, precum și Parcul Cancicov și Insula de Agrement.", false);
            addSection(container, "Cultură", "Viața culturală este reprezentată de instituții precum Teatrul Municipal 'Bacovia', Filarmonica 'Mihail Jora', Teatrul de Animație pentru Copii și Tineret 'Vasile Alecsandri' și Biblioteca Județeană 'Costache Sturdza'. Bacăul este locul de naștere al poetului simbolist George Bacovia, al cărui nume este purtat de multiple instituții culturale din oraș.", false);
            addSection(container, "Personalități", "Bacăul a dat țării personalități de seamă precum: George Bacovia (poet simbolist), Nicu Enea (pictor), Solomon Marcus (matematician), Radu Beligan (actor), Gabriela Adameșteanu (scriitoare) și mulți sportivi renumiți precum Cristina Grigoraș (gimnastă) și Doina Melinte (atletă).", false);
            addSection(container, "Economie", "Economia orașului este dominată de industria alimentară, industria textilă, industria aeronautică (Aerostar Bacău), industria materialelor de construcții și sectorul serviciilor. De asemenea, Bacăul este un important nod feroviar și rutier, facilitând legăturile comerciale între diferite regiuni ale țării.", false);
        } else if (className.contains("Iasi")) {
            // Conținut specific pentru Iași
            addSection(container, "Introducere", "Iași, cunoscut și ca 'Orașul celor șapte coline', este unul dintre cele mai importante centre culturale, economice și academice ale României. Fostă capitală a Principatului Moldovei, Iași impresionează prin bogăția sa istorică, patrimoniul arhitectural și viața culturală vibrantă.", true);
            addSection(container, "Geografie", "Situat în nord-estul României, în regiunea istorică Moldova, Iașiul este construit pe șapte coline, oferind panorame spectaculoase. Străbătut de râul Bahlui, orașul beneficiază de un cadru natural plăcut, cu numeroase parcuri și spații verzi, precum Grădina Botanică și Copou.", false);
            addSection(container, "Istorie", "Prima atestare documentară a orașului datează din 1408. Iași a devenit capitala Principatului Moldovei în 1565, păstrând acest statut până la Unirea Principatelor din 1859. Orașul a jucat un rol crucial în istoria României: aici s-a format primul guvern unificat al României în 1859, a servit ca refugiu pentru administrația română în timpul Primului Război Mondial și a fost capitala României între 1916-1918.", false);
            addSection(container, "Atracții Culturale", "Palatul Culturii (ce adăpostește patru muzee importante), Teatrul Național 'Vasile Alecsandri' (primul teatru național din România), Universitatea 'Alexandru Ioan Cuza' (prima universitate modernă din țară), Biserica Trei Ierarhi, Catedrala Mitropolitană și Mănăstirea Golia sunt doar câteva dintre comorile arhitectonice și culturale ale orașului.", false);
            addSection(container, "Viața Academică", "Iașiul este un centru universitar de renume, cu peste 60.000 de studenți la cele cinci universități de stat și multiple universități private. Universitatea 'Alexandru Ioan Cuza', Universitatea Tehnică 'Gheorghe Asachi', Universitatea de Medicină și Farmacie 'Grigore T. Popa' atrag studenți din întreaga țară și din străinătate, oferind o viață studențească vibrantă.", false);
            addSection(container, "Cultură și Artă", "Orașului este considerat capitala culturală a României, găzduind primul teatru național, prima universitate modernă și unele dintre cele mai vechi muzee și biblioteci din țară. Festivalul Internațional de Literatură și Traducere (FILIT), Festivalul Internațional de Teatru pentru Publicul Tânăr (FITPT) și numeroase expoziții și concerte animă viața culturală a orașului pe tot parcursul anului.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională moldovenească poate fi savurată în numeroasele restaurante din Iași. Specialități precum borșul moldovenesc, sarmalele în foi de viță, poalele-n brâu (plăcintă cu brânză dulce), cozonacul moldovenesc și vinurile din podgoriile Cotnari și Bucium sunt delicii care reflectă bogăția și diversitatea gastronomică a regiunii.", false);
        } else if (className.contains("Timisoara")) {
            // Conținut specific pentru Timișoara
            addSection(container, "Introducere", "Timișoara este considerat capitala Banatului și un important centru cultural și economic. Orașul este cunoscut pentru arhitectura sa eclectică, numeroasele parcuri și piețe, precum și pentru rolul său istoric în declanșarea Revoluției din 1989. În 2023, Timișoara a fost Capitală Culturală Europeană.", true);
            addSection(container, "Geografie", "Situat în vestul României, în câmpia Banatului, orașul este străbătut de râul Bega. Având un climat temperat-continental cu influențe mediteraneene, Timișoara se bucură de ierni blânde și veri călduroase, fiind unul dintre cele mai verzi orașe din România, cu numeroase parcuri și grădini.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1212. De-a lungul secolelor, Timișoara a fost sub diverse stăpâniri: otomană (1552-1716), habsburgică (1716-1918) și parte a României Mari după 1918. Un moment de referință este 16 decembrie 1989, când aici a izbucnit revoluția care a dus la căderea regimului comunist.", false);
            addSection(container, "Arhitectură", "Timișoara impresionează prin diversitatea stilurilor arhitecturale: baroc, art nouveau, secesion și modernism. Piața Unirii, Piața Victoriei și Piața Libertății formează un ansamblu urban de excepție. Domul Romano-Catolic, Palatul Baroc, Opera Națională și clădirile istorice din Cetate oferă o experiență arhitecturală unică.", false);
            addSection(container, "Cultură", "Primul oraș european cu iluminat stradal electric (1884), Timișoara este un important centru cultural cu Opera Română, Teatrul Național, Filarmonica Banatul și numeroase muzee. Festivaluri precum JazzTM, PLAI, Festivalul de Operă și Operetă și Festivalul Filmului European animă viața culturală a orașului.", false);
            addSection(container, "Educație", "Timișoara este un centru universitar important, găzduind Universitatea de Vest, Universitatea Politehnica, Universitatea de Medicină și alte instituții de învățământ superior care atrag studenți din întreaga țară și din străinătate.", false);
            addSection(container, "Gastronomie", "Bucătăria bănățeană reflectă caracterul multicultural al regiunii. Specialități precum papricașul, supele consistente, sarmalele, cârnaţii bănăţeni și prăjiturile tradiționale germane și austro-ungare pot fi savurate în restaurantele locale.", false);
        } else if (className.contains("Resita")) {
            // Conținut specific pentru Reșița
            addSection(container, "Introducere", "Reșița este unul dintre cele mai importante centre industriale din Banat, cu o tradiție în metalurgie și construcții de mașini. Orașul este înconjurat de dealuri și păduri, oferind și oportunități pentru turism în natură.", true);
            addSection(container, "Geografie", "Situat în sud-vestul României, în județul Caraș-Severin, orașul se află în Depresiunea Reșiței, fiind înconjurat de Munții Semenic și Munții Dognecei. Râul Bârzava străbate orașul, contribuind la dezvoltarea sa industrială istorică datorită forței apei.", false);
            addSection(container, "Istorie", "Istoria Reșiței este strâns legată de dezvoltarea industriei metalurgice. Prima mențiune documentară datează din 1673, dar dezvoltarea modernă începe în 1771, când Imperiul Habsburgic a construit primele furnale pentru producția de fontă. De-a lungul secolelor, Reșița a devenit un simbol al industrializării în România.", false);
            addSection(container, "Patrimoniu Industrial", "Reșița este un adevărat muzeu în aer liber al patrimoniului industrial. Locomotive cu abur fabricate aici, vechile furnale, uzina cocso-chimică și alte instalații industriale conservate reprezintă atracții unice pentru pasionații de arheologie industrială.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Muzeul de Locomotive cu Abur (unic în Sud-Estul Europei), Muzeul Banatului Montan, Biserica Romano-Catolică Maria Zăpezii, Biserica Ortodoxă de pe Dealul Crucii și zona pietonală din centrul orașului. În apropiere se află Stațiunea Secu, Lacul Secu și Munții Semenic.", false);
            addSection(container, "Cultură", "Viața culturală a orașului este reprezentată de instituții precum Teatrul de Vest, Casa de Cultură a Sindicatelor și Biblioteca Județeană „Paul Iorgovici. Festivalul Internațional de Jazz și Festivalul Berii sunt evenimente anuale care atrag numeroși participanți.", false);
            addSection(container, "Natură și Recreere", "Împrejurimile Reșiței oferă numeroase oportunități pentru activități în aer liber: drumeții montane în Munții Semenic, sporturi de iarnă la Stațiunea Semenic, ciclism montan, pescuit și agrement nautic pe Lacul Secu, precum și drumeții la Cheile Caraşului și Peștera Comarnic.", false);
        } else if (className.contains("Lugoj")) {
            // Conținut specific pentru Lugoj
            addSection(container, "Introducere", "Lugoj este un important centru cultural din Banat, cu o bogată istorie și arhitectură reprezentativă. Este cunoscut ca orașul lui Traian Vuia și pentru podul de fier, unul dintre simbolurile localității.", true);
            addSection(container, "Geografie", "Situat în partea de est a județului Timiș, pe malurile râului Timiș, Lugojul se bucură de o poziție geografică privilegiată, la contactul dintre câmpia Banatului și dealurile Poiana Ruscă. Orașul este împărțit în două de râul Timiș: Lugojul Român (pe malul drept) și Lugojul German (pe malul stâng).", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1334. Orașul a jucat un rol important în istoria românilor bănățeni, fiind un centru al luptei pentru emancipare națională în secolul al XIX-lea. În 1848, aici s-a constituit Legiunea Românească din Banat. Lugojul a fost, de asemenea, sediul Episcopiei Greco-Catolice a Lugojului începând cu 1853.", false);
            addSection(container, "Personalități", "Lugojul este orașul natal al lui Traian Vuia, inventator și pionier al aviației mondiale, care a realizat primul zbor autopropulsat cu un aparat mai greu decât aerul. Alte personalități importante născute aici includ pe: Ion Vidu (compozitor), Filaret Barbu (compozitor), Aurel Vlaicu (aviator) și Caius Brediceanu (diplomat).", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Catedrala Greco-Catolică „Coborârea Sfântului Spirit, Biserica Ortodoxă „Adormirea Maicii Domnului, Teatrul Municipal „Traian Grozăvescu, Muzeul de Istorie și Etnografie, Casa Bredicenilor, Podul de Fier (1902) și Monumentul lui Traian Vuia. Centrul istoric al orașului păstrează clădiri în stil baroc și secesion.", false);
            addSection(container, "Cultură", "Lugojul este considerat un important centru cultural al Banatului, cu o bogată tradiție muzicală. Corul „Ion Vidu, înființat în 1810, este unul dintre cele mai vechi coruri din România. Teatrul Municipal, Biblioteca Municipală și Casa de Cultură a Sindicatelor sunt principalele instituții culturale. Festivalul Internațional de Folclor „Ana Lugojana și Zilele Lugojului sunt evenimente culturale anuale importante.", false);
            addSection(container, "Tradiții", "Tradițiile lugojene îmbină elemente românești cu influențe germane, maghiare și sârbești, reflectând caracterul multicultural al Banatului. Târgurile tradiționale, festivalurile folclorice și meșteșugurile locale (olărit, țesut, sculptura în lemn) sunt încă păstrate și promovate.", false);
        } else if (className.contains("Caransebes")) {
            // Conținut specific pentru Caransebeș
            addSection(container, "Introducere", "Caransebeș este un oraș cu o istorie bogată, situat la confluența râurilor Timiș și Sebeș. Centrul său istoric păstrează arhitectură din perioada austro-ungară și are o importanță strategică și comercială încă din antichitate.", true);
            addSection(container, "Geografie", "Orașul este situat în partea de nord-est a județului Caraș-Severin, într-o zonă de contact între Munții Banatului și Culoarul Timiș-Cerna. Această poziție geografică i-a conferit dintotdeauna o importanță strategică, fiind un nod important de comunicații între Transilvania și Banat.", false);
            addSection(container, "Istorie", "Teritoriul actual al Caransebeșului a fost locuit încă din antichitate, fiind atestat ca așezare romană sub numele de Tibiscum. Prima mențiune documentară a orașului datează din 1289. În Evul Mediu, Caransebeșul a fost un important centru militar și administrativ, iar în perioada habsburgică (1718-1918) a făcut parte din Granița Militară Bănățeană.", false);
            addSection(container, "Atracții Turistice", "Principalele obiective turistice includ: Muzeul Județean de Etnografie și al Regimentului de Graniță, Catedrala Episcopală „Învierea Domnului, Biserica Romano-Catolică, Turnul Sânzienei (monument din secolul al XVI-lea), Cazarma (construită în perioada graniței militare), Parcul Dragalina și centrul istoric cu clădirile sale în stil austro-ungar.", false);
            addSection(container, "Cultură", "Viața culturală a orașului gravitează în jurul instituțiilor precum Casa de Cultură „George Suru, Biblioteca Municipală „Mihail Halici și Muzeul Județean. Festivalul Internațional de Folclor „Hercules, Zilele Orașulului Caransebeș și alte evenimente culturale animă viața comunității locale.", false);
            addSection(container, "Tradiții", "Zona Caransebeșului este cunoscută pentru păstrarea tradițiilor populare românești din Banat. Dansurile populare tradiționale (brâul, ardeleana, hora), costumele populare specifice și meșteșugurile tradiționale sunt componente importante ale identității culturale locale.", false);
            addSection(container, "Împrejurimi", "Caransebeșul este un punct de plecare ideal pentru explorarea atracțiilor turistice din zonă: stațiunea Muntele Mic, Munții Țarcu, Lacul Poiana Mărului, Cheile Caraşului, Parcul Național Semenic-Cheile Carașului și alte obiective naturale spectaculoase din Banatul Montan.", false);
        } else if (className.contains("BaileHerculane")) {
            // Conținut specific pentru Băile Herculane
            addSection(container, "Introducere", "Băile Herculane este una dintre cele mai vechi stațiuni balneare din Europa, cu o istorie ce datează din perioada romană. Apele termale cu proprietăți terapeutice și arhitectura impresionantă din perioada habsburgică sunt principalele atracții.", true);
            addSection(container, "Geografie", "Stațiunea este situată în sud-vestul României, în județul Caraș-Severin, în spectaculosul defileu al Cernei, la poalele Munților Domogled și Cernei. Această poziție privilegiată, protejată de munți și străbătută de râul Cerna, creează un microclimat submediteranean, cu ierni blânde și veri călduroase.", false);
            addSection(container, "Istorie", "Istoria stațiunii începe în perioada romană, când împăratul Traian a descoperit proprietățile terapeutice ale izvoarelor termale și a fondat aici o stațiune balneară numită „Ad Aquas Herculi Sacras. Arheologii au descoperit numeroase artefacte romane, inclusiv monede, statui votive dedicate lui Hercules și băi romane. În perioada habsburgică (1718-1918), stațiunea a cunoscut o dezvoltare impresionantă, fiind frecventată de familia imperială și de aristocrația europeană.", false);
            addSection(container, "Ape Termale", "Băile Herculane este renumită pentru cele 16 izvoare termale cu temperaturi între 38-60°C și un conținut bogat în sulf, calciu, sodiu, potasiu, magneziu și oligoelemente. Aceste ape au proprietăți terapeutice deosebite, fiind recomandate pentru tratarea afecțiunilor reumatismale, neurologice, ginecologice și dermatologice. Băile termale, sauna și diverse terapii balneare sunt disponibile în complexele hoteliere și centrele de tratament din stațiune.", false);
            addSection(container, "Arhitectura", "Stațiunea impresionează prin arhitectura sa în stil baroc și neoclasic, cu clădiri monumentale construite în perioada habsburgică: Pavilionul Băilor Imperiale Austriece (1883), Hotelul Roman, Cazinoul, Baia Neptun, Baia Diana, Baia Apollo și Baia Venera. Din păcate, multe dintre aceste clădiri istorice se află într-o stare avansată de degradare, deși recent au început eforturi de restaurare.", false);
            addSection(container, "Natură", "Stațiunea este înconjurată de Parcul Național Domogled-Valea Cernei, cel mai mare parc național din România, care protejează ecosisteme unice și o biodiversitate remarcabilă. Pinul negru de Banat, o specie relictă, și numeroase specii de orhidee rare pot fi admirate în zonă. Excursiile montane, traseele de drumeție și speleoturismul sunt activități populare printre vizitatori.", false);
            addSection(container, "Atracții Turistice", "Pe lângă băile termale și clădirile istorice, vizitatorii pot explora Peștera lui Adam, Grota Haiducilor, cascadele de pe Valea Cernei, Crucea Albă, Grota cu Aburi și inhalatorul natural. La mică distanță se află spectaculoasele Cheile Cernei și Vârful lui Stan.", false);
            addSection(container, "Legenda", "Conform legendei, zeul Hercules s-a oprit în această zonă pentru a se odihni și a face baie în apele termale, care i-au redat forțele pentru a lupta cu hidra, de unde și numele stațiunii. Numeroase statui și reprezentări ale lui Hercules pot fi văzute în întreaga stațiune.", false);
        } else if (className.contains("Transilvania")) {
            // Conținut specific pentru Transilvania
            addSection(container, "Introducere", "Transilvania este o regiune istorică situată în centrul României, cunoscută pentru peisajele spectaculoase, castelele medievale și diversitatea culturală.", true);
        } else if (className.contains("Muntenia")) {
            // Conținut specific pentru Muntenia
            addSection(container, "Introducere", "Muntenia, cunoscută și sub numele de Țara Românească, este o regiune istorică situată în sudul României. Cuprinde câmpii fertile, dealuri line și este străbătută de râuri importante precum Argeș, Ialomița și Dâmbovița. Regiunea include București, capitala țării, și este bogată în istorie, cultură și tradiții autohtone.", true);
            addSection(container, "Geografie", "Muntenia este situată între Carpații Meridionali la nord, Dunăre la sud, Olt la vest și Siret la est. Relieful este predominant de câmpie, cu dealuri line în nord și câmpii largi în sud. Clima este temperat-continentală, cu veri calde și ierni reci. Solurile fertile și apele abundente au făcut din Muntenia una dintre cele mai productive regiuni agricole ale României.", false);
            addSection(container, "Istorie", "Muntenia a fost centrul Principatului Țării Românești, format în secolul al XIV-lea. Aici au domnit figuri istorice importante precum Mircea cel Bătrân, Vlad Țepeș și Mihai Viteazul. În 1859, Muntenia s-a unit cu Moldova pentru a forma România modernă. Regiunea a fost martor la evenimente cruciale din istoria României, inclusiv Revoluția de la 1848 și Războiul de Independență din 1877.", false);
            addSection(container, "Cultură", "Muntenia este cunoscută pentru tradițiile sale bogate, inclusiv portul popular cu fote și catrințe colorate, dansurile tradiționale precum hora și sârba, și meșteșugurile precum olăritul și țesutul. Bucureștiul, capitala României, este un important centru cultural cu muzee, teatre și evenimente culturale de renume internațional.", false);
            addSection(container, "Atracții Turistice", "Principalele atracții includ: Palatul Parlamentului din București, Castelul Peleș din Sinaia, Curtea Domnească din Târgoviște, Mănăstirea Curtea de Argeș, Muzeul Național al Petrolului din Ploiești, și numeroase muzee, biserici și monumente istorice. Regiunea oferă și posibilități pentru turism rural, agroturism și turism de aventură în Munții Buzăului și în Parcul Natural Bucegi.", false);
            addSection(container, "Gastronomie", "Bucătăria muntenească este bogată și variată, cu specialități precum tochitura muntenească, ciorba de potroace, musaca de cartofi, mămăligă cu brânză și smântână, și cozonacul. Vinurile din podgoriile Dealu Mare sunt recunoscute pentru calitatea lor, iar țuica de prune este băutura tradițională a regiunii.", false);
        } else if (className.contains("Bucuresti")) {
            // Conținut specific pentru București
            addSection(container, "Introducere", "București, capitala României, este cel mai mare oraș al țării și un important centru cultural, economic și politic. Cunoscut drept 'Micul Paris', orașul impresionează prin amestecul unic de arhitectură veche și modernă, parcuri impresionante și viață culturală intensă.", true);
            addSection(container, "Geografie", "București este situat în sud-estul României, pe râul Dâmbovița. Orașul se întinde pe o suprafață de 228 km² și are o populație de peste 1,8 milioane de locuitori. Clima este temperat-continentală, cu veri calde și ierni reci. Orașul este împărțit în 6 sectoare administrative și are o rețea bine dezvoltată de transport public.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1459, când este menționat într-un document emis de Vlad Țepeș. În 1659 devine capitala Țării Românești. În perioada interbelică, cunoscută drept 'Epoca de Aur', Bucureștiul a cunoscut o dezvoltare urbană impresionantă, cu influențe arhitecturale franceze. În perioada comunistă, o parte semnificativă din centrul istoric a fost demolată pentru a face loc clădirilor administrative.", false);
            addSection(container, "Arhitectură", "Bucureștiul impresionează prin diversitatea arhitecturală: clădiri neoclasice și beaux-arts din perioada interbelică, arhitectură comunistă monumentală (Palatul Parlamentului), clădiri moderne și contemporane. Principalele atracții arhitecturale includ: Palatul Parlamentului, Ateneul Român, Arcul de Triumf, Palatul CEC, și numeroase biserici istorice.", false);
            addSection(container, "Cultură", "Bucureștiul este un important centru cultural, cu instituții precum Opera Națională, Teatrul Național, Filarmonica George Enescu, și numeroase muzee. Orașul găzduiește festivaluri importante precum Festivalul Internațional George Enescu, Festivalul de Film NexT, și Festivalul de Teatru Bucureștiul Tânăr.", false);
            addSection(container, "Parcuri și Spații Verzi", "Orașul are numeroase parcuri și spații verzi, printre care: Parcul Herăstrău (cel mai mare parc din București), Parcul Carol, Grădina Botanică, Parcul Cișmigiu, și Parcul Tineretului. Acestea oferă spații de relaxare și recreere pentru locuitori și vizitatori.", false);
        } else if (className.contains("Ploiesti")) {
            // Conținut specific pentru Ploiești
            addSection(container, "Introducere", "Ploiești este un important centru industrial și cultural din Muntenia, cunoscut drept capitala petrolului din România. Orașul are o bogată istorie legată de industria petrolieră și o arhitectură impresionantă din perioada interbelică.", true);
            addSection(container, "Geografie", "Ploiești este situat în județul Prahova, la poalele Carpaților, la aproximativ 60 km nord de București. Orașul se află într-o zonă de câmpie, cu acces facil la Munții Bucegi și Valea Prahovei. Clima este temperat-continentală, cu influențe montane.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1596. În secolul al XIX-lea, Ploieștiul a devenit centrul industriei petroliere din România, cu prima rafinărie modernă din lume construită în 1857. În timpul celui de-al Doilea Război Mondial, orașul a fost puternic bombardat datorită importanței strategice a industriei petroliere.", false);
            addSection(container, "Industrie și Economie", "Ploieștiul rămâne un important centru al industriei petroliere, cu rafinării moderne și companii de servicii petroliere. Orașul este și un important nod de transport, cu autostradă, cale ferată și aeroport. În ultimii ani, s-au dezvoltat și alte sectoare economice precum IT, construcții și comerț.", false);
            addSection(container, "Atracții Turistice", "Principalele atracții includ: Muzeul Național al Petrolului, Muzeul Județean de Istorie și Arheologie Prahova, Casa Memorială Ion Luca Caragiale, Biserica Sfântul Ioan, și numeroase clădiri istorice din perioada interbelică. În apropiere se află stațiunile din Valea Prahovei și Castelul Peleș.", false);
        } else if (className.contains("Targoviste")) {
            // Conținut specific pentru Târgoviște
            addSection(container, "Introducere", "Târgoviște este un oraș cu o bogată istorie, fiind fostă capitală a Țării Românești între secolele XIV-XVII. Cunoscut pentru Curtea Domnească și pentru faptul că aici a avut loc procesul și execuția lui Nicolae Ceaușescu, orașul păstrează numeroase monumente istorice și o atmosferă medievală.", true);
            addSection(container, "Geografie", "Târgoviște este situat în județul Dâmbovița, în sudul României, la aproximativ 80 km nord-vest de București. Orașul se află într-o zonă de câmpie, străbătută de râul Ialomița. Clima este temperat-continentală, cu veri calde și ierni reci.", false);
            addSection(container, "Istorie", "Târgoviște a fost capitala Țării Românești din 1396 până în 1714. Aici au domnit figuri istorice importante precum Vlad Țepeș, Mircea cel Bătrân și Mihai Viteazul. În 1989, orașul a devenit cunoscut mondial ca locul unde a avut loc procesul și execuția lui Nicolae Ceaușescu.", false);
            addSection(container, "Monumente Istorice", "Principalele monumente includ: Curtea Domnească (cu Turnul Chindiei), Biserica Domnească, Mănăstirea Dealu, și Muzeul de Istorie. Orașul păstrează și numeroase clădiri istorice din perioadele ulterioare, inclusiv arhitectură din perioada interbelică.", false);
            addSection(container, "Cultură", "Târgoviște este un important centru cultural, cu instituții precum Teatrul Municipal, Filarmonica, și numeroase muzee. Orașul găzduiește festivaluri culturale și evenimente istorice care reconstituie perioada medievală.", false);
        } else if (className.contains("Pitesti")) {
            // Conținut specific pentru Pitești
            addSection(container, "Introducere", "Pitești este un important centru universitar și industrial din Muntenia, cunoscut pentru industria auto și pentru viața culturală vibrantă. Orașul se află la intersecția unor importante rute de transport și este un nod economic important al regiunii.", true);
            addSection(container, "Geografie", "Pitești este situat în județul Argeș, la confluența râurilor Argeș și Doamnei, la aproximativ 120 km nord-vest de București. Orașul se află într-o zonă de câmpie, cu acces facil la Munții Făgăraș și la Transilvania. Clima este temperat-continentală.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1388. În secolul al XIX-lea, Piteștiul a cunoscut o dezvoltare semnificativă datorită poziției sale strategice pe ruta comercială dintre Transilvania și Muntenia. În perioada comunistă, orașul a devenit un important centru industrial, în special pentru industria auto.", false);
            addSection(container, "Industrie și Educație", "Piteștiul este cunoscut pentru fabrica Dacia, cea mai mare producătoare de automobile din România. Orașul este și un important centru universitar, cu Universitatea din Pitești, care oferă programe în diverse domenii, inclusiv inginerie auto și tehnologii informaționale.", false);
            addSection(container, "Atracții Turistice", "Principalele atracții includ: Muzeul Județean Argeș, Teatrul Municipal, Parcul Trivale, și numeroase biserici istorice. În apropiere se află Mănăstirea Curtea de Argeș și stațiunile din Valea Prahovei.", false);
        } else if (className.contains("Buzau")) {
            // Conținut specific pentru Buzău
            addSection(container, "Introducere", "Buzău este un oraș cu tradiție culturală și industrială, situat la poalele Munților Buzăului. Cunoscut pentru peisajele naturale spectaculoase și pentru patrimoniul cultural bogat, orașul este un important centru al regiunii Muntenia.", true);
            addSection(container, "Geografie", "Buzău este situat în județul omonim, la poalele Munților Buzăului, la aproximativ 120 km nord-est de București. Orașul se află într-o zonă de dealuri și câmpii, străbătută de râul Buzău. Clima este temperat-continentală, cu influențe montane.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1431. În perioada medievală, Buzăul a fost un important centru comercial și administrativ. În secolul al XIX-lea, orașul a cunoscut o dezvoltare semnificativă, devenind un important centru cultural și industrial al regiunii.", false);
            addSection(container, "Cultură și Educație", "Buzăul este un important centru cultural, cu instituții precum Teatrul Municipal, Filarmonica, și numeroase muzee. Orașul are o bogată tradiție literară, fiind locul de naștere al unor personalități precum Vasile Voiculescu și George Călinescu. Universitatea din Buzău oferă programe în diverse domenii.", false);
            addSection(container, "Atracții Turistice", "Principalele atracții includ: Muzeul Județean Buzău, Casa Memorială George Călinescu, Parcul Crâng, și numeroase biserici istorice. În apropiere se află stațiunile din Munții Buzăului, Cheile Buzăului, și rezervațiile naturale din zonă.", false);
        } else if (className.contains("BaiaMare")) {
            // Conținut specific pentru Baia Mare
            addSection(container, "Introducere", "Baia Mare este reședința județului Maramureș și cel mai important centru urban din regiune. Orașului îmbină în mod armonios o bogată istorie minieră cu elemente de arhitectură medievală și modernă, fiind înconjurat de un peisaj natural deosebit.", true);
            addSection(container, "Geografie", "Situat în nord-vestul României, Baia Mare se află într-o depresiune la poalele Munților Gutâi, parte a lanțului vulcanic al Carpaților Orientali. Orașul este străbătut de râul Săsar și se bucură de un climat temperat-continental moderat, influențat de prezența munților din împrejurimi.", false);
            addSection(container, "Istorie", "Baia Mare are o istorie îndelungată, fiind menționat pentru prima dată în documente în 1329 sub numele de Rivulus Dominarum. Orașul s-a dezvoltat ca un important centru minier pentru extracția și prelucrarea aurului, argintului și a altor metale neferoase, activitate ce a influențat profund istoria și cultura locală.", false);
            addSection(container, "Atracții Turistice", "Centrul Vechi este principala atracție a orașului, cu Turnul Ștefan (simbol al orașului), Bastionul Măcelarilor și numeroase clădiri istorice. Alte obiective importante sunt Muzeul Județean de Artă, Muzeul de Mineralogie, Muzeul de Etnografie și Artă Populară, precum și Planetariul și Complexul Astronomic.", false);
            addSection(container, "Cultură", "Baia Mare are o viață culturală vibrantă, cu instituții precum Teatrul Municipal, Biblioteca Județeană, Galeriile de Artă și Casa de Cultură. Orașul găzduiește anual diverse festivaluri și evenimente culturale, printre care Festivalul Castanelor, Sărbătoarea Mărcii și Festivalul de Jazz.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională maramureșeană poate fi savurată în restaurantele din oraș, cu specialități precum balmoșul, tocana de oaie, sarmalele în foi de varză acră, plăcintele cu brânză și cozonacul. Horinca, pălinca și afinata sunt băuturile tradiționale specifice zonei.", false);
        } else if (className.contains("SighetuMarmatiei")) {
            // Conținut specific pentru Sighetu Marmației
            addSection(container, "Introducere", "Sighetu Marmației este al doilea oraș ca mărime din județul Maramureș, situat la granița cu Ucraina. Este un important centru cultural și istoric al Maramureșului, cunoscut pentru Memorialul Victimelor Comunismului și al Rezistenței, dar și pentru bogatul patrimoniu etnografic.", true);
            addSection(container, "Geografie", "Orașul este situat în nordul României, în Depresiunea Maramureșului, pe malul drept al râului Tisa, care formează granița naturală cu Ucraina. Înconjurat de dealuri și munți împăduriți, Sighetul se bucură de un cadru natural pitoresc și de un climat temperat continental.", false);
            addSection(container, "Istorie", "Prima atestare documentară datează din 1326. De-a lungul timpului, orașul a fost un important centru administrativ, comercial și cultural al Maramureșului istoric. După al Doilea Război Mondial, închisoarea din Sighet a devenit un loc de detenție pentru elitele politice și intelectuale românești, transformată astăzi în Memorial pentru victimele regimului comunist.", false);
            addSection(container, "Atracții Turistice", "Memorialul Victimelor Comunismului și al Rezistenței (fostă închisoare politică), Muzeul Etnografic al Maramureșului, Muzeul Culturii Evreiești din Maramureș, Casa Memorială Elie Wiesel, Casa Memorială \"Gheorghe Pop de Băsești\", centrul istoric și numeroasele biserici de lemn din împrejurimi sunt principalele atracții turistice.", false);
            addSection(container, "Cultură", "Sighetul este un important centru cultural al Maramureșului, cu instituții precum Muzeul Etnografic, Casa de Cultură și Biblioteca Municipală. Festivalul de Datini și Obiceiuri de Iarnă \"Marmația\", Zilele Sighetului și Festivalul de Film și Istorie sunt evenimente culturale reprezentative.", false);
            addSection(container, "Turism Rural", "Zona Sighetului oferă multiple oportunități pentru turismul rural, cu sate tradiționale maramureșene precum Săpânța, Breb, Budești și Desești, unde se pot admira biserici de lemn incluse în patrimoniul UNESCO, porți maramureșene sculptate și case tradiționale, și unde se poate experimenta autenticitatea vieții rurale maramureșene.", false);
        } else if (className.contains("Borsa")) {
            // Conținut specific pentru Borșa
            addSection(container, "Introducere", "Borșa este o cunoscută stațiune montană din nordul Maramureșului, situată la poalele Munților Rodnei. Este o destinație populară atât pentru sporturile de iarnă, cât și pentru drumeții montane în sezonul cald, oferind peisaje naturale spectaculoase și aer curat.", true);
            addSection(container, "Geografie", "Situată în extremitatea estică a județului Maramureș, la aproximativ 140 km de Baia Mare, Borșa se află la o altitudine de 850 metri, la poalele Vârfului Pietrosu Rodnei (2303 m), cel mai înalt vârf din Carpații Orientali. Stațiunea este străbătută de râul Vișeu și se bucură de un climat montan blând, cu ierni lungi bogate în zăpadă.", false);
            addSection(container, "Istorie", "Localitatea Borșa este atestată documentar din 1365. A fost un important centru minier pentru extracția de aur, argint și alte minereuri. În perioada comunistă, zona a cunoscut o dezvoltare importantă a industriei miniere, dar după 1990, majoritatea minelor au fost închise, iar localitatea s-a reorientat spre turism și industria lemnului.", false);
            addSection(container, "Pârtii de Schi", "Complexul Turistic Borșa dispune de mai multe pârtii de schi de diferite grade de dificultate, dintre care cea mai cunoscută este pârtia olimpică de la Stiol, cu o lungime de peste 3 km și o diferență de nivel de 900 m. Instalațiile de transport pe cablu și sistemele de producere a zăpezii artificiale asigură condiții bune pentru practicarea sporturilor de iarnă.", false);
            addSection(container, "Atracții Naturale", "Rezervația Naturală Pietrosu Mare, parte a Parcului Național Munții Rodnei, oferă oportunități pentru drumeții montane spectaculoase, cu peisaje alpine impresionante, lacuri glaciare, cascade și o biodiversitate remarcabilă. Cascada Cailor, cu o cădere de apă de peste 90 m, este una dintre cele mai spectaculoase cascade din România.", false);
            addSection(container, "Cultură și Tradiții", "Zona Borșa își păstrează vie cultura tradițională maramureșeană, cu port popular specific, arhitectură în lemn, meșteșuguri tradiționale și obiceiuri folclorice autentice. Zilele Orașului Borșa și Festivalul Flori de pe Coasta Borcutului sunt evenimente care pun în valoare folclorul și tradițiile locale.", false);
        } else if (className.contains("ViseuDeSus")) {
            // Conținut specific pentru Vișeu de Sus
            addSection(container, "Introducere", "Vișeu de Sus este un oraș din Maramureșul istoric, cunoscut în special pentru Mocănița de pe Valea Vaserului, ultima cale ferată forestieră cu aburi din Europa care încă funcționează în scopuri comerciale. Acest tren turistic atrage anual mii de vizitatori din întreaga lume.", true);
            addSection(container, "Geografie", "Situat în nord-estul județului Maramureș, Vișeu de Sus se află în depresiunea Maramureșului, la confluența râurilor Vișeu și Vaser, la poalele Munților Maramureșului. Orașul este înconjurat de peisaje montane spectaculoase, acoperite cu păduri de conifere și foioase.", false);
            addSection(container, "Istorie", "Prima atestare documentară a localității datează din 1365. De-a lungul secolelor, orașul s-a dezvoltat ca un centru pentru exploatări forestiere, iar în secolul XIX a fost construită calea ferată forestieră cu ecartament îngust de pe Valea Vaserului pentru transportul buștenilor.", false);
            addSection(container, "Mocănița", "Atracția principală a orașului este Mocănița, trenul cu aburi care străbate Valea Vaserului pe o distanță de 21 km până în Munții Maramureșului. Acest tren forestier, construit în 1932, este astăzi o atracție turistică unică în Europa, oferind o călătorie pitorească prin sălbăticia munților, pe o cale ferată care urmează cursul sinuos al râului Vaser.", false);
            addSection(container, "Atracții Turistice", "Pe lângă Mocăniță, Vișeu de Sus oferă și alte atracții: Muzeul de Locomotive cu Abur, Sinagoga evreiască, bisericile ortodoxe și greco-catolice, precum și peisajele naturale din împrejurimi. La aproximativ 40 km se află Cimitirul Vesel de la Săpânța, o altă atracție emblematică a Maramureșului.", false);
            addSection(container, "Cultură și Tradiții", "Zona Vișeului păstrează vii tradițiile și obiceiurile maramureșene: portul popular specific, arhitectura tradițională în lemn, meșteșugurile și gastronomia locală. Festivalul Văii Vaserului și Zilele Orașului sunt evenimente care celebrează această bogăție culturală, cu parade ale portului popular, muzică și dansuri tradiționale.", false);
        } else if (className.contains("Sapanta")) {
            // Conținut specific pentru Săpânța
            addSection(container, "Introducere", "Săpânța este o comună din Maramureș, faimoasă în întreaga lume pentru Cimitirul Vesel, un cimitir neobișnuit unde crucile multicolore cu epitafuri în versuri satirice transformă moartea dintr-un eveniment trist într-o celebrare a vieții celei care a trecut.", true);
            addSection(container, "Geografie", "Situată în nord-vestul județului Maramureș, la aproximativ 20 km de Sighetu Marmației, comuna Săpânța se află pe malul râului Tisa, la granița cu Ucraina. Zona beneficiază de un cadru natural pitoresc, cu dealuri împădurite, pășuni și terenuri agricole.", false);
            addSection(container, "Istorie", "Prima atestare documentară a localității datează din 1373. De-a lungul timpului, Săpânța s-a remarcat prin păstrarea autenticității tradițiilor maramureșene, a meșteșugurilor populare și a unei comunități puternic ancorate în valorile tradiționale românești.", false);
            addSection(container, "Cimitirul Vesel", "Creat în 1935 de meșterul popular Stan Ioan Pătraș, Cimitirul Vesel a devenit simbolul comunei și una dintre cele mai cunoscute atracții turistice din România. Crucile din stejar, viu colorate în albastru (culoarea predominantă, simbolizând speranța și libertatea), sunt decorate cu scene naive reprezentând momente din viața celui decedat și inscripționate cu epitafuri în versuri, adesea cu umor și ironie, care povestesc despre viața, ocupația și, uneori, despre modul în care a murit persoana respectivă.", false);
            addSection(container, "Casa Memorială Stan Ioan Pătraș", "Lângă Cimitirul Vesel se află casa-atelier a creatorului acestuia, transformată astăzi în muzeu. Aici se pot admira lucrări originale ale meșterului, unelte și obiecte personale, precum și procesul de creație a crucilor pictate. După moartea sa în 1977, tradiția a fost continuată de ucenicul său, Dumitru Pop Tincu.", false);
            addSection(container, "Alte Atracții", "Pe lângă Cimitirul Vesel, Săpânța oferă și alte atracții valoroase: Biserica de lemn din secolul al XVIII-lea, Muzeul de Etnografie, Complexul Monastic de la Săpânța-Peri (care include cea mai înaltă biserică de lemn din lume, cu 78 m), portul popular tradițional, meșteșugurile locale (țesut, sculptură în lemn) și frumoasele porți maramureșene sculptate.", false);
            addSection(container, "Gastronomie și Tradiții", "Vizitatorii pot experimenta gastronomia tradițională maramureșeană în gospodăriile localnicilor, cu specialități precum balmoșul, mămăliga cu brânză de burduf, tocana de oaie, sarmalele în foi de varză acră și horinca (țuica locală). Localnicii păstrează vii tradițiile și obiceiurile strămoșești, inclusiv portul popular, dansurile și cântecele tradiționale.", false);
        } else if (className.contains("Maramures")) {
            // Conținut specific pentru regiunea Maramureș
            addSection(container, "Introducere", "Maramureșul este una dintre cele mai autentice și pitorești regiuni din România, cunoscut pentru peisajele sale naturale spectaculoase, arhitectura tradițională în lemn, bisericile de lemn incluse în patrimoniul UNESCO, portul popular și ospitalitatea localnicilor care păstrează vie cultura tradițională românească.", true);
            addSection(container, "Geografie", "Situat în nord-vestul României, Maramureșul istoric este delimitat natural de lanțuri muntoase: Munții Oaș, Gutâi, Țibleș și Rodnei la sud, și râul Tisa la nord, care formează granița naturală cu Ucraina. Relieful variat include munți împăduriți, dealuri line, depresiuni și văi pitorești, străbătute de râuri cu ape limpezi.", false);
            addSection(container, "Istorie", "Maramureșul are o istorie bogată, fiind locuit încă din antichitate. În Evul Mediu a fost o regiune autonomă, cu o nobilime românească puternică (nobilii maramureșeni). După secole de stăpânire austro-ungară, partea de sud a Maramureșului istoric a devenit parte a României în 1918, iar partea de nord (Maramureșul din dreapta Tisei) aparține astăzi Ucrainei.", false);
            addSection(container, "Biserici de Lemn", "Regiunea este faimoasă pentru bisericile sale de lemn, construite între secolele XVII-XVIII, dintre care opt sunt incluse în patrimoniul mondial UNESCO: Budești, Desești, Bârsana, Ieud, Șurdești, Plopiș, Poienile Izei și Rogoz. Aceste biserici impresionează prin arhitectura lor unică, proporțiile armonioase, turnurile-săgeată și pictura interioară valoroasă.", false);
            addSection(container, "Arhitectură Tradițională", "Maramureșul păstrează o arhitectură tradițională în lemn remarcabilă: case cu pridvor, șuri, porți monumentale sculptate, mori de apă, pive, vâltori. Porțile maramureșene, adevărate arcuri de triumf țărănești, sunt simboluri ale regiunii, decorate cu motive solare, vegetale, antropomorfe și zoomorfe, cu semnificații simbolice profunde.", false);
            addSection(container, "Tradiții și Obiceiuri", "Aici tradițiile strămoșești sunt încă vii: port popular colorat și ornamentat, meșteșuguri tradiționale (țesut, sculptură în lemn, olărit, încondeierea ouălor), dansuri și cântece folclorice, obiceiuri calendaristice și de familie. Festivalurile populare precum \"Tânjaua de pe Mara\", \"Hora la Prislop\" și obiceiurile de iarnă sunt manifestări autentice ale spiritului local.", false);
            addSection(container, "Cimitirul Vesel din Săpânța", "Unic în lume, Cimitirul Vesel transformă conceptul de moarte printr-o abordare optimistă și plină de umor. Crucile colorate în albastru, decorate cu scene naive și epitafuri în versuri care povestesc viața celui decedat, reflectă filozofia de viață a maramureșenilor și atitudinea lor aparte față de moarte.", false);
            addSection(container, "Gastronomie", "Bucătăria tradițională maramureșeană oferă delicii culinare autentice: balmoș, cozonac, plăcinte de casă, sarmale în foi de varză acră, tochitura, slănina afumată cu ceapă roșie. Produsele lactate și cele din carne de oaie sunt specifice zonei. Horinca (țuica dublu distilată) și afinata sunt băuturile tradiționale ce nu lipsesc de la masa maramureșeană.", false);
        } else {
            // Conținut generic pentru alte regiuni
            addSection(container, "Informații", "Vă rugăm selectați o regiune specifică pentru a vedea informații detaliate.", true);
        }
    }

    protected void addSection(LinearLayout container, String title, String content, boolean isHighlighted) {
        if (container == null) {
            return;
        }

        View sectionView = getLayoutInflater().inflate(R.layout.section_layout, container, false);

        TextView titleView = sectionView.findViewById(R.id.sectionTitle);
        TextView contentView = sectionView.findViewById(R.id.sectionContent);
        CheckBox checkBox = sectionView.findViewById(R.id.visitedCheckbox);
        View importantBadge = sectionView.findViewById(R.id.importantBadge);

        titleView.setText(title);
        contentView.setText(content);

        // Verificăm dacă secțiunea a fost vizitată anterior
        String region = getRegionName().toLowerCase();
        String landmarkKey = "landmark_" + region + "_" + title.replace(" ", "_").toLowerCase();
        SharedPreferences prefs = getSharedPreferences("LandmarkPrefs", MODE_PRIVATE);
        boolean wasVisited = prefs.getBoolean(landmarkKey, false);
        
        // Setăm starea checkbox-ului în funcție de vizitele anterioare
        checkBox.setChecked(wasVisited);

        if (isHighlighted) {
            sectionView.setBackgroundResource(R.drawable.enhanced_card_background);
            titleView.setTextAppearance(android.R.style.TextAppearance_Large);
            titleView.setTextColor(getResources().getColor(R.color.colorPrimary));
            contentView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            importantBadge.setVisibility(View.VISIBLE);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (soundEffect != null) {
                soundEffect.start();
            }
            buttonView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            
            // Actualizează punctele utilizând PointsManager
            PointsManager pointsManager = PointsManager.getInstance(this);
            pointsManager.updateLandmarkStatus(this, getRegionName().toLowerCase(), isChecked);
            
            // Salvăm starea checkbox-ului în SharedPreferences
            String currentRegion = getRegionName().toLowerCase();
            String currentLandmarkKey = "landmark_" + currentRegion + "_" + title.replace(" ", "_").toLowerCase();
            SharedPreferences landmarkPrefs = getSharedPreferences("LandmarkPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = landmarkPrefs.edit();
            editor.putBoolean(currentLandmarkKey, isChecked);
            editor.apply();
            
            // Actualizăm ambele TextView-uri pentru afișarea punctelor
            int totalPoints = pointsManager.getTotalPoints(this);
            
            // 1. Actualizăm TextView-ul din bara de sus
            TextView topPointsTextView = findViewById(R.id.pointsTextView);
            if (topPointsTextView != null) {
                // Setăm textul cu font mai mare și culoare evidentă
                topPointsTextView.setTextSize(18);
                topPointsTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                topPointsTextView.setText("PUNCTE: " + totalPoints);
                
                // Animăm TextView-ul pentru a atrage atenția
                topPointsTextView.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.button_bounce));
            }
            
            // 2. Actualizăm TextView-ul cu iconiță (pointsText)
            TextView iconPointsTextView = findViewById(R.id.pointsText);
            if (iconPointsTextView != null) {
                iconPointsTextView.setText(totalPoints + " Puncte");
                
                // Animăm și acest TextView pentru a atrage atenția
                iconPointsTextView.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.button_bounce));
            }
            
            // Dacă niciunul din TextView-uri nu există, afișăm un mesaj Toast
            if (topPointsTextView == null && iconPointsTextView == null) {
                Toast.makeText(this, "Puncte totale: " + totalPoints, 
                              Toast.LENGTH_LONG).show();
            }
        });

        sectionView.setOnClickListener(v -> {
            Intent intent = new Intent(this, SectionPreviewActivity.class);
            intent.putExtra(SectionPreviewActivity.EXTRA_TITLE, title);
            intent.putExtra(SectionPreviewActivity.EXTRA_CONTENT, content);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_slide_in);
        fadeIn.setDuration(500);
        sectionView.startAnimation(fadeIn);

        container.addView(sectionView);
    }

    private void addImageToCarousel(Uri imageUri, boolean isUserManaged) {
        CityImage newImage = new CityImage(imageUri, isUserManaged);
        images.add(newImage);
        imageAdapter.notifyDataSetChanged();
        imageCarousel.setCurrentItem(images.size() - 1, true);
    }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
            
            // Determinăm ce imagini să încărcăm în funcție de regiunea/orașul curent
            String className = getClass().getSimpleName();
            
            if (className.contains("Dobrogea") || className.contains("Constanta")) {
                cityImages.add("constanta_port");
                cityImages.add("cernavoda");
                cityImages.add("tulcea");
            } else if (className.contains("Transilvania")) {
                cityImages.add("cluj");
                cityImages.add("brasov");
                cityImages.add("sibiu");
                cityImages.add("targumures");
                cityImages.add("alba_iulia");
            } else if (className.contains("Oltenia")) {
                cityImages.add("craiova");
                cityImages.add("targujiu");
                cityImages.add("oltenia_landscape");
                cityImages.add("valcea");
                cityImages.add("slatina");
            } else if (className.contains("Banat")) {
                cityImages.add("timisoara");
                cityImages.add("banat_panorama");
                cityImages.add("piata_unirii");
            } else if (className.contains("Moldova")) {
                cityImages.add("iasi");
                cityImages.add("suceava");
                cityImages.add("bacau");
                cityImages.add("piatra_neamt");
                cityImages.add("copou_iasi");
                cityImages.add("rapa_galbena_iasi");
            } else if (className.contains("Crisana")) {
                cityImages.add("oradea");
                cityImages.add("salonta");
            } else if (className.contains("Bucovina")) {
                cityImages.add("suceava");
                cityImages.add("vatra_dornei");
                cityImages.add("campulung");
            } else if (className.contains("Muntenia")) {
                cityImages.add("bucuresti");
                cityImages.add("alexandria");
                cityImages.add("pitesti");
                cityImages.add("curtea_de_arges");
            } else if (className.contains("Maramures")) {
                cityImages.add("baia_mare");
                cityImages.add("muzeu_sat_mar");
                cityImages.add("turn_cetatii");
            } else {
                // Imagini implicite pentru orice alt caz
                cityImages.add("romania_harta");
                cityImages.add("comp_baroc");
            }
        }
        return cityImages;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundEffect != null) {
            soundEffect.release();
            soundEffect = null;
        }
    }

    protected String getCityDescription() {
        String cityName = getCityName();
        
        switch (cityName) {
            case "Sibiu":
                return "Sibiu este un oraș medieval din Transilvania cu o arhitectură saxonă deosebită. Cunoscut pentru Piața Mare și Piața Mică, Podul Minciunilor, și muzeele sale de renume. A fost Capitală Culturală Europeană în 2007.";
                
            case "Brașov":
                return "Brașov este un important centru turistic din Transilvania, cunoscut pentru Cetatea Medievală, Biserica Neagră și apropierea de stațiunile montane. Situat la poalele Munților Carpați, oferă un peisaj montan spectaculos.";
                
            case "Cluj-Napoca":
                return "Cluj-Napoca este un centru cultural, universitar și economic din Transilvania. Orașul găzduiește festivaluri internaționale precum TIFF și Untold, universități de prestigiu și o viață culturală activă.";
                
            case "Constanța":
                return "Constanța este cel mai mare port al României la Marea Neagră, un important centru turistic și economic, cunoscut pentru plajele sale, vechiul cazino și siturile arheologice care atestă istoria sa bimilenară.";
                
            case "Craiova":
                return "Craiova este cel mai important oraș din Oltenia, cu o bogată istorie și cultură. Parcul Nicolae Romanescu, Centrul Vechi și Muzeul de Artă sunt principalele atracții turistice.";
                
            case "Iași":
                return "Iași este un important centru cultural și spiritual al României, cunoscut ca orașul celor 100 de biserici. Palatul Culturii, Grădina Botanică și Universitatea Alexandru Ioan Cuza sunt atracții turistice importante.";
                
            case "București":
                return "București este capitala și cel mai mare oraș al României, centrul administrativ, cultural și economic al țării. Palatul Parlamentului, Ateneul Român și numeroasele parcuri și muzee fac din acesta o destinație turistică importantă.";
                
            case "Timișoara":
                return "Timișoara, cunoscută ca 'Mica Vienă', este cel mai mare oraș din Banat. A fost primul oraș european iluminat electric și locul de început al Revoluției din 1989. Va fi Capitală Culturală Europeană în 2023.";
                
            default:
                return "Informații detaliate despre acest oraș nu sunt disponibile momentan.";
        }
    }

        private void setupImageCarousel() {
    imageCarousel = findViewById(R.id.imageCarousel);
    imageIndicator = findViewById(R.id.imageIndicator);
    
    // Încărcăm imaginile specifice orașului
    images = new ArrayList<>();
    
    try {
        // 1. Mai întâi adăugăm imaginile predefinite
        ArrayList<String> cityImagesList = getCityImages();
        if (cityImagesList != null && !cityImagesList.isEmpty()) {
            Log.d("ImageCarousel", "Regiunea: " + getRegionName() + ", Imagini de încărcat: " + cityImagesList.size());
            
            for (String imageName : cityImagesList) {
                // Adăugăm direct imaginea folosind ID-ul resursei
                try {
                    int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                    
                    if (resourceId != 0) {
                        // Folosim Constructor care ia direct resursele
                        ImageView testImageView = new ImageView(this);
                        testImageView.setImageResource(resourceId);
                        
                        // Creăm corect URI pentru resursă
                        Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
                        images.add(new CityImage(imageUri, false));
                        Log.d("ImageCarousel", "Imagine găsită și adăugată: " + imageName + " (resourceId=" + resourceId + ")");
                    } else {
                        // Încercăm cu extensia jpg/png exact
                        int jpgId = getResources().getIdentifier(imageName + ".jpg", "drawable", getPackageName());
                        if (jpgId != 0) {
                            Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + jpgId);
                            images.add(new CityImage(imageUri, false));
                            Log.d("ImageCarousel", "Imagine găsită cu extensie .jpg: " + imageName);
                            continue;
                        }
                        
                        int pngId = getResources().getIdentifier(imageName + ".png", "drawable", getPackageName());
                        if (pngId != 0) {
                            Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + pngId);
                            images.add(new CityImage(imageUri, false));
                            Log.d("ImageCarousel", "Imagine găsită cu extensie .png: " + imageName);
                            continue;
                        }
                        
                        Log.e("ImageCarousel", "Imagine negăsită: " + imageName);
                        // Adăugăm un placeholder pentru imaginile negăsite
                        int placeholderId = getResources().getIdentifier("city_image_placeholder", "drawable", getPackageName());
                        if (placeholderId == 0) {
                            placeholderId = android.R.drawable.ic_menu_gallery; // Folosim un placeholder din Android
                        }
                        Uri placeholderUri = Uri.parse("android.resource://" + getPackageName() + "/" + placeholderId);
                        images.add(new CityImage(placeholderUri, false));
                    }
                } catch (Exception e) {
                    Log.e("ImageCarousel", "Eroare la încărcarea imaginii " + imageName + ": " + e.getMessage());
                    // În caz de eroare, adăugăm un placeholder
                    int placeholderId = android.R.drawable.ic_menu_gallery;
                    Uri placeholderUri = Uri.parse("android.resource://" + getPackageName() + "/" + placeholderId);
                    images.add(new CityImage(placeholderUri, false));
                }
            }
            
            Log.d("ImageCarousel", "Număr total de imagini adăugate: " + images.size());
        } else {
            Log.e("ImageCarousel", "Lista de imagini este goală sau null pentru " + getRegionName());
        }
        
        // 2. Apoi încărcăm imaginile adăugate de utilizator
        loadUserImages();
        
        // 3. Dacă nu avem nicio imagine, adăugăm un placeholder
        if (images.isEmpty()) {
            Log.w("ImageCarousel", "Nu s-a găsit nicio imagine, se adaugă placeholder");
            int placeholderId = android.R.drawable.ic_menu_gallery;
            Uri placeholderUri = Uri.parse("android.resource://" + getPackageName() + "/" + placeholderId);
            images.add(new CityImage(placeholderUri, false));
        }
        
    } catch (Exception e) {
        Log.e("ImageCarousel", "Eroare la încărcarea imaginilor: " + e.getMessage(), e);
        e.printStackTrace();
        // În caz de eroare, adăugăm placeholder-ul
        int placeholderId = android.R.drawable.ic_menu_gallery;
        Uri placeholderUri = Uri.parse("android.resource://" + getPackageName() + "/" + placeholderId);
        images.add(new CityImage(placeholderUri, false));
    }
        
        imageAdapter = new CityImageAdapter(images, this);
        imageCarousel.setAdapter(imageAdapter);

        if (imageIndicator != null && imageCarousel != null && images.size() > 1) {
            new TabLayoutMediator(imageIndicator, imageCarousel,
                    (tab, position) -> tab.setIcon(R.drawable.tab_selector)
            ).attach();
            
            imageIndicator.setVisibility(View.VISIBLE);
        } else if (imageIndicator != null) {
            imageIndicator.setVisibility(View.GONE);
        }
    }

    // Metodă pentru încărcarea imaginilor adăugate de utilizator
    private void loadUserImages() {
        try {
            SharedPreferences prefs = getSharedPreferences("UserImagesPrefs", MODE_PRIVATE);
            String cityKey = "city_images_" + getCityName().toLowerCase().replace(" ", "_");
            
            String savedImages = prefs.getString(cityKey, "");
            if (!savedImages.isEmpty()) {
                String[] imageUris = savedImages.split(",");
                
                for (String uriString : imageUris) {
                    try {
                        Uri uri = Uri.parse(uriString);
                        // Verificăm dacă URI-ul este valid
                        getContentResolver().getType(uri);
                        // Adăugăm imaginea
                        images.add(new CityImage(uri, true));
                    } catch (Exception e) {
                        // Ignorăm imaginile care nu mai sunt valide
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodă ajutătoare pentru verificarea tuturor imaginilor
    private void checkAllImageResources() {
        ArrayList<String> imagesToCheck = getCityImages();
        if (imagesToCheck == null || imagesToCheck.isEmpty()) {
            Log.e("ImageChecker", "Lista de imagini este goală sau null!");
            return;
        }
        
        Log.d("ImageChecker", "Verificare resurse pentru " + getRegionName() + ", " + imagesToCheck.size() + " imagini:");
        
        for (String imageName : imagesToCheck) {
            int resourceId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            
            if (resourceId != 0) {
                Log.d("ImageChecker", "✅ Imagine găsită: " + imageName);
            } else {
                Log.e("ImageChecker", "❌ Imagine NEGĂSITĂ: " + imageName);
                
                // Încercăm cu diverse extensii
                String[] extensions = {".jpg", ".jpeg", ".png", ".webp"};
                boolean found = false;
                
                for (String ext : extensions) {
                    int resId = getResources().getIdentifier(imageName + ext, "drawable", getPackageName());
                    if (resId != 0) {
                        found = true;
                        Log.d("ImageChecker", "   Dar a fost găsită ca: " + imageName + ext);
                        break;
                    }
                }
                
                if (!found) {
                    Log.e("ImageChecker", "   Sugestie: Adaugă fișierul " + imageName + ".jpg în folderul drawable!");
                }
            }
        }
    }
}
