@file:Suppress("UNCHECKED_CAST")

package jscl.math.operator

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.Variable
import jscl.math.function.Constant
import jscl.mathml.MathML
import jscl.util.ArrayComparator

/**
 * User: serso
 * Date: 11/29/11
 * Time: 9:50 PM
 */
abstract class AbstractFunction protected constructor(name: String, parameters: Array<Generic>?) :
    Variable(name) {

    internal var parameters: Array<Generic>? = null

    init {
        // Note: checkParameters is not called here because subclass fields (like parameterNames)
        // are not yet initialized when this init block runs. The check happens in setParameters().
        this.parameters = parameters
    }

    private fun checkParameters(parameters: Array<Generic>?) {
        // Only check if parameters is not null (null is used for template functions)
        if (parameters != null) {
            assert(getMinParameters() <= parameters.size && parameters.size <= getMaxParameters())
        }
    }

    fun getParameters(): Array<Generic>? {
        return parameters
    }

    fun setParameters(parameters: Array<Generic>?) {
        checkParameters(parameters)
        this.parameters = parameters
    }

    abstract fun getMinParameters(): Int

    open fun getMaxParameters(): Int {
        return getMinParameters()
    }

    abstract fun selfExpand(): Generic

    override fun expand(): Generic {
        val function = newExpandedFunction()
        return function.selfExpand()
    }

    protected fun newExpandedFunction(): AbstractFunction {
        val function = newInstance() as AbstractFunction
        val params = parameters!!
        // Allocate array if newInstance() returned with null parameters
        if (function.parameters == null) {
            function.parameters = arrayOfNulls<Generic>(params.size) as Array<Generic>
        }
        for (i in params.indices) {
            function.parameters!![i] = params[i].expand()
        }
        return function
    }

    override fun elementary(): Generic {
        val function = newElementarizedFunction()
        return function.selfElementary()
    }

    protected fun newElementarizedFunction(): AbstractFunction {
        val function = newInstance() as AbstractFunction
        val params = parameters!!
        if (function.parameters == null) {
            function.parameters = arrayOfNulls<Generic>(params.size) as Array<Generic>
        }
        for (i in params.indices) {
            function.parameters!![i] = params[i].elementary()
        }
        return function
    }

    abstract fun selfElementary(): Generic

    override fun factorize(): Generic {
        val function = newFactorizedFunction()
        return function.expressionValue()
    }

    protected fun newFactorizedFunction(): AbstractFunction {
        val function = newInstance() as AbstractFunction
        val params = parameters!!
        if (function.parameters == null) {
            function.parameters = arrayOfNulls<Generic>(params.size) as Array<Generic>
        }
        for (i in params.indices) {
            function.parameters!![i] = params[i].factorize()
        }
        return function
    }

    override fun simplify(): Generic {
        val function = newSimplifiedFunction()
        return function.selfSimplify()
    }

    protected fun newSimplifiedFunction(): AbstractFunction {
        val function = newInstance() as AbstractFunction
        val params = parameters!!
        if (function.parameters == null) {
            function.parameters = arrayOfNulls<Generic>(params.size) as Array<Generic>
        }
        for (i in params.indices) {
            function.parameters!![i] = params[i].simplify()
        }
        return function
    }

    abstract fun selfSimplify(): Generic

    override fun numeric(): Generic {
        val result = newNumericFunction()
        return result.selfNumeric()
    }

    protected fun newNumericFunction(): AbstractFunction {
        val result = newInstance() as AbstractFunction
        val params = parameters!!
        if (result.parameters == null) {
            result.parameters = arrayOfNulls<Generic>(params.size) as Array<Generic>
        }
        for (i in params.indices) {
            result.parameters!![i] = params[i].numeric()
        }
        return result
    }

    abstract fun selfNumeric(): Generic

    override fun toString(): String {
        val result = StringBuilder()

        // f(x, y, z)
        result.append(name)
        result.append("(")
        val params = parameters
        val size = params?.size ?: getMinParameters()
        for (i in 0 until size) {
            result.append(formatParameter(i))
            if (i < size - 1) {
                result.append(", ")
            }
        }
        result.append(")")

        return result.toString()
    }

    protected fun formatParameter(i: Int): String {
        val params = parameters
        val parameter = if (params != null && i < params.size) params[i] else null

        return if (parameter != null) {
            parameter.toString()
        } else {
            formatUndefinedParameter(i)
        }
    }

    protected open fun formatUndefinedParameter(i: Int): String {
        return DEFAULT_PARAMETER_NAMES[i - (i / DEFAULT_PARAMETER_NAMES.length) * DEFAULT_PARAMETER_NAMES.length].toString()
    }

    override fun toJava(): String {
        val result = StringBuilder()

        result.append(parameters!![0].toJava())
        result.append(".").append(name).append("()")

        return result.toString()
    }

    override fun compareTo(variable: Variable): Int {
        if (this === variable) return 0

        var c = comparator.compare(this, variable)

        return if (c < 0) {
            -1
        } else if (c > 0) {
            1
        } else {
            val thatFunction = variable as AbstractFunction
            c = name.compareTo(thatFunction.name)
            if (c < 0) {
                -1
            } else if (c > 0) {
                1
            } else {
                @Suppress("UNCHECKED_CAST")
                ArrayComparator.comparator.compare(parameters as Array<Comparable<*>?>, thatFunction.parameters as Array<Comparable<*>?>)
            }
        }
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        val function = newInstance() as AbstractFunction
        val params = parameters!!
        if (function.parameters == null) {
            function.parameters = arrayOfNulls<Generic>(params.size) as Array<Generic>
        }
        for (i in params.indices) {
            function.parameters!![i] = params[i].substitute(variable, generic)
        }
        return if (function.isIdentity(variable)) {
            generic
        } else {
            function.selfExpand()
        }
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1

        val result: MathML
        if (exponent == 1) {
            nameToMathML(element)
        } else {
            result = element.element("msup")
            nameToMathML(result)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            result.appendChild(e2)
            element.appendChild(result)
        }

        val fenced = element.element("mfenced")
        for (parameter in parameters!!) {
            parameter.toMathML(fenced, null)
        }

        element.appendChild(fenced)
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            for (parameter in parameters!!) {
                result.addAll(parameter.constants)
            }
            return result
        }

    companion object {
        internal val UNDEFINED_PARAMETER: Generic = JsclInteger.valueOf(Long.MIN_VALUE + 1)

        private const val DEFAULT_PARAMETER_NAMES = "xyzabcdefghijklmnopqrstuvw"

        internal fun getParameter(parameters: Array<Generic>?, i: Int): Generic? {
            return if (parameters == null) null else if (parameters.size > i) parameters[i] else null
        }
    }
}
