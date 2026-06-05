// Configuración del SDK de administración de Firebase.
// Se inicializa al arrancar el servidor usando las credenciales de la cuenta de servicio
// guardadas en la variable de entorno FIREBASE_SERVICE_ACCOUNT (configurada en Render).
// Sin esta configuración, la gestión de usuarios desde el dashboard no funcionará.

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

    // @PostConstruct indica que este método se ejecuta una vez al arrancar el servidor,
    // después de que Spring haya inyectado todas las dependencias
    @PostConstruct
    public void initialize() {
        try {
            // Lee el JSON de la cuenta de servicio desde la variable de entorno de Render
            String serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT");

            // Si no está configurada, las funciones de admin quedan desactivadas
            if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
                log.warning("FIREBASE_SERVICE_ACCOUNT no configurado - funciones de admin deshabilitadas");
                return;
            }

            // Crea las credenciales a partir del JSON de la cuenta de servicio
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(
                new ByteArrayInputStream(serviceAccountJson.getBytes())
            );

            // Construye las opciones de Firebase con las credenciales
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

            // Solo inicializa si no hay ya una app de Firebase activa (evita duplicados al reiniciar)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK inicializado correctamente");
            }
        } catch (Exception e) {
            log.severe("Error al inicializar Firebase Admin SDK: " + e.getMessage());
        }
    }
}
