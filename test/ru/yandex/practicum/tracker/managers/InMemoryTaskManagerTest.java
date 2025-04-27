package ru.yandex.practicum.tracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Status;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void setUp() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
    }

    @Test
    public void shouldReturnTaskListWithThreeItemsAfterCreationOfThreeTasks() {
        Task first = new Task("First", "First");
        Task second = new Task("Second", "Second");
        Task third = new Task("Third", "Third");

        taskManager.createTask(first);
        taskManager.createTask(second);
        taskManager.createTask(third);

        assertTrue(taskManager.getEpicList().isEmpty(), "Список эпиков не пустой");
        assertTrue(taskManager.getSubtaskList().isEmpty(), "Список подзадач не пустой");
        assertEquals(3, taskManager.getTaskList().size(), "Размер списка задач не равен трём");
        assertTrue(taskManager.getTaskList().stream().anyMatch(task -> task.getName().equals(first.getName())),
                "Не найдено название первой задачи");
        assertTrue(taskManager.getTaskList().stream().anyMatch(task -> task.getName().equals(second.getName())),
                "Не найдено название второй задачи");
        assertTrue(taskManager.getTaskList().stream().anyMatch(task -> task.getName().equals(third.getName())),
                "Не найдено название третьей задачи");
    }

    @Test
    public void shouldReturnThreeSubtasksForEpicAfterCreationTheseOnes() {
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask first = new Subtask("First", "First", epic.getId());
        Subtask second = new Subtask("Second", "Second", epic.getId());
        Subtask third = new Subtask("Third", "Third", epic.getId());

        taskManager.createSubtask(first);
        taskManager.createSubtask(second);
        taskManager.createSubtask(third);

        assertEquals(3, taskManager.getSubtaskListForEpic(epic.getId()).size(),
                "Размер списка подзадач для эпика не равен трём");
        assertTrue(taskManager.getSubtaskListForEpic(epic.getId()).stream()
                        .anyMatch(s -> s.getName().equals(first.getName())),
                "Не найдено название первой подзадачи");
        assertTrue(taskManager.getSubtaskListForEpic(epic.getId()).stream()
                        .anyMatch(s -> s.getName().equals(second.getName())),
                "Не найдено название второй подзадачи");
        assertTrue(taskManager.getSubtaskListForEpic(epic.getId()).stream()
                        .anyMatch(s -> s.getName().equals(third.getName())),
                "Не найдено название третьей подзадачи");

        Optional<Epic> optionalEpic = taskManager.getEpicById(epic.getId());
        optionalEpic.ifPresent(e -> assertEquals(3, e.getSubtaskIds().size(),
                "Размер списка подзадач для эпика не равен трём"));
    }

    @Test
    public void shouldReturnOneSubtaskForEpicWhenEpicHasListWithThreeSubtaskIdsButAddedOnlyOne() {
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask subtask = new Subtask("Subtask", "Subtask", epic.getId());
        taskManager.createSubtask(subtask);
        subtask = taskManager.getSubtaskListForEpic(epic.getId()).getFirst();

        epic.addSubtaskId(subtask.getId());
        epic.addSubtaskId(12899);
        epic.addSubtaskId(9876500);
        taskManager.updateEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        assertEquals(1, epic.getSubtaskIds().size(),
                "Размер списка идентификатор подзадач не равен 3");
        assertTrue(epic.getSubtaskIds().contains(subtask.getId()),
                "Список идентификаторов подзадач не содержит id подзадачи");
        assertEquals(1, taskManager.getSubtaskListForEpic(epic.getId()).size(),
                "Размер списка подзадач из TaskManager не равен 1");
        assertTrue(taskManager.getSubtaskListForEpic(epic.getId()).contains(subtask),
                "Список подзадач эпика не содержит нужной подзадачи");
    }

    @Test
    public void shouldSetDoneStatusForEpicWhenAllSubtasksAreDone() {
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask first = new Subtask("First", "First", epic.getId());
        Subtask second = new Subtask("Second", "Second", epic.getId());
        Subtask third = new Subtask("Third", "Third", epic.getId());

        taskManager.createSubtask(first);
        taskManager.createSubtask(second);
        taskManager.createSubtask(third);

        List<Subtask> subtaskList = taskManager.getSubtaskListForEpic(epic.getId());
        subtaskList.forEach(s -> s.setStatus(Status.DONE));
        subtaskList.forEach(taskManager::updateSubtask);

        assertEquals(Status.DONE, taskManager.getEpicList().getFirst().getStatus(),
                "Статус эпика не DONE");
    }

    @Test
    public void shouldReturnFalseWhenTryToCompareReferencesBetweenManualCreatedTaskAndTaskFromManager() {
        Task task = new Task("Task", "Task");
        taskManager.createTask(task);
        Task fromManager = taskManager.getTaskList().getFirst();

        assertNotSame(task, fromManager);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenTryToCreateSubtaskByUsingCreateTaskMethod() {
        Subtask subtask = new Subtask("Subtask", "Subtask", 1);

        Throwable thrown = assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(subtask));
        assertEquals("Task must be only Task type", thrown.getMessage());
    }
}