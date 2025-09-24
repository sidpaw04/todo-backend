package com.sidpaw.todobackend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoPatchDTO {
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    private String status;
}
