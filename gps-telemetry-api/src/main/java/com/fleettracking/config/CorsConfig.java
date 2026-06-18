package com.fleettracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Habilita CORS para que el frontend (Next.js, que corre en localhost:3000)
 * pueda hacer peticiones HTTP a esta API (que corre en localhost:8080).
 *
 * Sin esto, el navegador bloquea las peticiones del frontend por política
 * de same-origin, aunque Postman/curl funcionen bien (esas herramientas no
 * aplican esa restricción, por eso el problema solo se nota al conectar el
 * frontend real).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
                .allowedHeaders("*");
    }
}
