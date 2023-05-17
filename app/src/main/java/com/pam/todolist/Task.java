package com.pam.todolist;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Task implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String createTime;
    private String deadline;
    private boolean completed;
    private boolean notify;
    private String category;

    private String attachments;

    public Task(Long id, String title, String category, String description, String deadline,
                boolean notificationsEnabled, List<Uri> attachments) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.deadline = deadline;
        this.notify = notificationsEnabled;
        this.attachments = convertUriListToString(attachments);
        this.createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        this.completed = false;
    }

    public Task() {
        this.id = null;
        this.title = "N/A";
        this.category = "N/A";
        this.description = "N/A";
        this.deadline = "N/A";
        this.notify = false;
        this.attachments = null;
        this.createTime = "N/A";
        this.completed = false;
    }

    @NonNull
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creationTime='" + createTime + '\'' +
                ", deadline='" + deadline + '\'' +
                ", completed=" + completed +
                ", notificationEnabled=" + notify +
                ", category='" + category + '\'' +
                ", attachments=" + attachments +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Uri> getAttachments() {
        return convertStringToUriList(attachments);
    }

    public void setAttachments(List<Uri> attachments) {
        this.attachments = convertUriListToString(attachments);
    }

    public void setAttachmentsWithString(String attachments) {
        this.attachments = attachments;
    }

    public static List<Task> sortTasksByDeadline(List<Task> tasks) {
        tasks.sort((task1, task2) -> {
            if (task1.getDeadline() == null && task2.getDeadline() == null) {
                return 0;
            } else if (task1.getDeadline() == null) {
                return 1;
            } else if (task2.getDeadline() == null) {
                return -1;
            } else {
                return task1.getDeadline().compareTo(task2.getDeadline());
            }
        });
        return tasks;
    }

    public static List<Task> filterTasksByCategory(List<Task> tasks, String category) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getCategory().equals(category)) {
                filteredTasks.add(task);
            }
        }

        return sortTasksByDeadline(filteredTasks);
    }

    public static String convertUriListToString(List<Uri> uris) {
        StringBuilder sb = new StringBuilder();
        for (Uri uri : uris) {
            sb.append(uri.toString()).append(";");
        }
        return sb.toString();
    }

    public static List<Uri> convertStringToUriList(String uriString) {
        List<Uri> uris = new ArrayList<>();
        String[] uriArray = uriString.split(";");
        for (String s : uriArray) {
            if (!s.isEmpty()) {
                uris.add(Uri.parse(s));
            }
        }
        return uris;
    }
}
