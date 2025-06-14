package ru.yandex.practicum.tracker.api;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.tracker.api.handlers.*;
import ru.yandex.practicum.tracker.managers.FileBackedTaskManager;
import ru.yandex.practicum.tracker.managers.InMemoryHistoryManager;
import ru.yandex.practicum.tracker.managers.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler(manager));
        server.createContext("/subtasks", new SubtaskHandler(manager));
        server.createContext("/epics", new EpicHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public static void main(String[] args) {
        HttpTaskServer apiServer = null;
        try {
            File file = File.createTempFile("tasks-", ".csv");
            TaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file.getPath());
            apiServer = new HttpTaskServer(manager);
            apiServer.start();
            System.out.println("Server started on port " + PORT);
        } catch (Throwable throwable) {
            System.out.println(throwable.getMessage());
        } finally {
            if (apiServer != null) {
                apiServer.stop();
            }
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}