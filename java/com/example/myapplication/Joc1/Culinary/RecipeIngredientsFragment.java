package com.example.myapplication.Joc1.Culinary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment pentru afișarea listei de ingrediente pentru o rețetă
 */
public class RecipeIngredientsFragment extends Fragment {
    
    private static final String ARG_RECIPE_ID = "recipe_id";
    
    private long recipeId;
    private RecyclerView ingredientsRecyclerView;
    private TextView emptyView;
    
    public static RecipeIngredientsFragment newInstance(long recipeId) {
        RecipeIngredientsFragment fragment = new RecipeIngredientsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_RECIPE_ID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeId = getArguments().getLong(ARG_RECIPE_ID);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_ingredients, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inițializează views
        ingredientsRecyclerView = view.findViewById(R.id.ingredientsRecyclerView);
        emptyView = view.findViewById(R.id.emptyIngredientsView);
        
        // Configurează RecyclerView
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ingredientsRecyclerView.setHasFixedSize(true);
        
        // Încarcă ingredientele
        loadIngredients();
    }
    
    private void loadIngredients() {
        // TODO: În implementarea reală, datele ar fi încărcate dintr-o bază de date sau un API
        // Simulăm datele pentru demonstrație
        List<Ingredient> ingredients = new ArrayList<>();
        
        // Simulează pentru rețetă de sarmale
        ingredients.add(new Ingredient("Varză murată", "1 buc", "Legume"));
        ingredients.add(new Ingredient("Carne tocată (porc+vită)", "500g", "Carne"));
        ingredients.add(new Ingredient("Ceapă", "2 buc", "Legume"));
        ingredients.add(new Ingredient("Orez", "100g", "Cereale"));
        ingredients.add(new Ingredient("Roșii pasate", "200ml", "Legume"));
        ingredients.add(new Ingredient("Cimbru", "1 legătură", "Condimente"));
        ingredients.add(new Ingredient("Sare", "după gust", "Condimente"));
        ingredients.add(new Ingredient("Piper", "după gust", "Condimente"));
        ingredients.add(new Ingredient("Ulei", "3 linguri", "Grăsimi"));
        
        // Setează adapter
        if (ingredients.isEmpty()) {
            ingredientsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            IngredientsAdapter adapter = new IngredientsAdapter(ingredients);
            ingredientsRecyclerView.setAdapter(adapter);
            ingredientsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Model pentru ingrediente
     */
    private static class Ingredient {
        String name;
        String quantity;
        String category;
        
        Ingredient(String name, String quantity, String category) {
            this.name = name;
            this.quantity = quantity;
            this.category = category;
        }
    }
    
    /**
     * Adapter pentru lista de ingrediente
     */
    private class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {
        
        private final List<Ingredient> ingredients;
        
        IngredientsAdapter(List<Ingredient> ingredients) {
            this.ingredients = ingredients;
        }
        
        @NonNull
        @Override
        public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ingredient, parent, false);
            return new IngredientViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
            Ingredient ingredient = ingredients.get(position);
            holder.nameTextView.setText(ingredient.name);
            holder.quantityTextView.setText(ingredient.quantity);
            
            // Opțional: setează culoarea categoriei sau alte elemente vizuale
        }
        
        @Override
        public int getItemCount() {
            return ingredients.size();
        }
        
        class IngredientViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView quantityTextView;
            TextView categoryTextView;
            
            IngredientViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.ingredientNameTextView);
                quantityTextView = itemView.findViewById(R.id.ingredientQuantityTextView);
                categoryTextView = itemView.findViewById(R.id.ingredientCategoryTextView);
            }
        }
    }
} 