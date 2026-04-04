package com.example.myapplication.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.myapplication.core.domain.model.DobrogeaGame;
import java.util.ArrayList;
import java.util.List;

public class RegionViewModel extends ViewModel {
    private final MutableLiveData<Integer> totalPoints = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> showConfetti = new MutableLiveData<>(false);

    public LiveData<Integer> getTotalPoints() {
        return totalPoints;
    }

    public LiveData<Boolean> getShowConfetti() {
        return showConfetti;
    }

    public void addPoints(int points) {
        totalPoints.setValue((totalPoints.getValue() != null ? totalPoints.getValue() : 0) + points);
    }

    public void subtractPoints(int points) {
        totalPoints.setValue((totalPoints.getValue() != null ? totalPoints.getValue() : 0) - points);
    }

    public void setShowConfetti(boolean show) {
        showConfetti.setValue(show);
    }

    private final MutableLiveData<List<DobrogeaGame>> dobrogeaGames = new MutableLiveData<>();
    private final MutableLiveData<List<String>> dobrogeaStories = new MutableLiveData<>();

    public LiveData<List<DobrogeaGame>> getDobrogeaGames() {
        return dobrogeaGames;
    }

    public LiveData<List<String>> getDobrogeaStories() {
        return dobrogeaStories;
    }

    public void loadDobrogeaContent() {
        // Initialize games
        List<DobrogeaGame> games = new ArrayList<>();
        games.add(new DobrogeaGame("Delta Quiz", "Test your knowledge about Danube Delta", 20));
        games.add(new DobrogeaGame("Casino Story", "Experience historic Constanta casino", 15));
        dobrogeaGames.setValue(games);

        // Initialize stories
        List<String> stories = new ArrayList<>();
        stories.add("Dobrogea's Multicultural History");
        stories.add("The Ecosystem of Danube Delta");
        dobrogeaStories.setValue(stories);
    }
}
