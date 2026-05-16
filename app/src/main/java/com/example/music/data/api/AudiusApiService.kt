package com.example.music.data.api

import com.example.music.data.model.AudiusTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AudiusApiService {

    @GET("tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "MusicApp",
        @Query("limit") limit: Int = 25
    ): AudiusTracksResponse

    @GET("tracks/trending")
    suspend fun getTrendingTracks(
        @Query("app_name") appName: String = "MusicApp",
        @Query("limit") limit: Int = 25
    ): AudiusTracksResponse
}
