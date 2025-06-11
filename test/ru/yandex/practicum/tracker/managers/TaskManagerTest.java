package ru.yandex.practicum.tracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Status;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T getTaskManager();

    @BeforeEach
    public void setUp() {
        taskManager = getTaskManager();
    }

    @Test
    public void shouldReturnTaskListWithThreeItemsAfterCreationOfThreeTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task first = new Task("First", "First", now, Duration.ofMinutes(60));
        Task second = new Task("Second", "Second", now.plusHours(2), Duration.ofMinutes(10));
        Task third = new Task("Third", "Third", now.plusMinutes(70), Duration.ofMinutes(5));

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
        LocalDateTime now = LocalDateTime.now();
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask first = new Subtask("First", "First", now.plusDays(1), Duration.ofMinutes(4), epic.getId());
        Subtask second = new Subtask("Second", "Second", now.plusDays(2), Duration.ofMinutes(4), epic.getId());
        Subtask third = new Subtask("Third", "Third", now.plusDays(3), Duration.ofMinutes(4), epic.getId());

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
        LocalDateTime now = LocalDateTime.now();
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask subtask = new Subtask("Subtask", "Subtask", now.plusDays(1), Duration.ofMinutes(1), epic.getId());
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
        LocalDateTime now = LocalDateTime.now();
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask first = new Subtask("First", "First", now.plusDays(1), Duration.ofMinutes(5), epic.getId());
        Subtask second = new Subtask("Second", "Second", now.plusDays(2), Duration.ofMinutes(6), epic.getId());
        Subtask third = new Subtask("Third", "Third", now.plusDays(3), Duration.ofMinutes(7), epic.getId());

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
    public void shouldSetNewStatusForEpicWhenAllSubtasksAreNew() {
        LocalDateTime now = LocalDateTime.now();
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask first = new Subtask("First", "First", now.plusDays(1), Duration.ofMinutes(5), epic.getId());
        Subtask second = new Subtask("Second", "Second", now.plusDays(2), Duration.ofMinutes(6), epic.getId());
        Subtask third = new Subtask("Third", "Third", now.plusDays(3), Duration.ofMinutes(7), epic.getId());

        taskManager.createSubtask(first);
        taskManager.createSubtask(second);
        taskManager.createSubtask(third);

        List<Subtask> subtaskList = taskManager.getSubtaskListForEpic(epic.getId());
        subtaskList.forEach(s -> s.setStatus(Status.NEW));
        subtaskList.forEach(taskManager::updateSubtask);

        assertEquals(Status.NEW, taskManager.getEpicList().getFirst().getStatus(),
                "Статус эпика не NEW");

    }

    @Test
    public void shouldSetInProgressStatusForEpicWhenOneSubtaskHasDifferentStatus() {
        LocalDateTime now = LocalDateTime.now();
        Epic epic = new Epic("Epic", "Epic");
        taskManager.createEpic(epic);
        epic = taskManager.getEpicList().getFirst();

        Subtask first = new Subtask("First", "First", now.plusDays(1), Duration.ofMinutes(5), epic.getId());
        Subtask second = new Subtask("Second", "Second", now.plusDays(2), Duration.ofMinutes(6), epic.getId());
        Subtask third = new Subtask("Third", "Third", now.plusDays(3), Duration.ofMinutes(7), epic.getId());
        third.setStatus(Status.IN_PROGRESS);

        taskManager.createSubtask(first);
        taskManager.createSubtask(second);
        taskManager.createSubtask(third);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicList().getFirst().getStatus(),
                "Статус эпика не IN_PROGRESS");

    }

    @Test
    public void shouldReturnFalseWhenTryToCompareReferencesBetweenManualCreatedTaskAndTaskFromManager() {
        Task task = new Task("Task", "Task", LocalDateTime.now(), Duration.ofMinutes(60));
        taskManager.createTask(task);
        Task fromManager = taskManager.getTaskList().getFirst();

        assertNotSame(task, fromManager, "Ссылки не должны быть равны");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenTryToCreateSubtaskByUsingCreateTaskMethod() {
        Subtask subtask = new Subtask("Subtask", "Subtask", LocalDateTime.now(), Duration.ofMinutes(4), 1);

        Throwable thrown = assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(subtask),
                "Ошибка при выбросе исключения");
        assertEquals("Task must be only Task type", thrown.getMessage(), "Неверное сообщение в исключении");
    }

    @Test
    public void shouldReturnTrueWhenCheckIntersectionAndTimeIntervalIsAvailable() {
        LocalDateTime now = LocalDateTime.now();
        Task firstTask = new Task("Task", "Task", now, Duration.ofMinutes(10));
        Task secondTask = new Task("Task", "Task", now.plusMinutes(30), Duration.ofMinutes(50));

        taskManager.createTask(firstTask);

        assertTrue(taskManager.checkIntersection(secondTask), "Задачи пересекаются по времени выполнения");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenCheckIntersectWithInvalidStartTime() {
        Subtask subtask = new Subtask("Subtask", "Subtask", LocalDateTime.now().minusMinutes(16), Duration.ofMinutes(20), 0);
        Throwable thrown = assertThrows(IllegalArgumentException.class, () -> taskManager.checkIntersection(subtask),
                "Ошибка при выбросе исключения");
        assertEquals("Start time is out of range", thrown.getMessage(), "Неверное сообщение в исключении");
    }

    @Test
    public void shouldReturnPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task firstTask = new Task("Task", "Task", now.plusDays(4), Duration.ofMinutes(30));
        Task secondTask = new Task("Task", "Task", now, Duration.ofMinutes(30));

        long firstTaskId = taskManager.createTask(firstTask);
        long secondTaskId = taskManager.createTask(secondTask);

        Optional<Task> optionalFirstTask = taskManager.getTaskById(firstTaskId);
        Optional<Task> optionalSecondTask = taskManager.getTaskById(secondTaskId);

        assertTrue(optionalFirstTask.isPresent());
        assertTrue(optionalSecondTask.isPresent());

        List<Task> expected = List.of(optionalSecondTask.get());
        List<Task> actual = taskManager.getPrioritizedTasks();

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
}