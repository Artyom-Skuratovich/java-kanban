package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.exceptions.ManagerLoadException;
import ru.yandex.practicum.tracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;
import ru.yandex.practicum.tracker.models.Tasks;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String TITLE = String.join(Tasks.WORD_SEPARATOR,
            "id", "type", "name", "status", "description", "epic");

    private final String path;

    public FileBackedTaskManager(HistoryManager historyManager, String path) {
        super(historyManager);

        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        this.path = path;
    }

    public static void main(String[] args) {
        try {
            File file = File.createTempFile("tasks-", ".csv");
            FileBackedTaskManager firstManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file.getPath());

            Task firstTask = new Task("First task", "");
            Task secondTask = new Task("Second task", "");
            Epic firstEpic = new Epic("First epic", "");
            Epic secondEpic = new Epic("Second epic", "");

            firstManager.createTask(firstTask);
            firstManager.createTask(secondTask);
            firstManager.createEpic(firstEpic);
            firstManager.createEpic(secondEpic);

            List<Epic> epicList = firstManager.getEpicList();
            firstEpic = epicList.get(0);
            secondEpic = epicList.get(1);

            Subtask firstSubtask = new Subtask("First subtask", "", firstEpic.getId());
            Subtask secondSubtask = new Subtask("Second subtask", "", secondEpic.getId());
            Subtask thirdSubtask = new Subtask("Third subtask", "", secondEpic.getId());
            Subtask unusedSubtask = new Subtask("Unused subtask", "", firstEpic.getId());

            firstManager.createSubtask(firstSubtask);
            firstManager.createSubtask(secondSubtask);
            firstManager.createSubtask(thirdSubtask);
            firstManager.createSubtask(unusedSubtask);

            FileBackedTaskManager secondManager = FileBackedTaskManager.loadFromFile(file, Managers.getDefaultHistory());

            System.out.println("Epics from first manager:");
            firstManager.getEpicList().forEach(e -> System.out.println(Tasks.toString(e)));

            System.out.println("\nEpics from second manager:");
            secondManager.getEpicList().forEach(e -> System.out.println(Tasks.toString(e)));

            System.out.println("\nTasks from first manager:");
            firstManager.getTaskList().forEach(t -> System.out.println(Tasks.toString(t)));

            System.out.println("\nTasks from second manager:");
            secondManager.getTaskList().forEach(t -> System.out.println(Tasks.toString(t)));

            System.out.println("\nSubtasks from first manager:");
            firstManager.getSubtaskList().forEach(s -> System.out.println(Tasks.toString(s)));

            System.out.println("\nSubtasks from second manager:");
            secondManager.getSubtaskList().forEach(s -> System.out.println(Tasks.toString(s)));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file, HistoryManager historyManager) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("File object cannot be directory");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, CHARSET))) {
            FileBackedTaskManager taskManager = new FileBackedTaskManager(historyManager, file.getPath());
            Map<Long, Subtask> unusedSubtasks = new HashMap<>();

            // Skip first line with title.
            if (reader.ready()) reader.readLine();
            while (reader.ready()) {
                Task task = Tasks.fromString(reader.readLine());

                if (task instanceof Epic epic) {
                    taskManager.epicMap.put(epic.getId(), epic);
                    Subtask subtask = unusedSubtasks.get(epic.getId());

                    if (subtask != null) {
                        epic.addSubtaskId(subtask.getId());
                        taskManager.subtaskMap.put(subtask.getId(), subtask);
                        unusedSubtasks.remove(epic.getId());
                    }
                } else if (task instanceof Subtask subtask) {
                    Epic parentEpic = taskManager.epicMap.get(subtask.getParentEpicId());

                    if (parentEpic != null) {
                        parentEpic.addSubtaskId(subtask.getId());
                        taskManager.subtaskMap.put(subtask.getId(), subtask);
                        unusedSubtasks.remove(parentEpic.getId());
                    } else {
                        unusedSubtasks.put(subtask.getParentEpicId(), subtask);
                    }
                } else {
                    taskManager.taskMap.put(task.getId(), task);
                }
            }

            return taskManager;
        } catch (IOException exception) {
            throw new ManagerLoadException("Cannot load data from file, reason: " + exception.getMessage());
        }
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeTask(long id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeSubtask(long id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public void removeEpic(long id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(path, CHARSET)) {
            StringBuilder fileContent = new StringBuilder();

            getTaskList().forEach(t -> appendContentToStringBuilder(fileContent, t));
            getEpicList().forEach(e -> appendContentToStringBuilder(fileContent, e));
            getSubtaskList().forEach(s -> appendContentToStringBuilder(fileContent, s));

            if (!fileContent.isEmpty()) {
                fileContent.insert(0, TITLE + System.lineSeparator());
                fileContent.delete(fileContent.length() - 2, fileContent.length());
                fileWriter.write(fileContent.toString());
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Cannot save data to file, reason: " + exception.getMessage());
        }
    }

    private static void appendContentToStringBuilder(StringBuilder source, Task task) {
        source.append(Tasks.toString(task)).append(System.lineSeparator());
    }
}