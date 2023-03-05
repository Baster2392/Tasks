package com.example.task.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.ListAdapter;

import com.example.task.R;
import com.example.task.databinding.ActivityTaskBinding;

import java.util.ArrayList;

public class TaskActivity extends Activity {

    private TextView mTextView;
    private ActivityTaskBinding binding;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listView = findViewById(R.id.subtask_list_view);
        @SuppressLint("InflateParams") View taskListHeader = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_task_header, null, false);
        @SuppressLint("InflateParams") View taskListFooter = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_task_footer, null, false);
        TextView title = taskListHeader.findViewById(R.id.task_date_view);
        title.setText("TAAAAAAAAAAAAAAAAA");
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("CLIIIICKKCKCKC");
            }
        });
        listView.addHeaderView(taskListHeader);
        listView.addFooterView(taskListFooter);
        listView.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);
    }
}