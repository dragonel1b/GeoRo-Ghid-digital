package com.example.myapplication.Joc1;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {

    private static final String PREFS_NAME = "SharedPrefs";

    public static int getBalance(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt("balance", 0);  // Default balance is 0
    }

    public static void setBalance(Context context, int balance) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt("balance", balance);
        editor.apply();
    }

    public static boolean getCheckboxState(Context context, String checkboxKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(checkboxKey, false);  // Default is unchecked (false)
    }

    public static void setCheckboxState(Context context, String checkboxKey, boolean isChecked) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(checkboxKey, isChecked);
        editor.apply();
}
}