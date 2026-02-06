package com.example.music.data.repository;

import com.example.music.data.model.Track;
import com.example.music.data.model.Artist;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.music.data.db.MusicDatabase;
import com.example.music.data.db.TrackDao;
import com.example.music.data.model.Track;
import com.example.music.data.model.Artist;
import com.example.music.data.model.Album;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommunityRepository {
    private static CommunityRepository instance;
    private final TrackDao trackDao;
    private final ExecutorService executorService;

    private CommunityRepository(Context context) {
        MusicDatabase db = MusicDatabase.getInstance(context);
        trackDao = db.trackDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized CommunityRepository getInstance(Context context) {
        if (instance == null) {
            instance = new CommunityRepository(context);
        }
        return instance;
    }

    public LiveData<List<Track>> getUploadedTracks() {
        return trackDao.getAllTracks();
    }

    public void addTrack(Track track) {
        executorService.execute(() -> trackDao.insertTrack(track));
    }
}
