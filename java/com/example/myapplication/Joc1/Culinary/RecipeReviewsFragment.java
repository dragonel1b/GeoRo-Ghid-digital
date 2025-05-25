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
 * Fragment to display recipe reviews
 */
public class RecipeReviewsFragment extends Fragment {

    private static final String ARG_RECIPE_ID = "recipe_id";
    
    private long recipeId;
    private RecyclerView reviewsRecyclerView;
    private TextView noReviewsTextView;
    private RecipeDBHelper dbHelper;
    
    public static RecipeReviewsFragment newInstance(long recipeId) {
        RecipeReviewsFragment fragment = new RecipeReviewsFragment();
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
        dbHelper = new RecipeDBHelper(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_reviews, container, false);
        
        reviewsRecyclerView = view.findViewById(R.id.reviewsRecyclerView);
        noReviewsTextView = view.findViewById(R.id.noReviewsTextView);
        
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        loadReviews();
        
        return view;
    }
    
    private void loadReviews() {
        // In a real app, get data from database
        List<RecipeDBHelper.Review> reviews = dbHelper.getReviewsForRecipe(recipeId);
        
        if (reviews.isEmpty()) {
            reviewsRecyclerView.setVisibility(View.GONE);
            noReviewsTextView.setVisibility(View.VISIBLE);
        } else {
            reviewsRecyclerView.setVisibility(View.VISIBLE);
            noReviewsTextView.setVisibility(View.GONE);
            reviewsRecyclerView.setAdapter(new ReviewsAdapter(reviews));
        }
    }
    
    private static class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        private final List<RecipeDBHelper.Review> reviews;
        
        ReviewsAdapter(List<RecipeDBHelper.Review> reviews) {
            this.reviews = reviews;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecipeDBHelper.Review review = reviews.get(position);
            holder.usernameTextView.setText(review.getUserName());
            holder.ratingBar.setRating(review.getRating());
            holder.commentTextView.setText(review.getReviewText());
            // Convert long date to String
            holder.dateTextView.setText(new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date(review.getDate())));
        }
        
        @Override
        public int getItemCount() {
            return reviews.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView usernameTextView;
            final android.widget.RatingBar ratingBar;
            final TextView commentTextView;
            final TextView dateTextView;
            
            ViewHolder(View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.usernameText);
                ratingBar = itemView.findViewById(R.id.rating_review);
                commentTextView = itemView.findViewById(R.id.text_review_content);
                dateTextView = itemView.findViewById(R.id.dateText);
            }
        }
    }
} 