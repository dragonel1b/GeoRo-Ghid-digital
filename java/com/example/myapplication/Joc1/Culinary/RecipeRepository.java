package com.example.myapplication.Joc1.Culinary;

import android.content.Context;

import com.example.myapplication.Joc1.Culinary.ModernCulinaryActivity.Recipe;
import com.example.myapplication.Joc1.Culinary.RecipeDBHelper;
import com.example.myapplication.Joc1.Culinary.MealPlanDBHelper;
import com.example.myapplication.Joc1.Culinary.UserCulinaryProfile;
import com.example.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Repository care gestionează accesul la date pentru rețete
 * Oferă o interfață unificată pentru accesarea datelor din diverse surse
 */
public class RecipeRepository {
    private static RecipeRepository instance;
    
    private final Context context;
    private final RecipeDBHelper dbHelper;
    private final MealPlanDBHelper mealPlanHelper;
    private List<Recipe> cachedRecipes;
    
    private RecipeRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new RecipeDBHelper(context);
        this.mealPlanHelper = new MealPlanDBHelper(context);
        loadRecipesFromDatabase();
    }
    
    public static synchronized RecipeRepository getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeRepository(context);
        }
        return instance;
    }
    
    private void loadRecipesFromDatabase() {
        // Încărcăm rețetele din baza de date
        // Pentru exemplificare, vom crea o listă de rețete mock
        cachedRecipes = new ArrayList<>();
        
        // Aici ar trebui să folosim dbHelper pentru a încărca rețetele din baza de date
        // Pentru exemplu, vom crea câteva rețete dummy
        createMockRecipes();
    }
    
    private void createMockRecipes() {
        // ====== TRANSILVANIA ======
        Recipe recipe1 = new Recipe(
            "Gulaș Ardelenesc", "Transilvania", "Feluri principale",
            "Gulaș tradițional cu carne de vită și cartofi, aromat cu paprika.",
            "Mediu", "2h 30min", new String[]{}, new String[]{}
        );
        recipe1.setId(1);
        recipe1.setRating(4.7f, 1);
        cachedRecipes.add(recipe1);
        
        Recipe recipe2 = new Recipe(
            "Kürtőskalács", "Transilvania", "Deserturi",
            "Cozonac secuiesc în formă de horn, copt pe jar și glazurat cu zahăr caramelizat.",
            "Dificil", "1h 15min", new String[]{}, new String[]{}
        );
        recipe2.setId(2);
        recipe2.setRating(4.9f, 1);
        recipe2.setFavorite(true);
        cachedRecipes.add(recipe2);

        // Add more recipes following the same pattern...
        // For brevity, I'm showing just a couple examples
        // In the real implementation, you would add all recipes
    }
    
    public List<Recipe> getAllRecipes() {
        if (cachedRecipes == null || cachedRecipes.isEmpty()) {
            loadRecipesFromDatabase();
        }
        return new ArrayList<>(cachedRecipes);
    }
    
    public Recipe getRecipeById(long id) {
        if (cachedRecipes == null) {
            loadRecipesFromDatabase();
        }
        
        for (Recipe recipe : cachedRecipes) {
            if (recipe.getId() == id) {
                return recipe;
            }
        }
        
        return null;
    }
    
    public List<Recipe> getFeaturedRecipes() {
        // Returnează 5 rețete aleatorii pentru carusel
        List<Recipe> allRecipes = getAllRecipes();
        List<Recipe> featured = new ArrayList<>();
        
        if (allRecipes.size() <= 5) {
            return new ArrayList<>(allRecipes);
        }
        
        Random random = new Random();
        List<Integer> indexes = new ArrayList<>();
        
        while (indexes.size() < 5) {
            int index = random.nextInt(allRecipes.size());
            if (!indexes.contains(index)) {
                indexes.add(index);
                featured.add(allRecipes.get(index));
            }
        }
        
        return featured;
    }
    
    public List<Recipe> getPopularRecipes() {
        // Returnează rețetele cu cele mai mari ratinguri
        return getAllRecipes().stream()
            .sorted((r1, r2) -> Float.compare(r2.getRating(), r1.getRating()))
            .limit(6)
            .collect(Collectors.toList());
    }
    
    public List<Recipe> getRecommendedRecipesForUser(UserCulinaryProfile userProfile) {
        // Returnează rețete recomandate bazate pe preferințele utilizatorului
        List<Recipe> allRecipes = getAllRecipes();
        
        // Obținem rețetele deja completate
        Set<String> completedRecipeIds = userProfile.getCompletedRecipes();
        
        // Filtrăm rețetele care nu au fost gătite încă
        List<Recipe> untriedRecipes = allRecipes.stream()
            .filter(recipe -> !completedRecipeIds.contains(recipe.getTitle() + "_" + recipe.getRegion()))
            .collect(Collectors.toList());
        
        // Dacă nu sunt suficiente rețete încercate, întoarcem aleator din cele disponibile
        if (untriedRecipes.size() < 4) {
            return untriedRecipes;
        }
        
        // Altfel, recomandăm bazat pe preferințe (implementare simplificată)
        String skillLevel = userProfile.getSkillLevel();
        
        return untriedRecipes.stream()
            .filter(recipe -> {
                if (skillLevel.equals(UserCulinaryProfile.SKILL_BEGINNER)) {
                    return recipe.getDifficulty().equals("Ușor");
                } else if (skillLevel.equals(UserCulinaryProfile.SKILL_INTERMEDIATE)) {
                    return recipe.getDifficulty().equals("Ușor") || recipe.getDifficulty().equals("Mediu");
                } else {
                    return true; // Pentru utilizatorii avansați, toate sunt ok
                }
            })
            .limit(4)
            .collect(Collectors.toList());
    }
    
    public void toggleFavorite(Recipe recipe) {
        boolean newStatus = !recipe.isFavorite();
        recipe.setFavorite(newStatus);
        
        // Aici ar trebui să actualizăm și în baza de date
        // dbHelper.updateFavoriteStatus(recipe.getId(), newStatus);
    }
    
    public void markRecipeCompleted(Recipe recipe) {
        // Aici ar trebui să marcăm rețeta ca finalizată în baza de date
        // dbHelper.markRecipeAsCompleted(recipe.getId());
    }
    
    public void addRecipeToMealPlan(Recipe recipe, String mealType, String dateStr) {
        // Convertim string-ul de dată într-un obiect Date
        Date date;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            date = format.parse(dateStr);
        } catch (ParseException e) {
            date = new Date(); // Dacă apare o eroare, folosim data curentă
        }
        
        // Aici ar trebui să adăugăm în baza de date
        // mealPlanHelper.addMealToPlan(recipe.getId(), mealType, date);
    }
    
    public void saveRecipe(Recipe recipe) {
        // Aici ar trebui să salvăm rețeta în baza de date
        // Dacă are id, facem update, altfel inserăm
        
        // Dacă nu are id (e nouă), generăm unul
        if (recipe.getId() == 0) {
            long newId = cachedRecipes.size() + 1;
            
            // Ar trebui să adăugăm în baza de date și să obținem id-ul real
            // long newId = dbHelper.insertRecipe(...);
            
            // Adăugăm la cache
            cachedRecipes.add(recipe);
        } else {
            // Actualizăm în cache
            for (int i = 0; i < cachedRecipes.size(); i++) {
                if (cachedRecipes.get(i).getId() == recipe.getId()) {
                    cachedRecipes.set(i, recipe);
                    break;
                }
            }
            
            // Ar trebui să actualizăm și în baza de date
            // dbHelper.updateRecipe(...);
        }
    }
}
