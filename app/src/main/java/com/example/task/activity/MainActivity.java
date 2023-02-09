package com.example.task.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.task.R;
import com.example.task.databinding.ActivityMainBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TaskListsModel;
import com.example.task.other.TaskListListAdapter;
import com.google.api.services.tasks.model.TaskList;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ActivityMainBinding binding;
    private GoogleSignInModel googleSignInModel;
    private TaskListsModel taskListsModel;

    private ListView taskListListView;
    private View taskListsHeader;
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
        taskListListView = findViewById(R.id.tasklist_list_view);
        taskListsHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.tasklist_list_header, null, false);
        progressBar = findViewById(R.id.tasklist_progressbar);

        Button changeAccountButton = taskListsHeader.findViewById(R.id.change_account_tasklists_button);
        Button refreshButton = taskListsHeader.findViewById(R.id.refresh_tasklists_button);

        changeAccountButton.setOnClickListener(view -> changeAccount());

        refreshButton.setOnClickListener(view -> refresh());
    }

    private void changeAccount() {
        googleSignInModel.signOut();
        googleSignInModel.signIn();
    }

    private void refresh() {
        taskListListView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        taskListListView.removeHeaderView(taskListsHeader);
        taskListsModel.getTasksList();
    }

    private void goToAnotherActivity() {
        Intent intent = new Intent(this, TaskListsActivity.class);
        startActivity(intent);
    }

    public void updateUI(ArrayList<TaskList> taskLists) {
        TaskListListAdapter adapter = new TaskListListAdapter(this, taskLists);
        taskListListView.addHeaderView(taskListsHeader);
        taskListListView.setAdapter(adapter);
        taskListListView.setOnItemClickListener((adapterView, view, i, l) -> Toast.makeText(MainActivity.this, taskLists.get(i - 1).getTitle(), Toast.LENGTH_SHORT).show());
        progressBar.setVisibility(View.INVISIBLE);
        taskListListView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            googleSignInModel.onLogResult(data);
            taskListsModel.changeAccount(googleSignInModel.getAccount());
            refresh();
        }
    }
}