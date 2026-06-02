package com.rapidaid.api.model;

public class SubtypeResponse {
    private String name;
    private String sintomas;
    private String tratamiento;
    private String imageUrl;

    public SubtypeResponse() {}
    public SubtypeResponse(String name, String sintomas, String tratamiento) {
        this.name = name; this.sintomas = sintomas; this.tratamiento = tratamiento;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSintomas() { return sintomas; }
    public void setSintemas(String sintomas) { this.sintomas = sintomas; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
