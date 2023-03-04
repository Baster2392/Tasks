package com.example.task.model;

import android.accounts.Account;
import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class TaskListsModel {
    private Tasks service;

    public TaskListsModel(Context context, Account account) {
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

    public void changeAccount(Context context, Account account) {
        this.service = new Tasks(
                new NetHttpTransport(),
                new JacksonFactory(),
                getCredential(context, account)
        );
    }

    public TaskList getTaskList(String id) throws IOException {
        return service.tasklists().get(id).execute();
    }

     public ArrayList<TaskList> getTasksListAsArray() throws IOException {
         TaskLists taskLists = service.tasklists().list().execute();
         return new ArrayList<>(taskLists.getItems());
    }

    public void insertTaskList(String title) throws IOException {
        TaskList newTaskList = new TaskList();
        newTaskList.setTitle(title);
        service.tasklists().insert(newTaskList).execute();
    }

    public ArrayList<Task> getTasks(String taskList) throws IOException {
        com.google.api.services.tasks.model.Tasks tasks = service.tasks().list(taskList).execute();
        return new ArrayList<>(tasks.getItems());
    }
}
