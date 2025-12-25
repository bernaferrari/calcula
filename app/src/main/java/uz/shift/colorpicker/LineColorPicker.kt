package uz.shift.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.solovyev.android.calculator.R

class LineColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var colors: IntArray = if (isInEditMode) Palette.DEFAULT else intArrayOf(0)
        set(value) {
            field = value
            if (!containsColor(value, selectedColor)) {
                selectedColor = value[0]
            }
            recalcCellSize()
            invalidate()
        }

    var isColorSelected = false
        private set

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val rect = Rect()
    private var selectedColor = colors[0]
    private var onColorChanged: OnColorChangedListener? = null
    private var cellSize: Int = 0
    private var orientation = HORIZONTAL
    private var isClick = false
    private var screenW: Int = 0
    private var screenH: Int = 0

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.LineColorPicker, 0, 0)

        try {
            orientation = a.getInteger(R.styleable.LineColorPicker_lcp_orientation, HORIZONTAL)

            if (!isInEditMode) {
                val colorsArrayResId = a.getResourceId(R.styleable.LineColorPicker_lcp_colors, -1)
                if (colorsArrayResId > 0) {
                    colors = context.resources.getIntArray(colorsArrayResId)
                }
            }

            val selected = a.getInteger(R.styleable.LineColorPicker_lcp_selectedColorIndex, -1)
            if (selected != -1 && selected < colors.size) {
                setSelectedColorPosition(selected)
            }
        } finally {
            a.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (orientation == HORIZONTAL) {
            drawHorizontalPicker(canvas)
        } else {
            drawVerticalPicker(canvas)
        }
    }

    private fun drawVerticalPicker(canvas: Canvas) {
        rect.left = 0
        rect.top = 0
        rect.right = width
        rect.bottom = 0

        // 8%
        val margin = (width * 0.08f).toInt()

        for (i in colors.indices) {
            paint.color = colors[i]

            rect.top = rect.bottom
            rect.bottom += cellSize

            if (isColorSelected && colors[i] == selectedColor) {
                rect.left = 0
                rect.right = width
            } else {
                rect.left = margin
                rect.right = width - margin
            }

            canvas.drawRect(rect, paint)
        }
    }

    private fun drawHorizontalPicker(canvas: Canvas) {
        rect.left = 0
        rect.top = 0
        rect.right = 0
        rect.bottom = height

        // 8%
        val margin = (height * 0.08f).toInt()

        for (i in colors.indices) {
            paint.color = colors[i]

            rect.left = rect.right
            rect.right += cellSize

            if (isColorSelected && colors[i] == selectedColor) {
                rect.top = 0
                rect.bottom = height
            } else {
                rect.top = margin
                rect.bottom = height - margin
            }

            canvas.drawRect(rect, paint)
        }
    }

    private fun notifyColorChanged(color: Int) {
        onColorChanged?.onColorChanged(color)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val newColor: Int

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isClick = true
            }
            MotionEvent.ACTION_UP -> {
                newColor = getColorAtXY(event.x, event.y)
                setSelectedColor(newColor)
                if (isClick) {
                    performClick()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                newColor = getColorAtXY(event.x, event.y)
                setSelectedColor(newColor)
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_OUTSIDE -> {
                isClick = false
            }
        }

        return true
    }

    /**
     * Return color at x,y coordinate of view.
     */
    private fun getColorAtXY(x: Float, y: Float): Int {
        if (orientation == HORIZONTAL) {
            var left = 0
            var right = 0

            for (i in colors.indices) {
                left = right
                right += cellSize

                if (left <= x && right >= x) {
                    return colors[i]
                }
            }
        } else {
            var top = 0
            var bottom = 0

            for (i in colors.indices) {
                top = bottom
                bottom += cellSize

                if (y >= top && y <= bottom) {
                    return colors[i]
                }
            }
        }

        return selectedColor
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            selectedColor = this@LineColorPicker.selectedColor
            isColorSelected = this@LineColorPicker.isColorSelected
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        selectedColor = state.selectedColor
        isColorSelected = state.isColorSelected
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        screenW = w
        screenH = h
        recalcCellSize()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    /**
     * Return currently selected color.
     */
    fun getColor(): Int = selectedColor

    /**
     * Set selected color as color value from palette.
     */
    fun setSelectedColor(color: Int) {
        // not from current palette
        if (!containsColor(colors, color)) {
            return
        }

        // do we need to re-draw view?
        if (!isColorSelected || selectedColor != color) {
            selectedColor = color
            isColorSelected = true
            invalidate()
            notifyColorChanged(color)
        }
    }

    /**
     * Set selected color as index from palette
     */
    fun setSelectedColorPosition(position: Int) {
        setSelectedColor(colors[position])
    }

    private fun recalcCellSize(): Int {
        cellSize = if (orientation == HORIZONTAL) {
            (screenW / colors.size.toFloat()).toInt()
        } else {
            (screenH / colors.size.toFloat()).toInt()
        }
        return cellSize
    }

    /**
     * Return true if palette contains this color
     */
    private fun containsColor(colors: IntArray, c: Int): Boolean {
        return colors.any { it == c }
    }

    /**
     * Set onColorChanged listener
     */
    fun setOnColorChangedListener(listener: OnColorChangedListener) {
        onColorChanged = listener
    }

    private class SavedState : BaseSavedState {
        var selectedColor: Int = 0
        var isColorSelected: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            selectedColor = parcel.readInt()
            isColorSelected = parcel.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedColor)
            out.writeInt(if (isColorSelected) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
    }
}
