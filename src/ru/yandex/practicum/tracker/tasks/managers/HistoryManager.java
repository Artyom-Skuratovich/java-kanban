package ru.yandex.practicum.tracker.tasks.managers;

import ru.yandex.practicum.tracker.tasks.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();
}