package com.example.music.data.repository;

import com.example.music.data.model.Track;
import com.example.music.data.model.Artist;
import java.util.ArrayList;
import java.util.List;

public class CommunityRepository {
    private static CommunityRepository instance;
    private final List<Track> uploadedTracks;

    private CommunityRepository() {
        uploadedTracks = new ArrayList<>();

        // Add some dummy data to simulate community activity
        Track dummy1 = new Track();
        dummy1.setTitle("Indie Vibes");
        Artist artist1 = new Artist();
        artist1.setName("Local Artist");
        dummy1.setArtist(artist1);
        com.example.music.data.model.Album album1 = new com.example.music.data.model.Album();
        album1.setCoverMedium(""); // Placeholder or empty
        dummy1.setAlbum(album1);

        Track dummy2 = new Track();
        dummy2.setTitle("Late Night Jam");
        Artist artist2 = new Artist();
        artist2.setName("Bedroom Producer");
        dummy2.setArtist(artist2);
        com.example.music.data.model.Album album2 = new com.example.music.data.model.Album();
        album2.setCoverMedium(""); // Placeholder or empty
        dummy2.setAlbum(album2);

        uploadedTracks.add(dummy1);
        uploadedTracks.add(dummy2);
    }

    public static synchronized CommunityRepository getInstance() {
        if (instance == null) {
            instance = new CommunityRepository();
        }
        return instance;
    }

    public List<Track> getUploadedTracks() {
        return new ArrayList<>(uploadedTracks);
    }

    public void addTrack(Track track) {
        uploadedTracks.add(0, track); // Add to top
    }
}
