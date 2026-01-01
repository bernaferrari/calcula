@file:Suppress("UNCHECKED_CAST")

package jscl.math.function

import jscl.CustomFunctionCalculationException
import jscl.JsclMathEngine
import jscl.NumeralBase
import jscl.math.*
import jscl.text.ParseException
import jscl.text.msg.JsclMessage
import jscl.text.msg.Messages
import jscl.common.math.MathEntity
import jscl.common.msg.MessageType
import kotlinx.atomicfu.atomic

class CustomFunction : Function, IFunction {

    private val id: Int
    private var content: Expression
    private var description: String?
    private var parameterNames: List<String> = emptyList()
    private var parameterConstants: List<ConstantData>? = null

    private constructor(
        name: String,
        parameterNames: List<String>,
        content: Expression,
        description: String?
    ) : super(name, arrayOfNulls<Generic>(parameterNames.size) as Array<Generic>?) {
        this.parameterNames = parameterNames
        this.content = content
        this.description = description
        this.id = counter.incrementAndGet()
    }

    private constructor(
        name: String,
        parameterNames: List<String>,
        content: String,
        description: String?
    ) : super(name, arrayOfNulls<Generic>(parameterNames.size) as Array<Generic>?) {
        this.parameterNames = parameterNames
        val engine = JsclMathEngine.getInstance()
        val nb = engine.getNumeralBase()
        if (nb != NumeralBase.dec) {
            // numbers in functions are only supported in decimal base
            engine.setNumeralBase(NumeralBase.dec)
        }
        try {
            this.content = Expression.valueOf(content)
            ensureNoImplicitFunctions()
        } catch (e: ParseException) {
            throw CustomFunctionCalculationException(this, e)
        } finally {
            if (nb != NumeralBase.dec) {
                engine.setNumeralBase(nb)
            }
        }
        this.description = description
        this.id = counter.incrementAndGet()
    }

    private fun ensureNoImplicitFunctions() {
        for (i in 0 until content.size()) {
            val literal = content.literal(i)
            for (j in 0 until literal.size()) {
                val variable = literal.getVariable(j)
                if (variable is ImplicitFunction) {
                    throw CustomFunctionCalculationException(this, JsclMessage(Messages.msg_13, MessageType.error, variable.name))
                }
            }
        }
    }

    private fun makeParameterConstants(names: List<String>): List<ConstantData> {
        return names.map { name -> ConstantData(name) }.toMutableList()
    }

    override fun getMinParameters(): Int {
        return parameterNames.size
    }

    override fun getMaxParameters(): Int {
        return parameterNames.size
    }

    override val constants: Set<Constant>
        get() = content.constants

    override fun substitute(variable: Variable, generic: Generic): Generic {
        return super.substitute(variable, generic)
    }

    override fun selfExpand(): Generic {
        var content: Generic = this.content
        val parameterConstants = getParameterConstants()
        for (cd in parameterConstants) {
            content = content.substitute(cd.local, cd.globalExpression)
        }
        for (i in parameterConstants.indices) {
            val cd = parameterConstants[i]
            content = content.substitute(cd.global, parameters!![i])
        }
        for (cd in parameterConstants) {
            content = content.substitute(cd.global, cd.localExpression)
        }
        return content
    }

    private fun getParameterConstants(): List<ConstantData> {
        if (parameterConstants == null) {
            parameterConstants = makeParameterConstants(parameterNames)
        }
        return parameterConstants!!
    }

    override fun copy(that: MathEntity) {
        super.copy(that)
        if (that is CustomFunction) {
            content = that.content
            parameterNames = that.parameterNames.toMutableList()
            description = that.description
        }
    }

    override fun selfElementary(): Generic {
        return selfExpand().elementary()
    }

    override fun selfSimplify(): Generic {
        return expressionValue()
    }

    override fun selfNumeric(): Generic {
        return selfExpand().numeric()
    }

    override fun antiDerivative(variable: Variable): Generic {
        return if (getParameterForAntiDerivation(variable) < 0) {
            throw NotIntegrableException(this)
        } else {
            content.antiDerivative(variable)
        }
    }

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(variable: Variable): Generic {
        var result: Generic = JsclInteger.valueOf(0)

        for (i in parameters!!.indices) {
            // chain rule: f(x) = g(h(x)) => f'(x) = g'(h(x)) * h'(x)
            // hd = h'(x)
            // gd = g'(x)
            val hd = parameters!![i].derivative(variable)
            val gd = content.derivative(variable)

            result = result.add(hd.multiply(gd))
        }

        return result
    }

    override fun derivative(n: Int): Generic {
        throw ArithmeticException()
    }

    override fun getContent(): String {
        return content.toString()
    }

    override fun getDescription(): String? {
        return description
    }

    override fun getParameterNames(): List<String> {
        return parameterNames.toList()
    }

    override fun formatUndefinedParameter(i: Int): String {
        return if (i < parameterNames.size) {
            parameterNames[i]
        } else {
            super.formatUndefinedParameter(i)
        }
    }

    override fun newInstance(): CustomFunction {
        return CustomFunction(name, parameterNames, content, description)
    }

    class Builder {
        private val system: Boolean
        private var content: String
        private var description: String?
        private var parameterNames: List<String>
        private var name: String
        private var id: Int?

        constructor(name: String, parameterNames: List<String>, content: String) {
            this.system = false
            this.content = content
            this.parameterNames = parameterNames
            this.name = name
            this.description = null
            this.id = null
        }

        constructor(function: IFunction) {
            this.system = function.isSystem()
            this.content = function.getContent()
            this.description = function.getDescription()
            this.parameterNames = function.getParameterNames().toMutableList()
            this.name = function.name
            this.id = if (function.isIdDefined()) function.getId() else null
        }

        constructor() {
            this.system = false
            this.content = ""
            this.description = null
            this.parameterNames = emptyList()
            this.name = ""
            this.id = null
        }

        constructor(system: Boolean, name: String, parameterNames: List<String>, content: String) {
            this.system = system
            this.content = content
            this.parameterNames = parameterNames
            this.name = name
            this.description = null
            this.id = null
        }

        fun setDescription(description: String?): Builder {
            this.description = description
            return this
        }

        fun setId(id: Int): Builder {
            this.id = id
            return this
        }

        fun setContent(content: String): Builder {
            this.content = content
            return this
        }

        fun setParameterNames(parameterNames: List<String>): Builder {
            this.parameterNames = parameterNames
            return this
        }

        fun setName(name: String): Builder {
            this.name = name
            return this
        }

        fun create(): CustomFunction {
            val customFunction = CustomFunction(name, parameterNames, prepareContent(content), description)
            customFunction.setSystem(system)
            if (id != null) {
                customFunction.setId(id!!)
            }
            return customFunction
        }

        companion object {
            private fun prepareContent(content: String): String {
                val result = StringBuilder(content.length)

                val groupingSeparator = JsclMathEngine.getInstance().getGroupingSeparator()

                for (i in content.indices) {
                    val ch = content[i]
                    when (ch) {
                        ' ', '\'', '\n', '\r' -> {
                            // do nothing
                        }
                        else -> {
                            // remove grouping separator
                            if (ch != groupingSeparator) {
                                result.append(ch)
                            }
                        }
                    }
                }

                return result.toString()
            }
        }
    }

    private inner class ConstantData(name: String) {
        val global: Constant = Constant(name + "#" + id)
        val globalExpression: Generic = Expression.valueOf(global)
        val local: Constant = Constant(name)
        val localExpression: Generic = Expression.valueOf(local)
    }

    companion object {
        private val counter = atomic(0)
    }
}
