package ru.yandex.practicum.tracker.api.handlers;

import ru.yandex.practicum.tracker.managers.TaskManager;
import ru.yandex.practicum.tracker.models.Subtask;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SubtaskHandler extends BaseHttpHandler<Subtask> {
    private final TaskManager manager;

    public SubtaskHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "Task manager can't be null");
    }

    @Override
    protected List<Subtask> list() {
        return manager.getSubtaskList();
    }

    @Override
    protected Optional<Subtask> getById(long id) {
        return manager.getSubtaskById(id);
    }

    @Override
    protected long create(Subtask value) {
        return manager.createSubtask(value);
    }

    @Override
    protected void update(Subtask value) {
        manager.updateSubtask(value);
    }

    @Override
    protected void delete(long id) {
        manager.removeSubtask(id);
    }

    @Override
    protected Class<Subtask> getType() {
        return Subtask.class;
    }
}