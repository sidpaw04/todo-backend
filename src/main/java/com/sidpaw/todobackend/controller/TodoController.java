package com.sidpaw.todobackend.controller;


import com.sidpaw.todobackend.dto.TodoPatchDTO;
import com.sidpaw.todobackend.dto.TodoRequestDTO;
import com.sidpaw.todobackend.dto.TodoResponseDTO;
import com.sidpaw.todobackend.service.TodoItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.vavr.control.Try;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing todo items.
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Todo Management", description = "Operations for managing todo items")
public class TodoController {

    private final TodoItemService todoItemService;

    @PostMapping
    @Operation(summary = "Create a new todo item", description = "Creates a new todo item with the provided description and optional due date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Todo item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<TodoResponseDTO> createTodoItem(
            @Valid @RequestBody TodoRequestDTO request) {
        
        log.info("Received request to create todo item: {}", request.getDescription());
        TodoResponseDTO todoResponse = todoItemService.createTodoItem(request);
        return new ResponseEntity<>(todoResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all todo items", description = "Retrieves all todo items ordered by creation date (newest first)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved todo items")
    public ResponseEntity<List<TodoResponseDTO>> getAllTodoItems(
            @Parameter(description = "Optional status filter ('done' or 'not done')")
            @RequestParam(required = false) String status) {
        if (status == null) {
            return ResponseEntity.ok(todoItemService.getAllTodoItems());
        }
        return ResponseEntity.ok(todoItemService.getTodoItemsByStatus(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get todo item by ID", description = "Retrieves a specific todo item by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo item found"),
            @ApiResponse(responseCode = "404", description = "Todo item not found")
    })
    public ResponseEntity<TodoResponseDTO> getTodoItemById(
            @Parameter(description = "ID of the todo item to retrieve") 
            @PathVariable Long id) {
        
        log.info("Received request to get todo item with ID: {}", id);
        Optional<TodoResponseDTO> todoItem = todoItemService.getTodoItemById(id);
        
        return todoItem.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a todo item", description = "Updates specified fields of a todo item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Todo item not found"),
            @ApiResponse(responseCode = "400", description = "Bad request - cannot update past due item or invalid status")
    })
    public ResponseEntity<TodoResponseDTO> patchTodoItem(
            @Parameter(description = "ID of the todo item to update") 
            @PathVariable Long id, 
            @Valid @RequestBody TodoPatchDTO patchRequest) {
        
        log.info("Received request to patch todo item with ID: {}", id);
        return Try.of(() -> todoItemService.patchTodo(id, patchRequest))
                .map(opt -> opt.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()))
                .recover(IllegalStateException.class, ResponseEntity.badRequest().build())
                .get();
    }

}
