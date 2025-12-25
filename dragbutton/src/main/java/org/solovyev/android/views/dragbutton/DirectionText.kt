package org.solovyev.android.views.dragbutton

import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextUtils
import android.view.View

class DirectionText internal constructor(
    private val direction: DragDirection,
    private val view: View,
    private val minTextSize: Float
) {
    private val offset = PointF(Integer.MIN_VALUE.toFloat(), Integer.MIN_VALUE.toFloat())
    private val paintCache: PaintCache = PaintCache.get()
    private var entry: PaintCache.Entry

    var value: String = ""
        set(value) {
            if (TextUtils.equals(field, value)) return
            field = value
            invalidate(true)
        }

    var isVisible: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            invalidate(false)
        }

    private var padding: Int = 0
    private var scale: Float = 0f
    var baseTextSize: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            entry = paintCache.get(view.context, entry.spec.textSize(scaledTextSize(value, scale)))
            invalidate(true)
        }

    init {
        // Initialize with a default spec, will be properly initialized in init()
        val defaultSpec = PaintCache.Spec(
            color = 0,
            alpha = 1f,
            typeface = Typeface.DEFAULT,
            textSize = minTextSize,
            highContrast = false
        )
        entry = paintCache.get(view.context, defaultSpec)
    }

    fun init(
        array: TypedArray?,
        defScale: Float,
        defColor: Int,
        defAlpha: Float,
        defPadding: Int,
        defTypeface: Typeface,
        textSize: Float
    ) {
        baseTextSize = textSize
        if (array != null) {
            if (array.hasValue(direction.textAttr)) {
                value = nullToEmpty(array.getString(direction.textAttr))
            }
            padding = array.getDimensionPixelSize(direction.paddingAttr, defPadding)
            scale = array.getFloat(direction.scaleAttr, defScale)
        } else {
            value = ""
            scale = defScale
            padding = defPadding
        }
        val spec = PaintCache.Spec(
            color = defColor,
            alpha = defAlpha,
            typeface = defTypeface,
            textSize = scaledTextSize(textSize, scale),
            highContrast = false
        )
        entry = paintCache.get(view.context, spec)
    }

    private fun nullToEmpty(s: String?): String = s ?: ""

    private fun scaledTextSize(textSize: Float, scale: Float): Float {
        return maxOf(textSize * scale, minTextSize)
    }

    private fun invalidate(remeasure: Boolean) {
        view.invalidate()
        if (remeasure) {
            offset.set(Integer.MIN_VALUE.toFloat(), Integer.MIN_VALUE.toFloat())
        }
    }

    fun setColor(color: Int) {
        setColor(color, entry.spec.alpha)
    }

    private fun setColor(color: Int, alpha: Float) {
        if (entry.spec.color == color && entry.spec.alpha == alpha) {
            return
        }
        entry = paintCache.get(view.context, entry.spec.color(color, alpha))
        invalidate(false)
    }

    var alpha: Float
        get() = entry.spec.alpha
        set(value) {
            setColor(entry.spec.color, value)
        }

    fun setHighContrast(highContrast: Boolean) {
        if (entry.spec.highContrast == highContrast) {
            return
        }
        entry = paintCache.get(view.context, entry.spec.highContrast(highContrast))
        invalidate(false)
    }

    var typeface: Typeface
        get() = entry.spec.typeface
        set(value) {
            if (entry.spec.typeface == value) return
            entry = paintCache.get(view.context, entry.spec.typeface(value))
            invalidate(true)
        }

    fun draw(canvas: Canvas) {
        if (!hasValue()) {
            return
        }
        if (offset.x == Integer.MIN_VALUE.toFloat() || offset.y == Integer.MIN_VALUE.toFloat()) {
            calculatePosition()
        }
        val width = view.width
        val height = view.height
        when (direction) {
            DragDirection.up -> canvas.drawText(value, width + offset.x, offset.y, entry.paint)
            DragDirection.down -> canvas.drawText(value, width + offset.x, height + offset.y, entry.paint)
            DragDirection.left -> canvas.drawText(value, offset.x, height / 2f + offset.y, entry.paint)
            DragDirection.right -> canvas.drawText(value, width + offset.x, height / 2f + offset.y, entry.paint)
        }
    }

    fun hasValue(): Boolean {
        return isVisible && !TextUtils.isEmpty(value)
    }

    private fun calculatePosition() {
        TMP.setEmpty()
        entry.paint.getTextBounds(value, 0, value.length, TMP)

        val paddingLeft = padding
        val paddingRight = padding
        val paddingTop = padding
        val paddingBottom = padding

        when (direction) {
            DragDirection.up, DragDirection.down -> {
                offset.x = (-paddingLeft - TMP.width() - TMP.left).toFloat()
                offset.y = if (direction == DragDirection.up) {
                    (paddingTop + entry.getFixedTextHeight(scaledTextSize(baseTextSize, DEF_SCALE)))
                } else {
                    -paddingBottom.toFloat()
                }
            }
            DragDirection.left, DragDirection.right -> {
                offset.x = if (direction == DragDirection.left) {
                    paddingLeft.toFloat()
                } else {
                    (-paddingRight - TMP.width()).toFloat()
                }
                offset.y = ((paddingTop - paddingBottom) / 2 +
                    entry.getFixedTextHeight(scaledTextSize(baseTextSize, DEF_SCALE)) / 2)
            }
        }
    }

    companion object {
        const val DEF_SCALE = 0.4f
        private val TMP = Rect()
    }
}
