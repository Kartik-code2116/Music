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
import com.example.music.data.api.RetrofitClient;
import com.example.music.data.model.DataResponse;
import com.example.music.data.model.Track;
import com.example.music.ui.adapters.CategoryAdapter;
import com.example.music.ui.adapters.TrackAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private TrackAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private RecyclerView recyclerResults;
    private RecyclerView recyclerCategories;
    private View browseAllHeader;

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
        browseAllHeader = view.findViewById(R.id.browse_all_header); // Needs ID in XML or just manage via parent

        com.example.music.MainViewModel mainViewModel = new ViewModelProvider(requireActivity())
                .get(com.example.music.MainViewModel.class);

        recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TrackAdapter();
        recyclerResults.setAdapter(adapter);

        // Categories Setup
        java.util.List<com.example.music.data.model.Category> categories = new java.util.ArrayList<>();
        categories.add(new com.example.music.data.model.Category("Pop", android.graphics.Color.parseColor("#E13300")));
        categories.add(new com.example.music.data.model.Category("Rock", android.graphics.Color.parseColor("#1E3264")));
        categories.add(
                new com.example.music.data.model.Category("Hip-Hop", android.graphics.Color.parseColor("#BC5900")));
        categories.add(
                new com.example.music.data.model.Category("Classical", android.graphics.Color.parseColor("#7D4B32")));
        categories.add(new com.example.music.data.model.Category("Jazz", android.graphics.Color.parseColor("#7744FF")));
        categories
                .add(new com.example.music.data.model.Category("Indie", android.graphics.Color.parseColor("#608108")));

        categoryAdapter = new com.example.music.ui.adapters.CategoryAdapter(categories);
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
    }

    private void performSearch(String query) {
        RetrofitClient.getApi().searchTracks(query).enqueue(new Callback<DataResponse<Track>>() {
            @Override
            public void onResponse(Call<DataResponse<Track>> call, Response<DataResponse<Track>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setTracks(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<DataResponse<Track>> call, Throwable t) {
                // Handle error
            }
        });
    }
}
