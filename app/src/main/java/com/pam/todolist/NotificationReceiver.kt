package com.pam.todolist

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        val serviceIntent = Intent(context, NotificationService::class.java)
//        serviceIntent.putExtra("task", task)
//        ContextCompat.startForegroundService(context!!, serviceIntent)

        var taskId: Long? = intent?.getLongExtra("task_id", -1) as Long? ?: return
        if (taskId!! < 0) {
            throw java.lang.Exception("Invalid id")
        }
        var task : Task? = MainActivity.getDbHandler().getTask(taskId) ?: return

        Log.i("Notification", "Running onReceive")

        val detailsIntent = Intent(context!!, TaskDetailsActivity::class.java)
        detailsIntent.putExtra("task", task)
        detailsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, taskId.toInt(), detailsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.emoji)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_notifications_alerted)
            .setLargeIcon(largeIcon)
            .setContentTitle(task!!.title)
            .setContentText(task.description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (context.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Notification", "Invalid context or no permission")
            return
        }
        context.let {
            NotificationManagerCompat.from(
                it
            )
        }.notify(1, builder.build())
    }

}
