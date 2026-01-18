package com.example.music.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music.MainViewModel;
import com.example.music.R;
import com.example.music.ui.adapters.AlbumAdapter;
import com.example.music.ui.adapters.RecentlyPlayedAdapter;
import com.example.music.ui.adapters.TrackAdapter;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private MainViewModel mainViewModel;
    private TrackAdapter trackAdapter;
    private AlbumAdapter albumAdapter;
    private RecentlyPlayedAdapter recentAdapter;
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

        greetingText = view.findViewById(R.id.greeting_text);
        setGreeting();

        RecyclerView recyclerRecent = view.findViewById(R.id.recycler_recently_played);
        recentAdapter = new RecentlyPlayedAdapter();
        recyclerRecent.setAdapter(recentAdapter);

        recentAdapter.setOnItemClickListener(track -> {
            mainViewModel.playTrack(track);
        });

        RecyclerView recyclerTracks = view.findViewById(R.id.recycler_tracks);
        recyclerTracks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        trackAdapter = new TrackAdapter();
        recyclerTracks.setAdapter(trackAdapter);

        trackAdapter.setOnItemClickListener(track -> {
            if (homeViewModel.getTracks().getValue() != null) {
                mainViewModel.setPlaylist(homeViewModel.getTracks().getValue());
            }
            mainViewModel.playTrack(track);
        });

        RecyclerView recyclerAlbums = view.findViewById(R.id.recycler_albums);
        recyclerAlbums.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        albumAdapter = new AlbumAdapter();
        recyclerAlbums.setAdapter(albumAdapter);

        // Observe Data
        homeViewModel.getTracks().observe(getViewLifecycleOwner(), tracks -> {
            trackAdapter.setTracks(tracks);
            recentAdapter.setTracks(tracks); // Share same data for recently played for now
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
        if (hour < 12)
            greeting = "Good morning";
        else if (hour < 17)
            greeting = "Good afternoon";
        else
            greeting = "Good evening";
        if (greetingText != null)
            greetingText.setText(greeting);
    }
}
