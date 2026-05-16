package com.example.music.data.model

import com.example.music.data.api.AudiusRetrofitClient
import com.google.gson.annotations.SerializedName

data class AudiusTracksResponse(
    @SerializedName("data") val data: List<AudiusTrack> = emptyList()
)

data class AudiusTrack(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "Unknown Title",
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("user") val user: AudiusUser? = null,
    @SerializedName("artwork") val artwork: AudiusArtwork? = null,
    @SerializedName("is_streamable") val isStreamable: Boolean = true
) {
    fun toTrack(): Track {
        val artistName = user?.name ?: "Audius Artist"
        val coverUrl = artwork?.large ?: artwork?.medium ?: artwork?.small
        val streamUrl = "${AudiusRetrofitClient.BASE_URL}tracks/$id/stream?app_name=${AudiusRetrofitClient.APP_NAME}"
        return Track(
            id = "audius:$id".hashCode().toLong(),
            title = title,
            duration = duration * 1000,
            preview = streamUrl,
            artist = Artist(
                id = user?.id?.hashCode()?.toLong() ?: 0L,
                name = artistName
            ),
            album = Album(
                id = id.hashCode().toLong(),
                title = "Audius",
                coverSmall = artwork?.small,
                coverMedium = artwork?.medium,
                coverBig = coverUrl
            )
        )
    }
}

data class AudiusUser(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "Audius Artist"
)

data class AudiusArtwork(
    @SerializedName("150x150") val small: String? = null,
    @SerializedName("480x480") val medium: String? = null,
    @SerializedName("1000x1000") val large: String? = null
)
