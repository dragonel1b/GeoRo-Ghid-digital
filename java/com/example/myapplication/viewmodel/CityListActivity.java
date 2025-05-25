package com.example.myapplication.viewmodel;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.example.myapplication.R;

public class CityListActivity extends AppCompatActivity {
    public static final String EXTRA_REGION_NAME = "region_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        String regionName = getIntent().getStringExtra(EXTRA_REGION_NAME);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(regionName + " Cities");
        }

        // Add cities based on region
        LinearLayout citiesContainer = findViewById(R.id.citiesContainer);
        if (regionName != null) {
            switch (regionName) {
                case "Dobrogea":
                    addCity(citiesContainer, "Constanța", "Historical port city with beautiful beaches");
                    addCity(citiesContainer, "Tulcea", "Gateway to the Danube Delta");
                    addCity(citiesContainer, "Mangalia", "Ancient city with modern resorts");
                    break;
                case "Moldova":
                    addCity(citiesContainer, "Iași", "Cultural capital of Moldova");
                    addCity(citiesContainer, "Suceava", "Historical capital of Moldavia");
                    addCity(citiesContainer, "Bacău", "Important industrial center");
                    break;
                case "Transilvania":
                    addCity(citiesContainer, "Cluj-Napoca", "Heart of Transylvania");
                    addCity(citiesContainer, "Brașov", "Medieval charm meets modern city");
                    addCity(citiesContainer, "Sibiu", "European Capital of Culture 2007");
                    break;
            }
        }
    }

    private void addCity(LinearLayout container, String name, String description) {
        MaterialCardView cardView = new MaterialCardView(this);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardView.setCardElevation(4);
        cardView.setRadius(16);
        cardView.setUseCompatPadding(true);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(32, 24, 32, 24);

        AppCompatTextView titleView = new AppCompatTextView(this);
        titleView.setText(name);
        titleView.setTextSize(18);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        titleView.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));

        AppCompatTextView descView = new AppCompatTextView(this);
        descView.setText(description);
        descView.setTextSize(14);
        descView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        descView.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = 8;
        descView.setLayoutParams(descParams);

        content.addView(titleView);
        content.addView(descView);
        cardView.addView(content);
        container.addView(cardView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
