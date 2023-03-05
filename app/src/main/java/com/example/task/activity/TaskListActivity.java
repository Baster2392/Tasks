package com.example.task.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.Toast;

import com.example.task.R;
import com.example.task.databinding.ActivityTaskListBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TasksModel;
import com.example.task.other.Consts;
import com.example.task.other.TaskListAdapter;
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
    private TasksModel tasksModel;
    private ListView taskListView;
    private TaskListAdapter adapter;
    private TextView clockView, taskListTitleView;
    private View taskListHeader;
    private ProgressBar progressBar;
    private Timer timer;
    private String taskListID, taskListTitle;
    private ArrayList<Task> tasks, completed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleSignInModel = new GoogleSignInModel(this);
        taskListID = getIntent().getStringExtra(Consts.KEY_TASKLIST_ID);
        taskListTitle = getIntent().getStringExtra(Consts.KEY_TASKLIST_TITLE);

        init();
        refresh();
    }

    @SuppressLint("InflateParams")
    private void init() {
        tasksModel = new TasksModel(this, googleSignInModel.getAccount(), taskListID);

        progressBar = findViewById(R.id.tasklist_progressbar);
        taskListView = findViewById(R.id.tasklist_view);
        taskListHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tasklist_header, null, false);
        taskListView.addHeaderView(taskListHeader);
        clockView = taskListHeader.findViewById(R.id.tasklist_clock_view);
        taskListTitleView = taskListHeader.findViewById(R.id.tasklist_list_title_view);

        displayClock();
        taskListTitleView.setText(taskListTitle);
        Button addTaskButton = taskListHeader.findViewById(R.id.go_to_add_task_activity_button);
        Button refreshButton = taskListHeader.findViewById(R.id.refresh_tasklist_button);

        addTaskButton.setOnClickListener(view -> addTask());
        refreshButton.setOnClickListener(view -> refresh());

        completed = new ArrayList<>();
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
                tasks = tasksModel.getUncompletedTasksAsArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            runOnUiThread(this::getTasksCallback);
        });
    }

    private void updateList() {
        tasks.removeAll(completed);
        adapter = new TaskListAdapter(this, this, tasks);
        taskListView.setAdapter(adapter);
    }

    private void getTasksCallback() {
        taskListView.addHeaderView(taskListHeader);
        adapter = new TaskListAdapter(this, this, tasks);

        taskListView.setAdapter(adapter);
        progressBar.setVisibility(View.INVISIBLE);
        taskListView.setVisibility(View.VISIBLE);
    }

    private void taskCompletedCallback(Task task) {
        completed.remove(task);
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
        Task task = tasks.get(i);
        Toast.makeText(this, "Completed: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        completed.add(task);
        updateList();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                tasksModel.setTaskDone(task);
                taskCompletedCallback(task);
            } catch (IOException e) {
                completed.remove(task);
                throw new RuntimeException(e);
            }
        });
    }

    public void onClickTask(View view, int i) {
        Intent intent = new Intent(this, TaskActivity.class);
        startActivityForResult(intent, 100);
    }
}