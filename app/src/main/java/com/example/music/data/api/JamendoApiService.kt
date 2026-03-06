package com.example.music.data.api

import com.example.music.data.model.JamendoTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {

    @GET("tracks/")
    suspend fun getPopularTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("boost") boost: String = "popularity_total",
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("include") include: String = "musicinfo"
    ): JamendoTracksResponse

    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("namesearch") query: String,
        @Query("limit") limit: Int = 20,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("include") include: String = "musicinfo"
    ): JamendoTracksResponse

    @GET("tracks/")
    suspend fun getTracksByTag(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("tags") tags: String,
        @Query("limit") limit: Int = 20,
        @Query("boost") boost: String = "popularity_total",
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoTracksResponse

    @GET("tracks/")
    suspend fun getFeaturedTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("featured") featured: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoTracksResponse
}
