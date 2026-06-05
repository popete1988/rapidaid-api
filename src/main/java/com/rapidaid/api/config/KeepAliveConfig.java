package com.rapidaid.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import java.util.logging.Logger;

@Configuration
@EnableScheduling
public class KeepAliveConfig {

    private static final Logger log = Logger.getLogger(KeepAliveConfig.class.getName());
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.port:10000}")
    private int port;

    // Ping cada 10 minutos para evitar que Render duerma el servicio
    @Scheduled(fixedDelay = 600000)
    public void keepAlive() {
        try {
            restTemplate.getForObject("http://localhost:" + port + "/api/v1/health", String.class);
        } catch (Exception e) {
            log.warning("Keep-alive fallido: " + e.getMessage());
        }
    }
}
