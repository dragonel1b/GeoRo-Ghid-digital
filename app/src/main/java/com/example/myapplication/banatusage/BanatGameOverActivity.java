package com.example.myapplication.banatusage;

import android.content.Intent;

import com.example.myapplication.core.domain.model.BaseGameOverActivity;

/**
 * Activitate GameOver specifică pentru regiunea Banat
 * Extinde BaseGameOverActivity pentru funcționalitate modulară
 */
public class BanatGameOverActivity extends BaseGameOverActivity {
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, BanatGameActivity.class);
    }
    
    @Override
    protected String getDefaultRegionName() {
        return "Banat";
    }
    
    @Override
    protected String getDefaultQuizTitle() {
        return "Quiz Banat";
    }
    
    @Override
    protected String getRegionGenitive() {
        return "Banatului";
    }
} 
