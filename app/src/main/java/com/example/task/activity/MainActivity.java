package com.example.task.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.task.R;
import com.example.task.databinding.ActivityMainBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TaskListsModel;
import com.example.task.other.Consts;
import com.example.task.adapter.TaskListListAdapter;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;
    private GoogleSignInModel googleSignInModel;
    private TaskListsModel taskListsModel;
    private ListView taskListListView;
    private TextView accountNameView, clockView;
    private View taskListsHeader, taskListsFooter;
    private ProgressBar progressBar;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleSignInModel = new GoogleSignInModel(this);
        if (googleSignInModel.getAccount() != null) {
            taskListsModel = new TaskListsModel(this, googleSignInModel.getAccount());
        } else {
            changeAccount();
        }

        init();

        if (googleSignInModel.getAccount() != null) {
            refresh();
        }
    }

    @SuppressLint("InflateParams")
    private void init() {
        progressBar = findViewById(R.id.tasklist_list_progressbar);
        taskListListView = findViewById(R.id.tasklist_list_view);
        taskListsHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tasklist_list_header, null, false);
        taskListsFooter = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tasklist_list_footer, null, false);

        clockView = taskListsHeader.findViewById(R.id.tasklisk_list_clock_view);
        displayClock();
        Button addTaskListButton = taskListsHeader.findViewById(R.id.go_to_add_tasklists_activity_button);
        Button refreshButton = taskListsHeader.findViewById(R.id.refresh_tasklists_button);
        accountNameView = taskListsFooter.findViewById(R.id.tasklists_account_name_view);

        taskListsFooter.setOnClickListener(view -> changeAccount());
        addTaskListButton.setOnClickListener(view -> addTaskList());
        refreshButton.setOnClickListener(view -> refresh());
    }

    private void addTaskList() {
        Intent intent = new Intent(this, AddTaskListActivity.class);
        startActivityForResult(intent, Consts.REQUEST_CODE_ADDED_TASKLIST);
    }

    private void changeAccount() {
        googleSignInModel.singOut();
        googleSignInModel.signIn();
    }

    public void onAccountChanged() {
        if (taskListsModel == null) {
            taskListsModel = new TaskListsModel(this, googleSignInModel.getAccount());
        } else {
            taskListsModel.changeAccount(this, googleSignInModel.getAccount());
        }

        refresh();
    }

    private void refresh() {
        taskListListView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        taskListListView.removeHeaderView(taskListsHeader);
        taskListListView.removeFooterView(taskListsFooter);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                ArrayList<TaskList> taskListsArray = taskListsModel.getTasksListAsArray();
                runOnUiThread(() -> getTaskListsCallback(taskListsArray));
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), Consts.REQUEST_CODE_RECOVERABLE_AUTH);
            } catch (IOException e) {
                showRuntimeAlertDialog();
            }
        });
    }

    private void addNewTaskList(String title) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                if (title.isEmpty()) {
                    throw new IOException();
                }

                taskListsModel.insertTaskList(title);
                runOnUiThread(this::refresh);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void getTaskListsCallback(ArrayList<TaskList> taskLists) {
        //TODO: what if empty list

        TaskListListAdapter adapter = new TaskListListAdapter(this, taskLists);
        taskListListView.addHeaderView(taskListsHeader);
        taskListListView.addFooterView(taskListsFooter);

        taskListListView.setAdapter(adapter);
        taskListListView.setOnItemClickListener((adapterView, view, i, l) -> onClickTaskList(taskLists, i));
        progressBar.setVisibility(View.INVISIBLE);
        taskListListView.setVisibility(View.VISIBLE);
        accountNameView.setText(googleSignInModel.getAccount().name);
    }

    public void showRuntimeAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.runtime_alert_dialog_title);
        builder.setMessage(R.string.runtime_alert_dialog_message);
        builder.show();
    }

    private void displayClock() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                clockView.setText(DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()).substring(0, 5));
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);
    }

    private void onClickTaskList(ArrayList<TaskList> taskLists, int i) {
        Intent intent = new Intent(this, TaskListActivity.class);
        intent.putExtra(Consts.KEY_TASKLIST_ID, taskLists.get(i - 1).getId());
        intent.putExtra(Consts.KEY_TASKLIST_TITLE, taskLists.get(i - 1).getTitle());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Consts.REQUEST_CODE_SING_IN) {
                googleSignInModel.onSignedResult(data);
                taskListsModel.changeAccount(this, googleSignInModel.getAccount());
                onAccountChanged();
            } else if (requestCode == Consts.REQUEST_CODE_RECOVERABLE_AUTH) {
                onAccountChanged();
            } else if (requestCode == Consts.REQUEST_CODE_ADDED_TASKLIST) {
                String title = data.getStringExtra(Consts.KEY_TASKLIST_TITLE);
                addNewTaskList(title);
            }
        }
    }
}