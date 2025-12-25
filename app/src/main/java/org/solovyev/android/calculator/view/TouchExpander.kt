package org.solovyev.android.calculator.view

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.Px
import androidx.core.view.ViewCompat

class TouchExpander private constructor(
    private val view: View,
    private val extra: Rect
) : ViewTreeObserver.OnGlobalLayoutListener {

    private var parent: View? = null

    init {
        attach()
    }

    private fun attach() {
        if (ViewCompat.isLaidOut(view)) {
            onLaidOut()
            return
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        if (!ViewCompat.isLaidOut(view)) return
        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
        onLaidOut()
    }

    private fun onLaidOut() {
        check(ViewCompat.isLaidOut(view)) { "View is not laid out" }

        val hitRect = Rect()
        view.getHitRect(hitRect)
        hitRect.outset(extra)

        parent = (view.parent as View).also {
            it.touchDelegate = TouchDelegate(hitRect, view)
        }
    }

    fun detach() {
        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
        parent?.let {
            it.touchDelegate = null
            parent = null
        }
    }

    companion object {
        @JvmStatic
        fun attach(view: View, @Px extra: Int): TouchExpander =
            TouchExpander(view, Rect(extra, extra, extra, extra))

        @JvmStatic
        fun attach(view: View, extra: Rect): TouchExpander =
            TouchExpander(view, extra)

        private fun Rect.outset(diff: Rect) {
            left -= diff.left
            top -= diff.top
            right += diff.right
            bottom += diff.bottom
        }
    }
}
