package com.example.music;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.music.data.model.Track;
import com.example.music.data.repository.MusicRepository;
import com.example.music.data.api.RetrofitClient;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    private final MutableLiveData<List<Track>> playlist = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isShuffle = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRepeat = new MutableLiveData<>(false);
    private final MutableLiveData<List<Long>> favoriteIds = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> accentColor = new MutableLiveData<>(0xFF1DB954); // Default Spotify Green
    private final MutableLiveData<List<Track>> cloudTracks = new MutableLiveData<>(new ArrayList<>());
    private final MusicRepository musicRepository;

    public MainViewModel() {
        musicRepository = new MusicRepository(RetrofitClient.getApi());
    }

    public LiveData<Track> getCurrentTrack() {
        return currentTrack;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public LiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }

    public LiveData<Integer> getDuration() {
        return duration;
    }

    public LiveData<List<Track>> getPlaylist() {
        return playlist;
    }

    public LiveData<Boolean> getIsShuffle() {
        return isShuffle;
    }

    public LiveData<Boolean> getIsRepeat() {
        return isRepeat;
    }

    public LiveData<List<Long>> getFavoriteIds() {
        return favoriteIds;
    }

    public LiveData<Integer> getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int color) {
        accentColor.setValue(color);
    }

    public void toggleFavorite(long trackId) {
        List<Long> current = favoriteIds.getValue();
        if (current == null)
            current = new ArrayList<>();
        if (current.contains(trackId)) {
            current.remove(trackId);
        } else {
            current.add(trackId);
        }
        favoriteIds.setValue(new ArrayList<>(current)); // Ensure update notification
    }

    public boolean isFavorite(long trackId) {
        List<Long> current = favoriteIds.getValue();
        return current != null && current.contains(trackId);
    }

    public LiveData<List<Track>> getCloudTracks() {
        return cloudTracks;
    }

    public void loadCloudTracks() {
        musicRepository.getCloudTracks().observeForever(tracks -> {
            if (tracks != null) {
                cloudTracks.setValue(tracks);
            }
        });
    }

    public void setPlaylist(List<Track> tracks) {
        playlist.setValue(tracks);
    }

    public void playTrack(Track track) {
        currentTrack.setValue(track);
        isPlaying.setValue(true);
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }

    public void setDuration(int d) {
        duration.setValue(d);
    }

    public void setCurrentPosition(int p) {
        currentPosition.setValue(p);
    }

    public void toggleShuffle() {
        isShuffle.setValue(!Boolean.TRUE.equals(isShuffle.getValue()));
    }

    public void toggleRepeat() {
        isRepeat.setValue(!Boolean.TRUE.equals(isRepeat.getValue()));
    }

    public void nextTrack() {
        List<Track> tracks = playlist.getValue();
        Track current = currentTrack.getValue();
        if (tracks == null || tracks.isEmpty() || current == null)
            return;

        if (Boolean.TRUE.equals(isShuffle.getValue())) {
            int randomIndex = (int) (Math.random() * tracks.size());
            playTrack(tracks.get(randomIndex));
            return;
        }

        int index = -1;
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId() == current.getId()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            int nextIndex = (index + 1) % tracks.size();
            playTrack(tracks.get(nextIndex));
        }
    }

    public void previousTrack() {
        List<Track> tracks = playlist.getValue();
        Track current = currentTrack.getValue();
        if (tracks == null || tracks.isEmpty() || current == null)
            return;

        int index = -1;
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId() == current.getId()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            int prevIndex = (index - 1 + tracks.size()) % tracks.size();
            playTrack(tracks.get(prevIndex));
        }
    }
}
