package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.recipe.model.Ingredient;

import java.util.List;

public class EditableIngredientAdapter extends RecyclerView.Adapter<EditableIngredientAdapter.IngredientViewHolder> {

    private final Context context;
    private final List<Ingredient> ingredients;

    public EditableIngredientAdapter(Context context, List<Ingredient> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_editable_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        
        // Format ingredient text
        String formattedText;
        if (ingredient.getUnit() != null && !ingredient.getUnit().isEmpty()) {
            formattedText = String.format("%s (%.1f %s)", 
                    ingredient.getName(), 
                    ingredient.getQuantity(), 
                    ingredient.getUnit());
        } else {
            formattedText = String.format("%s (%.1f)", 
                    ingredient.getName(), 
                    ingredient.getQuantity());
        }
        
        holder.ingredientText.setText(formattedText);
        
        // Setup delete button
        holder.deleteButton.setOnClickListener(v -> {
            ingredients.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, ingredients.size());
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientText;
        ImageButton deleteButton;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientText = itemView.findViewById(R.id.ingredient_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
} 