package com.rapidaid.api.controller;

import com.rapidaid.api.model.DiseaseResponse;
import com.rapidaid.api.service.DiseaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Allow requests from Android app
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    /**
     * Search diseases by name.
     * GET /api/v1/disease?name=meningitis
     *
     * Returns JSON array:
     * [
     *   {
     *     "name": "Meningitis",
     *     "description": "La meningitis es...",
     *     "symptoms": "Fiebre, dolor de cabeza...",
     *     "treatment": "El tratamiento incluye...",
     *     "source": "MedlinePlus NLM"
     *   }
     * ]
     */
    @GetMapping("/disease")
    public ResponseEntity<?> searchDisease(@RequestParam(name = "name") String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El parámetro 'name' es obligatorio"));
        }

        if (name.length() > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El parámetro 'name' no puede superar 100 caracteres"));
        }

        List<DiseaseResponse> results = diseaseService.search(name.trim());
        return ResponseEntity.ok(results);
    }

    /**
     * Health check endpoint — Railway uses this to verify the service is running.
     * GET /api/v1/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RapidAid Disease API",
                "version", "1.0.0"
        ));
    }
}
