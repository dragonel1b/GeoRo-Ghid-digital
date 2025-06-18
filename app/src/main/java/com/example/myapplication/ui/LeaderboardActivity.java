package com.example.myapplication.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapter.LeaderboardAdapter;
import com.example.myapplication.model.LeaderboardEntry;
import com.example.myapplication.repository.QuizResultRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

/**
 * Activitate pentru afișarea clasamentului (leaderboard) quiz-urilor
 */
public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderboardActivity";
    
    // Repository pentru accesul la date
    private QuizResultRepository quizResultRepository;
    
    // UI Components
    private Spinner regionSpinner;
    private Spinner gameTypeSpinner;
    private RecyclerView leaderboardRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private TextView userRankTextView;
    private Button retryButton;
    
    // Data
    private List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
    private LeaderboardAdapter adapter;
    private String selectedRegion = "transilvania";
    private String selectedGameType = "quiz";
    
    // Regiuni și tipuri de joc
    private final String[] regions = {
        "transilvania", "muntenia", "oltenia", "moldova", 
        "dobrogea", "banat", "crisana", "maramures", "bucovina"
    };
    
    private final String[] gameTypes = {
        "quiz", "memory", "puzzle", "matching"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        
        // Inițializăm repository-ul
        quizResultRepository = QuizResultRepository.getInstance();
        
        // Inițializăm UI components
        initializeViews();
        setupSpinners();
        setupRecyclerView();
        setupRetryButton();
        
        // Încărcăm clasamentul pentru selecția inițială
        loadLeaderboard();
    }
    
    private void initializeViews() {
        regionSpinner = findViewById(R.id.regionSpinner);
        gameTypeSpinner = findViewById(R.id.gameTypeSpinner);
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        userRankTextView = findViewById(R.id.userRankTextView);
        
        // Adăugăm butonul de reîncercare
        retryButton = findViewById(R.id.retryButton);
        if (retryButton == null) {
            // Dacă butonul nu există în layout, îl creăm programatic
            retryButton = new Button(this);
            retryButton.setId(View.generateViewId());
            retryButton.setText("Reîncearcă");
            retryButton.setVisibility(View.GONE);
            
            // Adăugăm butonul sub mesajul de eroare
            androidx.constraintlayout.widget.ConstraintLayout rootLayout = findViewById(R.id.leaderboardRootLayout);
            if (rootLayout != null) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
                    );
                
                params.topToBottom = emptyStateTextView.getId();
                params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                params.topMargin = 16;
                
                retryButton.setLayoutParams(params);
                rootLayout.addView(retryButton);
            }
        }
    }
    
    private void setupRetryButton() {
        retryButton.setOnClickListener(v -> {
            // Ascundem butonul și mesajul de eroare
            retryButton.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.GONE);
            
            // Reîncărcăm clasamentul
            loadLeaderboard();
        });
    }
    
    private void setupSpinners() {
        // Setup region spinner
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, regions);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionSpinner.setAdapter(regionAdapter);
        
        // Setup game type spinner
        ArrayAdapter<String> gameTypeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, gameTypes);
        gameTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gameTypeSpinner.setAdapter(gameTypeAdapter);
        
        // Setup listeners
        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRegion = regions[position];
                loadLeaderboard();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGameType = gameTypes[position];
                loadLeaderboard();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    
    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(leaderboardEntries);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setAdapter(adapter);
    }
    
    private void loadLeaderboard() {
        // Arătăm un progress indicator
        progressBar.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        
        // Golim lista curentă
        leaderboardEntries.clear();
        adapter.notifyDataSetChanged();
        
        try {
            // Încărcăm clasamentul pentru regiunea și jocul selectat
            quizResultRepository.getLeaderboard(selectedRegion, selectedGameType, 50)
                .thenAccept(entries -> {
                    runOnUiThread(() -> {
                        try {
                            // Ascundem progress indicator
                            progressBar.setVisibility(View.GONE);
                            
                            if (entries.isEmpty()) {
                                // Afișăm mesajul pentru starea goală
                                emptyStateTextView.setVisibility(View.VISIBLE);
                                emptyStateTextView.setText(getString(R.string.no_leaderboard_entries));
                            } else {
                                // Folosim metoda updateEntries pentru a actualiza lista și a recalcula rangurile
                                adapter.updateEntries(entries);
                            }
                            
                            // Obținem și afișăm rangul utilizatorului curent
                            loadCurrentUserRank();
                        } catch (Exception e) {
                            // Tratăm orice excepție care ar putea apărea
                            Log.e(TAG, "Error updating UI with leaderboard data", e);
                            emptyStateTextView.setVisibility(View.VISIBLE);
                            emptyStateTextView.setText("Eroare la afișarea clasamentului: " + e.getMessage());
                            retryButton.setVisibility(View.VISIBLE);
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        // Ascundem progress indicator
                        progressBar.setVisibility(View.GONE);
                        
                        // Afișăm mesajul pentru eroare
                        emptyStateTextView.setVisibility(View.VISIBLE);
                        String errorMessage = "Eroare la încărcarea clasamentului: ";
                        if (e.getCause() != null) {
                            errorMessage += e.getCause().getMessage();
                        } else {
                            errorMessage += e.getMessage();
                        }
                        emptyStateTextView.setText(errorMessage);
                        
                        // Afișăm butonul de reîncercare
                        retryButton.setVisibility(View.VISIBLE);
                        
                        Log.e(TAG, "Error loading leaderboard", e);
                    });
                    return null;
                });
        } catch (Exception e) {
            // Tratăm orice excepție care ar putea apărea la inițierea cererii
            progressBar.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText("Eroare la inițierea cererii: " + e.getMessage());
            retryButton.setVisibility(View.VISIBLE);
            Log.e(TAG, "Error initiating leaderboard request", e);
        }
    }
    
    private void loadCurrentUserRank() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Utilizatorul nu este autentificat
            userRankTextView.setText(getString(R.string.not_logged_in));
            return;
        }
        
        quizResultRepository.getCurrentUserRank(selectedRegion, selectedGameType)
            .thenAccept(rank -> {
                runOnUiThread(() -> {
                    if (rank == -1) {
                        // Utilizatorul nu are un rang în acest clasament
                        userRankTextView.setText(getString(R.string.not_ranked));
                    } else {
                        // Afișăm rangul utilizatorului
                        userRankTextView.setText(getString(R.string.your_rank, rank));
                    }
                });
            })
            .exceptionally(e -> {
                runOnUiThread(() -> {
                    userRankTextView.setText(getString(R.string.error_loading_rank));
                    Log.e(TAG, "Error loading user rank", e);
                });
                return null;
            });
    }
} 