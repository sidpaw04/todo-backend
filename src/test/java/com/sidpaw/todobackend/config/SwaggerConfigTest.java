package com.sidpaw.todobackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    void setUp() {
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    void apiDocumentation_shouldReturnConfiguredOpenAPI() {
        OpenAPI result = swaggerConfig.apiDocumentation();

        assertThat(result).isNotNull();
        assertThat(result.getInfo()).isNotNull();
        assertThat(result.getInfo().getTitle()).isEqualTo("Todo List Service API");
        assertThat(result.getInfo().getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(result.getInfo().getDescription()).contains("A simple and efficient todo list backend service API");
        
        assertThat(result.getInfo().getContact()).isNotNull();
        assertThat(result.getInfo().getContact().getName()).isEqualTo("Todo List Service Team");
        assertThat(result.getInfo().getContact().getEmail()).isEqualTo("developer@sidpaw.com");
        
        assertThat(result.getInfo().getLicense()).isNotNull();
        assertThat(result.getInfo().getLicense().getName()).isEqualTo("Apache License 2.0");
        assertThat(result.getInfo().getLicense().getUrl()).isEqualTo("https://www.apache.org/licenses/LICENSE-2.0");
        
        assertThat(result.getServers()).hasSize(1);
        assertThat(result.getServers().get(0).getUrl()).isEqualTo("http://localhost:8080");
        assertThat(result.getServers().get(0).getDescription()).isEqualTo("Development server");
    }
}
