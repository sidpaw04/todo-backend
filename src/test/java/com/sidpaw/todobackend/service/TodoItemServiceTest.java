package com.sidpaw.todobackend.service;

import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.mapper.TodoItemMapper;
import com.sidpaw.todobackend.model.TodoStatus;
import com.sidpaw.todobackend.repository.TodoItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TodoItemService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class TodoItemServiceTest {

    @Mock
    private TodoItemRepository todoItemRepository;

    @Mock
    private TodoItemMapper todoItemMapper;

    @InjectMocks
    private TodoItemService todoItemService;

    private TodoRequestDTO validRequest;
    private TodoItemEntity todoEntity;
    private TodoResponseDTO expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = new TodoRequestDTO(
                "Complete project documentation",
                LocalDateTime.of(2025, 12, 31, 23, 59)
        );

        todoEntity = new TodoItemEntity();
        todoEntity.setId(1L);
        todoEntity.setDescription("Complete project documentation");
        todoEntity.setStatus(TodoStatus.NOT_DONE);
        todoEntity.setCreationDatetime(LocalDateTime.of(2025, 9, 23, 10, 0));
        todoEntity.setDueDatetime(LocalDateTime.of(2025, 12, 31, 23, 59));

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
    void givenValidRequest_WhenCreateTodoItem_ThenReturnsSavedTodo() {
        // Given
        when(todoItemMapper.toEntity(validRequest)).thenReturn(todoEntity);
        when(todoItemRepository.save(todoEntity)).thenReturn(todoEntity);
        when(todoItemMapper.toResponseDTO(todoEntity)).thenReturn(expectedResponse);

        // When
        TodoResponseDTO result = todoItemService.createTodoItem(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Complete project documentation");
        assertThat(result.getStatus()).isEqualTo(TodoStatus.NOT_DONE);

        verify(todoItemMapper).toEntity(validRequest);
        verify(todoItemRepository).save(todoEntity);
        verify(todoItemMapper).toResponseDTO(todoEntity);
    }

    @Test
    void givenTodoItemsExist_WhenGetAllTodoItems_ThenReturnsOrderedList() {
        // Given
        List<TodoItemEntity> entities = Arrays.asList(todoEntity);
        List<TodoResponseDTO> expectedResponses = Arrays.asList(expectedResponse);

        when(todoItemRepository.findAllByOrderByCreationDatetimeDesc()).thenReturn(entities);
        when(todoItemMapper.toResponseDTOList(entities)).thenReturn(expectedResponses);

        // When
        List<TodoResponseDTO> result = todoItemService.getAllTodoItems();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(todoItemRepository).findAllByOrderByCreationDatetimeDesc();
        verify(todoItemMapper).toResponseDTOList(entities);
    }

    @Test
    void givenNoTodoItems_WhenGetAllTodoItems_ThenReturnsEmptyList() {
        // Given
        List<TodoItemEntity> entities = Arrays.asList();
        List<TodoResponseDTO> expectedResponses = Arrays.asList();

        when(todoItemRepository.findAllByOrderByCreationDatetimeDesc()).thenReturn(entities);
        when(todoItemMapper.toResponseDTOList(entities)).thenReturn(expectedResponses);

        // When
        List<TodoResponseDTO> result = todoItemService.getAllTodoItems();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(todoItemRepository).findAllByOrderByCreationDatetimeDesc();
        verify(todoItemMapper).toResponseDTOList(entities);
    }

    @Test
    void givenExistingTodoId_WhenGetTodoItemById_ThenReturnsTodo() {
        // Given
        when(todoItemRepository.findById(eq(1L))).thenReturn(Optional.of(todoEntity));
        when(todoItemMapper.toResponseDTO(todoEntity)).thenReturn(expectedResponse);

        // When
        Optional<TodoResponseDTO> result = todoItemService.getTodoItemById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getDescription()).isEqualTo("Complete project documentation");

        verify(todoItemRepository).findById(1L);
        verify(todoItemMapper).toResponseDTO(todoEntity);
    }

    @Test
    void givenNonExistingTodoId_WhenGetTodoItemById_ThenReturnsEmpty() {
        // Given
        when(todoItemRepository.findById(eq(999L))).thenReturn(Optional.empty());

        // When
        Optional<TodoResponseDTO> result = todoItemService.getTodoItemById(999L);

        // Then
        assertThat(result).isEmpty();

        verify(todoItemRepository).findById(999L);
        verify(todoItemMapper, never()).toResponseDTO(any());
    }

    @Test
    void givenNullId_WhenGetTodoItemById_ThenRepositoryIsCalled() {
        // Given
        when(todoItemRepository.findById(eq(null))).thenReturn(Optional.empty());

        // When
        Optional<TodoResponseDTO> result = todoItemService.getTodoItemById(null);

        // Then
        assertThat(result).isEmpty();

        verify(todoItemRepository).findById(null);
        verify(todoItemMapper, never()).toResponseDTO(any());
    }

    @Test
    void givenServiceThrowsException_WhenCreateTodoItem_ThenExceptionPropagates() {
        // Given
        when(todoItemMapper.toEntity(validRequest)).thenThrow(new RuntimeException("Mapping failed"));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            todoItemService.createTodoItem(validRequest);
        });

        verify(todoItemMapper).toEntity(validRequest);
        verify(todoItemRepository, never()).save(any());
    }
}
