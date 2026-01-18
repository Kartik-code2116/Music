package com.example.music.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChartResponse {
    @SerializedName("tracks")
    private TrackList tracks;

    @SerializedName("albums")
    private AlbumList albums;

    @SerializedName("artists")
    private ArtistList artists;

    public List<Track> getTracks() {
        return tracks != null ? tracks.data : null;
    }

    public List<Album> getAlbums() {
        return albums != null ? albums.data : null;
    }

    public List<Artist> getArtists() {
        return artists != null ? artists.data : null;
    }

    private static class TrackList {
        @SerializedName("data")
        List<Track> data;
    }

    private static class AlbumList {
        @SerializedName("data")
        List<Album> data;
    }

    private static class ArtistList {
        @SerializedName("data")
        List<Artist> data;
    }
}
