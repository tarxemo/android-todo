package com.example.todoapp.ui.tasks

import androidx.lifecycle.*
import com.example.todoapp.data.model.Task
import com.example.todoapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import androidx.work.*
import com.example.todoapp.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class TaskViewModel(
    private val repository: TaskRepository,
    private val application: android.app.Application
) : ViewModel() {

    private val workManager = WorkManager.getInstance(application)

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryStats: StateFlow<Map<String, Pair<Int, Int>>> = repository.allTasks
        .map { tasks ->
            tasks.groupBy { it.category }.mapValues { entry ->
                val groupTasks = entry.value
                val groupCompleted = groupTasks.count { it.isCompleted }
                Pair(groupCompleted, groupTasks.size)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val productivityHistory: StateFlow<List<Int>> = repository.allTasks
        .map { tasks ->
            val now = System.currentTimeMillis()
            val last7Days = (0L..6L).map { day ->
                val startOfDay = getStartOfDay(now - day * 24 * 60 * 60 * 1000L)
                val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
                tasks.count { it.isCompleted && it.completedAt != null && it.completedAt!! in startOfDay until endOfDay }
            }.reversed()
            last7Days
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getStartOfDay(millis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val completionStats: StateFlow<Pair<Int, Int>> = combine(
        repository.completedCount,
        repository.totalCount
    ) { completed, total ->
        Pair(completed, total)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    fun addTask(task: Task) = viewModelScope.launch {
        repository.insert(task)
        scheduleReminder(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.update(task)
        scheduleReminder(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.delete(task)
        cancelReminder(task)
    }

    fun toggleTaskCompletion(task: Task) = viewModelScope.launch {
        val nextCompleted = !task.isCompleted
        val updatedTask = task.copy(
            isCompleted = nextCompleted,
            completedAt = if (nextCompleted) System.currentTimeMillis() else null
        )
        repository.update(updatedTask)
        if (updatedTask.isCompleted) cancelReminder(updatedTask) else scheduleReminder(updatedTask)
    }

    private fun scheduleReminder(task: Task) {
        if (task.dueDate == null || task.isCompleted) return
        
        val delay = task.dueDate - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString("title", "Deadline Approaching")
            .putString("message", task.title)
            .putString("taskId", task.id)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(task.id)
            .build()

        workManager.enqueueUniqueWork(
            task.id,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun cancelReminder(task: Task) {
        workManager.cancelUniqueWork(task.id)
    }
}

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
