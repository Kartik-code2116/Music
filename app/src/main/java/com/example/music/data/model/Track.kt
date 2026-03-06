package com.example.music.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("title_short")
    val titleShort: String? = null,
    @SerializedName("link")
    val link: String? = null,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("preview")
    val preview: String,
    @Embedded(prefix = "artist_")
    @SerializedName("artist")
    val artist: Artist,
    @Embedded(prefix = "album_")
    @SerializedName("album")
    val album: Album,
    val isFavorite: Boolean = false
)

data class Artist(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("picture_small")
    val pictureSmall: String? = null,
    @SerializedName("picture_medium")
    val pictureMedium: String? = null
)

data class Album(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("cover_small")
    val coverSmall: String? = null,
    @SerializedName("cover_medium")
    val coverMedium: String? = null,
    @SerializedName("cover_big")
    val coverBig: String? = null
)
