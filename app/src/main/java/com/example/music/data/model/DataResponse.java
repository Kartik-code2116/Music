package com.example.music.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DataResponse<T> {
    @SerializedName("data")
    private List<T> data;
    @SerializedName("total")
    private int total;
    @SerializedName("next")
    private String next;

    public List<T> getData() {
        return data;
    }

    public int getTotal() {
        return total;
    }

    public String getNext() {
        return next;
    }
}
