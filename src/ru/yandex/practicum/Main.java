package ru.yandex.practicum;

import ru.yandex.practicum.tasks.*;
import ru.yandex.practicum.utils.UniqueIdCreator;

import java.util.Collection;

public class Main {

    public static void main(String[] args) {
        Task firstTask = new Task(UniqueIdCreator.createId(), "FirstTask", "FT", TaskStatus.NEW);
        Task secondTask = new Task(UniqueIdCreator.createId(), "SecondTask", "ST", TaskStatus.NEW);

        Epic firstEpic = new Epic(UniqueIdCreator.createId(), "EpicWithTwoSubtasks", "FE");
        Subtask firstSubtask = new Subtask(UniqueIdCreator.createId(), "FirstSub", "FS", TaskStatus.NEW, firstEpic);
        Subtask secondSubtask = new Subtask(UniqueIdCreator.createId(), "SecondSub", "SecS", TaskStatus.IN_PROGRESS, firstEpic);

        Epic secondEpic = new Epic(UniqueIdCreator.createId(), "EpicWithOneSubtask", "SE");
        Subtask thirdSubtask = new Subtask(UniqueIdCreator.createId(), "ThirdSub", "TS", TaskStatus.IN_PROGRESS, secondEpic);

        TaskManager taskManager = new TaskManager();
        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(firstEpic);
        taskManager.createTask(secondEpic);
        taskManager.createTask(firstSubtask);
        taskManager.createTask(secondSubtask);
        taskManager.createTask(thirdSubtask);

        System.out.println("################################## ALL TASKS, SUBTASKS, EPICS ##################################\n");

        System.out.println("TASKS:");
        printTasks(taskManager.getTasks(firstTask));
        System.out.println("SUBTASKS:");
        printSubtasks(taskManager.getTasks(firstSubtask));
        System.out.println("EPICS:");
        printEpics(taskManager.getTasks(firstEpic));

        System.out.println("########### CHANGE STATUS FOR SECOND SUBTASK STATUS, PRINT SECOND SUBTASK AND FIRST EPIC ###########\n");

        secondSubtask = new Subtask(secondSubtask.getId(), secondSubtask.getName(), secondSubtask.getDescription(), TaskStatus.NEW, firstEpic);
        taskManager.updateTask(secondSubtask);
        printTask(taskManager.getTaskOrNull(secondSubtask.getId(), firstSubtask));
        System.out.println("\n---------------------------------------------------\n");
        printEpic((Epic) taskManager.getTaskOrNull(secondSubtask.getParentEpic().getId(), firstEpic));

        System.out.println("\n###################### ALL TASKS LISTS AFTER REMOVE SECOND TASK AND FIRST EPIC ######################\n");

        taskManager.removeTaskById(secondTask.getId(), firstTask);
        taskManager.removeTaskById(firstEpic.getId(), firstEpic);
        System.out.println("TASKS:");
        printTasks(taskManager.getTasks(firstTask));
        System.out.println("SUBTASKS:");
        printSubtasks(taskManager.getTasks(firstSubtask));
        System.out.println("EPICS:");
        printEpics(taskManager.getTasks(firstEpic));
    }

    private static void printTask(Task task) {
        System.out.println("[" + task.getClass().getName() + "]");
        System.out.println("Id=" + task.getId());
        System.out.println("Name=" + task.getName());
        System.out.println("Description=" + task.getDescription());
        System.out.println("Status=" + task.getStatus());
    }

    private static void printEpic(Epic epic) {
        printTask(epic);
        System.out.println("Epic's subtasks:\n");
        for (Subtask subtask : epic.getSubtasks()) {
            printTask(subtask);
        }
    }

    private static void printTasks(Collection<Task> tasks) {
        System.out.println();
        tasks.forEach(Main::printTask);
        System.out.println();
    }

    private static void printSubtasks(Collection<Task> subtasks) {
        System.out.println();
        subtasks.forEach(Main::printTask);
        System.out.println();
    }

    private static void printEpics(Collection<Task> epics) {
        System.out.println();
        epics.forEach(t -> printEpic((Epic) t));
        System.out.println();
    }
}
