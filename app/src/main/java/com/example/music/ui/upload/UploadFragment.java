package com.example.music.ui.upload;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.R;
import com.example.music.data.model.Track;
import com.example.music.data.repository.CommunityRepository;
import com.example.music.ui.adapters.TrackAdapter;
import com.google.android.material.textfield.TextInputEditText;

public class UploadFragment extends Fragment {

    private TextInputEditText editTitle;
    private TextInputEditText editArtist;
    private Button btnSelectFile;
    private Button btnUpload;
    private RecyclerView recyclerCommunity;
    private TrackAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTitle = view.findViewById(R.id.edit_song_title);
        editArtist = view.findViewById(R.id.edit_artist_name);
        btnSelectFile = view.findViewById(R.id.btn_select_file);
        btnUpload = view.findViewById(R.id.btn_upload);
        recyclerCommunity = view.findViewById(R.id.recycler_community_uploads);

        // Setup RecyclerView
        recyclerCommunity.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TrackAdapter();
        recyclerCommunity.setAdapter(adapter);

        loadCommunityTracks();

        btnSelectFile.setOnClickListener(v -> {
            // Simulate file selection
            Toast.makeText(getContext(), "File Selected: audio_track.mp3", Toast.LENGTH_SHORT).show();
            btnSelectFile.setText("audio_track.mp3");
        });

        btnUpload.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String artist = editArtist.getText().toString();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(artist)) {
                Toast.makeText(getContext(), "Please enter title and artist", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulate upload
            Track newTrack = new Track();
            newTrack.setTitle(title);
            com.example.music.data.model.Artist artistObj = new com.example.music.data.model.Artist();
            artistObj.setName(artist);
            newTrack.setArtist(artistObj);
            // newTrack.setDuration(180); // Dummy duration

            CommunityRepository.getInstance().addTrack(newTrack);

            // Clear input
            editTitle.setText("");
            editArtist.setText("");
            btnSelectFile.setText("Select File");

            Toast.makeText(getContext(), "Upload Successful!", Toast.LENGTH_SHORT).show();

            // Refresh list
            loadCommunityTracks();
        });
    }

    private void loadCommunityTracks() {
        adapter.setTracks(CommunityRepository.getInstance().getUploadedTracks());
    }
}
