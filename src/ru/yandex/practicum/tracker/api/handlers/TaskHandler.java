package ru.yandex.practicum.tracker.api.handlers;

import ru.yandex.practicum.tracker.managers.TaskManager;
import ru.yandex.practicum.tracker.models.Task;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler<Task> {
    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "Task manager can't be null");
    }

    @Override
    protected List<Task> list() {
        return manager.getTaskList();
    }

    @Override
    protected Optional<Task> getById(long id) {
        return manager.getTaskById(id);
    }

    @Override
    protected long create(Task value) {
        return manager.createTask(value);
    }

    @Override
    protected void update(Task value) {
        manager.updateTask(value);
    }

    @Override
    protected void delete(long id) {
        manager.removeTask(id);
    }

    @Override
    protected Class<Task> getType() {
        return Task.class;
    }

    @Override
    protected boolean checkIntersection(Task value) {
        return manager.checkIntersection(value);
    }
}