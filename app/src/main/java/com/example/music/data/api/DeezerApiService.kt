package com.example.music.data.api

import com.example.music.data.model.DeezerAlbumsResponse
import com.example.music.data.model.DeezerTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApiService {

    @GET("chart/0/tracks")
    suspend fun getChartTracks(
        @Query("limit") limit: Int = 50
    ): DeezerTracksResponse

    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30
    ): DeezerTracksResponse

    @GET("chart/0/albums")
    suspend fun getChartAlbums(
        @Query("limit") limit: Int = 20
    ): DeezerAlbumsResponse

    @GET("chart/0/artists")
    suspend fun getChartArtists(
        @Query("limit") limit: Int = 10
    ): DeezerTracksResponse
}
