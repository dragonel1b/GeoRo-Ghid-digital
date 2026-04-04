package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.core.domain.model.FirestoreQuestionModel;
import com.google.android.material.button.MaterialButton;
import java.util.List;

/**
 * Adapter pentru afișarea întrebărilor în RecyclerView
 */
public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    
    private final List<FirestoreQuestionModel> questions;
    private final OnQuestionClickListener editListener;
    private final OnQuestionClickListener deleteListener;
    
    /**
     * Interfață pentru gestionarea click-urilor pe întrebări
     */
    public interface OnQuestionClickListener {
        void onQuestionClick(FirestoreQuestionModel question);
    }
    
    /**
     * Constructor pentru adapter
     * @param questions Lista de întrebări
     * @param editListener Listener pentru butonul de editare
     * @param deleteListener Listener pentru butonul de ștergere
     */
    public QuestionAdapter(List<FirestoreQuestionModel> questions, 
                           OnQuestionClickListener editListener, 
                           OnQuestionClickListener deleteListener) {
        this.questions = questions;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        FirestoreQuestionModel question = questions.get(position);
        holder.bind(question);
    }
    
    @Override
    public int getItemCount() {
        return questions.size();
    }
    
    /**
     * ViewHolder pentru întrebări
     */
    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView questionTextView;
        private final TextView correctAnswerTextView;
        private final MaterialButton editButton;
        private final MaterialButton deleteButton;
        
        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            correctAnswerTextView = itemView.findViewById(R.id.correctAnswerTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
        
        /**
         * Leagă datele întrebării de ViewHolder
         * @param question Întrebarea de afișat
         */
        public void bind(FirestoreQuestionModel question) {
            questionTextView.setText(question.getQuestion());
            correctAnswerTextView.setText("Răspuns corect: " + question.getCorrectAnswer());
            
            editButton.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onQuestionClick(question);
                }
            });
            
            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onQuestionClick(question);
                }
            });
        }
    }
} 