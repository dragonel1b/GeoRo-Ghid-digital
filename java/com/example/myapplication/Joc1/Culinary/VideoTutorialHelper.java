package com.example.myapplication.Joc1.Culinary;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;

/**
 * Helper pentru gestionarea tutorialelor video în rețetă
 */
public class VideoTutorialHelper {

    private final Context context;
    private final FragmentManager fragmentManager;
    
    public VideoTutorialHelper(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
    }
    
    /**
     * Deschide un tutorial video într-un BottomSheetDialogFragment
     */
    public void showVideoTutorial(String videoUrl, String title, String description) {
        VideoTutorialDialog dialog = VideoTutorialDialog.newInstance(videoUrl, title, description);
        dialog.show(fragmentManager, "video_tutorial");
    }
    
    /**
     * BottomSheetDialogFragment pentru afișarea tutorialelor video
     */
    public static class VideoTutorialDialog extends BottomSheetDialogFragment {
        
        private static final String ARG_VIDEO_URL = "video_url";
        private static final String ARG_TITLE = "title";
        private static final String ARG_DESCRIPTION = "description";
        
        private ExoPlayer player;
        private PlayerView playerView;
        private String videoUrl;
        private String videoTitle;
        private String videoDescription;
        
        public static VideoTutorialDialog newInstance(String videoUrl, String title, String description) {
            VideoTutorialDialog fragment = new VideoTutorialDialog();
            Bundle args = new Bundle();
            args.putString(ARG_VIDEO_URL, videoUrl);
            args.putString(ARG_TITLE, title);
            args.putString(ARG_DESCRIPTION, description);
            fragment.setArguments(args);
            return fragment;
        }
        
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                videoUrl = getArguments().getString(ARG_VIDEO_URL);
                videoTitle = getArguments().getString(ARG_TITLE);
                videoDescription = getArguments().getString(ARG_DESCRIPTION);
            }
        }
        
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_video_tutorial, container, false);
        }
        
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            
            // Inițializare UI
            playerView = view.findViewById(R.id.playerView);
            TextView titleTextView = view.findViewById(R.id.videoTitleTextView);
            TextView descriptionTextView = view.findViewById(R.id.videoDescriptionTextView);
            MaterialButton closeButton = view.findViewById(R.id.closeVideoButton);
            
            // Setare texte
            titleTextView.setText(videoTitle);
            descriptionTextView.setText(videoDescription);
            
            // Configurare buton închidere
            closeButton.setOnClickListener(v -> dismiss());
            
            // Inițializare și configurare player
            initializePlayer();
        }
        
        private void initializePlayer() {
            if (getContext() == null) return;
            
            player = new SimpleExoPlayer.Builder(getContext()).build();
            playerView.setPlayer(player);
            
            // Adaugă URL-ul video
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            player.setMediaItem(mediaItem);
            
            // Opțiuni player
            player.setPlayWhenReady(true);
            player.prepare();
            
            // Configurare listener pentru erori sau finalizare
            player.addListener(new Player.Listener() {
                public void onPlayerError(com.google.android.exoplayer2.ExoPlaybackException error) {
                    // Gestionare eroare
                }
                
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_ENDED) {
                        // Video terminat
                    }
                }
            });
        }
        
        @Override
        public void onPause() {
            super.onPause();
            if (player != null) {
                player.pause();
            }
        }
        
        @Override
        public void onDestroy() {
            super.onDestroy();
            releasePlayer();
        }
        
        private void releasePlayer() {
            if (player != null) {
                player.release();
                player = null;
            }
        }
    }
} 