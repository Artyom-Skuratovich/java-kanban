package ru.yandex.practicum.tracker.tasks.managers;

import ru.yandex.practicum.tracker.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final ArrayList<Task> history;

    public InMemoryHistoryManager() {
        history = new ArrayList<>(MAX_HISTORY_SIZE);
    }

    @Override
    public void add(Task task) {
        if (task == null) return;
        if (history.size() == MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}