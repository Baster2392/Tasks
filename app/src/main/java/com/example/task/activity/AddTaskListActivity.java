package com.example.task.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.task.R;
import com.example.task.databinding.ActivityAddTaskListBinding;
import com.example.task.databinding.ActivityMainBinding;
import com.example.task.other.Consts;

public class AddTaskListActivity extends Activity {

    private ActivityAddTaskListBinding binding;
    private EditText titleEditText;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        titleEditText = findViewById(R.id.tasklist_title_edittext);
        addButton = findViewById(R.id.add_tasklist_button);
        addButton.setOnClickListener(view -> onClickAddButton());
    }

    private void onClickAddButton() {
        if (titleEditText.getText().toString().length() == 0) {
            titleEditText.setHintTextColor(Color.RED);
            Toast.makeText(this, "Type title", Toast.LENGTH_SHORT).show();
        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Consts.KEY_TASKLIST_TITLE, titleEditText.getText().toString());
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
}