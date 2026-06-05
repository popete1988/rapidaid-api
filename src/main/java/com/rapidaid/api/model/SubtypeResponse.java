package com.rapidaid.api.model;

public class SubtypeResponse {
    private String name;
    private String sintomas;
    private String tratamiento;
    private String imageUrl;

    public SubtypeResponse() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
