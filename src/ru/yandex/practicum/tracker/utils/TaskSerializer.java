package ru.yandex.practicum.tracker.utils;

import ru.yandex.practicum.tracker.exceptions.TaskFormatException;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Status;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TaskSerializer {
    private static final int WORD_COUNT = 8;
    private static final int ID_INDEX = 0;
    private static final int TYPE_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int STATUS_INDEX = 3;
    private static final int DESCRIPTION_INDEX = 4;
    private static final int START_TIME_INDEX = 5;
    private static final int DURATION_INDEX = 6;
    private static final int EPIC_INDEX = 7;

    private final String wordSeparator;
    private final DateTimeFormatter formatter;
    private final String title;

    public TaskSerializer(String wordSeparator, DateTimeFormatter formatter) {
        Objects.requireNonNull(wordSeparator, "Word separator can't be null");
        Objects.requireNonNull(formatter, "Formatter can't be null");
        this.wordSeparator = wordSeparator;
        this.formatter = formatter;
        title = String.join(wordSeparator,
                "id", "type", "name", "status", "description", "startTime", "duration", "epic");
    }

    public String toString(Task task) {
        Objects.requireNonNull(task, "Task can't be null");
        String id = Long.toString(task.getId());
        String type = task.getClass().getSimpleName();
        String name = task.getName() != null ? task.getName() : "";
        String status = task.getStatus() != null ? task.getStatus().name() : "";
        String description = task.getDescription() != null ? task.getDescription() : "";
        String startTime = task.getStartTime() != null ? formatter.format(task.getStartTime()) : "";
        String duration = task.getDuration() != null ? Long.toString(task.getDuration().toMinutes()) : "";
        String epic = task instanceof Subtask subtask ? Long.toString(subtask.getParentEpicId()) : "";

        return String.join(wordSeparator, id, type, name, status, description, startTime, duration, epic);
    }

    public Task fromString(String value) {
        Objects.requireNonNull(value, "Value can't be null");
        String[] words = value.split(wordSeparator, WORD_COUNT);
        if (words.length != WORD_COUNT) {
            throw new TaskFormatException("String value has bad format and can't be converted to Task");
        }

        String type = words[TYPE_INDEX];
        String name = words[NAME_INDEX];
        String description = words[DESCRIPTION_INDEX];
        LocalDateTime startTime = LocalDateTime.from(formatter.parse(words[START_TIME_INDEX]));
        Duration duration = Duration.ofMinutes(Long.parseLong(words[DURATION_INDEX]));
        Task task;

        if (checkTaskType(type, Epic.class)) {
            task = new Epic(name, description);
            task.setStartTime(startTime);
            task.setDuration(duration);
        } else if (checkTaskType(type, Subtask.class)) {
            task = new Subtask(name, description, startTime, duration, Long.parseLong(words[EPIC_INDEX]));
        } else if (checkTaskType(type, Task.class)) {
            task = new Task(name, description, startTime, duration);
        } else {
            throw new TaskFormatException("String value has unknown Task type");
        }
        task.setId(Long.parseLong(words[ID_INDEX]));
        task.setStatus(Enum.valueOf(Status.class, words[STATUS_INDEX]));

        return task;
    }

    public String getTitle() {
        return title;
    }

    private static <T extends Task> boolean checkTaskType(String type, Class<T> taskClass) {
        return type.equalsIgnoreCase(taskClass.getSimpleName());
    }
}