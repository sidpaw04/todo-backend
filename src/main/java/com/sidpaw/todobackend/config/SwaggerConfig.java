package com.sidpaw.todobackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiDocumentation() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(List.of(createDevelopmentServer()));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Todo List Service API")
                .version("0.0.1-SNAPSHOT")
                .description("A simple and efficient todo list backend service API")
                .contact(createTeamContact())
                .license(createApacheLicense());
    }

    private Server createDevelopmentServer() {
        return new Server()
                .url("http://localhost:8080")
                .description("Development server");
    }

    private Contact createTeamContact() {
        return new Contact()
                .name("Todo List Service Team")
                .email("developer@sidpaw.com");
    }

    private License createApacheLicense() {
        return new License()
                .name("Apache License 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");
    }
}
