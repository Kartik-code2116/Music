package com.example.music.data.repository

import com.example.music.data.api.ItunesRetrofitClient
import com.example.music.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItunesRepository {

    private val api = ItunesRetrofitClient.api

    suspend fun getTopHits(limit: Int = 30): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTopHits(limit = limit)
                val tracks = response.results
                    .filter { it.isPlayable() }
                    .map { it.toTrack() }
                Result.success(tracks)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchTracks(query: String, limit: Int = 25): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchTracks(term = query, limit = limit)
                val tracks = response.results
                    .filter { it.isPlayable() }
                    .map { it.toTrack() }
                Result.success(tracks)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getGenreTracks(genre: String, limit: Int = 25): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getGenreTracks(term = genre, limit = limit)
                val tracks = response.results
                    .filter { it.isPlayable() }
                    .map { it.toTrack() }
                Result.success(tracks)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
