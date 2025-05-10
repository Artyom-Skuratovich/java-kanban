package ru.yandex.practicum.tracker.models;

import ru.yandex.practicum.tracker.exceptions.TaskFormatException;

public final class Tasks {
    public static final String WORD_SEPARATOR = ",";

    private Tasks() {
    }

    public static String toString(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        String id = Long.toString(task.getId());
        String status = task.getStatus().name();
        String epic = "";
        String type;

        if (task instanceof Epic) {
            type = TaskType.EPIC.name();
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK.name();
            epic = Long.toString(((Subtask) task).getParentEpicId());
        } else {
            type = TaskType.TASK.name();
        }

        return String.join(WORD_SEPARATOR, id, type, task.getName(), status, task.getDescription(), epic);
    }

    public static Task fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        String[] words = value.split(WORD_SEPARATOR);
        if ((words.length < 5) || (words.length > 6)) {
            throw new TaskFormatException("String value has bad format and cannot be converted to Task");
        }

        Task resultTask;
        if (words[1].equals(TaskType.EPIC.name())) {
            resultTask = new Epic(words[2], words[4]);
        } else if (words[1].equals(TaskType.SUBTASK.name())) {
            resultTask = new Subtask(words[2], words[4], Long.parseLong(words[5]));
        } else {
            resultTask = new Task(words[2], words[4]);
        }
        resultTask.setId(Long.parseLong(words[0]));
        resultTask.setStatus(Enum.valueOf(Status.class, words[3]));

        return resultTask;
    }
}