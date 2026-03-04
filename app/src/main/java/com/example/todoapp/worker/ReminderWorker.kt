package com.example.todoapp.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.todoapp.util.NotificationHelper

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Task Reminder"
        val message = inputData.getString("message") ?: "You have a task due soon!"
        val taskId = inputData.getString("taskId") ?: "0"

        NotificationHelper(applicationContext).showNotification(title, message, taskId)
        
        return Result.success()
    }
}
