package com.plant.weighing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS is only needed here for local development (e.g. running `ng serve`
 * on port 4200 against the backend on 8080). In production the Angular
 * build is served from the same origin (embedded in the JAR), so this has
 * no effect at runtime.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
       
				
		registry.addMapping("/api/**")
            .allowedOrigins(
            "http://localhost:4200",
            "https://sap-weighing-station-production.up.railway.app/"
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
