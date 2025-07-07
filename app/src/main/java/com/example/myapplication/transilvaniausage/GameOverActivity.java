package com.example.myapplication.transilvaniausage;

import android.content.Intent;

import com.example.myapplication.model.base.BaseGameOverActivity;

/**
 * Activitate GameOver specifică pentru regiunea Transilvania
 * Extinde BaseGameOverActivity pentru funcționalitate modulară
 */
public class GameOverActivity extends BaseGameOverActivity {
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, TransilvaniaGameActivity.class);
    }
    
    @Override
    protected String getDefaultRegionName() {
        return "Transilvania";
    }
    
    @Override
    protected String getDefaultQuizTitle() {
        return "Quiz Transilvania";
    }
    
    @Override
    protected String getRegionGenitive() {
        return "Transilvaniei";
    }
} 