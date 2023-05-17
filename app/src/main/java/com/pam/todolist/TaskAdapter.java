package com.pam.todolist;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder>  {

    private List<Task> tasks;
    private final OnTaskClickListener onTaskClickListener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener onTaskClickListener) {
        this.tasks = tasks;
        this.onTaskClickListener = onTaskClickListener;
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_view, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task, this.onTaskClickListener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private List<Task> sortByDeadline(List<Task> tasks) {
        tasks.sort(((t1, t2) -> {
            if (t1.getDeadline() == null && t2.getDeadline() == null) {
                return 0;
            }
            else if (t1.getDeadline() == null) {
                return 1;
            }
            else if (t2.getDeadline() == null) {
                return -1;
            }
            else {
                return t1.getDeadline().compareTo(t2.getDeadline());
            }
        }));
        return tasks;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateTasks(List<Task> updatedTasks) {
        this.tasks = sortByDeadline(updatedTasks);
        this.notifyDataSetChanged();
    }
}
