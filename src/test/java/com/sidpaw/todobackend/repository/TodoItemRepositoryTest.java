package com.sidpaw.todobackend.repository;

import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.model.TodoStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TodoItemRepository using @DataJpaTest.
 */
@DataJpaTest
@ActiveProfiles("test")
class TodoItemRepositoryTest {

    @Autowired
    private TodoItemRepository todoItemRepository;

    private TodoItemEntity todoItem1;
    private TodoItemEntity todoItem2;
    private TodoItemEntity todoItem3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        todoItemRepository.deleteAll();

        todoItem1 = new TodoItemEntity();
        todoItem1.setDescription("First task");
        todoItem1.setStatus(TodoStatus.NOT_DONE);
        todoItem1.setCreationDatetime(LocalDateTime.of(2025, 9, 23, 10, 0));
        todoItem1.setDueDatetime(LocalDateTime.of(2025, 12, 31, 23, 59));

        todoItem2 = new TodoItemEntity();
        todoItem2.setDescription("Second task");
        todoItem2.setStatus(TodoStatus.DONE);
        todoItem2.setCreationDatetime(LocalDateTime.of(2025, 9, 22, 9, 0));
        todoItem2.setDueDatetime(LocalDateTime.of(2025, 9, 25, 17, 0));
        todoItem2.setDoneDatetime(LocalDateTime.of(2025, 9, 24, 16, 30));

        todoItem3 = new TodoItemEntity();
        todoItem3.setDescription("Third task");
        todoItem3.setStatus(TodoStatus.NOT_DONE);
        todoItem3.setCreationDatetime(LocalDateTime.of(2025, 9, 21, 8, 0));
        todoItem3.setDueDatetime(LocalDateTime.of(2025, 10, 15, 12, 0));
    }

    @Test
    void givenNewTodoItem_WhenSave_ThenItemIsPersisted() {
        // When
        TodoItemEntity saved = todoItemRepository.save(todoItem1);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("First task");
        assertThat(saved.getStatus()).isEqualTo(TodoStatus.NOT_DONE);

        // Verify it's actually in the database
        TodoItemEntity found = todoItemRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getDescription()).isEqualTo("First task");
    }

    @Test
    void givenExistingTodoItem_WhenFindById_ThenReturnsTodoItem() {
        // Given
        TodoItemEntity saved = todoItemRepository.save(todoItem1);

        // When
        Optional<TodoItemEntity> result = todoItemRepository.findById(saved.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("First task");
        assertThat(result.get().getStatus()).isEqualTo(TodoStatus.NOT_DONE);
    }

    @Test
    void givenNonExistentId_WhenFindById_ThenReturnsEmpty() {
        // When
        Optional<TodoItemEntity> result = todoItemRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void givenMultipleTodoItems_WhenFindAll_ThenReturnsAllItems() {
        // Given
        todoItemRepository.saveAll(List.of(todoItem1, todoItem2, todoItem3));

        // When
        List<TodoItemEntity> result = todoItemRepository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(TodoItemEntity::getDescription)
                .containsExactlyInAnyOrder("First task", "Second task", "Third task");
    }

    @Test
    void givenMultipleTodoItems_WhenFindAllByOrderByCreationDatetimeDesc_ThenReturnsOrderedList() {
        // Given
        todoItemRepository.saveAll(List.of(todoItem1, todoItem2, todoItem3));

        // When
        List<TodoItemEntity> result = todoItemRepository.findAllByOrderByCreationDatetimeDesc();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDescription()).isEqualTo("First task");  // Most recent
        assertThat(result.get(1).getDescription()).isEqualTo("Second task");
        assertThat(result.get(2).getDescription()).isEqualTo("Third task");  // Oldest
    }

    @Test
    void givenNoTodoItems_WhenFindAllByOrderByCreationDatetimeDesc_ThenReturnsEmptyList() {
        // When
        List<TodoItemEntity> result = todoItemRepository.findAllByOrderByCreationDatetimeDesc();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void givenExistingTodoItem_WhenUpdate_ThenItemIsUpdated() {
        // Given
        TodoItemEntity saved = todoItemRepository.save(todoItem1);

        // When
        saved.setDescription("Updated task description");
        saved.setStatus(TodoStatus.DONE);
        saved.setDoneDatetime(LocalDateTime.now());
        
        TodoItemEntity updated = todoItemRepository.save(saved);

        // Then
        assertThat(updated.getDescription()).isEqualTo("Updated task description");
        assertThat(updated.getStatus()).isEqualTo(TodoStatus.DONE);
        assertThat(updated.getDoneDatetime()).isNotNull();

        // Verify it's actually updated in the database
        TodoItemEntity found = todoItemRepository.findById(saved.getId()).orElse(null);
        Assertions.assertNotNull(found);
        assertThat(found.getDescription()).isEqualTo("Updated task description");
        assertThat(found.getStatus()).isEqualTo(TodoStatus.DONE);
    }

    @Test
    void givenExistingTodoItem_WhenDelete_ThenItemIsRemoved() {
        // Given
        TodoItemEntity saved = todoItemRepository.save(todoItem1);
        Long savedId = saved.getId();

        // When
        todoItemRepository.delete(saved);

        // Then
        TodoItemEntity found = todoItemRepository.findById(savedId).orElse(null);
        assertThat(found).isNull();
    }

    @Test
    void givenTodoItems_WhenCount_ThenReturnsCorrectCount() {
        // Given
        todoItemRepository.saveAll(List.of(todoItem1, todoItem2));

        // When
        long count = todoItemRepository.count();

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void givenNoItems_WhenFindNotDoneItems_ThenReturnsEmptyList() {
        List<TodoItemEntity> result = todoItemRepository.findNotDoneItems(LocalDateTime.now(), TodoStatus.NOT_DONE);
        assertThat(result).isEmpty();
    }

    @Test
    void givenNotDoneItemsWithNoDueDate_WhenFindNotDoneItems_ThenReturnsAllItems() {
        // Given
        TodoItemEntity item1 = createTodoItem("Task 1", TodoStatus.NOT_DONE, null);
        TodoItemEntity item2 = createTodoItem("Task 2", TodoStatus.NOT_DONE, null);
        todoItemRepository.saveAll(List.of(item1, item2));

        // When
        List<TodoItemEntity> result = todoItemRepository.findNotDoneItems(LocalDateTime.now(), TodoStatus.NOT_DONE);

        // Then
        assertThat(result)
            .hasSize(2)
            .extracting("description")
            .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    void givenMixedDueDates_WhenFindNotDoneItems_ThenReturnsFutureAndNullDueDatesOnly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TodoItemEntity pastDue = createTodoItem("Past due", TodoStatus.NOT_DONE, now.minusDays(1));
        TodoItemEntity futureDue = createTodoItem("Future due", TodoStatus.NOT_DONE, now.plusDays(1));
        TodoItemEntity noDueDate = createTodoItem("No due date", TodoStatus.NOT_DONE, null);
        todoItemRepository.saveAll(List.of(pastDue, futureDue, noDueDate));

        // When
        List<TodoItemEntity> result = todoItemRepository.findNotDoneItems(now, TodoStatus.NOT_DONE);

        // Then
        assertThat(result)
            .hasSize(2)
            .extracting("description")
            .containsExactlyInAnyOrder("Future due", "No due date");
    }

    @Test
    void givenDoneItemsWithDifferentCreationDates_WhenFindByStatus_ThenReturnsItemsInDescendingOrder() {
        // Given
        TodoItemEntity older = createTodoItem("Older task", TodoStatus.DONE, null);
        older.setCreationDatetime(LocalDateTime.now().minusDays(2));
        TodoItemEntity newer = createTodoItem("Newer task", TodoStatus.DONE, null);
        newer.setCreationDatetime(LocalDateTime.now().minusDays(1));
        todoItemRepository.saveAll(List.of(older, newer));

        // When
        List<TodoItemEntity> result = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.DONE);

        // Then
        assertThat(result)
            .hasSize(2)
            .extracting("description")
            .containsExactly("Newer task", "Older task");
    }

    private TodoItemEntity createTodoItem(String description, TodoStatus status, LocalDateTime dueDate) {
        TodoItemEntity item = new TodoItemEntity();
        item.setDescription(description);
        item.setStatus(status);
        item.setDueDatetime(dueDate);
        item.setCreationDatetime(LocalDateTime.now());
        return item;
    }
}
