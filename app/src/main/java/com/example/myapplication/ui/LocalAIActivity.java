package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.utils.LocalAIHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LocalAIActivity extends AppCompatActivity {
    private TextView statusTextView;
    private EditText messageEditText;
    private Button sendButton;
    private Button checkStatusButton;
    private TextView responseTextView;
    private String aiServerUrl = "http://192.168.1.100:5000"; // Înlocuiește cu IP-ul tău

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_ai);

        initializeViews();
        setupListeners();
        checkAIStatus(); // Verifică statusul la pornire
    }

    private void initializeViews() {
        statusTextView = findViewById(R.id.statusTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        checkStatusButton = findViewById(R.id.checkStatusButton);
        responseTextView = findViewById(R.id.responseTextView);
    }

    private void setupListeners() {
        checkStatusButton.setOnClickListener(v -> checkAIStatus());
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void checkAIStatus() {
        statusTextView.setText("Verificare status...");
        sendButton.setEnabled(false);

        LocalAIHelper.checkAILocalStatus(aiServerUrl, isActive -> {
            runOnUiThread(() -> {
                if (isActive != null && isActive) {
                    statusTextView.setText("✅ AI Local ACTIV");
                    statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
                    sendButton.setEnabled(true);
                } else {
                    statusTextView.setText("❌ AI Local INACTIV");
                    statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
                    sendButton.setEnabled(false);
                }
            });
        });
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Introdu o întrebare!", Toast.LENGTH_SHORT).show();
            return;
        }

        responseTextView.setText("Se trimite către AI local...");
        sendButton.setEnabled(false);

        // Trimiterea reală către AI-ul local
        LocalAIHelper.sendMessageToLocalAI(aiServerUrl, message, response -> {
            runOnUiThread(() -> {
                responseTextView.setText(response != null ? response : "Eroare: Nu s-a primit răspuns");
                sendButton.setEnabled(true);
                
                // Opțiune de salvare dacă răspunsul nu e o eroare
                if (response != null && !response.startsWith("Eroare")) {
                    showSaveDialog(message, response);
                }
            });
        });
    }

    private void showSaveDialog(String userQuestion, String aiResponse) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Salvează răspunsul?")
            .setMessage("Vrei să salvezi acest răspuns ca curiozitate?")
            .setPositiveButton("Da", (dialog, which) -> {
                saveLocalAIResponse(userQuestion, aiResponse);
            })
            .setNegativeButton("Nu", null)
            .show();
    }

    private void saveLocalAIResponse(String userQuestion, String aiResponse) {
        // Salvare în Firestore (similar cu OpenAI)
        com.example.myapplication.core.domain.model.CuriosityModel curiosity = 
            new com.example.myapplication.core.domain.model.CuriosityModel(
                userQuestion, aiResponse, "local", "curiozitate", 
                com.google.firebase.Timestamp.now(), "AI Local"
            );

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("ai_curiosities")
            .add(curiosity)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Răspuns salvat cu succes!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Eroare la salvare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 