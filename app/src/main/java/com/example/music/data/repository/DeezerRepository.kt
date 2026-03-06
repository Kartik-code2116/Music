package com.example.music.data.repository

import com.example.music.data.api.DeezerRetrofitClient
import com.example.music.data.model.DeezerAlbum
import com.example.music.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeezerRepository {

    private val api = DeezerRetrofitClient.api

    suspend fun getChartTracks(limit: Int = 50): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChartTracks(limit)
                if (response.error != null) {
                    Result.failure(Exception("Deezer error: ${response.error.message}"))
                } else {
                    Result.success(response.data)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchTracks(query: String, limit: Int = 30): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchTracks(query, limit)
                if (response.error != null) {
                    Result.failure(Exception("Deezer error: ${response.error.message}"))
                } else {
                    Result.success(response.data)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getChartAlbums(limit: Int = 20): Result<List<DeezerAlbum>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getChartAlbums(limit)
                if (response.error != null) {
                    Result.failure(Exception("Deezer error: ${response.error.message}"))
                } else {
                    Result.success(response.data)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
