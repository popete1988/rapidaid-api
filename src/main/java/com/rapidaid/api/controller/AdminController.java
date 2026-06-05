// Controlador REST para la gestión de usuarios desde el dashboard de administración.
// Todos los endpoints requieren la cabecera X-Admin-Key con la clave configurada
// en la variable de entorno ADMIN_API_KEY de Render.
// Expone cuatro operaciones:
//   GET    /api/v1/admin/users          → lista todos los usuarios de Firebase
//   POST   /api/v1/admin/users/{uid}/disable → deshabilita un usuario
//   POST   /api/v1/admin/users/{uid}/enable  → habilita un usuario
//   DELETE /api/v1/admin/users/{uid}    → elimina permanentemente un usuario

package com.rapidaid.api.controller;

import com.rapidaid.api.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*") // Permite peticiones desde el dashboard web
public class AdminController {

    // Servicio que se comunica con Firebase Admin SDK
    private final UserAdminService userAdminService;

    // Spring inyecta el servicio automáticamente mediante el constructor
    public AdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    // Comprueba si la clave de administración es válida comparándola con la variable de entorno
    private boolean isAuthorized(String apiKey) {
        String expected = System.getenv("ADMIN_API_KEY"); // Lee la clave secreta de Render
        return expected != null && !expected.isBlank() && expected.equals(apiKey);
    }

    // GET /api/v1/admin/users
    // Devuelve la lista de todos los usuarios registrados en Firebase Auth
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            return ResponseEntity.ok(userAdminService.listUsers()); // Obtiene usuarios de Firebase
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/v1/admin/users/{uid}/disable
    // Deshabilita la cuenta del usuario con el UID indicado (no la elimina)
    @PostMapping("/users/{uid}/disable")
    public ResponseEntity<?> disableUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            userAdminService.setUserDisabled(uid, true); // Deshabilita el usuario en Firebase
            return ResponseEntity.ok(Map.of("status", "deshabilitado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/v1/admin/users/{uid}/enable
    // Vuelve a habilitar la cuenta de un usuario previamente deshabilitado
    @PostMapping("/users/{uid}/enable")
    public ResponseEntity<?> enableUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            userAdminService.setUserDisabled(uid, false); // Habilita el usuario en Firebase
            return ResponseEntity.ok(Map.of("status", "habilitado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/v1/admin/users/{uid}
    // Elimina permanentemente la cuenta del usuario con el UID indicado
    @DeleteMapping("/users/{uid}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            userAdminService.deleteUser(uid); // Elimina el usuario de Firebase
            return ResponseEntity.ok(Map.of("status", "eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
