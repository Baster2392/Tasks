package com.example.task.model;

import android.accounts.Account;
import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TasksModel {
    private final Tasks service;
    private final String taskListID;
    public final String STATUS_COMPLETED = "completed";

    public TasksModel(Context context, Account account, String taskListID) {
        this.service = new Tasks(
                new NetHttpTransport(),
                new JacksonFactory(),
                getCredential(context, account)
        );
        this.taskListID = taskListID;
    }

    private GoogleAccountCredential getCredential(Context context, Account account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(TasksScopes.TASKS)
        );

        credential.setSelectedAccount(account);
        return credential;
    }

    public ArrayList<Task> getUncompletedTasksAsArray() throws IOException {
        com.google.api.services.tasks.model.Tasks tasks = service.tasks().list(taskListID).execute();
        ArrayList<Task> tasksArray = new ArrayList<>();

        for (Task task : tasks.getItems()) {
            if (!task.getStatus().equals(STATUS_COMPLETED)) {
                tasksArray.add(task);
            }
        }

        return tasksArray;
    }

    public void setTaskDone(Task task) throws IOException {
        task.setStatus(STATUS_COMPLETED);
        service.tasks().update(taskListID, task.getId(), task).execute();
    }
}
