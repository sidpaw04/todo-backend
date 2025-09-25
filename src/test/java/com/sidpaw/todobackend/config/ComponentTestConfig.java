package com.sidpaw.todobackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class ComponentTestConfig {

    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate(new RestTemplateBuilder()
            .rootUri("http://localhost:8080"));
    }
}