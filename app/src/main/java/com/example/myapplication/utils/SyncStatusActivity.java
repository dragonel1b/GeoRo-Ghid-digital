package com.example.myapplication.utils;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Activitate pentru monitorizarea și administrarea sistemului hibrid de stocare
 */
public class SyncStatusActivity extends AppCompatActivity {
    
    private SyncManager syncManager;
    private TextView connectionStatusText;
    private TextView authStatusText;
    private TextView lastSyncText;
    private TextView pendingChangesText;
    private MaterialCardView connectionCard;
    private MaterialCardView pendingCard;
    private Button syncNowButton;
    private ProgressBar syncProgressBar;
    private SimpleDateFormat dateFormat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_status);
        
        // Initialize date formatter
        dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("ro", "RO"));
        
        setupToolbar();
        initializeViews();
        
        syncManager = SyncManager.getInstance(this);
        
        updateSyncStatus();
        setupSyncButton();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("🔄 Status Sincronizare");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initializeViews() {
        connectionStatusText = findViewById(R.id.connectionStatusText);
        authStatusText = findViewById(R.id.authStatusText);
        lastSyncText = findViewById(R.id.lastSyncText);
        pendingChangesText = findViewById(R.id.pendingChangesText);
        connectionCard = findViewById(R.id.connectionCard);
        pendingCard = findViewById(R.id.pendingCard);
        syncNowButton = findViewById(R.id.syncNowButton);
        syncProgressBar = findViewById(R.id.syncProgressBar);
    }
    
    private void updateSyncStatus() {
        Map<String, Object> status = syncManager.getSyncStatus();
        
        // Connection status
        boolean hasInternet = (Boolean) status.get("hasInternet");
        connectionStatusText.setText(hasInternet ? "✅ Conectat la internet" : "❌ Fără conexiune internet");
        connectionCard.setStrokeColor(ContextCompat.getColor(this, 
            hasInternet ? R.color.success_green : R.color.error_red));
        
        // Authentication status
        boolean isAuthenticated = (Boolean) status.get("isAuthenticated");
        authStatusText.setText(isAuthenticated ? "✅ Utilizator autentificat" : "❌ Utilizator neautentificat");
        
        // Last sync
        Date lastSync = (Date) status.get("lastSync");
        if (lastSync.getTime() == 0) {
            lastSyncText.setText("❌ Nu a fost efectuată niciodată");
        } else {
            lastSyncText.setText("🕒 " + dateFormat.format(lastSync));
        }
        
        // Pending changes
        int pendingChanges = (Integer) status.get("pendingChanges");
        pendingChangesText.setText(pendingChanges + " modificări în așteptare");
        
        if (pendingChanges > 0) {
            pendingCard.setStrokeColor(ContextCompat.getColor(this, R.color.warning_orange));
            pendingCard.setVisibility(View.VISIBLE);
        } else {
            pendingCard.setStrokeColor(ContextCompat.getColor(this, R.color.success_green));
            pendingCard.setVisibility(View.VISIBLE);
        }
        
        // Enable/disable sync button
        syncNowButton.setEnabled(hasInternet && isAuthenticated && pendingChanges > 0);
    }
    
    private void setupSyncButton() {
        syncNowButton.setOnClickListener(v -> {
            if (!syncManager.isInternetAvailable()) {
                Toast.makeText(this, "❌ Nu există conexiune la internet", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!syncManager.isUserAuthenticated()) {
                Toast.makeText(this, "❌ Utilizatorul nu este autentificat", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Start sync
            syncProgressBar.setVisibility(View.VISIBLE);
            syncNowButton.setEnabled(false);
            syncNowButton.setText("⏳ Sincronizare în curs...");
            
            syncManager.syncPendingChanges(new SyncManager.SyncCallback() {
                @Override
                public void onSyncComplete(boolean success, String message) {
                    runOnUiThread(() -> {
                        syncProgressBar.setVisibility(View.GONE);
                        syncNowButton.setEnabled(true);
                        syncNowButton.setText("🔄 Sincronizează acum");
                        
                        if (success) {
                            Toast.makeText(SyncStatusActivity.this, "✅ " + message, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SyncStatusActivity.this, "❌ " + message, Toast.LENGTH_LONG).show();
                        }
                        
                        // Update status after sync
                        updateSyncStatus();
                    });
                }
            });
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateSyncStatus();
    }
} 