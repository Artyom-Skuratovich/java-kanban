package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        final Task data;
        Node next;
        Node prev;

        Node(Task data, Node next, Node prev) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }

    private Node head;
    private Node tail;
    private final Map<Long, Node> nodeMap;

    public InMemoryHistoryManager() {
        nodeMap = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        linkLast(task);
    }

    @Override
    public void remove(long id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Task copyTask(Task task) {
        if (task instanceof Subtask) {
            return new Subtask((Subtask) task);
        } else if (task instanceof Epic) {
            return new Epic((Epic) task);
        }
        return new Task(task);
    }

    private List<Task> getTasks() {
        List<Task> taskList = new ArrayList<>(nodeMap.size());
        Node current = head;

        while (current != null) {
            taskList.add(copyTask(current.data));
            current = current.next;
        }
        return taskList;
    }

    private void linkLast(Task task) {
        Node node = new Node(copyTask(task), null, tail);
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;

        remove(task.getId());
        nodeMap.put(task.getId(), node);
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        }
        node.next = node.prev = null;
    }
}