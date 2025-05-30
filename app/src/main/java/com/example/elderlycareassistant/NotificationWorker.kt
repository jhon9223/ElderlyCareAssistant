package com.example.elderlycareassistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val medicationName = inputData.getString("medicationName") ?: "Medication"
        val medicationTime = inputData.getString("medicationTime") ?: ":NOW!!!(1 pill) "

        showNotification(medicationName, medicationTime)
        return Result.success()
    }

    private fun showNotification(medicationName: String, medicationTime: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "medication_channel",
                "Medication Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "medication_channel")
            .setSmallIcon(R.drawable.medicine)
            .setContentTitle("Medication Reminder: $medicationName")
            .setContentText("Take at $medicationTime")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}