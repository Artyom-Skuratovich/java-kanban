package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.tasks.Epic;
import ru.yandex.practicum.tracker.tasks.Subtask;
import ru.yandex.practicum.tracker.tasks.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> ordinaryTasksMap;
    private final HashMap<Integer, Subtask> subtasksMap;
    private final HashMap<Integer, Epic> epicsMap;
    private final HistoryManager historyManager;
    private static int nextId = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        if (historyManager == null) {
            throw new IllegalArgumentException();
        }
        this.historyManager = historyManager;
        ordinaryTasksMap = new HashMap<>();
        subtasksMap = new HashMap<>();
        epicsMap = new HashMap<>();
    }

    public static synchronized int generateId() {
        return nextId++;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicsMap.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasksMap.values());
    }

    @Override
    public List<Task> getOrdinaryTasks() {
        return new ArrayList<>(ordinaryTasksMap.values());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Subtask> getSubtasksForEpic(int epicId) {
        Epic epic = epicsMap.get(epicId);
        if (epic != null) {
            return epic.getSubtasks();
        }
        return Collections.emptyList();
    }

    @Override
    public void removeAllEpics() {
        epicsMap.clear();
    }

    @Override
    public void removeAllTasks() {
        ordinaryTasksMap.clear();
    }

    @Override
    public void removeAllSubtasks() {
        subtasksMap.clear();
        epicsMap.forEach((i, e) -> e.setSubtasks(Collections.emptyList()));
    }

    @Override
    public Optional<Task> getOrdinaryTask(int id) {
        Optional<Task> ordinaryTask = Optional.ofNullable(ordinaryTasksMap.get(id));
        ordinaryTask.ifPresent(historyManager::add);
        return ordinaryTask;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Optional<Epic> epic = Optional.ofNullable(epicsMap.get(id));
        epic.ifPresent(historyManager::add);
        return epic;
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        Optional<Subtask> subtask = Optional.ofNullable(subtasksMap.get(id));
        subtask.ifPresent(historyManager::add);
        return subtask;
    }

    @Override
    public void createOrdinaryTask(Task ordinaryTask) {
        if (ordinaryTask == null) {
            throw new IllegalArgumentException();
        }
        ordinaryTasksMap.putIfAbsent(ordinaryTask.getId(), ordinaryTask);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException();
        }
        Epic previousEpic = epicsMap.putIfAbsent(epic.getId(), epic);
        if (previousEpic == null) {
            List<Subtask> subtasks = epic.getSubtasks();
            subtasks.forEach(s -> subtasksMap.put(s.getId(), s));
        }
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException();
        }
        Epic parentEpic = epicsMap.get(subtask.getParentEpicId());
        if ((parentEpic != null) && (subtasksMap.putIfAbsent(subtask.getId(), subtask) == null)) {
            epicsMap.put(parentEpic.getId(), parentEpic);
            List<Subtask> subtasks = parentEpic.getSubtasks();
            subtasks.add(subtask);
            parentEpic.setSubtasks(subtasks);
        }
    }

    @Override
    public void removeOrdinaryTask(int id) {
        ordinaryTasksMap.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        Epic removedEpic = epicsMap.remove(id);
        if (removedEpic != null) {
            removedEpic.getSubtasks().forEach(s -> subtasksMap.remove(s.getId()));
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask removedSubtask = subtasksMap.remove(id);
        if (removedSubtask != null) {
            Epic parentEpic = epicsMap.get(removedSubtask.getParentEpicId());
            if (parentEpic != null) {
                List<Subtask> subtasks = parentEpic.getSubtasks();
                subtasks.remove(removedSubtask);
                parentEpic.setSubtasks(subtasks);
            }
        }
    }

    @Override
    public void updateOrdinaryTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }
        if (ordinaryTasksMap.containsKey(task.getId())) {
            ordinaryTasksMap.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException();
        }
        if (epicsMap.containsKey(epic.getId())) {
            Epic oldEpic = epicsMap.put(epic.getId(), epic);
            List<Subtask> newSubtasks = epic.getSubtasks();
            if (oldEpic != null) {
                List<Subtask> oldSubtasks = oldEpic.getSubtasks();
                List<Subtask> forRemove = oldSubtasks.stream().filter(s -> !newSubtasks.contains(s)).toList();
                forRemove.forEach(s -> subtasksMap.remove(s.getId()));
            }
            newSubtasks.forEach(s -> subtasksMap.put(s.getId(), s));
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException();
        }
        if (subtasksMap.containsKey(subtask.getId())) {
            subtasksMap.put(subtask.getId(), subtask);
            Epic parentEpic = epicsMap.get(subtask.getParentEpicId());
            if (parentEpic != null) {
                List<Subtask> subtasks = parentEpic.getSubtasks();
                subtasks.add(subtask);
                parentEpic.setSubtasks(subtasks);
                epicsMap.put(parentEpic.getId(), parentEpic);
            }
        }
    }
}