package com.sidpaw.todobackend.exception;

public class TodoSchedulerUpdateException extends RuntimeException {
    public TodoSchedulerUpdateException(String message, Throwable e) {
        super(message, e);
    }
}