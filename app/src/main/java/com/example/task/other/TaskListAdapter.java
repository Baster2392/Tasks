package com.example.task.other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.task.R;
import com.example.task.activity.TaskListActivity;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;

public class TaskListAdapter extends ArrayAdapter<Task> {
    private final TaskListActivity activity;
    public TaskListAdapter(TaskListActivity activity, Context context, ArrayList<Task> tasks) {
        super(context, R.layout.task_list_item, tasks);
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Task task = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_list_item, parent, false);
        }

        TextView titleView = convertView.findViewById(R.id.task_list_item_title_view);
        titleView.setText(task.getTitle());
        titleView.setOnClickListener(view -> activity.onClickTask(view, position));

        RadioButton radioButton = convertView.findViewById(R.id.task_list_item_radio_button);
        radioButton.setOnClickListener(view -> activity.onClickTaskRadio(view, position));

        return convertView;
    }
}
