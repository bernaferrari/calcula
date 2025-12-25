package org.solovyev.android.views

/*
 * DO WHAT YOU WANT TO PUBLIC LICENSE
 * Version 2, December 2004
 *
 * Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
 *
 * Everyone is permitted to copy and distribute verbatim or modified
 * copies of this license document, and changing it is allowed as long
 * as the name is changed.
 *
 * DO WHAT YOU WANT TO PUBLIC LICENSE
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *
 * 0. You just DO WHAT YOU WANT TO.
 */

import android.annotation.SuppressLint
import android.content.Context
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TimingLogger
import android.util.TypedValue
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Text view that auto adjusts text size to fit within the view.
 * If the text size equals the minimum text size and still does not
 * fit, append with an ellipsis.
 *
 * @author Chase Colburn
 * @since Apr 4, 2011
 */
open class AutoResizeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    // Registered resize listener
    private var textResizeListener: OnTextResizeListener? = null

    // Flag for text and/or size changes to force a resize
    private var needsResize = false

    // Text size that is set from code. This acts as a starting point for resizing
    private var textSize: Float = 0f

    // Temporary upper bounds on the starting text size
    private var maxTextSize: Float = 0f

    // Lower bounds for text size
    private var minTextSize: Float = MIN_TEXT_SIZE

    // Text view line spacing multiplier
    private var spacingMult = 1.0f

    // Text view additional line spacing
    private var spacingAdd = 0.0f

    // Add ellipsis to text that overflows at the smallest text size
    private var addEllipsis = true

    private val tmpPaint = TextPaint()
    private val step: Float
    private val timer = TimingLogger(TAG, "")

    init {
        textSize = getTextSize()
        step = max(
            2f,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                1f,
                resources.displayMetrics
            )
        )
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        before: Int,
        after: Int
    ) {
        needsResize = true
        // Since this view may be reused, it is good to reset the text size
        resetTextSize()

        val height = height
        val width = width
        if (height > 0 && width > 0) {
            resizeText()
        }
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            needsResize = true
        }
    }

    /**
     * Register listener to receive resize notifications
     */
    fun setOnResizeListener(listener: OnTextResizeListener?) {
        textResizeListener = listener
    }

    /**
     * Override the set text size to update our internal reference values
     */
    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        textSize = getTextSize()
    }

    /**
     * Override the set text size to update our internal reference values
     */
    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        textSize = getTextSize()
    }

    /**
     * Override the set line spacing to update our internal reference values
     */
    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        spacingMult = mult
        spacingAdd = add
    }

    /**
     * Return upper text size limit
     */
    fun getMaxTextSize(): Float = maxTextSize

    /**
     * Set the upper text size limit and invalidate the view
     */
    fun setMaxTextSize(maxTextSize: Float) {
        this.maxTextSize = maxTextSize
        requestLayout()
        invalidate()
    }

    /**
     * Return lower text size limit
     */
    fun getMinTextSize(): Float = minTextSize

    /**
     * Set the lower text size limit and invalidate the view
     */
    fun setMinTextSize(minTextSize: Float) {
        this.minTextSize = minTextSize
        requestLayout()
        invalidate()
    }

    /**
     * Return flag to add ellipsis to text that overflows at the smallest text size
     */
    fun getAddEllipsis(): Boolean = addEllipsis

    /**
     * Set flag to add ellipsis to text that overflows at the smallest text size
     */
    fun setAddEllipsis(addEllipsis: Boolean) {
        this.addEllipsis = addEllipsis
    }

    /**
     * Reset the text to the original size
     */
    fun resetTextSize() {
        if (textSize > 0) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            maxTextSize = textSize
        }
    }

    /**
     * Resize text after measuring
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed || needsResize) {
            val widthLimit = (right - left) - compoundPaddingLeft - compoundPaddingRight
            val heightLimit = (bottom - top) - compoundPaddingBottom - compoundPaddingTop
            resizeText(widthLimit, heightLimit)
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    /**
     * Resize the text size with default width and height
     */
    fun resizeText() {
        val heightLimit = height - paddingBottom - paddingTop
        val widthLimit = width - paddingLeft - paddingRight
        resizeText(widthLimit, heightLimit)
    }

    /**
     * Resize the text size with specified width and height
     */
    @SuppressLint("SetTextI18n")
    fun resizeText(width: Int, height: Int) {
        timer.reset(TAG, "resizeText")
        var text: CharSequence? = text
        // Do not resize if the view does not have dimensions or there is no text
        if (text == null || text.isEmpty() || height <= 0 || width <= 0 || textSize == 0f) {
            return
        }

        if (transformationMethod != null) {
            text = transformationMethod.getTransformation(text, this)
        }

        // Get the text view's paint object
        val textPaint = paint

        // Store the current text size
        val oldTextSize = textPaint.textSize
        // If there is a max text size set, use the lesser of that and the default text size
        var targetTextSize = if (maxTextSize > 0) min(textSize, maxTextSize) else textSize

        // Get the required text height
        var textHeight = getTextHeight(text, textPaint, width, targetTextSize)

        timer.addSplit("beforeScaling")
        if (textHeight > height && targetTextSize > minTextSize) {
            // Until we either fit within our text view or we had reached our min text size, incrementally try smaller sizes
            while (textHeight > height && targetTextSize > minTextSize) {
                // to make search faster let's use "textHeight / height" factor for the step (it is always > 1)
                val factor = textHeight / height
                targetTextSize = max(floor(targetTextSize - step * factor), minTextSize)
                textHeight = getTextHeight(text, textPaint, width, targetTextSize)
            }
        } else if (textHeight < height) {
            // Try bigger sizes until we fill the view
            var newTargetTextSize = targetTextSize
            var newTextHeight = textHeight
            while (newTextHeight < height) {
                // use last values which don't exceed view dimensions
                targetTextSize = newTargetTextSize
                textHeight = newTextHeight

                // to make search faster let's use "height / newTextHeight" factor for the step (it is always > 1)
                val factor = height / newTextHeight
                newTargetTextSize = floor(newTargetTextSize + step * factor)
                newTextHeight = getTextHeight(text, textPaint, width, newTargetTextSize)
            }
        }
        timer.addSplit("scaling")

        // If we had reached our minimum text size and still don't fit, append an ellipsis
        if (addEllipsis && targetTextSize == minTextSize && textHeight > height) {
            // Draw using a static layout
            // modified: use a copy of TextPaint for measuring
            val paint = TextPaint(textPaint)
            // Draw using a static layout
            val layout = StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                spacingMult,
                spacingAdd,
                false
            )
            // Check that we have a least one line of rendered text
            if (layout.lineCount > 0) {
                // Since the line at the specific vertical position would be cut off,
                // we must trim up to the previous line
                val lastLine = layout.getLineForVertical(height) - 1
                // If the text would not even fit on a single line, clear it
                if (lastLine < 0) {
                    setText("")
                }
                // Otherwise, trim to the previous line and add an ellipsis
                else {
                    val start = layout.getLineStart(lastLine)
                    var end = layout.getLineEnd(lastLine)
                    var lineWidth = layout.getLineWidth(lastLine)
                    val ellipseWidth = textPaint.measureText(ELLIPSIS)

                    // Trim characters off until we have enough room to draw the ellipsis
                    while (width < lineWidth + ellipseWidth) {
                        lineWidth = textPaint.measureText(text.subSequence(start, --end + 1).toString())
                    }
                    setText(text.subSequence(0, end).toString() + ELLIPSIS)
                }
            }
        }
        timer.addSplit("ellipsising")

        // Some devices try to auto adjust line spacing, so force default line spacing
        // and invalidate the layout as a side effect
        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize)
        setLineSpacing(spacingAdd, spacingMult)

        // Notify the listener if registered
        textResizeListener?.onTextResize(this, oldTextSize, targetTextSize)

        // Reset force resize flag
        needsResize = false
        timer.dumpToLog()
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private fun getTextHeight(
        source: CharSequence,
        paint: TextPaint,
        width: Int,
        textSize: Float
    ): Int {
        // modified: make a copy of the original TextPaint object for measuring
        // (apparently the object gets modified while measuring, see also the
        // docs for TextView.getPaint() (which states to access it read-only)
        tmpPaint.set(paint)
        // Update the text paint object
        tmpPaint.textSize = textSize
        // Measure using a static layout
        val layout = StaticLayout(
            source,
            tmpPaint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            spacingMult,
            spacingAdd,
            true
        )
        return layout.height
    }

    // Interface for resize notifications
    fun interface OnTextResizeListener {
        fun onTextResize(textView: TextView, oldSize: Float, newSize: Float)
    }

    companion object {
        // Minimum text size for this text view
        const val MIN_TEXT_SIZE = 20f

        // Our ellipse string
        private const val ELLIPSIS = "…"
        private const val TAG = "AutoResizeTextView"
    }
}
