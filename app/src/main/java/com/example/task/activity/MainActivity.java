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
import android.widget.Toast;

import com.example.task.R;
import com.example.task.databinding.ActivityMainBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TaskListsModel;
import com.example.task.other.TaskListListAdapter;
import com.google.api.services.tasks.model.TaskList;

import java.text.DateFormat;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;
    private GoogleSignInModel googleSignInModel;
    private TaskListsModel taskListsModel;
    private ListView taskListListView;
    private TextView accountNameView;
    private View taskListsHeader, taskListsFooter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleSignInModel = new GoogleSignInModel(this);
        if (googleSignInModel.getAccount() != null) {
            taskListsModel = new TaskListsModel(this, googleSignInModel.getAccount());
        } else {
            googleSignInModel.signIn();
        }

        init();

        if (googleSignInModel.getAccount() != null) {
            taskListsModel.getTasksList();
        }
    }

    @SuppressLint("InflateParams")
    private void init() {
        progressBar = findViewById(R.id.tasklist_progressbar);
        taskListListView = findViewById(R.id.tasklist_list_view);
        taskListsHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tasklist_list_header, null, false);
        taskListsFooter = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tasklist_list_footer, null, false);

        TextView clockView = taskListsHeader.findViewById(R.id.tasklisk_clock_view);
        Button addTaskListButton = taskListsHeader.findViewById(R.id.add_tasklists_button);
        Button refreshButton = taskListsHeader.findViewById(R.id.refresh_tasklists_button);
        accountNameView = taskListsFooter.findViewById(R.id.tasklists_account_name_view);

        taskListsFooter.setOnClickListener(view -> changeAccount());
        clockView.setText(DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()));
        addTaskListButton.setOnClickListener(view -> addTaskList());
        refreshButton.setOnClickListener(view -> refresh());
    }

    private void addTaskList() {
        // TODO: adding tasklist
    }

    private void changeAccount() {
        googleSignInModel.signIn();
    }

    public void onAccountChanged() {
        taskListsModel.changeAccount(googleSignInModel.getAccount());
        refresh();
    }

    private void refresh() {
        taskListListView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        taskListListView.removeHeaderView(taskListsHeader);
        taskListListView.removeFooterView(taskListsFooter);

        taskListsModel.getTasksList();
    }

    private void goToAnotherActivity() {
        Intent intent = new Intent(this, TaskListsActivity.class);
        startActivity(intent);
    }

    public void displayTaskLists(ArrayList<TaskList> taskLists) {
        TaskListListAdapter adapter = new TaskListListAdapter(this, taskLists);
        taskListListView.addHeaderView(taskListsHeader);
        taskListListView.addFooterView(taskListsFooter);

        taskListListView.setAdapter(adapter);
        taskListListView.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(MainActivity.this, taskLists.get(i - 1).getTitle(), Toast.LENGTH_SHORT).show());
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
}