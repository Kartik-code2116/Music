package com.example.music.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music.R;
import com.example.music.data.repository.MusicRepository;
import com.example.music.data.api.RetrofitClient;
import com.example.music.MainViewModel;
import com.example.music.ui.adapters.TrackAdapter;
import com.example.music.ui.adapters.CategoryAdapter;

public class SearchFragment extends Fragment {

    private TrackAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private RecyclerView recyclerResults;
    private RecyclerView recyclerCategories;
    private View browseAllHeader;
    private MusicRepository musicRepository;
    private MainViewModel mainViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText searchBar = view.findViewById(R.id.search_bar);
        recyclerResults = view.findViewById(R.id.recycler_search_results);
        recyclerCategories = view.findViewById(R.id.recycler_categories);
        mainViewModel = new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);
        musicRepository = new MusicRepository(RetrofitClient.getApi());

        recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TrackAdapter();
        recyclerResults.setAdapter(adapter);

        // Categories Setup
        java.util.List<com.example.music.data.model.Category> categories = new java.util.ArrayList<>();
        categories.add(new com.example.music.data.model.Category("Pop", android.graphics.Color.parseColor("#E13300"))); // Orange-Red
        categories.add(new com.example.music.data.model.Category("Rock", android.graphics.Color.parseColor("#E91429"))); // Red
        categories.add(
                new com.example.music.data.model.Category("Hip-Hop", android.graphics.Color.parseColor("#BC5900"))); // Orange
        categories.add(
                new com.example.music.data.model.Category("Classical", android.graphics.Color.parseColor("#7D4B32"))); // Brown
        categories.add(new com.example.music.data.model.Category("Jazz", android.graphics.Color.parseColor("#7744FF"))); // Purple
        categories
                .add(new com.example.music.data.model.Category("Indie", android.graphics.Color.parseColor("#608108"))); // Green
        categories.add(new com.example.music.data.model.Category("R&B", android.graphics.Color.parseColor("#DC148C"))); // Pink
        categories
                .add(new com.example.music.data.model.Category("K-Pop", android.graphics.Color.parseColor("#148A08"))); // Green
        categories.add(
                new com.example.music.data.model.Category("Workout", android.graphics.Color.parseColor("#477D95"))); // Teal-Grey
        categories
                .add(new com.example.music.data.model.Category("Sleep", android.graphics.Color.parseColor("#1E3264"))); // Dark
                                                                                                                        // Blue
        categories
                .add(new com.example.music.data.model.Category("Party", android.graphics.Color.parseColor("#AF2896"))); // Purple-Pink
        categories
                .add(new com.example.music.data.model.Category("Focus", android.graphics.Color.parseColor("#503750"))); // Dark
                                                                                                                        // Purple

        categoryAdapter = new CategoryAdapter(categories);
        recyclerCategories.setAdapter(categoryAdapter);

        adapter.setOnItemClickListener(track -> {
            mainViewModel.playTrack(track);
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (browseAllHeader != null)
                        browseAllHeader.setVisibility(View.GONE);
                    recyclerCategories.setVisibility(View.GONE);
                    recyclerResults.setVisibility(View.VISIBLE);
                    if (s.length() > 2) {
                        performSearch(s.toString());
                    }
                } else {
                    if (browseAllHeader != null)
                        browseAllHeader.setVisibility(View.VISIBLE);
                    recyclerCategories.setVisibility(View.VISIBLE);
                    recyclerResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchBar.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (searchBar.getRight()
                        - searchBar.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    searchBar.setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void performSearch(String query) {
        musicRepository.searchTracks(query).observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                adapter.setTracks(tracks);
            }
        });
    }
}
