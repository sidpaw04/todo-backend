package com.sidpaw.todobackend.mapper;


import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.model.TodoStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for converting between TodoItem entities and DTOs.
 */
@Mapper(componentModel = "spring")
public abstract class TodoItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.sidpaw.todobackend.model.TodoStatus.NOT_DONE)")
    @Mapping(target = "creationDatetime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "doneDatetime", ignore = true)
    public abstract TodoItemEntity toEntity(TodoRequestDTO todoRequestDTO);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
    public abstract TodoResponseDTO toResponseDTO(TodoItemEntity todoItemEntity);

    public abstract List<TodoResponseDTO> toResponseDTOList(List<TodoItemEntity> todoItemEntities);

    @Named("mapStatus")
    protected String mapStatus(TodoStatus status) {
        return status != null ? status.getDisplayName() : null;
    }
}
