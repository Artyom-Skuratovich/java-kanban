package ru.yandex.practicum.tracker.tasks.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.tracker.tasks.Epic;
import ru.yandex.practicum.tracker.tasks.Subtask;
import ru.yandex.practicum.tracker.tasks.Task;
import ru.yandex.practicum.tracker.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    public void createHistoryManager() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    public void addNewEpic() {
        Epic epic = new Epic(1, "Epic name", "Epic desc");
        Subtask subtask = new Subtask(2, "Sub1", "", TaskStatus.NEW, epic.getId());
        ArrayList<Subtask> subtasks = new ArrayList<>();
        subtasks.add(subtask);
        epic.setSubtasks(subtasks);
        historyManager.add(epic);

        subtasks.add(new Subtask(3, "", "", TaskStatus.IN_PROGRESS, epic.getId()));
        epic.setSubtasks(subtasks);

        List<Task> fromHistory = historyManager.getHistory();
        assertNotNull(fromHistory, "История не должна быть пустой");
        assertEquals(1, fromHistory.size(), "Размер истории должен быть равен единице");
    }

    @Test
    public void addElevenTasks() {
        for (int i = 0; i < 11; i++) {
            Task task = new Task(i, "Task" + i, "", TaskStatus.IN_PROGRESS);
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size(), "Размер истории не должен превышать 10 элементов");
    }
}