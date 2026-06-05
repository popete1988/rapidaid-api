// Modelo de respuesta para un consejo o recomendación de salud.
// Representa el JSON de cada elemento dentro de tips.json.

package com.rapidaid.api.model;

public class TipResponse {
    private String id;        // Identificador único del consejo (ej. "ciclismo")
    private String title;     // Título del consejo (ej. "Ciclismo")
    private String contenido; // Texto completo del consejo, con párrafos separados por saltos de línea
    private String imageUrl;  // URL de imagen opcional asignada desde el dashboard

    // Constructor vacío requerido por Jackson para deserializar el JSON
    public TipResponse() {}

    // Getters y setters requeridos por Jackson para serializar/deserializar el objeto a JSON
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
