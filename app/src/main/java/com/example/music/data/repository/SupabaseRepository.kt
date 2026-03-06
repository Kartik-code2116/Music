package com.example.music.data.repository

import com.example.music.data.api.SupabaseClient
import com.example.music.data.model.SavedMusicItem
import com.example.music.data.model.Track
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhanced Repository for Supabase Postgrest operations.
 * Optimized for Supabase SDK v2.6.1 Compatibility.
 */
class SupabaseRepository {

    private val supabase = SupabaseClient.client
    private val TABLE_NAME = "saved_music"

    /**
     * Save a track using 'upsert' to prevent duplicate entries based on track_id.
     */
    suspend fun saveTrack(track: Track, isFavorite: Boolean = false): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val item = SavedMusicItem.fromTrack(track, isFavorite)
                // In v2.6.1, upsert is a simple call
                supabase.postgrest[TABLE_NAME].upsert(item)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun saveTracks(tracks: List<Track>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val items = tracks.map { SavedMusicItem.fromTrack(it) }
                supabase.postgrest[TABLE_NAME].upsert(items)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSavedTracks(): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val items = supabase.postgrest[TABLE_NAME]
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<SavedMusicItem>()
                Result.success(items.map { it.toTrack() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun toggleFavorite(trackId: Long, isFavorite: Boolean): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest[TABLE_NAME].update(
                    { set("is_favorite", isFavorite) }
                ) {
                    filter { eq("track_id", trackId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteTrack(trackId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.postgrest[TABLE_NAME].delete {
                    filter { eq("track_id", trackId) }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
