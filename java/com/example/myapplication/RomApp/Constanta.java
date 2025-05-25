package com.example.myapplication.RomApp;

import android.os.Bundle;
import java.util.ArrayList;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.EnhancedCityActivity;

public class Constanta extends EnhancedCityActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initializeSpecificContent() {
        super.initializeSpecificContent();

        // Set city title
        setTitle("Constanța");

        // Add city-specific sections
        addSection(
                findViewById(R.id.cityContentContainer),
                "Istorie",
                "Constanța, cunoscută în antichitate sub numele de Tomis, este cel mai vechi oraș atestat de pe teritoriul României. " +
                        "Fondat în secolul VI î.Hr. de coloniștii greci, orașul a devenit un important centru comercial la Marea Neagră.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Cazinoul Constanța",
                "Cazinoul din Constanța, construit în stil Art Nouveau în 1910, este simbolul orașului și unul dintre cele mai " +
                        "reprezentative monumente istorice din România. Clădirea impresionează prin arhitectura sa deosebită și poziția " +
                        "privilegiată pe faleza Mării Negre.",
                true
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Plaje și Turism",
                "Constanța este înconjurată de cele mai populare stațiuni de pe litoralul românesc, precum Mamaia, " +
                        "oferind plaje întinse cu nisip fin și multiple oportunități de agrement.",
                false
        );

        addSection(
                findViewById(R.id.cityContentContainer),
                "Cultură și Artă",
                "Orașul găzduiește numeroase instituții culturale importante, inclusiv Muzeul de Istorie Națională și Arheologie, " +
                        "Muzeul de Artă Populară și Acvariul, care prezintă fauna Mării Negre.",
                false
        );
    }

    @Override
    protected ArrayList<String> getCityImages() {
        ArrayList<String> images = new ArrayList<>();
        images.add("dobrogea_constanta_1");
        images.add("dobrogea_constanta_casino");
        images.add("dobrogea_constanta_plaja");
        images.add("dobrogea_constanta_port");
        return images;
    }
}
