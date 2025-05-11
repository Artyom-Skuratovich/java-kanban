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

    @Test
    public void shouldReturnStringRepresentsSubtask() {
        Subtask subtask = new Subtask("Subtask", "Subtask description", 2);
        subtask.setId(1);
        subtask.setStatus(Status.IN_PROGRESS);
        String expected = "1,SUBTASK,Subtask,IN_PROGRESS,Subtask description,2";

        String actual = Tasks.toString(subtask);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldReturnStringRepresentsEpic() {
        Epic epic = new Epic("Epic", "Epic description");
        epic.setId(2);
        epic.setStatus(Status.NEW);
        String expected = "2,EPIC,Epic,NEW,Epic description,";

        String actual = Tasks.toString(epic);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldReturnTaskWhenStringRepresentsTask() {
        String value = "12,TASK,Task,DONE,Task description,";
        Task expected = new Task("Task", "Task description");
        expected.setId(12);
        expected.setStatus(Status.DONE);

        Task actual = Tasks.fromString(value);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    public void shouldReturnSubtaskWhenStringRepresentsSubtask() {
        String value = "1,SUBTASK,Subtask,IN_PROGRESS,Subtask description,2";
        Subtask expected = new Subtask("Subtask", "Subtask description", 2);
        expected.setId(1);
        expected.setStatus(Status.IN_PROGRESS);

        Task actual = Tasks.fromString(value);

        assertInstanceOf(Subtask.class, actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getParentEpicId(), ((Subtask) actual).getParentEpicId());
    }

    @Test
    public void shouldReturnEpicWhenStringRepresentsEpic() {
        String value = "2,EPIC,Epic,NEW,Epic description,";
        Epic expected = new Epic("Epic", "Epic description");
        expected.setId(2);
        expected.setStatus(Status.NEW);

        Task actual = Tasks.fromString(value);

        assertInstanceOf(Epic.class, actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getStatus(), actual.getStatus());
    }
}