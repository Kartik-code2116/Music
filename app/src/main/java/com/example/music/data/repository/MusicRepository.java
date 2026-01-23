package com.example.music.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.data.api.DeezerApi;
import com.example.music.data.model.Album;
import com.example.music.data.model.ChartResponse;
import com.example.music.data.model.DataResponse;
import com.example.music.data.model.Track;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicRepository {
    private final DeezerApi deezerApi;

    public MusicRepository(DeezerApi deezerApi) {
        this.deezerApi = deezerApi;
    }

    public LiveData<List<Track>> getChartTracks() {
        MutableLiveData<List<Track>> result = new MutableLiveData<>();
        deezerApi.getChart().enqueue(new Callback<ChartResponse>() {
            @Override
            public void onResponse(Call<ChartResponse> call, Response<ChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getTracks());
                }
            }

            @Override
            public void onFailure(Call<ChartResponse> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<List<Album>> getChartAlbums() {
        MutableLiveData<List<Album>> result = new MutableLiveData<>();
        deezerApi.getChart().enqueue(new Callback<ChartResponse>() {
            @Override
            public void onResponse(Call<ChartResponse> call, Response<ChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getAlbums());
                }
            }

            @Override
            public void onFailure(Call<ChartResponse> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<List<Track>> searchTracks(String query) {
        MutableLiveData<List<Track>> result = new MutableLiveData<>();
        deezerApi.searchTracks(query).enqueue(new Callback<DataResponse<Track>>() {
            @Override
            public void onResponse(Call<DataResponse<Track>> call, Response<DataResponse<Track>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<DataResponse<Track>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    public LiveData<List<Track>> getPlaylistTracks(long playlistId) {
        MutableLiveData<List<Track>> result = new MutableLiveData<>();
        deezerApi.getPlaylistTracks(playlistId).enqueue(new Callback<DataResponse<Track>>() {
            @Override
            public void onResponse(Call<DataResponse<Track>> call, Response<DataResponse<Track>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<DataResponse<Track>> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }
}
