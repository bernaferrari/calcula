/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viewpagerindicator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.viewpager.widget.ViewPager
import org.solovyev.android.calculator.R
import kotlin.math.abs

/**
 * Draws circles (one for each view). The current view position is filled and
 * others are only stroked.
 */
class CirclePageIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.vpiCirclePageIndicatorStyle
) : View(context, attrs, defStyle), PageIndicator {

    companion object {
        private const val INVALID_POINTER = -1
    }

    private val paintPageFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)

    private var radius: Float = 0f
    private var viewPager: ViewPager? = null
    private var listener: ViewPager.OnPageChangeListener? = null
    private var currentPage: Int = 0
    private var snapPage: Int = 0
    private var pageOffset: Float = 0f
    private var scrollState: Int = 0
    private var orientation: Int = 0
    private var centered: Boolean = false
    private var snap: Boolean = false

    private var touchSlop: Int = 0
    private var lastMotionX: Float = -1f
    private var activePointerId: Int = INVALID_POINTER
    private var isDragging: Boolean = false

    init {
        if (!isInEditMode) {
            val res = resources
        val defaultPageColor = res.getColor(R.color.default_circle_indicator_page_color)
        val defaultFillColor = res.getColor(R.color.default_circle_indicator_fill_color)
        val defaultOrientation = res.getInteger(R.integer.default_circle_indicator_orientation)
        val defaultStrokeColor = res.getColor(R.color.default_circle_indicator_stroke_color)
        val defaultStrokeWidth = res.getDimension(R.dimen.default_circle_indicator_stroke_width)
        val defaultRadius = res.getDimension(R.dimen.default_circle_indicator_radius)
        val defaultCentered = res.getBoolean(R.bool.default_circle_indicator_centered)
        val defaultSnap = res.getBoolean(R.bool.default_circle_indicator_snap)

        val a = context.obtainStyledAttributes(attrs, R.styleable.CirclePageIndicator, defStyle, 0)

        centered = a.getBoolean(R.styleable.CirclePageIndicator_centered, defaultCentered)
        orientation = a.getInt(R.styleable.CirclePageIndicator_android_orientation, defaultOrientation)
        paintPageFill.style = Style.FILL
        paintPageFill.color = a.getColor(R.styleable.CirclePageIndicator_pageColor, defaultPageColor)
        paintStroke.style = Style.STROKE
        paintStroke.color = a.getColor(R.styleable.CirclePageIndicator_strokeColor, defaultStrokeColor)
        paintStroke.strokeWidth = a.getDimension(R.styleable.CirclePageIndicator_strokeWidth, defaultStrokeWidth)
        paintFill.style = Style.FILL
        paintFill.color = a.getColor(R.styleable.CirclePageIndicator_fillColor, defaultFillColor)
        radius = a.getDimension(R.styleable.CirclePageIndicator_radius, defaultRadius)
        snap = a.getBoolean(R.styleable.CirclePageIndicator_snap, defaultSnap)

        val background = a.getDrawable(R.styleable.CirclePageIndicator_android_background)
        background?.let { setBackgroundDrawable(it) }

        a.recycle()

        val configuration = ViewConfiguration.get(context)
        touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration)
        }
    }

    var isCentered: Boolean
        get() = centered
        set(value) {
            centered = value
            invalidate()
        }

    var pageColor: Int
        get() = paintPageFill.color
        set(value) {
            paintPageFill.color = value
            invalidate()
        }

    var fillColor: Int
        get() = paintFill.color
        set(value) {
            paintFill.color = value
            invalidate()
        }

    var pageOrientation: Int
        get() = orientation
        set(value) {
            when (value) {
                HORIZONTAL, VERTICAL -> {
                    orientation = value
                    requestLayout()
                }
                else -> throw IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.")
            }
        }

    var strokeColor: Int
        get() = paintStroke.color
        set(value) {
            paintStroke.color = value
            invalidate()
        }

    var strokeWidth: Float
        get() = paintStroke.strokeWidth
        set(value) {
            paintStroke.strokeWidth = value
            invalidate()
        }

    var circleRadius: Float
        get() = radius
        set(value) {
            radius = value
            invalidate()
        }

    var isSnap: Boolean
        get() = snap
        set(value) {
            snap = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewPager = this.viewPager ?: return
        val count = viewPager.adapter?.count ?: 0
        if (count == 0) return

        if (currentPage >= count) {
            setCurrentItem(count - 1)
            return
        }

        val longSize: Int
        val longPaddingBefore: Int
        val longPaddingAfter: Int
        val shortPaddingBefore: Int

        if (orientation == HORIZONTAL) {
            longSize = width
            longPaddingBefore = paddingLeft
            longPaddingAfter = paddingRight
            shortPaddingBefore = paddingTop
        } else {
            longSize = height
            longPaddingBefore = paddingTop
            longPaddingAfter = paddingBottom
            shortPaddingBefore = paddingLeft
        }

        val threeRadius = radius * 3
        val shortOffset = shortPaddingBefore + radius
        var longOffset = longPaddingBefore + radius

        if (centered) {
            longOffset += ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f) - ((count * threeRadius) / 2.0f)
        }

        var pageFillRadius = radius
        if (paintStroke.strokeWidth > 0) {
            pageFillRadius -= paintStroke.strokeWidth / 2.0f
        }

        // Draw stroked circles
        for (iLoop in 0 until count) {
            val drawLong = longOffset + (iLoop * threeRadius)
            val (dX, dY) = if (orientation == HORIZONTAL) {
                drawLong to shortOffset
            } else {
                shortOffset to drawLong
            }

            // Only paint fill if not completely transparent
            if (paintPageFill.alpha > 0) {
                canvas.drawCircle(dX, dY, pageFillRadius, paintPageFill)
            }

            // Only paint stroke if a stroke width was non-zero
            if (pageFillRadius != radius) {
                canvas.drawCircle(dX, dY, radius, paintStroke)
            }
        }

        // Draw the filled circle according to the current scroll
        var cx = (if (snap) snapPage else currentPage) * threeRadius
        if (!snap) {
            cx += pageOffset * threeRadius
        }

        val (dX, dY) = if (orientation == HORIZONTAL) {
            longOffset + cx to shortOffset
        } else {
            shortOffset to longOffset + cx
        }
        canvas.drawCircle(dX, dY, radius, paintFill)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (super.onTouchEvent(ev)) {
            return true
        }

        val viewPager = this.viewPager ?: return false
        val count = viewPager.adapter?.count ?: 0
        if (count == 0) return false

        when (ev.action and MotionEventCompat.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = MotionEventCompat.getPointerId(ev, 0)
                lastMotionX = ev.x
            }

            MotionEvent.ACTION_MOVE -> {
                val activePointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId)
                val x = MotionEventCompat.getX(ev, activePointerIndex)
                val deltaX = x - lastMotionX

                if (!isDragging) {
                    if (abs(deltaX) > touchSlop) {
                        isDragging = true
                    }
                }

                if (isDragging) {
                    lastMotionX = x
                    if (viewPager.isFakeDragging || viewPager.beginFakeDrag()) {
                        viewPager.fakeDragBy(deltaX)
                    }
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    val width = width
                    val halfWidth = width / 2f
                    val sixthWidth = width / 6f

                    if ((currentPage > 0) && (ev.x < halfWidth - sixthWidth)) {
                        if (ev.action != MotionEvent.ACTION_CANCEL) {
                            viewPager.currentItem = currentPage - 1
                        }
                        return true
                    } else if ((currentPage < count - 1) && (ev.x > halfWidth + sixthWidth)) {
                        if (ev.action != MotionEvent.ACTION_CANCEL) {
                            viewPager.currentItem = currentPage + 1
                        }
                        return true
                    }
                }

                isDragging = false
                activePointerId = INVALID_POINTER
                if (viewPager.isFakeDragging) viewPager.endFakeDrag()
            }

            MotionEventCompat.ACTION_POINTER_DOWN -> {
                val index = MotionEventCompat.getActionIndex(ev)
                lastMotionX = MotionEventCompat.getX(ev, index)
                activePointerId = MotionEventCompat.getPointerId(ev, index)
            }

            MotionEventCompat.ACTION_POINTER_UP -> {
                val pointerIndex = MotionEventCompat.getActionIndex(ev)
                val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    activePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
                }
                lastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, activePointerId))
            }
        }

        return true
    }

    override fun setViewPager(view: ViewPager) {
        if (viewPager === view) return

        viewPager?.setOnPageChangeListener(null)
        requireNotNull(view.adapter) { "ViewPager does not have adapter instance." }

        viewPager = view
        viewPager?.setOnPageChangeListener(this)
        invalidate()
    }

    override fun setViewPager(view: ViewPager, initialPosition: Int) {
        setViewPager(view)
        setCurrentItem(initialPosition)
    }

    override fun setCurrentItem(item: Int) {
        checkNotNull(viewPager) { "ViewPager has not been bound." }
        viewPager?.currentItem = item
        currentPage = item
        invalidate()
    }

    override fun notifyDataSetChanged() {
        invalidate()
    }

    override fun onPageScrollStateChanged(state: Int) {
        scrollState = state
        listener?.onPageScrollStateChanged(state)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        currentPage = position
        pageOffset = positionOffset
        invalidate()
        listener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
    }

    override fun onPageSelected(position: Int) {
        if (snap || scrollState == ViewPager.SCROLL_STATE_IDLE) {
            currentPage = position
            snapPage = position
            invalidate()
        }
        listener?.onPageSelected(position)
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        this.listener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (orientation == HORIZONTAL) {
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec))
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec))
        }
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private fun measureLong(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY || viewPager == null) {
            // We were told how big to be
            return specSize
        }

        // Calculate the width according the views count
        val count = viewPager?.adapter?.count ?: 0
        val result = (paddingLeft + paddingRight + (count * 2 * radius) + (count - 1) * radius + 1).toInt()

        // Respect AT_MOST value if that was what is called for by measureSpec
        return if (specMode == MeasureSpec.AT_MOST) {
            result.coerceAtMost(specSize)
        } else {
            result
        }
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private fun measureShort(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            return specSize
        }

        // Measure the height
        val result = (2 * radius + paddingTop + paddingBottom + 1).toInt()

        // Respect AT_MOST value if that was what is called for by measureSpec
        return if (specMode == MeasureSpec.AT_MOST) {
            result.coerceAtMost(specSize)
        } else {
            result
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        currentPage = savedState.currentPage
        snapPage = savedState.currentPage
        requestLayout()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState).apply {
            currentPage = this@CirclePageIndicator.currentPage
        }
    }

    internal class SavedState : BaseSavedState {
        var currentPage: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            currentPage = parcel.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(currentPage)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
