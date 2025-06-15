package com.example.myapplication.transilvaniausage;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.R;
import com.example.myapplication.model.base.BaseGameOverActivity;

public class TransilvaniaGameOverActivity extends BaseGameOverActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transilvania_game_over);
        
        // Inițializăm elementele UI
        initializeCommonViews();
        
        // Actualizăm UI-ul cu rezultatele
        updateUI();
    }

    @Override
    protected void restartGame() {
        Intent intent = new Intent(this, TransilvaniaGameActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void returnToMap() {
        Intent intent = new Intent(this, TransilvaniaMapActivity.class);
        startActivity(intent);
        finish();
    }
} 