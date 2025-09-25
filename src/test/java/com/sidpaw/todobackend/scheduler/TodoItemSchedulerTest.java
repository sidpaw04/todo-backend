package com.sidpaw.todobackend.scheduler;

import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.model.TodoStatus;
import com.sidpaw.todobackend.repository.TodoItemRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.dao.DataAccessException;

@SpringBootTest
@Transactional
class TodoItemSchedulerTest {

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private TodoItemScheduler todoItemScheduler;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        todoItemRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void givenPastDueItems_WhenSchedulerRuns_ThenUpdatesToPastDueStatus() {
        // Given
        LocalDateTime pastDueDate = LocalDateTime.now().minusDays(1);
        createTodoItem("Past due item", TodoStatus.NOT_DONE, pastDueDate);
        createTodoItem("Future due item", TodoStatus.NOT_DONE, LocalDateTime.now().plusDays(1));
        createTodoItem("No due date item", TodoStatus.NOT_DONE, null);

        entityManager.flush();
        entityManager.clear();

        // When
        todoItemScheduler.updatePastDueItems();

        // Then
        List<TodoItemEntity> pastDueItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.PAST_DUE);
        List<TodoItemEntity> notDoneItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.NOT_DONE);

        assertThat(pastDueItems)
                .hasSize(1)
                .extracting("description")
                .containsExactly("Past due item");

        assertThat(notDoneItems)
                .hasSize(2)
                .extracting("description")
                .containsExactlyInAnyOrder("Future due item", "No due date item");
    }

    @Test
    void givenNoPastDueItems_WhenSchedulerRuns_ThenNoUpdates() {
        // Given
        createTodoItem("Future due item", TodoStatus.NOT_DONE, LocalDateTime.now().plusDays(1));
        createTodoItem("No due date item", TodoStatus.NOT_DONE, null);
        createTodoItem("Done item", TodoStatus.DONE, LocalDateTime.now().minusDays(1));

        entityManager.flush();
        entityManager.clear();

        // When
        todoItemScheduler.updatePastDueItems();

        // Then
        List<TodoItemEntity> pastDueItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.PAST_DUE);
        assertThat(pastDueItems).isEmpty();
    }

    @Test
    void givenItemsWithExactDueTime_WhenSchedulerRuns_ThenHandlesEdgeCaseCorrectly() {
        // Given
        LocalDateTime futureTime = LocalDateTime.now().plusSeconds(1);
        createTodoItem("Due in 1 second", TodoStatus.NOT_DONE, futureTime);
        
        entityManager.flush();
        entityManager.clear();

        // When
        todoItemScheduler.updatePastDueItems();

        // Then
        List<TodoItemEntity> pastDueItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.PAST_DUE);
        List<TodoItemEntity> notDoneItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.NOT_DONE);

        // Items due in the future should not be marked as past due yet
        assertThat(pastDueItems).isEmpty();
        assertThat(notDoneItems).hasSize(1);
    }

    @Test
    void givenItemsWithDifferentStates_WhenSchedulerRuns_ThenOnlyUpdatesNotDoneItems() {
        // Given
        LocalDateTime pastDueTime = LocalDateTime.now().minusDays(1);
        
        // Create items in different states but all past due
        createTodoItem("Past due not done", TodoStatus.NOT_DONE, pastDueTime);
        createTodoItem("Past due but already done", TodoStatus.DONE, pastDueTime);
        createTodoItem("Already past due", TodoStatus.PAST_DUE, pastDueTime);
        
        entityManager.flush();
        entityManager.clear();

        // When
        int updatedCount = todoItemScheduler.updatePastDueItems();

        // Then
        assertThat(updatedCount).isEqualTo(1); // Only the NOT_DONE item should be updated

        List<TodoItemEntity> pastDueItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.PAST_DUE);
        List<TodoItemEntity> doneItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.DONE);

        assertThat(pastDueItems)
            .hasSize(2)
            .extracting("description")
            .containsExactlyInAnyOrder("Past due not done", "Already past due");

        assertThat(doneItems)
            .hasSize(1)
            .extracting("description")
            .containsExactly("Past due but already done");
    }

    @Test
    void givenInvalidDueDates_WhenSchedulerRuns_ThenHandlesNullAndFutureDatesCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        createTodoItem("Null due date", TodoStatus.NOT_DONE, null);
        createTodoItem("Future due date", TodoStatus.NOT_DONE, now.plusYears(1));
        createTodoItem("Past due date", TodoStatus.NOT_DONE, now.minusYears(1));
        
        entityManager.flush();
        entityManager.clear();

        // When
        int updatedCount = todoItemScheduler.updatePastDueItems();

        // Then
        List<TodoItemEntity> pastDueItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.PAST_DUE);
        List<TodoItemEntity> notDoneItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.NOT_DONE);

        assertThat(updatedCount).isEqualTo(1); // Only the past due item should be updated
        
        assertThat(pastDueItems)
            .hasSize(1)
            .extracting("description")
            .containsExactly("Past due date");

        assertThat(notDoneItems)
            .hasSize(2)
            .extracting("description")
            .containsExactlyInAnyOrder("Null due date", "Future due date");
    }

    @Test
    void givenMultipleRunsOfScheduler_WhenSchedulerRuns_ThenNoDoubleUpdates() {
        // Given
        LocalDateTime pastDueTime = LocalDateTime.now().minusDays(1);
        createTodoItem("Past due item", TodoStatus.NOT_DONE, pastDueTime);
        
        entityManager.flush();
        entityManager.clear();

        // When
        int firstRunCount = todoItemScheduler.updatePastDueItems();
        int secondRunCount = todoItemScheduler.updatePastDueItems();

        // Then
        assertThat(firstRunCount).isEqualTo(1);  // First run should update one item
        assertThat(secondRunCount).isZero();     // Second run should update nothing

        List<TodoItemEntity> pastDueItems = todoItemRepository.findByStatusOrderByCreationDatetimeDesc(TodoStatus.PAST_DUE);
        assertThat(pastDueItems).hasSize(1);
    }

    private void createTodoItem(String description, TodoStatus status, LocalDateTime dueDate) {
        TodoItemEntity item = new TodoItemEntity();
        item.setDescription(description);
        item.setStatus(status);
        item.setDueDatetime(dueDate);
        item.setCreationDatetime(LocalDateTime.now());
        todoItemRepository.save(item);
    }

    @Test
    void givenRepositoryFailure_whenUpdatingPastDueItems_thenThrowsRuntimeException() {
        // Given
        TodoItemRepository mockRepo = mock();
        doThrow(new DataAccessException("Dummy database error") {})
            .when(mockRepo)
            .updatePastDueItems(any(), any(), any());
        
        TodoItemScheduler schedulerWithMockRepo = new TodoItemScheduler(mockRepo);

        // When/Then
        assertThatThrownBy(schedulerWithMockRepo::updatePastDueItems)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to update past due items")
            .hasCauseInstanceOf(DataAccessException.class);
    }
}