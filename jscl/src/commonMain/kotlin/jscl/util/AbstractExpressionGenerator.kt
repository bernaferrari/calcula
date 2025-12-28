package jscl.util

import kotlin.math.pow

abstract class AbstractExpressionGenerator<T>(protected val depth: Int = 10) {

    abstract fun generate(): T

    protected fun generateBrackets(): Boolean {
        return Math.random() > 0.8
    }

    protected fun generateOperation(): Operation {
        val operationId = (Math.random() * 4.0).toInt()
        return Operation.getOperationById(operationId)
            ?: throw UnsupportedOperationException("Check!")
    }

    protected fun generateFunction(): Function? {
        val functionId = (Math.random() * 8.0).toInt()
        return Function.getFunctionById(functionId)
    }

    // only positive values (as - operator exists)
    protected fun generateNumber(): Double {
        return Math.random() * MAX_VALUE
    }

    protected enum class Operation(private val operationId: Int, val token: String) {
        ADDITION(0, "+"),
        SUBTRACTION(1, "-"),
        MULTIPLICATION(2, "*"),
        DIVISION(3, "/");

        companion object {
            fun getOperationById(operationId: Int): Operation? {
                return values().find { it.operationId == operationId }
            }
        }
    }

    protected enum class Function(private val functionId: Int, val token: String) {
        SIN(0, "sin"),
        COS(1, "cos"),
        SQRT(2, "√"),
        LN(3, "ln");

        companion object {
            fun getFunctionById(functionId: Int): Function? {
                return values().find { it.functionId == functionId }
            }
        }
    }

    companion object {
        val MAX_VALUE = 10.0.pow(4)
    }
}
