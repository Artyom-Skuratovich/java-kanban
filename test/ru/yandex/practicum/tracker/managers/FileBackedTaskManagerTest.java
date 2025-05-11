package ru.yandex.practicum.tracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;
import ru.yandex.practicum.tracker.models.Tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File file;

    @BeforeEach
    public void setUp() {
        try {
            file = File.createTempFile("tasks-version-", ".csv");
        } catch (IOException ignored) {
        }
    }

    @Test
    public void shouldSaveAndLoadAnEmptyFile() {
        FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile(file, Managers.getDefaultHistory());

        assertTrue(taskManager.getTaskList().isEmpty());
        assertTrue(taskManager.getSubtaskList().isEmpty());
        assertTrue(taskManager.getEpicList().isEmpty());

        taskManager.save();

        try {
            assertEquals("", Files.readString(file.toPath(), StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            fail();
        }
    }

    @Test
    public void shouldCreateAndSaveEpicTaskAndSubtask() {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file.getPath());

        Epic epic = new Epic("Epic", "Epic");
        Task task = new Task("Task", "Task");
        taskManager.createEpic(epic);
        taskManager.createTask(task);
        epic = taskManager.getEpicList().getFirst();
        Subtask subtask = new Subtask("Subtask", "Subtask", epic.getId());
        taskManager.createSubtask(subtask);

        FileBackedTaskManager fromFile = FileBackedTaskManager.loadFromFile(file, Managers.getDefaultHistory());

        assertTrue(taskManager.getTaskList().containsAll(fromFile.getTaskList()));
        assertTrue(taskManager.getSubtaskList().containsAll(fromFile.getSubtaskList()));
        assertTrue(taskManager.getEpicList().containsAll(fromFile.getEpicList()));
    }

    @Test
    public void shouldLoadOnlyUsefulSubtasksFromFile() {
        FileBackedTaskManager firstManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file.getPath());
        Epic epic = new Epic("Epic", "Epic");
        Task task = new Task("Task", "Task");
        firstManager.createEpic(epic);
        firstManager.createTask(task);
        epic = firstManager.getEpicList().getFirst();
        Subtask subtask = new Subtask("Subtask", "Subtask", epic.getId());
        firstManager.createSubtask(subtask);

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, true)) {
            String unusedSubtask = Tasks.toString(new Subtask("Unused", "", Long.MIN_VALUE));
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