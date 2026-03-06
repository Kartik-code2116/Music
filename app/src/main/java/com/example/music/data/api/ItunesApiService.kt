package com.example.music.data.api

import com.example.music.data.model.ItunesSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {

    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("media") media: String = "music",
        @Query("limit") limit: Int = 25
    ): ItunesSearchResponse

    @GET("search")
    suspend fun getTopHits(
        @Query("term") term: String = "top hits 2024",
        @Query("entity") entity: String = "song",
        @Query("media") media: String = "music",
        @Query("limit") limit: Int = 30
    ): ItunesSearchResponse

    @GET("search")
    suspend fun getGenreTracks(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("media") media: String = "music",
        @Query("genreId") genreId: Int? = null,
        @Query("limit") limit: Int = 25
    ): ItunesSearchResponse
}
