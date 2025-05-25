package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.recipe.model.Ingredient;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private final Context context;
    private final List<Ingredient> ingredients;

    public IngredientAdapter(Context context, List<Ingredient> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox ingredientCheckbox;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientCheckbox = itemView.findViewById(R.id.ingredientCheckbox);
        }

        public void bind(Ingredient ingredient) {
            ingredientCheckbox.setText(ingredient.getFullDescription());
            ingredientCheckbox.setChecked(ingredient.isChecked());
            
            ingredientCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ingredient.setChecked(isChecked);
            });
        }
    }
} 