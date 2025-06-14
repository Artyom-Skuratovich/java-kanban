package ru.yandex.practicum.tracker.exceptions;

public class TasksIntersectException extends RuntimeException {
  public TasksIntersectException(String message) {
    super(message);
  }
}