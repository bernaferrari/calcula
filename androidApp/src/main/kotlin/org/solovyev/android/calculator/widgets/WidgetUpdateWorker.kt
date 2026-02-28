package org.solovyev.android.calculator.widgets

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Stub worker for widget updates - functionality disabled for build.
 * Widget implementations removed to fix build errors.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        // Widget updates disabled - stub implementation
        return Result.success()
    }

    companion object {
        fun scheduleUpdates(context: Context) {
            // No-op - widgets disabled
        }

        fun cancelUpdates(context: Context) {
            // No-op - widgets disabled
        }

        fun scheduleSmartStackUpdates(context: Context) {
            // No-op - widgets disabled
        }

        fun cancelSmartStackUpdates(context: Context) {
            // No-op - widgets disabled
        }

        fun triggerImmediateUpdate(context: Context) {
            // No-op - widgets disabled
        }
    }
}
