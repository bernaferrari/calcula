package org.solovyev.android.calculator.ui.compose.entities

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.operators.OperatorCategory
import org.solovyev.android.calculator.operators.OperatorsRegistry
import org.solovyev.android.calculator.operators.PostfixFunctionsRegistry
import javax.inject.Inject

@HiltViewModel
class OperatorsComposeViewModel @Inject constructor(
    private val operatorsRegistry: OperatorsRegistry,
    private val postfixFunctionsRegistry: PostfixFunctionsRegistry,
    private val keyboard: Keyboard
) : ViewModel() {

    fun getOperatorCategories(): List<OperatorCategory> = OperatorCategory.values().toList()

    fun getOperatorsFor(category: OperatorCategory): List<jscl.math.operator.Operator> {
        val operators = operatorsRegistry.getEntities()
        val postfix = postfixFunctionsRegistry.getEntities()
        return (operators + postfix).filter { category.isInCategory(it) }
    }

    fun getOperatorDescription(operator: jscl.math.operator.Operator): String? {
        val name = operator.name
        return operatorsRegistry.getDescription(name) ?: postfixFunctionsRegistry.getDescription(name)
    }

    fun useName(name: String) {
        keyboard.buttonPressed(name)
    }
}
