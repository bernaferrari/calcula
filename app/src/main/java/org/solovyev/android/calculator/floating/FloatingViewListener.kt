package org.solovyev.android.calculator.floating

interface FloatingViewListener {
    // view minimized == view is in the action bar
    fun onViewMinimized()

    // view hidden == view closed
    fun onViewHidden()
}
