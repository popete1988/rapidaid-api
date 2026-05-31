package com.rapidaid.api.model;

public class TipResponse {
    private String id;
    private String title;
    private String contenido;

    public TipResponse() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
