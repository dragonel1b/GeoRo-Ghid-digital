package com.example.myapplication.munteniausage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import androidx.core.widget.NestedScrollView;
import com.example.myapplication.R;
import com.example.myapplication.RomApp.PointsManager;
import com.example.myapplication.RomApp.Muntenia;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class MunteniaTourActivity extends AppCompatActivity {
    private static final String REGION = "muntenia";
    private PointsManager pointsManager;
    private List<String> tourImages = new ArrayList<>();
    private boolean allLocationsViewed = false;
    private static final int POINTS_VIEW_ALL = 10;
    private TextView pointsText;
    private int currentStoryPart = 0;
    private boolean storyCompleted = false;
    
    // New views for story-first approach
    private ScrollView storyScrollView;
    private androidx.core.widget.NestedScrollView locationsLayout;
    private TextView storyTextView;
    private MaterialButton nextStoryButton;
    private MaterialButton showLocationsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muntenia_tour);

        // Initialize PointsManager
        pointsManager = PointsManager.getInstance(this);

        // Initialize points text view
        pointsText = findViewById(R.id.pointsTextView);

        // Initialize tour images list
        tourImages.add("bucuresti_parlament");
        tourImages.add("bucuresti_ateneu");
        tourImages.add("targoviste_curtea");
        tourImages.add("ploiesti_muzeu");
        tourImages.add("sinaia_peles");
        tourImages.add("curtea_arges");

        // Get the views
        storyScrollView = findViewById(R.id.storyScrollView);
        locationsLayout = findViewById(R.id.locationsLayout);
        storyTextView = findViewById(R.id.storyTextView);
        nextStoryButton = findViewById(R.id.btnNextStory);
        showLocationsButton = findViewById(R.id.btnShowLocations);
        
        // Initially show story and hide locations
        storyScrollView.setVisibility(View.VISIBLE);
        locationsLayout.setVisibility(View.GONE);
        
        // Display first part of the story
        if (storyTextView != null) {
            storyTextView.setText(storyParts[0]);
        }
        
        // Set up button listeners
        if (nextStoryButton != null) {
            nextStoryButton.setOnClickListener(v -> showNextStoryPart());
        }
        
        if (showLocationsButton != null) {
            showLocationsButton.setOnClickListener(v -> showLocations());
            showLocationsButton.setVisibility(View.GONE); // Initially hidden
        }

        // Set up image click listeners
        setupImageClickListeners();

        // Setup navigation buttons
        setupNavigationButtons();

        // Award points for starting the tour
        pointsManager.addPoints(this, REGION, 5);
        Toast.makeText(this, "Ai primit 5 puncte pentru începerea turului!", Toast.LENGTH_SHORT).show();
        
        // Update points display
        updatePointsDisplay();
    }

    private void setupImageClickListeners() {
        int[] imageIds = {
            R.id.imageTour1,
            R.id.imageTour2,
            R.id.imageTour3,
            R.id.imageTour4,
            R.id.imageTour5,
            R.id.imageTour6
        };

        for (int i = 0; i < imageIds.length; i++) {
            final int index = i;
            findViewById(imageIds[i]).setOnClickListener(v -> {
                // Show more info about the location
                String locationName = getLocationName(index);
                String locationDesc = getLocationDescription(index);
                
                // Show a dialog with location info
                showLocationDialog(locationName, locationDesc);
                
                // Award points for viewing this location
                pointsManager.addPoints(this, REGION, 2);
                Toast.makeText(this, "Ai primit 2 puncte pentru vizitarea virtuală!", Toast.LENGTH_SHORT).show();
                
                // Update points display
                updatePointsDisplay();
                
                // Check if all locations have been viewed
                checkAllLocationsViewed();
            });
        }
    }

    private void showLocationDialog(String title, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
               .setMessage(description)
               .setPositiveButton("Închide", (dialog, id) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getLocationName(int index) {
        String[] names = {
            "Palatul Parlamentului",
            "Ateneul Român",
            "Curtea Domnească din Târgoviște",
            "Muzeul Petrolului din Ploiești",
            "Castelul Peleș din Sinaia",
            "Mănăstirea Curtea de Argeș"
        };
        
        if (index >= 0 && index < names.length) {
            return names[index];
        }
        return "Locație necunoscută";
    }

    private String getLocationDescription(int index) {
        String[] descriptions = {
            "A doua cea mai mare clădire administrativă din lume, după Pentagon. Construcția a început în timpul regimului comunist și continuă să impresioneze prin dimensiuni și arhitectură. Palatul dispune de 1.100 de încăperi și are o suprafață de 340.000 m².",
            
            "Un simbol cultural al Bucureștiului și unul dintre cele mai frumoase clădiri din România. Construită în stil neoclasic între 1886 și 1888, clădirea găzduiește importante evenimente culturale, inclusiv Festivalul George Enescu. Fresca circulară din interior, realizată de pictorul Costin Petrescu, prezintă istoria poporului român.",
            
            "Fostul palat domnesc din Târgoviște, cu celebrul Turn al Chindiei. Complexul a fost construit în secolul al XIV-lea și a fost extins de Vlad Țepeș în secolul al XV-lea. Aici au domnit figuri istorice precum Mircea cel Bătrân, Vlad Țepeș și Mihai Viteazul. Turnul Chindiei, înalt de 27 metri, a fost folosit inițial ca punct de observație și mai târziu ca turn al clopotelor.",
            
            "Singurul muzeu al petrolului din România, amplasat în Ploiești, centrul industriei petroliere românești. Prezintă istoria extracției și prelucrării petrolului, de la primele sonde din secolul al XIX-lea până la tehnologiile moderne. Ploieștiul a fost pionier în industria mondială a petrolului, prima rafinărie industrială din lume fiind construită aici în 1857.",
            
            "Una dintre cele mai frumoase reședințe regale din Europa, construită între 1873 și 1914 la comanda Regelui Carol I. Castelul combină stiluri arhitecturale diferite, de la renascentist german și gotic la baroc și rococo. Colecțiile de artă și mobilierul original sunt impresionante, iar castelul este înconjurat de grădini minunate în stil italian.",
            
            "O bijuterie arhitecturală și un loc de pelerinaj important, construită în secolul al XVI-lea de Neagoe Basarab. Decorațiunile exterioare din piatră sculptată sunt unice, iar biserica adăpostește mormintele regilor Carol I și Ferdinand I, împreună cu reginele Elisabeta și Maria. Legenda Meșterului Manole, care și-ar fi zidit soția în pereții lăcașului pentru ca acesta să reziste, este asociată cu acest edificiu."
        };
        
        if (index >= 0 && index < descriptions.length) {
            return descriptions[index];
        }
        return "Descriere indisponibilă";
    }

    private void setupNavigationButtons() {
        findViewById(R.id.btnBackToRegion).setOnClickListener(v -> {
            Intent intent = new Intent(this, Muntenia.class);
            startActivity(intent);
            finish();
        });
    }

    private void updatePointsDisplay() {
        if (pointsText != null) {
            int points = pointsManager.getPoints(this);
            pointsText.setText(String.valueOf(points));
        }
    }

    public void goBack(View view) {
        Intent intent = new Intent(this, Muntenia.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePointsDisplay();
    }

    // Check if all locations have been viewed
    private void checkAllLocationsViewed() {
        // Add points for viewing all locations in tour
        if (!allLocationsViewed) {
            // Logic to check if all locations have been viewed
            // This is a simplified example - in a real app you would track which locations have been viewed
            allLocationsViewed = true;
            viewAllLocations();
        }
    }

    // Add points for viewing all locations
    private void viewAllLocations() {
        // Add points for viewing all locations in tour
        if (allLocationsViewed) {
            pointsManager.addPoints(this, REGION, POINTS_VIEW_ALL);
            
            // Show a message that points were awarded
            Toast.makeText(this, 
                    "Felicitări! Ai câștigat " + POINTS_VIEW_ALL + " puncte pentru vizualizarea tuturor locațiilor!", 
                    Toast.LENGTH_LONG).show();
            
            // Update points display
            updatePointsDisplay();
        }
    }

    // Story parts
    private String[] storyParts = {
        "Povestea Țării Românești - Partea 1: Începuturile\n\n" +
        "Muntenia, cunoscută și sub numele de Țara Românească, își începe drumul istoric în negura timpurilor, când triburile geto-dacice populau aceste ținuturi. După cucerirea romană și retragerea aureliană, zona a devenit o punte între culturile latine și cele slave.\n\n" +
        "În documentele medievale, primele mențiuni ale unei formațiuni statale în această zonă datează din secolul al XIII-lea. Basarab I (1310-1352) este considerat întemeietorul Țării Românești, după ce a obținut independența față de Regatul Ungariei. Victoria sa de la Posada din 1330 a marcat momentul nașterii statului medieval.",

        "Povestea Țării Românești - Partea 2: Epoca de Aur\n\n" +
        "Mircea cel Bătrân (1386-1418) a extins granițele țării până la Marea Neagră și a construit un sistem de alianțe care a asigurat independența Țării Românești. Sub domnia sa, țara a cunoscut o înflorire culturală și economică, iar Mănăstirea Cozia, ctitoria sa, stă mărturie vremurilor de glorie.\n\n" +
        "Vlad Țepeș (1456-1462, cu întreruperi) a continuat lupta pentru independență și a implementat reforme administrative drastice pentru a întări autoritatea domnească și a combate corupția. Reședința sa principală era la Târgoviște, unde Turnul Chindiei domină și astăzi peisajul urban.",

        "Povestea Țării Românești - Partea 3: Sub Imperiul Otoman\n\n" +
        "Începând cu a doua jumătate a secolului al XV-lea, Țara Românească a intrat sub suzeranitatea Imperiului Otoman, păstrându-și însă autonomia internă. Domnitorii erau confirmați de Poartă, dar aveau libertate în problemele interne.\n\n" +
        "Matei Basarab (1632-1654) și Constantin Brâncoveanu (1688-1714) au fost domnitori care au reușit să mențină o relativă independență și au promovat cultura și artele. Stilul brâncovenesc în arhitectură, o sinteză între elemente orientale și occidentale, reprezintă cea mai originală contribuție a Țării Românești la patrimoniul cultural european.",

        "Povestea Țării Românești - Partea 4: Epoca Modernă\n\n" +
        "Revoluția de la 1821 condusă de Tudor Vladimirescu și apoi Revoluția de la 1848 au marcat trezirea conștiinței naționale. Figuri precum Nicolae Bălcescu au militat pentru unirea românilor și obținerea independenței.\n\n" +
        "În 1859, prin alegerea lui Alexandru Ioan Cuza ca domn al Moldovei și al Țării Românești, s-a realizat unirea principatelor și s-au pus bazele statului român modern. București, vechea reședință domnească a Țării Românești, a devenit capitala noului stat.",

        "Povestea Țării Românești - Partea 5: Moștenirea Culturală\n\n" +
        "Muntenia a dat culturii române personalități de marcă, precum Ion Luca Caragiale, născut la Haimanale (astăzi numit I.L. Caragiale) și Nicolae Grigorescu, originar din Pitaru, județul Dâmbovița.\n\n" +
        "Tradițiile populare sunt încă vii în regiunea Munteniei. Călușul, dans ce a fost inclus în patrimoniul UNESCO, își are originile în această zonă. Portul popular muntenesc se distinge prin cămăși albe brodate, fote și catrințe cu modele geometrice viu colorate.",

        "Povestea Țării Românești - Partea 6: Muntenia Contemporană\n\n" +
        "Astăzi, Muntenia continuă să fie o regiune dinamică, ce îmbină tradițiile cu modernitatea. București, capitala țării, este un oraș cosmopolit cu o viață culturală intensă.\n\n" +
        "Câmpia Română, cu solul său fertil, rămâne un important bazin agricol, în timp ce zonele urbane precum Ploiești reprezintă centre industriale de primă importanță.\n\n" +
        "Munții care străjuiesc la nord Muntenia, cu stațiuni precum Sinaia, Bușteni și Predeal, atrag anual milioane de turiști, dornici să descopere frumusețile naturale și culturale ale regiunii."
    };

    // Show the next part of the story
    private void showNextStoryPart() {
        currentStoryPart++;
        
        if (currentStoryPart < storyParts.length) {
            // Display the next part of the story
            storyTextView.setText(storyParts[currentStoryPart]);
            
            // Award points for reading story part
            pointsManager.addPoints(this, REGION, 5);
            Toast.makeText(this, "Ai primit 5 puncte pentru citirea poveștii!", Toast.LENGTH_SHORT).show();
            
            // Update points display
            updatePointsDisplay();
        } else {
            // Story is completed, show button to view locations
            showLocationsButton.setVisibility(View.VISIBLE);
            nextStoryButton.setVisibility(View.GONE);
            
            // Award bonus points for completing the story
            if (!storyCompleted) {
                storyCompleted = true;
                pointsManager.addPoints(this, REGION, 15);
                Toast.makeText(this, "Felicitări! Ai finalizat întreaga poveste și ai primit 15 puncte bonus!", 
                        Toast.LENGTH_LONG).show();
                updatePointsDisplay();
            }
        }
    }
    
    // Show the locations section
    private void showLocations() {
        storyScrollView.setVisibility(View.GONE);
        locationsLayout.setVisibility(View.VISIBLE);
    }

    // Show a dialog with the story part (old method, kept for reference)
    private void showStoryDialog(String storyText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Povestea Munteniei")
               .setMessage(storyText)
               .setPositiveButton("Continuă", (dialog, id) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
} 
