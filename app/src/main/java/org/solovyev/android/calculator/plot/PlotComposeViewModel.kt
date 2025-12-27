package org.solovyev.android.calculator.plot

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.solovyev.android.plotter.BasePlotterListener
import org.solovyev.android.plotter.PlotFunction
import org.solovyev.android.plotter.Plotter
import javax.inject.Inject

@HiltViewModel
class PlotComposeViewModel @Inject constructor(
    private val plotter: Plotter
) : ViewModel() {

    private val _functions = MutableStateFlow(plotter.plotData.functions.toList())
    val functions: StateFlow<List<PlotFunction>> = _functions.asStateFlow()

    private val listener = object : BasePlotterListener() {
        override fun onFunctionAdded(function: PlotFunction) {
            _functions.value = plotter.plotData.functions.toList()
        }

        override fun onFunctionUpdated(id: Int, function: PlotFunction) {
            _functions.value = plotter.plotData.functions.toList()
        }

        override fun onFunctionRemoved(function: PlotFunction) {
            _functions.value = plotter.plotData.functions.toList()
        }
    }

    init {
        plotter.addListener(listener)
    }

    override fun onCleared() {
        plotter.removeListener(listener)
        super.onCleared()
    }
}
