// Configuración del sistema de caché del servidor.
// Usa Caffeine (caché en memoria) para guardar los resultados de búsquedas de enfermedades
// durante 24 horas, evitando llamadas repetidas al JSON o a Gemini AI.

package com.rapidaid.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

// @Configuration indica que esta clase contiene configuración de Spring
@Configuration
public class CacheConfig {

    // Crea y registra el gestor de caché como bean de Spring
    // @Bean hace que Spring gestione el ciclo de vida de este objeto
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("diseases"); // Nombre de la caché
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS) // Los resultados se borran tras 24 horas
                        .maximumSize(500)                      // Máximo 500 enfermedades distintas en caché
        );
        return manager;
    }
}
