package com.example.music.data.model;

public class Category {
    private String title;
    private int color;

    public Category(String title, int color) {
        this.title = title;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }
}
