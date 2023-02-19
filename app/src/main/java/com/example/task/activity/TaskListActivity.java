package com.example.task.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.task.R;
import com.example.task.databinding.ActivityTaskListBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TaskListsModel;
import com.example.task.other.Consts;
import com.example.task.other.TaskListAdapter;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskListActivity extends Activity {

    private ActivityTaskListBinding binding;
    private GoogleSignInModel googleSignInModel;
    private TaskListsModel taskListsModel;
    private ListView taskListView;
    private TextView clockView;
    private View taskListHeader;
    private ProgressBar progressBar;
    private Timer timer;
    private String taskListID, taskListTitle;
    private ArrayList<Task> tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleSignInModel = new GoogleSignInModel(this);
        init();

        taskListID = getIntent().getStringExtra(Consts.KEY_TASKLIST_ID);
        taskListTitle = getIntent().getStringExtra(Consts.KEY_TASKLIST_TITLE);
        refresh();
    }

    @SuppressLint("InflateParams")
    private void init() {
        taskListsModel = new TaskListsModel(this, googleSignInModel.getAccount());

        progressBar = findViewById(R.id.tasklist_progressbar);
        taskListView = findViewById(R.id.tasklist_view);
        taskListHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tasklist_header, null, false);
        taskListView.addHeaderView(taskListHeader);

        clockView = taskListHeader.findViewById(R.id.tasklist_clock_view);
        displayClock();
        Button addTaskButton = taskListHeader.findViewById(R.id.go_to_add_task_activity_button);
        Button refreshButton = taskListHeader.findViewById(R.id.refresh_tasklist_button);

        addTaskButton.setOnClickListener(view -> addTask());
        refreshButton.setOnClickListener(view -> refresh());
    }

    private void addTask() {
        //TODO: add task
    }

    private void refresh() {
        taskListView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        taskListView.removeHeaderView(taskListHeader);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                tasks = taskListsModel.getTasks(taskListID);
                runOnUiThread(this::getTasksCallback);
            } catch (UserRecoverableAuthIOException e) {
                startActivityForResult(e.getIntent(), Consts.REQUEST_CODE_RECOVERABLE_AUTH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void getTasksCallback() {
        taskListView.addHeaderView(taskListHeader);
        TaskListAdapter adapter = new TaskListAdapter(this, this, tasks);

        taskListView.setAdapter(adapter);
        progressBar.setVisibility(View.INVISIBLE);
        taskListView.setVisibility(View.VISIBLE);
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

    public void onClickTaskRadio(View view, int i) {
        Toast.makeText(this, tasks.get(i).getTitle(), Toast.LENGTH_SHORT).show();
        tasks.remove(i);
        TaskListAdapter adapter = new TaskListAdapter(this, this, tasks);
        taskListView.setAdapter(adapter);
    }
}