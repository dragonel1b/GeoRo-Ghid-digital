package com.example.myapplication.Joc1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Add import for RecipeDBHelper
import com.example.myapplication.Joc1.Culinary.RecipeDBHelper;

/**
 * Activity for viewing and adding recipe reviews and comments
 */
public class RecipeReviewActivity extends AppCompatActivity {
    private RecyclerView reviewsRecyclerView;
    private EditText reviewEditText;
    private RatingBar reviewRatingBar;
    private MaterialButton submitReviewButton;
    private TextView recipeTitle;
    private TextView noReviewsText;
    private MaterialCardView addReviewCard;
    
    private RecipeDBHelper dbHelper;
    private ReviewAdapter reviewAdapter;
    private String recipeTitleStr;
    private String recipeRegion;
    private long recipeId;
    private RomGameState gameState;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_review);
        
        // Initialize database helper
        dbHelper = new RecipeDBHelper(this);
        
        // Initialize game state
        gameState = RomGameState.getInstance();
        gameState.initialize(this);
        
        // Get recipe data from intent
        recipeTitleStr = getIntent().getStringExtra("recipe_title");
        recipeRegion = getIntent().getStringExtra("recipe_region");
        
        // Get recipe ID from database
        recipeId = dbHelper.getRecipeId(recipeTitleStr, recipeRegion);
        
        if (recipeId == -1) {
            Toast.makeText(this, "Nu s-a putut găsi rețeta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSubmitButton();
        loadReviews();
    }
    
    private void initializeViews() {
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewEditText = findViewById(R.id.reviewEditText);
        reviewRatingBar = findViewById(R.id.reviewRatingBar);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        recipeTitle = findViewById(R.id.recipeTitle);
        noReviewsText = findViewById(R.id.noReviewsText);
        addReviewCard = findViewById(R.id.addReviewCard);
        
        // Set recipe title
        recipeTitle.setText(recipeTitleStr);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recenzii și comentarii");
        }
    }
    
    private void setupRecyclerView() {
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }
    
    private void setupSubmitButton() {
        submitReviewButton.setOnClickListener(v -> {
            String reviewText = reviewEditText.getText().toString().trim();
            float rating = reviewRatingBar.getRating();
            
            if (TextUtils.isEmpty(reviewText)) {
                Toast.makeText(this, "Te rugăm să adaugi un comentariu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (rating < 1) {
                Toast.makeText(this, "Te rugăm să acordzi un rating", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get user name from preferences or use "Utilizator anonim"
            String userName = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getString("user_name", "Utilizator anonim");
            
            // Add review to database
            long reviewId = dbHelper.addReview(recipeId, userName, reviewText, rating);
            
            if (reviewId != -1) {
                // Clear input fields
                reviewEditText.setText("");
                reviewRatingBar.setRating(0);
                
                // Reload reviews
                loadReviews();
                
                // Track review for achievements
                gameState.writeReview(this);
                
                // Show success message
                Toast.makeText(this, "Recenzie adăugată cu succes!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Eroare la adăugarea recenziei", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadReviews() {
        List<RecipeDBHelper.Review> dbReviews = dbHelper.getReviewsForRecipe(recipeId);
        
        if (dbReviews.isEmpty()) {
            noReviewsText.setVisibility(View.VISIBLE);
            reviewsRecyclerView.setVisibility(View.GONE);
        } else {
            noReviewsText.setVisibility(View.GONE);
            reviewsRecyclerView.setVisibility(View.VISIBLE);
            
            // Convert from RecipeDBHelper.Review to RecipeReviewActivity.Review
            List<Review> reviews = new ArrayList<>();
            for (RecipeDBHelper.Review dbReview : dbReviews) {
                Review review = new Review(
                    dbReview.getId(),
                    dbReview.getRecipeId(),
                    dbReview.getUserName(),
                    dbReview.getRating(),
                    dbReview.getReviewText(),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(dbReview.getDate()))
                );
                reviews.add(review);
            }
            
            reviewAdapter.setReviews(reviews);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Inner class to replace RecipeDBHelper.Review references
    public static class Review {
        private long id;
        private long recipeId;
        private String userName;
        private float rating;
        private String comment;
        private String date;
        
        public Review(long id, long recipeId, String userName, float rating, String comment, String date) {
            this.id = id;
            this.recipeId = recipeId;
            this.userName = userName;
            this.rating = rating;
            this.comment = comment;
            this.date = date;
        }
        
        public long getId() {
            return id;
        }
        
        public long getRecipeId() {
            return recipeId;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public float getRating() {
            return rating;
        }
        
        public String getComment() {
            return comment;
        }
        
        public String getDate() {
            return date;
        }
    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        private List<Review> reviews;

        public void setReviews(List<Review> reviews) {
            this.reviews = reviews;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(reviews.get(position));
        }

        @Override
        public int getItemCount() {
            return reviews != null ? reviews.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textUsername;
            private RatingBar ratingBar;
            private TextView textComment;
            private TextView textDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textUsername = itemView.findViewById(R.id.usernameText);
                ratingBar = itemView.findViewById(R.id.rating_review);
                textComment = itemView.findViewById(R.id.text_review_content);
                textDate = itemView.findViewById(R.id.dateText);
            }

            public void bind(Review review) {
                textUsername.setText(review.getUserName());
                ratingBar.setRating(review.getRating());
                textComment.setText(review.getComment());
                textDate.setText(review.getDate());
            }
        }
    }
} 