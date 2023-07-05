package com.example.task.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.task.R;
import com.google.api.services.tasks.model.TaskList;

import java.util.ArrayList;

public class TaskListListAdapter extends ArrayAdapter<TaskList> {

    public TaskListListAdapter(Context context, ArrayList<TaskList> taskLists) {
        super(context, R.layout.tasklist_list_item, taskLists);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TaskList taskList = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tasklist_list_item, parent, false);
        }

        TextView taskListTitle = convertView.findViewById(R.id.tasklist_list_item_title_view);
        taskListTitle.setText(taskList.getTitle());

        return convertView;
    }
}
