package com.example.task.model;

import android.accounts.Account;

import com.example.task.activity.MainActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskListsModel {
    private final MainActivity activity;
    private Tasks service;

    public TaskListsModel(MainActivity activity, Account account) {
        this.activity = activity;
        this.service = new Tasks(
                new NetHttpTransport(),
                new JacksonFactory(),
                getCredential(account)
        );
    }

    private GoogleAccountCredential getCredential(Account account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(),
                Collections.singleton(TasksScopes.TASKS)
        );

        credential.setSelectedAccount(account);
        return credential;
    }

    public void changeAccount(Account account) {
        this.service = new Tasks(
                new NetHttpTransport(),
                new JacksonFactory(),
                getCredential(account)
        );
    }

     public void getTasksList() throws RuntimeException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                TaskLists taskLists = service.tasklists().list().execute();
                ArrayList<TaskList> taskListsArray = new ArrayList<>(taskLists.getItems());

                activity.runOnUiThread(() -> activity.displayTaskLists(taskListsArray));
            } catch (IOException e) {
                activity.runOnUiThread(activity::showRuntimeAlertDialog);
            }
        });
    }
}
