package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;
import ru.yandex.practicum.tracker.models.Status;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static long nextId = 1;

    private final Map<Long, Task> taskMap;
    private final Map<Long, Subtask> subtaskMap;
    private final Map<Long, Epic> epicMap;

    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        if (historyManager == null) {
            throw new IllegalArgumentException("History manager cannot be null");
        }

        this.historyManager = historyManager;
        taskMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        epicMap = new HashMap<>();
    }

    @Override
    public List<Task> getTaskList() {
        return taskMap.values().stream().map(Task::new).toList();
    }

    @Override
    public List<Subtask> getSubtaskList() {
        return subtaskMap.values().stream().map(Subtask::new).toList();
    }

    @Override
    public List<Subtask> getSubtaskListForEpic(long epicId) {
        if (!epicMap.containsKey(epicId)) return Collections.emptyList();
        return subtaskMap.values().stream()
                .filter(s -> s.getParentEpicId() == epicId)
                .map(Subtask::new).toList();
    }

    @Override
    public List<Epic> getEpicList() {
        return epicMap.values().stream().map(Epic::new).toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Optional<Task> getTaskById(long id) {
        Task task = taskMap.get(id);
        if (task != null) {
            Task copy = new Task(task);
            historyManager.add(copy);
            return Optional.of(copy);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subtask> getSubtaskById(long id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            Subtask copy = new Subtask(subtask);
            historyManager.add(copy);
            return Optional.of(copy);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Epic> getEpicById(long id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            Epic copy = new Epic(epic);
            historyManager.add(copy);
            return Optional.of(copy);
        }
        return Optional.empty();
    }

    @Override
    public void createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if ((task instanceof Subtask) || (task instanceof Epic)) {
            throw new IllegalArgumentException("Task must be only Task type");
        }

        Task copy = new Task(task);
        copy.setId(getNextId());
        taskMap.put(copy.getId(), copy);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask cannot be null");
        }
        Epic parentEpic = epicMap.get(subtask.getParentEpicId());
        if (parentEpic == null) {
            throw new IllegalArgumentException("Parent epic doesn't exist");
        }

        Subtask copy = new Subtask(subtask);
        copy.setId(getNextId());
        subtaskMap.put(copy.getId(), copy);
        parentEpic.addSubtaskId(copy.getId());
        changeEpicStatus(parentEpic);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Epic cannot be null");
        }
        Epic copy = new Epic(epic);
        copy.setId(getNextId());
        epicMap.put(copy.getId(), copy);
        changeEpicStatus(copy);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if ((task instanceof Subtask) || (task instanceof Epic)) {
            throw new IllegalArgumentException("Task must be only Task type");
        }

        if (taskMap.containsKey(task.getId())) {
            Task copy = new Task(task);
            taskMap.put(copy.getId(), copy);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask cannot be null");
        }
        Epic parentEpic = epicMap.get(subtask.getParentEpicId());
        if (parentEpic == null) {
            throw new IllegalArgumentException("Parent epic doesn't exist");
        }
        if (parentEpic.getId() == subtask.getId()) {
            throw new IllegalArgumentException("Subtask cannot be its own epic");
        }

        Subtask innerSubtask = subtaskMap.get(subtask.getId());
        if (innerSubtask != null) {
            if (innerSubtask.getParentEpicId() != subtask.getParentEpicId()) {
                Epic oldParentEpic = epicMap.get(innerSubtask.getParentEpicId());
                if (oldParentEpic != null) {
                    oldParentEpic.removeSubtaskId(innerSubtask.getId());
                }
            }

            Subtask copy = new Subtask(subtask);
            subtaskMap.put(copy.getId(), copy);
            parentEpic.addSubtaskId(copy.getId());
            changeEpicStatus(parentEpic);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Epic cannot be null");
        }

        if (epicMap.containsKey(epic.getId())) {
            Epic copy = new Epic(epic);
            epicMap.put(copy.getId(), copy);
            changeEpicStatus(copy);
        }
    }

    @Override
    public void removeTask(long id) {
        taskMap.remove(id);
    }

    @Override
    public void removeSubtask(long id) {
        Subtask removedSubtask = subtaskMap.remove(id);
        if (removedSubtask != null) {
            Epic parentEpic = epicMap.get(removedSubtask.getParentEpicId());
            if (parentEpic != null) {
                parentEpic.removeSubtaskId(id);
                changeEpicStatus(parentEpic);
            }
        }
    }

    @Override
    public void removeEpic(long id) {
        Epic removedEpic = epicMap.remove(id);
        if (removedEpic != null) {
            removedEpic.getSubtaskIds().forEach(subtaskMap::remove);
        }
    }

    @Override
    public void removeAllTasks() {
        taskMap.clear();
    }

    @Override
    public void removeAllSubtasks() {
        subtaskMap.clear();
        Collection<Epic> epics = epicMap.values();
        for (Epic epic : epics) {
            epic.removeAllSubtaskIds();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public void removeAllEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    private static synchronized long getNextId() {
        return nextId++;
    }

    private static boolean checkSubtasksStatus(List<Subtask> subtaskList, Status status) {
        return subtaskList.stream().allMatch(s -> s.getStatus().equals(status));
    }

    private void changeEpicStatus(Epic epic) {
        List<Subtask> subtaskList = subtaskMap.values().stream()
                .filter(s -> s.getParentEpicId() == epic.getId()).toList();
        if (subtaskList.isEmpty() || checkSubtasksStatus(subtaskList, Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else if (checkSubtasksStatus(subtaskList, Status.DONE)) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}