package com.rapidaid.api.model;

import java.util.List;

public class FirstAidResponse {
    private String id;
    private String title;
    private String color;
    private String imageUrl;
    private List<SubtypeResponse> subtypes;

    public FirstAidResponse() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<SubtypeResponse> getSubtypes() { return subtypes; }
    public void setSubtypes(List<SubtypeResponse> subtypes) { this.subtypes = subtypes; }
}
