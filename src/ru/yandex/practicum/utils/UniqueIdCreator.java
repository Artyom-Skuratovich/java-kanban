package ru.yandex.practicum.utils;

public class UniqueIdCreator {
    private static int nextId = 1;

    private UniqueIdCreator() {
    }

    public static int createId() {
        return nextId++;
    }
}