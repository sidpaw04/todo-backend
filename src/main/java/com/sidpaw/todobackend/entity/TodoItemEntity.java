package com.sidpaw.todobackend.entity;


import com.sidpaw.todobackend.model.TodoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a todo item in the database.
 */
@Entity
@Table(name = "todo_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @Column(name = "creation_datetime", nullable = false)
    private LocalDateTime creationDatetime;

    @Column(name = "due_datetime")
    private LocalDateTime dueDatetime;

    @Column(name = "done_datetime")
    private LocalDateTime doneDatetime;

    public TodoItemEntity(String description, LocalDateTime dueDatetime) {
        this.description = description;
        this.status = TodoStatus.NOT_DONE;
        this.creationDatetime = LocalDateTime.now();
        this.dueDatetime = dueDatetime;
    }

}
