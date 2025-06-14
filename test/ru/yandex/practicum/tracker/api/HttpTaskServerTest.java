package ru.yandex.practicum.tracker.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.api.adapters.DurationAdapter;
import ru.yandex.practicum.tracker.api.adapters.LocalDateTimeAdapter;
import ru.yandex.practicum.tracker.managers.InMemoryHistoryManager;
import ru.yandex.practicum.tracker.managers.InMemoryTaskManager;
import ru.yandex.practicum.tracker.managers.TaskManager;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private final TaskManager manager;
    private final HttpTaskServer server;
    private final Gson gson;

    HttpTaskServerTest() throws IOException {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        server = new HttpTaskServer(manager);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    @BeforeEach
    public void setUp() {
        manager.removeAllTasks();
        manager.removeAllSubtasks();
        manager.removeAllEpics();

        server.start();
    }

    @AfterEach
    public void shutDown() {
        server.stop();
    }

    // POST requests.

    @Test
    public void shouldAddNewTaskAndReturnStatus201() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Task task = new Task("Task", "Some description", LocalDateTime.now(), Duration.ofMinutes(60));
            String json = gson.toJson(task);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode(), response.body());

            List<Task> tasksFromManager = manager.getTaskList();

            assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
            assertEquals(task.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void shouldUpdateTaskAndReturnStatus201() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Task task = new Task("Task", "Some description", LocalDateTime.now(), Duration.ofMinutes(60));
            long id = manager.createTask(task);
            Optional<Task> optionalTask = manager.getTaskById(id);

            assertTrue(optionalTask.isPresent(), "Задача не была добавлена в TaskManager");
            assertEquals(task.getName(), optionalTask.get().getName(), "Из TaskManager получена неверная задача");

            task = optionalTask.get();
            task.setStartTime(task.getStartTime().plusDays(30));
            String json = gson.toJson(task);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode(), response.body());

            optionalTask = manager.getTaskById(task.getId());

            assertTrue(optionalTask.isPresent(), "Обновлённая задача не найдена в TaskManager");
            assertEquals(task.getStartTime(), optionalTask.get().getStartTime(), "Время не обновлено");
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void shouldReturnStatus406WhenTryToCreateTaskWithBusyStartTime() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Task first = new Task("First", "", LocalDateTime.now(), Duration.ofMinutes(60));
            manager.createTask(first);

            Task second = new Task(first);
            second.setStartTime(second.getStartTime().plusMinutes(50));
            String json = gson.toJson(second);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(406, response.statusCode(), response.body());
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void shouldReturnStatus400WhenJsonBodyIsIncorrect() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Task task = new Task("Task", "Some description", LocalDateTime.now(), Duration.ofMinutes(60));
            String json = gson.toJson(task).replace("}", "");

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(400, response.statusCode(), response.body());
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    // GET requests.

    @Test
    public void shouldReturnStatus200WithJsonOfTaskList() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Task first = new Task("Task", "Some description", LocalDateTime.now(), Duration.ofMinutes(60));
            Task second = new Task("Task 1", "", LocalDateTime.now().plusMinutes(80), Duration.ofMinutes(60));
            Task third = new Task("Task 2", "", LocalDateTime.now().plusDays(1), Duration.ofMinutes(10));
            manager.createTask(first);
            manager.createTask(second);
            manager.createTask(third);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/tasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), response.body());

            List<Task> fromResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
            }.getType());

            assertEquals(manager.getTaskList().size(), fromResponse.size(), "Количество элементов в коллекциях не совпадает");
            assertTrue(manager.getTaskList().containsAll(fromResponse), "Задачи в коллекциях не равны");
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void shouldReturnStatus200WithJsonEpicObject() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Epic epic = new Epic("Epic", "Description");
            long id = manager.createEpic(epic);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/epics/" + id);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), response.body());

            Epic fromApi = gson.fromJson(response.body(), Epic.class);

            assertEquals(epic.getName(), fromApi.getName(), "Имена эпиков не совпадают");
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void shouldReturnStatus200WithEpicWithStartTimeAndOneSubtaskId() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Epic epic = new Epic("Epic", "Description");
            long epicId = manager.createEpic(epic);
            Subtask subtask = new Subtask("Subtask", "Description", LocalDateTime.now(), Duration.ofMinutes(10), epicId);
            long subtaskId = manager.createSubtask(subtask);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/epics/" + epicId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), response.body());

            Epic epicFromApi = gson.fromJson(response.body(), Epic.class);

            assertEquals(epic.getName(), epicFromApi.getName(), "Имена эпиков не совпадают");
            assertEquals(subtask.getStartTime(), epicFromApi.getStartTime(), "Даты начала не совпадают");
            assertEquals(subtask.getDuration(), epicFromApi.getDuration(), "Продолжительность не совпадает");
            assertEquals(1, epicFromApi.getSubtaskIds().size(), "Количество подзадач не совпадает");
            assertEquals(subtaskId, epicFromApi.getSubtaskIds().getFirst(), "Id подзадачи неверное");
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void shouldReturnStatus200WithJsonSubtaskListForEpic() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Epic epic = new Epic("Epic", "Description");
            long epicId = manager.createEpic(epic);
            Subtask firstSubtask = new Subtask("Subtask 1", "Description", LocalDateTime.now(), Duration.ofMinutes(10), epicId);
            manager.createSubtask(firstSubtask);
            Subtask secondSubtask = new Subtask("Subtask 2", "Description", LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(1), epicId);
            manager.createSubtask(secondSubtask);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/epics/" + epicId + "/subtasks");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), response.body());

            List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
            }.getType());

            assertEquals(2, subtasks.size(), "Размер списка подзадач неправильный");
            assertTrue(manager.getSubtaskListForEpic(epicId).containsAll(subtasks), "Подзадачи в списке неверные");
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    // DELETE requests.

    @Test
    public void shouldReturnStatus200WhenDeleteSubtask() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Epic epic = new Epic("Epic", "Description");
            long epicId = manager.createEpic(epic);
            Subtask subtask = new Subtask("Subtask 1", "Description", LocalDateTime.now(), Duration.ofMinutes(10), epicId);
            long subtaskId = manager.createSubtask(subtask);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/subtasks/" + subtaskId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode(), response.body());
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void shouldReturnStatus400WhenDeleteSubtaskWithIncorrectId() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            Epic epic = new Epic("Epic", "Description");
            long epicId = manager.createEpic(epic);
            Subtask subtask = new Subtask("Subtask 1", "Description", LocalDateTime.now(), Duration.ofMinutes(10), epicId);
            manager.createSubtask(subtask);

            URI uri = URI.create("http://localhost:" + HttpTaskServer.PORT + "/subtasks/qwerty99");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(400, response.statusCode(), response.body());
        } catch (Throwable exception) {
            fail(exception.getMessage());
        }
    }
}