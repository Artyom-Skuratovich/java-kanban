package ru.yandex.practicum.tasks;

import java.util.Collection;
import java.util.Collections;
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
                Epic epic = new Epic(oldEpic.getId(), oldEpic.getName(), oldEpic.getDescription(), Collections.emptyList());
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

    public <T extends Task> void createTask(T task) {
        if (task == null) {
            throw new IllegalArgumentException();
        }
    }
}