package com.example.myapplication.dobrogeausage;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Collections;

public class DeltaQuizActivity extends AppCompatActivity {

    private ArrayList<Question> questions;
    private int currentQuestion = 0;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delta_quiz);

        initializeQuestions();
        displayQuestion();

        Button trueBtn = findViewById(R.id.trueButton);
        Button falseBtn = findViewById(R.id.falseButton);

        trueBtn.setOnClickListener(v -> checkAnswer(true));
        falseBtn.setOnClickListener(v -> checkAnswer(false));
    }

    private void initializeQuestions() {
        questions = new ArrayList<>();
        questions.add(new Question("Delta Dunării este cea mai mare deltă din Europa?", true));
        questions.add(new Question("Delta are peste 300 de specii de păsări?", true));
        questions.add(new Question("Este locul cu cea mai mare biodiversitate din România?", false));
        Collections.shuffle(questions);
    }

    private void displayQuestion() {
        TextView questionText = findViewById(R.id.questionText);
        questionText.setText(questions.get(currentQuestion).getQuestion());
    }

    private void checkAnswer(boolean userAnswer) {
        if(questions.get(currentQuestion).isAnswer() == userAnswer) {
            score++;
            // Show correct answer animation
        }

        if(currentQuestion < questions.size() - 1) {
            currentQuestion++;
            displayQuestion();
        } else {
            // Show final score
        }
    }

    class Question {
        private String question;
        private boolean answer;

        public Question(String question, boolean answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() { return question; }
        public boolean isAnswer() { return answer; }
    }
}
