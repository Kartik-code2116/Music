package com.example.music;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import com.example.music.service.PlaybackService;
import android.content.ComponentName;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.music.data.model.Track;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private MediaController mediaController;
    private ListenableFuture<MediaController> controllerFuture;
    private View miniPlayerContainer;
    private ImageView miniPlayerImage;
    private TextView miniPlayerTitle, miniPlayerArtist;
    private ImageView miniPlayerPlayPause;
    private android.widget.ProgressBar miniPlayerProgress;
    private NavController navController;

    private final Handler progressHandler = new Handler();
    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaController != null && mediaController.isPlaying()) {
                mainViewModel.setCurrentPosition((int) mediaController.getCurrentPosition());
                if (miniPlayerProgress != null) {
                    miniPlayerProgress.setProgress((int) mediaController.getCurrentPosition());
                }
            }
            progressHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        miniPlayerContainer = findViewById(R.id.mini_player_container);
        miniPlayerImage = findViewById(R.id.mini_player_image);
        miniPlayerTitle = findViewById(R.id.mini_player_title);
        miniPlayerArtist = findViewById(R.id.mini_player_artist);
        miniPlayerPlayPause = findViewById(R.id.mini_player_play_pause);
        miniPlayerProgress = findViewById(R.id.mini_player_progress);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        if (navView == null)
            return; // Prevent crash if layout inflation failed

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment == null)
            return;

        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navView, navController);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initializeController();
        setupObservers();
        setupListeners();
    }

    private void initializeController() {
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, PlaybackService.class));
        controllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                setupControllerObservers();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, MoreExecutors.directExecutor());
    }

    private void setupControllerObservers() {
        mediaController.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                mainViewModel.setPlaying(isPlaying);
            }

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                // Handle transitions if needed
            }
        });
    }

    private void setupObservers() {
        mainViewModel.getCurrentTrack().observe(this, this::prepareTrack);

        mainViewModel.getIsPlaying().observe(this, isPlaying -> {
            if (mediaController != null) {
                if (isPlaying && !mediaController.isPlaying()) {
                    mediaController.play();
                    progressHandler.post(updateProgressRunnable);
                } else if (!isPlaying && mediaController.isPlaying()) {
                    mediaController.pause();
                    progressHandler.removeCallbacks(updateProgressRunnable);
                }
            }
            if (miniPlayerPlayPause != null) {
                miniPlayerPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
            }
        });

        mainViewModel.getCurrentPosition().observe(this, position -> {
            if (mediaController != null && Math.abs(mediaController.getCurrentPosition() - position) > 2000) {
                mediaController.seekTo(position);
            }
        });

        mainViewModel.getAccentColor().observe(this, color -> {
            if (miniPlayerProgress != null) {
                miniPlayerProgress.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        });
    }

    private void setupListeners() {
        miniPlayerPlayPause.setOnClickListener(v -> {
            Boolean currentlyPlaying = mainViewModel.getIsPlaying().getValue();
            mainViewModel.setPlaying(currentlyPlaying == null || !currentlyPlaying);
        });

        miniPlayerContainer.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_player);
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_player) {
                findViewById(R.id.nav_view).setVisibility(View.GONE);
                miniPlayerContainer.setVisibility(View.GONE);
            } else {
                findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
                if (mainViewModel.getCurrentTrack().getValue() != null) {
                    if (miniPlayerContainer.getVisibility() != View.VISIBLE) {
                        miniPlayerContainer.setVisibility(View.VISIBLE);
                        miniPlayerContainer.setAlpha(0f);
                        miniPlayerContainer.animate().alpha(1f).setDuration(300).start();
                    }
                }
            }
        });
    }

    private void prepareTrack(Track track) {
        if (track == null || mediaController == null)
            return;

        miniPlayerContainer.setVisibility(View.VISIBLE);
        miniPlayerTitle.setText(track.getTitle());
        miniPlayerArtist.setText(track.getArtist().getName());
        Glide.with(this).load(track.getAlbum().getCoverMedium()).into(miniPlayerImage);

        MediaItem mediaItem = new MediaItem.Builder()
                .setMediaId(String.valueOf(track.getId()))
                .setUri(track.getPreview())
                .build();

        mediaController.setMediaItem(mediaItem);
        mediaController.prepare();
        mediaController.play();

        mainViewModel.setDuration((int) mediaController.getDuration());
        if (miniPlayerProgress != null) {
            miniPlayerProgress.setMax((int) mediaController.getDuration());
        }
        mainViewModel.setPlaying(true);
        progressHandler.post(updateProgressRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressHandler.removeCallbacks(updateProgressRunnable);
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
        }
    }
}
