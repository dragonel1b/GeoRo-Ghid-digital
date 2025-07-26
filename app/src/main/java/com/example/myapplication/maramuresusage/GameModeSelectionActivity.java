package com.example.myapplication.maramuresusage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.EnhancedQuestionModel;
import java.util.Arrays;
import java.util.List;

public class GameModeSelectionActivity extends AppCompatActivity implements GameModeAdapter.OnGameModeClickListener {
    private RecyclerView recyclerView;
    private GameModeAdapter adapter;
    private List<GameModeManager.GameMode> gameModes;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode_selection);
        recyclerView = findViewById(R.id.recyclerViewGameModes);
        gameModes = Arrays.asList(GameModeManager.GameMode.values());
        adapter = new GameModeAdapter(gameModes, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    @Override
    public void onGameModeClick(GameModeManager.GameMode gameMode) {
        Intent intent = new Intent(this, MaramuresGameActivity.class);
        intent.putExtra("selected_game_mode", gameMode.name());
        // Pentru CATEGORY_FOCUS, poți adăuga un selector de categorie suplimentar aici
        startActivity(intent);
        Toast.makeText(this, "Mod selectat: " + gameMode.displayName, Toast.LENGTH_SHORT).show();
    }
} 