package com.m4sak1.taskapp.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.m4sak1.taskapp.R
import com.m4sak1.taskapp.data.PreferenceManager

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefManager = PreferenceManager(applicationContext)
        if (!prefManager.notificationsEnabled) {
            return Result.success()
        }

        val taskTitle = inputData.getString("task_title") ?: return Result.failure()
        val hour = inputData.getInt("notification_hour", 24)
        
        val arrayResId = when (hour) {
            48 -> R.array.notification_messages_48h
            72 -> R.array.notification_messages_72h
            else -> R.array.notification_messages_24h
        }
        
        val messages = applicationContext.resources.getStringArray(arrayResId)
        val randomMessage = messages.random()
        val parts = randomMessage.split("|")
        
        val notificationTitle = parts.getOrNull(0) ?: applicationContext.getString(R.string.notification_reminder_title)
        val rawBody = parts.getOrNull(1) ?: applicationContext.getString(R.string.notification_reminder_body)
        val notificationBody = String.format(rawBody, taskTitle)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = android.content.Intent(applicationContext, com.m4sak1.taskapp.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "task_reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}
