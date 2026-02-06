package com.example.music.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music.MainViewModel;
import com.example.music.R;
import com.example.music.data.LocalMusicLoader;
import com.example.music.data.model.Track;
import com.example.music.ui.adapters.LibraryAdapter;
import com.example.music.ui.home.HomeViewModel;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import java.util.List;

public class LibraryFragment extends Fragment {

    private LibraryAdapter adapter;
    private HomeViewModel homeViewModel;
    private MainViewModel mainViewModel;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadLocalMusic();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_library);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LibraryAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(track -> {
            mainViewModel.playTrack(track);
        });

        view.findViewById(R.id.btn_cloud).setOnClickListener(v -> {
            mainViewModel.loadCloudTracks();
        });

        mainViewModel.getCloudTracks().observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null && !tracks.isEmpty()) {
                adapter.setItems(tracks);
                mainViewModel.setPlaylist(tracks);
            }
        });

        checkPermissionsAndLoad();
    }

    private void checkPermissionsAndLoad() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadLocalMusic();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void loadLocalMusic() {
        List<Track> localTracks = LocalMusicLoader.loadLocalTracks(requireContext());
        if (!localTracks.isEmpty()) {
            adapter.setItems(localTracks);
            mainViewModel.setPlaylist(localTracks);
        } else {
            // Fallback to charts if no local music found
            homeViewModel.getTracks().observe(getViewLifecycleOwner(), tracks -> {
                adapter.setItems(tracks);
            });
            homeViewModel.fetchCharts();
        }
    }
}
