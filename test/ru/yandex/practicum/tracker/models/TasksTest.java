package ru.yandex.practicum.tracker.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {
    @Test
    public void shouldReturnStringRepresentsTask() {
        Task task = new Task("Task", "Task description");
        task.setId(12);
        task.setStatus(Status.DONE);
        String expected = "12,TASK,Task,DONE,Task description,";

        String actual = Tasks.toString(task);

        assertEquals(expected, actual);
    }
}