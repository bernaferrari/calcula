package org.solovyev.android.views.dragbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView

class DirectionDragImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DragImageButton(context, attrs, defStyleAttr), DirectionDragView {

    private val textView = DirectionTextView()

    init {
        val view = TextView(context, attrs)
        textView.init(this, attrs, view.paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textView.draw(canvas)
    }

    override fun getText(direction: DragDirection): DirectionText {
        return textView.getText(direction)
    }

    fun setTypeface(typeface: Typeface) {
        textView.typeface = typeface
    }

    fun setTextSize(textSize: Float) {
        textView.textSize = textSize
    }

    fun getTextSize(): Float {
        return textView.textSize
    }

    override fun setHighContrast(highContrast: Boolean) {
        textView.setHighContrast(highContrast)
    }
}
