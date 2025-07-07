package com.example.myapplication.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.LeaderboardAdapter;
import com.example.myapplication.model.LeaderboardEntry;
import com.example.myapplication.repository.QuizResultRepository;
import com.google.android.material.imageview.ShapeableImageView;
import androidx.core.view.WindowCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

/**
 * Activitate pentru afișarea clasamentului (leaderboard) quiz-urilor
 * Designul nou cu podium pentru top 3 și tema României
 */
public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderboardActivity";
    
    // Repository pentru accesul la date
    private QuizResultRepository quizResultRepository;
    
    // UI Components
    private MaterialToolbar toolbar;
    private TextView regionTab, countryTab;
    private RecyclerView leaderboardRecyclerView;
    private FrameLayout loadingOverlay;
    
    // Podium Components pentru Top 3 cu noile ID-uri
    private ImageView firstPlaceImage, secondPlaceImage, thirdPlaceImage;
    private TextView firstPlaceName, secondPlaceName, thirdPlaceName;
    private TextView firstPlaceScore, secondPlaceScore, thirdPlaceScore;
    private View firstPlaceContainer, secondPlaceContainer, thirdPlaceContainer;
    private ImageView winnerCrown;
    private TextView tvTopPlayers;
    private View[] sparkles;
    
    // Data
    private LeaderboardAdapter leaderboardAdapter;
    private List<LeaderboardEntry> leaderboardData = new ArrayList<>();
    private boolean isRegionalMode = true; // true = Regiune, false = Țară
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        
        // Configurăm fereastra pentru a utiliza edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Setăm culoarea de fundal a barei de stare transparent
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        
        // Inițializăm repository-ul
        quizResultRepository = QuizResultRepository.getInstance();
        
        // Inițializăm UI components
        initializeViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        
        // Încărcăm datele reale din baza de date
        loadLeaderboardData();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        regionTab = findViewById(R.id.regionTab);
        countryTab = findViewById(R.id.countryTab);
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        
        // Enhanced Podium components with new IDs
        firstPlaceImage = findViewById(R.id.firstPlaceImage);
        secondPlaceImage = findViewById(R.id.secondPlaceImage);
        thirdPlaceImage = findViewById(R.id.thirdPlaceImage);
        
        firstPlaceName = findViewById(R.id.firstPlaceName);
        secondPlaceName = findViewById(R.id.secondPlaceName);
        thirdPlaceName = findViewById(R.id.thirdPlaceName);
        
        firstPlaceScore = findViewById(R.id.firstPlaceScore);
        secondPlaceScore = findViewById(R.id.secondPlaceScore);
        thirdPlaceScore = findViewById(R.id.thirdPlaceScore);
        
        // Containers for animation
        firstPlaceContainer = findViewById(R.id.firstPlaceContainer);
        secondPlaceContainer = findViewById(R.id.secondPlaceContainer);
        thirdPlaceContainer = findViewById(R.id.thirdPlaceContainer);
        
        // Crown elements
        winnerCrown = findViewById(R.id.winnerCrown);
        
        // Title
        tvTopPlayers = findViewById(R.id.tvTopPlayers);
        
        // Sparkles for animation
        sparkles = new View[5];
        sparkles[0] = findViewById(R.id.sparkle1);
        sparkles[1] = findViewById(R.id.sparkle2);
        sparkles[2] = findViewById(R.id.sparkle3);
        sparkles[3] = findViewById(R.id.sparkle4);
        sparkles[4] = findViewById(R.id.sparkle5);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Debug: Long click pentru a adăuga date de test în baza de date
        toolbar.setOnLongClickListener(v -> {
            populateDatabaseWithTestData();
            return true;
        });
    }
    
    private void setupTabs() {
        regionTab.setOnClickListener(v -> {
            if (isRegionalMode) {
                // Dacă suntem deja în modul regional, afișăm dialogul de selecție a regiunii
                showRegionSelectionDialog();
            } else {
                // Comutăm la modul regional
                switchToRegionalMode();
            }
        });
        countryTab.setOnClickListener(v -> switchToCountryMode());
        
        // Setăm tab-ul regional ca fiind activ inițial
        updateTabAppearance();
    }
    
    private void setupRecyclerView() {
        leaderboardAdapter = new LeaderboardAdapter(leaderboardData);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);
    }
    
    private void switchToRegionalMode() {
        if (!isRegionalMode) {
            isRegionalMode = true;
            animateTabSwitch();
            updateTabAppearance();
            loadLeaderboardData();
        }
    }
    
    private void switchToCountryMode() {
        if (isRegionalMode) {
            isRegionalMode = false;
            animateTabSwitch();
            updateTabAppearance();
            loadLeaderboardData();
        }
    }
    
    private void updateTabAppearance() {
        // Obținem numele regiunii pentru afișare
        String selectedRegion = getSelectedRegion();
        String regionDisplayName = getRegionDisplayName(selectedRegion);
        
        if (isRegionalMode) {
            // Region tab active
            regionTab.setBackgroundResource(R.drawable.tab_active_exact_background);
            regionTab.setTextColor(getResources().getColor(R.color.white, getTheme()));
            regionTab.setText(regionDisplayName);
            
            // Country tab inactive
            countryTab.setBackgroundResource(R.drawable.tab_inactive_background);
            countryTab.setTextColor(getResources().getColor(R.color.romania_text_secondary, getTheme()));
        } else {
            // Country tab active
            countryTab.setBackgroundResource(R.drawable.tab_active_exact_background);
            countryTab.setTextColor(getResources().getColor(R.color.white, getTheme()));
            
            // Region tab inactive
            regionTab.setBackgroundResource(R.drawable.tab_inactive_background);
            regionTab.setTextColor(getResources().getColor(R.color.romania_text_secondary, getTheme()));
            regionTab.setText(regionDisplayName);
        }
    }
    
    /**
     * Convertește cheia regiunii în numele pentru afișare
     */
    private String getRegionDisplayName(String regionKey) {
        switch (regionKey) {
            case "transilvania": return "Transilvania";
            case "muntenia": return "Muntenia";
            case "oltenia": return "Oltenia";
            case "moldova": return "Moldova";
            case "dobrogea": return "Dobrogea";
            case "banat": return "Banat";
            case "crisana": return "Crișana";
            case "maramures": return "Maramureș";
            case "bucovina": return "Bucovina";
            default: return "Regiune";
        }
    }
    
    private void showLoading() {
        loadingOverlay.setVisibility(View.VISIBLE);
        // Fade in animation pentru loading
        loadingOverlay.setAlpha(0f);
        loadingOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }
    
    private void hideLoading() {
        // Fade out animation pentru loading
        loadingOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.AccelerateInterpolator())
                .withEndAction(() -> {
        loadingOverlay.setVisibility(View.GONE);
                })
                .start();
    }
    
    private void loadLeaderboardData() {
        showLoading();
        
        if (isRegionalMode) {
            // Încărcăm clasamentul regional din noua structură organizată
            String region = getSelectedRegion();
            Log.d(TAG, "Loading regional leaderboard from organized structure for region: " + region);
            loadRegionalLeaderboardFromOrganizedStructure(region);
        } else {
            // Încărcăm clasamentul global din noua structură
            Log.d(TAG, "Loading global leaderboard from organized structure");
            loadGlobalLeaderboardFromOrganizedStructure();
        }
    }
    
    /**
     * Încarcă clasamentul regional din noua structură organizată (pentru Transilvania)
     */
    private void loadRegionalLeaderboardFromOrganizedStructure(String region) {
        if ("transilvania".equalsIgnoreCase(region)) {
            // Pentru Transilvania folosim noua structură organizată
            loadTransilvaniaLeaderboardFromOrganizedStructure();
        } else {
            // Pentru alte regiuni folosim vechea structură prin QuizResultRepository
            CompletableFuture<List<LeaderboardEntry>> leaderboardFuture = 
                quizResultRepository.getLeaderboard(region, "quiz", 50);
            handleLeaderboardFuture(leaderboardFuture);
        }
    }
    
    /**
     * Încarcă clasamentul pentru Transilvania din noua structură organizată
     */
    private void loadTransilvaniaLeaderboardFromOrganizedStructure() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        
        // Încărcăm din noua structură: leaderboards/transilvania_quiz/entries
        db.collection("leaderboards")
            .document("transilvania_quiz")
            .collection("entries")
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                runOnUiThread(() -> {
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    
                    Log.d(TAG, "Transilvania leaderboard loaded: " + queryDocumentSnapshots.size() + " entries");
                    
                    // Convertim documentele în LeaderboardEntry objects
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        try {
                            java.util.Map<String, Object> data = queryDocumentSnapshots.getDocuments().get(i).getData();
                            if (data != null) {
                                LeaderboardEntry entry = new LeaderboardEntry();
                                entry.setUserId((String) data.get("userId"));
                                entry.setUsername((String) data.get("username"));
                                entry.setDisplayName((String) data.get("displayName"));
                                entry.setProfileImageUrl((String) data.get("profileImageUrl"));
                                
                                // Handle score - poate fi Integer sau Long
                                Object scoreObj = data.get("score");
                                if (scoreObj instanceof Long) {
                                    entry.setScore(((Long) scoreObj).intValue());
                                } else if (scoreObj instanceof Integer) {
                                    entry.setScore((Integer) scoreObj);
                                } else {
                                    entry.setScore(0);
                                }
                                
                                entry.setRegion("transilvania");
                                entry.setGameType("quiz");
                                entry.setRank(i + 1); // Setăm rangul bazat pe poziție
                                
                                // Adăugăm data realizării scorului
                                Object achievedAtObj = data.get("achievedAt");
                                if (achievedAtObj instanceof com.google.firebase.Timestamp) {
                                    entry.setAchievedAt(((com.google.firebase.Timestamp) achievedAtObj).toDate());
                                }
                                
                                entries.add(entry);
                                
                                Log.d(TAG, "Transilvania entry " + (i+1) + ": " + entry.getDisplayName() + 
                                        " - Score: " + entry.getScore());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting document " + i + " to LeaderboardEntry", e);
                        }
                    }
                    
                    // Actualizăm UI cu datele încărcate din noua structură organizată
                    updateLeaderboardUI(entries);
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading Transilvania leaderboard from organized structure", e);
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Eroare la încărcarea clasamentului Transilvania", 
                            Toast.LENGTH_SHORT).show();
                    // Fallback la date de test
                    loadTestDataAsFallback();
                });
            });
    }
    
    /**
     * Încarcă clasamentul global din structura organizată
     */
    private void loadGlobalLeaderboardFromOrganizedStructure() {
        // Pentru clasamentul global, încărcăm din toate regiunile organizate
        // Pentru moment, încărcăm doar Transilvania din noua structură
        loadTransilvaniaLeaderboardFromOrganizedStructure();
    }
    
    /**
     * Actualizează UI cu clasamentul încărcat din noua structură organizată
     */
    private void updateLeaderboardUI(List<LeaderboardEntry> entries) {
        hideLoading();
        
        Log.d(TAG, "Leaderboard UI update with organized data: " + (entries != null ? entries.size() : 0) + " entries");
        
        if (entries != null && !entries.isEmpty()) {
            // Actualizăm podiumul cu top 3
            updatePodium(entries);
            
            // Actualizăm lista cu restul clasamentului (de la poziția 4)
            List<LeaderboardEntry> remainingResults = new ArrayList<>();
            for (int i = 3; i < entries.size(); i++) {
                remainingResults.add(entries.get(i));
            }
            
            Log.d(TAG, "Remaining results for RecyclerView from organized data: " + remainingResults.size() + " entries");
            
            // Actualizăm adapter-ul cu datele din noua structură
            leaderboardAdapter.updateEntriesWithoutRecalculation(remainingResults);
            
            // Asigurăm că RecyclerView-ul este vizibil dacă avem date
            if (!remainingResults.isEmpty()) {
                leaderboardRecyclerView.setVisibility(View.VISIBLE);
                animateRecyclerViewEntrance();
                Log.d(TAG, "RecyclerView made visible with organized data: " + remainingResults.size() + " entries");
            } else {
                leaderboardRecyclerView.setVisibility(View.GONE);
                Log.d(TAG, "RecyclerView hidden - no entries beyond top 3 in organized data");
            }
        } else {
            // Nu avem date de afișat - încărcăm date de test
            Log.w(TAG, "No organized leaderboard data found, loading test data");
            loadTestDataAsFallback();
        }
    }
    
    /**
     * Handle CompletableFuture pentru clasamentele cu vechea structură (alte regiuni)
     */
    private void handleLeaderboardFuture(CompletableFuture<List<LeaderboardEntry>> leaderboardFuture) {
        leaderboardFuture
                .thenAccept(results -> {
                    runOnUiThread(() -> {
                        hideLoading();
                        
                        Log.d(TAG, "Leaderboard data received from old structure: " + (results != null ? results.size() : 0) + " entries");
                        
                        if (results != null && !results.isEmpty()) {
                            // Actualizăm podiumul cu top 3
                            updatePodium(results);
                            
                            // Actualizăm lista cu restul clasamentului (de la poziția 4)
                            // IMPORTANT: păstrăm rangurile originale, nu le recalculăm
                            List<LeaderboardEntry> remainingResults = new ArrayList<>();
                            for (int i = 3; i < results.size(); i++) {
                                remainingResults.add(results.get(i));
                            }
                            
                            Log.d(TAG, "Remaining results for RecyclerView from old structure: " + remainingResults.size() + " entries");
                            
                            // Actualizăm adapter-ul fără a recalcula rangurile
                            leaderboardAdapter.updateEntriesWithoutRecalculation(remainingResults);
                            
                            // Asigurăm că RecyclerView-ul este vizibil dacă avem date
                            if (!remainingResults.isEmpty()) {
                                leaderboardRecyclerView.setVisibility(View.VISIBLE);
                                animateRecyclerViewEntrance();
                                Log.d(TAG, "RecyclerView made visible with old structure data: " + remainingResults.size() + " entries");
                            } else {
                                leaderboardRecyclerView.setVisibility(View.GONE);
                                Log.d(TAG, "RecyclerView hidden - no entries beyond top 3 in old structure");
                            }
                        } else {
                            // Nu avem date de afișat - încărcăm date de test
                            Log.w(TAG, "No leaderboard data found in old structure, loading test data");
                            loadTestDataAsFallback();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        hideLoading();
                        Log.e(TAG, "Error loading leaderboard data from old structure", e);
                        
                        // În caz de eroare, încărcăm date de test
                        Log.w(TAG, "Loading test data due to error in old structure");
                        loadTestDataAsFallback();
                    });
                    return null;
                });
    }
    
    /**
     * Încarcă date de test când nu există date în baza de date
     */
    private void loadTestDataAsFallback() {
        Log.d(TAG, "Creating exact test leaderboard data to match image");
        
        List<LeaderboardEntry> testData = new ArrayList<>();
        
        // Date exact ca în imagine + pozițiile 4+ pentru a testa RecyclerView-ul
        testData.add(new LeaderboardEntry("david123", "David", "David", null, 60, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("utilizator", "Utilizator", "Utilizator", null, 50, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("mradita6", "mradita6", "mradita6", null, 40, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("util", "Util...", "Util...", null, 40, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("test1", "Ana M.", "Ana Maria", null, 35, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("test2", "Ion P.", "Ion Popescu", null, 32, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("test3", "Maria S.", "Maria Stan", null, 30, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("test4", "Alex R.", "Alexandru Radu", null, 28, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("test5", "Elena V.", "Elena Vasilescu", null, 25, "transilvania", "quiz"));
        testData.add(new LeaderboardEntry("test6", "Mihai D.", "Mihai Dumitrescu", null, 22, "transilvania", "quiz"));
        
        // Setăm rangurile manual
        for (int i = 0; i < testData.size(); i++) {
            testData.get(i).setRank(i + 1);
        }
        
        // Actualizăm podiumul cu top 3
        updatePodium(testData);
        
        // Actualizăm lista cu restul clasamentului (de la poziția 4)
        // IMPORTANT: păstrăm rangurile originale (4, 5, 6, etc.)
        List<LeaderboardEntry> remainingResults = new ArrayList<>();
        for (int i = 3; i < testData.size(); i++) {
            remainingResults.add(testData.get(i));
        }
        
        Log.d(TAG, "Test data - remaining results for RecyclerView: " + remainingResults.size() + " entries");
        
        leaderboardAdapter.updateEntriesWithoutRecalculation(remainingResults);
        
        // Asigurăm că RecyclerView-ul este vizibil pentru datele de test
        if (!remainingResults.isEmpty()) {
            leaderboardRecyclerView.setVisibility(View.VISIBLE);
            animateRecyclerViewEntrance();
            Log.d(TAG, "Test data - RecyclerView made visible with " + remainingResults.size() + " entries");
        } else {
            leaderboardRecyclerView.setVisibility(View.GONE);
            Log.d(TAG, "Test data - RecyclerView hidden - no entries beyond top 3");
        }
        
        Log.d(TAG, "Test data loaded successfully: " + testData.size() + " entries");
    }
    
    /**
     * Obține regiunea selectată pentru clasamentul regional
     * În aplicația reală, aceasta ar trebui să vină din preferințele utilizatorului
     * sau dintr-o interfață de selecție
     */
    private String getSelectedRegion() {
        SharedPreferences prefs = getSharedPreferences("leaderboard_prefs", MODE_PRIVATE);
        return prefs.getString("selected_region", "transilvania");
    }
    
    /**
     * Salvează regiunea selectată în preferințe
     */
    private void saveSelectedRegion(String region) {
        SharedPreferences prefs = getSharedPreferences("leaderboard_prefs", MODE_PRIVATE);
        prefs.edit().putString("selected_region", region).apply();
    }
    
    /**
     * Afișează dialogul de selecție a regiunii
     */
    private void showRegionSelectionDialog() {
        String[] regions = {
            "Transilvania", "Muntenia", "Oltenia", "Moldova", 
            "Dobrogea", "Banat", "Crișana", "Maramureș", "Bucovina"
        };
        
        String[] regionKeys = {
            "transilvania", "muntenia", "oltenia", "moldova", 
            "dobrogea", "banat", "crisana", "maramures", "bucovina"
        };
        
        String currentRegion = getSelectedRegion();
        int selectedIndex = 0;
        
        // Găsim indexul regiunii curente
        for (int i = 0; i < regionKeys.length; i++) {
            if (regionKeys[i].equals(currentRegion)) {
                selectedIndex = i;
                    break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Selectează Regiunea")
                .setSingleChoiceItems(regions, selectedIndex, null)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedItem = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (selectedItem >= 0 && selectedItem < regionKeys.length) {
                        String newRegion = regionKeys[selectedItem];
                        if (!newRegion.equals(currentRegion)) {
                            saveSelectedRegion(newRegion);
                            loadLeaderboardData(); // Reîncărcăm datele pentru noua regiune
                        }
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    
    /**
     * Metodă pentru dezvoltare: populează baza de date cu intrări de test
     * Se activează prin long click pe toolbar
     */
    private void populateDatabaseWithTestData() {
        new AlertDialog.Builder(this)
                .setTitle("Date de test")
                .setMessage("Vrei să adaugi date de test în baza de date? Aceasta va crea intrări fictive în clasament pentru dezvoltare.")
                .setPositiveButton("Da", (dialog, which) -> {
                    Toast.makeText(this, "Se creează datele de test...", Toast.LENGTH_SHORT).show();
                    createTestDataInDatabase();
                })
                .setNegativeButton("Nu", null)
                .show();
    }
    
    /**
     * Creează date de test în Firebase pentru dezvoltare
     */
    private void createTestDataInDatabase() {
        // Importăm clasa necesară pentru a crea manual intrări LeaderboardEntry
        String region = getSelectedRegion();
        String gameType = "quiz";
        
        // Creăm câteva intrări de test pentru regiunea curentă
        String[][] testUsers = {
            {"user1", "ana_popescu", "Ana Popescu", "9850"},
            {"user2", "ion_georgescu", "Ion Georgescu", "9720"},
            {"user3", "maria_dumitrescu", "Maria Dumitrescu", "9500"},
            {"user4", "alex_stan", "Alexandru Stan", "9200"},
            {"user5", "elena_radu", "Elena Radu", "8950"},
            {"user6", "mihai_popa", "Mihai Popa", "8800"},
            {"user7", "diana_ion", "Diana Ion", "8650"},
            {"user8", "cristian_marin", "Cristian Marin", "8500"},
            {"user9", "roxana_vlad", "Roxana Vlad", "8350"},
            {"user10", "stefan_pavel", "Ștefan Pavel", "8200"}
        };
        
        // Pentru a evita complexitatea, vom folosi Firestore direct
        // Aceasta este doar pentru dezvoltare
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        String leaderboardId = region + "_" + gameType;
        
        int successCount = 0;
        for (String[] userData : testUsers) {
            LeaderboardEntry entry = new LeaderboardEntry(
                userData[0], // userId
                userData[1], // username
                userData[2], // displayName
                null, // profileImageUrl
                Integer.parseInt(userData[3]), // score
                region,
                gameType
            );
            
            // Adăugăm în Firestore
            db.collection("leaderboards")
                    .document(leaderboardId)
                    .collection("entries")
                    .document(userData[0])
                    .set(entry)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Test entry created: " + userData[2]);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error creating test entry: " + userData[2], e);
                    });
        }
        
        // Așteptăm puțin și apoi reîncărcăm datele
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(this, "Date de test create! Reîncărcarea clasamentului...", Toast.LENGTH_SHORT).show();
            loadLeaderboardData();
        }, 2000);
    }
    
    private void updatePodium(List<LeaderboardEntry> results) {
        if (results == null || results.isEmpty()) {
        clearPodium();
            return;
        }
        
        // Reset la starea inițială pentru animații
        resetPodiumToInitialState();
        
        // Start animații spectaculoase
        animatePodiumEntrance();
        
        // Afișăm utilizatorii reali din baza de date
        
        // Primul loc (dacă există)
        if (results.size() > 0) {
            LeaderboardEntry first = results.get(0);
            firstPlaceName.setText(first.getDisplayNameOrUsername());
            firstPlaceScore.setText(String.valueOf(first.getScore()));
            
            // Încărcăm imaginea de profil pentru primul loc
            if (firstPlaceImage != null) {
            Glide.with(this)
                    .load(first.getProfileImageUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(firstPlaceImage);
            }
        }
        
        // Al doilea loc (dacă există)
        if (results.size() > 1) {
            LeaderboardEntry second = results.get(1);
            secondPlaceName.setText(second.getDisplayNameOrUsername());
            secondPlaceScore.setText(String.valueOf(second.getScore()));
            
            // Încărcăm imaginea de profil pentru al doilea loc (dacă există)
            if (secondPlaceImage != null) {
            Glide.with(this)
                    .load(second.getProfileImageUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(secondPlaceImage);
            }
        }
        
        // Al treilea loc (dacă există)
        if (results.size() > 2) {
            LeaderboardEntry third = results.get(2);
            thirdPlaceName.setText(third.getDisplayNameOrUsername());
            thirdPlaceScore.setText(String.valueOf(third.getScore()));
            
            // Încărcăm imaginea de profil pentru al treilea loc
            if (thirdPlaceImage != null) {
            Glide.with(this)
                    .load(third.getProfileImageUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(thirdPlaceImage);
        }
        }
        
        // Vizibilitatea RecyclerView-ului este gestionată în loadLeaderboardData()
        // pentru a evita duplicarea logicii
        
        // Start animații continue după 2 secunde
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            startContinuousGlowAnimations();
            animateFirstPlaceVictory();
        }, 2000);
    }
    
    private void clearPodium() {
        firstPlaceName.setText("");
        firstPlaceScore.setText("");
        secondPlaceName.setText("");
        secondPlaceScore.setText("");
        thirdPlaceName.setText("");
        thirdPlaceScore.setText("");
        
        if (firstPlaceImage != null) Glide.with(this).clear(firstPlaceImage);
        if (secondPlaceImage != null) Glide.with(this).clear(secondPlaceImage);
        if (thirdPlaceImage != null) Glide.with(this).clear(thirdPlaceImage);
        
        // Reset all animations and alpha values
        resetPodiumToInitialState();
    }
    
    /**
     * Spectacular podium entrance animation with staggered effects
     */
    private void animatePodiumEntrance() {
        // Animate title entrance cu spring effect
        if (tvTopPlayers != null) {
            tvTopPlayers.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .setStartDelay(300)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                    .start();
        }
        
        // Animate winner crown cu animația XML spectaculoasă
        if (winnerCrown != null) {
            android.view.animation.Animation crownAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.crown_victory_spectacular);
            winnerCrown.startAnimation(crownAnimation);
        }
        
        // Spectacular staggered podium animations cu bounce effect
        // Gold first (cel mai spectaculos)
        if (firstPlaceContainer != null) {
            firstPlaceContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1200)
                    .setStartDelay(800)
                    .setInterpolator(new android.view.animation.BounceInterpolator())
                    .withEndAction(() -> {
                        // Confetti effect pentru primul loc
                        animateConfettiForWinner();
                    })
                    .start();
        }
        
        // Silver second
        if (secondPlaceContainer != null) {
            secondPlaceContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .setStartDelay(600)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(0.8f))
                    .start();
        }
        
        // Bronze third
        if (thirdPlaceContainer != null) {
            thirdPlaceContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .setStartDelay(1000)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(0.6f))
                    .start();
        }
        
        // Animate sparkles with spectacular effects
        animateSparkles();
    }
    
    /**
     * Animate the sparkles with spectacular twinkling effect
     */
    private void animateSparkles() {
        if (sparkles != null) {
            for (int i = 0; i < sparkles.length; i++) {
                final View sparkle = sparkles[i];
                if (sparkle != null) {
                    // Set initial state
                    sparkle.setAlpha(0f);
                    sparkle.setScaleX(0f);
                    sparkle.setScaleY(0f);
                    sparkle.setRotation(0f);
                    
                    // Staggered spectacular sparkle animation
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        sparkle.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .rotation(360f)
                                .setDuration(1000)
                                .setInterpolator(new android.view.animation.OvershootInterpolator(2f))
                                .withEndAction(() -> {
                                    // Start continuous twinkling
                                    startContinuousTwinkling(sparkle);
                                })
                                .start();
                    }, 1200 + (i * 300)); // Stagger each sparkle by 300ms
                }
            }
        }
    }
    
    /**
     * Confetti effect pentru câștigător cu animații XML și efecte spectaculoase
     */
    private void animateConfettiForWinner() {
        if (firstPlaceContainer != null) {
            // Folosim animația XML pentru confetti burst
            android.view.animation.Animation confettiAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.confetti_explosion);
            firstPlaceContainer.startAnimation(confettiAnimation);
            
            // Adăugăm și un efect de glow pentru primul loc
            firstPlaceContainer.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(500)
                    .setStartDelay(400)
                    .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        firstPlaceContainer.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(500)
                                .start();
                    })
                    .start();
            
            // Efect de highlight pe nume și scor
            if (firstPlaceName != null) {
                firstPlaceName.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(300)
                        .setStartDelay(600)
                        .withEndAction(() -> {
                            firstPlaceName.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300)
                                    .start();
                        })
                        .start();
            }
            
            if (firstPlaceScore != null) {
                firstPlaceScore.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(300)
                        .setStartDelay(700)
                        .withEndAction(() -> {
                            firstPlaceScore.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300)
                                    .start();
                        })
                        .start();
            }
        }
    }
    
    /**
     * Continuous twinkling pentru sparkles
     */
    private void startContinuousTwinkling(View sparkle) {
        sparkle.animate()
                .alpha(0.3f)
                .setDuration(1500)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    sparkle.animate()
                            .alpha(1f)
                            .setDuration(1500)
                            .withEndAction(() -> startContinuousTwinkling(sparkle))
                            .start();
                })
                .start();
    }
    
    /**
     * Animații pulsante pentru podium
     */
    private void animatePodiumPulse() {
        // Pulse pentru primul loc
        if (firstPlaceContainer != null) {
            startPulseAnimation(firstPlaceContainer, 3000);
        }
        
        // Pulse pentru al doilea loc 
        if (secondPlaceContainer != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                startPulseAnimation(secondPlaceContainer, 3500);
            }, 500);
        }
        
        // Pulse pentru al treilea loc
        if (thirdPlaceContainer != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                startPulseAnimation(thirdPlaceContainer, 4000);
            }, 1000);
        }
    }
    
    /**
     * Start pulse animation pentru un view
     */
    private void startPulseAnimation(View view, long duration) {
        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(duration / 2)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(duration / 2)
                            .withEndAction(() -> startPulseAnimation(view, duration))
                            .start();
                })
                .start();
    }
    
    /**
     * Special victory animation for first place
     */
    private void animateFirstPlaceVictory() {
        // Crown animation is handled by the main crown
        
        // Apply dynamic gradient background
        if (firstPlaceContainer != null) {
            firstPlaceContainer.setBackgroundResource(R.drawable.podium_first_exact_background);
        }
    }
    
    /**
     * Animație pentru intrarea RecyclerView-ului
     */
    private void animateRecyclerViewEntrance() {
        if (leaderboardRecyclerView != null) {
            leaderboardRecyclerView.setAlpha(0f);
            leaderboardRecyclerView.setTranslationY(100f);
            
            leaderboardRecyclerView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(1500)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }
    }
    
    /**
     * Start continuous glow animations for all podium positions
     */
    private void startContinuousGlowAnimations() {
        // Glow effects are built into the drawable backgrounds
        applyDynamicGradients();
        
        // Adăugăm animații pulsante pentru containere
        animatePodiumPulse();
    }
    
    /**
     * Animație pentru schimbarea tab-urilor
     */
    private void animateTabSwitch() {
        // Fade out content
        if (leaderboardRecyclerView != null) {
            leaderboardRecyclerView.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .start();
        }
        
        // Scale down podium containers
        animatePodiumScaleDown();
    }
    
    /**
     * Scale down animation pentru podium la schimbarea tab-urilor
     */
    private void animatePodiumScaleDown() {
        if (firstPlaceContainer != null) {
            firstPlaceContainer.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .alpha(0.5f)
                    .setDuration(300)
                    .start();
        }
        
        if (secondPlaceContainer != null) {
            secondPlaceContainer.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .alpha(0.5f)
                    .setDuration(300)
                    .start();
        }
        
        if (thirdPlaceContainer != null) {
            thirdPlaceContainer.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .alpha(0.5f)
                    .setDuration(300)
                    .start();
        }
    }
    
    /**
     * Apply dynamic gradients to podium containers
     */
    private void applyDynamicGradients() {
        if (firstPlaceContainer != null) {
            firstPlaceContainer.setBackgroundResource(R.drawable.podium_first_exact_background);
        }
        
        if (secondPlaceContainer != null) {
            secondPlaceContainer.setBackgroundResource(R.drawable.podium_second_exact_background);
        }
        
        if (thirdPlaceContainer != null) {
            thirdPlaceContainer.setBackgroundResource(R.drawable.podium_third_exact_background);
        }
    }
    
    /**
     * Reset podium to initial state for new animations
     */
    private void resetPodiumToInitialState() {
        // Reset crown
        if (winnerCrown != null) {
            winnerCrown.setAlpha(0f);
            winnerCrown.setScaleX(0.5f);
            winnerCrown.setScaleY(0.5f);
        }
        

        
        // Reset title
        if (tvTopPlayers != null) {
            tvTopPlayers.setAlpha(0f);
            tvTopPlayers.setTranslationY(50f);
            tvTopPlayers.setScaleX(0.8f);
            tvTopPlayers.setScaleY(0.8f);
        }
        
        // Reset containers
        if (firstPlaceContainer != null) {
            firstPlaceContainer.setAlpha(0f);
            firstPlaceContainer.setTranslationY(100f);
            firstPlaceContainer.setScaleX(0.8f);
            firstPlaceContainer.setScaleY(0.8f);
        }
        
        if (secondPlaceContainer != null) {
            secondPlaceContainer.setAlpha(0f);
            secondPlaceContainer.setTranslationY(100f);
            secondPlaceContainer.setScaleX(0.8f);
            secondPlaceContainer.setScaleY(0.8f);
        }
        
        if (thirdPlaceContainer != null) {
            thirdPlaceContainer.setAlpha(0f);
            thirdPlaceContainer.setTranslationY(100f);
            thirdPlaceContainer.setScaleX(0.8f);
            thirdPlaceContainer.setScaleY(0.8f);
        }
        
        // Reset sparkles
        if (sparkles != null) {
            for (View sparkle : sparkles) {
                if (sparkle != null) {
                    sparkle.setAlpha(0f);
                    sparkle.clearAnimation();
                }
            }
        }
        
        // Glow effects are handled by drawable backgrounds
    }
} 