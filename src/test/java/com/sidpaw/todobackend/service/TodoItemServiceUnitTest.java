package com.sidpaw.todobackend.service;

import com.sidpaw.todobackend.dto.TodoPatchDTO;
import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.exception.InvalidStatusException;
import com.sidpaw.todobackend.mapper.TodoItemMapper;
import com.sidpaw.todobackend.model.TodoStatus;
import com.sidpaw.todobackend.repository.TodoItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoItemServiceUnitTest {

    @Mock
    private TodoItemRepository todoItemRepository;

    @Mock
    private TodoItemMapper todoItemMapper;

    @InjectMocks
    private TodoItemService todoItemService;

    private TodoRequestDTO validRequest;
    private TodoItemEntity todoEntity;
    private TodoItemEntity existingTodo;
    private TodoResponseDTO expectedResponse;
    private TodoResponseDTO responseDTO;

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
                "not done",
                LocalDateTime.of(2025, 9, 23, 10, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59),
                null
        );

        existingTodo = new TodoItemEntity();
        existingTodo.setId(1L);
        existingTodo.setDescription("Original description");
        existingTodo.setStatus(TodoStatus.NOT_DONE);

        responseDTO = new TodoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setDescription("Original description");
        responseDTO.setStatus("not done");
    }

    @Test
    void givenValidRequest_WhenCreateTodoItem_ThenReturnsSavedTodo() {
        // Given
        TodoRequestDTO request = new TodoRequestDTO(
                "Test todo",
                LocalDateTime.now().plusDays(1)
        );

        TodoItemEntity entity = new TodoItemEntity();
        entity.setDescription("Test todo");
        entity.setStatus(TodoStatus.NOT_DONE);
        entity.setDueDatetime(request.getDueDatetime());

        TodoResponseDTO expectedResponse = new TodoResponseDTO();
        expectedResponse.setDescription("Test todo");
        expectedResponse.setStatus("not done");
        expectedResponse.setDueDatetime(request.getDueDatetime());

        when(todoItemMapper.toEntity(any(TodoRequestDTO.class))).thenReturn(entity);
        when(todoItemRepository.save(any(TodoItemEntity.class))).thenReturn(entity);
        when(todoItemMapper.toResponseDTO(any(TodoItemEntity.class))).thenReturn(expectedResponse);

        // When
        TodoResponseDTO result = todoItemService.createTodoItem(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Test todo");
        assertThat(result.getStatus()).isEqualTo("not done");
        assertThat(result.getDueDatetime()).isEqualTo(request.getDueDatetime());
    }

    @Test
    void givenTodoItemsExist_WhenGetAllTodoItems_ThenReturnsOrderedList() {
        // Given
        List<TodoItemEntity> entities = Collections.singletonList(todoEntity);
        List<TodoResponseDTO> expectedResponses = Collections.singletonList(expectedResponse);

        when(todoItemRepository.findAllByOrderByCreationDatetimeDesc()).thenReturn(entities);
        when(todoItemMapper.toResponseDTOList(entities)).thenReturn(expectedResponses);

        // When
        List<TodoResponseDTO> result = todoItemService.getAllTodoItems();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);

        verify(todoItemRepository).findAllByOrderByCreationDatetimeDesc();
        verify(todoItemMapper).toResponseDTOList(entities);
    }

    @Test
    void givenNoTodoItems_WhenGetAllTodoItems_ThenReturnsEmptyList() {
        // Given
        List<TodoItemEntity> entities = List.of();
        List<TodoResponseDTO> expectedResponses = List.of();

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
    void givenServiceThrowsException_WhenCreateTodoItem_ThenExceptionPropagates() {
        // Given
        when(todoItemMapper.toEntity(validRequest)).thenThrow(new RuntimeException("Mapping failed"));

        // When
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            todoItemService.createTodoItem(validRequest);
        });

        //Then
        verify(todoItemMapper).toEntity(validRequest);
        verify(todoItemRepository, never()).save(any());
    }

    @Test
    void givenExistingTodo_whenPatchDescription_thenDescriptionUpdated() {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setDescription("Updated description");

        when(todoItemRepository.findById(1L)).thenReturn(Optional.of(existingTodo));
        when(todoItemRepository.save(any(TodoItemEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(todoItemMapper.toResponseDTO(any(TodoItemEntity.class))).thenReturn(responseDTO);

        // When
        Optional<TodoResponseDTO> result = todoItemService.patchTodo(1L, patchDTO);

        // Then
        assertThat(result).isPresent();
        verify(todoItemRepository).save(argThat(saved ->
                "Updated description".equals(saved.getDescription()) &&
                        TodoStatus.NOT_DONE.equals(saved.getStatus())
        ));
    }

    @Test
    void givenExistingTodo_whenPatchStatus_thenStatusUpdated() {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setStatus("done");

        when(todoItemRepository.findById(1L)).thenReturn(Optional.of(existingTodo));
        when(todoItemRepository.save(any(TodoItemEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(todoItemMapper.toResponseDTO(any(TodoItemEntity.class))).thenReturn(responseDTO);

        // When
        Optional<TodoResponseDTO> result = todoItemService.patchTodo(1L, patchDTO);

        // Then
        assertThat(result).isPresent();
        verify(todoItemRepository).save(argThat(saved ->
                "Original description".equals(saved.getDescription()) &&
                        TodoStatus.DONE.equals(saved.getStatus())
        ));
    }

    @Test
    void givenExistingTodo_whenPatchWithInvalidStatus_thenThrowException() {
        // Arrange
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setStatus("past due");

        when(todoItemRepository.findById(1L)).thenReturn(Optional.of(existingTodo));

        // Act & Assert
        assertThatThrownBy(() -> todoItemService.patchTodo(1L, patchDTO))
            .isInstanceOf(InvalidStatusException.class)
            .hasMessageContaining("Valid values are: 'done', 'not done'");

        verify(todoItemRepository, never()).save(any());
    }

    @Test
    void givenNonExistentId_whenPatchTodo_thenReturnEmpty() {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();
        patchDTO.setDescription("New description");

        when(todoItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TodoResponseDTO> result = todoItemService.patchTodo(999L, patchDTO);

        // Then
        assertThat(result).isEmpty();
        verify(todoItemRepository, never()).save(any());
    }

    @Test
    void givenExistingTodo_whenPatchWithNoChanges_thenReturnUnmodifiedItem() {
        // Given
        TodoPatchDTO patchDTO = new TodoPatchDTO();

        when(todoItemRepository.findById(1L)).thenReturn(Optional.of(existingTodo));
        when(todoItemRepository.save(any(TodoItemEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(todoItemMapper.toResponseDTO(any(TodoItemEntity.class))).thenReturn(responseDTO);

        // When
        Optional<TodoResponseDTO> result = todoItemService.patchTodo(1L, patchDTO);

        // Then
        assertThat(result).isPresent();
        verify(todoItemRepository).save(argThat(saved ->
                "Original description".equals(saved.getDescription()) &&
                        TodoStatus.NOT_DONE.equals(saved.getStatus())
        ));
    }

    @Test
    void givenInvalidStatus_WhenGetTodoItemsByStatus_ThenThrowsException() {
        assertThatThrownBy(() -> todoItemService.getTodoItemsByStatus("invalid"))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("Invalid status");
    }
}