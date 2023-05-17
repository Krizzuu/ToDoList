package com.pam.todolist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EditTaskActivity : AddTaskActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Edit"

        val intent = intent
        task = intent.getSerializableExtra("task") as Task

        if (task != null) {
            titleEdit.setText(task!!.title)
            descriptionEdit.setText(task!!.description)
            notificationsBox.isChecked = task!!.isNotify
            categoryEdit.setText(task!!.category)
            attachments = task!!.attachments as ArrayList<Uri>
            attachmentAdapter.updateAttachments(attachments)
            attachmentAdapter.notifyDataSetChanged()

            val deadlineString = task!!.deadline
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val deadlineDate = LocalDateTime.parse(deadlineString, formatter)
            datePicker.updateDate(deadlineDate.year, deadlineDate.monthValue, deadlineDate.dayOfMonth)

            timePicker.hour = deadlineDate.hour
            timePicker.minute = deadlineDate.minute
        }

        fun saveTask() {
            val intent = Intent(this, NotificationService::class.java)
            intent.putExtra("task", task)
            intent.action = "cancel"
            startService(intent)
            super.saveTask()
            finish()
        }
    }

}