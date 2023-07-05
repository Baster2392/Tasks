package com.example.task.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.task.R;
import com.example.task.databinding.ActivityAddTaskBinding;
import com.example.task.model.GoogleSignInModel;
import com.example.task.model.TasksModel;
import com.example.task.other.Consts;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddTaskActivity extends Activity {
    private TasksModel tasksModel;
    private ActivityAddTaskBinding binding;
    private DateTime date;
    private EditText titleEditText, notesEditText;
    private Button addButton;
    private TextView dateView;
    private String taskListId, parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GoogleSignInModel googleSignInModel = new GoogleSignInModel(this);
        tasksModel = new TasksModel(this, googleSignInModel.getAccount());
        taskListId = getIntent().getStringExtra(Consts.KEY_TASKLIST_ID);
        parentId = getIntent().getStringExtra(Consts.KEY_TASK_PARENT);

        titleEditText = findViewById(R.id.task_title_edittext);
        notesEditText = findViewById(R.id.task_notes_edittext);
        dateView = findViewById(R.id.date_picker);
        dateView.setOnClickListener(view -> buildDatePickerDialog());
        addButton = findViewById(R.id.add_task_button);
        addButton.setOnClickListener(view -> onAddButtonClicked());
    }

    private void buildDatePickerDialog() {
        DatePickerDialog dialog = new DatePickerDialog(AddTaskActivity.this);
        dialog.setOnDateSetListener((datePicker, year, month, day) -> {
            month += 1;
            String dateStr = day +
                    " - " + month +
                    " - " + year;
            dateView.setText(dateStr);

            dateStr = (day + 1) +
                    " - " + month +
                    " - " + year;

            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd - MM - yyyy");

            try {
                date = new DateTime(dateFormat.parse(dateStr));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        dialog.show();
    }

    private void onAddButtonClicked() {
        addButton.setClickable(false);
        addButton.setText(R.string.adding);
        addTask();
    }

    private void addTask() {
        String title = titleEditText.getText().toString();
        String notes = notesEditText.getText().toString();

        if (title.length() == 0) {
            Toast.makeText(this, "Type title", Toast.LENGTH_SHORT).show();
            titleEditText.setHintTextColor(Color.RED);

            addButton.setClickable(true);
            addButton.setText(R.string.add);
            return;
        }

        Task newTask = new Task();
        newTask.setTitle(title);

        if (notes.length() != 0) {
            newTask.setNotes(notes);
        }

        if (date != null) {
            newTask.setDue(date);
        }

        if (parentId != null) {
            newTask.setParent(parentId);
            System.out.println(newTask.getParent());
        }

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                tasksModel.createTask(newTask, taskListId);
                setResult(Consts.REQUEST_CODE_TASK_ADDED);
                finish();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}