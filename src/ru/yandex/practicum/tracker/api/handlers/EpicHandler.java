package ru.yandex.practicum.tracker.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.tracker.exceptions.TaskNotFoundException;
import ru.yandex.practicum.tracker.managers.TaskManager;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler<Epic> {
    private final TaskManager manager;

    public EpicHandler(TaskManager manager) {
        this.manager = Objects.requireNonNull(manager, "Task manager can't be null");
    }

    @Override
    protected List<Epic> list() {
        return manager.getEpicList();
    }

    @Override
    protected Optional<Epic> getById(long id) {
        return manager.getEpicById(id);
    }

    @Override
    protected long create(Epic value) {
        return manager.createEpic(value);
    }

    @Override
    protected void update(Epic value) {
        manager.updateEpic(value);
    }

    @Override
    protected void delete(long id) {
        manager.removeEpic(id);
    }

    @Override
    protected Class<Epic> getType() {
        return Epic.class;
    }

    @Override
    protected boolean checkIntersection(Epic value) {
        return manager.checkIntersection(value);
    }

    @Override
    protected void get(HttpExchange exchange) throws IOException {
        String[] pathComponents = exchange.getRequestURI().getPath().split("/");

        if ((pathComponents.length == 4) && (pathComponents[3].equals("subtasks"))) {
            try {
                long id = Long.parseLong(pathComponents[2]);
                Optional<Epic> optionalEpic = getById(id);

                if (optionalEpic.isEmpty()) {
                    throw new TaskNotFoundException("Epic with id=" + id + " not found");
                }

                List<Subtask> subtasks = manager.getSubtaskListForEpic(id);
                sendResponse(exchange, subtasks, 200);
            } catch (NumberFormatException | TaskNotFoundException exception) {
                sendResponse(exchange, null, 404);
            } catch (Exception exception) {
                sendResponse(exchange, null, 500);
            }
        } else {
            super.get(exchange);
        }
    }
}