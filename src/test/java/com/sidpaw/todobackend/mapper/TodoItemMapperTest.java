package com.sidpaw.todobackend.mapper;

import com.sidpaw.todobackend.dto.TodoPatchDTO;
import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.model.TodoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TodoItemMapper.
 */
class TodoItemMapperTest {

    private TodoItemMapper mapper;
    private TodoRequestDTO requestDTO;
    private TodoItemEntity entity;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TodoItemMapper.class);

        requestDTO = new TodoRequestDTO(
                "Complete project documentation",
                LocalDateTime.of(2025, 12, 31, 23, 59)
        );

        entity = new TodoItemEntity();
        entity.setId(1L);
        entity.setDescription("Complete project documentation");
        entity.setStatus(TodoStatus.NOT_DONE);
        entity.setCreationDatetime(LocalDateTime.of(2025, 9, 23, 10, 0));
        entity.setDueDatetime(LocalDateTime.of(2025, 12, 31, 23, 59));
    }

    @Test
    void givenTodoRequestDTO_WhenToEntity_ThenMapsCorrectly() {
        // When
        TodoItemEntity result = mapper.toEntity(requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // Should be ignored
        assertThat(result.getDescription()).isEqualTo("Complete project documentation");
        assertThat(result.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(result.getDueDatetime()).isEqualTo(LocalDateTime.of(2025, 12, 31, 23, 59));
        assertThat(result.getCreationDatetime()).isNotNull(); // Should be set by mapping expression
        assertThat(result.getDoneDatetime()).isNull(); // Should be ignored
    }

    @Test
    void givenTodoRequestDTOWithoutDueDate_WhenToEntity_ThenMapsCorrectly() {
        // Given
        TodoRequestDTO requestWithoutDueDate = new TodoRequestDTO("Simple task", null);

        // When
        TodoItemEntity result = mapper.toEntity(requestWithoutDueDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Simple task");
        assertThat(result.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(result.getDueDatetime()).isNull();
        assertThat(result.getCreationDatetime()).isNotNull();
    }

    @Test
    void givenTodoItemEntity_WhenToResponseDTO_ThenMapsCorrectly() {
        // When
        TodoResponseDTO result = mapper.toResponseDTO(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Complete project documentation");
        assertThat(result.getStatus()).isEqualTo(TodoStatus.NOT_DONE.getDisplayName());
        assertThat(result.getCreationDatetime()).isEqualTo(LocalDateTime.of(2025, 9, 23, 10, 0));
        assertThat(result.getDueDatetime()).isEqualTo(LocalDateTime.of(2025, 12, 31, 23, 59));
        assertThat(result.getDoneDatetime()).isNull();
    }

    @Test
    void givenCompletedTodoItemEntity_WhenToResponseDTO_ThenMapsCorrectly() {
        // Given
        entity.setStatus(TodoStatus.DONE);
        entity.setDoneDatetime(LocalDateTime.of(2025, 10, 1, 15, 30));

        // When
        TodoResponseDTO result = mapper.toResponseDTO(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TodoStatus.DONE.getDisplayName());
        assertThat(result.getDoneDatetime()).isEqualTo(LocalDateTime.of(2025, 10, 1, 15, 30));
    }

    @Test
    void givenListOfEntities_WhenToResponseDTOList_ThenMapsCorrectly() {
        // Given
        TodoItemEntity entity2 = new TodoItemEntity();
        entity2.setId(2L);
        entity2.setDescription("Another task");
        entity2.setStatus(TodoStatus.DONE);
        entity2.setCreationDatetime(LocalDateTime.of(2025, 9, 22, 9, 0));
        entity2.setDoneDatetime(LocalDateTime.of(2025, 9, 24, 16, 30));

        List<TodoItemEntity> entities = Arrays.asList(entity, entity2);

        // When
        List<TodoResponseDTO> result = mapper.toResponseDTOList(entities);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        TodoResponseDTO first = result.getFirst();
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getDescription()).isEqualTo("Complete project documentation");
        assertThat(first.getStatus()).isEqualTo(TodoStatus.NOT_DONE.getDisplayName());

        TodoResponseDTO second = result.get(1);
        assertThat(second.getId()).isEqualTo(2L);
        assertThat(second.getDescription()).isEqualTo("Another task");
        assertThat(second.getStatus()).isEqualTo(TodoStatus.DONE.getDisplayName());
        assertThat(second.getDoneDatetime()).isEqualTo(LocalDateTime.of(2025, 9, 24, 16, 30));
    }

    @Test
    void givenEmptyList_WhenToResponseDTOList_ThenReturnsEmptyList() {
        // Given
        List<TodoItemEntity> emptyList = List.of();

        // When
        List<TodoResponseDTO> result = mapper.toResponseDTOList(emptyList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void givenNullList_WhenToResponseDTOList_ThenReturnsNull() {
        // When
        List<TodoResponseDTO> result = mapper.toResponseDTOList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenListWithNullEntity_WhenToResponseDTOList_ThenHandlesNullsCorrectly() {
        // Given
        List<TodoItemEntity> listWithNull = Arrays.asList(entity, null, entity);

        // When
        List<TodoResponseDTO> result = mapper.toResponseDTOList(listWithNull);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(1)).isNull(); // MapStruct should preserve null entries
        assertThat(result.get(2)).isNotNull();
        
        // Verify non-null entries are mapped correctly
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getDescription()).isEqualTo("Complete project documentation");
        assertThat(result.get(2).getId()).isEqualTo(1L);
        assertThat(result.get(2).getDescription()).isEqualTo("Complete project documentation");
    }

    @Test
    void givenSingletonList_WhenToResponseDTOList_ThenReturnsSingleItem() {
        // Given
        List<TodoItemEntity> singletonList = Collections.singletonList(entity);

        // When
        List<TodoResponseDTO> result = mapper.toResponseDTOList(singletonList);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getDescription()).isEqualTo("Complete project documentation");
        assertThat(result.getFirst().getStatus()).isEqualTo(TodoStatus.NOT_DONE.getDisplayName());
    }

    @Test
    void givenNullEntity_WhenToResponseDTO_ThenReturnsNull() {
        // When
        TodoResponseDTO result = mapper.toResponseDTO(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenNullRequestDTO_WhenToEntity_ThenReturnsNull() {
        // When
        TodoItemEntity result = mapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void givenNullStatusInTodoItemEntity_WhenToResponseDTO_ThenMapsStatusAsNull() {
        // Given
        entity.setStatus(null);
        entity.setDoneDatetime(LocalDateTime.of(2025, 10, 1, 15, 30));

        // When
        TodoResponseDTO result = mapper.toResponseDTO(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isNull();
        assertThat(result.getDoneDatetime()).isEqualTo(LocalDateTime.of(2025, 10, 1, 15, 30));
    }
}
