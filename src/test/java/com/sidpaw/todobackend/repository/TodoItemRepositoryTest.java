package com.sidpaw.todobackend.repository;

import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.model.TodoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
    private TestEntityManager entityManager;

    @Autowired
    private TodoItemRepository todoItemRepository;

    private TodoItemEntity todoItem1;
    private TodoItemEntity todoItem2;
    private TodoItemEntity todoItem3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        entityManager.clear();

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
        TodoItemEntity found = entityManager.find(TodoItemEntity.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getDescription()).isEqualTo("First task");
    }

    @Test
    void givenExistingTodoItem_WhenFindById_ThenReturnsTodoItem() {
        // Given
        TodoItemEntity saved = entityManager.persistAndFlush(todoItem1);

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
        entityManager.persistAndFlush(todoItem1);
        entityManager.persistAndFlush(todoItem2);
        entityManager.persistAndFlush(todoItem3);

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
        entityManager.persistAndFlush(todoItem1); // 2025-09-23
        entityManager.persistAndFlush(todoItem2); // 2025-09-22 
        entityManager.persistAndFlush(todoItem3); // 2025-09-21

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
        TodoItemEntity saved = entityManager.persistAndFlush(todoItem1);
        entityManager.clear(); // Clear the persistence context

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
        TodoItemEntity found = entityManager.find(TodoItemEntity.class, saved.getId());
        assertThat(found.getDescription()).isEqualTo("Updated task description");
        assertThat(found.getStatus()).isEqualTo(TodoStatus.DONE);
    }

    @Test
    void givenExistingTodoItem_WhenDelete_ThenItemIsRemoved() {
        // Given
        TodoItemEntity saved = entityManager.persistAndFlush(todoItem1);
        Long savedId = saved.getId();

        // When
        todoItemRepository.delete(saved);
        entityManager.flush();

        // Then
        TodoItemEntity found = entityManager.find(TodoItemEntity.class, savedId);
        assertThat(found).isNull();
        
        Optional<TodoItemEntity> result = todoItemRepository.findById(savedId);
        assertThat(result).isEmpty();
    }

    @Test
    void givenTodoItems_WhenCount_ThenReturnsCorrectCount() {
        // Given
        entityManager.persistAndFlush(todoItem1);
        entityManager.persistAndFlush(todoItem2);

        // When
        long count = todoItemRepository.count();

        // Then
        assertThat(count).isEqualTo(2L);
    }
}
