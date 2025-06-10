package ru.yandex.practicum.tracker.managers;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager getTaskManager() {
        return new InMemoryTaskManager(Managers.getDefaultHistory());
    }
}