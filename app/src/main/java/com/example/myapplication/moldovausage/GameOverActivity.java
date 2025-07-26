package com.example.myapplication.moldovausage;

import android.content.Intent;
import com.example.myapplication.model.base.BaseGameOverActivity;

/**
 * Activitate GameOver specifică pentru regiunea Moldova
 * Extinde BaseGameOverActivity pentru funcționalitate modulară
 */
public class GameOverActivity extends BaseGameOverActivity {
    
    @Override
    protected Intent getPlayAgainIntent() {
        return new Intent(this, MoldovaGameActivity.class);
    }
    
    @Override
    protected String getDefaultRegionName() {
        return "Moldova";
    }
    
    @Override
    protected String getDefaultQuizTitle() {
        return "Quiz Moldova";
    }
    
    @Override
    protected String getRegionGenitive() {
        return "Moldovei";
    }
} 