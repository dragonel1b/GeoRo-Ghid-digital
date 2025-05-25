package com.example.myapplication.dobrogeausage;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.media.AudioManager;
import android.content.Context;

import com.example.myapplication.R;

public class DobrogeaMapActivity extends AppCompatActivity {
    private TextView welcomeText;
    private String fullText = "Bine ai venit în Dobrogea!";
    private int charIndex = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobrogea_map);

        // Play welcome sound
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupWelcomeScreen();
        setupLocationClickListeners();
    }

    private void setupWelcomeScreen() {
        welcomeText = findViewById(R.id.welcome_text);
        Button startButton = findViewById(R.id.start_button);
        startButton.setVisibility(View.INVISIBLE);
        
        animateText();

        startButton.setOnClickListener(v -> {
            // Play button click sound
            try {
                AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(AudioManager.FX_KEY_CLICK);
            } catch (Exception e) {
                e.printStackTrace();
            }

            welcomeText.setVisibility(View.GONE);
            startButton.setVisibility(View.GONE);
            showMapElements();
        });
    }

    private void animateText() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (charIndex <= fullText.length()) {
                    welcomeText.setText(fullText.substring(0, charIndex));
                    charIndex++;
                    handler.postDelayed(this, 150);
                } else {
                    findViewById(R.id.start_button).setVisibility(View.VISIBLE);
                }
            }
        }, 500);
    }

    private void showMapElements() {
        int[] locationIds = {
            R.id.delta_location,
            R.id.constanta_location,
            R.id.histria_location,
            R.id.mosque_location,
            R.id.beach_location
        };
        
        for (int id : locationIds) {
            ImageView location = findViewById(id);
            location.setVisibility(View.VISIBLE);
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            location.startAnimation(fadeIn);
        }
    }

    private void setupLocationClickListeners() {
        int[] locationIds = {
            R.id.delta_location,
            R.id.constanta_location,
            R.id.histria_location,
            R.id.mosque_location,
            R.id.beach_location
        };

        for (int id : locationIds) {
            findViewById(id).setOnClickListener(v -> {
                // Play location selection sound
                try {
                    AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    am.playSoundEffect(AudioManager.FX_KEY_CLICK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showLocationActivity(id);
            });
        }
    }

    private void showLocationActivity(int locationId) {
        Class<?> activityClass = null;
        
        if (locationId == R.id.delta_location) {
            activityClass = DeltaQuizActivity.class;
        } else if (locationId == R.id.constanta_location) {
            activityClass = CasinoStoryActivity.class;
        } else if (locationId == R.id.histria_location) {
            activityClass = HistriaPuzzleActivity.class;
        } else if (locationId == R.id.beach_location) {
            activityClass = BeachTreasureActivity.class;
        }

        if (activityClass != null) {
            startActivity(new Intent(this, activityClass));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
