package com.pam.todolist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*


open class AddTaskActivity : AppCompatActivity() {

    protected lateinit var attachmentAdapter: AttachmentAdapter
    protected lateinit var titleEdit: EditText
    protected lateinit var categoryEdit: EditText
    protected lateinit var descriptionEdit: EditText
    protected lateinit var datePicker: DatePicker
    protected lateinit var timePicker: TimePicker
    protected lateinit var notificationsBox: CheckBox
    protected lateinit var attachmentsRecyclerView: RecyclerView

    protected lateinit var saveButton: Button
    protected lateinit var addAttachment: Button

    protected lateinit var attachments: ArrayList<Uri>
    protected var task : Task? = null

    protected val getContentLauncher = registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { result ->
        if (result != null) {
            val imagePath = copyImageToPrivateStorage(result);
            Log.i("ImageLoader", "Copied an image")
            if (imagePath != null) {
                attachments.add(imagePath)
                Log.i("Loaded:", imagePath.toString())
                attachmentAdapter.updateAttachments(attachments)
            }
            else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        val resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
            }
        }
        attachments = ArrayList()
        titleEdit = findViewById(R.id.task_title)
        categoryEdit = findViewById(R.id.task_category)
        descriptionEdit = findViewById(R.id.task_description)
        datePicker = findViewById(R.id.task_deadline_date)
        timePicker = findViewById(R.id.task_deadline_time)
        notificationsBox = findViewById(R.id.task_notification)
        attachmentsRecyclerView = findViewById(R.id.task_attachments)

        saveButton = findViewById(R.id.save_task_button)
        addAttachment = findViewById(R.id.add_attachment_button)

        saveButton.setOnClickListener{ saveTask() }
        addAttachment.setOnClickListener{
            getContentLauncher.launch("image/*");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ToDoList channel"
            val description = "Channel to remind about tasks"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("reminder_channel", name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }

        attachmentAdapter = AttachmentAdapter(ArrayList<Uri>(), object:
            AttachmentAdapter.OnClickListener {
            override fun onLongClick(uri: Uri, position: Int) {
                val builder = AlertDialog.Builder(this@AddTaskActivity)
                builder.setMessage("Do you want to delete this image?")
                    .setPositiveButton("Yes") { _, _ ->
                        val attachment = attachments[position]
                        attachments.remove(attachment)
                        attachment.path?.let { it1 -> File(it1) }?.delete()

                        attachmentAdapter.notifyItemRemoved(position)
                    }
                    .setNegativeButton(
                        "No"
                    ) { dialog, _ -> dialog.dismiss() }
                builder.create().show()
            }
        })

        attachmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        attachmentsRecyclerView.adapter = attachmentAdapter
    }

    fun saveTask() {
        val category: String = categoryEdit.text.toString().trim()
        if (category.isBlank()) {
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show()
            return
        }
        val title: String = titleEdit.text.toString()
        val description: String = descriptionEdit.text.toString()

        val day: Int = datePicker.dayOfMonth
        val month: Int = datePicker.month + 1
        val year: Int = datePicker.year
        val date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month, day)

        val hour: Int = timePicker.hour
        val minute: Int = timePicker.minute
        val time: String = String.format(Locale.getDefault(),"%02d:%02d", hour, minute)

        val notify: Boolean = notificationsBox.isChecked

        var newTask = Task(null, title, category, description, "$date $time", notify, attachments)

        if (task != null) {
            newTask.id = task!!.id
            MainActivity.getDbHandler().updateTask(newTask)
            if (task!!.isNotify && !newTask.isNotify) {
                val intent = Intent(this, NotificationService::class.java)
                intent.putExtra("task", task)
                intent.action = "cancel"
                startService(intent)
            }
        }
        else {
            newTask.id = MainActivity.getDbHandler().addTask(newTask)
        }
        task = newTask

        if (this is EditTaskActivity) {
            val resultIntent = Intent()
            resultIntent.putExtra("task", task)
            setResult(RESULT_OK, resultIntent)
        }

        if (task!!.isNotify) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val intent = Intent(this, NotificationService::class.java)
        intent.putExtra("task", task)
        intent.action = "schedule"
        startService(intent)

        finish()
    }


    private fun copyImageToPrivateStorage(sourceUri: Uri): Uri? {
        return try {
            val inputStream: InputStream = contentResolver.openInputStream(sourceUri) ?: return null
            val fileName = "attachment_" + System.currentTimeMillis() + ".jpg"
            val directory: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(directory, fileName)
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
            inputStream.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            }
        }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private var hasNotificationPermissionGranted = false

    companion object {
        fun markAsDone(task: Task) {
            MainActivity.getDbHandler().updateTask(task)
        }
    }
}