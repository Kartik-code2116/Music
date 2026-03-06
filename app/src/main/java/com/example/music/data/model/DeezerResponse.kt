package com.example.music.data.model

import com.google.gson.annotations.SerializedName

data class DeezerTracksResponse(
    @SerializedName("data") val data: List<Track> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("next") val next: String? = null,
    @SerializedName("error") val error: DeezerError? = null
)

data class DeezerAlbumsResponse(
    @SerializedName("data") val data: List<DeezerAlbum> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("error") val error: DeezerError? = null
)

data class DeezerAlbum(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("cover_small") val coverSmall: String? = null,
    @SerializedName("cover_medium") val coverMedium: String? = null,
    @SerializedName("cover_big") val coverBig: String? = null,
    @SerializedName("cover_xl") val coverXl: String? = null,
    @SerializedName("fans") val fans: Long = 0,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("record_type") val recordType: String? = null,
    @SerializedName("tracklist") val tracklist: String? = null,
    @SerializedName("artist") val artist: Artist? = null
) {
    fun toAlbum(): Album = Album(
        id = id,
        title = title,
        coverSmall = coverSmall,
        coverMedium = coverMedium,
        coverBig = coverBig
    )
}

data class DeezerError(
    @SerializedName("type") val type: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("code") val code: Int = 0
)

data class DeezerChartResponse(
    @SerializedName("tracks") val tracks: DeezerTracksResponse? = null,
    @SerializedName("albums") val albums: DeezerAlbumsResponse? = null,
    @SerializedName("artists") val artists: DeezerArtistsResponse? = null
)

data class DeezerArtistsResponse(
    @SerializedName("data") val data: List<DeezerArtist> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

data class DeezerArtist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("picture_small") val pictureSmall: String? = null,
    @SerializedName("picture_medium") val pictureMedium: String? = null,
    @SerializedName("picture_big") val pictureBig: String? = null,
    @SerializedName("nb_fan") val fans: Long = 0,
    @SerializedName("position") val position: Int = 0
) {
    fun toArtist(): Artist = Artist(
        id = id,
        name = name,
        pictureSmall = pictureSmall,
        pictureMedium = pictureMedium
    )
}
