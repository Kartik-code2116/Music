package com.example.music.data.repository

import com.example.music.data.api.JamendoRetrofitClient
import com.example.music.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JamendoRepository {

    private val api = JamendoRetrofitClient.api
    private val clientId = JamendoRetrofitClient.CLIENT_ID

    suspend fun getPopularTracks(limit: Int = 20): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPopularTracks(clientId = clientId, limit = limit)
                if (response.headers?.code == 0) {
                    Result.success(response.results.map { it.toTrack() })
                } else {
                    Result.success(response.results.map { it.toTrack() })
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun searchTracks(query: String, limit: Int = 20): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchTracks(
                    clientId = clientId,
                    query = query,
                    limit = limit
                )
                Result.success(response.results.map { it.toTrack() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTracksByTag(tag: String, limit: Int = 20): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTracksByTag(
                    clientId = clientId,
                    tags = tag,
                    limit = limit
                )
                Result.success(response.results.map { it.toTrack() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getFeaturedTracks(limit: Int = 20): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getFeaturedTracks(
                    clientId = clientId,
                    limit = limit
                )
                Result.success(response.results.map { it.toTrack() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
