// Modelo de respuesta para una categoría de Primeros Auxilios.
// Representa el JSON que devuelve el servidor desde firstaid.json,
// incluyendo todos los subtipos que pertenecen a esa categoría.

package com.rapidaid.api.model;

import java.util.List;

public class FirstAidResponse {
    private String id;                   // Identificador único (ej. "quemaduras")
    private String title;                // Nombre de la categoría (ej. "Quemaduras")
    private String color;                // Color de la tarjeta en la app (ej. "red", "blue")
    private String imageUrl;             // URL de imagen opcional asignada desde el dashboard
    private List<SubtypeResponse> subtypes; // Lista de subtipos que pertenecen a esta categoría

    // Constructor vacío requerido por Jackson para deserializar el JSON
    public FirstAidResponse() {}

    // Getters y setters requeridos por Jackson para serializar/deserializar el objeto a JSON
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
