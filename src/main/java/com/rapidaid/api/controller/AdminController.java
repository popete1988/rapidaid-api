// Controlador REST para la gestión de usuarios desde el dashboard de administración.
// Todos los endpoints requieren la cabecera X-Admin-Key con la clave configurada
// en la variable de entorno ADMIN_API_KEY de Render.

package com.rapidaid.api.controller;

import com.rapidaid.api.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "https://rapidaid-api.onrender.com") // Solo permite peticiones desde el propio dashboard
public class AdminController {

    private static final Logger log = Logger.getLogger(AdminController.class.getName());
    private final UserAdminService userAdminService;

    public AdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    // Comprueba si la clave de administración es válida comparándola con la variable de entorno
    private boolean isAuthorized(String apiKey) {
        String expected = System.getenv("ADMIN_API_KEY");
        return expected != null && !expected.isBlank() && expected.equals(apiKey);
    }

    // Valida que el UID tiene el formato correcto de Firebase (20-30 caracteres alfanuméricos)
    private boolean isValidUid(String uid) {
        return uid != null && uid.matches("[a-zA-Z0-9]{20,30}");
    }

    // GET /api/v1/admin/users — lista todos los usuarios de Firebase
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            return ResponseEntity.ok(userAdminService.listUsers());
        } catch (Exception e) {
            log.severe("Error listando usuarios: " + e.getMessage()); // Log interno, no expuesto al cliente
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    // POST /api/v1/admin/users/{uid}/disable — deshabilita un usuario
    @PostMapping("/users/{uid}/disable")
    public ResponseEntity<?> disableUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        if (!isValidUid(uid)) return ResponseEntity.badRequest().body(Map.of("error", "UID no válido"));
        try {
            userAdminService.setUserDisabled(uid, true);
            return ResponseEntity.ok(Map.of("status", "deshabilitado"));
        } catch (Exception e) {
            log.severe("Error deshabilitando usuario " + uid + ": " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    // POST /api/v1/admin/users/{uid}/enable — habilita un usuario
    @PostMapping("/users/{uid}/enable")
    public ResponseEntity<?> enableUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        if (!isValidUid(uid)) return ResponseEntity.badRequest().body(Map.of("error", "UID no válido"));
        try {
            userAdminService.setUserDisabled(uid, false);
            return ResponseEntity.ok(Map.of("status", "habilitado"));
        } catch (Exception e) {
            log.severe("Error habilitando usuario " + uid + ": " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    // DELETE /api/v1/admin/users/{uid} — elimina permanentemente un usuario
    @DeleteMapping("/users/{uid}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        if (!isValidUid(uid)) return ResponseEntity.badRequest().body(Map.of("error", "UID no válido"));
        try {
            userAdminService.deleteUser(uid);
            return ResponseEntity.ok(Map.of("status", "eliminado"));
        } catch (Exception e) {
            log.severe("Error eliminando usuario " + uid + ": " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }
}
