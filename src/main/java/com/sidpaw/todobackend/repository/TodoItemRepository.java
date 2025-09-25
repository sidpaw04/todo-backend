package com.sidpaw.todobackend.repository;


import com.sidpaw.todobackend.entity.TodoItemEntity;
import com.sidpaw.todobackend.model.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for TodoItemEntity entities.
 */
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItemEntity, Long> {

    /**
     * Find all todo items ordered by creation date (newest first).
     */
    List<TodoItemEntity> findAllByOrderByCreationDatetimeDesc();

    // Get NOT_DONE items that are not past due
    @Query("SELECT t FROM TodoItemEntity t WHERE t.status = 'NOT_DONE' " +
           "AND (t.dueDatetime IS NULL OR t.dueDatetime > :now) " +
           "ORDER BY t.creationDatetime DESC")
    List<TodoItemEntity> findNotDoneItems(@Param("now") LocalDateTime now);

    // Get items by specific status (for DONE and PAST_DUE)
    List<TodoItemEntity> findByStatusOrderByCreationDatetimeDesc(TodoStatus status);
}
