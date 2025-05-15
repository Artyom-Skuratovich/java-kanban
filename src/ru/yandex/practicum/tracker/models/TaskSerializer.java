package ru.yandex.practicum.tracker.models;

import ru.yandex.practicum.tracker.exceptions.TaskFormatException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        this.wordSeparator = wordSeparator;
        this.formatter = formatter;
        title = String.join(wordSeparator,
                "id", "type", "name", "status", "description", "startTime", "duration", "epic");
    }

    public String toString(Task task) {
        String id = Long.toString(task.getId());
        String type = task.getClass().getSimpleName();
        String name = task.getName();
        String status = task.getStatus().name();
        String description = task.getDescription();
        String startTime = formatter.format(task.getStartTime());
        String duration = Long.toString(task.getDuration().toMinutes());
        String epic = task instanceof Subtask subtask ? Long.toString(subtask.getParentEpicId()) : "";

        return String.join(wordSeparator, id, type, name, status, description, startTime, duration, epic);
    }

    public Task fromString(String value) {
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
            task = new Epic(name, description, startTime, duration);
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