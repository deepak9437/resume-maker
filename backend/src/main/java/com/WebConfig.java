package com.resumemaker;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * This is the development configuration for CORS.
     * It allows all origins, all methods, and all headers.
     * This is the fix for the "blocked by CORS policy" error.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Allow all paths, not just /api/**
                .allowedOrigins("*")   // Allow all origins (e.E. http://127.0.0.1:3000)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // *Explicitly* allow OPTIONS
                .allowedHeaders("*");
    }
}

