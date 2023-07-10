package com.example.task.other;

import com.google.api.services.tasks.model.Task;

public class TaskInfo {
    private Task task;
    private String taskListId, taskListTitle;

    public TaskInfo(Task task, String taskListId, String taskListTitle) {
        this.task = task;
        this.taskListId = taskListId;
        this.taskListTitle = taskListTitle;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getTaskListId() {
        return taskListId;
    }

    public void setTaskListId(String taskListId) {
        this.taskListId = taskListId;
    }

    public String getTaskListTitle() {
        return taskListTitle;
    }

    public void setTaskListTitle(String taskListTitle) {
        this.taskListTitle = taskListTitle;
    }
}
