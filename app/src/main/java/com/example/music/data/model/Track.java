package com.example.music.data.model;

import com.google.gson.annotations.SerializedName;

public class Track {
    @SerializedName("id")
    private long id;
    @SerializedName("title")
    private String title;
    @SerializedName("title_short")
    private String titleShort;
    @SerializedName("link")
    private String link;
    @SerializedName("duration")
    private int duration;
    @SerializedName("preview")
    private String preview;
    @SerializedName("artist")
    private Artist artist;
    @SerializedName("album")
    private Album album;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleShort() {
        return titleShort;
    }

    public String getLink() {
        return link;
    }

    public int getDuration() {
        return duration;
    }

    public String getPreview() {
        return preview;
    }

    public Artist getArtist() {
        return artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleShort(String titleShort) {
        this.titleShort = titleShort;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}
