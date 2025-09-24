package com.sidpaw.todobackend;

import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.model.TodoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TodoItemEntity.
 */
class TodoItemEntityTest {

    private TodoItemEntity todoItem;

    @BeforeEach
    void setUp() {
        todoItem = new TodoItemEntity();
        todoItem.setId(1L);
        todoItem.setDescription("Test task");
        todoItem.setStatus(TodoStatus.NOT_DONE);
        todoItem.setCreationDatetime(LocalDateTime.of(2025, 9, 23, 10, 0));
        todoItem.setDueDatetime(LocalDateTime.of(2025, 12, 31, 23, 59));
    }


    @Test
    void givenDescriptionAndDueDate_WhenCreateTodoItemWithConstructor_ThenSetsDefaultValues() {
        // Given
        String description = "New task";
        LocalDateTime dueDate = LocalDateTime.of(2025, 12, 31, 23, 59);

        // When
        TodoItemEntity newTodoItem = new TodoItemEntity(description, dueDate);

        // Then
        assertThat(newTodoItem.getDescription()).isEqualTo(description);
        assertThat(newTodoItem.getDueDatetime()).isEqualTo(dueDate);
        assertThat(newTodoItem.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(newTodoItem.getCreationDatetime()).isNotNull();
        assertThat(newTodoItem.getCreationDatetime()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(newTodoItem.getId()).isNull();
        assertThat(newTodoItem.getDoneDatetime()).isNull();
    }

    @Test
    void givenDescriptionAndNullDueDate_WhenCreateTodoItemWithConstructor_ThenSetsDefaultValues() {
        // Given
        String description = "Task without due date";

        // When
        TodoItemEntity newTodoItem = new TodoItemEntity(description, null);

        // Then
        assertThat(newTodoItem.getDescription()).isEqualTo(description);
        assertThat(newTodoItem.getDueDatetime()).isNull();
        assertThat(newTodoItem.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(newTodoItem.getCreationDatetime()).isNotNull();
    }

    @Test
    void givenTodoItem_WhenUsingAllArgsConstructor_ThenAllFieldsAreSet() {
        // Given
        Long id = 2L;
        String description = "Constructor test task";
        TodoStatus status = TodoStatus.DONE;
        LocalDateTime creationDatetime = LocalDateTime.of(2025, 9, 20, 8, 0);
        LocalDateTime dueDatetime = LocalDateTime.of(2025, 10, 15, 17, 0);
        LocalDateTime doneDatetime = LocalDateTime.of(2025, 9, 25, 14, 30);

        // When
        TodoItemEntity todoItem = new TodoItemEntity(id, description, status, creationDatetime, dueDatetime, doneDatetime);

        // Then
        assertThat(todoItem.getId()).isEqualTo(id);
        assertThat(todoItem.getDescription()).isEqualTo(description);
        assertThat(todoItem.getStatus()).isEqualTo(status);
        assertThat(todoItem.getCreationDatetime()).isEqualTo(creationDatetime);
        assertThat(todoItem.getDueDatetime()).isEqualTo(dueDatetime);
        assertThat(todoItem.getDoneDatetime()).isEqualTo(doneDatetime);
    }

    @Test
    void givenTwoTodoItemsWithSameData_WhenCompareEquals_ThenAreEqual() {
        // Given
        TodoItemEntity todoItem1 = new TodoItemEntity();
        todoItem1.setId(1L);
        todoItem1.setDescription("Same task");
        todoItem1.setStatus(TodoStatus.NOT_DONE);

        TodoItemEntity todoItem2 = new TodoItemEntity();
        todoItem2.setId(1L);
        todoItem2.setDescription("Same task");
        todoItem2.setStatus(TodoStatus.NOT_DONE);

        // When & Then
        assertThat(todoItem1).isEqualTo(todoItem2);
        assertThat(todoItem1.hashCode()).isEqualTo(todoItem2.hashCode());
    }

    @Test
    void givenTodoItem_WhenCallToString_ThenReturnsNonNullString() {
        // When
        String result = todoItem.toString();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("Test task");
        assertThat(result).contains("not done"); // Status toString returns display name
    }
}
