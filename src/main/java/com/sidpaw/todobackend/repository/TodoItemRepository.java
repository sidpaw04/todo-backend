package com.sidpaw.todobackend.repository;


import com.sidpaw.todobackend.entity.TodoItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TodoItemEntity entities.
 */
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItemEntity, Long> {

    /**
     * Find all todo items ordered by creation date (newest first).
     */
    List<TodoItemEntity> findAllByOrderByCreationDatetimeDesc();
}
