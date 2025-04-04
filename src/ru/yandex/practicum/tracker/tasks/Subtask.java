package ru.yandex.practicum.tracker.tasks;

public class Subtask extends Task {
    private final int parentEpicId;

    public Subtask(int id, String name, String description, TaskStatus status, int parentEpicId) {
        super(id, name, description, status);
        this.parentEpicId = parentEpicId;
    }

    public int getParentEpicId() {
        return parentEpicId;
    }
}