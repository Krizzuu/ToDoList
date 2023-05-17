package com.pam.todolist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    TextView title;
    TextView category;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.task_title);
        category = itemView.findViewById(R.id.task_category);
    }

    public void bind(Task task, TaskAdapter.OnTaskClickListener listener) {
        title.setText(task.getTitle());
        category.setText(task.getCategory());
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTaskClick(task);
            }
        });
    }
}
