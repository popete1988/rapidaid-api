package com.rapidaid.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DiseaseResponse {

    private String name;
    private String description;
    private String symptoms;
    private String treatment;
    private String source;

    public DiseaseResponse() {}

    public DiseaseResponse(String name, String description, String symptoms, String treatment, String source) {
        this.name = name;
        this.description = description;
        this.symptoms = symptoms;
        this.treatment = treatment;
        this.source = source;
    }

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
