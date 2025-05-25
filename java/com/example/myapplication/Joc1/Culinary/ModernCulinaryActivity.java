package com.example.myapplication.Joc1.Culinary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.Joc1.RomGameState;
import com.example.myapplication.Joc1.Culinary.RecipeDetailActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/**
 * Activitate principală modernă pentru modulul culinar
 * Folosește Material Design 3 și arhitectura MVVM
 */
public class ModernCulinaryActivity extends AppCompatActivity implements CulinaryActivityInterface, RecipeAdapter.OnRecipeActionListener {

    private ViewPager categoryViewPager;
    private TabLayout categoryTabLayout;
    private RecyclerView featuredRecipesRecyclerView;
    private ChipGroup regionsChipGroup;
    // CulinaryViewModel removed

    /**
     * Recipe class representing a culinary recipe
     */
    public static class Recipe {
        private long id;
        private String title;
        private String region;
        private String category;
        private String description;
        private String difficulty;
        private String time;
        private String[] ingredients;
        private String[] steps;
        private float rating;
        private int ratingCount;
        private boolean favorite;
        private int imageResourceId;
        private String prepTime;
        private NutritionalInfo nutritionalInfo;
        private boolean isDiscovered;
        private boolean isUserSubmitted;
        private String submittedBy;
        private String userPhotoPath;

        // Constructor gol necesar pentru Firebase
        public Recipe() {
            this.rating = 0;
            this.ratingCount = 0;
            this.favorite = false;
            this.isDiscovered = false;
            this.isUserSubmitted = false;
        }

        public Recipe(String title, String region, String category, String description,
                     String difficulty, String time, String[] ingredients, String[] steps) {
            this.title = title;
            this.region = region;
            this.category = category;
            this.description = description;
            this.difficulty = difficulty;
            this.time = time;
            this.ingredients = ingredients;
            this.steps = steps;
            this.rating = 0;
            this.ratingCount = 0;
            this.favorite = false;
            this.isDiscovered = false;
            this.isUserSubmitted = false;
            this.nutritionalInfo = new NutritionalInfo("", 0, 0, 0, 0, 0, 0, 0);
        }

        public Recipe(String recipeId, String title, String region, String category, String description,
                     String difficulty, String time, String[] ingredients, String[] steps) {
            this.id = Long.parseLong(recipeId);
            this.title = title;
            this.region = region;
            this.category = category;
            this.description = description;
            this.difficulty = difficulty;
            this.time = time;
            this.ingredients = ingredients;
            this.steps = steps;
            this.rating = 0;
            this.ratingCount = 0;
            this.favorite = false;
            this.isDiscovered = false;
            this.isUserSubmitted = false;
            this.nutritionalInfo = new NutritionalInfo("", 0, 0, 0, 0, 0, 0, 0);
        }

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getName() { return title; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public String[] getIngredients() { return ingredients; }
        public void setIngredients(String[] ingredients) { this.ingredients = ingredients; }
        public String[] getSteps() { return steps; }
        public void setSteps(String[] steps) { this.steps = steps; }
        public float getRating() { return rating; }
        public void setRating(float rating) { this.rating = rating; }
        public int getRatingCount() { return ratingCount; }
        public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
        public boolean isFavorite() { return favorite; }
        public void setFavorite(boolean favorite) { 
            this.favorite = favorite; 
        }

        public void toggleFavorite() {
            this.favorite = !this.favorite;
        }

        public boolean isDiscovered() {
            return isDiscovered;
        }

        public void setDiscovered(boolean discovered) {
            isDiscovered = discovered;
        }

        public boolean isUserSubmitted() {
            return isUserSubmitted;
        }

        public void setUserSubmitted(boolean userSubmitted) {
            isUserSubmitted = userSubmitted;
        }

        public String getSubmittedBy() {
            return submittedBy;
        }

        public void setSubmittedBy(String submittedBy) {
            this.submittedBy = submittedBy;
        }

        public String getUserPhotoPath() {
            return userPhotoPath;
        }

        public void setUserPhotoPath(String userPhotoPath) {
            this.userPhotoPath = userPhotoPath;
        }

        public boolean hasNutritionalInfo() {
            return nutritionalInfo != null && nutritionalInfo.isComplete();
        }

        public void addRating(float newRating) {
            float totalRating = (rating * ratingCount) + newRating;
            ratingCount++;
            rating = totalRating / ratingCount;
        }
        
        public void setRating(float rating, int count) {
            this.rating = rating;
            this.ratingCount = count;
        }

        public int getImageResourceId() {
            return imageResourceId;
        }

        public void setImageResourceId(int imageResourceId) {
            this.imageResourceId = imageResourceId;
        }

        public String getFormattedRating() {
            return String.format("%.1f ★", rating);
        }

        public String getPrepTime() {
            return time;
        }
        
        public void setPrepTime(String prepTime) {
            this.prepTime = prepTime;
            this.time = prepTime; // Se asigură compatibilitatea cu codul existent
        }

        public NutritionalInfo getNutritionalInfo() {
            return nutritionalInfo;
        }

        public void setNutritionalInfo(NutritionalInfo nutritionalInfo) {
            this.nutritionalInfo = nutritionalInfo;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modern_culinary);

        // Inițializare ViewModel - removed

        // Inițializare UI
        initializeViews();
        setupToolbar();
        setupCategoryPager();
        setupFeaturedRecipes();
        setupFilters();
        
        // Observă datele din ViewModel
        observeViewModel();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.rom_culinary_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void initializeViews() {
        categoryViewPager = findViewById(R.id.categoryViewPager);
        categoryTabLayout = findViewById(R.id.categoryTabLayout);
        featuredRecipesRecyclerView = findViewById(R.id.featuredRecipesRecyclerView);
        regionsChipGroup = findViewById(R.id.regionsChipGroup);
    }

    private void setupCategoryPager() {
        // Inițializare adapter pentru categorii - simplificat pentru a elimina referințele la CategoryPagerAdapter
        /*CategoryPagerAdapter categoryAdapter = new CategoryPagerAdapter(this);
        categoryViewPager.setAdapter(categoryAdapter);

        // Conectare cu TabLayout pentru a afișa titlurile categoriilor
        for (String category : CulinaryUtils.CATEGORIES) {
            categoryTabLayout.addTab(categoryTabLayout.newTab().setText(category));
        }*/
        
        // Folosim categorii definite static
        String[] categories = {"Aperitive", "Supe și ciorbe", "Feluri principale", "Deserturi", "Pâine și produse de patiserie"};
        for (String category : categories) {
            categoryTabLayout.addTab(categoryTabLayout.newTab().setText(category));
        }

        // Listener pentru schimbarea taburilor
        categoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                categoryViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });

        categoryViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Not needed
            }

            @Override
            public void onPageSelected(int position) {
                categoryTabLayout.selectTab(categoryTabLayout.getTabAt(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Not needed
            }
        });
    }

    @Override
    public void setupFilters() {
        // Adăugare filtre de regiune
        /*for (String region : CulinaryUtils.REGIONS) {
            Chip chip = new Chip(this);
            chip.setText(region);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_color);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Aplică filtrul
                applyFilters();
            });
            regionsChipGroup.addView(chip);
        }*/
        
        // Folosim regiuni definite static
        String[] regions = {"Moldova", "Transilvania", "Muntenia", "Oltenia", "Banat", "Dobrogea", "Bucovina", "Maramureș"};
        for (String region : regions) {
            Chip chip = new Chip(this);
            chip.setText(region);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_color);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Aplică filtrul
                applyFilters();
            });
            regionsChipGroup.addView(chip);
        }
    }

    @Override
    public void setupRecipes() {
        // Încarcă lista de rețete
        //viewModel.loadRecipes();
        // Implementare simplificată fără ViewModel
    }

    private void setupFeaturedRecipes() {
        // Simulare date
        List<Recipe> recipes = createSampleRecipes();
        
        // Setare adapter pentru lista de rețete recomandate - înlocuim FeaturedRecipeAdapter cu RecipeAdapter
        /*FeaturedRecipeAdapter adapter = new FeaturedRecipeAdapter(recipes, recipe -> {
            // Deschide detalii rețetă
            openRecipeDetail(recipe);
        });*/
        
        RecipeAdapter adapter = new RecipeAdapter(recipes, this, this);
        featuredRecipesRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // ViewModel removed
    }

    private void updateRecipesList(List<Recipe> recipes) {
        // Actualizare filtre și vizibilitate
        // În implementarea reală, aici ar trebui să actualizați adaptoarele
    }

    @Override
    public void navigateToSearch() {
        // Metoda menținută pentru compatibilitatea cu interfața, dar nu mai afișează Toast
    }

    private void showAddRecipeScreen() {
        // Metoda menținută pentru compatibilitatea cu codul, dar nu mai afișează Toast
    }

    private void showMyRecipesScreen() {
        // Metoda menținută pentru compatibilitatea cu codul, dar nu mai afișează Toast
    }

    private void showMealPlanScreen() {
        // Metoda menținută pentru compatibilitatea cu codul, dar nu mai afișează Toast
    }

    @Override
    public void showUserProfile() {
        // Metoda menținută pentru compatibilitatea cu interfața, dar nu mai afișează Toast
    }

    /**
     * Open recipe detail screen
     * @param recipe Recipe to display
     */
    public void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        intent.putExtra("recipe_title", recipe.getTitle());
        intent.putExtra("recipe_region", recipe.getRegion());
        startActivity(intent);
    }

    private void showWelcomeScreen() {
        // Metoda menținută pentru compatibilitatea cu codul, dar nu mai afișează Toast
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRecipeSelected(long recipeId) {
        // Find recipe by ID
        Recipe recipe = findRecipeById(recipeId);
        if (recipe != null) {
            openRecipeDetail(recipe);
        }
    }

    private Recipe findRecipeById(long recipeId) {
        // Find recipe in all available recipes
        List<Recipe> allRecipes = createSampleRecipes();
        if (allRecipes != null) {
            for (Recipe recipe : allRecipes) {
                if (recipe.getId() == recipeId) {
                    return recipe;
                }
            }
        }
        return null;
    }

    @Override
    public void applyFilters() {
        // Get selected regions
        List<String> selectedRegions = new ArrayList<>();
        for (int i = 0; i < regionsChipGroup.getChildCount(); i++) {
            View view = regionsChipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                if (chip.isChecked()) {
                    selectedRegions.add(chip.getText().toString());
                }
            }
        }
        
        // Apply filters
        // viewModel.applyFilters(selectedRegions); - removed
    }

    /**
     * Create sample recipes for testing
     */
    public static List<Recipe> getRecipes() {
        return new ModernCulinaryActivity().createSampleRecipes();
    }

    private List<Recipe> createSampleRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        // Rețeta 1: Sarmale în foi de viță
        Recipe recipe1 = new Recipe(
            "Sarmale în foi de viță", "Moldova", "Feluri principale",
            "Sarmale tradiționale moldovenești cu carne tocată și orez în foi de viță. Această rețetă este specifică regiunii Moldovei și este preparată în special la sărbătorile importante.",
            "Mediu", "2h 30min", 
            new String[]{
                "500g carne tocată de porc și vită",
                "150g orez cu bobul rotund",
                "2 cepe mari",
                "2 morcovi",
                "50ml ulei",
                "1 legătură foi de viță (30-40 foi)",
                "2 linguri pastă de tomate",
                "1 legătură mărar",
                "1 legătură pătrunjel",
                "2 foi de dafin",
                "sare și piper după gust",
                "1 lingură cimbru uscat",
                "200ml bulion de roșii",
                "100ml smântână pentru servire"
            }, 
            new String[]{
                "Spălați orezul și scurgeți-l bine.",
                "Curățați și tocați fin ceapa și morcovii.",
                "Căliti ceapa și morcovii în ulei până se înmoaie.",
                "Într-un castron mare, amestecați carnea tocată cu orezul, legumele călite, mărar și pătrunjel tocat, sare, piper și cimbru.",
                "Opăriți foile de viță în apă fierbinte timp de 2-3 minute pentru a le înmuia.",
                "Puneți câte o lingură din compoziția de carne pe fiecare foaie de viță și rulați, îndoind marginile spre interior.",
                "Așezați sarmalele într-o oală, adăugați foi de dafin, pasta de tomate diluată în 500ml apă caldă și bulionul de roșii.",
                "Fierbeți la foc mic aproximativ 2 ore, cu capac.",
                "Serviți calde cu smântână deasupra."
            }
        );
        recipe1.setId(1);
        recipe1.setRating(4.8f, 125);
        recipe1.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe1);
        
        // Rețeta 2: Papanași cu smântână și dulceață
        Recipe recipe2 = new Recipe(
            "Papanași cu smântână și dulceață", "Transilvania", "Deserturi",
            "Papanași pufoși cu smântână și dulceață de afine. Un desert tradițional românesc foarte iubit, adesea servit în zonele rurale din Transilvania.",
            "Mediu", "45min", 
            new String[]{
                "500g brânză de vaci bine scursă",
                "2 ouă",
                "4-5 linguri făină",
                "2 linguri zahăr",
                "1 plic zahăr vanilat",
                "1 lingură coajă rasă de lămâie",
                "un praf de sare",
                "ulei pentru prăjit",
                "200g smântână",
                "200g dulceață de afine"
            }, 
            new String[]{
                "Amestecați brânza de vaci cu ouăle, zahărul, zahărul vanilat, coaja de lămâie și sarea până obțineți o compoziție omogenă.",
                "Adăugați treptat făina și frământați până obțineți un aluat moale dar care nu se lipește de mâini.",
                "Modelați din aluat bile de mărimea unei mingi de tenis și faceți-le o gaură în mijloc cu degetul, formând un inel.",
                "Modelați și câte o bilă mică pentru fiecare papanaș.",
                "Încălziți uleiul într-o tigaie adâncă.",
                "Prăjiți papanașii 2-3 minute pe fiecare parte, până devin aurii.",
                "Scoateți-i pe șervețele de hârtie pentru a absorbi surplusul de ulei.",
                "Serviți imediat, cu smântână și dulceață de afine, cu bila mică așezată deasupra."
            }
        );
        recipe2.setId(2);
        recipe2.setRating(4.9f, 187);
        recipe2.setFavorite(true);
        recipe2.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe2);
        
        // Rețeta 3: Ciorbă de burtă
        Recipe recipe3 = new Recipe(
            "Ciorbă de burtă", "Muntenia", "Supe și ciorbe",
            "Ciorbă de burtă autentică cu smântână și usturoi. Una dintre cele mai apreciate ciorbe românești, cu o textură cremoasă și un gust bogat.",
            "Dificil", "3h", 
            new String[]{
                "1kg burtă de vită fiartă și curățată",
                "2 morcovi",
                "1 păstârnac",
                "1 țelină mică",
                "1 ceapă",
                "150g smântână pentru gătit",
                "3 gălbenușuri",
                "100ml oțet",
                "6-8 căței de usturoi",
                "2 linguri ulei",
                "sare și piper după gust",
                "1 legătură pătrunjel",
                "ardei iute după preferințe"
            }, 
            new String[]{
                "Tăiați burta fiartă în fâșii subțiri.",
                "Curățați și tăiați legumele: morcovii, păstârnacul, țelina și ceapa.",
                "Într-o oală mare, puneți 3-4 litri de apă, adăugați legumele și fierbeți aproximativ 30 minute.",
                "Adăugați burta tăiată și continuați fierberea pentru încă 30 minute.",
                "Între timp, pisați usturoiul și amestecați-l cu puțină sare.",
                "Bateți gălbenușurile cu smântâna.",
                "Când legumele s-au fiert, luați 2-3 polonice de supă fierbinte și adăugați treptat peste amestecul de smântână și gălbenușuri, amestecând continuu.",
                "Turnați acest amestec înapoi în oală, amestecând ușor.",
                "Adăugați oțetul, usturoiul pisat, sare și piper după gust.",
                "Mai fierbeți 5 minute la foc mic, având grijă să nu clocotească (pentru a evita tăierea smântânii).",
                "Presărați pătrunjel proaspăt tocat și serviți cu ardei iute."
            }
        );
        recipe3.setId(3);
        recipe3.setRating(4.7f, 98);
        recipe3.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe3);
        
        // Rețeta 4: Plăcintă dobrogeană
        Recipe recipe4 = new Recipe(
            "Plăcintă dobrogeană", "Dobrogea", "Pâine și produse de patiserie",
            "Plăcintă tradițională dobrogeană cu brânză și mărar. O specialitate a regiunii Dobrogea, cu foi foarte subțiri și umplutură generoasă.",
            "Mediu", "1h 30min", 
            new String[]{
                "Pentru aluat:",
                "500g făină",
                "250ml apă călduță",
                "50ml ulei",
                "1 linguriță sare",
                "Pentru umplutură:",
                "500g brânză telemea de oaie",
                "200g urdă sau brânză de vaci",
                "4 ouă",
                "1 legătură mare de mărar",
                "1 lingură ulei pentru uns tava"
            }, 
            new String[]{
                "Într-un castron mare, amestecați făina cu sarea, apa călduță și uleiul, până obțineți un aluat moale.",
                "Frământați aluatul aproximativ 10 minute, până devine elastic.",
                "Împărțiți aluatul în 6 bucăți egale și lăsați-le să se odihnească 30 minute, acoperite cu un prosop.",
                "Între timp, preparați umplutura amestecând brânza telemea sfărâmată cu urda, 3 ouă și mărarul tocat.",
                "Întindeți fiecare bucată de aluat folosind făină pentru a nu se lipi, până devine foarte subțire.",
                "Ungeți o tavă de copt cu ulei.",
                "Așezați primul strat de aluat în tavă, ungeți-l cu puțin ulei, apoi continuați cu al doilea strat.",
                "Puneți jumătate din cantitatea de umplutură, apoi încă două foi de aluat unse cu ulei între ele.",
                "Adăugați restul umpluturii și acoperiți cu ultimele două foi de aluat, unsă fiecare cu ulei.",
                "Bateți ultimul ou și ungeți suprafața plăcintei.",
                "Coaceți la 180°C pentru aproximativ 35-40 minute, până când devine aurie.",
                "Lăsați să se răcească ușor înainte de a porționa și servi."
            }
        );
        recipe4.setId(4);
        recipe4.setRating(4.6f, 156);
        recipe4.setFavorite(true);
        recipe4.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe4);
        
        // Rețeta 5: Mămăligă cu brânză și smântână
        Recipe recipe5 = new Recipe(
            "Mămăligă cu brânză și smântână", "Oltenia", "Feluri principale",
            "Mămăligă cremoasă cu brânză de burduf și smântână. Un fel de mâncare tradițional românesc, simplu dar delicios, apreciat în toată țara.",
            "Ușor", "30min", 
            new String[]{
                "400g mălai",
                "1.2L apă",
                "1 lingură sare",
                "300g brânză de burduf (sau telemea de oaie)",
                "200g smântână",
                "100g unt",
                "100g jumări (opțional)"
            }, 
            new String[]{
                "Puneți apa cu sare la fiert într-o oală încăpătoare.",
                "Când apa începe să fiarbă, presărați mălaiul în ploaie, amestecând continuu cu un făcăleț pentru a evita formarea cocoloașelor.",
                "Fierbeți la foc mic aproximativ 20-25 minute, amestecând ocazional.",
                "Mămăliga este gata când se desprinde de pe marginile oalei și are o consistență potrivită.",
                "Răsturnați mămăliga pe un platou.",
                "Tăiați brânza felii sau sfărâmați-o.",
                "Tăiați mămăliga în felii și alternați straturi de mămăligă cu straturi de brânză.",
                "Adăugați bucăți de unt deasupra pentru a se topi.",
                "Serviți imediat cu smântână și jumări deasupra."
            }
        );
        recipe5.setId(5);
        recipe5.setRating(4.5f, 112);
        recipe5.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe5);

        // Rețeta 6: Ciorbă rădăuțeană
        Recipe recipe6 = new Recipe(
            "Ciorbă rădăuțeană", "Bucovina", "Supe și ciorbe",
            "Ciorbă tradițională din zona Bucovină, pe bază de carne de pui și smântână, cu un gust unic datorită usturoiului și oțetului.",
            "Mediu", "1h 45min", 
            new String[]{
                "1 pui întreg (sau 500g piept de pui)",
                "2 morcovi",
                "1 păstârnac",
                "1 țelină mică",
                "1 ceapă",
                "1 ardei gras",
                "200g smântână pentru gătit",
                "3 gălbenușuri",
                "100ml oțet",
                "8-10 căței de usturoi",
                "1 legătură pătrunjel",
                "sare și piper după gust"
            }, 
            new String[]{
                "Puneți puiul într-o oală cu apă rece și sare și fierbeți la foc mediu.",
                "Îndepărtați spuma formată la suprafață pe măsură ce apare.",
                "Adăugați legumele curățate și tăiate cubulețe: morcovi, păstârnac, țelină, ceapă și ardei gras.",
                "Fierbeți până când puiul și legumele sunt bine fierte (aproximativ 45-60 minute).",
                "Scoateți puiul, lăsați-l să se răcească și deșirați carnea în bucăți mici.",
                "Adăugați carnea înapoi în ciorbă.",
                "Într-un castron, amestecați smântâna cu gălbenușurile.",
                "Diluați amestecul cu câteva linguri din zeama fierbinte a ciorbei, amestecând continuu.",
                "Turnați amestecul înapoi în oala cu ciorbă, amestecând ușor.",
                "Adăugați oțetul, usturoiul pisat, sare și piper după gust.",
                "Mai fierbeți 5 minute la foc mic, fără a lăsa să clocotească.",
                "Presărați pătrunjel proaspăt tocat și serviți caldă."
            }
        );
        recipe6.setId(6);
        recipe6.setRating(4.7f, 85);
        recipe6.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe6);

        // Rețeta 7: Tochitură moldovenească
        Recipe recipe7 = new Recipe(
            "Tochitură moldovenească", "Moldova", "Feluri principale",
            "Preparat tradițional specific bucătăriei moldovenești, cu diverse tipuri de carne prăjită în untură și servit cu mămăligă și ouă ochiuri.",
            "Mediu", "1h 20min", 
            new String[]{
                "500g carne de porc (pulpă sau ceafă)",
                "200g cârnați afumați",
                "150g ficat de porc",
                "2 cepe mari",
                "3-4 căței de usturoi",
                "100g untură sau ulei",
                "100ml vin roșu",
                "sare și piper după gust",
                "4 ouă (pentru ochiuri)",
                "400g mălai (pentru mămăligă)",
                "200g brânză de burduf sau telemea",
                "pătrunjel pentru decor"
            }, 
            new String[]{
                "Tăiați carnea de porc în cuburi de aproximativ 2 cm.",
                "Tăiați cârnații în rondele și ficatul în bucăți mai mici.",
                "Tocați ceapa și usturoiul.",
                "Într-o tigaie mare, încălziți untura sau uleiul și prăjiți ceapa până devine translucidă.",
                "Adăugați carnea de porc și prăjiți la foc mediu-mare până se rumenește bine pe toate părțile.",
                "Adăugați cârnații și ficatul și continuați să prăjiți încă 5-7 minute.",
                "Turnați vinul și lăsați să fiarbă până se reduce puțin.",
                "Adăugați usturoiul pisat, sare și piper după gust.",
                "Între timp, preparați mămăliga și prăjiți ouăle ochiuri separat.",
                "Serviți tochirura fierbinte cu mămăligă, brânză și ouă ochiuri deasupra, presărată cu pătrunjel tocat."
            }
        );
        recipe7.setId(7);
        recipe7.setRating(4.9f, 132);
        recipe7.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe7);

        // Rețeta 8: Cozonac
        Recipe recipe8 = new Recipe(
            "Cozonac tradițional", "Muntenia", "Deserturi",
            "Cozonac tradițional românesc cu umplutură de nucă, cacao și stafide. Nelipsit de pe mesele românești în perioada sărbătorilor.",
            "Dificil", "3h 30min", 
            new String[]{
                "Pentru aluat:",
                "1kg făină",
                "500ml lapte",
                "200g zahăr",
                "200g unt",
                "6 gălbenușuri",
                "40g drojdie proaspătă",
                "coaja rasă de la o lămâie",
                "1 plic zahăr vanilat",
                "un praf de sare",
                "Pentru umplutură:",
                "400g nucă măcinată",
                "200g zahăr",
                "50g cacao",
                "100g stafide",
                "2 albușuri",
                "esență de rom",
                "1 ou pentru uns"
            }, 
            new String[]{
                "Încălziți laptele și dizolvați drojdia în el împreună cu 2 linguri de zahăr și puțină făină. Lăsați 15 minute să crească.",
                "Frecați gălbenușurile cu zahărul, apoi adăugați untul topit și răcit, coaja de lămâie și zahărul vanilat.",
                "Încorporați treptat maiaua de drojdie și făina, frământând până obțineți un aluat elastic care se desprinde de pereții vasului.",
                "Acoperiți aluatul și lăsați-l la dospit într-un loc cald timp de 1-1,5 ore, până își dublează volumul.",
                "Pentru umplutură, bateți albușurile spumă cu zahărul, apoi adăugați nucile măcinate, cacaoa, esența de rom și stafidele înmuiate în prealabil.",
                "Împărțiți aluatul dospit în două și întindeți fiecare bucată în formă de dreptunghi.",
                "Întindeți uniform jumătate din umplutură pe fiecare bucată de aluat, apoi rulați strâns ca pe un rulou.",
                "Așezați rulourile în două forme unse cu unt și lăsați-le să mai crească aproximativ 30 minute.",
                "Ungeți suprafața cu ou bătut și coaceți la 170°C pentru aproximativ 50-60 minute.",
                "Verificați cu scobitoarea dacă sunt copți - trebuie să iasă curată.",
                "Lăsați cozonacii să se răcească în forme timp de 10 minute, apoi scoateți-i și lăsați-i să se răcească complet pe un grătar."
            }
        );
        recipe8.setId(8);
        recipe8.setRating(4.8f, 215);
        recipe8.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe8);

        // Rețeta 9: Zacuscă
        Recipe recipe9 = new Recipe(
            "Zacuscă tradițională", "Transilvania", "Aperitive",
            "Conservă tradițională românească preparată din legume coapte. Perfectă ca aperitiv pe pâine sau ca garnitură pentru diverse feluri principale.",
            "Mediu", "4h", 
            new String[]{
                "3kg vinete",
                "2kg ardei kapia",
                "1kg ceapă",
                "1kg roșii coapte",
                "200ml ulei",
                "3-4 foi de dafin",
                "1 lingură cimbru uscat",
                "sare și piper după gust",
                "2-3 linguri zahăr",
                "borcanele pentru conservare"
            }, 
            new String[]{
                "Coaceți vinetele pe plită sau la grătar până devin moi. Curățați-le de coajă și lăsați-le să se scurgă de zeamă timp de 1-2 ore.",
                "Coaceți ardeii kapia, curățați-i de coajă și semințe.",
                "Tocați fin vinetele și ardeii (manual sau cu robotul de bucătărie).",
                "Tocați ceapa mărunt și căliți-o în ulei până devine transluciă.",
                "Adăugați vinetele și ardeii tocați și gătiți la foc mic aproximativ 1 oră, amestecând ocazional.",
                "Opăriți roșiile, curățați-le de pieliță și semințe și pasați-le.",
                "Adăugați roșiile pasate în compoziție și continuați gătirea la foc mic încă 1-2 ore, până când se reduce și capătă o consistență potrivită.",
                "Adăugați foile de dafin, cimbru, sare, piper și zahăr după gust.",
                "Între timp, steriliați borcanele și capacele.",
                "Umpleți borcanele cu zacuscă fierbinte, închideți-le ermetic și răsturnați-le cu capacul în jos până se răcesc complet.",
                "Păstrați borcanele în loc răcoros și uscat."
            }
        );
        recipe9.setId(9);
        recipe9.setRating(4.6f, 178);
        recipe9.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe9);

        // Rețeta 10: Varză a la Cluj
        Recipe recipe10 = new Recipe(
            "Varză a la Cluj", "Transilvania", "Feluri principale",
            "Preparat specific bucătăriei transilvănene, cu straturi alternante de varză și carne tocată. Asemănătoare cu sarmalele, dar mult mai ușor de preparat.",
            "Ușor", "1h 45min", 
            new String[]{
                "1 varză dulce (1-1.5kg)",
                "500g carne tocată (amestec porc și vită)",
                "2 cepe",
                "200g orez",
                "100g slănină afumată",
                "2 linguri ulei",
                "500ml bulion de roșii",
                "2 linguri pastă de tomate",
                "sare și piper după gust",
                "1 lingură cimbru uscat",
                "1 lingurița boia dulce",
                "smântână pentru servire"
            }, 
            new String[]{
                "Tăiați varza fâșii subțiri și opăriți-o 2-3 minute în apă cu sare.",
                "Tocați ceapa mărunt și căliti-o în ulei până devine translucidă.",
                "Adăugați carnea tocată și prăjiți-o până își schimbă culoarea.",
                "Condimentați cu sare, piper, cimbru și boia dulce.",
                "Spălați orezul și adăugați-l peste carne, amestecând bine.",
                "Într-o tavă termorezistentă, alternați straturi de varză cu straturi de amestec de carne și orez.",
                "Tăiați slănina cubulețe mici și presărați-o între straturi.",
                "Amestecați bulionul cu pasta de tomate și turnați-l peste compoziție.",
                "Acoperiți tava cu folie de aluminiu și coaceți la 180°C timp de 45 minute.",
                "Îndepărtați folia și mai coaceți încă 15-20 minute, până când se rumenește frumos deasupra.",
                "Serviți fierbinte, cu smântână deasupra."
            }
        );
        recipe10.setId(10);
        recipe10.setRating(4.5f, 95);
        recipe10.setImageResourceId(R.drawable.placeholder_recipe);
        recipes.add(recipe10);
        
        return recipes;
    }

    // Implementare OnRecipeActionListener
    @Override
    public void onRecipeAction(Recipe recipe) {
        openRecipeDetail(recipe);
    }
}
