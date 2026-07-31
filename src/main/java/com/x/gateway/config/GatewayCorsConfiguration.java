package com.x.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class GatewayCorsConfiguration {

    private static final List<String> LOCAL_UI_ORIGINS =
            List.of(
                    "http://localhost:5173",
                    "http://127.0.0.1:5173",
                    "http://localhost:3000",
                    "http://127.0.0.1:3000");
    @Bean
    public CorsWebFilter corsWebFilter(
            @Value("${GATEWAY_CORS_ALLOWED_ORIGINS:}") String additionalOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins(additionalOrigins));
        configuration.addAllowedMethod(CorsConfiguration.ALL);
        configuration.addAllowedHeader(CorsConfiguration.ALL);
        configuration.setExposedHeaders(List.of("Location", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }

    static List<String> allowedOrigins(String additionalOrigins) {
        Set<String> origins = new LinkedHashSet<>(LOCAL_UI_ORIGINS);
        if (StringUtils.hasText(additionalOrigins)) {
            for (String origin : additionalOrigins.split(",")) {
                if (StringUtils.hasText(origin)) {
                    origins.add(origin.trim());
                }
            }
        }
        return new ArrayList<>(origins);
    }

}
