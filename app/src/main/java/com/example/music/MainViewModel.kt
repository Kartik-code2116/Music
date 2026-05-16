package com.example.music

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.data.api.RetrofitClient
import com.example.music.data.api.SupabaseClient
import com.example.music.data.model.Album
import com.example.music.data.model.Artist
import com.example.music.data.model.Track
import com.example.music.data.repository.MusicRepository
import com.example.music.data.repository.SupabaseRepository
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel : ViewModel() {

    // --- 🎵 Playback State ---
    private val _currentTrack = MutableLiveData<Track?>()
    val currentTrack: LiveData<Track?> get() = _currentTrack

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _playlist = MutableLiveData<List<Track>>(emptyList())
    val playlist: LiveData<List<Track>> get() = _playlist

    private val _currentPosition = MutableLiveData(0)
    val currentPosition: LiveData<Int> get() = _currentPosition

    private val _pendingSeek = MutableLiveData<Int?>(null)
    val pendingSeek: LiveData<Int?> get() = _pendingSeek

    private val _duration = MutableLiveData(0)
    val duration: LiveData<Int> get() = _duration

    private val _isShuffle = MutableLiveData(false)
    val isShuffle: LiveData<Boolean> get() = _isShuffle

    private val _isRepeat = MutableLiveData(false)
    val isRepeat: LiveData<Boolean> get() = _isRepeat

    private val _favoriteIds = MutableLiveData<List<Long>>(emptyList())
    val favoriteIds: LiveData<List<Long>> get() = _favoriteIds

    // --- ☁️ Supabase / Cloud State ---
    private val _savedTracks = MutableLiveData<List<Track>>(emptyList())
    val savedTracks: LiveData<List<Track>> get() = _savedTracks

    private val _supabaseMessage = MutableLiveData<String?>()
    val supabaseMessage: LiveData<String?> get() = _supabaseMessage

    private val _savingInProgress = MutableLiveData(false)
    val savingInProgress: LiveData<Boolean> get() = _savingInProgress

    private val _playbackSpeed = MutableLiveData(1.0f)
    val playbackSpeed: LiveData<Float> get() = _playbackSpeed

    private val _accentColor = MutableLiveData(0xFF1DB954.toInt())
    val accentColor: LiveData<Int> get() = _accentColor

    // --- 🛠️ Repositories ---
    private val musicRepo = MusicRepository(RetrofitClient.api)
    private val supabaseRepo = SupabaseRepository()
    private val storageBucket = SupabaseClient.client.storage["songs"]

    fun uploadTrack(title: String, artist: String, fileData: ByteArray, fileName: String) {
        if (_savingInProgress.value == true) return
        _savingInProgress.value = true
        viewModelScope.launch {
            try {
                storageBucket.upload(fileName, fileData, upsert = true)
                val publicUrl = "https://cizbkhhbufimvqswvwsh.storage.supabase.co/storage/v1/object/public/songs/$fileName"
                val newTrack = Track(
                    id = fileName.hashCode().toLong(),
                    title = title,
                    artist = Artist(0, artist),
                    album = Album(0, "Cloud Uploads", null, "https://cizbkhhbufimvqswvwsh.supabase.co/storage/v1/object/public/songs/cover.png"),
                    preview = publicUrl,
                    duration = 0
                )
                val dbResult = supabaseRepo.saveTrack(newTrack)
                if (dbResult.isSuccess) {
                    _supabaseMessage.postValue("Successfully uploaded \"$title\"! 🎵")
                    refreshCloudLibrary()
                } else {
                    _supabaseMessage.postValue("Metadata failed to save: ${dbResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _supabaseMessage.postValue("Upload error: ${e.localizedMessage}")
            } finally {
                _savingInProgress.postValue(false)
            }
        }
    }

    fun toggleFavorite(trackId: Long) {
        val currentFavs = _favoriteIds.value?.toMutableList() ?: mutableListOf()
        val isNowFav = if (currentFavs.contains(trackId)) {
            currentFavs.remove(trackId)
            false
        } else {
            currentFavs.add(trackId)
            true
        }
        _favoriteIds.value = currentFavs
        viewModelScope.launch {
            supabaseRepo.toggleFavorite(trackId, isNowFav)
        }
    }

    fun shiftTrack(forward: Boolean) {
        val tracks = _playlist.value ?: return
        val current = _currentTrack.value ?: return
        val currentIndex = tracks.indexOfFirst { it.id == current.id }
        if (currentIndex != -1) {
            val nextIndex = if (forward) (currentIndex + 1) % tracks.size else (currentIndex - 1 + tracks.size) % tracks.size
            playTrack(tracks[nextIndex])
        }
    }

    fun playTrack(track: Track) {
        if (_playlist.value.isNullOrEmpty()) {
            _playlist.value = listOf(track)
        }
        _currentTrack.value = track
        _isPlaying.value = true
    }

    fun playTracks(tracks: List<Track>, startTrack: Track) {
        val playableTracks = tracks.filter { it.preview.isNotBlank() }
        val queue = playableTracks.ifEmpty { listOf(startTrack) }
        _playlist.value = queue
        _currentTrack.value = queue.firstOrNull { it.id == startTrack.id } ?: queue.first()
        _isPlaying.value = true
    }

    fun syncControllerTrack(trackId: Long?) {
        val nextTrack = _playlist.value
            ?.firstOrNull { it.id == trackId }
            ?: return
        if (_currentTrack.value?.id != nextTrack.id) {
            _currentTrack.value = nextTrack
        }
        _isPlaying.value = true
    }

    fun refreshCloudLibrary() {
        viewModelScope.launch {
            supabaseRepo.getSavedTracks().onSuccess {
                _savedTracks.postValue(it)
            }.onFailure {
                _supabaseMessage.postValue("Sync failed: ${it.message}")
            }
        }
    }

    fun toggleShuffle() {
        val shuffled = !(_isShuffle.value ?: false)
        _isShuffle.value = shuffled
        if (shuffled) {
            _playlist.value = _playlist.value?.shuffled() ?: emptyList()
        }
    }

    fun toggleRepeat() {
        _isRepeat.value = !(_isRepeat.value ?: false)
    }

    fun shiftTrackWithRepeat(forward: Boolean) {
        val tracks = _playlist.value ?: return
        val current = _currentTrack.value ?: return
        val repeat = _isRepeat.value ?: false
        val shuffle = _isShuffle.value ?: false

        if (repeat) {
            // Replay the same track by re-setting it
            playTrack(current)
            return
        }

        val currentIndex = tracks.indexOfFirst { it.id == current.id }
        if (currentIndex == -1) return

        val nextIndex = if (shuffle) {
            Random.nextInt(tracks.size)
        } else {
            if (forward) (currentIndex + 1) % tracks.size
            else (currentIndex - 1 + tracks.size) % tracks.size
        }
        playTrack(tracks[nextIndex])
    }
    fun setPlaying(playing: Boolean) { _isPlaying.value = playing }
    fun setDuration(d: Int) { _duration.value = d }
    fun setCurrentPosition(p: Int) { _currentPosition.value = p }

    /** Called only when the user explicitly scrubs the seekbar. Always triggers a seek. */
    fun seekTo(positionMs: Int) {
        _currentPosition.value = positionMs
        _pendingSeek.value = positionMs
    }

    fun clearPendingSeek() {
        _pendingSeek.value = null
    }
    fun setPlaybackSpeed(s: Float) { _playbackSpeed.value = s }

    /**
     * Verifies if the app can reach Supabase.
     */
    fun testConnection() {
        _savingInProgress.value = true
        _supabaseMessage.value = "Testing connection... 📡"
        viewModelScope.launch {
            try {
                val result = supabaseRepo.getSavedTracks()
                _savingInProgress.value = false
                if (result.isSuccess) {
                    _supabaseMessage.value = "Connected to Supabase! ✅"
                } else {
                    _supabaseMessage.value = "Connection failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _savingInProgress.value = false
                _supabaseMessage.value = "Network error: ${e.message}"
            }
        }
    }

    fun saveCurrentTrackToSupabase() {
        _currentTrack.value?.let { saveTrackToSupabase(it) }
            ?: run { _supabaseMessage.value = "No track is currently playing" }
    }

    fun saveTrackToSupabase(track: Track) {
        _savingInProgress.value = true
        viewModelScope.launch {
            val result = supabaseRepo.saveTrack(track, favoriteIds.value?.contains(track.id) == true)
            _savingInProgress.value = false
            if (result.isSuccess) {
                _supabaseMessage.value = "\"${track.title}\" saved to cloud ☁️"
                refreshCloudLibrary()
            } else {
                _supabaseMessage.value = "Failed to save: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearSupabaseMessage() { _supabaseMessage.value = null }
    fun setPlaylist(tracks: List<Track>) { _playlist.value = tracks }
    fun setAccentColor(color: Int) { _accentColor.value = color }
}
