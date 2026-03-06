package com.example.music.data.model

import com.google.gson.annotations.SerializedName

data class ItunesSearchResponse(
    @SerializedName("resultCount") val resultCount: Int = 0,
    @SerializedName("results") val results: List<ItunesTrack> = emptyList()
)

data class ItunesTrack(
    @SerializedName("wrapperType") val wrapperType: String? = null,
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("trackId") val trackId: Long = 0L,
    @SerializedName("artistId") val artistId: Long = 0L,
    @SerializedName("collectionId") val collectionId: Long = 0L,
    @SerializedName("trackName") val trackName: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("collectionName") val collectionName: String? = null,
    @SerializedName("previewUrl") val previewUrl: String? = null,
    @SerializedName("artworkUrl30") val artworkUrl30: String? = null,
    @SerializedName("artworkUrl60") val artworkUrl60: String? = null,
    @SerializedName("artworkUrl100") val artworkUrl100: String? = null,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long = 0L,
    @SerializedName("primaryGenreName") val primaryGenreName: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("trackNumber") val trackNumber: Int = 0,
    @SerializedName("trackCount") val trackCount: Int = 0,
    @SerializedName("discNumber") val discNumber: Int = 0,
    @SerializedName("country") val country: String? = null
) {
    /**
     * Returns true only if this entry is an actual playable music track.
     */
    fun isPlayable(): Boolean =
        kind == "song" && !previewUrl.isNullOrBlank() && !trackName.isNullOrBlank()

    /**
     * Returns a higher-resolution version of the artwork by replacing
     * the 100x100 suffix with 600x600.
     */
    fun artworkHd(): String? =
        artworkUrl100?.replace("100x100bb", "600x600bb")

    /**
     * Maps this iTunes result to the app's unified [Track] model.
     */
    fun toTrack(): Track = Track(
        id = trackId,
        title = trackName ?: "Unknown Title",
        titleShort = trackName,
        duration = (trackTimeMillis / 1000).toInt(),
        preview = previewUrl ?: "",
        artist = Artist(
            id = artistId,
            name = artistName ?: "Unknown Artist",
            pictureSmall = null,
            pictureMedium = null
        ),
        album = Album(
            id = collectionId,
            title = collectionName ?: "Unknown Album",
            coverSmall = artworkUrl60,
            coverMedium = artworkUrl100,
            coverBig = artworkHd()
        ),
        isFavorite = false
    )
}
