// Modelo de respuesta para una enfermedad.
// Es el objeto que devuelve el servidor al buscar una enfermedad por nombre.
// @JsonInclude(NON_EMPTY) hace que los campos vacíos no aparezcan en el JSON de respuesta.

package com.rapidaid.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

// Solo incluye en el JSON los campos que tienen valor (evita "symptoms": "" en la respuesta)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DiseaseResponse {

    private String name;        // Nombre de la enfermedad
    private String description; // Descripción general
    private String symptoms;    // Síntomas principales
    private String treatment;   // Tratamiento recomendado
    private String source;      // Fuente del resultado (ej. "Gemini AI", "MedlinePlus")

    // Constructor vacío requerido por Jackson para deserializar JSON
    public DiseaseResponse() {}

    // Constructor completo usado al crear la respuesta desde DiseaseService o GeminiService
    public DiseaseResponse(String name, String description, String symptoms, String treatment, String source) {
        this.name = name;
        this.description = description;
        this.symptoms = symptoms;
        this.treatment = treatment;
        this.source = source;
    }

    // Getters y setters — requeridos por Jackson para serializar/deserializar el objeto a JSON
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
