package com.example.myapplication.viewmodel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.example.myapplication.R;
import com.example.myapplication.ui.ComposeEntryActivity;

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
            getSupportActionBar().setTitle(regionName + " - Orașe");
        }

        // Add cities based on region
        LinearLayout citiesContainer = findViewById(R.id.citiesContainer);
        if (regionName != null) {
            switch (regionName) {
                case "Dobrogea":
                    addCity(citiesContainer, "Constanța", "Orașul principal al Dobrogei și al doilea ca mărime din România", "constanta");
                    addCity(citiesContainer, "Tulcea", "Poarta de intrare în Delta Dunării", "tulcea");
                    addCity(citiesContainer, "Cernavodă", "Oraș istoric la intersecția dintre Dunăre și Canalul Dunăre-Marea Neagră", "cernavoda");
                    break;
                case "Crișana":
                    addCity(citiesContainer, "Oradea", "Capitala județului Bihor, oraș cu arhitectură Art Nouveau și băi termale", "oradea");
                    addCity(citiesContainer, "Arad", "Important centru economic, cultural și universitar al regiunii", "arad");
                    break;
                case "Moldova":
                    addCity(citiesContainer, "Iași", "Capitala culturală a Moldovei", "iasi");
                    addCity(citiesContainer, "Suceava", "Capitala istorică a Moldovei", "suceava");
                    addCity(citiesContainer, "Bacău", "Centru industrial important", "bacau");
                    break;
                case "Transilvania":
                    addCity(citiesContainer, "Cluj-Napoca", "Inima Transilvaniei", "clujnapoca");
                    addCity(citiesContainer, "Brașov", "Charm medieval întâlnit cu orașul modern", "brasov");
                    addCity(citiesContainer, "Sibiu", "Capitala Culturală Europeană 2007", "sibiu");
                    break;
                case "Oltenia":
                    addCity(citiesContainer, "Craiova", "Capitala Olteniei și cel mai mare oraș din regiune", "craiova");
                    addCity(citiesContainer, "Târgu Jiu", "Orașul lui Brâncuși, cu ansamblul monumental unic în lume", "targujiu");
                    addCity(citiesContainer, "Drobeta-Turnu Severin", "Orașul de la Porțile de Fier, cu istorie bogată", "drobetaturnuseverin");
                    addCity(citiesContainer, "Râmnicu Vâlcea", "Centru cultural și stațiune balneară importantă", "valcea");
                    addCity(citiesContainer, "Slatina", "Cel mai important centru al industriei aluminiului din România", "slatina");
                    break;
                case "Banat":
                    addCity(citiesContainer, "Timișoara", "Capitala Banatului, oraș cu arhitectură eclectică și spații verzi", "timisoara");
                    addCity(citiesContainer, "Reșița", "Important centru industrial cu tradiție în metalurgie", "resita");
                    addCity(citiesContainer, "Lugoj", "Oraș cu bogată istorie și cultură, locul natal al lui Traian Vuia", "lugoj");
                    addCity(citiesContainer, "Caransebeș", "Oraș istoric la confluența râurilor Timiș și Sebeș", "caransebes");
                    addCity(citiesContainer, "Băile Herculane", "Una dintre cele mai vechi stațiuni balneare din Europa", "baileherculane");
                    break;
                case "Bucovina":
                    addCity(citiesContainer, "Suceava", "Fostă capitală a Moldovei și centrul administrativ al Bucovinei", "suceava");
                    addCity(citiesContainer, "Gura Humorului", "Orășel pitoresc, punct de plecare spre mănăstirile Humor și Voroneț", "gurahumorului");
                    addCity(citiesContainer, "Rădăuți", "Unul dintre cele mai vechi orașe din Moldova, cunoscut pentru celebra ciorbă rădăuțeană", "radauti");
                    addCity(citiesContainer, "Câmpulung Moldovenesc", "Oraș înconjurat de munți și păduri, cunoscut pentru Muzeul Lemnului", "campullung");
                    addCity(citiesContainer, "Vatra Dornei", "Renumită stațiune balneoclimaterică cu ape minerale terapeutice și pârtii de schi", "vatradornei");
                    break;
                case "Maramureș":
                    addCity(citiesContainer, "Baia Mare", "Capitala județului Maramureș, centru economic și cultural important", "baiamare");
                    addCity(citiesContainer, "Sighetu Marmației", "Al doilea oraș ca mărime din județ, cunoscut pentru Memorialul Victimelor Comunismului", "sighetu");
                    addCity(citiesContainer, "Borșa", "Stațiune montană cunoscută pentru pârtiile de schi și pentru peisajele spectaculoase", "borsa");
                    addCity(citiesContainer, "Vișeu de Sus", "Cunoscut pentru Mocănița, trenul cu aburi care străbate Valea Vaserului", "viseu");
                    addCity(citiesContainer, "Săpânța", "Faimoasă pentru Cimitirul Vesel cu crucile sale colorate și epitafurile în versuri", "sapanta");
                    break;
                case "Muntenia":
                    addCity(citiesContainer, "București", "Capitala României, centrul cultural și economic al țării", "bucuresti");
                    addCity(citiesContainer, "Ploiești", "Centrul industriei petroliere din România, cu o bogată istorie", "ploiesti");
                    addCity(citiesContainer, "Târgoviște", "Fostă capitală a Țării Românești, cu un important ansamblu medieval", "targoviste");
                    addCity(citiesContainer, "Pitești", "Oraș universitar și industrial, cunoscut pentru producția de automobile", "pitesti");
                    addCity(citiesContainer, "Buzău", "Oraș cu tradiție culturală și industrială, poartă de intrare în Munții Buzăului", "buzau");
                    break;
            }
        }
    }

    private void addCity(LinearLayout container, String name, String description, String cityId) {
        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(params);
        cardView.setCardElevation(8);
        cardView.setRadius(16);
        cardView.setUseCompatPadding(true);
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.rom_surface));
        cardView.setStrokeWidth(2);
        cardView.setStrokeColor(getResources().getColor(R.color.rom_primary));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(24, 24, 24, 24);

        AppCompatTextView titleView = new AppCompatTextView(this);
        titleView.setText(name);
        titleView.setTextSize(20);
        titleView.setTextColor(getResources().getColor(R.color.rom_primary));
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

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, ComposeEntryActivity.class);
            intent.putExtra(ComposeEntryActivity.EXTRA_CITY_ID, cityId);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

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
