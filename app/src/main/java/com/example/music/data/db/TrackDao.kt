package com.example.music.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.music.data.model.Track

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY id DESC")
    fun getAllTracks(): LiveData<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()
}
