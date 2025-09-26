package com.sidpaw.todobackend.scheduler;

import com.sidpaw.todobackend.exception.TodoSchedulerUpdateException;
import com.sidpaw.todobackend.model.TodoStatus;
import com.sidpaw.todobackend.repository.TodoItemRepository;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TodoItemScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TodoItemScheduler.class);
    private final TodoItemRepository todoItemRepository;

    public TodoItemScheduler(TodoItemRepository todoItemRepository) {
        this.todoItemRepository = todoItemRepository;
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public int updatePastDueItems() {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Starting past due items update check at {}", now);

        return Try.of(() -> todoItemRepository.updatePastDueItems(
                        now,
                        TodoStatus.NOT_DONE,
                        TodoStatus.PAST_DUE
                ))
                .andThen(count -> Optional.of(count)
                        .filter(c -> c > 0)
                        .ifPresentOrElse(
                                c -> logger.info("Updated {} items to PAST_DUE status", c),
                                () -> logger.debug("No items needed to be updated to PAST_DUE status")
                        )
                )
                .recover(ex -> {
                    logger.error("Error updating past due items", ex);
                    throw new TodoSchedulerUpdateException("Failed to update past due items", ex);
                })
                .get();
    }
}