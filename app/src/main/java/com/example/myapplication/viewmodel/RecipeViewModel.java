package com.example.myapplication.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.core.domain.model.Ingredient;
import com.example.myapplication.core.domain.model.Recipe;
import com.example.myapplication.repository.RecipeRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ViewModel for recipe functionality and data
 */
public class RecipeViewModel extends ViewModel {

    private final RecipeRepository repository;
    private final MutableLiveData<List<Recipe>> suggestedRecipes = new MutableLiveData<>();
    
    public RecipeViewModel() {
        // In a real app, this would be injected
        repository = new RecipeRepository();
    }
    
    /**
     * Get all available ingredients
     * @return List of ingredients
     */
    public List<Ingredient> getAllIngredients() {
        // In a real app, this would be fetched from repository/database
        List<Ingredient> ingredients = new ArrayList<>();
        
        // Sample data
        ingredients.add(new Ingredient(1, "Cartofi", "Legume", "kg", 1.0));
        ingredients.add(new Ingredient(2, "Ceapă", "Legume", "buc", 1.0));
        ingredients.add(new Ingredient(3, "Morcovi", "Legume", "buc", 2.0));
        ingredients.add(new Ingredient(4, "Varză", "Legume", "kg", 1.0));
        ingredients.add(new Ingredient(5, "Roșii", "Legume", "kg", 0.5));
        ingredients.add(new Ingredient(6, "Carne de porc", "Carne", "kg", 0.5));
        ingredients.add(new Ingredient(7, "Carne de vită", "Carne", "kg", 0.5));
        ingredients.add(new Ingredient(8, "Piept de pui", "Carne", "kg", 0.5));
        ingredients.add(new Ingredient(9, "Orez", "Cereale", "g", 200.0));
        ingredients.add(new Ingredient(10, "Făină", "Cereale", "g", 500.0));
        ingredients.add(new Ingredient(11, "Ulei", "Condimente", "ml", 50.0));
        ingredients.add(new Ingredient(12, "Sare", "Condimente", "g", 10.0));
        ingredients.add(new Ingredient(13, "Piper", "Condimente", "g", 5.0));
        ingredients.add(new Ingredient(14, "Ouă", "Lactate", "buc", 2.0));
        ingredients.add(new Ingredient(15, "Lapte", "Lactate", "ml", 200.0));
        
        return ingredients;
    }
    
    /**
     * Get recipe suggestions based on available ingredients
     * @param availableIngredients List of available ingredients
     * @return List of recipe suggestions sorted by match score
     */
    public List<Recipe> getSuggestedRecipesForIngredients(List<Ingredient> availableIngredients) {
        if (availableIngredients == null || availableIngredients.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Get all recipes from repository
        List<Recipe> allRecipes = getSampleRecipes();
        List<Recipe> matchedRecipes = new ArrayList<>();
        
        // Calculate match score for each recipe
        for (Recipe recipe : allRecipes) {
            recipe.calculateMatchScoreAndMissingIngredients(availableIngredients);
            
            // Only include recipes with at least 30% match
            if (recipe.getMatchScore() >= 30) {
                matchedRecipes.add(recipe);
            }
        }
        
        // Sort recipes by match score (highest first)
        Collections.sort(matchedRecipes, (r1, r2) -> Integer.compare(r2.getMatchScore(), r1.getMatchScore()));
        
        // Update LiveData
        suggestedRecipes.postValue(matchedRecipes);
        
        return matchedRecipes;
    }
    
    /**
     * Get Live Data of suggested recipes
     * @return LiveData for observing recipe suggestions
     */
    public LiveData<List<Recipe>> getSuggestedRecipesLiveData() {
        return suggestedRecipes;
    }
    
    /**
     * Sample recipe data for demonstration
     * In a real app, this would come from a repository/database
     */
    private List<Recipe> getSampleRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        List<Ingredient> allIngredients = getAllIngredients();
        
        // Sample recipe 1: Sarmale
        Recipe sarmale = new Recipe(
                1, 
                "Sarmale tradiționale moldovenești",
                "Sarmalele sunt un preparat tradițional românesc pe bază de carne tocată, orez și condimente, învelite în foi de varză sau de viță.",
                "https://example.com/images/sarmale.jpg",
                "Moldova",
                "Felul principal",
                60,
                180,
                "Mediu"
        );
        
        sarmale.setIngredients(Arrays.asList(
                allIngredients.get(3), // Varză
                allIngredients.get(5), // Carne de porc
                allIngredients.get(8), // Orez
                allIngredients.get(1), // Ceapă
                allIngredients.get(2), // Morcovi
                allIngredients.get(10), // Ulei
                allIngredients.get(11), // Sare
                allIngredients.get(12)  // Piper
        ));
        
        // Sample recipe 2: Cartofi țărănești
        Recipe cartofiTaranesti = new Recipe(
                2, 
                "Cartofi țărănești",
                "Un preparat simplu și gustos, cartofii țărănești sunt ideali ca garnitură sau ca fel principal.",
                "https://example.com/images/cartofi.jpg",
                "Transilvania",
                "Garnitură",
                20,
                40,
                "Ușor"
        );
        
        cartofiTaranesti.setIngredients(Arrays.asList(
                allIngredients.get(0), // Cartofi
                allIngredients.get(1), // Ceapă
                allIngredients.get(10), // Ulei
                allIngredients.get(11), // Sare
                allIngredients.get(12)  // Piper
        ));
        
        // Sample recipe 3: Ciorbă de legume
        Recipe ciorbaLegume = new Recipe(
                3, 
                "Ciorbă de legume",
                "O ciorbă sănătoasă, plină de vitamine, perfectă pentru orice anotimp.",
                "https://example.com/images/ciorba.jpg",
                "Muntenia",
                "Supă/Ciorbă",
                30,
                45,
                "Ușor"
        );
        
        ciorbaLegume.setIngredients(Arrays.asList(
                allIngredients.get(0), // Cartofi
                allIngredients.get(1), // Ceapă
                allIngredients.get(2), // Morcovi
                allIngredients.get(4), // Roșii
                allIngredients.get(11), // Sare
                allIngredients.get(12)  // Piper
        ));
        
        // Sample recipe 4: Tocăniță de pui
        Recipe tocanitaPui = new Recipe(
                4, 
                "Tocăniță de pui",
                "Un preparat tradițional, tocănița de pui este gustoasă și ușor de preparat.",
                "https://example.com/images/tocanita.jpg",
                "Oltenia",
                "Felul principal",
                20,
                60,
                "Mediu"
        );
        
        tocanitaPui.setIngredients(Arrays.asList(
                allIngredients.get(7), // Piept de pui
                allIngredients.get(0), // Cartofi
                allIngredients.get(1), // Ceapă
                allIngredients.get(2), // Morcovi
                allIngredients.get(4), // Roșii
                allIngredients.get(10), // Ulei
                allIngredients.get(11), // Sare
                allIngredients.get(12)  // Piper
        ));
        
        recipes.add(sarmale);
        recipes.add(cartofiTaranesti);
        recipes.add(ciorbaLegume);
        recipes.add(tocanitaPui);
        
        return recipes;
    }
} 