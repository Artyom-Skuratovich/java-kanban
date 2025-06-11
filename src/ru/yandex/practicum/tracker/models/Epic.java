package ru.yandex.practicum.tracker.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Epic extends Task {
    private final Set<Long> subtaskIds;

    public Epic(String name, String description) {
        super(name, description, null, null);
        subtaskIds = new HashSet<>();
    }

    public Epic(Epic other) {
        super(other);
        subtaskIds = new HashSet<>(other.getSubtaskIds());
    }

    public List<Long> getSubtaskIds() {
        return subtaskIds.stream().toList();
    }

    public boolean addSubtaskId(long subtaskId) {
        return subtaskIds.add(subtaskId);
    }

    public boolean removeSubtaskId(long subtaskId) {
        return subtaskIds.remove(subtaskId);
    }

    public void removeAllSubtaskIds() {
        subtaskIds.clear();
    }
}