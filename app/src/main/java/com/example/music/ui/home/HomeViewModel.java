package com.example.music.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.music.data.api.RetrofitClient;
import com.example.music.data.model.Album;
import com.example.music.data.model.Artist;
import com.example.music.data.model.ChartResponse;
import com.example.music.data.model.Track;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Track>> tracks = new MutableLiveData<>();
    private final MutableLiveData<List<Album>> albums = new MutableLiveData<>();
    private final MutableLiveData<List<Artist>> artists = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Track>> getTracks() {
        return tracks;
    }

    public LiveData<List<Album>> getAlbums() {
        return albums;
    }

    public LiveData<List<Artist>> getArtists() {
        return artists;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchCharts() {
        // Using Top Worldwide Playlist ID: 3155776842 instead of /chart
        RetrofitClient.getApi().getPlaylistTracks(3155776842L)
                .enqueue(new Callback<com.example.music.data.model.DataResponse<Track>>() {
                    @Override
                    public void onResponse(Call<com.example.music.data.model.DataResponse<Track>> call,
                            Response<com.example.music.data.model.DataResponse<Track>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Track> trackList = response.body().getData();
                            tracks.postValue(trackList);

                            // Extract unique albums from tracks
                            java.util.Set<String> albumIds = new java.util.HashSet<>();
                            List<Album> albumList = new java.util.ArrayList<>();
                            for (Track track : trackList) {
                                Album album = track.getAlbum();
                                if (album != null && !albumIds.contains(String.valueOf(album.getId()))) {
                                    albumIds.add(String.valueOf(album.getId()));
                                    albumList.add(album);
                                }
                            }
                            albums.postValue(albumList);

                        } else {
                            error.postValue("Failed to load charts: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.music.data.model.DataResponse<Track>> call, Throwable t) {
                        error.postValue("Error: " + t.getMessage());
                    }
                });
    }
}
