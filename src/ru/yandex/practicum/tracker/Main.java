package ru.yandex.practicum.tracker;

import ru.yandex.practicum.tracker.tasks.Epic;
import ru.yandex.practicum.tracker.tasks.Subtask;
import ru.yandex.practicum.tracker.tasks.Task;
import ru.yandex.practicum.tracker.tasks.TaskStatus;
import ru.yandex.practicum.tracker.tasks.managers.InMemoryTaskManager;
import ru.yandex.practicum.tracker.tasks.managers.Managers;
import ru.yandex.practicum.tracker.tasks.managers.TaskManager;

import java.util.List;

public class Main {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) {
        Task firstTask = new Task(InMemoryTaskManager.generateId(), "First task", "", TaskStatus.NEW);
        Task secondTask = new Task(InMemoryTaskManager.generateId(), "Second task", "", TaskStatus.DONE);

        Epic firstEpic = new Epic(InMemoryTaskManager.generateId(), "First epic", "");
        Epic secondEpic = new Epic(InMemoryTaskManager.generateId(), "Second epic", "");

        Subtask firstSubtask = new Subtask(InMemoryTaskManager.generateId(), "The first", "", TaskStatus.NEW, firstEpic.getId());
        Subtask secondSubtask = new Subtask(InMemoryTaskManager.generateId(), "The second", "", TaskStatus.NEW, firstEpic.getId());
        Subtask thirdSubtask = new Subtask(InMemoryTaskManager.generateId(), "The third", "", TaskStatus.DONE, secondEpic.getId());

        TaskManager taskManager = Managers.getDefault();
        taskManager.createOrdinaryTask(firstTask);
        taskManager.createOrdinaryTask(secondTask);
        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);
        taskManager.createSubtask(firstSubtask);
        taskManager.createSubtask(secondSubtask);
        taskManager.createSubtask(thirdSubtask);

        printOrdinaryTasks(taskManager.getOrdinaryTasks());
        printSubtasks(taskManager.getSubtasks());
        printEpics(taskManager.getEpics());

        System.out.println("******** DATA AFTER UPDATE ********\n");

        firstTask = new Task(firstTask.getId(), firstTask.getName(), firstTask.getDescription(), TaskStatus.IN_PROGRESS);
        secondSubtask = new Subtask(secondSubtask.getId(), secondSubtask.getName(), "", TaskStatus.DONE, firstEpic.getId());
        taskManager.updateOrdinaryTask(firstTask);
        taskManager.updateSubtask(secondSubtask);

        printOrdinaryTasks(taskManager.getOrdinaryTasks());
        printSubtasks(taskManager.getSubtasks());
        printEpics(taskManager.getEpics());

        System.out.println("******** DATA AFTER REMOVE ********\n");

        taskManager.removeOrdinaryTask(secondTask.getId());
        taskManager.removeSubtask(firstSubtask.getId());

        printOrdinaryTasks(taskManager.getOrdinaryTasks());
        printSubtasks(taskManager.getSubtasks());
        printEpics(taskManager.getEpics());
    }

    private static void printOrdinaryTask(Task task) {
        System.out.println(ANSI_YELLOW + "Id: " + task.getId());
        System.out.println("Name: " + task.getName());
        System.out.println("Description: " + task.getDescription());
        System.out.println("Status: " + task.getStatus());
    }

    private static void printEpic(Epic epic) {
        System.out.println(ANSI_GREEN + "Id: " + epic.getId());
        System.out.println("Name: " + epic.getName());
        System.out.println("Description: " + epic.getDescription());
        System.out.println("Status: " + epic.getStatus());
        System.out.println(ANSI_BLUE + "SUBTASK LIST:");
        List<Subtask> subtasks = epic.getSubtasks();
        for (int i = 0; i < subtasks.size(); i++) {
            Subtask subtask = subtasks.get(i);
            System.out.println("---- Id: " + subtask.getId());
            System.out.println("---- Name: " + subtask.getName());
            System.out.println("---- Description: " + subtask.getDescription());
            System.out.println("---- Status: " + subtask.getStatus());
            System.out.println("---- Parent epic Id: " + subtask.getParentEpicId());
            if (i != subtasks.size() - 1) {
                System.out.println();
            }
        }
    }

    private static void printSubtask(Subtask subtask) {
        System.out.println(ANSI_BLUE + "Id: " + subtask.getId());
        System.out.println("Name: " + subtask.getName());
        System.out.println("Description: " + subtask.getDescription());
        System.out.println("Status: " + subtask.getStatus());
        System.out.println("Parent epic Id: " + subtask.getParentEpicId());
    }

    private static void printOrdinaryTasks(Iterable<Task> ordinaryTasks) {
        System.out.println("ORDINARY TASK LIST:");
        for (Task task : ordinaryTasks) {
            printOrdinaryTask(task);
            System.out.println(ANSI_RESET);
        }
    }

    private static void printEpics(Iterable<Epic> epics) {
        System.out.println("EPIC LIST:");
        for (Epic epic : epics) {
            printEpic(epic);
            System.out.println(ANSI_RESET);
        }
    }

    private static void printSubtasks(Iterable<Subtask> subtasks) {
        System.out.println("SUBTASK LIST:");
        for (Subtask subtask : subtasks) {
            printSubtask(subtask);
            System.out.println(ANSI_RESET);
        }
    }
}