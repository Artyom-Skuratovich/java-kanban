package ru.yandex.practicum.tracker.exceptions;

public class TaskFormatException extends RuntimeException {
    public TaskFormatException(String message) {
        super(message);
    }
}