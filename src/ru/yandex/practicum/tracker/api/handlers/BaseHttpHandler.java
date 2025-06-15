package ru.yandex.practicum.tracker.api.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.tracker.api.adapters.DurationAdapter;
import ru.yandex.practicum.tracker.api.adapters.LocalDateTimeAdapter;
import ru.yandex.practicum.tracker.exceptions.TaskNotFoundException;
import ru.yandex.practicum.tracker.exceptions.TasksIntersectException;
import ru.yandex.practicum.tracker.models.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

abstract class BaseHttpHandler<T extends Task> implements HttpHandler {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final Gson gson;

    BaseHttpHandler() {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET" -> get(exchange);
            case "POST" -> post(exchange);
            case "DELETE" -> delete(exchange);
        }
    }

    protected abstract List<T> list();

    protected abstract Optional<T> getById(long id);

    protected abstract long create(T value);

    protected abstract void update(T value);

    protected abstract void delete(long id);

    protected abstract Class<T> getType();

    protected abstract boolean checkIntersection(T value);

    protected void sendResponse(HttpExchange exchange, Object object, int code) throws IOException {
        Headers headers = exchange.getResponseHeaders();

        if (object == null) {
            headers.set("Content-Length", "0");
            exchange.sendResponseHeaders(code, 0);
        } else {
            headers.set("Content-Type", "application/json; charset=" + CHARSET);

            String json = gson.toJson(object);
            byte[] responseBody = json.getBytes(CHARSET);

            exchange.sendResponseHeaders(code, responseBody.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody);
            }
        }
    }

    protected void get(HttpExchange exchange) throws IOException {
        String[] pathComponents = exchange.getRequestURI().getPath().split("/");

        if (pathComponents.length == 2) {
            List<T> values = list();
            sendResponse(exchange, values, 200);
        } else if (pathComponents.length == 3) {
            try {
                long id = Long.parseLong(pathComponents[2]);
                Optional<T> optionalValue = getById(id);

                if (optionalValue.isEmpty()) {
                    throw new TaskNotFoundException("Task with id=" + id + " not found");
                }

                sendResponse(exchange, optionalValue.get(), 200);
            } catch (NumberFormatException | TaskNotFoundException exception) {
                sendResponse(exchange, null, 404);
            } catch (Exception exception) {
                sendResponse(exchange, null, 500);
            }
        } else {
            sendResponse(exchange, null, 404);
        }
    }

    protected void post(HttpExchange exchange) throws IOException {
        String[] pathComponents = exchange.getRequestURI().getPath().split("/");

        if (pathComponents.length == 2) {
            try {
                String json = new String(exchange.getRequestBody().readAllBytes(), CHARSET);
                T value = gson.fromJson(json, getType());
                long id = value.getId();
                Optional<T> optionalValue = getById(id);

                if (optionalValue.isEmpty()) {
                    if (!checkIntersection(value)) {
                        throw new TasksIntersectException("Can't create task");
                    }
                    id = create(value);
                } else {
                    update(value);
                }
                sendResponse(exchange, getById(id).get(), 200);
            } catch (JsonSyntaxException exception) {
                sendResponse(exchange, null, 400);
            } catch (TasksIntersectException exception) {
                sendResponse(exchange, null, 406);
            } catch (Exception exception) {
                sendResponse(exchange, null, 500);
            }
        } else {
            sendResponse(exchange, null, 404);
        }
    }

    protected void delete(HttpExchange exchange) throws IOException {
        String[] pathComponents = exchange.getRequestURI().getPath().split("/");

        if (pathComponents.length == 3) {
            try {
                long id = Long.parseLong(pathComponents[2]);
                delete(id);
                sendResponse(exchange, null, 200);
            } catch (NumberFormatException exception) {
                sendResponse(exchange, null, 404);
            } catch (Exception exception) {
                sendResponse(exchange, null, 500);
            }
        } else {
            sendResponse(exchange, null, 404);
        }
    }
}