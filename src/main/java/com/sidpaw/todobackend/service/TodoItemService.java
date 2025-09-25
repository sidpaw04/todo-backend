package com.sidpaw.todobackend.service;


import com.sidpaw.todobackend.dto.TodoPatchDTO;
import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.exception.InvalidStatusException;
import com.sidpaw.todobackend.mapper.TodoItemMapper;
import com.sidpaw.todobackend.model.TodoStatus;
import com.sidpaw.todobackend.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing todo items.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TodoItemService {

    private final TodoItemRepository todoItemRepository;
    private final TodoItemMapper todoItemMapper;

    /**
     * Creates a new todo item.
     */
    public TodoResponseDTO createTodoItem(TodoRequestDTO request) {
        log.info("Creating new todo item with description: {}", request.getDescription());
        
        TodoItemEntity todoItem = todoItemMapper.toEntity(request);
        TodoItemEntity savedItem = todoItemRepository.save(todoItem);
        
        log.info("Successfully created todo item with ID: {}", savedItem.getId());
        return todoItemMapper.toResponseDTO(savedItem);
    }

    /**
     * Retrieves all todo items.
     */
    @Transactional(readOnly = true)
    public List<TodoResponseDTO> getAllTodoItems() {
        log.info("Retrieving all todo items");
        
        List<TodoItemEntity> todoItems = todoItemRepository.findAllByOrderByCreationDatetimeDesc();
        
        log.info("Retrieved {} todo items", todoItems.size());
        return todoItemMapper.toResponseDTOList(todoItems);
    }

    /**
     * Retrieves a todo item by ID.
     */
    @Transactional(readOnly = true)
    public Optional<TodoResponseDTO> getTodoItemById(Long id) {
        log.info("Retrieving todo item with ID: {}", id);
        
        Optional<TodoItemEntity> todoItem = todoItemRepository.findById(id);
        
        return todoItem.map(todoItemMapper::toResponseDTO);
    }

    public Optional<TodoResponseDTO> patchTodo(Long id, TodoPatchDTO patchDTO) {
        return todoItemRepository.findById(id)
                .map(todo -> updateTodoFields(todo, patchDTO))
                .map(todoItemRepository::save)
                .map(todoItemMapper::toResponseDTO);
    }

    private TodoItemEntity updateTodoFields(TodoItemEntity todo, TodoPatchDTO patchDTO) {
        // Prevent updates to past due items
        if (todo.getStatus() == TodoStatus.PAST_DUE) {
            throw new IllegalStateException("Cannot update a past due item");
        }

        Optional.ofNullable(patchDTO.getDescription())
                .ifPresent(todo::setDescription);
                
        Optional.ofNullable(patchDTO.getStatus())
                .ifPresent(statusStr -> {
                    TodoStatus status = convertToTodoStatus(statusStr);
                    todo.setStatus(status);
                    
                    // Set the done datetime when the item is marked as done
                    if (status == TodoStatus.DONE) {
                        todo.setDoneDatetime(LocalDateTime.now());
                    }
                });
        
        return todo;
    }

    private TodoStatus convertToTodoStatus(String statusStr) {
        // Call TodoStatus.from to convert string to enum using the existing utility method
        TodoStatus requestedStatus = TodoStatus.from(statusStr.toLowerCase());
        
        // Verify that we can update to this status
        if (!TodoStatus.isUpdatableStatus(requestedStatus)) {
            throw new InvalidStatusException(
                String.format("Invalid status value: '%s'. Valid values are: 'done', 'not done'", statusStr)
            );
        }
        
        return requestedStatus;
    }

    public List<TodoResponseDTO> getTodoItemsByStatus(String requestedStatus) {
        
        TodoStatus status = TodoStatus.from(requestedStatus);
        
        return (status.equals(TodoStatus.NOT_DONE)
                ? todoItemRepository.findNotDoneItems(LocalDateTime.now(), TodoStatus.NOT_DONE)
                : todoItemRepository.findByStatusOrderByCreationDatetimeDesc(status))
                .stream()
                .map(todoItemMapper::toResponseDTO)
                .toList();
    }
}
