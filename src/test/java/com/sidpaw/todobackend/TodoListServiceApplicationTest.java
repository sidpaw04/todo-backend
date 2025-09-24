package com.sidpaw.todobackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to verify the Spring Boot application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class TodoListServiceApplicationTest {

    @Test
    void givenSpringBootApplication_WhenContextLoads_ThenSuccess() {
        // This test will pass if the Spring application context loads successfully.
        // The @SpringBootTest annotation will start the entire Spring Boot application context.
    }
}
