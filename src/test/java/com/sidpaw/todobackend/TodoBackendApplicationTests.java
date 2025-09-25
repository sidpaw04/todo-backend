package com.sidpaw.todobackend;

import com.sidpaw.todobackend.dto.TodoPatchDTO;
import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.repository.TodoItemRepository;
import com.sidpaw.todobackend.scheduler.TodoItemScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TodoBackendApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private TodoItemScheduler todoItemScheduler;

    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        todoItemRepository.deleteAll();
    }

    @Test
    void givenApplicationContext_whenLoading_thenAllDependenciesAreInjected() {
        // Verifies that the application context loads correctly
        assertThat(restTemplate).isNotNull();
        assertThat(todoItemRepository).isNotNull();
        assertThat(todoItemScheduler).isNotNull();
    }

    @Test
    void givenMultipleNewTodos_whenPerformingAllOperations_thenBehavesAsExpected() {
        // 1. Create multiple todos
        TodoResponseDTO item1 = createTodoItem("Task 1", LocalDateTime.now().plusDays(1));
        TodoResponseDTO item2 = createTodoItem("Task 2", LocalDateTime.now().plusHours(1));
        createTodoItem("Task 3", null); // No due date

        // 2. Verify items are created correctly
        assertThat(getAllTodoItems())
            .hasSize(3)
            .extracting("description")
            .containsExactlyInAnyOrder("Task 1", "Task 2", "Task 3");

        // 3. Mark one item as done
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setStatus("done");
        updateTodoStatus(item1.getId(), patchDTO);

        // 4. Verify done items filter
        List<TodoResponseDTO> doneItems = getTodoItemsByStatus("done");
        assertThat(doneItems)
            .hasSize(1)
            .extracting("description")
            .containsExactly("Task 1");

        // 5. Update description of an item
        TodoPatchDTO descriptionUpdate = new TodoPatchDTO();
        descriptionUpdate.setDescription("Updated Task 2");
        updateTodoStatus(item2.getId(), descriptionUpdate);

        // 6. Verify update
        TodoResponseDTO updatedItem = getTodoById(item2.getId());
        assertThat(updatedItem.getDescription()).isEqualTo("Updated Task 2");

        // 7. Simulate time passing for past due items
        createTodoItem("Past Due Task", LocalDateTime.now().minusHours(1));
        todoItemScheduler.updatePastDueItems();

        // 8. Verify past due items
        List<TodoResponseDTO> pastDueItems = getTodoItemsByStatus("past due");
        assertThat(pastDueItems)
            .hasSize(1)
            .extracting("description")
            .containsExactly("Past Due Task");

        // 9. Try to update a past due item (should fail)
        TodoPatchDTO updatePastDue = new TodoPatchDTO();
        updatePastDue.setDescription("Try to update past due");
        ResponseEntity<TodoResponseDTO> response = restTemplate.exchange(
            "/api/todos/" + pastDueItems.getFirst().getId(),
            HttpMethod.PATCH,
            new HttpEntity<>(updatePastDue, headers),
            TodoResponseDTO.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void givenTodoItemsWithSpecialConditions_whenPerformingOperations_thenHandlesEdgeCasesCorrectly() {
        // 1. Create item with exactly current time
        LocalDateTime now = LocalDateTime.now();
        TodoResponseDTO exactTimeItem = createTodoItem("Exact time task", now);

        // 2. Create item with far future date
        TodoResponseDTO futureItem = createTodoItem("Future task", now.plusYears(100));

        // 3. Create item with invalid status (should fail)
        TodoPatchDTO invalidStatus = new TodoPatchDTO();
        invalidStatus.setStatus("invalid");
        ResponseEntity<TodoResponseDTO> response = restTemplate.exchange(
            "/api/todos/" + futureItem.getId(),
            HttpMethod.PATCH,
            new HttpEntity<>(invalidStatus, headers),
            TodoResponseDTO.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // 4. Multiple status transitions
        Long itemId = exactTimeItem.getId();
        updateTodoStatus(itemId, createPatchDTO("done")); // not_done -> done
        updateTodoStatus(itemId, createPatchDTO("not done")); // done -> not_done
        TodoResponseDTO finalItem = getTodoById(itemId);
        assertThat(finalItem.getStatus()).isEqualTo("not done");
    }

    @Test
    void givenTodoItemsWithDifferentStates_whenFiltering_thenReturnsCorrectItemsAndOrder() {
        // Create items with different states
        TodoResponseDTO doneTask1 = createTodoItem("Done task 1", LocalDateTime.now().plusDays(1)); // Will be marked as done
        TodoResponseDTO doneTask2 = createTodoItem("Done task 2", LocalDateTime.now().plusDays(1)); // Will also be marked as done
        createTodoItem("Not done task", LocalDateTime.now().plusDays(2)); // Not done and not due yet  
        createTodoItem("Past due task", LocalDateTime.now().minusDays(1)); // Will become past due

        // Mark tasks as done using their saved IDs
        updateTodoStatus(doneTask1.getId(), createPatchDTO("done"));
        updateTodoStatus(doneTask2.getId(), createPatchDTO("done"));

        // Update past due items (makes the past due task past_due)
        todoItemScheduler.updatePastDueItems();

        // Verify counts for each status
        assertThat(getTodoItemsByStatus("done")).hasSize(2);
        assertThat(getTodoItemsByStatus("not done")).hasSize(1);
        assertThat(getTodoItemsByStatus("past due")).hasSize(1);

        // Verify order of items (by creation time)
        List<TodoResponseDTO> allItemsOrdered = getAllTodoItems();
        assertThat(allItemsOrdered)
            .extracting("description")
            .containsExactly(
                "Past due task",
                "Not done task",
                "Done task 2",
                "Done task 1"
            );
    }

    private TodoResponseDTO createTodoItem(String description, LocalDateTime dueDate) {
        TodoRequestDTO request = new TodoRequestDTO(description, dueDate);
        ResponseEntity<TodoResponseDTO> response = restTemplate.postForEntity(
            "/api/todos",
            new HttpEntity<>(request, headers),
            TodoResponseDTO.class
        );
        return Objects.requireNonNull(response.getBody());
    }

    private List<TodoResponseDTO> getAllTodoItems() {
        ResponseEntity<List<TodoResponseDTO>> response = restTemplate.exchange(
            "/api/todos",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<TodoResponseDTO>>() {}
        );
        return Objects.requireNonNull(response.getBody());
    }

    private List<TodoResponseDTO> getTodoItemsByStatus(String status) {
        ResponseEntity<List<TodoResponseDTO>> response = restTemplate.exchange(
            "/api/todos?status=" + status,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<TodoResponseDTO>>() {}
        );
        return Objects.requireNonNull(response.getBody());
    }

    private TodoResponseDTO getTodoById(Long id) {
        ResponseEntity<TodoResponseDTO> response = restTemplate.getForEntity(
            "/api/todos/" + id,
            TodoResponseDTO.class
        );
        return Objects.requireNonNull(response.getBody());
    }

    private void updateTodoStatus(Long id, TodoPatchDTO patchDTO) {
        restTemplate.exchange(
            "/api/todos/" + id,
            HttpMethod.PATCH,
            new HttpEntity<>(patchDTO, headers),
            TodoResponseDTO.class
        );
    }

    private TodoPatchDTO createPatchDTO(String status) {
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setStatus(status);
        return patchDTO;
    }

    @Test
    void givenPastDueItem_whenAttemptingModifications_thenAllModificationsAreRejected() {
        // 1. Create an item that will be past due
        TodoResponseDTO pastDueItem = createTodoItem("Past Due Item", LocalDateTime.now().minusDays(1));
        
        // 2. Make it past due by running the scheduler
        todoItemScheduler.updatePastDueItems();
        
        // 3. Verify item is now past due
        TodoResponseDTO item = getTodoById(pastDueItem.getId());
        assertThat(item.getStatus()).isEqualTo("past due");

        // 4. Try all possible modifications and verify they are rejected
        
        // Try to update description
        TodoPatchDTO descriptionUpdate = new TodoPatchDTO();
        descriptionUpdate.setDescription("Updated Description");
        ResponseEntity<TodoResponseDTO> descResponse = restTemplate.exchange(
            "/api/todos/" + pastDueItem.getId(),
            HttpMethod.PATCH,
            new HttpEntity<>(descriptionUpdate, headers),
            TodoResponseDTO.class
        );
        assertThat(descResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Try to update status to done
        TodoPatchDTO statusUpdate = new TodoPatchDTO();
        statusUpdate.setStatus("done");
        ResponseEntity<TodoResponseDTO> statusResponse = restTemplate.exchange(
            "/api/todos/" + pastDueItem.getId(),
            HttpMethod.PATCH,
            new HttpEntity<>(statusUpdate, headers),
            TodoResponseDTO.class
        );
        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Try multiple updates at once
        TodoPatchDTO multiUpdate = new TodoPatchDTO();
        multiUpdate.setDescription("New Description");
        multiUpdate.setStatus("done");
        ResponseEntity<TodoResponseDTO> multiResponse = restTemplate.exchange(
            "/api/todos/" + pastDueItem.getId(),
            HttpMethod.PATCH,
            new HttpEntity<>(multiUpdate, headers),
            TodoResponseDTO.class
        );
        assertThat(multiResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // 5. Verify item remains unchanged
        TodoResponseDTO finalItem = getTodoById(pastDueItem.getId());
        assertThat(finalItem)
            .extracting("id", "description", "status")
            .containsExactly(pastDueItem.getId(), "Past Due Item", "past due");
    }
}
