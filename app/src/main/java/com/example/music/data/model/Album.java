package com.example.music.data.model;

import com.google.gson.annotations.SerializedName;

public class Album {
    @SerializedName("id")
    private long id;
    @SerializedName("title")
    private String title;
    @SerializedName("cover")
    private String cover;
    @SerializedName("cover_small")
    private String coverSmall;
    @SerializedName("cover_medium")
    private String coverMedium;
    @SerializedName("cover_big")
    private String coverBig;
    @SerializedName("cover_xl")
    private String coverXl;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCover() {
        return cover;
    }

    public String getCoverSmall() {
        return coverSmall;
    }

    public String getCoverMedium() {
        return coverMedium;
    }

    public String getCoverBig() {
        return coverBig;
    }

    public String getCoverXl() {
        return coverXl;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setCoverSmall(String coverSmall) {
        this.coverSmall = coverSmall;
    }

    public void setCoverMedium(String coverMedium) {
        this.coverMedium = coverMedium;
    }

    public void setCoverBig(String coverBig) {
        this.coverBig = coverBig;
    }

    public void setCoverXl(String coverXl) {
        this.coverXl = coverXl;
    }
}
