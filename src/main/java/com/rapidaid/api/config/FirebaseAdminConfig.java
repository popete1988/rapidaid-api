package com.rapidaid.api.config;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.io.ByteArrayInputStream;
import java.util.logging.Logger;

@Configuration
public class FirebaseAdminConfig {

    private static final Logger log = Logger.getLogger(FirebaseAdminConfig.class.getName());

    @PostConstruct
    public void initialize() {
        try {
            String serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT");
            if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
                log.warning("FIREBASE_SERVICE_ACCOUNT no configurado - funciones de admin deshabilitadas");
                return;
            }
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(
                new ByteArrayInputStream(serviceAccountJson.getBytes())
            );
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK inicializado correctamente");
            }
        } catch (Exception e) {
            log.severe("Error al inicializar Firebase Admin SDK: " + e.getMessage());
        }
    }
}