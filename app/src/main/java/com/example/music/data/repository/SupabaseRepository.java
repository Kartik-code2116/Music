package com.example.music.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.data.api.SupabaseApi;
import com.example.music.data.model.Album;
import com.example.music.data.model.Artist;
import com.example.music.data.model.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SupabaseRepository {
    private static final String BASE_URL = "https://impwtgbbgzhfhqqnogca.storage.supabase.co/";
    private static final String BUCKET_NAME = "songs"; // Default bucket name, should be confirmed
    private final SupabaseApi supabaseApi;

    public SupabaseRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build();
        supabaseApi = retrofit.create(SupabaseApi.class);
    }

    public LiveData<List<Track>> getCloudTracks() {
        MutableLiveData<List<Track>> result = new MutableLiveData<>();
        supabaseApi.listObjects(BUCKET_NAME, 2).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String xml = response.body().string();
                        result.setValue(parseS3Xml(xml));
                    } catch (IOException e) {
                        result.setValue(null);
                    }
                } else {
                    result.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                result.setValue(null);
            }
        });
        return result;
    }

    private List<Track> parseS3Xml(String xml) {
        List<Track> tracks = new ArrayList<>();
        // Simple regex to extract <Key> values from S3 ListBucket XML
        Pattern pattern = Pattern.compile("<Key>(.*?)</Key>");
        Matcher matcher = pattern.matcher(xml);

        while (matcher.find()) {
            String key = matcher.group(1);
            if (key != null && (key.endsWith(".mp3") || key.endsWith(".wav") || key.endsWith(".m4a"))) {
                tracks.add(createTrackFromKey(key));
            }
        }
        return tracks;
    }

    private Track createTrackFromKey(String key) {
        Track track = new Track();
        // Clean up the key to get a title
        String fileName = key.contains("/") ? key.substring(key.lastIndexOf("/") + 1) : key;
        String title = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;

        track.setId(key.hashCode());
        track.setTitle(title);
        // Correct S3 public URL format for Supabase
        track.setPreview(BASE_URL + "storage/v1/object/public/" + BUCKET_NAME + "/" + key);

        Artist artist = new Artist();
        artist.setName("Supabase Cloud");
        track.setArtist(artist);

        Album album = new Album();
        album.setTitle("Cloud Storage");
        album.setCoverMedium(
                "https://impwtgbbgzhfhqqnogca.storage.supabase.co/storage/v1/object/public/songs/cover.png"); // Potential
                                                                                                              // default
                                                                                                              // cover
        track.setAlbum(album);

        return track;
    }
}
