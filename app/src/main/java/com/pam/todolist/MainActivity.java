package com.pam.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TaskAdapter taskAdapter;
    private static List<Task> tasks;
    private SearchView taskSearchView;
    private boolean hideDone = false;
    private FloatingActionButton addTaskButton;

    public static DatabaseHandler getDbHandler() {
        return dbHandler;
    }

    private static DatabaseHandler dbHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (dbHandler == null) {
            dbHandler = new DatabaseHandler(this);
        }
//        dbHandler.clearTable();
        loadRecyclerView();
        addTaskButton = findViewById(R.id.add_task_button);

        addTaskButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        taskSearchView = findViewById(R.id.task_search);
        taskSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTasks(newText);
                return true;
            }
        });
    }

    private void searchTasks(String query) {
        List<Task> filteredTasks;
        if (query.isEmpty()) {
            filteredTasks = tasks;
        } else {
            filteredTasks = new ArrayList<>();
            for (Task task : tasks) {
                if (task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredTasks.add(task);
                }
            }
        }

        taskAdapter.updateTasks(filteredTasks);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecyclerView();
    }

    private void loadRecyclerView() {
        tasks = new ArrayList<>();
        tasks = dbHandler.getAllTasks();
        ArrayList<Task> filteredTasks = null;
        if (hideDone) {
            filteredTasks = new ArrayList<>();
            tasks.stream().filter(t -> !t.isCompleted()).forEach(filteredTasks::add);
        }
        else {
            filteredTasks = (ArrayList<Task>) tasks;
        }
        taskAdapter = new TaskAdapter(filteredTasks, new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(MainActivity.this, TaskDetailsActivity.class);
                intent.putExtra("task", task);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.task_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        String label = hideDone ? "Show done" : "Hide done";
        MenuItem item = menu.findItem(R.id.hide_done);
        item.setTitle(label);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.filter_category) {
            showCategoryFilterDialog();
            Log.i("MenuSelect", "Filter by category");
            return true;
        } else if (id == R.id.change_remind_time) {
            showRemindTimeEditDialog();
            Log.i("MenuSelect", "Change remind time");
            return true;
        } else if (id == R.id.hide_done) {
            toggleHide();
            String label = hideDone ? "Show done" : "Hide done";
            item.setTitle(label);
            Log.i("MenuSelect", "Toggle hide");
            // TODO: Actually hide done tasks

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCategoryFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose category");

        Set<String> categoriesSet = new HashSet<>();
        for (Task task : tasks) {
            categoriesSet.add(task.getCategory());
        }

        List<String> categoriesList = new ArrayList<>(categoriesSet);
        categoriesList.add(0, "All");
        String[] categoriesArray = categoriesList.toArray(new String[0]);

        builder.setItems(categoriesArray, (dialogInterface, i) -> {
            if (i == 0) {
                taskAdapter.updateTasks(tasks);
            }
            else {
                String category = categoriesArray[i];
                List<Task> filteredTasks = Task.filterTasksByCategory(tasks, category);
                taskAdapter.updateTasks(filteredTasks);
            }
        });

        builder.setNegativeButton("cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRemindTimeEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        SharedPreferences config = getSharedPreferences(getString(R.string.config_file), MODE_PRIVATE);

        String[] remindTimeStrings = {
                "5 minutes before",
                "10 minutes before",
                "15 minutes before",
                "30 minutes before",
                "1 hour before"
        };

        int[] remindTimes = {5, 10, 15, 30, 60};

        builder.setTitle("Choose reminder time (now: " + config.getInt("remind_time", 15) + "m)");
        builder.setItems(remindTimeStrings, (dialogInterface, i) -> {
            int minutes = remindTimes[i];
            Log.i("RemindTime", "User has chosen to remind " + minutes + " minutes before deadline");
            SharedPreferences.Editor editor = config.edit();
            editor.putInt("remind_time", minutes);
            editor.apply();
            for (Task task : tasks) {
                Intent intent = new Intent(this, NotificationService.class);
                intent.putExtra("task", task);
                intent.setAction("update");
                startService(intent);
            }
        });
        builder.setNegativeButton("cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void toggleHide() {
        hideDone = !hideDone;
        loadRecyclerView();
    }
}