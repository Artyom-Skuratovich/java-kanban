package ru.yandex.practicum.tracker.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    public void shouldReturnTrueWhenTasksIdsAreEqual() {
        Task firstTask = new Task(1, "", "", TaskStatus.IN_PROGRESS);
        Task secondTask = new Task(1, "Random name", "Random description", TaskStatus.DONE);

        assertEquals(firstTask, secondTask, "Задачи с одинаковыми id должны совпадать");
    }

    @Test
    public void shouldReturnTrueWhenEpicsIdsAreEqual() {
        Epic firstEpic = new Epic(1, "First", "");
        Epic secondEpic = new Epic(1, "Second", "");

        assertEquals(firstEpic, secondEpic, "Задачи с одинаковыми id должны совпадать");
    }

    @Test
    public void shouldReturnFalseWhenTasksIdsAreNotEqual() {
        Task firstTask = new Task(1, "First", "", TaskStatus.IN_PROGRESS);
        Task secondTask = new Task(2, "First", "", TaskStatus.IN_PROGRESS);

        assertNotEquals(firstTask, secondTask, "Задачи с разными id не должны совпадать");
    }
}