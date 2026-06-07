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
        
        // Ensure context has the correct locale based on user settings
        // Actually, we'll just use the applicationContext which should be updated by our AppLanguage setting
        // or just rely on string resources which use the current context's locale.
        
        val notificationTitle = applicationContext.getString(R.string.notification_reminder_title)
        val notificationBody = applicationContext.getString(R.string.notification_reminder_body, taskTitle)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "task_reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }
}
