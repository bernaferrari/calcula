package org.solovyev.android.plotter

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import org.solovyev.android.plotter.meshes.MeshSpec

// Color class
@JvmInline
value class Color(val value: Int) {
    fun toInt(): Int = value

    companion object {
        @JvmStatic
        fun create(value: Int): Color = Color(value)
    }
}

// Check utility
object Check {
    @JvmStatic
    fun isNotNull(obj: Any?) {
        if (obj == null) throw IllegalArgumentException("Object must not be null")
    }

    @JvmStatic
    fun isTrue(condition: Boolean) {
        if (!condition) throw IllegalArgumentException("Condition must be true")
    }
}

// Data classes
data class PlotData(
    val functions: MutableList<PlotFunction> = mutableListOf(),
    val axisStyle: AxisStyle = AxisStyle(),
    val dimensions: Dimensions = Dimensions()
) {
    fun get(id: Int): PlotFunction? = functions.find { it.function.id == id }
}

data class AxisStyle(
    var backgroundColor: Int = 0,
    var axisColor: Int = 0x66FFFFFF,
    var gridColor: Int = 0x22FFFFFF,
    var labelColor: Int = 0x99FFFFFF.toInt(),
    var labelTextSizePx: Float = 24f
)

data class Dimensions(
    val graph: Graph = Graph()
) {
    class Graph(
        private var bounds: RectF = RectF(-10f, -10f, 10f, 10f)
    ) {
        fun makeBounds(): RectF = RectF(bounds)

        fun setBounds(rect: RectF) {
            bounds = RectF(rect)
        }
    }
}

// Base listener class
open class BasePlotterListener {
    open fun onFunctionAdded(function: PlotFunction) {}
    open fun onFunctionUpdated(id: Int, function: PlotFunction) {}
    open fun onFunctionRemoved(function: PlotFunction) {}
    open fun onBoundsChanged(bounds: RectF) {}
    open fun onModeChanged(is3d: Boolean) {}
}

// Function-related classes
data class PlotFunction(
    val function: Function,
    val meshSpec: MeshSpec
) {
    companion object {
        @JvmStatic
        fun create(function: Function, meshSpec: MeshSpec): PlotFunction {
            return PlotFunction(function, meshSpec)
        }
    }
}

abstract class Function(val name: String) {
    var id: Int = 0

    abstract fun getArity(): Int
    abstract fun evaluate(): Float
    abstract fun evaluate(x: Float): Float
    abstract fun evaluate(x: Float, y: Float): Float
}

// Plotter interface
interface Plotter {
    val plotData: PlotData
    val dimensions: Dimensions
    val is3d: Boolean

    fun addListener(listener: BasePlotterListener)
    fun removeListener(listener: BasePlotterListener)
    fun add(function: PlotFunction)
    fun update(id: Int, function: PlotFunction)
    fun remove(function: PlotFunction?)
    fun setAxisStyle(axisStyle: AxisStyle)
    fun setBounds(bounds: RectF)
    fun set3d(enabled: Boolean)
}

object Plot {
    @JvmStatic
    fun setGraphBounds(context: Context?, plotter: Plotter, bounds: RectF, is3d: Boolean) {
        plotter.setBounds(bounds)
    }

    @JvmStatic
    fun newPlotter(application: Application): Plotter {
        return object : Plotter {
            private val graph = Dimensions.Graph()
            override val plotData = PlotData(dimensions = Dimensions(graph))
            override val dimensions = plotData.dimensions
            override var is3d = false
                private set

            private val listeners = mutableListOf<BasePlotterListener>()

            override fun addListener(listener: BasePlotterListener) {
                listeners.add(listener)
            }

            override fun removeListener(listener: BasePlotterListener) {
                listeners.remove(listener)
            }

            override fun add(function: PlotFunction) {
                plotData.functions.add(function)
                listeners.forEach { it.onFunctionAdded(function) }
            }

            override fun update(id: Int, function: PlotFunction) {
                val index = plotData.functions.indexOfFirst { it.function.id == id }
                if (index >= 0) {
                    plotData.functions[index] = function
                    listeners.forEach { it.onFunctionUpdated(id, function) }
                }
            }

            override fun remove(function: PlotFunction?) {
                if (function != null && plotData.functions.remove(function)) {
                    listeners.forEach { it.onFunctionRemoved(function) }
                }
            }

            override fun setAxisStyle(axisStyle: AxisStyle) {
                plotData.axisStyle.backgroundColor = axisStyle.backgroundColor
                plotData.axisStyle.axisColor = axisStyle.axisColor
                plotData.axisStyle.gridColor = axisStyle.gridColor
                plotData.axisStyle.labelColor = axisStyle.labelColor
                plotData.axisStyle.labelTextSizePx = axisStyle.labelTextSizePx
            }

            override fun setBounds(bounds: RectF) {
                dimensions.graph.setBounds(bounds)
                listeners.forEach { it.onBoundsChanged(bounds) }
            }

            override fun set3d(enabled: Boolean) {
                if (is3d == enabled) return
                is3d = enabled
                listeners.forEach { it.onModeChanged(enabled) }
            }
        }
    }
}

class PlotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var plotter: Plotter? = null
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        textAlign = Paint.Align.LEFT
    }
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, PanListener())

    fun setPlotter(plotter: Plotter?) {
        this.plotter = plotter
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val plotter = plotter ?: return
        val bounds = plotter.dimensions.graph.makeBounds()

        val axisStyle = plotter.plotData.axisStyle
        backgroundPaint.color = axisStyle.backgroundColor
        axisPaint.color = axisStyle.axisColor
        gridPaint.color = axisStyle.gridColor
        labelPaint.color = axisStyle.labelColor
        labelPaint.textSize = axisStyle.labelTextSizePx

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        drawGrid(canvas, bounds)
        drawAxes(canvas, bounds)
        drawAxisLabels(canvas, bounds)

        plotter.plotData.functions.forEach { function ->
            drawFunction(canvas, bounds, function)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun drawGrid(canvas: Canvas, bounds: RectF) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val density = resources.displayMetrics.density
        val targetPx = 80f * density
        val targetStepsX = (w / targetPx).coerceIn(3f, 10f)
        val targetStepsY = (h / targetPx).coerceIn(3f, 10f)

        val stepX = niceStep((bounds.right - bounds.left) / targetStepsX)
        val stepY = niceStep((bounds.bottom - bounds.top) / targetStepsY)

        var x = kotlin.math.floor(bounds.left / stepX) * stepX
        while (x <= bounds.right) {
            val sx = mapX(x, bounds)
            canvas.drawLine(sx, 0f, sx, h, gridPaint)
            x += stepX
        }

        var y = kotlin.math.floor(bounds.top / stepY) * stepY
        while (y <= bounds.bottom) {
            val sy = mapY(y, bounds)
            canvas.drawLine(0f, sy, w, sy, gridPaint)
            y += stepY
        }
    }

    private fun drawAxes(canvas: Canvas, bounds: RectF) {
        if (bounds.left < 0f && bounds.right > 0f) {
            val x = mapX(0f, bounds)
            canvas.drawLine(x, 0f, x, height.toFloat(), axisPaint)
        }
        if (bounds.top < 0f && bounds.bottom > 0f) {
            val y = mapY(0f, bounds)
            canvas.drawLine(0f, y, width.toFloat(), y, axisPaint)
        }
    }

    private fun drawAxisLabels(canvas: Canvas, bounds: RectF) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val density = resources.displayMetrics.density
        val targetPx = 80f * density
        val targetStepsX = (w / targetPx).coerceIn(3f, 10f)
        val targetStepsY = (h / targetPx).coerceIn(3f, 10f)
        val stepX = niceStep((bounds.right - bounds.left) / targetStepsX)
        val stepY = niceStep((bounds.bottom - bounds.top) / targetStepsY)

        val xAxisVisible = bounds.top < 0f && bounds.bottom > 0f
        val yAxisVisible = bounds.left < 0f && bounds.right > 0f
        val xAxisY = if (xAxisVisible) mapY(0f, bounds) else h - 6f * density
        val yAxisX = if (yAxisVisible) mapX(0f, bounds) else 6f * density

        var x = kotlin.math.floor(bounds.left / stepX) * stepX
        while (x <= bounds.right) {
            if (x != 0f || !yAxisVisible) {
                val sx = mapX(x, bounds)
                val label = formatLabel(x)
                val textWidth = labelPaint.measureText(label)
                val tx = (sx - textWidth / 2f).coerceIn(0f, w - textWidth)
                canvas.drawText(label, tx, (xAxisY + 18f * density).coerceIn(18f * density, h - 4f), labelPaint)
            }
            x += stepX
        }

        var y = kotlin.math.floor(bounds.top / stepY) * stepY
        while (y <= bounds.bottom) {
            if (y != 0f || !xAxisVisible) {
                val sy = mapY(y, bounds)
                val label = formatLabel(y)
                val ty = (sy + 6f * density).coerceIn(18f * density, h - 6f * density)
                canvas.drawText(label, (yAxisX + 6f * density).coerceIn(6f * density, w - 40f * density), ty, labelPaint)
            }
            y += stepY
        }
    }

    private fun niceStep(raw: Float): Float {
        if (!raw.isFinite() || raw == 0f) return 1f
        val exponent = kotlin.math.floor(kotlin.math.log10(kotlin.math.abs(raw).toDouble())).toInt()
        val base = Math.pow(10.0, exponent.toDouble()).toFloat()
        val fraction = raw / base
        val niceFraction = when {
            fraction <= 1f -> 1f
            fraction <= 2f -> 2f
            fraction <= 5f -> 5f
            else -> 10f
        }
        return niceFraction * base
    }

    private fun formatLabel(value: Float): String {
        val absValue = kotlin.math.abs(value)
        val decimals = when {
            absValue >= 1000f -> 0
            absValue >= 100f -> 0
            absValue >= 10f -> 1
            absValue >= 1f -> 2
            absValue >= 0.1f -> 3
            else -> 4
        }
        return "%.${decimals}f".format(java.util.Locale.US, value)
            .trimEnd('0')
            .trimEnd('.')
    }

    private fun drawFunction(canvas: Canvas, bounds: RectF, function: PlotFunction) {
        val mesh = function.meshSpec
        val path = Path()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mesh.color.toInt()
            strokeWidth = mesh.width.toFloat()
            style = Paint.Style.STROKE
        }

        val points = mesh.pointsCount.coerceAtLeast(16)
        val xStep = (bounds.right - bounds.left) / (points - 1)

        when (function.function.getArity()) {
            0 -> {
                val yValue = function.function.evaluate()
                if (!yValue.isFinite()) return
                val y = mapY(yValue, bounds)
                canvas.drawLine(0f, y, width.toFloat(), y, paint)
            }
            1 -> {
                var started = false
                var prevY = 0f
                val jumpThreshold = bounds.height() * 5f
                val clampThreshold = bounds.height() * 10f
                for (i in 0 until points) {
                    val x = bounds.left + i * xStep
                    val yValue = function.function.evaluate(x)
                    if (!yValue.isFinite()) {
                        started = false
                        continue
                    }
                    if (kotlin.math.abs(yValue) > clampThreshold) {
                        started = false
                        continue
                    }
                    if (started && kotlin.math.abs(yValue - prevY) > jumpThreshold) {
                        started = false
                    }
                    val sx = mapX(x, bounds)
                    val sy = mapY(yValue, bounds)
                    if (!started) {
                        path.moveTo(sx, sy)
                        started = true
                    } else {
                        path.lineTo(sx, sy)
                    }
                    prevY = yValue
                }
                canvas.drawPath(path, paint)
            }
            else -> {
                if (plotter?.is3d == true) {
                    drawHeatmap(canvas, bounds, function, mesh)
                } else {
                    val slices = listOf(bounds.top, (bounds.top + bounds.bottom) / 2f, bounds.bottom)
                    slices.forEach { ySlice ->
                        val slicePath = Path()
                        var started = false
                        var prevY = 0f
                        val jumpThreshold = bounds.height() * 5f
                        val clampThreshold = bounds.height() * 10f
                        for (i in 0 until points) {
                            val x = bounds.left + i * xStep
                            val yValue = function.function.evaluate(x, ySlice)
                            if (!yValue.isFinite()) {
                                started = false
                                continue
                            }
                            if (kotlin.math.abs(yValue) > clampThreshold) {
                                started = false
                                continue
                            }
                            if (started && kotlin.math.abs(yValue - prevY) > jumpThreshold) {
                                started = false
                            }
                            val sx = mapX(x, bounds)
                            val sy = mapY(yValue, bounds)
                            if (!started) {
                                slicePath.moveTo(sx, sy)
                                started = true
                            } else {
                                slicePath.lineTo(sx, sy)
                            }
                            prevY = yValue
                        }
                        canvas.drawPath(slicePath, paint)
                    }
                }
            }
        }
    }

    private fun drawHeatmap(canvas: Canvas, bounds: RectF, function: PlotFunction, mesh: MeshSpec) {
        val grid = (mesh.pointsCount / 2).coerceIn(24, 80)
        val xStep = (bounds.right - bounds.left) / (grid - 1)
        val yStep = (bounds.bottom - bounds.top) / (grid - 1)
        val values = Array(grid) { FloatArray(grid) }
        var min = Float.POSITIVE_INFINITY
        var max = Float.NEGATIVE_INFINITY

        for (j in 0 until grid) {
            val y = bounds.top + j * yStep
            for (i in 0 until grid) {
                val x = bounds.left + i * xStep
                val z = function.function.evaluate(x, y)
                values[j][i] = z
                if (z.isFinite()) {
                    if (z < min) min = z
                    if (z > max) max = z
                }
            }
        }

        if (!min.isFinite() || !max.isFinite() || min == max) {
            return
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val cellWidth = width.toFloat() / (grid - 1)
        val cellHeight = height.toFloat() / (grid - 1)

        for (j in 0 until grid - 1) {
            for (i in 0 until grid - 1) {
                val z = values[j][i]
                if (!z.isFinite()) continue
                val t = ((z - min) / (max - min)).coerceIn(0f, 1f)
                paint.color = blendColor(heatmapColor(t), mesh.color.toInt(), 0.25f)
                val left = i * cellWidth
                val top = height.toFloat() - (j + 1) * cellHeight
                canvas.drawRect(left, top, left + cellWidth, top + cellHeight, paint)
            }
        }
    }

    private fun heatmapColor(t: Float): Int {
        val clamped = t.coerceIn(0f, 1f)
        val stops = intArrayOf(
            0xFF1E3A8A.toInt(),
            0xFF2563EB.toInt(),
            0xFF22D3EE.toInt(),
            0xFF10B981.toInt(),
            0xFFFACC15.toInt(),
            0xFFEF4444.toInt()
        )
        val step = 1f / (stops.size - 1)
        val index = (clamped / step).toInt().coerceAtMost(stops.size - 2)
        val localT = (clamped - index * step) / step
        return lerpColor(stops[index], stops[index + 1], localT)
    }

    private fun lerpColor(start: Int, end: Int, t: Float): Int {
        val a = lerpChannel(start shr 24 and 0xFF, end shr 24 and 0xFF, t)
        val r = lerpChannel(start shr 16 and 0xFF, end shr 16 and 0xFF, t)
        val g = lerpChannel(start shr 8 and 0xFF, end shr 8 and 0xFF, t)
        val b = lerpChannel(start and 0xFF, end and 0xFF, t)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun lerpChannel(start: Int, end: Int, t: Float): Int {
        return (start + (end - start) * t).toInt().coerceIn(0, 255)
    }

    private fun blendColor(base: Int, overlay: Int, overlayAlpha: Float): Int {
        val alpha = overlayAlpha.coerceIn(0f, 1f)
        val inv = 1f - alpha
        val a = ((base shr 24 and 0xFF) * inv + (overlay shr 24 and 0xFF) * alpha).toInt()
        val r = ((base shr 16 and 0xFF) * inv + (overlay shr 16 and 0xFF) * alpha).toInt()
        val g = ((base shr 8 and 0xFF) * inv + (overlay shr 8 and 0xFF) * alpha).toInt()
        val b = ((base and 0xFF) * inv + (overlay and 0xFF) * alpha).toInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun mapX(x: Float, bounds: RectF): Float {
        val width = width.toFloat()
        if (width <= 0f) return 0f
        return (x - bounds.left) / (bounds.right - bounds.left) * width
    }

    private fun mapY(y: Float, bounds: RectF): Float {
        val height = height.toFloat()
        if (height <= 0f) return 0f
        val normalized = (y - bounds.top) / (bounds.bottom - bounds.top)
        return height - normalized * height
    }

    private fun screenToWorldX(screenX: Float, bounds: RectF): Float {
        val width = width.toFloat().coerceAtLeast(1f)
        return bounds.left + (screenX / width) * (bounds.right - bounds.left)
    }

    private fun screenToWorldY(screenY: Float, bounds: RectF): Float {
        val height = height.toFloat().coerceAtLeast(1f)
        return bounds.top + (1f - screenY / height) * (bounds.bottom - bounds.top)
    }

    private inner class PanListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val plotter = plotter ?: return false
            val bounds = plotter.dimensions.graph.makeBounds()
            val rangeX = bounds.right - bounds.left
            val rangeY = bounds.bottom - bounds.top
            if (rangeX == 0f || rangeY == 0f) return false
            val viewWidth = width.toFloat().coerceAtLeast(1f)
            val viewHeight = height.toFloat().coerceAtLeast(1f)
            val dx = distanceX / viewWidth * rangeX
            val dy = -distanceY / viewHeight * rangeY
            val newBounds = RectF(
                bounds.left + dx,
                bounds.top + dy,
                bounds.right + dx,
                bounds.bottom + dy
            )
            plotter.setBounds(newBounds)
            return true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val plotter = plotter ?: return false
            val bounds = plotter.dimensions.graph.makeBounds()
            val scale = detector.scaleFactor
            if (scale.isNaN() || scale <= 0f) return false
            val focusX = screenToWorldX(detector.focusX, bounds)
            val focusY = screenToWorldY(detector.focusY, bounds)
            val newWidth = (bounds.right - bounds.left) / scale
            val newHeight = (bounds.bottom - bounds.top) / scale
            val left = focusX - (focusX - bounds.left) / scale
            val top = focusY - (focusY - bounds.top) / scale
            val newBounds = RectF(left, top, left + newWidth, top + newHeight)
            plotter.setBounds(newBounds)
            return true
        }
    }
}

class PlotViewFrame @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface Listener {
        fun onButtonPressed(id: Int): Boolean
        fun unableToZoom(`in`: Boolean)
    }

    private val plotView = PlotView(context)
    private var plotter: Plotter? = null
    private var listener: Listener? = null
    private val plotListener = object : BasePlotterListener() {
        override fun onFunctionAdded(function: PlotFunction) {
            plotView.invalidate()
        }

        override fun onFunctionUpdated(id: Int, function: PlotFunction) {
            plotView.invalidate()
        }

        override fun onFunctionRemoved(function: PlotFunction) {
            plotView.invalidate()
        }

        override fun onBoundsChanged(bounds: RectF) {
            plotView.invalidate()
        }

        override fun onModeChanged(is3d: Boolean) {
            plotView.invalidate()
        }
    }

    init {
        addView(
            plotView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )
    }

    fun addControlView(id: Int) {
        // Stub implementation
    }

    fun setPlotter(plotter: Plotter) {
        this.plotter?.removeListener(plotListener)
        this.plotter = plotter
        plotter.addListener(plotListener)
        plotView.setPlotter(plotter)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun onPause() {
        // Stub implementation
    }

    fun onResume() {
        // Stub implementation
    }

    override fun onDetachedFromWindow() {
        plotter?.removeListener(plotListener)
        plotter = null
        super.onDetachedFromWindow()
    }
}

class PlotIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var meshSpec: MeshSpec? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    fun setMeshSpec(meshSpec: MeshSpec) {
        this.meshSpec = meshSpec
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val spec = meshSpec ?: return
        paint.color = spec.color.toInt()
        paint.strokeWidth = spec.width.toFloat()
        val midY = height / 2f
        val pad = (spec.width * 2).toFloat()
        canvas.drawLine(pad, midY, width - pad, midY, paint)
    }
}
