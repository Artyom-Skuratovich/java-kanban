package ru.yandex.practicum.tasks;

import java.util.Collection;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Integer, Subtask> subtasks;

    public Epic(int id, String name, String description, Iterable<Subtask> subtasks) {
        super(id, name, description, TaskStatus.NEW);
        this.subtasks = new HashMap<>();
        for (Subtask subtask : subtasks) {
            this.subtasks.put(subtask.getId(), subtask);
        }
    }

    public Collection<Subtask> getSubtasks() {
        return subtasks.values();
    }

    @Override
    public TaskStatus getStatus() {
        if (subtasks.isEmpty() || checkSubtasksStatus(TaskStatus.NEW)) {
            status = TaskStatus.NEW;
        } else if (checkSubtasksStatus(TaskStatus.DONE)) {
            status = TaskStatus.DONE;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
        return status;
    }

    private boolean checkSubtasksStatus(TaskStatus status) {
        return subtasks.values().stream().allMatch(s -> s.getStatus() == status);
    }
}