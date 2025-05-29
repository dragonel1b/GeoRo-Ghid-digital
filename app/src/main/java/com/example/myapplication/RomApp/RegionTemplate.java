package com.example.myapplication.RomApp;

import android.os.Bundle;
import java.util.ArrayList;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.EnhancedCityActivity;

public abstract class RegionTemplate extends EnhancedCityActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set up custom carousel
        androidx.viewpager2.widget.ViewPager2 viewPager = findViewById(R.id.imageCarousel);
        viewPager.setPageTransformer(new androidx.viewpager2.widget.MarginPageTransformer(16));
        viewPager.setOffscreenPageLimit(3);
    }

    @Override
    protected void initializeSpecificContent() {
        super.initializeSpecificContent();

        // Standard sections for all regions
        addSection(findViewById(R.id.cityContentContainer), "Introducere", getIntroductionText(), true);
        addSection(findViewById(R.id.cityContentContainer), "Istorie și Geografie", getHistoryGeographyText(), true);
        addSection(findViewById(R.id.cityContentContainer), "Cultură și Tradiții", getCultureTraditionsText(), false);
        addSection(findViewById(R.id.cityContentContainer), "Atracții Turistice", getAttractionsText(), false);
        
        // Optional sections
        if (hasGastronomy()) {
            addSection(findViewById(R.id.cityContentContainer), "Gastronomie", getGastronomyText(), false);
        }
        if (hasPersonalitiesEvents()) {
            addSection(findViewById(R.id.cityContentContainer), "Personalități/Evenimente", getPersonalitiesEventsText(), false);
        }
        if (hasCuriosities()) {
            addSection(findViewById(R.id.cityContentContainer), "Curiozități", getCuriositiesText(), false);
        }
    }

    // Abstract methods to be implemented by each region
    protected abstract String getIntroductionText();
    protected abstract String getHistoryGeographyText();
    protected abstract String getCultureTraditionsText();
    protected abstract String getAttractionsText();
    protected abstract String getRegionName();
    
    // Optional sections - default implementations return null
    protected String getGastronomyText() { return null; }
    protected String getPersonalitiesEventsText() { return null; }
    protected String getCuriositiesText() { return null; }
    
    protected boolean hasGastronomy() { return getGastronomyText() != null; }
    protected boolean hasPersonalitiesEvents() { return getPersonalitiesEventsText() != null; }
    protected boolean hasCuriosities() { return getCuriositiesText() != null; }

    @Override
    protected ArrayList<String> getCityImages() {
        if (cityImages == null) {
            cityImages = new ArrayList<>();
        // Default images - can be overridden
            cityImages.add("default_region_1");
            cityImages.add("default_region_2");
            cityImages.add("default_region_3");
        }
        return cityImages;
    }
}
