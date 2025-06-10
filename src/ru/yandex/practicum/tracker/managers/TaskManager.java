package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    List<Task> getTaskList();

    List<Subtask> getSubtaskList();

    List<Subtask> getSubtaskListForEpic(long epicId);

    List<Epic> getEpicList();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    Optional<Task> getTaskById(long id);

    Optional<Subtask> getSubtaskById(long id);

    Optional<Epic> getEpicById(long id);

    boolean checkIntersection(Task task);

    long createTask(Task task);

    long createSubtask(Subtask subtask);

    long createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void removeTask(long id);

    void removeSubtask(long id);

    void removeEpic(long id);

    void removeAllTasks();

    void removeAllSubtasks();

    void removeAllEpics();
}