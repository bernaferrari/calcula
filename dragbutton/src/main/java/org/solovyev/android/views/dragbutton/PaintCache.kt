package org.solovyev.android.views.dragbutton

import android.content.Context
import android.graphics.Color.BLACK
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import org.solovyev.android.views.dragbutton.DirectionTextView.Companion.SHADOW_RADIUS_DPS

internal class PaintCache private constructor() {

    private var shadowRadius: Float = 0f
    private val map = HashMap<Spec, Entry>()

    class Entry(
        val spec: Spec,
        val paint: Paint
    ) {
        private var lastTextSize: Float = 0f
        private var fixedTextHeight: Float = 0f

        fun getFixedTextHeight(textSize: Float): Float {
            if (lastTextSize == textSize) {
                return fixedTextHeight
            }
            if (lastTextSize != 0f) {
                Log.d(TAG, "Remeasuring text for size: $textSize")
            }
            val oldTextSize = paint.textSize
            paint.textSize = textSize
            TMP.setEmpty()
            paint.getTextBounds("|", 0, 1, TMP)
            paint.textSize = oldTextSize
            lastTextSize = textSize
            fixedTextHeight = TMP.height().toFloat()
            return fixedTextHeight
        }
    }

    data class Spec(
        @ColorInt val color: Int,
        val alpha: Float,
        val typeface: Typeface,
        val textSize: Float,
        val highContrast: Boolean
    ) {
        internal fun contrastColor(context: Context): Int {
            val colorRes = if (isLightColor(color)) {
                R.color.drag_button_text
            } else {
                R.color.drag_text_inverse
            }
            return ContextCompat.getColor(context, colorRes)
        }

        internal fun intAlpha(): Int = (255 * alpha).toInt()

        private fun needsShadow(): Boolean = needsShadow(color)

        fun highContrast(highContrast: Boolean): Spec {
            return copy(highContrast = highContrast)
        }

        fun color(color: Int, alpha: Float): Spec {
            return copy(color = color, alpha = alpha)
        }

        fun typeface(typeface: Typeface): Spec {
            return copy(typeface = typeface)
        }

        fun textSize(textSize: Float): Spec {
            return copy(textSize = textSize)
        }

        companion object {
            @JvmStatic
            fun isLightColor(@ColorInt color: Int): Boolean {
                return ColorUtils.calculateLuminance(color) > 0.5f
            }

            @JvmStatic
            fun needsShadow(@ColorInt color: Int): Boolean {
                return isLightColor(color)
            }
        }
    }

    private fun lazyLoad(context: Context) {
        if (shadowRadius != 0f) {
            return
        }
        val res = context.resources
        shadowRadius = applyDimension(COMPLEX_UNIT_DIP, SHADOW_RADIUS_DPS, res.displayMetrics)
    }

    fun get(context: Context, spec: Spec): Entry {
        lazyLoad(context)
        var entry = map[spec]
        if (entry == null) {
            entry = Entry(spec, makePaint(context, spec))
            map[spec] = entry
        }
        return entry
    }

    private fun makePaint(context: Context, spec: Spec): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        if (spec.highContrast) {
            paint.color = spec.contrastColor(context)
            paint.alpha = 255
        } else {
            paint.color = spec.color
            paint.alpha = spec.intAlpha()
        }
        if (spec.typeface.style != Typeface.NORMAL) {
            paint.typeface = Typeface.create(spec.typeface, Typeface.NORMAL)
        } else {
            paint.typeface = spec.typeface
        }
        paint.textSize = spec.textSize
        setHighContrast(paint, spec.highContrast, spec.color)
        return paint
    }

    companion object {
        private const val TAG = "PaintCache"
        private val TMP = Rect()
        private val INSTANCE = PaintCache()

        @JvmStatic
        fun get(): PaintCache = INSTANCE

        @JvmStatic
        fun setHighContrast(paint: Paint, highContrast: Boolean, @ColorInt color: Int) {
            if (highContrast && Spec.needsShadow(color)) {
                INSTANCE.shadowRadius.let { radius ->
                    paint.setShadowLayer(radius, 0f, 0f, BLACK)
                }
            } else {
                paint.clearShadowLayer()
            }
        }
    }
}
