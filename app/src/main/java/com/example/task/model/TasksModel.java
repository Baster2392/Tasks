package com.example.task.model;

import android.accounts.Account;
import android.content.Context;

import com.example.task.other.TaskPositionComparator;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class TasksModel {
    private final Tasks service;
    public final static String STATUS_COMPLETED = "completed";

    public TasksModel(Context context, Account account) {
        this.service = new Tasks(
                new NetHttpTransport(),
                new JacksonFactory(),
                getCredential(context, account)
        );
    }

    private GoogleAccountCredential getCredential(Context context, Account account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(TasksScopes.TASKS)
        );

        credential.setSelectedAccount(account);
        return credential;
    }

    public ArrayList<Task> getUncompletedTasksAsArray(String taskListId) throws IOException {
        com.google.api.services.tasks.model.Tasks tasks = service.tasks().list(taskListId).execute();
        ArrayList<Task> tasksArray = new ArrayList<>();

        for (Task task : tasks.getItems()) {
            if (!task.getStatus().equals(STATUS_COMPLETED) && task.getParent() == null) {
                tasksArray.add(task);
            }
        }

        return tasksArray;
    }

    public ArrayList<Task> getUncompletedChildren(String taskListId, String parentId) throws IOException {
        com.google.api.services.tasks.model.Tasks tasks = service.tasks().list(taskListId).execute();
        ArrayList<Task> tasksArray = new ArrayList<>();

        for (Task task : tasks.getItems()) {
            if (!task.getStatus().equals(STATUS_COMPLETED) && Objects.equals(task.getParent(), parentId)) {
                tasksArray.add(task);
            }
        }

        return tasksArray;
    }

    public void setTaskDone(Task task, String taskListId) throws IOException {
        task.setStatus(STATUS_COMPLETED);
        service.tasks().update(taskListId, task.getId(), task).execute();
    }

    public void createTask(Task task, String taskListId) throws IOException {
        service.tasks().insert(taskListId, task).setParent(task.getParent()).execute();
    }

    public void deleteTask(Task task, String taskListId) throws IOException {
        service.tasks().delete(taskListId, task.getId()).execute();
    }
}
