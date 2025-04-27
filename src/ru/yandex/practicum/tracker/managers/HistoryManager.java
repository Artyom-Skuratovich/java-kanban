package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.models.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    void remove(long id);

    List<Task> getHistory();
}