package ru.yandex.practicum.tasks;

import java.util.Collection;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> ordinaryTasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;

    public TaskManager() {
        ordinaryTasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
    }

    public <T extends Task> Collection<T> getTasks(Class<T> theClass) {
        if (theClass == null) {
            throw new IllegalArgumentException();
        }

        if (theClass == Epic.class) {
            return (Collection<T>) epics.values();
        } else if (theClass == Subtask.class) {
            return (Collection<T>) subtasks.values();
        }
        return (Collection<T>) ordinaryTasks.values();
    }

    public <T extends Task> void removeAll(Class<T> theClass) {
        if (theClass == null) {
            throw new IllegalArgumentException();
        }

        if (theClass == Epic.class) {
            epics.clear();
            // Subtasks should be removed too.
            subtasks.clear();
        } else if (theClass == Subtask.class) {
            subtasks.clear();
            // Removing subtasks from all epics.
            // We should recreate each epic and update epics map.
            for (Epic oldEpic : epics.values()) {
                Epic epic = new Epic(oldEpic.getId(), oldEpic.getName(), oldEpic.getDescription());
                epics.put(epic.getId(), epic);
            }
        } else {
            ordinaryTasks.clear();
        }
    }

    public <T extends Task> T getTaskOrNull(int id, Class<T> theClass) {
        if (theClass == null) {
            throw new IllegalArgumentException();
        }

        if ((theClass == Epic.class) && epics.containsKey(id)) {
            return (T) epics.get(id);
        } else if ((theClass == Subtask.class) && subtasks.containsKey(id)) {
            return (T) subtasks.get(id);
        } else if (ordinaryTasks.containsKey(id)) {
            return (T) ordinaryTasks.get(id);
        }
        return null;
    }

    public void createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }

        if (task.getClass() == Epic.class) {
            Epic epic = (Epic) task;
            Epic previous = epics.putIfAbsent(epic.getId(), epic);
            if ((epic.getSubtasks() != null) && (previous == null)) {
                epic.getSubtasks().forEach(s -> {
                    subtasks.putIfAbsent(s.getId(), s);
                    epic.addSubtask(s);
                });
            }
        } else if (task.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) task;
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

        if ((task.getClass() == Epic.class) && epics.containsKey(task.getId())) {
            Epic epic = (Epic) task;
            epics.put(epic.getId(), epic);
            if (epic.getSubtasks() != null) {
                epic.getSubtasks().forEach(s -> {
                    subtasks.put(s.getId(), s);
                    epic.addSubtask(s);
                });
            }
        } else if ((task.getClass() == Subtask.class) && subtasks.containsKey(task.getId())) {
            Subtask subtask = (Subtask) task;
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

    public Collection<Subtask> getSubtasksForEpic(int epicId) {
        Epic epic = epics.getOrDefault(epicId, null);
        if (epic != null) {
            return epic.getSubtasks();
        }
        return null;
    }

    public <T extends Task> void removeTaskById(int id, Class<T> theClass) {
        if (theClass == null) {
            throw new IllegalArgumentException();
        }

        if (theClass == Epic.class) {
            Epic removedEpic = epics.remove(id);
            if ((removedEpic != null) && (removedEpic.getSubtasks() != null)) {
                removedEpic.getSubtasks().forEach(s -> subtasks.remove(s.getId()));
            }
        } else if (theClass == Subtask.class) {
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