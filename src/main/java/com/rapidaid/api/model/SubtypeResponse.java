// Modelo de respuesta para un subtipo de Primeros Auxilios.
// Representa un caso concreto dentro de una categoría
// (ej. "Quemadura de primer grado" dentro de "Quemaduras").

package com.rapidaid.api.model;

public class SubtypeResponse {
    private String name;        // Nombre del subtipo (ej. "Quemadura de primer grado")
    private String sintomas;    // Descripción de los síntomas
    private String tratamiento; // Descripción del tratamiento recomendado
    private String imageUrl;    // URL de imagen opcional asignada desde el dashboard

    // Constructor vacío requerido por Jackson para deserializar el JSON
    public SubtypeResponse() {}

    // Getters y setters requeridos por Jackson para serializar/deserializar el objeto a JSON
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
