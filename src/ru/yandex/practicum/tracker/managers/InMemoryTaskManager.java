package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.exceptions.TasksIntersectException;
import ru.yandex.practicum.tracker.models.*;
import ru.yandex.practicum.tracker.utils.TaskScheduler;
import ru.yandex.practicum.tracker.utils.TaskSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private static long nextId = 1;

    protected final Map<Long, Task> taskMap;
    protected final Map<Long, Subtask> subtaskMap;
    protected final Map<Long, Epic> epicMap;

    protected final Set<Task> prioritizedTasks;
    protected final TaskScheduler scheduler;

    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        Objects.requireNonNull(historyManager, "History manager can't be null");
        this.historyManager = historyManager;
        taskMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        epicMap = new HashMap<>();

        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        LocalDateTime now = LocalDateTime.now();
        scheduler = new TaskScheduler(now, now.plusYears(1));
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
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().map(TaskSerializer::copyTask).toList();
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
    public boolean checkIntersection(Task task) {
        Objects.requireNonNull(task, "Task cannot be null");
        return scheduler.isAvailable(task.getStartTime(), task.getEndTime());
    }

    @Override
    public long createTask(Task task) {
        Objects.requireNonNull(task, "Task can't be null");
        if ((task instanceof Subtask) || (task instanceof Epic)) {
            throw new IllegalArgumentException("Task must be only Task type");
        }

        Task copy = new Task(task);
        copy.setId(getNextId());

        scheduleTask(copy, taskMap, false);
        taskMap.put(copy.getId(), copy);
        addToPriorityList(copy);

        return copy.getId();
    }

    @Override
    public long createSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "Subtask can't be null");
        Epic parentEpic = epicMap.get(subtask.getParentEpicId());
        if (parentEpic == null) {
            throw new IllegalArgumentException("Parent epic doesn't exist");
        }

        Subtask copy = new Subtask(subtask);
        copy.setId(getNextId());

        scheduleTask(copy, subtaskMap, false);
        subtaskMap.put(copy.getId(), copy);
        parentEpic.addSubtaskId(copy.getId());
        changeEpicStatus(parentEpic);
        setStartTimeAndDurationForEpic(parentEpic);
        addToPriorityList(copy);

        return copy.getId();
    }

    @Override
    public long createEpic(Epic epic) {
        Objects.requireNonNull(epic, "Epic can't be null");
        Epic copy = new Epic(epic);
        copy.setId(getNextId());

        epicMap.put(copy.getId(), copy);
        removeAllUnusedSubtaskIds(copy);
        changeEpicStatus(copy);
        setStartTimeAndDurationForEpic(copy);
        addToPriorityList(copy);

        return copy.getId();
    }

    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "Task can't be null");
        if ((task instanceof Subtask) || (task instanceof Epic)) {
            throw new IllegalArgumentException("Task must be only Task type");
        }

        if (taskMap.containsKey(task.getId())) {
            Task copy = new Task(task);

            scheduleTask(copy, taskMap, true);
            taskMap.put(copy.getId(), copy);
            addToPriorityList(copy);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "Subtask can't be null");
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

            scheduleTask(copy, subtaskMap, true);
            subtaskMap.put(copy.getId(), copy);
            parentEpic.addSubtaskId(copy.getId());
            changeEpicStatus(parentEpic);
            setStartTimeAndDurationForEpic(parentEpic);
            addToPriorityList(copy);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        Objects.requireNonNull(epic, "Epic can't be null");
        if (epicMap.containsKey(epic.getId())) {
            Epic copy = new Epic(epic);

            epicMap.put(copy.getId(), copy);
            removeAllUnusedSubtaskIds(copy);
            changeEpicStatus(copy);
            setStartTimeAndDurationForEpic(copy);
            addToPriorityList(copy);
        }
    }

    @Override
    public void removeTask(long id) {
        Task removedTask = taskMap.remove(id);
        if (removedTask != null) {
            scheduler.removeSchedule(removedTask);
            historyManager.remove(id);
            prioritizedTasks.removeIf(t -> t.getId() == id);
        }
    }

    @Override
    public void removeSubtask(long id) {
        Subtask removedSubtask = subtaskMap.remove(id);
        if (removedSubtask != null) {
            scheduler.removeSchedule(removedSubtask);
            Epic parentEpic = epicMap.get(removedSubtask.getParentEpicId());
            if (parentEpic != null) {
                parentEpic.removeSubtaskId(id);
                changeEpicStatus(parentEpic);
                setStartTimeAndDurationForEpic(parentEpic);
            }
        }
        historyManager.remove(id);
        prioritizedTasks.removeIf(t -> t.getId() == id);
    }

    @Override
    public void removeEpic(long id) {
        Epic removedEpic = epicMap.remove(id);
        if (removedEpic != null) {
            removedEpic.getSubtaskIds().forEach(subtaskId -> {
                Subtask subtask = subtaskMap.get(subtaskId);
                if (subtask != null) {
                    scheduler.removeSchedule(subtask);
                }
                subtaskMap.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
        }
        historyManager.remove(id);
        prioritizedTasks.removeIf(t -> t.getId() == id);
    }

    @Override
    public void removeAllTasks() {
        taskMap.forEach((id, task) -> {
            historyManager.remove(id);
            prioritizedTasks.remove(task);
            scheduler.removeSchedule(task);
        });
        taskMap.clear();
    }

    @Override
    public void removeAllSubtasks() {
        subtaskMap.forEach((id, subtask) -> {
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);
            scheduler.removeSchedule(subtask);
        });
        subtaskMap.clear();
        Collection<Epic> epics = epicMap.values();
        for (Epic epic : epics) {
            epic.removeAllSubtaskIds();
            epic.setStatus(Status.NEW);
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
        }
    }

    @Override
    public void removeAllEpics() {
        subtaskMap.forEach((id, subtask) -> {
            historyManager.remove(id);
            prioritizedTasks.remove(subtask);
            scheduler.removeSchedule(subtask);
        });
        epicMap.forEach((id, epic) -> {
            historyManager.remove(id);
            prioritizedTasks.remove(epic);
            scheduler.removeSchedule(epic);
        });
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

    private void setStartTimeAndDurationForEpic(Epic epic) {
        Duration duration = Duration.ZERO;
        LocalDateTime startTime = null;

        for (Subtask subtask : getSubtaskListForEpic(epic.getId())) {
            Duration subtaskDuration = subtask.getDuration();
            LocalDateTime subtaskStartTime = subtask.getStartTime();

            if ((subtaskStartTime != null) && (subtaskDuration != null)) {
                if ((startTime == null) || subtaskStartTime.isBefore(startTime)) {
                    startTime = subtaskStartTime;
                }
                duration = duration.plus(subtaskDuration);
            }
        }

        epic.setStartTime(startTime);
        epic.setDuration(duration);
    }

    private void removeAllUnusedSubtaskIds(Epic epic) {
        epic.getSubtaskIds().forEach(id -> {
            if (!subtaskMap.containsKey(id)) {
                epic.removeSubtaskId(id);
            }
        });
    }

    private <T extends Task> void scheduleTask(T task, Map<Long, T> map, boolean removeIfExists) {
        if ((task.getStartTime() == null) || (task.getDuration() == null)) {
            return;
        }
        T currentTask = map.get(task.getId());
        if (removeIfExists && (currentTask != null)) {
            scheduler.removeSchedule(currentTask);
        }
        if (!scheduler.addSchedule(task)) {
            throw new TasksIntersectException("Can't schedule task because it intersects another one");
        }
    }

    private void addToPriorityList(Task task) {
        if (task.getStartTime() != null) {
            // Remove if already exists.
            prioritizedTasks.remove(task);
            prioritizedTasks.add(task);
        }
    }
}