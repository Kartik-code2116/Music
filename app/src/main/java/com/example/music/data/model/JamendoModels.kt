package com.example.music.data.model

import com.google.gson.annotations.SerializedName

data class JamendoTracksResponse(
    @SerializedName("headers") val headers: JamendoHeaders? = null,
    @SerializedName("results") val results: List<JamendoTrack> = emptyList()
)

data class JamendoHeaders(
    @SerializedName("status") val status: String? = null,
    @SerializedName("code") val code: Int = 0,
    @SerializedName("error_message") val errorMessage: String? = null,
    @SerializedName("results_count") val resultsCount: Int = 0
)

data class JamendoTrack(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("artist_id") val artistId: String = "",
    @SerializedName("artist_name") val artistName: String = "Unknown Artist",
    @SerializedName("artist_idstr") val artistIdStr: String? = null,
    @SerializedName("album_name") val albumName: String = "Unknown Album",
    @SerializedName("album_id") val albumId: String = "0",
    @SerializedName("album_image") val albumImage: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("audio") val audio: String? = null,
    @SerializedName("audiodownload") val audioDownload: String? = null,
    @SerializedName("prourl") val proUrl: String? = null,
    @SerializedName("shorturl") val shortUrl: String? = null,
    @SerializedName("shareurl") val shareUrl: String? = null,
    @SerializedName("license_ccurl") val licenseCcUrl: String? = null,
    @SerializedName("position") val position: Int? = null,
    @SerializedName("releasedate") val releaseDate: String? = null,
    @SerializedName("album_idstr") val albumIdStr: String? = null
) {
    fun toTrack(): Track {
        val coverUrl = image ?: albumImage
        val playbackUrl = audio ?: audioDownload ?: ""

        return Track(
            id = id.toLongOrNull() ?: id.hashCode().toLong(),
            title = name,
            duration = duration * 1000, // convert seconds → milliseconds
            preview = playbackUrl,
            artist = Artist(
                id = artistId.toLongOrNull() ?: 0L,
                name = artistName,
                pictureSmall = null,
                pictureMedium = null
            ),
            album = Album(
                id = albumId.toLongOrNull() ?: 0L,
                title = albumName,
                coverSmall = coverUrl,
                coverMedium = coverUrl,
                coverBig = coverUrl
            )
        )
    }
}
