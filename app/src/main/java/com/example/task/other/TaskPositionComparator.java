package com.example.task.other;

import com.google.api.services.tasks.model.Task;

import java.util.Comparator;

public class TaskPositionComparator implements Comparator<Task> {
    @Override
    public int compare(Task t1, Task t2) {
        return t1.getPosition().compareTo(t2.getPosition());
    }
}
