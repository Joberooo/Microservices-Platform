package com.example.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void shouldConfigureBearerAuthSecurityScheme() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.api();

        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey("bearerAuth");

        var scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");

        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }

    @Test
    void shouldConfigureApiServerUrl() {
        OpenApiConfig config = new OpenApiConfig();

        OpenAPI openAPI = config.api();

        assertThat(openAPI.getServers()).isNotEmpty();
        assertThat(openAPI.getServers().getFirst().getUrl())
                .isEqualTo("/api");
    }
}
