package org.solovyev.android.calculator

import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.*
import org.solovyev.android.Check
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

internal class TaskExecutor {

    private data class Task(
        val runnable: Runnable,
        val cancellable: Boolean,
        val job: Job
    )

    private val tasks = ConcurrentHashMap<Int, Task>()
    private val taskIdCounter = AtomicInteger(0)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var synchronous = false

    fun execute(runnable: Runnable, cancellable: Boolean) {
        Check.isMainThread()

        if (synchronous) {
            runnable.run()
            return
        }

        synchronized(tasks) {
            if (tasks.size >= MAX_TASKS) {
                val taskToCancel = tasks.entries.firstOrNull { it.value.cancellable }
                if (taskToCancel != null) {
                    tasks.remove(taskToCancel.key)
                    taskToCancel.value.job.cancel()
                    Log.d(TAG, "Task cancelled: ${taskToCancel.key}")
                }
            }
        }

        val taskId = taskIdCounter.getAndIncrement()
        val job = scope.launch {
            try {
                Log.d(TAG, "Running task: $taskId on ${Thread.currentThread().name}")
                runnable.run()
            } finally {
                onTaskFinished(taskId)
            }
        }

        onTaskStarted(taskId, Task(runnable, cancellable, job))
    }

    private fun onTaskStarted(taskId: Int, task: Task) {
        if (!task.job.isCompleted) {
            Log.d(TAG, "Task added: $taskId")
            tasks[taskId] = task
        }
    }

    private fun onTaskFinished(taskId: Int) {
        Log.d(TAG, "Task removed: $taskId")
        tasks.remove(taskId)
    }

    @VisibleForTesting
    fun setSynchronous() {
        synchronous = true
    }

    fun shutdown() {
        scope.cancel()
    }

    companion object {
        private const val MAX_TASKS = 5
        private const val TAG = "TaskExecutor"
    }
}
