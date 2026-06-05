// Clase principal de la aplicación Spring Boot.
// Es el punto de entrada del servidor — arranca el servidor embebido Tomcat
// y carga todos los componentes de Spring automáticamente.

package com.rapidaid.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// @SpringBootApplication activa la configuración automática de Spring Boot
// @EnableCaching activa el sistema de caché (usado para cachear búsquedas de enfermedades)
@SpringBootApplication
@EnableCaching
public class RapidAidApiApplication {

    // Punto de entrada de la aplicación — Spring Boot arranca aquí
    public static void main(String[] args) {
        SpringApplication.run(RapidAidApiApplication.class, args);
    }
}
