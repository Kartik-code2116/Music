package com.example.music.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.music.data.model.DeezerAlbum
import com.example.music.data.model.Track
import com.example.music.data.repository.DeezerRepository
import com.example.music.data.repository.ItunesRepository
import com.example.music.data.repository.JamendoRepository
import com.example.music.data.repository.SupabaseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val deezerRepository = DeezerRepository()
    private val itunesRepository = ItunesRepository()
    private val jamendoRepository = JamendoRepository()
    private val supabaseRepository = SupabaseRepository()

    // Deezer — chart tracks (grid + "Trending Now" row)
    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> get() = _tracks

    // Deezer — chart albums
    private val _deezerAlbums = MutableLiveData<List<DeezerAlbum>>()
    val deezerAlbums: LiveData<List<DeezerAlbum>> get() = _deezerAlbums

    // iTunes — top hits row
    private val _itunesTracks = MutableLiveData<List<Track>>()
    val itunesTracks: LiveData<List<Track>> get() = _itunesTracks

    // Jamendo — full free songs row
    private val _jamendoTracks = MutableLiveData<List<Track>>()
    val jamendoTracks: LiveData<List<Track>> get() = _jamendoTracks

    // Supabase — saved/uploaded tracks
    private val _savedTracks = MutableLiveData<List<Track>>()
    val savedTracks: LiveData<List<Track>> get() = _savedTracks

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchCharts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Fire all 4 network calls in parallel
            val deezerTracksDeferred  = async { deezerRepository.getChartTracks(50) }
            val deezerAlbumsDeferred  = async { deezerRepository.getChartAlbums(20) }
            val itunesDeferred        = async { itunesRepository.getTopHits(30) }
            val jamendoDeferred       = async { jamendoRepository.getPopularTracks(20) }

            // Deezer tracks
            deezerTracksDeferred.await()
                .onSuccess { _tracks.postValue(it) }
                .onFailure { _error.postValue("Deezer: ${it.localizedMessage}") }

            // Deezer albums
            deezerAlbumsDeferred.await()
                .onSuccess { _deezerAlbums.postValue(it) }

            // iTunes top hits
            itunesDeferred.await()
                .onSuccess { _itunesTracks.postValue(it) }
                .onFailure { _error.postValue("iTunes: ${it.localizedMessage}") }

            // Jamendo free full songs
            jamendoDeferred.await()
                .onSuccess { _jamendoTracks.postValue(it) }
                .onFailure { _error.postValue("Jamendo: ${it.localizedMessage}") }

            _isLoading.value = false
        }
    }

    fun fetchSavedTracks() {
        viewModelScope.launch {
            supabaseRepository.getSavedTracks()
                .onSuccess { _savedTracks.postValue(it) }
        }
    }
}
