package com.example.music.ui.player;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.music.MainViewModel;
import com.example.music.R;
import androidx.palette.graphics.Palette;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.music.data.model.Track;

import java.util.Locale;

public class PlayerFragment extends Fragment {

    private MainViewModel mainViewModel;
    private ImageView btnClose, albumArt, btnLike, btnShuffle, btnPrevious, btnPlayPause, btnNext, btnRepeat;
    private TextView songTitle, artistName, currentTime, totalTime;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private boolean isUserSeeking = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        initViews(view);
        setupObservers();
        setupClickListeners();
    }

    private void initViews(View view) {
        btnClose = view.findViewById(R.id.btn_close);
        albumArt = view.findViewById(R.id.player_album_art);
        btnLike = view.findViewById(R.id.btn_like);
        btnShuffle = view.findViewById(R.id.btn_shuffle);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnNext = view.findViewById(R.id.btn_next);
        btnRepeat = view.findViewById(R.id.btn_repeat);
        songTitle = view.findViewById(R.id.player_song_title);
        artistName = view.findViewById(R.id.player_artist_name);
        currentTime = view.findViewById(R.id.tv_current_time);
        totalTime = view.findViewById(R.id.tv_total_time);
        seekBar = view.findViewById(R.id.player_seekbar);
    }

    private void setupObservers() {
        mainViewModel.getCurrentTrack().observe(getViewLifecycleOwner(), track -> {
            if (track != null) {
                songTitle.setText(track.getTitle());
                artistName.setText(track.getArtist().getName());
                Glide.with(this)
                        .asBitmap()
                        .load(track.getAlbum().getCoverBig())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource,
                                    @Nullable Transition<? super Bitmap> transition) {
                                albumArt.setImageBitmap(resource);
                                updateBackground(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                            }
                        });
            }
        });

        mainViewModel.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        });

        mainViewModel.getDuration().observe(getViewLifecycleOwner(), duration -> {
            seekBar.setMax(duration);
            totalTime.setText(formatTime(duration));
        });

        mainViewModel.getCurrentPosition().observe(getViewLifecycleOwner(), position -> {
            if (!isUserSeeking) {
                seekBar.setProgress(position);
                currentTime.setText(formatTime(position));
            }
        });

        mainViewModel.getIsShuffle().observe(getViewLifecycleOwner(), isShuffle -> {
            btnShuffle.setColorFilter(
                    getResources().getColor(isShuffle ? R.color.spotify_green : R.color.spotify_grey, null));
        });

        mainViewModel.getIsRepeat().observe(getViewLifecycleOwner(), isRepeat -> {
            btnRepeat.setColorFilter(
                    getResources().getColor(isRepeat ? R.color.spotify_green : R.color.spotify_grey, null));
        });

        mainViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), favoriteIds -> {
            Track track = mainViewModel.getCurrentTrack().getValue();
            if (track != null) {
                boolean isFav = favoriteIds.contains(track.getId());
                btnLike.setImageResource(isFav ? R.drawable.ic_play_arrow : R.drawable.ic_favorite_border);
                btnLike.setColorFilter(getResources().getColor(isFav ? R.color.spotify_green : R.color.white, null));
            }
        });
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> requireActivity().onBackPressed());

        btnPlayPause.setOnClickListener(v -> {
            Boolean playing = mainViewModel.getIsPlaying().getValue();
            mainViewModel.setPlaying(playing == null || !playing);
        });

        btnNext.setOnClickListener(v -> mainViewModel.nextTrack());
        btnPrevious.setOnClickListener(v -> mainViewModel.previousTrack());
        btnShuffle.setOnClickListener(v -> mainViewModel.toggleShuffle());
        btnRepeat.setOnClickListener(v -> mainViewModel.toggleRepeat());
        btnLike.setOnClickListener(v -> {
            Track track = mainViewModel.getCurrentTrack().getValue();
            if (track != null) {
                mainViewModel.toggleFavorite(track.getId());
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                mainViewModel.setCurrentPosition(seekBar.getProgress());
            }
        });
    }

    private void updateBackground(Bitmap bitmap) {
        Palette.from(bitmap).generate(palette -> {
            if (palette != null) {
                int dominantColor = palette.getDominantColor(getResources().getColor(R.color.spotify_black, null));

                // Create a beautiful vertical gradient from dominant color to black
                GradientDrawable gradient = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] { dominantColor, getResources().getColor(R.color.spotify_black, null) });

                View dynamicBg = getView().findViewById(R.id.dynamic_background);
                if (dynamicBg != null) {
                    dynamicBg.setBackground(gradient);
                }

                // Update accent color in ViewModel for other UI components to use
                mainViewModel.setAccentColor(dominantColor);
            }
        });
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
}
