package ru.yandex.practicum.tracker.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.tracker.managers.TaskManager;
import ru.yandex.practicum.tracker.models.Task;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PrioritizedHandler extends BaseHttpHandler<Task> {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "Task manager can't be null");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] pathComponents = exchange.getRequestURI().getPath().split("/");
        String method = exchange.getRequestMethod();

        if (method.equals("GET") && (pathComponents.length == 2)) {
            get(exchange);
        }
    }

    @Override
    protected List<Task> list() {
        return manager.getPrioritizedTasks();
    }

    @Override
    protected Optional<Task> getById(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected long create(Task value) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void update(Task value) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void delete(long id) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Class<Task> getType() {
        throw new UnsupportedOperationException();
    }
}