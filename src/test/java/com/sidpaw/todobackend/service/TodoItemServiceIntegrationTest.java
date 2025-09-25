package com.sidpaw.todobackend.service;

import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.exception.InvalidStatusException;
import com.sidpaw.todobackend.model.TodoStatus;
import com.sidpaw.todobackend.repository.TodoItemRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TodoItemServiceIntegrationTest {

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private TodoItemService todoItemService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        todoItemRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void givenNoItems_WhenGetTodoItemsByStatus_ThenReturnsEmptyList() {
        // When
        List<TodoResponseDTO> result = todoItemService.getTodoItemsByStatus("done");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void givenMixedItems_WhenGetNotDoneItems_ThenReturnsOnlyValidNotDoneItems() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        createAndSaveTodoItem("Past due", TodoStatus.NOT_DONE, now.minusDays(1));
        createAndSaveTodoItem("Future due", TodoStatus.NOT_DONE, now.plusDays(1));
        createAndSaveTodoItem("No due date", TodoStatus.NOT_DONE, null);
        createAndSaveTodoItem("Done item", TodoStatus.DONE, null);

        // Ensure all items are persisted
        entityManager.flush();
        entityManager.clear();

        // When
        List<TodoResponseDTO> result = todoItemService.getTodoItemsByStatus("not done");

        // Then
        assertThat(result)
                .hasSize(2)
                .extracting("description")
                .containsExactlyInAnyOrder("Future due", "No due date");
    }

    @Test
    void givenMixedItems_WhenGetDoneItems_ThenReturnsOnlyDoneItems() {
        // Given
        createAndSaveTodoItem("Done 1", TodoStatus.DONE, null);
        createAndSaveTodoItem("Done 2", TodoStatus.DONE, LocalDateTime.now().minusDays(1));
        createAndSaveTodoItem("Not done", TodoStatus.NOT_DONE, null);

        // Ensure all items are persisted
        entityManager.flush();
        entityManager.clear();

        // When
        List<TodoResponseDTO> result = todoItemService.getTodoItemsByStatus("done");

        // Then
        assertThat(result)
                .hasSize(2)
                .extracting("description")
                .containsExactlyInAnyOrder("Done 1", "Done 2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"DONE", "Done", "done", "NOT DONE", "Not Done", "not done"})
    void givenValidStatusInDifferentCases_WhenGetTodoItems_ThenAcceptsAllFormats(String status) {
        // Given
        createAndSaveTodoItem("Test item", TodoStatus.DONE, null);
        entityManager.flush();
        entityManager.clear();

        // When & Then
        List<TodoResponseDTO> result = todoItemService.getTodoItemsByStatus(status);
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "past_due", "in progress", "  "})
    void givenInvalidStatus_WhenGetTodoItems_ThenThrowsException(String invalidStatus) {
        assertThatThrownBy(() -> todoItemService.getTodoItemsByStatus(invalidStatus))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("Invalid status");
    }

    private TodoItemEntity createAndSaveTodoItem(String description, TodoStatus status, LocalDateTime dueDate) {
        TodoItemEntity item = new TodoItemEntity();
        item.setDescription(description);
        item.setStatus(status);
        item.setDueDatetime(dueDate);
        item.setCreationDatetime(LocalDateTime.now());
        return todoItemRepository.save(item);
    }
}