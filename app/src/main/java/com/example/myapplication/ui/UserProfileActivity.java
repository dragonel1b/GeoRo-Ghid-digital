package com.example.myapplication.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.myapplication.adapter.QuizResultAdapter;
import com.example.myapplication.model.QuizResult;
import com.example.myapplication.model.UserProfile;
import com.example.myapplication.repository.QuizResultRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

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
    private EditText displayNameEditText;
    private TextView emailTextView;
    private TextView quizPointsTextView;
    private TextView totalQuizzesTextView;
    private TextView accuracyTextView;
    private MaterialButton updateProfileButton;
    private FloatingActionButton changeImageButton;
    private MaterialButton viewLeaderboardButton;
    private ProgressBar progressBar;
    private View progressBarContainer;
    private RecyclerView recentQuizzesRecyclerView;
    private CardView statsCardView;
    
    // Data
    private UserProfile userProfile;
    private Uri selectedImageUri;
    private List<QuizResult> recentQuizResults = new ArrayList<>();
    
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
        setupListeners();
        
        // Verificăm dacă utilizatorul este autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Utilizatorul nu este autentificat, îl redirecționăm către ecranul de autentificare
            redirectToLogin();
            return;
        }
        
        // Încărcăm datele utilizatorului
        loadUserProfile();
        loadRecentQuizResults();
    }
    
    private void initializeViews() {
        profileImageView = findViewById(R.id.profileImageView);
        displayNameEditText = findViewById(R.id.displayNameEditText);
        emailTextView = findViewById(R.id.emailTextView);
        quizPointsTextView = findViewById(R.id.quizPointsTextView);
        totalQuizzesTextView = findViewById(R.id.totalQuizzesTextView);
        accuracyTextView = findViewById(R.id.accuracyTextView);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        changeImageButton = findViewById(R.id.changeImageButton);
        viewLeaderboardButton = findViewById(R.id.viewLeaderboardButton);
        progressBar = findViewById(R.id.progressBar);
        progressBarContainer = findViewById(R.id.progressBarContainer);
        recentQuizzesRecyclerView = findViewById(R.id.recentQuizzesRecyclerView);
        statsCardView = findViewById(R.id.statsCardView);
        
        // Configurăm RecyclerView
        recentQuizzesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentQuizzesRecyclerView.setHasFixedSize(true);
        
        // Configurăm toolbar-ul
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupListeners() {
        updateProfileButton.setOnClickListener(v -> updateUserProfile());
        changeImageButton.setOnClickListener(v -> selectProfileImage());
        viewLeaderboardButton.setOnClickListener(v -> openLeaderboard());
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
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.error_loading_profile),
                                Snackbar.LENGTH_LONG).show();
                        
                        Log.e(TAG, "Error loading user profile", e);
                    });
                    return null;
                });
    }
    
    private void createNewUserProfile(FirebaseUser currentUser) {
        // Creăm un profil nou pentru utilizator
        userProfile = new UserProfile();
        userProfile.setUserId(currentUser.getUid());
        userProfile.setEmail(currentUser.getEmail());
        
        // Setăm numele de afișare din Firebase Auth (dacă există)
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            userProfile.setDisplayName(currentUser.getDisplayName());
        } else {
            // Folosim adresa de email ca nume de afișare implicit
            userProfile.setDisplayName(currentUser.getEmail());
        }
        
        // Setăm URL-ul imaginii de profil din Firebase Auth (dacă există)
        if (currentUser.getPhotoUrl() != null) {
            userProfile.setProfileImageUrl(currentUser.getPhotoUrl().toString());
        }
        
        // Salvăm profilul utilizatorului în repository
        quizResultRepository.saveUserProfile(userProfile)
                .thenAccept(success -> {
                    runOnUiThread(() -> {
                        if (success) {
                            // Afișăm datele utilizatorului
                            displayUserProfileWithAnimation();
                        } else {
                            // Afișăm mesaj de eroare
                            Snackbar.make(findViewById(android.R.id.content),
                                    getString(R.string.error_creating_profile),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        // Afișăm mesaj de eroare
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.error_creating_profile),
                                Snackbar.LENGTH_LONG).show();
                        
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
        // Afișăm numele de afișare
        displayNameEditText.setText(userProfile.getDisplayName());
        
        // Afișăm adresa de email
        emailTextView.setText(userProfile.getEmail());
        
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
        profileImageView.setScaleX(0.5f);
        profileImageView.setScaleY(0.5f);
        profileImageView.setAlpha(0f);
        profileImageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .start();
        
        // Afișăm statisticile utilizatorului cu animație
        quizPointsTextView.setText(String.valueOf(userProfile.getQuizPoints()));
        totalQuizzesTextView.setText(String.valueOf(userProfile.getTotalQuizzesTaken()));
        
        // Calculăm și afișăm acuratețea
        if (userProfile.getTotalAnswers() > 0) {
            float accuracy = (float) userProfile.getCorrectAnswers() / userProfile.getTotalAnswers() * 100;
            accuracyTextView.setText(String.format("%.1f%%", accuracy));
        } else {
            accuracyTextView.setText("0%");
        }
        
        // Afișăm sau ascundem cardul cu statistici
        if (userProfile.getTotalQuizzesTaken() > 0) {
            statsCardView.setVisibility(View.VISIBLE);
            statsCardView.setAlpha(0f);
            statsCardView.setTranslationY(100f);
            statsCardView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(200)
                    .start();
        } else {
            statsCardView.setVisibility(View.GONE);
        }
    }
    
    private void loadRecentQuizResults() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;
        
        // Încărcăm rezultatele recente ale quiz-urilor
        quizResultRepository.getUserQuizResults(currentUser.getUid(), 5)
                .thenAccept(results -> {
                    runOnUiThread(() -> {
                        recentQuizResults.clear();
                        recentQuizResults.addAll(results);
                        
                        // Configurăm adaptorul pentru RecyclerView
                        recentQuizzesRecyclerView.setAdapter(new QuizResultAdapter(recentQuizResults));
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error loading recent quiz results", e);
                    });
                    return null;
                });
    }
    
    private void updateUserProfile() {
        // Verificăm dacă utilizatorul este autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;
        
        // Obținem numele de afișare
        String displayName = displayNameEditText.getText().toString().trim();
        if (displayName.isEmpty()) {
            displayNameEditText.setError(getString(R.string.error_empty_display_name));
            return;
        }
        
        // Afișăm progress indicator
        showProgressBar();
        
        // Actualizăm profilul utilizatorului
        userProfile.setDisplayName(displayName);
        
        // Dacă utilizatorul a selectat o imagine nouă, o încărcăm în Firebase Storage
        if (selectedImageUri != null) {
            uploadProfileImage(currentUser.getUid());
        } else {
            // Salvăm profilul utilizatorului în repository
            saveUserProfile();
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
                                Snackbar.make(findViewById(android.R.id.content),
                                        getString(R.string.error_uploading_image),
                                        Snackbar.LENGTH_LONG).show();
                                
                                Log.e(TAG, "Error getting download URL", e);
                            });
                })
                .addOnFailureListener(e -> {
                    // Ascundem progress indicator
                    hideProgressBar();
                    
                    // Afișăm mesaj de eroare
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.error_uploading_image),
                            Snackbar.LENGTH_LONG).show();
                    
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
                            Snackbar.make(findViewById(android.R.id.content),
                                    getString(R.string.profile_updated),
                                    Snackbar.LENGTH_LONG).show();
                        } else {
                            // Afișăm mesaj de eroare
                            Snackbar.make(findViewById(android.R.id.content),
                                    getString(R.string.error_updating_profile),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        hideProgressBar();
                        
                        // Afișăm mesaj de eroare
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.error_updating_profile),
                                Snackbar.LENGTH_LONG).show();
                        
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
        startActivity(intent);
        finish();
    }
} 