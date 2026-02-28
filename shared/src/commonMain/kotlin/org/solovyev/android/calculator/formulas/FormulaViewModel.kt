package org.solovyev.android.calculator.formulas

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import jscl.JsclMathEngine
import jscl.text.Parser
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.jscl.JsclOperation

class FormulaViewModel(private val engine: Engine) : ViewModel() {
    private val _formulas = MutableStateFlow(FormulaLibrary.getAll())
    val formulas: StateFlow<List<Formula>> = _formulas.asStateFlow()

    fun search(query: String) {
        _formulas.value = if (query.isBlank()) {
            FormulaLibrary.getAll()
        } else {
            FormulaLibrary.search(query)
        }
    }

    fun evaluate(expression: String): String {
        return try {
            val mathEngine = engine.getMathEngine()
            val result = JsclOperation.numeric.evaluateGeneric(expression, mathEngine)
            JsclOperation.numeric.getFromProcessor(engine).process(result)
        } catch (e: Exception) {
            "Error"
        }
    }
}
