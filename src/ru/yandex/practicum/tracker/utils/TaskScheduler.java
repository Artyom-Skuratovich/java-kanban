package ru.yandex.practicum.tracker.utils;

import ru.yandex.practicum.tracker.models.Task;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskScheduler {
    private static final int INTERVAL_MINUTES = 15;
    private static final boolean INTERVAL_FREE = true;
    private static final boolean INTERVAL_BUSY = false;

    private final Map<LocalDateTime, Boolean> intervals;

    public TaskScheduler(LocalDateTime initDateTime, LocalDateTime endDateTime) {
        Objects.requireNonNull(initDateTime, "Initial date time can't be null");
        Objects.requireNonNull(endDateTime, "End date time can't be null");
        if (!initDateTime.isBefore(endDateTime)) {
            throw new IllegalArgumentException("Initial date time must be lower than end date time");
        }

        intervals = new HashMap<>();
        changeIntervalStatus(initDateTime, endDateTime, INTERVAL_FREE);
    }

    public boolean addSchedule(Task task) {
        Objects.requireNonNull(task, "Task can't be null");
        LocalDateTime startTime = Objects.requireNonNull(task.getStartTime(), "Start time can't be null");
        LocalDateTime endTime = Objects.requireNonNull(task.getEndTime(), "End time can't be null");

        if (isAvailable(startTime, endTime)) {
            changeIntervalStatus(startTime, endTime, INTERVAL_BUSY);
            return true;
        }
        return false;
    }

    public void removeSchedule(Task task) {
        Objects.requireNonNull(task, "Task can't be null");
        LocalDateTime startTime = Objects.requireNonNull(task.getStartTime(), "Start time can't be null");
        LocalDateTime endTime = Objects.requireNonNull(task.getEndTime(), "End time can't be null");
        changeIntervalStatus(startTime, endTime, INTERVAL_FREE);
    }

    public boolean isAvailable(LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(startTime, "Start time can't be null");
        Objects.requireNonNull(endTime, "End time can't be null");

        LocalDateTime current = roundToNearestInterval(startTime);
        while (current.isBefore(endTime) || current.isEqual(endTime)) {
            Boolean available = intervals.get(current);
            if (available == null) {
                throw new IllegalArgumentException("Start time is out of range");
            } else if (available == INTERVAL_BUSY) {
                return false;
            }
            current = current.plusMinutes(INTERVAL_MINUTES);
        }
        return true;
    }

    public boolean isInRange(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "Date time can't be null");
        return intervals.containsKey(roundToNearestInterval(dateTime));
    }

    private static LocalDateTime roundToNearestInterval(LocalDateTime dateTime) {
        dateTime = dateTime.truncatedTo(ChronoUnit.MINUTES);
        return dateTime.withMinute(INTERVAL_MINUTES * (dateTime.getMinute() / INTERVAL_MINUTES));
    }

    private void changeIntervalStatus(LocalDateTime startTime, LocalDateTime endTime, boolean status) {
        LocalDateTime current = roundToNearestInterval(startTime);
        while (current.isBefore(endTime) || current.isEqual(endTime)) {
            intervals.put(current, status);
            current = current.plusMinutes(INTERVAL_MINUTES);
        }
    }
}