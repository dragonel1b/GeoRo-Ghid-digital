package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private final String[] ingredients;
    private final boolean[] checkedState;

    public IngredientAdapter(String[] ingredients) {
        this.ingredients = ingredients;
        this.checkedState = new boolean[ingredients.length];
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
        String ingredient = ingredients[position];
        holder.ingredientText.setText(ingredient);
        holder.ingredientCheckbox.setChecked(checkedState[position]);
        
        // Toggle checked state when clicked
        holder.itemView.setOnClickListener(v -> {
            checkedState[position] = !checkedState[position];
            holder.ingredientCheckbox.setChecked(checkedState[position]);
        });
        
        holder.ingredientCheckbox.setOnClickListener(v -> {
            checkedState[position] = holder.ingredientCheckbox.isChecked();
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.length;
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        CheckBox ingredientCheckbox;
        TextView ingredientText;

        IngredientViewHolder(View itemView) {
            super(itemView);
            ingredientCheckbox = itemView.findViewById(R.id.ingredientCheckbox);
            ingredientText = itemView.findViewById(R.id.ingredientText);
        }
    }
} 