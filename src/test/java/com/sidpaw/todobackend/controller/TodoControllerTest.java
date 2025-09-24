package com.sidpaw.todobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.model.TodoStatus;
import com.sidpaw.todobackend.service.TodoItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for TodoController using @WebMvcTest.
 * Tests the web layer with mocked service dependencies using @TestConfiguration.
 */
@WebMvcTest(TodoController.class)
@Import(TodoControllerTest.TestConfig.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoItemService todoItemService;

    private ObjectMapper objectMapper;
    private TodoRequestDTO validRequest;
    private TodoResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validRequest = new TodoRequestDTO(
                "Complete project documentation",
                LocalDateTime.of(2025, 12, 31, 23, 59)
        );

        expectedResponse = new TodoResponseDTO(
                1L,
                "Complete project documentation",
                TodoStatus.NOT_DONE,
                LocalDateTime.of(2025, 9, 23, 10, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59),
                null
        );
    }

    @Test
    void givenValidRequest_WhenCreateTodoItem_ThenReturnsCreatedTodo() throws Exception {
        // Given
        when(todoItemService.createTodoItem(any(TodoRequestDTO.class)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Complete project documentation"))
                .andExpect(jsonPath("$.status").value("NOT_DONE"));
    }

    @Test
    void givenInvalidRequest_WhenCreateTodoItem_ThenReturnsBadRequest() throws Exception {
        // Given - invalid request with blank description
        TodoRequestDTO invalidRequest = new TodoRequestDTO("", null);

        // When & Then
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenTodoItemsExist_WhenGetAllTodoItems_ThenReturnsAllTodos() throws Exception {
        // Given
        List<TodoResponseDTO> expectedTodos = Arrays.asList(
                expectedResponse,
                new TodoResponseDTO(2L, "Another task", TodoStatus.DONE,
                        LocalDateTime.of(2025, 9, 22, 9, 0),
                        LocalDateTime.of(2025, 9, 25, 17, 0),
                        LocalDateTime.of(2025, 9, 24, 16, 30))
        );

        when(todoItemService.getAllTodoItems()).thenReturn(expectedTodos);

        // When & Then
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void givenNoTodoItems_WhenGetAllTodoItems_ThenReturnsEmptyList() throws Exception {
        // Given
        when(todoItemService.getAllTodoItems()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenExistingTodoId_WhenGetTodoItemById_ThenReturnsTodo() throws Exception {
        // Given
        when(todoItemService.getTodoItemById(eq(1L)))
                .thenReturn(Optional.of(expectedResponse));

        // When & Then
        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Complete project documentation"));
    }

    @Test
    void givenNonExistingTodoId_WhenGetTodoItemById_ThenReturnsNotFound() throws Exception {
        // Given
        when(todoItemService.getTodoItemById(eq(999L)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/todos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenInvalidTodoId_WhenGetTodoItemById_ThenReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/todos/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenTooLongDescription_WhenCreateTodoItem_ThenReturnsBadRequest() throws Exception {
        // Given - description exceeds 1000 characters
        String longDescription = "a".repeat(1001);
        TodoRequestDTO invalidRequest = new TodoRequestDTO(longDescription, null);

        // When & Then
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNullDescription_WhenCreateTodoItem_ThenReturnsBadRequest() throws Exception {
        // Given
        TodoRequestDTO invalidRequest = new TodoRequestDTO(null, null);

        // When & Then
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test configuration that provides a mock TodoItemService bean.
     * This replaces the deprecated @MockBean approach with a modern @TestConfiguration.
     */
    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public TodoItemService todoItemService() {
            return Mockito.mock(TodoItemService.class);
        }
    }
}
