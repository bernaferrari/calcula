package org.solovyev.android.calculator.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PagerViewFabBehavior : FloatingActionButton.Behavior {

    private val visibilityListener = object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton) {
            // by default, FloatingActionButton#hide causes FAB to be GONE which blocks any
            // consequent scroll updates in CoordinatorLayout#onNestedScroll. Let's make the
            // FAB invisible instead
            fab.visibility = View.INVISIBLE
        }
    }

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super()

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        if (!child.isClickable) {
            return false
        }
        return when (axes) {
            ViewCompat.SCROLL_AXIS_HORIZONTAL -> target is ViewPager
            ViewCompat.SCROLL_AXIS_VERTICAL -> target is RecyclerView
            else -> false
        }
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        onScroll(child, dxConsumed.toFloat(), dyConsumed.toFloat())
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return onScroll(child, velocityX, velocityY)
    }

    private fun onScroll(child: FloatingActionButton, scrollX: Float, scrollY: Float): Boolean {
        return when {
            scrollY > 0 && child.visibility == View.VISIBLE -> {
                child.hide(visibilityListener)
                true
            }
            scrollY < 0 && child.visibility != View.VISIBLE -> {
                child.show(visibilityListener)
                true
            }
            scrollX != 0f && child.visibility != View.VISIBLE -> {
                child.show(visibilityListener)
                true
            }
            else -> false
        }
    }
}
