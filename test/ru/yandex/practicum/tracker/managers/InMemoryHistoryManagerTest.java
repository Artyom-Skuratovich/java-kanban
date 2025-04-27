package ru.yandex.practicum.tracker.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void shouldReturnHistoryWithThreeTasksAfterCreationFourOnesAndRemovedOne() {
        Task task = new Task("Task", "Task");
        Subtask subtask = new Subtask("Subtask", "Subtask", 1);
        subtask.setId(task.getId() + 1);
        Epic firstEpic = new Epic("Epic", "Epic");
        firstEpic.setId(subtask.getId() + 1);
        Epic secondEpic = new Epic(firstEpic);
        secondEpic.setId(firstEpic.getId() + 1);
        List<Task> expected = List.of(task, subtask, secondEpic);

        historyManager.add(task);
        historyManager.add(subtask);
        historyManager.add(firstEpic);
        historyManager.add(secondEpic);
        historyManager.remove(firstEpic.getId());

        List<Task> actual = historyManager.getHistory();
        assertEquals(expected.size(), actual.size());
        assertTrue(expected.containsAll(actual));

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void shouldReturnEmptyListWhenNoOneTaskNotAdded() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void shouldReturnFalseWhenTryToCompareReferencesBetweenManualCreatedTaskAndTaskFromHistory() {
        Epic epic = new Epic("Epic", "Epic");
        historyManager.add(epic);

        assertNotSame(epic, historyManager.getHistory().getFirst());
    }
}