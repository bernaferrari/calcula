package org.solovyev.android.views

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat

object Adjuster {

    private val MATRIX = FloatArray(9)

    private val textViewHelper = object : Helper<TextView> {
        override fun apply(view: TextView, textSize: Float) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        }

        override fun getTextSize(view: TextView): Float {
            return view.textSize
        }
    }

    @JvmStatic
    fun adjustText(view: TextView, percentage: Float) {
        adjustText(view, textViewHelper, percentage, 0f)
    }

    @JvmStatic
    fun adjustText(view: TextView, percentage: Float, minTextSizePxs: Int) {
        adjustText(view, textViewHelper, percentage, minTextSizePxs.toFloat())
    }

    @JvmStatic
    fun <V : View> adjustText(
        view: V,
        helper: Helper<V>,
        percentage: Float,
        minTextSizePxs: Float
    ) {
        val treeObserver = getTreeObserver(view) ?: return
        treeObserver.addOnPreDrawListener(TextViewAdjuster(view, helper, percentage, minTextSizePxs))
    }

    @JvmStatic
    fun getTreeObserver(view: View): ViewTreeObserver? {
        val treeObserver = view.viewTreeObserver
        if (!treeObserver.isAlive) {
            return null
        }
        return treeObserver
    }

    @JvmStatic
    fun adjustImage(view: ImageView, percentage: Float) {
        val treeObserver = getTreeObserver(view) ?: return
        treeObserver.addOnPreDrawListener(ImageViewAdjuster(view, percentage))
    }

    @JvmStatic
    fun maxWidth(view: View, maxWidth: Int) {
        val treeObserver = getTreeObserver(view) ?: return
        treeObserver.addOnPreDrawListener(MaxWidthAdjuster(view, maxWidth))
    }

    interface Helper<V : View> {
        fun apply(view: V, textSize: Float)
        fun getTextSize(view: V): Float
    }

    private abstract class BaseViewAdjuster<V : View>(
        protected val view: V,
        protected val oneShot: Boolean
    ) : ViewTreeObserver.OnPreDrawListener {

        private var usedWidth: Int = 0
        private var usedHeight: Int = 0

        final override fun onPreDraw(): Boolean {
            val width = view.width
            val height = view.height
            if (!ViewCompat.isLaidOut(view) || height <= 0 || width <= 0) {
                return true
            }
            if (usedWidth == width && usedHeight == height) {
                return true
            }
            usedWidth = width
            usedHeight = height
            if (oneShot) {
                getTreeObserver(view)?.removeOnPreDrawListener(this)
            }
            return adjust(width, height)
        }

        protected abstract fun adjust(width: Int, height: Int): Boolean
    }

    private class TextViewAdjuster<V : View>(
        view: V,
        private val helper: Helper<V>,
        private val percentage: Float,
        private val minTextSizePxs: Float
    ) : BaseViewAdjuster<V>(view, true) {

        override fun adjust(width: Int, height: Int): Boolean {
            val oldTextSize = helper.getTextSize(view).toInt().toFloat()
            val newTextSize = maxOf(minTextSizePxs, (height * percentage).toInt().toFloat())
            if (oldTextSize == newTextSize) {
                return true
            }
            helper.apply(view, newTextSize)
            return false
        }
    }

    private class MaxWidthAdjuster(
        view: View,
        private val maxWidth: Int
    ) : BaseViewAdjuster<View>(view, true) {

        override fun adjust(width: Int, height: Int): Boolean {
            if (width <= maxWidth) {
                return true
            }
            val lp = view.layoutParams
            lp.width = maxWidth
            view.layoutParams = lp
            return false
        }
    }

    private class ImageViewAdjuster(
        view: ImageView,
        private val percentage: Float
    ) : BaseViewAdjuster<ImageView>(view, false) {

        override fun adjust(width: Int, height: Int): Boolean {
            val d = view.drawable ?: return true
            view.imageMatrix.getValues(MATRIX)
            val oldImageHeight = (d.intrinsicHeight * MATRIX[Matrix.MSCALE_Y]).toInt()
            val newImageHeight = (height * percentage).toInt()
            if (oldImageHeight == newImageHeight) {
                return true
            }
            val newPaddings = maxOf(0, height - newImageHeight) / 2
            view.setPadding(0, newPaddings, 0, newPaddings)
            return false
        }
    }
}
