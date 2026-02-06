package com.example.music.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.music.data.model.Track;

@Database(entities = { Track.class }, version = 2, exportSchema = false)
public abstract class MusicDatabase extends RoomDatabase {
    private static MusicDatabase instance;

    public abstract TrackDao trackDao();

    public static synchronized MusicDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MusicDatabase.class, "music_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
