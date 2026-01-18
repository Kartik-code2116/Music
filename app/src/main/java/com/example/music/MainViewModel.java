package com.example.music;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.music.data.model.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<Track> currentTrack = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    private final MutableLiveData<List<Track>> playlist = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isShuffle = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRepeat = new MutableLiveData<>(false);

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
        isShuffle.setValue(Boolean.FALSE.equals(isShuffle.getValue()) ? true : false);
    }

    public void toggleRepeat() {
        isRepeat.setValue(Boolean.FALSE.equals(isRepeat.getValue()) ? true : false);
    }

    public void nextTrack() {
        List<Track> tracks = playlist.getValue();
        Track current = currentTrack.getValue();
        if (tracks == null || tracks.isEmpty() || current == null) return;

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
        if (tracks == null || tracks.isEmpty() || current == null) return;

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
