package org.solovyev.android.calculator.widgets

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return runBlocking {
            runCatching {
                WidgetUpdates.updateAll(applicationContext)
            }.fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
        }
    }

    companion object {
        private const val WIDGET_UPDATE_WORK_NAME = "calculator_widget_updates"
        private const val SMART_STACK_UPDATE_WORK_NAME = "calculator_smart_stack_updates"
        private const val IMMEDIATE_WIDGET_UPDATE_WORK_NAME = "calculator_immediate_widget_update"

        fun scheduleUpdates(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES)
                .setConstraints(defaultConstraints())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WIDGET_UPDATE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
        }

        fun cancelUpdates(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
        }

        fun scheduleSmartStackUpdates(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(defaultConstraints())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    SMART_STACK_UPDATE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
        }

        fun cancelSmartStackUpdates(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SMART_STACK_UPDATE_WORK_NAME)
        }

        fun triggerImmediateUpdate(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setConstraints(defaultConstraints())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    IMMEDIATE_WIDGET_UPDATE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        private fun defaultConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        }
    }
}
