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
 * Fragment pentru afișarea planului de masă pentru o zi specifică
 */
public class DayPlanFragment extends Fragment {

    private static final String ARG_DAY_INDEX = "day_index";
    private static final String ARG_DAY_NAME = "day_name";

    private int dayIndex;
    private String dayName;
    private RecyclerView mealsRecyclerView;
    private TextView emptyDayView;

    public static DayPlanFragment newInstance(int dayIndex, String dayName) {
        DayPlanFragment fragment = new DayPlanFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY_INDEX, dayIndex);
        args.putString(ARG_DAY_NAME, dayName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayIndex = getArguments().getInt(ARG_DAY_INDEX);
            dayName = getArguments().getString(ARG_DAY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mealsRecyclerView = view.findViewById(R.id.dayMealsRecyclerView);
        emptyDayView = view.findViewById(R.id.emptyDayView);
        
        mealsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mealsRecyclerView.setNestedScrollingEnabled(false);
        
        // Încărcăm mesele pentru ziua curentă
        loadMeals();
    }
    
    private void loadMeals() {
        // TODO: În implementarea reală, datele ar fi încărcate din baza de date
        List<MealItem> meals = new ArrayList<>();
        
        // Simulăm diverse planuri de masă în funcție de ziua săptămânii
        switch(dayIndex) {
            case 0: // Luni
                meals.add(new MealItem("Mic dejun", "Iaurt cu fructe și granola", "07:30", 280));
                meals.add(new MealItem("Prânz", "Salată de quinoa cu legume", "13:00", 350));
                meals.add(new MealItem("Cină", "Piept de pui la grătar cu legume", "19:00", 420));
                break;
            case 1: // Marți
                meals.add(new MealItem("Mic dejun", "Ouă poșate pe toast integral", "07:30", 320));
                meals.add(new MealItem("Prânz", "Supă cremă de legume", "13:00", 280));
                meals.add(new MealItem("Gustare", "Fructe proaspete", "16:00", 120));
                meals.add(new MealItem("Cină", "Somon la cuptor cu cartofi dulci", "19:00", 450));
                break;
            case 2: // Miercuri
                meals.add(new MealItem("Mic dejun", "Smoothie proteic", "07:30", 300));
                meals.add(new MealItem("Prânz", "Bowl cu orez, avocado și ton", "13:00", 420));
                meals.add(new MealItem("Cină", "Tocăniță de legume", "19:00", 380));
                break;
            case 3: // Joi
                meals.add(new MealItem("Brunch", "Omletă spaniolă", "10:00", 450));
                meals.add(new MealItem("Gustare", "Nuci și semințe", "15:00", 200));
                meals.add(new MealItem("Cină", "Paste integrale cu sos de roșii", "19:00", 520));
                break;
            case 4: // Vineri
                meals.add(new MealItem("Mic dejun", "Terci de ovăz cu miere și fructe", "07:30", 320));
                meals.add(new MealItem("Prânz", "Wrap cu humus și legume", "13:00", 380));
                meals.add(new MealItem("Cină", "Pizza de casă cu blat integral", "19:30", 580));
                break;
            case 5: // Sâmbătă
                meals.add(new MealItem("Mic dejun tardiv", "Clătite cu sirop de arțar", "09:30", 420));
                meals.add(new MealItem("Prânz", "Burger vegetal cu cartofi la cuptor", "14:00", 550));
                meals.add(new MealItem("Desert", "Înghețată de fructe", "15:30", 180));
                meals.add(new MealItem("Cină", "Risotto cu ciuperci", "20:00", 480));
                break;
            case 6: // Duminică
                meals.add(new MealItem("Brunch", "Ouă Benedict", "10:30", 480));
                meals.add(new MealItem("Gustare", "Fructe și brânză", "15:00", 220));
                meals.add(new MealItem("Cină festivă", "Friptură de vită cu legume la grătar", "19:00", 650));
                meals.add(new MealItem("Desert", "Tort de ciocolată", "21:00", 320));
                break;
            default:
                // Nu avem mese
                break;
        }
        
        // Verificăm dacă avem mese de afișat
        if (meals.isEmpty()) {
            mealsRecyclerView.setVisibility(View.GONE);
            emptyDayView.setVisibility(View.VISIBLE);
        } else {
            MealAdapter adapter = new MealAdapter(meals);
            mealsRecyclerView.setAdapter(adapter);
            mealsRecyclerView.setVisibility(View.VISIBLE);
            emptyDayView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Clasa pentru elementele din lista de mese
     */
    private static class MealItem {
        String type;
        String description;
        String time;
        int calories;
        
        MealItem(String type, String description, String time, int calories) {
            this.type = type;
            this.description = description;
            this.time = time;
            this.calories = calories;
        }
    }
    
    /**
     * Adapter pentru lista de mese
     */
    private class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
        private List<MealItem> mealItems;
        
        MealAdapter(List<MealItem> mealItems) {
            this.mealItems = mealItems;
        }
        
        @NonNull
        @Override
        public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_plan, parent, false);
            return new MealViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
            MealItem item = mealItems.get(position);
            
            // TODO: În implementarea completă, acest cod ar face binding la viewHolder
            
            // Setăm listener pentru click (pentru detalii sau editare)
            holder.itemView.setOnClickListener(v -> {
                // TODO: Implementare acțiuni la click (ex: editare sau afișare detalii)
            });
        }
        
        @Override
        public int getItemCount() {
            return mealItems.size();
        }
        
        class MealViewHolder extends RecyclerView.ViewHolder {
            // TODO: Declarare referințe la elementele din layout
            
            MealViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: Inițializare elemente UI din layout
            }
        }
    }
} 