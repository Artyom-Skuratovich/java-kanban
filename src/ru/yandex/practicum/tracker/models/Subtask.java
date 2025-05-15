package ru.yandex.practicum.tracker.models;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private long parentEpicId;

    public Subtask(String name, String description, LocalDateTime startTime, Duration duration, long parentEpicId) {
        super(name, description, startTime, duration);
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