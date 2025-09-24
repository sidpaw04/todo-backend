package com.sidpaw.todobackend.model;

/**
 * Enumeration representing the status of a todo item.
 */
public enum TodoStatus {
    NOT_DONE("not done"),
    DONE("done"),
    PAST_DUE("past due");

    private final String displayName;
    
    TodoStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static boolean isUpdatableStatus(TodoStatus status) {
        return status == TodoStatus.DONE || status == TodoStatus.NOT_DONE;
    }
}
