package com.example.myapplication.Joc1;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RomCulinaryActivity extends AppCompatActivity {
    private RomGameState gameState;
    private RecyclerView recipesRecyclerView;
    private ProgressBar culinaryProgress;
    private TextView culinaryProgressText;
    private ChipGroup regionChipGroup;
    private List<Recipe> recipes;
    private List<Recipe> filteredRecipes;
    private int discoveredRecipes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rom_culinary);

        gameState = RomGameState.getInstance();
        gameState.initialize(this);

        initializeViews();
        setupToolbar();
        setupRecipes();
        setupRecyclerView();
        setupRegionFilter();
        updateProgress();
    }

    private void initializeViews() {
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        culinaryProgress = findViewById(R.id.culinaryProgress);
        culinaryProgressText = findViewById(R.id.culinaryProgressText);
        regionChipGroup = findViewById(R.id.regionChipGroup);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupRecipes() {
        recipes = new ArrayList<>();

        // Add recipes for different regions
        recipes.add(new Recipe(
                "Sarmale Moldovenești",
                "Moldova",
                "Sarmale tradiționale moldovenești cu carne de porc, orez și verdeață",
                "Mediu",
                "120 min",
                new String[] {
                        "500g carne tocată de porc",
                        "200g orez",
                        "2 cepe",
                        "1 morcov",
                        "Varză murată",
                        "Mărar și pătrunjel"
                },
                new String[] {
                        "Se călește ceapa și morcovul",
                        "Se amestecă carnea cu orezul și legumele",
                        "Se înfășoară în foi de varză",
                        "Se fierb timp de 2 ore"
                }
        ));

        recipes.add(new Recipe(
                "Cozonac Ardelenesc",
                "Transilvania",
                "Cozonac tradițional cu nucă, mac și stafide",
                "Dificil",
                "180 min",
                new String[] {
                        "1kg făină",
                        "6 ouă",
                        "200g unt",
                        "200g zahăr",
                        "500ml lapte",
                        "Nucă măcinată",
                        "Mac",
                        "Stafide"
                },
                new String[] {
                        "Se prepară aluatul și se lasă la dospit",
                        "Se întinde și se umple cu nucă și mac",
                        "Se rulează și se coace 45 minute"
                }
        ));

        recipes.add(new Recipe(
                "Plăcintă Dobrogeană",
                "Dobrogea",
                "Plăcintă cu brânză de oaie și mărar proaspăt",
                "Mediu",
                "90 min",
                new String[] {
                        "Făină",
                        "Apă",
                        "Sare",
                        "Brânză de oaie",
                        "Mărar proaspăt"
                },
                new String[] {
                        "Se întinde foaia foarte subțire",
                        "Se pune umplutura de brânză",
                        "Se împătură și se coace"
                }
        ));

        filteredRecipes = new ArrayList<>(recipes);
    }

    private void setupRecyclerView() {
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecipeAdapter adapter = new RecipeAdapter(filteredRecipes,
                this::handleViewRecipe,
                this::handlePrepareRecipe);
        recipesRecyclerView.setAdapter(adapter);
        recipesRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupRegionFilter() {
        regionChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.allRegionsChip) {
                filteredRecipes = new ArrayList<>(recipes);
            } else {
                Chip selectedChip = group.findViewById(checkedId);
                String selectedRegion = selectedChip.getText().toString();
                filteredRecipes = recipes.stream()
                        .filter(recipe -> recipe.getRegion().equals(selectedRegion))
                        .collect(Collectors.toList());
            }
            recipesRecyclerView.getAdapter().notifyDataSetChanged();
        });
    }

    private void handleViewRecipe(Recipe recipe) {
        StringBuilder details = new StringBuilder();
        details.append("Ingrediente:\n");
        for (String ingredient : recipe.getIngredients()) {
            details.append("• ").append(ingredient).append("\n");
        }
        details.append("\nPași de preparare:\n");
        for (int i = 0; i < recipe.getSteps().length; i++) {
            details.append(i + 1).append(". ").append(recipe.getSteps()[i]).append("\n");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(recipe.getTitle())
                .setMessage(details.toString())
                .setPositiveButton("Închide", null)
                .show();
    }

    private void handlePrepareRecipe(Recipe recipe) {
        if (!recipe.isDiscovered()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Prepară " + recipe.getTitle())
                    .setMessage("Vrei să începi prepararea acestei rețete?\n\n" +
                            "Timp estimat: " + recipe.getTime() + "\n" +
                            "Dificultate: " + recipe.getDifficulty())
                    .setPositiveButton("Da", (dialog, which) -> completeRecipe(recipe))
                    .setNegativeButton("Nu", null)
                    .show();
        }
    }

    private void completeRecipe(Recipe recipe) {
        if (!recipe.isDiscovered()) {
            recipe.setDiscovered(true);
            discoveredRecipes++;
            updateProgress();

            // Award wisdom points
            gameState.addPuncteIntelepte(15, this);

            Toast.makeText(this,
                    "Rețetă descoperită! +15 Puncte Înțelepte",
                    Toast.LENGTH_LONG).show();

            // Update RecyclerView
            recipesRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void updateProgress() {
        int totalRecipes = recipes.size();
        int progress = (discoveredRecipes * 100) / totalRecipes;
        culinaryProgress.setProgress(progress);
        culinaryProgressText.setText(
                String.format("%d/%d rețete descoperite", discoveredRecipes, totalRecipes));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Inner class for recipes
    public static class Recipe {
        private final String title;
        private final String region;
        private final String description;
        private final String difficulty;
        private final String time;
        private final String[] ingredients;
        private final String[] steps;
        private boolean discovered;

        Recipe(String title, String region, String description,
               String difficulty, String time, String[] ingredients, String[] steps) {
            this.title = title;
            this.region = region;
            this.description = description;
            this.difficulty = difficulty;
            this.time = time;
            this.ingredients = ingredients;
            this.steps = steps;
            this.discovered = false;
        }

        public String getTitle() { return title; }
        public String getRegion() { return region; }
        public String getDescription() { return description; }
        public String getDifficulty() { return difficulty; }
        public String getTime() { return time; }
        public String[] getIngredients() { return ingredients; }
        public String[] getSteps() { return steps; }
        public boolean isDiscovered() { return discovered; }
        public void setDiscovered(boolean discovered) { this.discovered = discovered; }
    }
}
