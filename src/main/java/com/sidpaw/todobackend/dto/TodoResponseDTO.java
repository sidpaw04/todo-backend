package com.sidpaw.todobackend.dto;

import com.sidpaw.todobackend.model.TodoStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for todo item data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Todo item response object")
public class TodoResponseDTO {

    @Schema(description = "Unique identifier of the todo item", example = "1")
    private Long id;

    @Schema(description = "Description of the todo item", example = "Complete the project documentation")
    private String description;

    @Schema(description = "Current status of the todo item", example = "NOT_DONE")
    private TodoStatus status;

    @Schema(description = "Date and time when the todo item was created", example = "2025-09-23T15:30:00")
    private LocalDateTime creationDatetime;

    @Schema(description = "Due date and time for the todo item", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDatetime;

    @Schema(description = "Date and time when the todo item was completed", example = "2025-09-24T10:15:00")
    private LocalDateTime doneDatetime;
}
