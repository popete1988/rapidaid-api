package com.rapidaid.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.logging.Logger;

@Configuration
@EnableScheduling
public class KeepAliveConfig {

    private static final Logger log = Logger.getLogger(KeepAliveConfig.class.getName());

    /**
     * Ping ourselves every 10 minutes to prevent Render free tier from sleeping.
     * Render sleeps after 15 minutes of inactivity.
     */
    @Scheduled(fixedDelay = 600000) // Every 10 minutes
    public void keepAlive() {
        log.info("Keep-alive ping");
    }
}
