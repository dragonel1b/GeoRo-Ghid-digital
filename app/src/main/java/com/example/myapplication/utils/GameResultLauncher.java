package com.example.myapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.transilvaniausage.TransilvaniaGameResultActivity;
import com.example.myapplication.banatusage.BanatGameResultActivity;

/**
 * Utility class pentru lansarea modulară a activităților de rezultate pentru toate regiunile
 * Simplifică procesul de navigare la rezultate cu un API ușor de folosit
 */
public class GameResultLauncher {
    
    private static final String TAG = "GameResultLauncher";
    
    /**
     * Builder pattern pentru construirea și lansarea unui Intent pentru rezultate
     */
    public static class Builder {
        private final Context context;
        private final Intent intent;
        private final String regionName;
        
        private Builder(Context context, String regionName) {
            this.context = context;
            this.regionName = regionName;
            this.intent = new Intent(context, getResultActivityForRegion(regionName));
        }
        
        public Builder setScore(int score) {
            intent.putExtra("score", score);
            return this;
        }
        
        public Builder setQuestionStats(int correctAnswers, int totalQuestions) {
            intent.putExtra("correctAnswers", correctAnswers);
            intent.putExtra("totalQuestions", totalQuestions);
            return this;
        }
        
        public Builder setMaxStreak(int maxStreak) {
            intent.putExtra("maxStreak", maxStreak);
            return this;
        }
        
        public Builder setTotalTime(long totalTime) {
            intent.putExtra("totalTime", totalTime);
            return this;
        }
        
        public Builder setLifelinesUsed(int lifelinesUsed) {
            intent.putExtra("lifelinesUsed", lifelinesUsed);
            return this;
        }
        
        public Builder setGameType(String gameType) {
            intent.putExtra("gameType", gameType);
            return this;
        }
        
        /**
         * Lansează activitatea de rezultate
         */
        public void launch() {
            try {
                context.startActivity(intent);
                Log.d(TAG, "Launched result activity for region: " + regionName);
            } catch (Exception e) {
                Log.e(TAG, "Error launching result activity for region: " + regionName, e);
            }
        }
        
        /**
         * Returnează Intent-ul construit (pentru uz avansat)
         */
        public Intent getIntent() {
            return intent;
        }
    }
    
    /**
     * Creează un Builder pentru o regiune specifică
     */
    public static Builder forRegion(Context context, String regionName) {
        return new Builder(context, regionName);
    }
    
    /**
     * Metodă de conveniență pentru Transilvania
     */
    public static Builder forTransilvania(Context context) {
        return forRegion(context, "Transilvania");
    }
    
    /**
     * Metodă de conveniență pentru Banat
     */
    public static Builder forBanat(Context context) {
        return forRegion(context, "Banat");
    }
    
    /**
     * Metodă de conveniență pentru Bucovina
     */
    public static Builder forBucovina(Context context) {
        return forRegion(context, "Bucovina");
    }
    
    /**
     * Metodă de conveniență pentru Crișana
     */
    public static Builder forCrisana(Context context) {
        return forRegion(context, "Crisana");
    }
    
    /**
     * Metodă de conveniență pentru Dobrogea
     */
    public static Builder forDobrogea(Context context) {
        return forRegion(context, "Dobrogea");
    }
    
    /**
     * Metodă de conveniență pentru Maramureș
     */
    public static Builder forMaramures(Context context) {
        return forRegion(context, "Maramures");
    }
    
    /**
     * Metodă de conveniență pentru Moldova
     */
    public static Builder forMoldova(Context context) {
        return forRegion(context, "Moldova");
    }
    
    /**
     * Metodă de conveniență pentru Muntenia
     */
    public static Builder forMuntenia(Context context) {
        return forRegion(context, "Muntenia");
    }
    
    /**
     * Metodă de conveniență pentru Oltenia
     */
    public static Builder forOltenia(Context context) {
        return forRegion(context, "Oltenia");
    }
    
    /**
     * Determină clasa de rezultate pentru o regiune
     */
    private static Class<?> getResultActivityForRegion(String regionName) {
        switch (regionName.toLowerCase()) {
            case "transilvania":
                return TransilvaniaGameResultActivity.class;
            case "banat":
                return BanatGameResultActivity.class;
            // Pentru celelalte regiuni, se poate adăuga implementarea specifică
            // Pentru moment, folosim TransilvaniaGameResultActivity ca fallback
            case "bucovina":
            case "crisana":
            case "dobrogea":
            case "maramures":
            case "moldova":
            case "muntenia":
            case "oltenia":
            default:
                Log.d(TAG, "Using Transilvania result activity as fallback for region: " + regionName);
                return TransilvaniaGameResultActivity.class;
        }
    }
    
    /**
     * Metodă de conveniență pentru lansarea rapidă cu date complete
     */
    public static void launchQuickResult(Context context, String regionName, 
                                       int score, int correctAnswers, int totalQuestions,
                                       int maxStreak, long totalTime, int lifelinesUsed) {
        forRegion(context, regionName)
            .setScore(score)
            .setQuestionStats(correctAnswers, totalQuestions)
            .setMaxStreak(maxStreak)
            .setTotalTime(totalTime)
            .setLifelinesUsed(lifelinesUsed)
            .setGameType("quiz")
            .launch();
    }
} 