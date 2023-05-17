package com.pam.todolist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.FieldPosition


class TaskDetailsActivity : AppCompatActivity() {

    private var task: Task? = null

    private lateinit var title: TextView
    private lateinit var category: TextView
    private lateinit var status: TextView
    private lateinit var description: TextView
    private lateinit var createTime: TextView
    private lateinit var deadline: TextView
    private lateinit var attachments: RecyclerView

    private lateinit var markButton: Button
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        task = intent.getSerializableExtra("task") as Task?

        title = findViewById(R.id.task_title)
        category = findViewById(R.id.task_category)
        status = findViewById(R.id.task_status)
        description = findViewById(R.id.task_description)
        createTime = findViewById(R.id.task_create_time)
        deadline = findViewById(R.id.task_deadline)
        attachments = findViewById(R.id.task_attachments)
        markButton = findViewById(R.id.mark_button)
        editButton = findViewById(R.id.edit_button)
        deleteButton = findViewById(R.id.delete_button)

        markButton.isEnabled = !(task?.isCompleted == true)
        markButton.setOnClickListener{
            Log.i("Details", "Testing mark as done")
            markButton.isEnabled = false
            task!!.isCompleted  = true
            status.text = "Done"
            AddTaskActivity.markAsDone(task!!)
        }
        editButton.setOnClickListener {
            launcher.launch(task)
        }
        deleteButton.setOnClickListener {
            if (task != null) {
                val id : Long = task!!.id
                MainActivity.getDbHandler().deleteTask(id)
                val attachments : List<Uri> = task!!.attachments
                for (attachment in attachments) {
                    attachment.path?.let { it1 -> File(it1) }?.delete()
                }
                if(task!!.isCompleted) {
                    markButton.isEnabled = false
                }
            }
            finish()
        }

        Log.i("TaskLoader", "Loaded " + (task?.title ?: "N/A"))

        setViews()
    }

    private fun setViews() {
        title.setText(task?.title ?: "N/A")
        category.setText(task?.category ?: "N/A")
        if (task?.isCompleted == true) {
            status.text = "Done"
        } else {
            status.text = "In progress"
        }
        description.text = task?.description ?: "N/A"
        createTime.text = ("Created: " + task?.createTime) ?: "N/A"
        deadline.text = ("Deadline: " + task?.deadline) ?: "N/A"

        val attachmentAdapter = AttachmentAdapter(ArrayList<Uri>(task!!.attachments), object: AttachmentAdapter.OnClickListener {
            override fun onLongClick(uri: Uri, position: Int) {

            }
        })

        attachments.layoutManager = LinearLayoutManager(this)
        attachments.adapter = attachmentAdapter
    }

    val MY_CONTRACT: ActivityResultContract<Task?, Task?> =
        object : ActivityResultContract<Task?, Task?>() {
            override fun createIntent(context: Context, input: Task?): Intent {
                val intent = Intent(context, EditTaskActivity::class.java)
                intent.putExtra("task", input)
                return intent
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Task? {
                return if (intent != null && resultCode == RESULT_OK) {
                    intent.getSerializableExtra("task")!! as Task
                } else null
            }
        }
    var launcher: ActivityResultLauncher<Task?> = registerForActivityResult(MY_CONTRACT,
        ActivityResultCallback<Task?> { result ->
            if (result != null) {
                // Here is your modified object returned from SecondActivity
                task = result
                setViews()
            }
        })
}