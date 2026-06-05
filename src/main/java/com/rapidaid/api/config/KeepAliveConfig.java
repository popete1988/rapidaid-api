// Configuración del mecanismo de keep-alive del servidor.
// Render (plan gratuito) pone el servidor a dormir tras 15 minutos sin actividad.
// Esta clase envía una petición HTTP al propio servidor cada 10 minutos
// para mantenerlo despierto y evitar el tiempo de arranque en frío.

package com.rapidaid.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import java.util.logging.Logger;

// @EnableScheduling activa las tareas programadas en esta clase
@Configuration
@EnableScheduling
public class KeepAliveConfig {

    private static final Logger log = Logger.getLogger(KeepAliveConfig.class.getName());

    // Cliente HTTP para hacer la petición al propio servidor
    private final RestTemplate restTemplate = new RestTemplate();

    // Puerto del servidor (10000 por defecto en Render)
    @Value("${server.port:10000}")
    private int port;

    // Se ejecuta cada 10 minutos (600.000 ms) desde el último inicio
    // Hace una petición GET al endpoint /api/v1/health para mantener el servidor activo
    @Scheduled(fixedDelay = 600000)
    public void keepAlive() {
        try {
            // Llama al propio servidor para que Render no lo considere inactivo
            restTemplate.getForObject("http://localhost:" + port + "/api/v1/health", String.class);
        } catch (Exception e) {
            log.warning("Keep-alive fallido: " + e.getMessage());
        }
    }
}
