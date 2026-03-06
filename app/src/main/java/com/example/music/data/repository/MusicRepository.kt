package com.example.music.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.music.data.api.SupabaseApi
import com.example.music.data.model.Album
import com.example.music.data.model.Artist
import com.example.music.data.model.Track
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.regex.Pattern

class MusicRepository(private val supabaseApi: SupabaseApi) {
    private val BUCKET_NAME = "songs"
    private val BASE_URL = "https://cizbkhhbufimvqswvwsh.storage.supabase.co/"

    fun getChartTracks(): LiveData<List<Track>?> {
        return getCloudTracks()
    }

    fun searchTracks(query: String): LiveData<List<Track>?> {
        val result = MutableLiveData<List<Track>?>()
        supabaseApi.listObjects(BUCKET_NAME, 100).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val xml = response.body()!!.string()
                        val allTracks = parseS3Xml(xml)
                        result.value = allTracks.filter {
                            it.title.contains(query, ignoreCase = true) ||
                            it.artist.name.contains(query, ignoreCase = true)
                        }
                    } catch (e: IOException) {
                        result.value = emptyList()
                    }
                } else {
                    result.value = emptyList()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                result.value = emptyList()
            }
        })
        return result
    }

    fun getCloudTracks(): LiveData<List<Track>?> {
        val result = MutableLiveData<List<Track>?>()
        supabaseApi.listObjects(BUCKET_NAME, 100).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val xml = response.body()!!.string()
                        result.value = parseS3Xml(xml)
                    } catch (e: IOException) {
                        result.value = null
                    }
                } else {
                    result.value = null
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                result.value = null
            }
        })
        return result
    }

    private fun parseS3Xml(xml: String): List<Track> {
        val tracks = mutableListOf<Track>()
        val pattern = Pattern.compile("<Key>(.*?)</Key>")
        val matcher = pattern.matcher(xml)
        while (matcher.find()) {
            val key = matcher.group(1)
            if (key != null && (key.endsWith(".mp3") || key.endsWith(".wav") || key.endsWith(".m4a"))) {
                tracks.add(createTrackFromKey(key))
            }
        }
        return tracks
    }

    private fun createTrackFromKey(key: String): Track {
        val fileName = if (key.contains("/")) key.substring(key.lastIndexOf("/") + 1) else key
        val title = if (fileName.contains(".")) fileName.substring(0, fileName.lastIndexOf(".")) else fileName

        return Track(
            id = key.hashCode().toLong(),
            title = title,
            preview = "${BASE_URL}storage/v1/object/public/$BUCKET_NAME/$key",
            duration = 0,
            artist = Artist(0, "Cloud Upload"),
            album = Album(
                id = 0,
                title = "Cloud Content",
                coverMedium = "https://cizbkhhbufimvqswvwsh.supabase.co/storage/v1/object/public/songs/cover.png"
            )
        )
    }
}
