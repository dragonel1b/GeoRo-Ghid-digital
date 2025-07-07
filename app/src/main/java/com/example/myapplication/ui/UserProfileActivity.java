package com.example.myapplication.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.LoginActivity;
import com.example.myapplication.ui.LeaderboardActivity;
import com.example.myapplication.adapter.QuizResultAdapter;
import com.example.myapplication.adapter.RecentQuizAdapter;
import com.example.myapplication.adapter.RegionProgressAdapter;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.model.UserProfile;
import com.example.myapplication.model.RegionProgress;
import com.example.myapplication.model.ActivityItem;
import com.example.myapplication.repository.QuizResultRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Activitate pentru afișarea și editarea profilului utilizatorului
 */
public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    
    // Repository
    private QuizResultRepository quizResultRepository;
    
    // UI Components
    private ImageView profileImageView;
    private TextView userDisplayNameText;
    private EditText displayNameEditText;
    private TextView emailTextView;
    private TextView userLevelText;
    private TextView currentRankText;
    private TextView bestScoreText;
    private RecyclerView regionsRecyclerView;
    private View regionsEmptyState;
    private LinearLayout regionsHeaderLayout;
    private MaterialButton updateProfileButton;
    private FloatingActionButton changeImageButton;
    private MaterialButton viewLeaderboardButton;
    private ProgressBar progressBar;
    private View progressBarContainer;
    
    // Data
    private UserProfile userProfile;
    private Uri selectedImageUri;
    private List<QuizResult> recentQuizResults = new ArrayList<>();
    private RegionProgressAdapter regionProgressAdapter;
    private List<RegionProgress> regionProgressList = new ArrayList<>();
    
    // Activity Result Launcher pentru selectarea imaginii
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                            .load(selectedImageUri)
                            .circleCrop()
                            .into(profileImageView);
                }
            });
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        
        // Inițializăm Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        
        // Inițializăm repository-ul
        quizResultRepository = QuizResultRepository.getInstance();
        
        // Inițializăm UI components
        initializeViews();
        
        // Verificăm dacă utilizatorul este autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Utilizatorul nu este autentificat, îl redirecționăm către ecranul de autentificare
            redirectToLogin();
            return;
        }
        
        // Încărcăm datele utilizatorului
        loadUserProfile();
        
        // Load recent quiz results immediately (will show placeholder if no data)
        loadRecentQuizResults();
        
        // Afișăm un mesaj informativ pentru utilizatorii noi (doar pentru dezvoltare)
        showDevelopmentInfo();
    }
    
    private void initializeViews() {
        profileImageView = findViewById(R.id.profileImageView);
        userDisplayNameText = findViewById(R.id.userDisplayNameText);
        displayNameEditText = findViewById(R.id.displayNameEditText);
        emailTextView = findViewById(R.id.emailTextView);
        userLevelText = findViewById(R.id.userLevelText);
        currentRankText = findViewById(R.id.currentRankText);
        bestScoreText = findViewById(R.id.bestScoreText);
        regionsRecyclerView = findViewById(R.id.regionsRecyclerView);
        regionsEmptyState = findViewById(R.id.regionsEmptyState);
        regionsHeaderLayout = findViewById(R.id.regionsHeaderLayout);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        changeImageButton = findViewById(R.id.changeImageButton);
        viewLeaderboardButton = findViewById(R.id.viewLeaderboardButton);
        progressBar = findViewById(R.id.progressBar);
        progressBarContainer = findViewById(R.id.progressBarContainer);
        
        // Initialize regions RecyclerView
        setupRegionsRecyclerView();
        
        // DEBUG: Add test regions immediately for debugging
        if (regionProgressList.isEmpty()) {
            addDebugRegions();
        }
        
        // Configurăm toolbar-ul
        setupToolbar();
        
        // Configurăm listeners-ii
        setupListeners();
    }
    

    
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            
            // Clear any title to prevent overlap with user name
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("");
            }
            
            // Debug: Long click pentru a popula baza de date cu date de test
            toolbar.setOnLongClickListener(v -> {
                populateUserProfileWithTestData();
                return true;
            });
        }
        
        // CollapsingToolbarLayout no longer exists in layout
    }
    
    private void setupListeners() {
        updateProfileButton.setOnClickListener(v -> updateUserProfile());
        changeImageButton.setOnClickListener(v -> selectProfileImage());
        viewLeaderboardButton.setOnClickListener(v -> openLeaderboard());
        
        // Long click pe header-ul regiunilor pentru a adăuga date de test
        if (regionsHeaderLayout != null) {
            regionsHeaderLayout.setOnLongClickListener(v -> {
                createRegionTestData();
                return true;
            });
        }
    }
    
    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;
        
        // Afișăm progress indicator
        showProgressBar();
        
        // Încărcăm profilul utilizatorului din repository
        quizResultRepository.getUserProfile(currentUser.getUid())
                .thenAccept(profile -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        hideProgressBar();
                        
                        if (profile != null) {
                            // Salvăm profilul utilizatorului
                            userProfile = profile;
                            
                            // Afișăm datele utilizatorului cu animație
                            displayUserProfileWithAnimation();
                        } else {
                            // Creăm un profil nou pentru utilizator
                            createNewUserProfile(currentUser);
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        hideProgressBar();
                        
                        // Afișăm mesaj de eroare
                        showErrorMessage(getString(R.string.error_loading_profile));
                        
                        Log.e(TAG, "Error loading user profile", e);
                    });
                    return null;
                });
    }
    
    private void createNewUserProfile(FirebaseUser currentUser) {
        // Creăm un profil nou pentru utilizator cu valori implicite
        userProfile = new UserProfile();
        userProfile.setUserId(currentUser.getUid());
        userProfile.setEmail(currentUser.getEmail());
        
        // Setăm numele de afișare din Firebase Auth (dacă există)
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            userProfile.setDisplayName(currentUser.getDisplayName());
        } else {
            // Folosim adresa de email ca nume de afișare implicit sau un nume generat
            String email = currentUser.getEmail();
            if (email != null && email.contains("@")) {
                String username = email.substring(0, email.indexOf("@"));
                userProfile.setDisplayName(capitalizeFirstLetter(username));
            } else {
                userProfile.setDisplayName("Utilizator Român");
            }
        }
        
        // Setăm URL-ul imaginii de profil din Firebase Auth (dacă există)
        if (currentUser.getPhotoUrl() != null) {
            userProfile.setProfileImageUrl(currentUser.getPhotoUrl().toString());
        }
        
        // Setăm valori implicite pentru un utilizator nou (în loc de 0)
        userProfile.setQuizPoints(100); // Puncte de început pentru a face profilul mai interesant
        userProfile.setTotalQuizzesTaken(1); // Pentru a afișa statistici
        userProfile.setCorrectAnswers(17); // 17 răspunsuri corecte
        userProfile.setTotalAnswers(20); // din 20 de întrebări (85% acuratețe)
        userProfile.setPremiumUser(false);
        
        // Salvăm profilul utilizatorului în repository
        quizResultRepository.saveUserProfile(userProfile)
                .thenAccept(success -> {
                    runOnUiThread(() -> {
                        if (success) {
                            // Afișăm datele utilizatorului
                            displayUserProfileWithAnimation();
                        } else {
                            // Afișăm mesaj de eroare
                            showErrorMessage(getString(R.string.error_creating_profile));
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        // Afișăm mesaj de eroare
                        showErrorMessage(getString(R.string.error_creating_profile));
                        
                        Log.e(TAG, "Error creating user profile", e);
                    });
                    return null;
                });
    }
    
    private void showProgressBar() {
        if (progressBarContainer != null) {
            progressBarContainer.setVisibility(View.VISIBLE);
            progressBarContainer.setAlpha(0f);
            progressBarContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }
    
    private void hideProgressBar() {
        if (progressBarContainer != null && progressBarContainer.getVisibility() == View.VISIBLE) {
            progressBarContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> progressBarContainer.setVisibility(View.GONE))
                    .start();
        }
    }
    
    private void displayUserProfileWithAnimation() {
        if (userProfile == null) return;
        
        // Afișăm numele de afișare în header
        if (userDisplayNameText != null) {
            userDisplayNameText.setText(userProfile.getDisplayName() != null ? 
                userProfile.getDisplayName() : "Utilizator Român");
        }
        
        // Afișăm numele în formular de editare
        if (displayNameEditText != null) {
            displayNameEditText.setText(userProfile.getDisplayName());
        }
        
        // Afișăm adresa de email
        if (emailTextView != null) {
            emailTextView.setText(userProfile.getEmail());
        }
        
        // Afișăm nivelul utilizatorului
        updateUserLevel();
        
        // Afișăm badge-urile premium
        updatePremiumBadges();
        
        // Afișăm imaginea de profil cu animație
        updateProfileImage();
        
        // Afișăm statisticile rapide
        updateQuickStats();
        
        // Afișăm progresul regiunilor
        loadRegionProgress();
        
        // Încărcăm rezultatele recente (always load this to show placeholder if needed)
        loadRecentQuizResults();
        
        // Încărcăm cel mai bun scor din Transilvania pentru leaderboard
        loadTransilvaniaBestScore();
        
        // Logare pentru debugging Transilvania data
        logTransilvaniaDataStatus();
    }
    
    private void updateUserLevel() {
        if (userLevelText != null && userProfile != null) {
            String level = calculateUserLevel(userProfile.getQuizPoints());
            userLevelText.setText(level);
        }
    }
    
    private String calculateUserLevel(int points) {
        if (points >= 5000) return "Maestru Român";
        else if (points >= 3000) return "Expert Regional";
        else if (points >= 1500) return "Cunoscător Avançat";
        else if (points >= 500) return "Explorator Român";
        else if (points >= 100) return "Începător Curios";
        else return "Nou Venit";
    }
    
    private void updatePremiumBadges() {
        // Premium badges no longer exist in the simplified layout
        // User level is now displayed in userLevelText instead
    }
    
    private void updateProfileImage() {
        if (profileImageView == null) return;
        
        // Afișăm imaginea de profil (dacă există)
        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(userProfile.getProfileImageUrl())
                    .placeholder(R.drawable.default_profile_image)
                    .error(R.drawable.default_profile_image)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.default_profile_image);
        }
        
        // Aplicăm animație pentru imaginea de profil
        profileImageView.setScaleX(0.7f);
        profileImageView.setScaleY(0.7f);
        profileImageView.setAlpha(0f);
        profileImageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();
    }
    
    private void updateQuickStats() {
        if (userProfile == null) return;
        
        // Obținem regiunea selectată din LeaderboardActivity pentru consistență
        String selectedRegion = getSelectedRegionForRank();
        String gameType = "quiz";
        
        // Obținem rangul real din baza de date
        quizResultRepository.getCurrentUserRank(selectedRegion, gameType)
            .thenAccept(rank -> {
                runOnUiThread(() -> {
                    if (currentRankText != null) {
                        String regionDisplayName = getRegionDisplayName(selectedRegion);
                        if (rank != null && rank > 0) {
                            currentRankText.setText("#" + rank + " în " + regionDisplayName);
                            Log.d(TAG, "User rank updated: " + rank + " in " + regionDisplayName);
                        } else {
                            currentRankText.setText("N/A în " + regionDisplayName);
                            Log.d(TAG, "User rank not found in leaderboard for " + regionDisplayName);
                        }
                        
                        // Facem text-ul clickable pentru schimbarea regiunii
                        currentRankText.setOnClickListener(v -> showRegionSelectionDialog());
                        currentRankText.setTextColor(getResources().getColor(R.color.leaderboard_gradient_start, getTheme()));
                    }
                });
            })
            .exceptionally(e -> {
                runOnUiThread(() -> {
                    if (currentRankText != null) {
                        currentRankText.setText("N/A");
                    }
                    Log.e(TAG, "Error loading user rank", e);
                });
                return null;
            });
        
        // Obținem cel mai bun scor real din rezultatele quiz-urilor
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            quizResultRepository.getUserQuizResults(currentUser.getUid(), 0)
                .thenAccept(results -> {
                    runOnUiThread(() -> {
                            if (results != null && !results.isEmpty()) {
                                // Găsim cel mai bun scor din toate rezultatele
                                int bestScore = results.stream()
                                    .mapToInt(QuizResult::getScore)
                                    .max()
                                    .orElse(0);
                                bestScoreText.setText(String.valueOf(bestScore));
                                Log.d(TAG, "Best score updated: " + bestScore);
                            } else {
                                bestScoreText.setText("0");
                                Log.d(TAG, "No quiz results found for user");
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        if (bestScoreText != null) {
                            bestScoreText.setText("0");
                        }
                        Log.e(TAG, "Error loading user quiz results", e);
                    });
                    return null;
                });
        }
    }
    
    /**
     * Obține regiunea selectată pentru calcul rang, consistent cu LeaderboardActivity
     */
    private String getSelectedRegionForRank() {
        android.content.SharedPreferences prefs = getSharedPreferences("leaderboard_prefs", MODE_PRIVATE);
        return prefs.getString("selected_region", "transilvania");
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
    
    /**
     * Afișează dialogul de selecție a regiunii pentru calculul rangului
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
        
        String currentRegion = getSelectedRegionForRank();
        int selectedIndex = 0;
        
        // Găsim indexul regiunii curente
        for (int i = 0; i < regionKeys.length; i++) {
            if (regionKeys[i].equals(currentRegion)) {
                selectedIndex = i;
                break;
            }
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Selectează Regiunea pentru Rang")
                .setSingleChoiceItems(regions, selectedIndex, null)
                .setPositiveButton("OK", (dialog, which) -> {
                    androidx.appcompat.app.AlertDialog alertDialog = (androidx.appcompat.app.AlertDialog) dialog;
                    int selectedItem = alertDialog.getListView().getCheckedItemPosition();
                    if (selectedItem >= 0 && selectedItem < regionKeys.length) {
                        String newRegion = regionKeys[selectedItem];
                        if (!newRegion.equals(currentRegion)) {
                            // Salvăm noua regiune
                            android.content.SharedPreferences prefs = getSharedPreferences("leaderboard_prefs", MODE_PRIVATE);
                            prefs.edit().putString("selected_region", newRegion).apply();
                            // Reîncărcăm statisticile cu noua regiune
                            updateQuickStats();
                            Toast.makeText(this, "Regiunea pentru rang actualizată: " + getRegionDisplayName(newRegion), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Anulează", null)
                .show();
    }
    

    
    /**
     * Configurează RecyclerView-ul pentru afișarea progresului regiunilor
     */
    private void setupRegionsRecyclerView() {
        Log.d(TAG, "Setting up regions RecyclerView");
        if (regionsRecyclerView != null) {
            regionProgressAdapter = new RegionProgressAdapter(regionProgressList);
            regionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            regionsRecyclerView.setAdapter(regionProgressAdapter);
            
            // Setează click listener pentru regiuni
            regionProgressAdapter.setOnRegionClickListener(regionProgress -> {
                // Poți adăuga aici navigarea către activitatea regiunii specifice
                Log.d(TAG, "Region clicked: " + regionProgress.getRegionName());
                Toast.makeText(this, "Explorează " + regionProgress.getRegionName(), Toast.LENGTH_SHORT).show();
            });
            
            Log.d(TAG, "RecyclerView setup complete with adapter: " + (regionProgressAdapter != null));
        } else {
            Log.e(TAG, "regionsRecyclerView is null!");
        }
    }
    
    /**
     * Încarcă progresul utilizatorului pentru fiecare regiune
     */
    private void loadRegionProgress() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Cannot load region progress - current user is null");
            return;
        }
        
        Log.d(TAG, "Loading region progress from database for user: " + currentUser.getUid());
        
        // Încărcăm toate rezultatele utilizatorului
        quizResultRepository.getUserQuizResults(currentUser.getUid(), 0)
                .thenAccept(results -> {
                    Log.d(TAG, "Received quiz results: " + (results != null ? results.size() : "null"));
                    runOnUiThread(() -> {
                        // Creăm lista de regiuni cu progresul lor
                        List<RegionProgress> regions = createRegionProgressList(results);
                        Log.d(TAG, "Created regions list with " + regions.size() + " regions");
                        
                        // Actualizăm UI-ul
                        updateRegionsUI(regions);
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error loading region progress", e);
                        // În loc să afișez empty state, afișez toate regiunile fără progres
                        List<RegionProgress> emptyRegions = createRegionProgressList(null);
                        updateRegionsUI(emptyRegions);
                    });
                    return null;
                });
    }
    
    /**
     * Creează lista de progres pentru regiuni bazată pe rezultatele quiz-urilor
     */
    private List<RegionProgress> createRegionProgressList(List<QuizResult> results) {
        List<RegionProgress> regions = new ArrayList<>();
        
        // Definim regiunile României cu iconițele lor
        String[][] regionData = {
            {"transilvania", "Transilvania", "ic_mountain"},
            {"muntenia", "Muntenia", "ic_castle"},
            {"moldova", "Moldova", "ic_wine"},
            {"oltenia", "Oltenia", "ic_forest"},
            {"dobrogea", "Dobrogea", "ic_beach"},
            {"banat", "Banat", "ic_music"},
            {"crisana", "Crișana", "ic_field"},
            {"maramures", "Maramureș", "ic_wood"},
            {"bucovina", "Bucovina", "ic_church"}
        };
        
        for (String[] region : regionData) {
            String regionKey = region[0];
            String regionName = region[1];
            String iconName = region[2];
            
            // Obținem resource ID pentru iconița regiunii
            int iconResource = getResources().getIdentifier(iconName, "drawable", getPackageName());
            if (iconResource == 0) {
                iconResource = R.drawable.ic_explore; // Iconița default
            }
            
            // Creăm obiectul RegionProgress
            RegionProgress regionProgress = new RegionProgress(regionKey, regionName, iconResource);
            
            // Calculăm progresul pentru această regiune
            if (results != null && !results.isEmpty()) {
                List<QuizResult> regionResults = results.stream()
                    .filter(result -> regionKey.equals(result.getRegion()))
                    .collect(java.util.stream.Collectors.toList());
                
                if (!regionResults.isEmpty()) {
                    regionProgress.setQuizzesCompleted(regionResults.size());
                    regionProgress.setHasCompletedQuizzes(true);
                    
                    // Găsim cel mai bun scor
                    int bestScore = regionResults.stream()
                        .mapToInt(QuizResult::getScore)
                        .max()
                        .orElse(0);
                    regionProgress.setBestScore(bestScore);
                    
                    // Calculăm acuratețea medie
                    double averageAccuracy = regionResults.stream()
                        .mapToDouble(QuizResult::getAccuracy)
                        .average()
                        .orElse(0.0);
                    regionProgress.setAverageAccuracy((float) averageAccuracy);
                }
            }
            
            // Adăugăm toate regiunile, indiferent de progres
            regions.add(regionProgress);
        }
        
        // Sortăm: primul regiunile cu progres (după cel mai bun scor), apoi cele fără progres (alfabetic)
        regions.sort((r1, r2) -> {
            // Dacă ambele au progres, sortează după cel mai bun scor (descrescător)
            if (r1.hasCompletedQuizzes() && r2.hasCompletedQuizzes()) {
                return Integer.compare(r2.getBestScore(), r1.getBestScore());
            }
            // Dacă doar una are progres, pune-o pe aceea primul
            if (r1.hasCompletedQuizzes() && !r2.hasCompletedQuizzes()) {
                return -1;
            }
            if (!r1.hasCompletedQuizzes() && r2.hasCompletedQuizzes()) {
                return 1;
            }
            // Dacă niciuna nu are progres, sortează alfabetic
            return r1.getRegionName().compareTo(r2.getRegionName());
        });
        
        Log.d(TAG, "Region progress list created: " + regions.size() + " total regions");
        
        return regions;
    }
    
    /**
     * Actualizează UI-ul cu lista de regiuni
     */
    private void updateRegionsUI(List<RegionProgress> regions) {
        // Afișăm întotdeauna regiunile, nu mai avem stare goală
        showRegionsWithProgress(regions);
    }
    
    /**
     * Afișează starea goală când nu există progres în regiuni
     */
    private void showEmptyRegionsState() {
        if (regionsRecyclerView != null) {
            regionsRecyclerView.setVisibility(View.GONE);
        }
        if (regionsEmptyState != null) {
            regionsEmptyState.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Showing empty regions state");
    }
    
    /**
     * Afișează regiunile cu progres
     */
    private void showRegionsWithProgress(List<RegionProgress> regions) {
        Log.d(TAG, "showRegionsWithProgress called with " + regions.size() + " regions");
        
        if (regionsEmptyState != null) {
            regionsEmptyState.setVisibility(View.GONE);
        }
        if (regionsRecyclerView != null) {
            regionsRecyclerView.setVisibility(View.VISIBLE);
        }
        
        // Actualizăm adapter-ul
        regionProgressList.clear();
        regionProgressList.addAll(regions);
        if (regionProgressAdapter != null) {
            regionProgressAdapter.notifyDataSetChanged();
            Log.d(TAG, "Adapter updated with " + regionProgressList.size() + " items");
        } else {
            Log.e(TAG, "regionProgressAdapter is null!");
        }
        
        // Debug: listează regiunile
        for (int i = 0; i < regions.size(); i++) {
            RegionProgress region = regions.get(i);
            Log.d(TAG, "Region " + i + ": " + region.getRegionName() + " - " + 
                (region.hasCompletedQuizzes() ? "has progress" : "no progress"));
        }
        
        Log.d(TAG, "Showing " + regions.size() + " regions with progress");
    }
    
    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }
    
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    

    
    /**
     * Încarcă activitatea recentă din toate sursele - incluind Transilvania Quiz
     */
    private void loadRecentQuizResults() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "Cannot load recent quiz results: user is null");
            return;
        }
        
        Log.d(TAG, "Loading recent activity for user: " + currentUser.getUid());
        
        // Încărcăm activitatea din colecția specializată pentru istoric
        loadRecentActivityFromHistory(currentUser.getUid());
        
        // Încărcăm și din colecția de quiz results pentru compatibilitate
        loadRecentActivityFromQuizResults(currentUser.getUid());
    }
    
    /**
     * Încarcă activitatea recentă din colecția user_activity_history
     */
    private void loadRecentActivityFromHistory(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("user_activity_history")
            .document(userId)
            .collection("recent_activities")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(10) // Ultimele 10 activități
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                    runOnUiThread(() -> {
                    Log.d(TAG, "Recent activity from history loaded: " + queryDocumentSnapshots.size() + " items");
                    
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<ActivityItem> recentActivities = new ArrayList<>();
                        
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            ActivityItem activity = createActivityItemFromDocument(doc);
                            if (activity != null) {
                                recentActivities.add(activity);
                            }
                        }
                        
                        // Afișăm activitățile în UI
                        displayRecentActivities(recentActivities);
                            
                            // Debug logging
                        Log.d(TAG, "=== RECENT ACTIVITIES DEBUG ===");
                        for (int i = 0; i < recentActivities.size(); i++) {
                            ActivityItem activity = recentActivities.get(i);
                            Log.d(TAG, String.format("Activity %d: %s - Score: %d, Accuracy: %.1f%%, Date: %s", 
                                i + 1, 
                                activity.getDisplayName(),
                                activity.getScore(),
                                activity.getAccuracy(),
                                activity.getCompletedAt() != null ? activity.getCompletedAt().toString() : "null"
                                ));
                            }
                            Log.d(TAG, "=== END DEBUG ===");
                    } else {
                        Log.d(TAG, "No recent activities found in history");
                        // Încărcăm din quiz_results ca fallback
                        loadRecentActivityFromQuizResults(userId);
                    }
                });
            })
            .addOnFailureListener(e -> {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading recent activity from history", e);
                    // Fallback la quiz_results
                    loadRecentActivityFromQuizResults(userId);
                });
            });
    }
    
    /**
     * Încarcă activitatea din quiz_results (fallback pentru compatibilitate)
     */
    private void loadRecentActivityFromQuizResults(String userId) {
        // Load recent quiz results for internal data tracking
        quizResultRepository.getUserQuizResults(userId, 5)
                .thenAccept(results -> {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Recent quiz results loaded (fallback): " + (results != null ? results.size() : 0) + " items");
                        
                        if (results != null && !results.isEmpty()) {
                            recentQuizResults.clear();
                            recentQuizResults.addAll(results);
                            
                            // Convertim QuizResult în ActivityItem pentru afișare uniformă
                            List<ActivityItem> activities = convertQuizResultsToActivities(results);
                            displayRecentActivities(activities);
                            
                            Log.d(TAG, "Recent quiz results data loaded successfully (fallback)");
                        } else {
                            Log.d(TAG, "No recent quiz results found");
                            displayNoRecentActivity();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error loading recent quiz results", e);
                        displayNoRecentActivity();
                    });
                    return null;
                });
    }

    /**
     * Creează un ActivityItem din DocumentSnapshot
     */
    private ActivityItem createActivityItemFromDocument(DocumentSnapshot doc) {
        try {
            ActivityItem activity = new ActivityItem();
            activity.setActivityType(doc.getString("activityType"));
            activity.setDisplayName(doc.getString("displayName"));
            activity.setDescription(doc.getString("description"));
            activity.setScore(doc.getLong("score") != null ? doc.getLong("score").intValue() : 0);
            activity.setAccuracy(doc.getDouble("accuracy") != null ? doc.getDouble("accuracy").floatValue() : 0f);
            activity.setCorrectAnswers(doc.getLong("correctAnswers") != null ? doc.getLong("correctAnswers").intValue() : 0);
            activity.setTotalQuestions(doc.getLong("totalQuestions") != null ? doc.getLong("totalQuestions").intValue() : 0);
            activity.setMaxStreak(doc.getLong("maxStreak") != null ? doc.getLong("maxStreak").intValue() : 0);
            activity.setRegion(doc.getString("region"));
            activity.setGameType(doc.getString("gameType"));
            activity.setCompletedAt(doc.getDate("completedAt"));
            activity.setDuration(doc.getLong("duration") != null ? doc.getLong("duration") : 0L);
            activity.setIconResource(doc.getString("iconResource"));
            activity.setColorTheme(doc.getString("colorTheme"));
            
            return activity;
        } catch (Exception e) {
            Log.e(TAG, "Error creating ActivityItem from document", e);
            return null;
        }
    }
    
    /**
     * Convertește QuizResult în ActivityItem pentru afișare uniformă
     */
    private List<ActivityItem> convertQuizResultsToActivities(List<QuizResult> quizResults) {
        List<ActivityItem> activities = new ArrayList<>();
        
        for (QuizResult result : quizResults) {
            ActivityItem activity = new ActivityItem();
            activity.setActivityType(result.getRegion() + "_quiz");
            activity.setDisplayName("Quiz " + capitalizeRegionName(result.getRegion()));
            activity.setDescription("Quiz despre " + capitalizeRegionName(result.getRegion()) + 
                    " - " + result.getCorrectAnswers() + "/" + result.getTotalQuestions() + " corecte");
            activity.setScore(result.getScore());
            activity.setAccuracy(result.getAccuracy());
            activity.setCorrectAnswers(result.getCorrectAnswers());
            activity.setTotalQuestions(result.getTotalQuestions());
            activity.setMaxStreak(result.getMaxStreak());
            activity.setRegion(result.getRegion());
            activity.setGameType(result.getGameType());
            activity.setCompletedAt(result.getCompletedAt());
            activity.setIconResource("ic_" + result.getRegion());
            activity.setColorTheme(result.getRegion() + "_primary");
            
            activities.add(activity);
        }
        
        return activities;
    }
    
    /**
     * Afișează activitățile recente în UI
     */
    private void displayRecentActivities(List<ActivityItem> activities) {
        // Aici puteți adăuga codul pentru afișarea în RecyclerView sau alt component UI
        // Pentru moment, doar logăm datele
        
        Log.d(TAG, "Displaying " + activities.size() + " recent activities");
        
        // Exemplu de afișare simplă pentru debugging
        for (ActivityItem activity : activities) {
            Log.d(TAG, "Activity: " + activity.getDisplayName() + 
                    ", Score: " + activity.getScore() + 
                    ", Date: " + (activity.getCompletedAt() != null ? activity.getCompletedAt().toString() : "N/A"));
        }
        
        // TODO: Implementați afișarea în UI (RecyclerView, etc.)
        // updateRecentActivityRecyclerView(activities);
    }
    
    /**
     * Afișează mesaj când nu există activitate recentă
     */
    private void displayNoRecentActivity() {
        Log.d(TAG, "No recent activity to display");
        // TODO: Afișați placeholder pentru lipsa activității
    }
    
    /**
     * Încarcă cel mai bun scor pentru leaderboard din Transilvania
     */
    private void loadTransilvaniaBestScore() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("user_best_scores")
            .document(currentUser.getUid())
            .collection("regional_scores")
            .document("transilvania_quiz")
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                runOnUiThread(() -> {
                    if (documentSnapshot.exists()) {
                        Long bestScore = documentSnapshot.getLong("score");
                        Double accuracy = documentSnapshot.getDouble("accuracy");
                        Long maxStreak = documentSnapshot.getLong("maxStreak");
                        Date achievedAt = documentSnapshot.getDate("achievedAt");
                        
                        Log.d(TAG, "Transilvania best score loaded: " + bestScore);
                        
                        // Actualizați UI cu cel mai bun scor
                        updateTransilvaniaBestScoreUI(bestScore != null ? bestScore.intValue() : 0,
                                accuracy != null ? accuracy.floatValue() : 0f,
                                maxStreak != null ? maxStreak.intValue() : 0,
                                achievedAt);
                    } else {
                        Log.d(TAG, "No Transilvania best score found for user");
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading Transilvania best score", e);
            });
    }
    
    /**
     * Actualizează UI cu cel mai bun scor din Transilvania
     */
    private void updateTransilvaniaBestScoreUI(int bestScore, float accuracy, int maxStreak, Date achievedAt) {
        // TODO: Implementați actualizarea UI cu cel mai bun scor
        Log.d(TAG, "Updating UI with Transilvania best score: " + bestScore + 
                ", Accuracy: " + accuracy + "%, Max Streak: " + maxStreak);
        
        // Exemplu de actualizare a unui TextView
        if (bestScoreText != null) {
            bestScoreText.setText(String.valueOf(bestScore));
        }
    }
    
    /**
     * Capitalizează numele regiunii pentru afișare
     */
    private String capitalizeRegionName(String region) {
        if (region == null || region.isEmpty()) return "";
        if (region.equals("transilvania")) return "Transilvania";
        return region.substring(0, 1).toUpperCase() + region.substring(1);
    }
    
    /**
     * Logare pentru debugging datele din Transilvania
     */
    private void logTransilvaniaDataStatus() {
        Log.d(TAG, "=== TRANSILVANIA DATA STATUS ===");
        Log.d(TAG, "User authenticated: " + (firebaseAuth.getCurrentUser() != null));
        Log.d(TAG, "Loading recent activities from: user_activity_history");
        Log.d(TAG, "Loading best scores from: user_best_scores/regional_scores");
        Log.d(TAG, "Transilvania region key: transilvania_quiz");
        Log.d(TAG, "Expected collections:");
        Log.d(TAG, "  - user_activity_history/{userId}/recent_activities");
        Log.d(TAG, "  - user_best_scores/{userId}/regional_scores/transilvania_quiz");
        Log.d(TAG, "  - leaderboards/transilvania_quiz/entries");
        Log.d(TAG, "=== END STATUS ===");
    }
    
    /**
     * Încarcă toate datele specifice Transilvaniei pentru user profile
     */
    public void loadTransilvaniaDataForProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Cannot load Transilvania data - user not authenticated");
            return;
        }
        
        Log.d(TAG, "Loading complete Transilvania data set for user profile");
        
        // Încărcăm activitatea recentă din Transilvania
        loadTransilvaniaRecentActivity(currentUser.getUid());
        
        // Încărcăm cel mai bun scor din Transilvania
        loadTransilvaniaBestScore();
        
        // Încărcăm statisticile generale din Transilvania
        loadTransilvaniaStats(currentUser.getUid());
    }
    
    /**
     * Încarcă doar activitatea recentă din Transilvania
     */
    private void loadTransilvaniaRecentActivity(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("user_activity_history")
            .document(userId)
            .collection("recent_activities")
            .whereEqualTo("region", "transilvania")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "Transilvania recent activities loaded: " + queryDocumentSnapshots.size());
                    
                    List<ActivityItem> transilvaniaActivities = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ActivityItem activity = createActivityItemFromDocument(doc);
                        if (activity != null && activity.isTransilvaniaActivity()) {
                            transilvaniaActivities.add(activity);
                        }
                    }
                    
                    // Afișăm doar activitățile din Transilvania
                    displayTransilvaniaActivities(transilvaniaActivities);
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading Transilvania recent activities", e);
            });
    }
    
    /**
     * Încarcă statisticile generale din Transilvania
     */
    private void loadTransilvaniaStats(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                runOnUiThread(() -> {
                    if (documentSnapshot.exists()) {
                        Long totalTransilvaniaQuizzes = documentSnapshot.getLong("totalTransilvaniaQuizzes");
                        Long totalTransilvaniaPoints = documentSnapshot.getLong("totalTransilvaniaPoints");
                        String lastRegionPlayed = documentSnapshot.getString("lastRegionPlayed");
                        Date lastActivity = documentSnapshot.getDate("lastActivity");
                        
                        Log.d(TAG, "Transilvania stats - Quizzes: " + totalTransilvaniaQuizzes + 
                                ", Points: " + totalTransilvaniaPoints + 
                                ", Last region: " + lastRegionPlayed);
                        
                        // Actualizăm UI cu statisticile din Transilvania
                        updateTransilvaniaStatsUI(
                            totalTransilvaniaQuizzes != null ? totalTransilvaniaQuizzes.intValue() : 0,
                            totalTransilvaniaPoints != null ? totalTransilvaniaPoints.intValue() : 0,
                            lastRegionPlayed,
                            lastActivity
                        );
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading Transilvania stats", e);
            });
    }
    
    /**
     * Afișează activitățile specifice Transilvaniei
     */
    private void displayTransilvaniaActivities(List<ActivityItem> activities) {
        Log.d(TAG, "Displaying " + activities.size() + " Transilvania activities");
        
        for (ActivityItem activity : activities) {
            Log.d(TAG, "Transilvania Activity: " + activity.getDisplayName() + 
                    " - Score: " + activity.getScore() + 
                    " - " + activity.getShortDescription());
        }
        
        // TODO: Implementați UI specific pentru activitățile din Transilvania
        // Exemplu: RecyclerView cu tema Transilvania, carduri cu culorile specifice, etc.
    }
    
    /**
     * Actualizează UI cu statisticile din Transilvania
     */
    private void updateTransilvaniaStatsUI(int totalQuizzes, int totalPoints, 
                                         String lastRegion, Date lastActivity) {
        Log.d(TAG, "Updating Transilvania stats UI - " + 
                "Quizzes: " + totalQuizzes + 
                ", Points: " + totalPoints);
        
        // TODO: Actualizați UI-ul cu statisticile specifice Transilvaniei
        // Exemplu: badge-uri speciale, progress bars, text views cu tema Transilvania
    }
    
    /**
     * Încarcă rangul utilizatorului în leaderboard-ul Transilvaniei
     */
    public void loadTransilvaniaLeaderboardRank() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Obținem rangul din leaderboard-ul Transilvania
        db.collection("leaderboards")
            .document("transilvania_quiz")
            .collection("entries")
            .orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                runOnUiThread(() -> {
                    int rank = -1;
                    int totalPlayers = queryDocumentSnapshots.size();
                    
                    // Căutăm utilizatorul în clasament
                    for (int i = 0; i < queryDocumentSnapshots.getDocuments().size(); i++) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(i);
                        String userId = doc.getString("userId");
                        if (currentUser.getUid().equals(userId)) {
                            rank = i + 1; // Poziția în clasament (1-indexed)
                            break;
                        }
                    }
                    
                    Log.d(TAG, "Transilvania leaderboard rank: " + rank + " out of " + totalPlayers);
                    
                    // Actualizăm UI cu rangul în Transilvania
                    updateTransilvaniaRankUI(rank, totalPlayers);
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading Transilvania leaderboard rank", e);
            });
    }
    
    /**
     * Actualizează UI cu rangul în leaderboard-ul Transilvaniei
     */
    private void updateTransilvaniaRankUI(int rank, int totalPlayers) {
        if (rank > 0) {
            Log.d(TAG, "User rank in Transilvania: #" + rank + " din " + totalPlayers);
            
            // TODO: Actualizați UI cu rangul specific din Transilvania
            // Exemplu: TextView cu "#5 în Transilvania", progress indicator, badge special
            
            if (currentRankText != null) {
                String rankText = "#" + rank + " în Transilvania (" + totalPlayers + " jucători)";
                currentRankText.setText(rankText);
            }
        } else {
            Log.d(TAG, "User not found in Transilvania leaderboard");
            
            if (currentRankText != null) {
                currentRankText.setText("Neclasificat în Transilvania");
            }
        }
    }
    
    private void updateUserProfile() {
        // Verificăm dacă utilizatorul este autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            showErrorMessage("Utilizatorul nu este autentificat");
            return;
        }
        
        // Validăm input-ul utilizatorului
        if (!validateUserInput()) {
            return; // Validarea a eșuat, mesajul de eroare a fost afișat deja
        }
        
        // Obținem numele de afișare
        String displayName = displayNameEditText.getText().toString().trim();
        
        // Afișăm progress indicator
        showProgressBar();
        
        // Actualizăm profilul utilizatorului
        if (userProfile != null) {
            userProfile.setDisplayName(displayName);
            
            // Dacă utilizatorul a selectat o imagine nouă, o încărcăm în Firebase Storage
            if (selectedImageUri != null) {
                uploadProfileImage(currentUser.getUid());
            } else {
                // Salvăm profilul utilizatorului în repository
                saveUserProfile();
            }
        } else {
            hideProgressBar();
            showErrorMessage("Profilul utilizatorului nu a fost încărcat");
        }
    }
    
    private void uploadProfileImage(String userId) {
        // Creăm o referință pentru imaginea de profil în Firebase Storage
        StorageReference profileImageRef = storageReference.child("profile_images/" + userId + ".jpg");
        
        // Încărcăm imaginea în Firebase Storage
        profileImageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obținem URL-ul de descărcare pentru imagine
                    profileImageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                // Actualizăm URL-ul imaginii de profil în profilul utilizatorului
                                userProfile.setProfileImageUrl(uri.toString());
                                
                                // Actualizăm imaginea de profil în Firebase Auth
                                updateFirebaseAuthProfile(uri);
                                
                                // Salvăm profilul utilizatorului în repository
                                saveUserProfile();
                            })
                            .addOnFailureListener(e -> {
                                // Ascundem progress indicator
                                hideProgressBar();
                                
                                // Afișăm mesaj de eroare
                                showErrorMessage(getString(R.string.error_uploading_image));
                                
                                Log.e(TAG, "Error getting download URL", e);
                            });
                })
                .addOnFailureListener(e -> {
                    // Ascundem progress indicator
                    hideProgressBar();
                    
                    // Afișăm mesaj de eroare
                    showErrorMessage(getString(R.string.error_uploading_image));
                    
                    Log.e(TAG, "Error uploading profile image", e);
                });
    }
    
    private void updateFirebaseAuthProfile(Uri imageUri) {
        // Verificăm dacă utilizatorul este autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;
        
        // Creăm un obiect UserProfileChangeRequest pentru a actualiza profilul utilizatorului
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userProfile.getDisplayName())
                .setPhotoUri(imageUri)
                .build();
        
        // Actualizăm profilul utilizatorului în Firebase Auth
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error updating Firebase Auth profile", task.getException());
                    }
                });
    }
    
    private void saveUserProfile() {
        quizResultRepository.saveUserProfile(userProfile)
                .thenAccept(success -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        hideProgressBar();
                        
                        if (success) {
                            // Afișăm mesaj de succes
                            showSuccessMessage(getString(R.string.profile_updated));
                        } else {
                            // Afișăm mesaj de eroare
                            showErrorMessage(getString(R.string.error_updating_profile));
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        hideProgressBar();
                        
                        // Afișăm mesaj de eroare
                        showErrorMessage(getString(R.string.error_updating_profile));
                        
                        Log.e(TAG, "Error saving user profile", e);
                    });
                    return null;
                });
    }
    
    private void selectProfileImage() {
        getContent.launch("image/*");
    }
    
    private void openLeaderboard() {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    private void refreshProfile() {
        if (userProfile != null) {
            showProgressBar();
            loadUserProfile();
        }
    }
    
    private void showErrorMessage(String message) {
        if (findViewById(android.R.id.content) != null) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
            snackbar.setAction("Reîncearcă", v -> refreshProfile());
            snackbar.show();
        }
    }
    
    private void showSuccessMessage(String message) {
        if (findViewById(android.R.id.content) != null) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(getResources().getColor(R.color.romania_accent));
            snackbar.show();
        }
    }
    
    private boolean validateUserInput() {
        if (displayNameEditText != null) {
            String displayName = displayNameEditText.getText().toString().trim();
            if (displayName.isEmpty()) {
                displayNameEditText.setError("Numele nu poate fi gol");
                displayNameEditText.requestFocus();
                return false;
            }
            if (displayName.length() < 2) {
                displayNameEditText.setError("Numele trebuie să aibă cel puțin 2 caractere");
                displayNameEditText.requestFocus();
                return false;
            }
            if (displayName.length() > 50) {
                displayNameEditText.setError("Numele nu poate avea mai mult de 50 de caractere");
                displayNameEditText.requestFocus();
                return false;
            }
            // Curățăm eroarea dacă totul este ok
            displayNameEditText.setError(null);
        }
        return true;
    }
    
    /**
     * Populează baza de date cu date de test pentru profil - doar pentru dezvoltare
     */
    private void populateUserProfileWithTestData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Nu există utilizator autentificat pentru adăugarea datelor de test", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "Se adaugă date de test în profil...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Populating user profile with test data");
        
        // Creăm un profil cu date de test
        UserProfile testProfile = new UserProfile();
        testProfile.setUserId(currentUser.getUid());
        testProfile.setDisplayName("Alexandru Român");
        testProfile.setEmail(currentUser.getEmail());
        testProfile.setQuizPoints(2850);
        testProfile.setTotalQuizzesTaken(47);
        testProfile.setCorrectAnswers(329); // 329 răspunsuri corecte
        testProfile.setTotalAnswers(376); // din 376 întrebări (87.5% acuratețe)
        testProfile.setPremiumUser(true);
        
        // Salvăm profilul de test
        quizResultRepository.saveUserProfile(testProfile)
            .thenAccept(success -> {
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Date de test adăugate cu succes în profil!", Toast.LENGTH_SHORT).show();
                        // Reîncărcăm profilul pentru a afișa noile date
                        loadUserProfile();
                    } else {
                        Toast.makeText(this, "Eroare la salvarea profilului de test", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .exceptionally(e -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Eroare la adăugarea datelor de test: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding test profile data", e);
                });
                return null;
            });
        
        // Adăugăm și rezultate de test pentru quiz-uri
        createTestQuizResults(currentUser.getUid());
    }
    
    /**
     * Creează rezultate de test pentru quiz-uri
     */
    private void createTestQuizResults(String userId) {
        Log.d(TAG, "Creating test quiz results");
        
        // Creăm câteva rezultate de test
        String[] regions = {"transilvania", "muntenia", "moldova", "dobrogea", "banat"};
        String[] gameTypes = {"quiz", "story", "exploration"};
        
        for (int i = 0; i < 8; i++) {
            final int resultIndex = i + 1; // Create final copy for lambda
            
            QuizResult testResult = new QuizResult();
            testResult.setUserId(userId);
            testResult.setRegion(regions[i % regions.length]);
            testResult.setGameType(gameTypes[i % gameTypes.length]);
            testResult.setScore(150 + (i * 50) + (int)(Math.random() * 100));
            testResult.setCorrectAnswers(7 + (int)(Math.random() * 3));
            testResult.setTotalQuestions(10);
            // Nu mai setăm accuracy - este calculată automat în getAccuracy()
            
            // Setăm data completării (ultimele 30 de zile)
            long currentTime = System.currentTimeMillis();
            long randomOffset = (long)(Math.random() * 30 * 24 * 60 * 60 * 1000); // 30 zile în ms
            testResult.setCompletedAt(new java.util.Date(currentTime - randomOffset));
            
            // Salvăm rezultatul în repository
            quizResultRepository.saveQuizResult(testResult)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Test quiz result " + resultIndex + " saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving test quiz result " + resultIndex, e);
                });
        }
        
        Log.d(TAG, "Test quiz results creation completed");
    }
    
    /**
     * Creează date de test pentru toate regiunile României
     */
    private void createRegionTestData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Nu există utilizator autentificat pentru adăugarea datelor de test", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "Se creează date de test pentru toate regiunile...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Creating region test data for all regions");
        
        // Definim regiunile României
        String[] regions = {"transilvania", "muntenia", "moldova", "oltenia", "dobrogea", "banat", "crisana", "maramures", "bucovina"};
        String[] gameTypes = {"quiz", "story", "exploration"};
        
        // Pre-calculăm numărul de rezultate pentru fiecare regiune
        int[] resultsPerRegion = new int[regions.length];
        int totalResults = 0;
        for (int j = 0; j < regions.length; j++) {
            resultsPerRegion[j] = 2 + (int)(Math.random() * 4);
            totalResults += resultsPerRegion[j];
        }
        final int finalTotalResults = totalResults; // Facem variabila final pentru lambda
        
        java.util.concurrent.atomic.AtomicInteger resultIndex = new java.util.concurrent.atomic.AtomicInteger(0);
        
        for (int regionIdx = 0; regionIdx < regions.length; regionIdx++) {
            final String region = regions[regionIdx];
            final int resultsForRegion = resultsPerRegion[regionIdx];
            
            for (int i = 0; i < resultsForRegion; i++) {
                final int currentResultIndex = resultIndex.incrementAndGet();
                
                QuizResult testResult = new QuizResult();
                testResult.setUserId(currentUser.getUid());
                testResult.setRegion(region);
                testResult.setGameType(gameTypes[i % gameTypes.length]);
                
                // Generăm scoruri realiste pentru fiecare regiune
                int baseScore = 100 + (int)(Math.random() * 300); // 100-400 puncte
                int regionBonus = Math.abs(region.hashCode()) % 200; // Bonus consistent per regiune
                testResult.setScore(baseScore + regionBonus);
                
                // Generăm acuratețe între 60-95%
                int totalQuestions = 10;
                int correctAnswers = 6 + (int)(Math.random() * 4); // 6-9 răspunsuri corecte
                testResult.setCorrectAnswers(correctAnswers);
                testResult.setTotalQuestions(totalQuestions);
                
                // Setăm data completării (ultimele 60 de zile)
                long currentTime = System.currentTimeMillis();
                long randomOffset = (long)(Math.random() * 60 * 24 * 60 * 60 * 1000); // 60 zile în ms
                testResult.setCompletedAt(new java.util.Date(currentTime - randomOffset));
                
                // Salvăm rezultatul în repository
                quizResultRepository.saveQuizResult(testResult)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Region test result " + currentResultIndex + " saved for " + region);
                        
                        // Dacă este ultimul rezultat salvat cu succes, reîncarcă datele
                        if (currentResultIndex == finalTotalResults) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Date de test create pentru toate regiunile! Se reîncarcă...", Toast.LENGTH_SHORT).show();
                                // Reîncarcă progresul regiunilor după 1 secundă
                                new android.os.Handler().postDelayed(() -> {
                                    loadRegionProgress();
                                }, 1000);
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving region test result " + currentResultIndex + " for " + region, e);
                    });
            }
        }
        
        Log.d(TAG, "Created " + finalTotalResults + " test results for all regions");
    }
    
    /**
     * Adaugă regiuni de debug pentru testare rapidă
     */
    private void addDebugRegions() {
        Log.d(TAG, "Adding debug regions for testing");
        
        regionProgressList.clear();
        
        // Adaugă Transilvania cu progres
        RegionProgress transilvania = new RegionProgress("transilvania", "Transilvania", R.drawable.ic_mountain);
        transilvania.setQuizzesCompleted(3);
        transilvania.setBestScore(450);
        transilvania.setAverageAccuracy(85.0f);
        transilvania.setHasCompletedQuizzes(true);
        regionProgressList.add(transilvania);
        
        // Adaugă Muntenia fără progres
        RegionProgress muntenia = new RegionProgress("muntenia", "Muntenia", R.drawable.ic_castle);
        regionProgressList.add(muntenia);
        
        // Adaugă Moldova cu progres mic
        RegionProgress moldova = new RegionProgress("moldova", "Moldova", R.drawable.ic_wine);
        moldova.setQuizzesCompleted(1);
        moldova.setBestScore(220);
        moldova.setAverageAccuracy(70.0f);
        moldova.setHasCompletedQuizzes(true);
        regionProgressList.add(moldova);
        
        // Actualizează adapter-ul
        if (regionProgressAdapter != null) {
            regionProgressAdapter.notifyDataSetChanged();
            Log.d(TAG, "Debug regions added and adapter notified");
        }
    }
    
    /**
     * Afișează informații pentru dezvoltare - pentru a ajuta utilizatorii să testeze profilul
     */
    private void showDevelopmentInfo() {
        // Pentru dezvoltare: afișează instrucțiuni pentru adăugarea de date de test
        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(this, 
                "💡 Long click pe toolbar pentru date generale sau pe 'COLECȚIE REGIUNI' pentru date regionale!", 
                Toast.LENGTH_LONG).show();
        }, 2000); // Așteaptă 2 secunde după încărcare
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_refresh) {
            refreshProfile();
            return true;
        } else if (id == R.id.action_logout) {
            signOutUser();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void signOutUser() {
        // Clear any saved preferences (consistent with UserActivity)
        android.content.SharedPreferences sharedPref = getSharedPreferences("login_prefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("remember_me", false);
        editor.apply();
        
        // Clear UserPrefs as well
        android.content.SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        // Sign out from Firebase
        firebaseAuth.signOut();

        // Redirect to login screen with proper flags to clear task stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
} 