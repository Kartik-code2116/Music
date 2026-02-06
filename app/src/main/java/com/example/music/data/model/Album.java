package com.example.music.data.model;

import com.google.gson.annotations.SerializedName;

public class Album {
    @SerializedName("id")
    private long id;
    @SerializedName("title")
    private String title;
    @SerializedName("cover_medium")
    private String coverMedium;
    @SerializedName("cover_big")
    private String coverBig;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverMedium() {
        return coverMedium;
    }

    public void setCoverMedium(String coverMedium) {
        this.coverMedium = coverMedium;
    }

    public String getCoverBig() {
        return coverBig;
    }

    public void setCoverBig(String coverBig) {
        this.coverBig = coverBig;
    }
}
