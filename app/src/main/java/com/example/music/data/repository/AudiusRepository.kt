package com.example.music.data.repository

import com.example.music.data.api.AudiusRetrofitClient
import com.example.music.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudiusRepository {

    private val api = AudiusRetrofitClient.api

    suspend fun getTrendingTracks(limit: Int = 25): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTrendingTracks(limit = limit)
                Result.success(response.data.filter { it.isStreamable }.map { it.toTrack() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchTracks(query: String, limit: Int = 25): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchTracks(query = query, limit = limit)
                Result.success(response.data.filter { it.isStreamable }.map { it.toTrack() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
