package ru.yandex.practicum.tasks;

public class Subtask extends Task {
    private final Epic parentEpic;

    public Subtask(int id, String name, String description, TaskStatus status, Epic parentEpic) {
        super(id, name, description, status);
        this.parentEpic = parentEpic;
    }

    public Epic getParentEpic() {
        return parentEpic;
    }
}