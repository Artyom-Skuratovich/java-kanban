package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.tasks.Epic;
import ru.yandex.practicum.tracker.tasks.Subtask;
import ru.yandex.practicum.tracker.tasks.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    List<Task> getOrdinaryTasks();

    List<Task> getHistory();

    List<Subtask> getSubtasksForEpic(int epicId);

    void removeAllEpics();

    void removeAllTasks();

    void removeAllSubtasks();

    Optional<Task> getOrdinaryTask(int id);

    Optional<Epic> getEpic(int id);

    Optional<Subtask> getSubtask(int id);

    void createOrdinaryTask(Task ordinaryTask);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask);

    void removeOrdinaryTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    void updateOrdinaryTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);
}