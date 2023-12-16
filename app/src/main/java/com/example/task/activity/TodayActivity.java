package com.example.task.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.task.R;
import com.example.task.adapter.TaskListAdapter;
import com.example.task.adapter.TaskListAdaptiveActivity;
import com.example.task.databinding.ActivityTodayBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TasksModel;
import com.example.task.other.Consts;
import com.example.task.other.NetworkConnectionErrorDialog;
import com.example.task.other.TaskInfo;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodayActivity extends Activity implements TaskListAdaptiveActivity {

    private GoogleSignInModel googleSignInModel;
    private TasksModel tasksModel;
    private ArrayList<TaskInfo> taskInfos, completed;
    private TextView clockView;
    private ActivityTodayBinding binding;
    private ListView taskListView;
    private TaskListAdapter adapter;
    private DateTime todayDate;
    private View taskListHeader;
    private ProgressBar progressBar;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTodayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleSignInModel = new GoogleSignInModel(this);
        tasksModel = new TasksModel(this, googleSignInModel.getAccount());

        init();
        refresh();
    }

    private void init() {
        todayDate = new DateTime(Calendar.getInstance().getTime());
        completed = new ArrayList<>();

        taskListView = findViewById(R.id.tasklist_view);
        progressBar = findViewById(R.id.tasklist_progressbar);
        taskListHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.today_tasklist_header, null, false);
        taskListView.addHeaderView(taskListHeader);

        clockView = taskListView.findViewById(R.id.tasklist_clock_view);
        displayClock();
    }

    private void refresh() {
        taskListView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        taskListView.removeHeaderView(taskListHeader);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                taskInfos = tasksModel.getUncompletedTasksByDate(todayDate);

                runOnUiThread(() -> getTaskListsCallback(taskInfos));
            } catch (IOException e) {
                NetworkConnectionErrorDialog.show(this, e);
            }
        });
    }

    private void getTaskListsCallback(ArrayList<TaskInfo> taskInfoArray) {
        ArrayList<Task> tasks = new ArrayList<>();

        for (TaskInfo taskInfo : taskInfoArray) {
            tasks.add(taskInfo.getTask());
        }

        adapter = new TaskListAdapter(this, this, tasks);
        taskListView.addHeaderView(taskListHeader);
        taskListView.setAdapter(adapter);

        progressBar.setVisibility(View.INVISIBLE);
        taskListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClickTask(View view, int i) {
        Task chosenTask = taskInfos.get(i).getTask();
        Intent intent = new Intent(this, TaskActivity.class);

        System.out.println(chosenTask.getParent());

        intent.putExtra(Consts.KEY_HAS_POTENTIALLY_CHILDREN, chosenTask.getParent() == null);
        intent.putExtra(Consts.KEY_TASK_ID, chosenTask.getId());
        intent.putExtra(Consts.KEY_TASK_TITLE, chosenTask.getTitle());
        intent.putExtra(Consts.KEY_TASKLIST_TITLE, taskInfos.get(i).getTaskListTitle());
        intent.putExtra(Consts.KEY_TASKLIST_ID, taskInfos.get(i).getTaskListId());

        if (chosenTask.getDue() != null)
        {
            intent.putExtra(Consts.KEY_TASK_DUE, chosenTask.getDue().toString().substring(0, 10));
        }

        if (chosenTask.getNotes() != null)
        {
            intent.putExtra(Consts.KEY_TASK_NOTES, chosenTask.getNotes());
        }

        startActivityForResult(intent, Consts.REQUEST_CODE_TASK_DETAILS);
    }

    @Override
    public void onClickTaskRadio(View view, int position) {
        setTaskCompleted(taskInfos.get(position));
    }

    public void setTaskCompleted(TaskInfo taskInfo) {
        Task task = taskInfo.getTask();
        Toast.makeText(this, "Completed: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        completed.add(taskInfo);
        updateList();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                tasksModel.setTaskDone(task, taskInfo.getTaskListId());
                taskCompletedCallback(taskInfo);
            } catch (IOException e) {
                NetworkConnectionErrorDialog.show(this, e);
            }
        });
    }

    private void taskCompletedCallback(TaskInfo taskInfo) {
        completed.remove(taskInfo);
    }

    private void updateList() {
        taskInfos.removeAll(completed);
        ArrayList<Task> tasks = new ArrayList<>();

        for (TaskInfo taskInfo : taskInfos) {
            tasks.add(taskInfo.getTask());
        }

        adapter = new TaskListAdapter(this, this, tasks);
        taskListView.setAdapter(adapter);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Consts.REQUEST_CODE_TASK_COMPLETED) {
            String completedId = data.getStringExtra(Consts.KEY_TASK_ID);
            for (TaskInfo taskInfo : taskInfos) {
                if (taskInfo.getTask().getId().equals(completedId)) {
                    setTaskCompleted(taskInfo);
                    break;
                }
            }
        }
        else if (resultCode == Consts.REQUEST_CODE_TASK_DELETED) {
            String deletedId = data.getStringExtra(Consts.KEY_TASK_ID);
            for (TaskInfo taskInfo : taskInfos) {
                if (taskInfo.getTask().getId().equals(deletedId)) {
                    setTaskDeleted(taskInfo);
                    break;
                }
            }
        }
    }

    private void setTaskDeleted(TaskInfo taskInfo) {
        Toast.makeText(this, "Deleted: " + taskInfo.getTask().getTitle(), Toast.LENGTH_SHORT).show();
        taskInfos.remove(taskInfo);
        updateList();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                tasksModel.deleteTask(taskInfo.getTask(), taskInfo.getTaskListId());
            } catch (IOException e) {
                NetworkConnectionErrorDialog.show(this, e);
            }
        });
    }
}