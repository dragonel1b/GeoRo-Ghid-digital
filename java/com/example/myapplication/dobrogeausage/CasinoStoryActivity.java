package com.example.myapplication.dobrogeausage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.model.DobrogeaStoryNode;
import com.example.myapplication.model.DobrogeaGame;

public class CasinoStoryActivity extends AppCompatActivity {
    private TextView storyText;
    private TextView scoreText;
    private LinearLayout choicesContainer;
    private int currentStoryNode = 0;
    private int score = 0;
    private DobrogeaGame game;

    // Sample story nodes - should be replaced with actual story content
    private DobrogeaStoryNode[] storyNodes = {
            new DobrogeaStoryNode(0, "Welcome to the Casino!",
                    new String[]{"Play slots", "Play blackjack"},
                    new int[]{1, 2}, 0),
            new DobrogeaStoryNode(1, "You won 100 points at slots!",
                    new String[]{"Continue"},
                    new int[]{3}, 100),
            new DobrogeaStoryNode(2, "You lost 50 points at blackjack",
                    new String[]{"Try again", "Quit"},
                    new int[]{2, 3}, -50),
            new DobrogeaStoryNode(3, "Thanks for playing!",
                    new String[]{},
                    new int[]{}, 0)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casino_story);

        storyText = findViewById(R.id.storyText);
        scoreText = findViewById(R.id.scoreText);
        choicesContainer = findViewById(R.id.choicesContainer);
        game = new DobrogeaGame();

        updateStory();
    }

    private void updateStory() {
        DobrogeaStoryNode currentNode = storyNodes[currentStoryNode];
        storyText.setText(currentNode.getStoryText());
        score += currentNode.getPointsReward();
        scoreText.setText("Score: " + score);

        choicesContainer.removeAllViews();

        for (int i = 0; i < currentNode.getChoices().length; i++) {
            Button choiceButton = new Button(this);
            choiceButton.setText(currentNode.getChoices()[i]);
            final int choiceIndex = i;
            choiceButton.setOnClickListener(v -> {
                currentStoryNode = currentNode.getNextNodes()[choiceIndex];
                updateStory();
            });
            choicesContainer.addView(choiceButton);
        }
    }
}
