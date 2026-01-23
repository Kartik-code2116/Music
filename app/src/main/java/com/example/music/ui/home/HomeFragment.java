package com.example.music.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music.MainViewModel;
import com.example.music.R;
import com.example.music.ui.adapters.AlbumAdapter;
import com.example.music.ui.adapters.HomeGridAdapter;
import com.example.music.ui.adapters.TrackAdapter;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private MainViewModel mainViewModel;
    private TrackAdapter trackAdapter;
    private AlbumAdapter albumAdapter;
    private HomeGridAdapter gridAdapter; // New adapter for the top grid
    private TextView greetingText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        greetingText = view.findViewById(R.id.text_greeting);
        setGreeting();

        // 1. Top Grid (Quick Access / Recent)
        RecyclerView recyclerGrid = view.findViewById(R.id.recycler_view_grid);
        recyclerGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // Using existing generic logic for now, ideally this would be a specific
        // "mixes" list
        // Initialize adapter with empty list initially
        // Note: We need a way to set data on HomeGridAdapter later.
        // Let's modify HomeGridAdapter to have a setTracks method or pass it in
        // constructor.
        // For now, assuming we will fix Adapter to have a setter or recreated it.

        // 2. New Releases (Horizontal)
        RecyclerView recyclerNewReleases = view.findViewById(R.id.recycler_view_new_releases);
        recyclerNewReleases
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        trackAdapter = new TrackAdapter();
        recyclerNewReleases.setAdapter(trackAdapter);

        trackAdapter.setOnItemClickListener(track -> {
            if (homeViewModel.getTracks().getValue() != null) {
                mainViewModel.setPlaylist(homeViewModel.getTracks().getValue());
            }
            mainViewModel.playTrack(track);
        });

        // 3. Top Mixes (Horizontal)
        RecyclerView recyclerTopMixes = view.findViewById(R.id.recycler_view_top_mixes);
        recyclerTopMixes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        albumAdapter = new AlbumAdapter();
        recyclerTopMixes.setAdapter(albumAdapter);

        // Observe Data
        homeViewModel.getTracks().observe(getViewLifecycleOwner(), tracks -> {
            trackAdapter.setTracks(tracks);

            // Populate the grid with the first 6 items or so
            if (tracks != null && !tracks.isEmpty()) {
                int limit = Math.min(tracks.size(), 6);
                gridAdapter = new HomeGridAdapter(tracks.subList(0, limit), track -> {
                    mainViewModel.playTrack(track);
                });
                recyclerGrid.setAdapter(gridAdapter);
            }
        });

        homeViewModel.getAlbums().observe(getViewLifecycleOwner(), albums -> {
            albumAdapter.setAlbums(albums);
        });

        // Fetch Data
        homeViewModel.fetchCharts();
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12)
            greeting = "Good morning";
        else if (hour >= 12 && hour < 17)
            greeting = "Good afternoon";
        else
            greeting = "Good evening";

        if (greetingText != null)
            greetingText.setText(greeting);
    }
}
