package com.example.myapplication.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CityViewModel extends ViewModel {
    private final MutableLiveData<Integer> totalPoints = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> showConfetti = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> visitedCities = new MutableLiveData<>(0);

    public LiveData<Integer> getTotalPoints() {
        return totalPoints;
    }

    public String getTotalPointsWithEmoji() {
        return "💼 " + (totalPoints.getValue() != null ? totalPoints.getValue() : 0);
    }

    public LiveData<Boolean> getShowConfetti() {
        return showConfetti;
    }

    public LiveData<Integer> getVisitedCities() {
        return visitedCities;
    }

    public void incrementVisitedCities() {
        Integer currentCount = visitedCities.getValue();
        if (currentCount != null) {
            visitedCities.setValue(currentCount + 1);
            // Show confetti animation when a city is visited
            setShowConfetti(true);
        }
    }

    public void addPoints(int points) {
        Integer currentPoints = totalPoints.getValue();
        if (currentPoints != null) {
            totalPoints.setValue(currentPoints + points);
        }
    }

    public void subtractPoints(int points) {
        Integer currentPoints = totalPoints.getValue();
        if (currentPoints != null) {
            totalPoints.setValue(Math.max(0, currentPoints - points));
        }
    }

    public void setShowConfetti(boolean show) {
        showConfetti.setValue(show);
        if (show) {
            // Auto-hide confetti after a delay
            new android.os.Handler().postDelayed(() -> showConfetti.setValue(false), 2000);
        }
    }
}
