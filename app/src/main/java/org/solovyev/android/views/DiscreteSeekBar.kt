package org.solovyev.android.views

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import org.solovyev.android.Check
import org.solovyev.android.calculator.R

/**
 * SeekBar for discrete values with a label displayed underneath the active tick
 */
class DiscreteSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatSeekBar(context, attrs, defStyle) {

    private val paint = Paint()
    private var objectAnimator: ObjectAnimator? = null
    private var onChangeListener: OnChangeListener? = null
    private var currentTick = 0
    private lateinit var tickLabels: Array<CharSequence>
    private var labelColor: ColorStateList? = null

    init {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DiscreteSeekBar, defStyle, 0)
        tickLabels = a.getTextArray(R.styleable.DiscreteSeekBar_values) ?: emptyArray()
        val labelsSize = a.getDimensionPixelSize(R.styleable.DiscreteSeekBar_labelsSize, 0)
        val labelsColor = a.getColorStateList(R.styleable.DiscreteSeekBar_labelsColor)
        a.recycle()

        Check.isNotNull(tickLabels)
        Check.isTrue(tickLabels.isNotEmpty())
        Check.isTrue(labelsSize > 0)

        paint.style = Paint.Style.FILL
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.textSize = labelsSize.toFloat()

        if (labelsColor != null) {
            setLabelColor(labelsColor)
        } else {
            paint.color = Color.BLACK
        }

        // Extend the bottom padding to include tick label height (including descent in order to not
        // clip glyphs that extends below the baseline).
        val fi = paint.fontMetricsInt
        setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            paddingBottom + labelsSize + fi.descent
        )

        super.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    objectAnimator?.cancel()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                currentTick = getClosestTick(seekBar.progress)
                val endProgress = getProgressForTick(currentTick)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    objectAnimator = ObjectAnimator.ofInt(
                        seekBar, "progress", seekBar.progress, endProgress
                    ).apply {
                        interpolator = DecelerateInterpolator()
                        duration = THUMB_SNAP_DURATION_TIME.toLong()
                        start()
                    }
                } else {
                    seekBar.progress = endProgress
                }
                onChangeListener?.onValueChanged(currentTick)
            }
        })
    }

    private fun getClosestTick(progress: Int): Int {
        val normalizedValue = progress.toFloat() / max
        return (normalizedValue * maxTick).toInt()
    }

    private fun getProgressForTick(tick: Int): Int {
        return (max / maxTick) * tick
    }

    override fun setOnSeekBarChangeListener(seekBarChangeListener: OnSeekBarChangeListener?) {
        // It doesn't make sense to expose the interface for listening to intermediate changes.
        Check.isTrue(false)
    }

    /**
     * Get the largest tick value the SeekBar can represent
     *
     * @return maximum tick value
     */
    val maxTick: Int
        get() = tickLabels.size - 1

    /**
     * Set listener for observing value changes
     *
     * @param onChangeListener listener that should receive updates
     */
    fun setOnChangeListener(onChangeListener: OnChangeListener?) {
        this.onChangeListener = onChangeListener
    }

    /**
     * Set tick value
     *
     * @param tick tick value in range [0, maxTick]
     */
    fun setCurrentTick(tick: Int) {
        Check.isTrue(tick >= 0)
        Check.isTrue(tick <= maxTick)
        currentTick = tick
        progress = getProgressForTick(currentTick)
    }

    fun getCurrentTick(): Int = currentTick

    fun setLabelColor(color: Int) {
        labelColor = ColorStateList.valueOf(color)
        updateLabelColor()
    }

    fun setLabelColor(colors: ColorStateList) {
        labelColor = colors
        updateLabelColor()
    }

    private fun updateLabelColor() {
        val color = labelColor?.getColorForState(drawableState, Color.BLACK) ?: Color.BLACK
        if (color != paint.color) {
            paint.color = color
            invalidate()
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateLabelColor()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sliderWidth = width - paddingRight - paddingLeft.toFloat()
        val sliderStepSize = sliderWidth / maxTick
        val closestTick = getClosestTick(progress)
        val text = tickLabels[closestTick].toString()
        val startOffset = paddingLeft.toFloat()
        val tickLabelWidth = paint.measureText(text)
        val tickPos = sliderStepSize * closestTick
        val labelOffset: Float = when {
            // First step description text should be anchored with its left edge just
            // below the slider start tick. The last step description should be anchored
            // to the right just under the end tick. Tick labels in between are centered below
            // each tick.
            closestTick == 0 -> startOffset
            closestTick == maxTick -> startOffset + sliderWidth - tickLabelWidth
            else -> startOffset + tickPos - tickLabelWidth / 2
        }
        // Text position is drawn from bottom left, with bottom at the font baseline. We need to
        // offset by the descent to cover e.g 'g' that extends below the baseline.
        val m = paint.fontMetricsInt
        val lowestPosForFullGlyphCoverage = height - m.descent
        canvas.drawText(text, labelOffset, lowestPosForFullGlyphCoverage.toFloat(), paint)
    }

    /**
     * Listener for observing tick changes
     */
    fun interface OnChangeListener {
        fun onValueChanged(selectedTick: Int)
    }

    companion object {
        // Duration of how quick the SeekBar thumb should snap to its destination value
        private const val THUMB_SNAP_DURATION_TIME = 100
    }
}
