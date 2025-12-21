package com.example.logging_and_observability.config;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Permettre les requêtes depuis Angular
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        // Permettre tous les headers HTTP standards + nos headers personnalisés
        config.setAllowedHeaders(// ✅ AJOUTER
        // ✅ AJOUTER pour OpenTelemetry
        // ✅ AJOUTER pour OpenTelemetry// ✅ AJOUTER
        Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-User-Name", "X-User-Email", "traceparent", "tracestate"));
        // Permettre toutes les méthodes HTTP
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permettre les credentials (cookies, authorization headers)
        config.setAllowCredentials(true);
        // Appliquer la configuration à tous les endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}