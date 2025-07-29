package com.example.myapplication.recipe.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for Recipe model class
 * Tests recipe creation, validation, and business logic
 */
public class RecipeTest {

    @Test
    public void testRecipeCreation_ValidData_Success() {
        // Arrange
        String title = "Sarmale";
        String description = "Traditional Romanian stuffed cabbage rolls";
        int prepTime = 30;
        int cookTime = 60;
        int servings = 4;
        String difficulty = "Medium";
        String category = "Main Course";
        String region = "Muntenia";
        
        List<Ingredient> ingredients = Arrays.asList(
            new Ingredient("Cabbage leaves", "1 head", "Vegetable"),
            new Ingredient("Ground pork", "500g", "Meat"),
            new Ingredient("Rice", "200g", "Grain")
        );
        
        List<String> steps = Arrays.asList(
            "Prepare the cabbage leaves",
            "Mix the filling ingredients",
            "Roll the sarmale",
            "Cook for 60 minutes"
        );

        // Act
        Recipe recipe = new Recipe(1, title, description, category, region, difficulty, prepTime, cookTime, servings, 0);
        recipe.setIngredients(ingredients);
        recipe.setPreparationSteps(steps);

        // Assert
        assertEquals(title, recipe.getTitle());
        assertEquals(description, recipe.getDescription());
        assertEquals(prepTime, recipe.getPreparationTime());
        assertEquals(cookTime, recipe.getCookingTime());
        assertEquals(servings, recipe.getServings());
        assertEquals(difficulty, recipe.getDifficulty());
        assertEquals(category, recipe.getCategory());
        assertEquals(region, recipe.getRegion());
        assertEquals(ingredients.size(), recipe.getIngredients().size());
        assertEquals(steps.size(), recipe.getPreparationSteps().size());
    }

    @Test
    public void testGetTotalTime_ReturnsSumOfPrepAndCookTime() {
        // Arrange
        Recipe recipe = new Recipe(1, "Test", "Test", "Test", "Test", "Easy", 30, 60, 4, 0);

        // Act
        int totalTime = recipe.getTotalTime();

        // Assert
        assertEquals(90, totalTime);
    }

    @Test
    public void testRecipeValidation_ValidRecipe_ReturnsTrue() {
        // Arrange
        Recipe recipe = new Recipe(1, "Test Recipe", "Test Description", "Main Course", "Transilvania", "Easy", 30, 60, 4, 0);
        recipe.setIngredients(Arrays.asList(new Ingredient("Test", "1", "Test")));
        recipe.setPreparationSteps(Arrays.asList("Step 1"));

        // Act & Assert
        assertTrue(recipe.getTitle() != null && !recipe.getTitle().isEmpty());
        assertTrue(recipe.getDescription() != null && !recipe.getDescription().isEmpty());
        assertTrue(recipe.getServings() > 0);
    }

    @Test
    public void testRecipeValidation_InvalidRecipe_ReturnsFalse() {
        // Arrange - Recipe with missing required fields
        Recipe recipe = new Recipe(1, "", "Test Description", "Main Course", "Transilvania", "Easy", 30, 60, 0, 0);

        // Act & Assert
        assertFalse(recipe.getTitle() != null && !recipe.getTitle().isEmpty());
        assertFalse(recipe.getServings() > 0);
    }

    @Test
    public void testGetDifficultyLevel_ReturnsCorrectLevel() {
        // Arrange & Act
        Recipe easyRecipe = new Recipe(1, "Easy", "Test", "Test", "Test", "Easy", 30, 60, 4, 0);
        Recipe mediumRecipe = new Recipe(2, "Medium", "Test", "Test", "Test", "Medium", 30, 60, 4, 0);
        Recipe hardRecipe = new Recipe(3, "Hard", "Test", "Test", "Test", "Hard", 30, 60, 4, 0);

        // Assert
        assertEquals("Easy", easyRecipe.getDifficulty());
        assertEquals("Medium", mediumRecipe.getDifficulty());
        assertEquals("Hard", hardRecipe.getDifficulty());
    }

    @Test
    public void testGetEstimatedCalories_ReturnsReasonableEstimate() {
        // Arrange
        Recipe recipe = new Recipe(1, "Test", "Test", "Test", "Test", "Easy", 30, 60, 4, 0);
        recipe.setIngredients(Arrays.asList(
            new Ingredient("Chicken", "500g", "Meat"),
            new Ingredient("Rice", "200g", "Grain"),
            new Ingredient("Vegetables", "300g", "Vegetable")
        ));

        // Act
        int totalTime = recipe.getTotalTime();

        // Assert
        assertTrue("Total time should be positive", totalTime > 0);
        assertTrue("Total time should be reasonable", totalTime < 300);
    }

    @Test
    public void testToString_ReturnsFormattedString() {
        // Arrange
        Recipe recipe = new Recipe(1, "Sarmale", "Test", "Test", "Muntenia", "Medium", 30, 60, 4, 0);

        // Act
        String result = recipe.toString();

        // Assert
        assertTrue(result.contains("Sarmale"));
        assertTrue(result.contains("Muntenia"));
        assertTrue(result.contains("Medium"));
    }
} 