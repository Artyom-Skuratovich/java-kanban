package ru.yandex.practicum.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Epic extends Task {
    private final HashMap<Integer, Subtask> subtasks;

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
        this.subtasks = new HashMap<>();
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void addSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasks.put(subtask.getId(), subtask);
        }
    }

    public void removeSubtask(int id) {
        subtasks.remove(id);
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