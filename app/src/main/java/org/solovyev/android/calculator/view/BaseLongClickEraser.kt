package org.solovyev.android.calculator.view

import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

abstract class BaseLongClickEraser(
    private val view: View,
    var vibrateOnKeypress: Boolean
) : View.OnTouchListener {

    private val eraser = Eraser()
    private val gestureDetector = GestureDetector(view.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            if (eraser.isTracking) {
                eraser.start()
            }
        }
    })

    companion object {
        private const val DELAY = 300L
    }

    init {
        view.setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> eraser.stopTracking()
            else -> {
                eraser.startTracking()
                gestureDetector.onTouchEvent(event)
            }
        }
        return false
    }

    protected abstract fun onStopErase()
    protected abstract fun onStartErase()
    protected abstract fun erase(): Boolean

    private inner class Eraser : Runnable {
        private var delay: Long = 0
        private var erasing = false
        var isTracking = true
            private set

        override fun run() {
            if (!erase()) {
                stop()
                return
            }
            delay = maxOf(50, 2 * delay / 3)
            view.postDelayed(this, delay)
        }

        fun start() {
            if (erasing) {
                stop()
            }
            erasing = true
            delay = DELAY
            view.removeCallbacks(this)
            if (vibrateOnKeypress) {
                view.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or
                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            }
            onStartErase()
            run()
        }

        fun stop() {
            view.removeCallbacks(this)
            if (!erasing) {
                return
            }

            erasing = false
            onStopErase()
        }

        fun stopTracking() {
            stop()
            isTracking = false
        }

        fun startTracking() {
            isTracking = true
        }
    }
}
