package com.example.music.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.example.music.data.model.Album;
import com.example.music.data.model.Artist;
import com.example.music.data.model.Track;
import java.util.ArrayList;
import java.util.List;

public class LocalMusicLoader {

    public static List<Track> loadLocalTracks(Context context) {
        List<Track> tracks = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            do {
                long id = cursor.getLong(idColumn);
                String title = cursor.getString(titleColumn);
                String artistName = cursor.getString(artistColumn);
                String albumName = cursor.getString(albumColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri trackUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);

                Artist artist = new Artist();
                artist.setName(artistName);

                Album album = new Album();
                album.setTitle(albumName);
                album.setCoverMedium(albumArtUri.toString());
                album.setCoverBig(albumArtUri.toString());

                Track track = new Track();
                track.setId(id);
                track.setTitle(title);
                track.setArtist(artist);
                track.setAlbum(album);
                track.setPreview(trackUri.toString());

                tracks.add(track);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return tracks;
    }
}
