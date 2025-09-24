package com.sidpaw.todobackend.mapper;


import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting between TodoItem entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface TodoItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.sidpaw.todobackend.model.TodoStatus.NOT_DONE)")
    @Mapping(target = "creationDatetime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "doneDatetime", ignore = true)
    TodoItemEntity toEntity(TodoRequestDTO todoRequestDTO);

    TodoResponseDTO toResponseDTO(TodoItemEntity todoItemEntity);

    List<TodoResponseDTO> toResponseDTOList(List<TodoItemEntity> todoItemEntities);
}
