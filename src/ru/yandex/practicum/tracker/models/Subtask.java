package ru.yandex.practicum.tracker.models;

public class Subtask extends Task {
    private long parentEpicId;

    public Subtask(String name, String description, long parentEpicId) {
        super(name, description);
        this.parentEpicId = parentEpicId;
    }

    public Subtask(Subtask other) {
        super(other);
        parentEpicId = other.parentEpicId;
    }

    public long getParentEpicId() {
        return parentEpicId;
    }

    public void setParentEpicId(long parentEpicId) {
        this.parentEpicId = parentEpicId;
    }
}