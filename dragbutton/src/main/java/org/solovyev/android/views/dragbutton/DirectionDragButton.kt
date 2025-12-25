package org.solovyev.android.views.dragbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.ColorInt

class DirectionDragButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DragButton(context, attrs, defStyleAttr), DirectionDragView {

    private val textView = DirectionTextView()

    init {
        textView.init(this, attrs)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textView.draw(canvas)
    }

    fun getTextValue(direction: DragDirection): String {
        return getText(direction).value
    }

    fun setText(direction: DragDirection, value: String): DirectionDragButton {
        getText(direction).value = value
        return this
    }

    override fun setTypeface(tf: Typeface?, style: Int) {
        super.setTypeface(tf, style)
        // might be called from constructor
        textView.typeface = paint.typeface
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        // might be called from constructor
        textView.textSize = paint.textSize
    }

    override fun getText(direction: DragDirection): DirectionText {
        return textView.getText(direction)
    }

    fun setShowDirectionText(direction: DragDirection, show: Boolean) {
        getText(direction).isVisible = show
    }

    fun setDirectionTextColor(@ColorInt color: Int) {
        for (direction in DragDirection.values()) {
            getText(direction).setColor(color)
        }
    }

    fun setDirectionTextAlpha(alpha: Float) {
        for (direction in DragDirection.values()) {
            getText(direction).alpha = alpha
        }
    }

    override fun setHighContrast(highContrast: Boolean) {
        textView.setHighContrast(highContrast)
        PaintCache.setHighContrast(paint, highContrast, textColors.defaultColor)
    }
}
