package org.solovyev.android.plotter

import android.app.Application
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
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
    var backgroundColor: Int = 0
)

data class Dimensions(
    val graph: Graph = Graph()
) {
    class Graph {
        fun makeBounds(): RectF = RectF(0f, 0f, 10f, 10f)
    }
}

// Base listener class
open class BasePlotterListener {
    open fun onFunctionAdded(function: PlotFunction) {}
    open fun onFunctionUpdated(id: Int, function: PlotFunction) {}
    open fun onFunctionRemoved(function: PlotFunction) {}
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
}

object Plot {
    @JvmStatic
    fun setGraphBounds(context: Context?, plotter: Plotter, bounds: RectF, is3d: Boolean) {
        // Stub implementation for setting graph bounds
    }

    @JvmStatic
    fun newPlotter(application: Application): Plotter {
        return object : Plotter {
            override val plotData = PlotData()
            override val dimensions = Dimensions()
            override val is3d = false

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
                // Stub implementation
            }
        }
    }
}

class PlotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)

class PlotViewFrame @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface Listener {
        fun onButtonPressed(id: Int): Boolean
        fun unableToZoom(`in`: Boolean)
    }

    fun addControlView(id: Int) {
        // Stub implementation
    }

    fun setPlotter(plotter: Plotter) {
        // Stub implementation
    }

    fun setListener(listener: Listener) {
        // Stub implementation
    }

    fun onPause() {
        // Stub implementation
    }

    fun onResume() {
        // Stub implementation
    }
}

class PlotIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun setMeshSpec(meshSpec: MeshSpec) {
        // Stub implementation
    }
}
