package org.solovyev.android.calculator

import org.solovyev.android.Check

class Runnables : Runnable {
    private val list = mutableListOf<Runnable>()

    override fun run() {
        Check.isMainThread()
        list.forEach { it.run() }
        list.clear()
    }

    fun add(runnable: Runnable) {
        Check.isMainThread()
        list.add(runnable)
    }
}
