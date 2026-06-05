// Controlador REST para el contenido estático de la app (primeros auxilios y consejos).
// Expone tres endpoints:
//   GET /api/v1/firstaid → devuelve todas las categorías de primeros auxilios
//   GET /api/v1/tips     → devuelve todos los consejos y recomendaciones
//   GET /api/v1/version  → devuelve la versión actual del contenido

package com.rapidaid.api.controller;

import com.rapidaid.api.model.FirstAidResponse;
import com.rapidaid.api.model.TipResponse;
import com.rapidaid.api.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Permite peticiones desde la app Android
public class ContentController {

    // Servicio que carga el contenido desde los archivos JSON
    private final ContentService contentService;

    // Spring inyecta el servicio automáticamente mediante el constructor
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    // GET /api/v1/firstaid
    // Devuelve todas las categorías de primeros auxilios cargadas desde firstaid.json
    @GetMapping("/firstaid")
    public ResponseEntity<List<FirstAidResponse>> getFirstAid() {
        return ResponseEntity.ok(contentService.getFirstAid());
    }

    // GET /api/v1/tips
    // Devuelve todos los consejos cargados desde tips.json
    @GetMapping("/tips")
    public ResponseEntity<List<TipResponse>> getTips() {
        return ResponseEntity.ok(contentService.getTips());
    }

    // GET /api/v1/version
    // Devuelve la versión actual del contenido (número entero guardado en version.json).
    // La app Android lo usa para saber si hay contenido nuevo disponible.
    @GetMapping("/version")
    public ResponseEntity<Map<String, Integer>> getVersion() {
        return ResponseEntity.ok(Map.of("version", contentService.getVersion()));
    }
}
