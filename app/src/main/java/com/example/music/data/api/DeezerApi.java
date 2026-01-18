package com.example.music.data.api;

import com.example.music.data.model.ChartResponse;
import com.example.music.data.model.DataResponse;
import com.example.music.data.model.Track;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DeezerApi {
    @GET("chart")
    Call<ChartResponse> getChart();

    @GET("search")
    Call<DataResponse<Track>> searchTracks(@Query("q") String query);

    @GET("playlist/{id}/tracks")
    Call<DataResponse<Track>> getPlaylistTracks(@retrofit2.http.Path("id") long id);

    @GET("search/album")
    Call<DataResponse<com.example.music.data.model.Album>> searchAlbums(@Query("q") String query);
}
