package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Clasă simplă care înlocuiește temporar funcționalitatea originalului RomCityActivity pentru
 * a evita erorile de compilare fără a trebui să modificăm toate clasele care fac referire la ea.
 */
public class RomCityActivity extends AppCompatActivity {
    public static final String CITY_NAME = "city_name";
    public static final String CITY_REGION = "city_region";
    public static final String CITY_IMAGE = "city_image";
    public static final String CITY_DESC = "city_desc";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Această funcționalitate a fost eliminată", Toast.LENGTH_SHORT).show();
        finish();
    }
} 