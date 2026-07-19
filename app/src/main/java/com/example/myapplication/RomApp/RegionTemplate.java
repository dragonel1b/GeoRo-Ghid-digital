package com.example.myapplication.RomApp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import java.util.ArrayList;

/**
 * Base class for regional activities.
 * Refactored to be independent of the deleted legacy city classes.
 */
public abstract class RegionTemplate extends AppCompatActivity {
    
    protected ArrayList<String> cityImages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initializeSpecificContent() {
        ViewGroup container = findViewById(R.id.cityContentContainer);
        if (container == null) return;
        
        // Standard sections for all regions
        addSection(container, "Introducere", getIntroductionText(), true);
        addSection(container, "Istorie și Geografie", getHistoryGeographyText(), true);
        addSection(container, "Cultură și Tradiții", getCultureTraditionsText(), false);
        addSection(container, "Atracții Turistice", getAttractionsText(), false);
        
        // Optional sections
        if (hasGastronomy()) {
            addSection(container, "Gastronomie", getGastronomyText(), false);
        }
        if (hasPersonalitiesEvents()) {
            addSection(container, "Personalități/Evenimente", getPersonalitiesEventsText(), false);
        }
        if (hasCuriosities()) {
            addSection(container, "Curiozități", getCuriositiesText(), false);
        }
    }
    
    /**
     * Dynamically adds a content section to the container.
     */
    protected void addSection(ViewGroup container, String title, String content, boolean isImportant) {
        if (container == null || content == null) return;
        
        View sectionView = LayoutInflater.from(this).inflate(R.layout.section_layout, container, false);
        TextView titleView = sectionView.findViewById(R.id.sectionTitle);
        TextView contentView = sectionView.findViewById(R.id.sectionContent);
        View importantBadge = sectionView.findViewById(R.id.importantBadge);
        
        if (titleView != null) titleView.setText(title);
        if (contentView != null) contentView.setText(content);
        if (importantBadge != null) {
            importantBadge.setVisibility(isImportant ? View.VISIBLE : View.GONE);
        }
        
        container.addView(sectionView);
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

    protected abstract ArrayList<String> getCityImages();
}
