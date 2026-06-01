package com.rapidaid.api.controller;

import com.rapidaid.api.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserAdminService userAdminService;

    public AdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    private boolean isAuthorized(String apiKey) {
        String expected = System.getenv("ADMIN_API_KEY");
        return expected != null && !expected.isBlank() && expected.equals(apiKey);
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            return ResponseEntity.ok(userAdminService.listUsers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users/{uid}/disable")
    public ResponseEntity<?> disableUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            userAdminService.setUserDisabled(uid, true);
            return ResponseEntity.ok(Map.of("status", "deshabilitado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users/{uid}/enable")
    public ResponseEntity<?> enableUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            userAdminService.setUserDisabled(uid, false);
            return ResponseEntity.ok(Map.of("status", "habilitado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{uid}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String uid,
            @RequestHeader(value = "X-Admin-Key", required = false) String apiKey) {
        if (!isAuthorized(apiKey)) return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        try {
            userAdminService.deleteUser(uid);
            return ResponseEntity.ok(Map.of("status", "eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
