package com.example.myapplication.olteniausage;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Activitate pentru încărcarea întrebărilor despre Oltenia în Firebase
 * ATENȚIE: Această activitate este doar pentru dezvoltatori/administratori
 * Nu ar trebui să fie accesibilă pentru utilizatorii obișnuiți
 */
public class OlteniaQuestionsUploaderActivity extends AppCompatActivity {
    private static final String TAG = "OlteniaQuestionsUploader";
    
    private Button uploadButton;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private OlteniaQuestionsUploader uploader;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oltenia_questions_uploader);
        
        initializeViews();
        setupUploader();
        setupClickListeners();
    }
    
    private void initializeViews() {
        uploadButton = findViewById(R.id.uploadButton);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);
        
        // Inițial ascundem progress bar
        progressBar.setVisibility(View.GONE);
        statusTextView.setText("Pregătit pentru încărcarea întrebărilor despre Oltenia");
    }
    
    private void setupUploader() {
        uploader = new OlteniaQuestionsUploader(this);
    }
    
    private void setupClickListeners() {
        uploadButton.setOnClickListener(v -> showConfirmationDialog());
    }
    
    /**
     * Afișează un dialog de confirmare înainte de încărcare
     */
    private void showConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirmare încărcare")
            .setMessage("Ești sigur că vrei să încarci întrebările despre Oltenia în Firebase?\n\n" +
                       "Această operație va adăuga aproximativ 40 de întrebări noi în baza de date.\n\n" +
                       "ATENȚIE: Dacă întrebările există deja, se vor crea duplicate!")
            .setPositiveButton("Da, încarcă", (dialog, which) -> startUploadProcess())
            .setNegativeButton("Anulează", null)
            .show();
    }
    
    /**
     * Începe procesul de încărcare
     */
    private void startUploadProcess() {
        Log.d(TAG, "🚀 Începe procesul de încărcare întrebări Oltenia...");
        
        // Afișăm progress bar și actualizăm UI
        uploadButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText("Se încarcă întrebările în Firebase...");
        
        // Începem încărcarea
        uploader.uploadAllQuestions()
            .thenRun(() -> {
                // Operație reușită
                runOnUiThread(() -> {
                    uploadButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    statusTextView.setText("✅ Toate întrebările au fost încărcate cu succes!");
                    
                    Log.d(TAG, "✅ Încărcare completă cu succes!");
                    Toast.makeText(this, "Întrebările despre Oltenia au fost încărcate cu succes!", 
                                 Toast.LENGTH_LONG).show();
                    
                    showSuccessDialog();
                });
            })
            .exceptionally(throwable -> {
                // Operație eșuată
                runOnUiThread(() -> {
                    uploadButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    statusTextView.setText("❌ Eroare la încărcare: " + throwable.getMessage());
                    
                    Log.e(TAG, "❌ Eroare la încărcare", throwable);
                    Toast.makeText(this, "Eroare la încărcarea întrebărilor: " + throwable.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                    
                    showErrorDialog(throwable.getMessage());
                });
                return null;
            });
    }
    
    /**
     * Afișează dialog de succes
     */
    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Încărcare reușită! 🎉")
            .setMessage("Toate întrebările despre Oltenia au fost încărcate cu succes în Firebase!\n\n" +
                       "Categorii încărcate:\n" +
                       "• Istorie (5 întrebări)\n" +
                       "• Geografie (5 întrebări)\n" +
                       "• Cultură (5 întrebări)\n" +
                       "• Arhitectură (5 întrebări)\n" +
                       "• Gastronomie (5 întrebări)\n" +
                       "• Personalități (5 întrebări)\n" +
                       "• Natură (5 întrebări)\n" +
                       "• Legende (5 întrebări)\n\n" +
                       "Acum utilizatorii pot juca quiz-ul despre Oltenia!")
            .setPositiveButton("Excelent!", null)
            .show();
    }
    
    /**
     * Afișează dialog de eroare
     */
    private void showErrorDialog(String errorMessage) {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Eroare la încărcare ❌")
            .setMessage("A apărut o eroare la încărcarea întrebărilor:\n\n" + errorMessage + "\n\n" +
                       "Verifică:\n" +
                       "• Conexiunea la internet\n" +
                       "• Configurația Firebase\n" +
                       "• Permisiunile de scriere în Firestore")
            .setPositiveButton("OK", null)
            .show();
    }
    
    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            // Dacă încărcarea este în curs, confirmăm înainte de a ieși
            new MaterialAlertDialogBuilder(this)
                .setTitle("Încărcare în curs")
                .setMessage("Încărcarea întrebărilor este în curs. Ești sigur că vrei să ieși?")
                .setPositiveButton("Da, ieși", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Rămân", null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
} 