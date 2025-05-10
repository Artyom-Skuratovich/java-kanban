package ru.yandex.practicum.tracker.exceptions;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message) {
        super(message);
    }
}