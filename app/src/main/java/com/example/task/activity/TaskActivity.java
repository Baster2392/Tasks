package com.example.task.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.task.R;
import com.example.task.adapter.TaskListAdapter;
import com.example.task.databinding.ActivityTaskBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TasksModel;
import com.example.task.other.Consts;
import com.example.task.adapter.TaskListAdaptiveActivity;
import com.example.task.other.NetworkConnectionErrorDialog;
import com.example.task.other.TaskPositionComparator;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskActivity extends Activity implements TaskListAdaptiveActivity {

    private TasksModel tasksModel;
    private GoogleSignInModel googleSignInModel;
    private View taskListHeader, taskListFooter;
    private ProgressBar progressBar;
    private TaskListAdapter adapter;
    private ImageButton completedButton, deleteButton;
    private Button addTaskButton;
    private ActivityTaskBinding binding;
    private ListView childrenView;
    private ArrayList<Task> children, completed;
    private String id, title, notes, taskListTitle, taskListId;
    private String due;
    private boolean hasPotentiallyChildren;
    private TaskPositionComparator comparator;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleSignInModel = new GoogleSignInModel(this);
        tasksModel = new TasksModel(this, googleSignInModel.getAccount());
        children = new ArrayList<>();
        completed = new ArrayList<>();
        comparator = new TaskPositionComparator();

        // getting data from previous activity
        Intent extras = getIntent();
        hasPotentiallyChildren = extras.getBooleanExtra(Consts.KEY_HAS_POTENTIALLY_CHILDREN, false);
        id = extras.getStringExtra(Consts.KEY_TASK_ID);
        title = extras.getStringExtra(Consts.KEY_TASK_TITLE);
        notes = extras.getStringExtra(Consts.KEY_TASK_NOTES);
        due = extras.getStringExtra(Consts.KEY_TASK_DUE);
        taskListTitle = extras.getStringExtra(Consts.KEY_TASKLIST_TITLE);
        taskListId = extras.getStringExtra(Consts.KEY_TASKLIST_ID);

        init();
        refresh();
    }

    private void init() {
        // getting view
        progressBar = findViewById(R.id.task_progressbar);
        childrenView = findViewById(R.id.subtask_list_view);
        taskListHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_task_header, null, false);
        taskListFooter = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_task_footer, null, false);
        TextView titleView = taskListHeader.findViewById(R.id.task_title_view);
        TextView listTitleView = taskListHeader.findViewById(R.id.task_list_name_view);
        TextView dueView = taskListHeader.findViewById(R.id.task_date_view);
        TextView notesView = taskListHeader.findViewById(R.id.task_description_view);
        addTaskButton = taskListHeader.findViewById(R.id.add_task_button);
        completedButton = taskListFooter.findViewById(R.id.task_done_button);
        deleteButton = taskListFooter.findViewById(R.id.task_delete_button);

        if (!hasPotentiallyChildren) {
            RelativeLayout addTaskButtonLayout = taskListHeader.findViewById(R.id.add_task_button_layout);
            addTaskButtonLayout.removeAllViews();
        }

        if (due == null) {
            LinearLayout dueLayout = taskListHeader.findViewById(R.id.task_date_layout);
            dueLayout.removeAllViews();
        }

        if (notes == null) {
            LinearLayout notesLayout = taskListHeader.findViewById(R.id.task_description_layout);
            notesLayout.removeAllViews();
        }

        titleView.setText(title);
        listTitleView.setText(taskListTitle);
        dueView.setText(due);
        notesView.setText(notes);

        completedButton.setOnClickListener(view -> taskCompleted());
        deleteButton.setOnClickListener(view -> taskDeleted());
        addTaskButton.setOnClickListener(view -> addTask());

        // setting list view
        adapter = new TaskListAdapter(this, this, children);
        childrenView.setAdapter(adapter);
        childrenView.addHeaderView(taskListHeader);
        childrenView.addFooterView(taskListFooter);
        childrenView.setVisibility(View.VISIBLE);
    }

    private void refresh() {
        if (!hasPotentiallyChildren) {
            return;
        }

        childrenView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        childrenView.removeHeaderView(taskListHeader);
        childrenView.removeFooterView(taskListFooter);

        Context context = this;
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                children = tasksModel.getUncompletedChildren(taskListId, id);
            } catch (IOException e) {
                runOnUiThread(() -> NetworkConnectionErrorDialog.show(context, e));
            }

            runOnUiThread(this::getTasksCallback);
        });
    }

    private void getTasksCallback() {
        children.sort(comparator);
        childrenView.addHeaderView(taskListHeader);
        adapter = new TaskListAdapter(this, this, children);
        childrenView.setAdapter(adapter);
        childrenView.addFooterView(taskListFooter);

        progressBar.setVisibility(View.INVISIBLE);
        childrenView.setVisibility(View.VISIBLE);
    }

    public void taskCompleted() {
        Intent data = new Intent();
        data.putExtra(Consts.KEY_TASK_ID, id);

        setResult(Consts.REQUEST_CODE_TASK_COMPLETED, data);
        finish();
    }

    public void taskDeleted() {
        Intent data = new Intent();
        data.putExtra(Consts.KEY_TASK_ID, id);

        setResult(Consts.REQUEST_CODE_TASK_DELETED, data);
        finish();
    }

    public void setTaskCompleted(Task task) {
        Toast.makeText(this, "Completed: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        completed.add(task);
        updateList();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                tasksModel.setTaskDone(task, taskListId);
                taskCompletedCallback(task);
            } catch (IOException e) {
                NetworkConnectionErrorDialog.show(this, e);
            }
        });
    }

    private void taskCompletedCallback(Task task) {
        completed.remove(task);
    }

    public void setTaskDeleted(Task task) {
        Toast.makeText(this, "Deleted: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        children.remove(task);
        updateList();

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                tasksModel.deleteTask(task, taskListId);
            } catch (IOException e) {
                NetworkConnectionErrorDialog.show(this, e);
            }
        });
    }

    private void updateList() {
        children.removeAll(completed);
        adapter = new TaskListAdapter(this, this, children);
        childrenView.setAdapter(adapter);
    }
    
    public void onClickTask(View view, int position) {
        Task chosenTask = children.get(position);
        System.out.println(id + " " + chosenTask.getParent());
        Intent intent = new Intent(this, TaskActivity.class);

        intent.putExtra(Consts.KEY_HAS_POTENTIALLY_CHILDREN, false);
        intent.putExtra(Consts.KEY_TASK_ID, chosenTask.getId());
        intent.putExtra(Consts.KEY_TASK_TITLE, chosenTask.getTitle());
        intent.putExtra(Consts.KEY_TASKLIST_TITLE, taskListTitle);
        intent.putExtra(Consts.KEY_TASKLIST_ID, taskListId);

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

    public void onClickTaskRadio(View view, int position) {
        setTaskCompleted(children.get(position));
    }

    private void addTask() {
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra(Consts.KEY_TASKLIST_ID, taskListId);
        intent.putExtra(Consts.KEY_TASK_PARENT, id);
        startActivityForResult(intent, Consts.REQUEST_CODE_TASK_ADDED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Consts.REQUEST_CODE_TASK_COMPLETED) {
            String completedId = data.getStringExtra(Consts.KEY_TASK_ID);
            for (Task task : children) {
                if (task.getId().equals(completedId)) {
                    setTaskCompleted(task);
                    break;
                }
            }

            if (children.size() == 0) {
                hasPotentiallyChildren = false;
            }
        }
        else if (resultCode == Consts.REQUEST_CODE_TASK_DELETED) {
            String deletedId = data.getStringExtra(Consts.KEY_TASK_ID);
            for (Task task : children) {
                if (task.getId().equals(deletedId)) {
                    setTaskDeleted(task);
                    break;
                }
            }

            if (children.size() == 0) {
                hasPotentiallyChildren = false;
            }

        } else if (resultCode == Consts.REQUEST_CODE_TASK_ADDED) {
            hasPotentiallyChildren = true;
            refresh();
        }
    }
}