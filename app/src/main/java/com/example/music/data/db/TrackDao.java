package com.example.music.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.music.data.model.Track;
import java.util.List;

@Dao
public interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY id DESC")
    LiveData<List<Track>> getAllTracks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTracks(List<Track> tracks);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrack(Track track);

    @Query("DELETE FROM tracks")
    void deleteAll();
}
