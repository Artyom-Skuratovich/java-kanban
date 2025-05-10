package ru.yandex.practicum.tracker;

import ru.yandex.practicum.tracker.models.Task;

public class Main {
    public static void main(String[] args) {
        Task task = new Task("", "");
        System.out.println(task.getClass().getSimpleName());
    }
}