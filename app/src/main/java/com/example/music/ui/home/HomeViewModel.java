package com.example.music.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.music.data.api.RetrofitClient;
import com.example.music.data.db.MusicDatabase;
import com.example.music.data.db.TrackDao;
import com.example.music.data.model.Album;
import com.example.music.data.model.Artist;
import com.example.music.data.model.DataResponse;
import com.example.music.data.model.Track;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.music.data.repository.MusicRepository;
import com.example.music.data.api.SupabaseApi;

public class HomeViewModel extends AndroidViewModel {

    private final TrackDao trackDao;
    private final ExecutorService executorService;
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MusicRepository musicRepository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        MusicDatabase db = MusicDatabase.getInstance(application);
        trackDao = db.trackDao();
        executorService = Executors.newSingleThreadExecutor();
        musicRepository = new MusicRepository(RetrofitClient.getApi());
    }

    public LiveData<List<Track>> getTracks() {
        return trackDao.getAllTracks();
    }

    public LiveData<List<Album>> getAlbums() {
        // We can derive albums from tracks in the fragment or create a separate DAO
        // query
        // For simplicity, let's derive them in the fragment or use a MediatorLiveData
        MutableLiveData<List<Album>> albums = new MutableLiveData<>();
        getTracks().observeForever(trackList -> {
            if (trackList != null) {
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
            }
        });
        return albums;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void fetchCharts() {
        musicRepository.getChartTracks().observeForever(tracks -> {
            if (tracks != null) {
                executorService.execute(() -> {
                    trackDao.insertTracks(tracks);
                });
            } else {
                error.postValue("Failed to load cloud tracks from Supabase");
            }
        });
    }
}
