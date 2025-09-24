package com.sidpaw.todobackend.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TodoStatus enum.
 */
class TodoStatusTest {

    @Test
    void givenTodoStatus_WhenCheckValues_ThenAllExpectedValuesExist() {
        // When
        TodoStatus[] values = TodoStatus.values();

        // Then
        assertThat(values).hasSize(3);
        assertThat(values).containsExactlyInAnyOrder(
                TodoStatus.NOT_DONE,
                TodoStatus.DONE,
                TodoStatus.PAST_DUE
        );
    }

    @Test
    void givenTodoStatusValueOf_WhenUsingStringNames_ThenReturnsCorrectEnum() {
        // When & Then
        assertThat(TodoStatus.valueOf("NOT_DONE")).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(TodoStatus.valueOf("DONE")).isEqualTo(TodoStatus.DONE);
        assertThat(TodoStatus.valueOf("PAST_DUE")).isEqualTo(TodoStatus.PAST_DUE);
    }

    @Test
    void givenTodoStatus_WhenGetDisplayName_ThenReturnsCorrectDisplayName() {
        // When & Then
        assertThat(TodoStatus.NOT_DONE.getDisplayName()).isEqualTo("not done");
        assertThat(TodoStatus.DONE.getDisplayName()).isEqualTo("done");
        assertThat(TodoStatus.PAST_DUE.getDisplayName()).isEqualTo("past due");
    }

    @Test
    void givenTodoStatus_WhenToString_ThenReturnsDisplayName() {
        // When & Then
        assertThat(TodoStatus.NOT_DONE.toString()).isEqualTo("not done");
        assertThat(TodoStatus.DONE.toString()).isEqualTo("done");
        assertThat(TodoStatus.PAST_DUE.toString()).isEqualTo("past due");
    }
}
