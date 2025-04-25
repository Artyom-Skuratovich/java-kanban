package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> tasks;

    public InMemoryHistoryManager() {
        tasks = new ArrayList<>(MAX_HISTORY_SIZE);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        if (tasks.size() == MAX_HISTORY_SIZE) {
            tasks.removeFirst();
        }
        tasks.addLast(copyTask(task));
    }

    @Override
    public List<Task> getHistory() {
        return tasks.stream().map(this::copyTask).toList();
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