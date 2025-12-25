package org.solovyev.android.views.dragbutton

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import java.util.EnumMap

internal class DirectionTextView {

    private val texts: MutableMap<DragDirection, DirectionText> = EnumMap(DragDirection::class.java)
    var textSize: Float = 0f
        set(value) {
            if (field == value) return
            field = value
            for (text in texts.values) {
                text.baseTextSize = value
            }
        }

    var typeface: Typeface = Typeface.DEFAULT
        set(value) {
            if (field == value) return
            field = value
            for (text in texts.values) {
                text.typeface = value
            }
        }

    fun init(view: TextView, attrs: AttributeSet?) {
        init(view, attrs, view.paint)
    }

    fun init(view: View, attrs: AttributeSet?, base: TextPaint) {
        textSize = base.textSize
        typeface = base.typeface ?: Typeface.DEFAULT

        val context = view.context
        val res = context.resources

        val minTextSize = res.getDimensionPixelSize(R.dimen.drag_direction_text_min_size).toFloat()
        val a = context.obtainStyledAttributes(attrs, R.styleable.DirectionText)
        val scale = a.getFloat(R.styleable.DirectionText_directionTextScale, DirectionText.DEF_SCALE)
        val alpha = a.getFloat(R.styleable.DirectionText_directionTextAlpha, DEF_ALPHA)
        val color = a.getColor(R.styleable.DirectionText_directionTextColor, base.color)
        val padding = a.getDimensionPixelSize(
            R.styleable.DirectionText_directionTextPadding,
            res.getDimensionPixelSize(R.dimen.drag_direction_text_default_padding)
        )

        for (direction in DragDirection.values()) {
            val text = DirectionText(direction, view, minTextSize)
            text.init(a, scale, color, alpha, padding, typeface, textSize)
            texts[direction] = text
        }
        a.recycle()
    }

    fun draw(canvas: Canvas) {
        for (text in texts.values) {
            text.draw(canvas)
        }
    }

    fun getText(direction: DragDirection): DirectionText {
        return texts[direction]!!
    }

    fun setHighContrast(highContrast: Boolean) {
        for (text in texts.values) {
            text.setHighContrast(highContrast)
        }
    }

    companion object {
        const val SHADOW_RADIUS_DPS = 2f
        private const val DEF_ALPHA = 0.4f
    }
}
