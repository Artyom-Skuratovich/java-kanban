package ru.yandex.practicum.tracker.tasks.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.tasks.Epic;
import ru.yandex.practicum.tracker.tasks.Subtask;
import ru.yandex.practicum.tracker.tasks.Task;
import ru.yandex.practicum.tracker.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void createTaskManager() {
        taskManager = (InMemoryTaskManager) Managers.getDefault();
    }

    @Test
    public void shouldReturnTrueWhenTaskWasAddedAndGotById() {
        Task task = new Task(InMemoryTaskManager.generateId(), "Task", "", TaskStatus.NEW);
        taskManager.createOrdinaryTask(task);

        assertEquals(taskManager.getOrdinaryTask(task.getId()), Optional.of(task), "Id задач не совпадают");
    }

    @Test
    public void shouldReturnTaskListWithThreeItemsAfterCreationOfThreeTasks() {
        Task firstTask = new Task(InMemoryTaskManager.generateId(), "First", "", TaskStatus.DONE);
        Task secondTask = new Task(InMemoryTaskManager.generateId(), "Second", "", TaskStatus.DONE);
        Task thirdTask = new Task(InMemoryTaskManager.generateId(), "Third", "", TaskStatus.DONE);
        taskManager.createOrdinaryTask(firstTask);
        taskManager.createOrdinaryTask(secondTask);
        taskManager.createOrdinaryTask(thirdTask);
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);
        tasks.add(thirdTask);

        assertTrue(taskManager.getOrdinaryTasks().containsAll(tasks), "Списки задач не совпадают");
    }

    @Test
    public void shouldReturnOldEpicAfterCreationNewEpicWithDifferentPropertiesButSameId() {
        Epic oldEpic = new Epic(InMemoryTaskManager.generateId(), "Epic", "");
        Subtask subtask1 = new Subtask(InMemoryTaskManager.generateId(), "1", "", TaskStatus.NEW, oldEpic.getId());
        Subtask subtask2 = new Subtask(InMemoryTaskManager.generateId(), "2", "", TaskStatus.NEW, oldEpic.getId());

        ArrayList<Subtask> subtasks = new ArrayList<>();
        subtasks.add(subtask1);
        subtasks.add(subtask2);
        oldEpic.setSubtasks(subtasks);

        taskManager.createEpic(oldEpic);
        Epic newEpic = new Epic(oldEpic.getId(), "New Epic", "Rnd");
        subtasks.removeFirst();
        newEpic.setSubtasks(subtasks);
        taskManager.createEpic(newEpic);

        Optional<Epic> optionalEpic = taskManager.getEpic(oldEpic.getId());
        assertTrue(optionalEpic.isPresent(), "Задача не должна быть пустой");
        Epic fromTaskManager = optionalEpic.get();
        assertEquals(oldEpic, fromTaskManager, "Id не зовпадают");
        assertEquals(oldEpic.getName(), fromTaskManager.getName(), "Названия не совпадают");
        assertEquals(oldEpic.getDescription(), fromTaskManager.getDescription(), "Описания не совпадают");
        assertEquals(oldEpic.getStatus(), fromTaskManager.getStatus(), "Статусы не совпадают");
        assertTrue(oldEpic.getSubtasks().containsAll(fromTaskManager.getSubtasks()), "Списки подзадач не совпадают");
    }

    @Test
    public void shouldReturnEmptySubtaskAndEpicListsAfterRemoveAllSubtasks() {
        Epic firstEpic = new Epic(InMemoryTaskManager.generateId(), "Epic", "");
        Subtask subtask1 = new Subtask(InMemoryTaskManager.generateId(), "1", "", TaskStatus.NEW, firstEpic.getId());
        Subtask subtask2 = new Subtask(InMemoryTaskManager.generateId(), "2", "", TaskStatus.NEW, firstEpic.getId());
        taskManager.createEpic(firstEpic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic secondEpic = new Epic(InMemoryTaskManager.generateId(), "Epic2", "");
        subtask1 = new Subtask(InMemoryTaskManager.generateId(), "3", "", TaskStatus.DONE, secondEpic.getId());
        taskManager.createEpic(secondEpic);
        taskManager.createSubtask(subtask1);

        taskManager.removeAllEpics();

        assertTrue(taskManager.getEpics().isEmpty());
    }

    @Test
    public void updateEpic() {
        Epic epic = new Epic(InMemoryTaskManager.generateId(), "Epic", "");
        Subtask subtask1 = new Subtask(InMemoryTaskManager.generateId(), "1", "", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(InMemoryTaskManager.generateId(), "2", "", TaskStatus.NEW, epic.getId());
        taskManager.createEpic(epic);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        epic = new Epic(epic.getId(), "Updated", "New desc");
        taskManager.updateEpic(epic);
        taskManager.createSubtask(subtask1);

        ArrayList<Subtask> subtasks = new ArrayList<>();
        subtasks.add(subtask1);
        Optional<Epic> optionalEpic = taskManager.getEpic(epic.getId());
        assertTrue(optionalEpic.isPresent());
        Epic fromTaskManager = optionalEpic.get();
        assertEquals(epic.getName(), fromTaskManager.getName());
        assertEquals(epic.getDescription(), fromTaskManager.getDescription());
        assertEquals(TaskStatus.NEW, fromTaskManager.getStatus());
        assertTrue(subtasks.containsAll(fromTaskManager.getSubtasks()));
        assertTrue(taskManager.getSubtask(subtask2.getId()).isEmpty());
    }
}