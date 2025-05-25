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
 * Fragment pentru afișarea pașilor unei rețete
 */
public class RecipeStepsFragment extends Fragment {
    
    private static final String ARG_RECIPE_ID = "recipe_id";
    
    private long recipeId;
    private RecyclerView stepsRecyclerView;
    private TextView emptyView;
    
    public static RecipeStepsFragment newInstance(long recipeId) {
        RecipeStepsFragment fragment = new RecipeStepsFragment();
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
        return inflater.inflate(R.layout.fragment_recipe_steps, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inițializează views
        stepsRecyclerView = view.findViewById(R.id.stepsRecyclerView);
        emptyView = view.findViewById(R.id.emptyStepsView);
        
        // Configurează RecyclerView
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        stepsRecyclerView.setHasFixedSize(true);
        
        // Încarcă pașii rețetei
        loadSteps();
    }
    
    private void loadSteps() {
        // TODO: În implementarea reală, datele ar fi încărcate dintr-o bază de date sau un API
        // Simulăm datele pentru demonstrație
        List<Step> steps = new ArrayList<>();
        
        // Simulează pentru rețetă de sarmale
        steps.add(new Step(1, "Pregătirea verzei", 
                "Desprinde frunzele de varză murată cu atenție și taie nervurile groase pentru a le face mai flexibile. Clătește-le în apă rece dacă sunt prea sărate.", 
                5));
        steps.add(new Step(2, "Pregătirea umpluturii", 
                "Într-un bol mare, amestecă carnea tocată cu orezul spălat. Ceapa se taie mărunt și se călește în puțin ulei până devine translucidă, apoi se adaugă peste compoziția de carne. Adaugă sare, piper și cimbru după gust și amestecă bine.", 
                15));
        steps.add(new Step(3, "Formarea sarmalelor", 
                "Ia câte o frunză de varză și pune 1-2 linguri de umplutură în funcție de mărimea frunzei. Rulează frunza începând de la baza nervurii, îndoaie marginile laterale spre interior și continuă să rulezi până obții o sarma compactă.",
                30));
        steps.add(new Step(4, "Pregătirea oalei", 
                "Așterne câteva frunze de varză pe fundul unei oale sau cratiță. Aranjează sarmalele în straturi circulare, cu partea deschisă în jos pentru a nu se desface în timpul fierului.", 
                10));
        steps.add(new Step(5, "Fierberea sarmalelor", 
                "Adaugă apă cât să acopere sarmalele și toarnă roșiile pasate deasupra. Adaugă mai multe condimente și câteva frunze de dafin. Acoperă cu un capac și fierbe la foc mic aproximativ 2-3 ore.",
                180));
        steps.add(new Step(6, "Servirea", 
                "Servește sarmalele calde cu mămăligă și eventual smântână deasupra.",
                5));
        
        // Setează adapter
        if (steps.isEmpty()) {
            stepsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            StepsAdapter adapter = new StepsAdapter(steps);
            stepsRecyclerView.setAdapter(adapter);
            stepsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Model pentru pașii rețetei
     */
    private static class Step {
        int number;
        String title;
        String description;
        int duration; // în minute
        
        Step(int number, String title, String description, int duration) {
            this.number = number;
            this.title = title;
            this.description = description;
            this.duration = duration;
        }
    }
    
    /**
     * Adapter pentru pașii rețetei
     */
    private class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepViewHolder> {
        
        private final List<Step> steps;
        
        StepsAdapter(List<Step> steps) {
            this.steps = steps;
        }
        
        @NonNull
        @Override
        public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_step, parent, false);
            return new StepViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
            Step step = steps.get(position);
            holder.numberTextView.setText(String.valueOf(step.number));
            holder.titleTextView.setText(step.title);
            holder.descriptionTextView.setText(step.description);
            
            // Format durata în ore și minute dacă depășește 60 de minute
            String durationText;
            if (step.duration >= 60) {
                int hours = step.duration / 60;
                int minutes = step.duration % 60;
                if (minutes > 0) {
                    durationText = hours + " h " + minutes + " min";
                } else {
                    durationText = hours + " h";
                }
            } else {
                durationText = step.duration + " min";
            }
            holder.durationTextView.setText(durationText);
        }
        
        @Override
        public int getItemCount() {
            return steps.size();
        }
        
        class StepViewHolder extends RecyclerView.ViewHolder {
            TextView numberTextView;
            TextView titleTextView;
            TextView descriptionTextView;
            TextView durationTextView;
            
            StepViewHolder(@NonNull View itemView) {
                super(itemView);
                numberTextView = itemView.findViewById(R.id.stepNumberTextView);
                titleTextView = itemView.findViewById(R.id.stepTitleTextView);
                descriptionTextView = itemView.findViewById(R.id.stepDescriptionTextView);
                durationTextView = itemView.findViewById(R.id.stepDurationTextView);
            }
        }
    }
} 