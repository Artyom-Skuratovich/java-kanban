package ru.yandex.practicum.tracker.managers;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;
import ru.yandex.practicum.tracker.utils.TaskSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File file;

    @Override
    protected FileBackedTaskManager getTaskManager() {
        try {
            file = File.createTempFile("tasks-version-", ".csv");
            return new FileBackedTaskManager(Managers.getDefaultHistory(), file.getPath());
        } catch (IOException exception) {
            throw new RuntimeException("Can't create file", exception);
        }
    }

    @Test
    public void shouldSaveAndLoadAnEmptyFile() {
        FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile(file, Managers.getDefaultHistory());

        assertTrue(taskManager.getTaskList().isEmpty(), "Список задач не пустой");
        assertTrue(taskManager.getSubtaskList().isEmpty(), "Список подзадач не пустой");
        assertTrue(taskManager.getEpicList().isEmpty(), "Список эпиков не пустой");

        taskManager.save();

        try {
            assertEquals("", Files.readString(file.toPath(), StandardCharsets.UTF_8), "Файл не пустой");
        } catch (IOException ignored) {
            fail();
        }
    }

    @Test
    public void shouldLoadOnlyUsefulSubtasksFromFile() {
        FileBackedTaskManager firstManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file.getPath());
        LocalDateTime now = LocalDateTime.now();
        Epic epic = new Epic("Epic", "Epic");
        Task task = new Task("Task", "Task", now.plusDays(1), Duration.ofMinutes(50));
        firstManager.createEpic(epic);
        firstManager.createTask(task);
        epic = firstManager.getEpicList().getFirst();
        Subtask subtask = new Subtask("Subtask", "Subtask", now.plusMinutes(50), Duration.ofMinutes(10), epic.getId());
        firstManager.createSubtask(subtask);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, true)) {
            String unusedSubtask = new TaskSerializer(",", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toString(new Subtask("Unused", "", now.plusDays(2), Duration.ofMinutes(120), Long.MIN_VALUE));
            writer.write(System.lineSeparator() + unusedSubtask);
        } catch (IOException ignored) {
            fail();
        }

        FileBackedTaskManager secondManager = FileBackedTaskManager.loadFromFile(file, Managers.getDefaultHistory());

        assertTrue(firstManager.getTaskList().containsAll(secondManager.getTaskList()));
        assertTrue(firstManager.getSubtaskList().containsAll(secondManager.getSubtaskList()));
        assertTrue(firstManager.getEpicList().containsAll(secondManager.getEpicList()));
    }
}