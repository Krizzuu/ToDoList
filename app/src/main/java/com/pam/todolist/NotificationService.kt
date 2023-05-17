package com.pam.todolist

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class NotificationService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val task: Task = intent.getSerializableExtra("task") as Task
        val action: String = intent.getAction() ?: return START_NOT_STICKY

        when (action) {
            "schedule" -> scheduleNotification(task)
            "cancel" -> cancelNotifications(task)
            "update" -> updateNotifications(task)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun cancelNotifications(task: Task) {
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.id.toInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        Log.i("Remind", "Cancelling ${task.title} notifications")
    }

    private fun scheduleNotification(task: Task) {
        if (!task.isNotify) {
            return
        }

        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("task_id", task.id)
        val pendingIntent = PendingIntent.getBroadcast(this,
            task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val deadlineString = task.deadline
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val deadlineDate = formatter.parse(deadlineString)
        val sharedPreferences = getSharedPreferences(getString(R.string.config_file), MODE_PRIVATE)
        val remindTime = sharedPreferences.getInt("remind_time", 15)
        val triggerTime: Long = deadlineDate!!.time - (remindTime * 60 * 1000).toLong()

        if (triggerTime > System.currentTimeMillis()) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

            Log.i("Remind", "Scheduling ${task.title} notifications")
        }
        else {
            Log.i("Remind", "Ignoring ${task.title} notifications")
        }

//        Log.i("Remind time comparison", "$triggerTime vs ${System.currentTimeMillis()}")
    }

    private fun updateNotifications(task: Task) {
        Log.i("RemindUpdate", "Updating ${task.title}")
        cancelNotifications(task)
        scheduleNotification(task)
    }
}

