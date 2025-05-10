package ru.yandex.practicum.tracker.managers;

import ru.yandex.practicum.tracker.exceptions.ManagerLoadException;
import ru.yandex.practicum.tracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.tracker.models.Epic;
import ru.yandex.practicum.tracker.models.Subtask;
import ru.yandex.practicum.tracker.models.Task;
import ru.yandex.practicum.tracker.models.Tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

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

    public static FileBackedTaskManager loadFromFile(File file, HistoryManager historyManager) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("File object cannot be directory");
        }

        try {
            FileBackedTaskManager taskManager = new FileBackedTaskManager(historyManager, file.getPath());
            List<String> fileContent = Files.readAllLines(file.toPath(), CHARSET);

            for (int i = 1; i < fileContent.size(); i++) {
                Task task = Tasks.fromString(fileContent.get(i));
                if (task instanceof Epic) {
                    taskManager.epicMap.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask subtask) {
                    taskManager.subtaskMap.put(subtask.getId(), subtask);
                    Epic parentEpic = taskManager.epicMap.get(subtask.getParentEpicId());
                    if (parentEpic != null) {
                        parentEpic.addSubtaskId(subtask.getId());
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