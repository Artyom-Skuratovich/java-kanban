package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.exceptions.ManagerLoadException;
import ru.yandex.practicum.tracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;
import ru.yandex.practicum.tracker.utils.TaskSerializer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final TaskSerializer SERIALIZER = new TaskSerializer(",",
            DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private final String path;

    public FileBackedTaskManager(HistoryManager historyManager, String path) {
        super(historyManager);
        Objects.requireNonNull(path, "Path can't be null");
        this.path = path;
    }

    public static void main(String[] args) {
        try {
            File file = File.createTempFile("tasks-", ".csv");
            FileBackedTaskManager firstManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file.getPath());
            LocalDateTime now = LocalDateTime.now();

            Task firstTask = new Task("First task", "", now.plusDays(1), Duration.ofMinutes(60));
            Task secondTask = new Task("Second task", "", now.plusDays(2), Duration.ofMinutes(70));
            Epic firstEpic = new Epic("First epic", "");
            Epic secondEpic = new Epic("Second epic", "");

            firstManager.createTask(firstTask);
            firstManager.createTask(secondTask);
            firstManager.createEpic(firstEpic);
            firstManager.createEpic(secondEpic);

            List<Epic> epicList = firstManager.getEpicList();
            firstEpic = epicList.get(0);
            secondEpic = epicList.get(1);

            Subtask firstSubtask = new Subtask("First subtask", "", now.plusDays(4), Duration.ofMinutes(8), firstEpic.getId());
            Subtask secondSubtask = new Subtask("Second subtask", "", now.plusDays(5), Duration.ofMinutes(6), secondEpic.getId());
            Subtask thirdSubtask = new Subtask("Third subtask", "", now.plusDays(6), Duration.ofMinutes(5), secondEpic.getId());
            Subtask unusedSubtask = new Subtask("Unused subtask", "", now.plusDays(7), Duration.ofMinutes(4), firstEpic.getId());

            firstManager.createSubtask(firstSubtask);
            firstManager.createSubtask(secondSubtask);
            firstManager.createSubtask(thirdSubtask);
            firstManager.createSubtask(unusedSubtask);

            FileBackedTaskManager secondManager = FileBackedTaskManager.loadFromFile(file, Managers.getDefaultHistory());

            System.out.println("Epics from first manager:");
            firstManager.getEpicList().forEach(e -> System.out.println(SERIALIZER.toString(e)));

            System.out.println("\nEpics from second manager:");
            secondManager.getEpicList().forEach(e -> System.out.println(SERIALIZER.toString(e)));

            System.out.println("\nTasks from first manager:");
            firstManager.getTaskList().forEach(t -> System.out.println(SERIALIZER.toString(t)));

            System.out.println("\nTasks from second manager:");
            secondManager.getTaskList().forEach(t -> System.out.println(SERIALIZER.toString(t)));

            System.out.println("\nSubtasks from first manager:");
            firstManager.getSubtaskList().forEach(s -> System.out.println(SERIALIZER.toString(s)));

            System.out.println("\nSubtasks from second manager:");
            secondManager.getSubtaskList().forEach(s -> System.out.println(SERIALIZER.toString(s)));
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file, HistoryManager historyManager) {
        Objects.requireNonNull(file, "File can't be null");
        if (file.isDirectory()) {
            throw new IllegalArgumentException("File object cannot be directory");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, CHARSET))) {
            FileBackedTaskManager taskManager = new FileBackedTaskManager(historyManager, file.getPath());
            Map<Long, Subtask> unusedSubtasks = new HashMap<>();

            // Skip first line with title.
            if (reader.ready()) reader.readLine();
            while (reader.ready()) {
                Task task = SERIALIZER.fromString(reader.readLine());

                if ((task.getStartTime() == null) || (task.getDuration() == null)
                        || !taskManager.scheduler.isInRange(task.getStartTime())) {
                    continue;
                }

                if (task instanceof Epic epic) {
                    taskManager.epicMap.put(epic.getId(), epic);
                    taskManager.scheduler.addSchedule(epic);
                    taskManager.prioritizedTasks.add(epic);
                    Subtask subtask = unusedSubtasks.get(epic.getId());

                    if (subtask != null) {
                        epic.addSubtaskId(subtask.getId());
                        taskManager.subtaskMap.put(subtask.getId(), subtask);
                        taskManager.scheduler.addSchedule(subtask);
                        taskManager.prioritizedTasks.add(subtask);
                        unusedSubtasks.remove(epic.getId());
                    }
                } else if (task instanceof Subtask subtask) {
                    Epic parentEpic = taskManager.epicMap.get(subtask.getParentEpicId());

                    if (parentEpic != null) {
                        parentEpic.addSubtaskId(subtask.getId());
                        taskManager.subtaskMap.put(subtask.getId(), subtask);
                        taskManager.scheduler.addSchedule(subtask);
                        taskManager.prioritizedTasks.add(subtask);
                        unusedSubtasks.remove(parentEpic.getId());
                    } else {
                        unusedSubtasks.put(subtask.getParentEpicId(), subtask);
                    }
                } else {
                    taskManager.taskMap.put(task.getId(), task);
                    taskManager.scheduler.addSchedule(task);
                    taskManager.prioritizedTasks.add(task);
                }
            }

            return taskManager;
        } catch (IOException exception) {
            throw new ManagerLoadException("Cannot load data from file, reason: " + exception.getMessage());
        }
    }

    @Override
    public long createTask(Task task) {
        long id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public long createSubtask(Subtask subtask) {
        long id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public long createEpic(Epic epic) {
        long id = super.createEpic(epic);
        save();
        return id;
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

            taskMap.forEach((id, t) -> appendContentToStringBuilder(fileContent, t));
            epicMap.forEach((id, e) -> appendContentToStringBuilder(fileContent, e));
            subtaskMap.forEach((id, s) -> appendContentToStringBuilder(fileContent, s));

            if (!fileContent.isEmpty()) {
                fileContent.insert(0, SERIALIZER.getTitle() + System.lineSeparator());
                fileContent.delete(fileContent.length() - 2, fileContent.length());
                fileWriter.write(fileContent.toString());
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Cannot save data to file, reason: " + exception.getMessage());
        }
    }

    private static void appendContentToStringBuilder(StringBuilder source, Task task) {
        source.append(SERIALIZER.toString(task)).append(System.lineSeparator());
    }
}