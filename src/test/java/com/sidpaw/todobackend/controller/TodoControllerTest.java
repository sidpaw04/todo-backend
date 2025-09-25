package com.sidpaw.todobackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sidpaw.todobackend.dto.TodoPatchDTO;
import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.exception.InvalidStatusException;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private ObjectMapper objectMapper;

    @Autowired
    private TodoItemService todoItemService;

    private TodoRequestDTO validRequest;
    private TodoResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        validRequest = new TodoRequestDTO(
                "Complete project documentation",
                LocalDateTime.of(2025, 12, 31, 23, 59)
        );

        expectedResponse = new TodoResponseDTO(
                1L,
                "Complete project documentation",
                "not done",
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
                .andExpect(jsonPath("$.status").value("not done"));
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
                new TodoResponseDTO(2L, "Another task", "done",
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

    @Test
    void givenValidTodoIdAndDescription_WhenPatchTodoItem_ThenReturnsUpdatedTodo() throws Exception {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setDescription("Updated description");

        TodoResponseDTO patchedResponse = new TodoResponseDTO(
                1L,
                "Updated description",
                "not done",
                LocalDateTime.of(2025, 9, 23, 10, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59),
                null
        );

        when(todoItemService.patchTodo(eq(1L), any(TodoPatchDTO.class)))
                .thenReturn(Optional.of(patchedResponse));

        // When & Then
        mockMvc.perform(patch("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("not done"));
    }

    @Test
    void givenValidTodoIdAndStatus_WhenPatchTodoItem_ThenReturnsUpdatedTodo() throws Exception {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setStatus("done");

        TodoResponseDTO patchedResponse = new TodoResponseDTO(
                1L,
                "Complete project documentation",
                "done",
                LocalDateTime.of(2025, 9, 23, 10, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59),
                LocalDateTime.now()
        );

        when(todoItemService.patchTodo(eq(1L), any(TodoPatchDTO.class)))
                .thenReturn(Optional.of(patchedResponse));

        // When & Then
        mockMvc.perform(patch("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"done\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("done"));
    }

    @Test
    void givenPastDueStatus_WhenPatchTodoItem_ThenReturnsBadRequest() throws Exception {
        // Given
        TodoItemEntity existingTodo = new TodoItemEntity();
        existingTodo.setId(1L);
        existingTodo.setDescription("Original description");
        existingTodo.setStatus(TodoStatus.NOT_DONE);
        existingTodo.setCreationDatetime(LocalDateTime.now());

        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setStatus("past due");

        when(todoItemService.patchTodo(eq(1L), any(TodoPatchDTO.class)))
                .thenThrow(new InvalidStatusException("Status can only be set to 'done' or 'not done'"));

        // When & Then
        mockMvc.perform(patch("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"past due\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status"))
                .andExpect(jsonPath("$.message").value("Status can only be set to 'done' or 'not done'"));
    }

    @Test
    void givenInvalidTodoId_WhenPatchTodoItem_ThenReturnsBadRequest() throws Exception {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setDescription("Updated description");

        // When & Then
        mockMvc.perform(patch("/api/todos/invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistingTodoId_WhenPatchTodoItem_ThenReturnsNotFound() throws Exception {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setDescription("Updated description");

        when(todoItemService.patchTodo(eq(999L), any(TodoPatchDTO.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/api/todos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenTooLongDescription_WhenPatchTodoItem_ThenReturnsBadRequest() throws Exception {
        // Given - description exceeds 1000 characters
        String longDescription = "a".repeat(1001);
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setDescription(longDescription);

        // When & Then
        mockMvc.perform(patch("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidStatus_WhenPatchTodoItem_ThenReturnsBadRequest() throws Exception {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setStatus("invalid status");

        when(todoItemService.patchTodo(eq(1L), any(TodoPatchDTO.class)))
                .thenThrow(new InvalidStatusException("Status can only be set to 'done' or 'not done'"));

        // When & Then
        mockMvc.perform(patch("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status"))
                .andExpect(jsonPath("$.message").value("Status can only be set to 'done' or 'not done'"));
    }

    @Test
    void givenEmptyPatchBody_WhenPatchTodoItem_ThenReturnsUnmodifiedTodo() throws Exception {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();

        when(todoItemService.patchTodo(eq(1L), any(TodoPatchDTO.class)))
                .thenReturn(Optional.of(expectedResponse));

        // When & Then
        mockMvc.perform(patch("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Complete project documentation"))
                .andExpect(jsonPath("$.status").value("not done"));
    }

    @Test
    void givenValidStatus_WhenGetTodoItems_ThenReturnsFilteredItems() throws Exception {
        // Given
        List<TodoResponseDTO> items = List.of(
            new TodoResponseDTO(1L, "Task 1", "not done", LocalDateTime.now(), null, null),
            new TodoResponseDTO(2L, "Task 2", "not done", LocalDateTime.now(), null, null)
        );
        
        when(todoItemService.getTodoItemsByStatus("not done")).thenReturn(items);

        // When
        MvcResult result = mockMvc.perform(get("/api/todos")
                .param("status", "not done"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        List<TodoResponseDTO> responseItems = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, TodoResponseDTO.class)
        );

        assertThat(responseItems)
            .hasSize(2)
            .extracting("status")
            .containsOnly("not done");
    }

    @Test
    void givenInvalidStatus_WhenGetTodoItems_ThenReturnsBadRequest() throws Exception {
        // Given
        when(todoItemService.getTodoItemsByStatus("invalid"))
            .thenThrow(new InvalidStatusException("Invalid status value: 'invalid'"));

        // When
        MvcResult result = mockMvc.perform(get("/api/todos")
                .param("status", "invalid"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody)
            .contains("Invalid Status")
            .contains("Invalid status value: 'invalid'");
    }

    @Test
    void givenNoStatusParameter_WhenGetTodoItems_ThenReturnsAllItems() throws Exception {
        // Given
        List<TodoResponseDTO> items = List.of(
            new TodoResponseDTO(1L, "Task 1", "done", LocalDateTime.now(), null, null),
            new TodoResponseDTO(2L, "Task 2", "not done", LocalDateTime.now(), null, null)
        );
        
        when(todoItemService.getAllTodoItems()).thenReturn(items);

        // When
        MvcResult result = mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        List<TodoResponseDTO> responseItems = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, TodoResponseDTO.class)
        );

        assertThat(responseItems).hasSize(2);
        assertThat(responseItems)
            .extracting("status")
            .containsExactlyInAnyOrder("done", "not done");
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
