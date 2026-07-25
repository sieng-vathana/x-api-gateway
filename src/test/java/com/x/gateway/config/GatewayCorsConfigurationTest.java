package com.x.gateway.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

class GatewayCorsConfigurationTest {

    private final WebTestClient webTestClient = webTestClient();

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:5173", "http://localhost:3000"})
    void acceptsConfiguredLocalUiOrigins(String origin) {
        webTestClient.options()
                .uri("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,x-client-type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin)
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:4173", "https://malicious.example"})
    void rejectsUnconfiguredOrigins(String origin) {
        webTestClient.options()
                .uri("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST.name())
                .exchange()
                .expectStatus().isForbidden();
    }

    private static WebTestClient webTestClient() {
        GatewayCorsConfiguration configuration = new GatewayCorsConfiguration();
        CorsWebFilter filter = configuration.corsWebFilter(
                List.of("http://localhost:5173", "http://localhost:3000"));

        return WebTestClient.bindToWebHandler(exchange -> {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    return response.setComplete();
                })
                .webFilter(filter)
                .build();
    }
}
