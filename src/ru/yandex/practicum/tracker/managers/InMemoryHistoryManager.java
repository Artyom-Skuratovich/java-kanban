package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    public InMemoryHistoryManager() {

    }

    @Override
    public void add(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
    }

    @Override
    public List<Task> getHistory() {
        return List.of();
    }

    private Task copyTask(Task task) {
        if (task instanceof Subtask) {
            return new Subtask((Subtask) task);
        } else if (task instanceof Epic) {
            return new Epic((Epic) task);
        }
        return new Task(task);
    }
}