package ru.yandex.practicum.tracker.models;

import java.util.Objects;

public final class Tasks {
    private Tasks() {
    }

    public static Task copyTask(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");

        if (task instanceof Subtask) {
            return new Subtask((Subtask) task);
        } else if (task instanceof Epic) {
            return new Epic((Epic) task);
        }
        return new Task(task);
    }
}