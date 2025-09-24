package com.sidpaw.todobackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new todo item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new todo item")
public class TodoRequestDTO {

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Description of the todo item", example = "Complete the project documentation")
    private String description;

    @Schema(description = "Due date and time for the todo item", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDatetime;
}
