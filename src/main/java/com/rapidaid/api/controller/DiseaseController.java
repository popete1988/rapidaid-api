// Controlador REST para la búsqueda de enfermedades.
// Expone dos endpoints:
//   GET /api/v1/disease?name={nombre} → busca la enfermedad en el JSON local o en Gemini AI
//   GET /api/v1/health                → comprueba que el servidor está activo

package com.rapidaid.api.controller;

import com.rapidaid.api.model.DiseaseResponse;
import com.rapidaid.api.service.DiseaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// @RestController indica que esta clase maneja peticiones HTTP y devuelve JSON automáticamente
// @RequestMapping("/api/v1") define el prefijo de todas las rutas de este controlador
// @CrossOrigin permite peticiones desde cualquier origen (necesario para la app Android)
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class DiseaseController {

    // Servicio que contiene la lógica de búsqueda
    private final DiseaseService diseaseService;

    // Spring inyecta el servicio automáticamente mediante el constructor
    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    // GET /api/v1/disease?name={nombre}
    // Busca enfermedades por nombre. Primero busca en el JSON local,
    // si no encuentra nada llama a Gemini AI como fallback.
    @GetMapping("/disease")
    public ResponseEntity<?> searchDisease(@RequestParam(name = "name") String name) {

        // Valida que la consulta no esté vacía
        if (name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El parámetro 'name' es obligatorio"));
        }

        // Valida que la consulta no sea demasiado larga (evita abusos)
        if (name.length() > 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El parámetro 'name' no puede superar 100 caracteres"));
        }

        // Delega la búsqueda al servicio y devuelve los resultados
        List<DiseaseResponse> results = diseaseService.search(name.trim());
        return ResponseEntity.ok(results);
    }

    // GET /api/v1/health
    // Endpoint de comprobación de estado. Render usa este endpoint para verificar
    // que el servidor está activo. También lo usa el mecanismo de keep-alive.
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RapidAid Disease API",
                "version", "1.0.0"
        ));
    }
}
