package ru.yandex.practicum.tracker.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Epic extends Task {
    private final HashMap<Integer, Subtask> subtasksMap;

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
        subtasksMap = new HashMap<>();
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasksMap.values());
    }

    @Override
    public TaskStatus getStatus() {
        if (subtasksMap.isEmpty() || checkSubtasksStatus(TaskStatus.NEW)) {
            status = TaskStatus.NEW;
        } else if (checkSubtasksStatus(TaskStatus.DONE)) {
            status = TaskStatus.DONE;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
        return status;
    }

    public void setSubtasks(Iterable<Subtask> subtasks) {
        if (subtasks != null) {
            subtasksMap.clear();
            subtasks.forEach(s -> subtasksMap.put(s.getId(), s));
        }
    }

    private boolean checkSubtasksStatus(TaskStatus status) {
        return this.subtasksMap.values().stream().allMatch(s -> s.getStatus() == status);
    }
}