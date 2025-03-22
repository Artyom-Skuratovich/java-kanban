package ru.yandex.practicum.tasks;


import java.util.*;

public class TaskManager {
    private final HashMap<Integer, Task> ordinaryTasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;

    public TaskManager() {
        ordinaryTasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
    }

    public List<Task> getTasks(Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        if (task instanceof Epic) {
            return new ArrayList<>(epics.values());
        } else if (task instanceof Subtask) {
            return new ArrayList<>(subtasks.values());
        }
        return new ArrayList<>(ordinaryTasks.values());
    }

    public void removeAll(Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        if (task instanceof Epic) {
            epics.clear();
            subtasks.clear();
        } else if (task instanceof Subtask) {
            subtasks.clear();
            for (Epic oldEpic : epics.values()) {
                Epic epic = new Epic(oldEpic.getId(), oldEpic.getName(), oldEpic.getDescription());
                epics.put(epic.getId(), epic);
            }
        } else {
            ordinaryTasks.clear();
        }
    }

    public Optional<Task> getTask(int id, Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        if ((task instanceof Epic) && epics.containsKey(id)) {
            return Optional.of(epics.get(id));
        } else if ((task instanceof Subtask) && subtasks.containsKey(id)) {
            return Optional.of(subtasks.get(id));
        } else if (ordinaryTasks.containsKey(id)) {
            return Optional.of(ordinaryTasks.get(id));
        }
        return Optional.empty();
    }

    public void createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        if (task instanceof Epic epic) {
            Epic previous = epics.putIfAbsent(epic.getId(), epic);
            if ((epic.getSubtasks() != null) && (previous == null)) {
                epic.getSubtasks().forEach(s -> {
                    subtasks.putIfAbsent(s.getId(), s);
                    epic.addSubtask(s);
                });
            }
        } else if (task instanceof Subtask subtask) {
            Epic parentEpic = subtask.getParentEpic();
            Subtask previous = subtasks.putIfAbsent(subtask.getId(), subtask);
            if ((parentEpic != null) && (previous == null)) {
                epics.putIfAbsent(parentEpic.getId(), parentEpic);
                parentEpic.addSubtask(subtask);
            }
        } else {
            ordinaryTasks.putIfAbsent(task.getId(), task);
        }
    }

    public void updateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        if ((task instanceof Epic epic) && epics.containsKey(task.getId())) {
            epics.put(epic.getId(), epic);
            if (epic.getSubtasks() != null) {
                epic.getSubtasks().forEach(s -> {
                    subtasks.put(s.getId(), s);
                    epic.addSubtask(s);
                });
            }
        } else if ((task instanceof Subtask subtask) && subtasks.containsKey(task.getId())) {
            Epic parentEpic = subtask.getParentEpic();
            if (parentEpic != null) {
                epics.put(parentEpic.getId(), parentEpic);
                parentEpic.addSubtask(subtask);
            }
            subtasks.put(subtask.getId(), subtask);
        } else if (ordinaryTasks.containsKey(task.getId())) {
            ordinaryTasks.put(task.getId(), task);
        }
    }

    public List<Subtask> getSubtasksForEpic(int epicId) {
        Epic epic = epics.getOrDefault(epicId, null);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return Collections.emptyList();
    }

    public void removeTaskById(int id, Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        if (task instanceof Epic) {
            Epic removedEpic = epics.remove(id);
            if ((removedEpic != null) && (removedEpic.getSubtasks() != null)) {
                removedEpic.getSubtasks().forEach(s -> subtasks.remove(s.getId()));
            }
        } else if (task instanceof Subtask) {
            Subtask removedSubtask = subtasks.remove(id);
            if (removedSubtask != null) {
                Epic epic = removedSubtask.getParentEpic();
                if (epic != null) {
                    epic.removeSubtask(id);
                }
            }
        } else {
            ordinaryTasks.remove(id);
        }
    }
}