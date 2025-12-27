package org.solovyev.android.calculator.floating

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FloatingCalculatorBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val newIntent = Intent(intent).apply {
            setClass(context, FloatingCalculatorService::class.java)
        }
        context.startService(newIntent)
    }
}
