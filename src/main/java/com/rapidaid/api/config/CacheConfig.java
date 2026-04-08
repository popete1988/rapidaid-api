package com.rapidaid.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    /**
     * Cache disease results for 24 hours.
     * This means "meningitis" searched at 10am will be cached until 10am next day.
     * Reduces external API calls and makes the app faster for repeated searches.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("diseases");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(500) // Cache up to 500 different diseases
        );
        return manager;
    }
}
