package jscl.math

import jscl.math.function.*
import jscl.math.operator.Factorial
import jscl.math.operator.Operator
import jscl.mathml.MathML
import jscl.text.ParseException
import org.solovyev.common.math.MathEntity

abstract class Variable(
    override var name: String
) : Comparable<Any>, MathEntity {

    private var id: Int = -1
    private var system: Boolean = true

    override fun getId(): Int = id

    override fun setId(id: Int) {
        this.id = id
    }

    override fun isIdDefined(): Boolean = id >= 0

    override fun isSystem(): Boolean = system

    fun setSystem(system: Boolean) {
        this.system = system
    }

    override fun copy(that: MathEntity) {
        if (that is Variable) {
            this.name = that.name
            this.id = that.id
            this.system = that.system
        }
    }

    @Throws(NotIntegrableException::class)
    abstract fun antiDerivative(variable: Variable): Generic

    abstract fun derivative(variable: Variable): Generic

    abstract fun substitute(variable: Variable, generic: Generic): Generic

    abstract fun expand(): Generic

    abstract fun factorize(): Generic

    abstract fun elementary(): Generic

    abstract fun simplify(): Generic

    abstract fun numeric(): Generic

    open fun expressionValue(): Expression = Expression.valueOf(this)

    abstract fun isConstant(variable: Variable): Boolean

    open fun isIdentity(variable: Variable): Boolean = this.compareTo(variable) == 0

    abstract fun compareTo(variable: Variable): Int

    override fun compareTo(other: Any): Int = compareTo(other as Variable)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        return other is Variable && compareTo(other) == 0
    }

    override fun toString(): String = name

    open fun toJava(): String = name

    open fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) {
            nameToMathML(element)
        } else {
            val e1 = element.element("msup")
            nameToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    protected open fun nameToMathML(element: MathML) {
        val e1 = element.element("mi")
        e1.appendChild(element.text(if (special.containsKey(name)) special[name]!! else name))
        element.appendChild(e1)
    }

    abstract fun newInstance(): Variable

    abstract val constants: Set<Constant>

    companion object {
        @JvmField
        val comparator: Comparator<Variable> = VariableComparator.comparator

        protected val FACTORIZE_CONVERTER: (Generic) -> Generic = { it.factorize() }

        protected val ELEMENTARY_CONVERTER: (Generic) -> Generic = { it.elementary() }

        protected val EXPAND_CONVERTER: (Generic) -> Generic = { it.expand() }

        protected val NUMERIC_CONVERTER: (Generic) -> Generic = { it.numeric() }

        internal val special = mapOf(
            "Alpha" to "\u0391", "Beta" to "\u0392", "Gamma" to "\u0393", "Delta" to "\u0394",
            "Epsilon" to "\u0395", "Zeta" to "\u0396", "Eta" to "\u0397", "Theta" to "\u0398",
            "Iota" to "\u0399", "Kappa" to "\u039A", "Lambda" to "\u039B", "Mu" to "\u039C",
            "Nu" to "\u039D", "Xi" to "\u039E", "Pi" to "\u03A0", "Rho" to "\u03A1",
            "Sigma" to "\u03A3", "Tau" to "\u03A4", "Upsilon" to "\u03A5", "Phi" to "\u03A6",
            "Chi" to "\u03A7", "Psi" to "\u03A8", "Omega" to "\u03A9",
            "alpha" to "\u03B1", "beta" to "\u03B2", "gamma" to "\u03B3", "delta" to "\u03B4",
            "epsilon" to "\u03B5", "zeta" to "\u03B6", "eta" to "\u03B7", "theta" to "\u03B8",
            "iota" to "\u03B9", "kappa" to "\u03BA", "lambda" to "\u03BB", "mu" to "\u03BC",
            "nu" to "\u03BD", "xi" to "\u03BE", "pi" to "\u03C0", "rho" to "\u03C1",
            "sigma" to "\u03C3", "tau" to "\u03C4", "upsilon" to "\u03C5", "phi" to "\u03C6",
            "chi" to "\u03C7", "psi" to "\u03C8", "omega" to "\u03C9",
            "infin" to "\u221E", "nabla" to "\u2207", "aleph" to "\u2135",
            "hbar" to "\u210F", "hamilt" to "\u210B", "lagran" to "\u2112", "square" to "\u25A1"
        )

        @JvmStatic
        @Throws(ParseException::class, NotVariableException::class)
        fun valueOf(str: String): Variable = Expression.valueOf(str).variableValue()

        @JvmStatic
        @Throws(ParseException::class, NotVariableException::class)
        fun valueOf(str: Array<String>): Array<Variable> {
            val n = str.size
            val vars = Array<Variable>(n) { i -> valueOf(str[i]) }
            return vars
        }
    }
}

internal class VariableComparator private constructor() : Comparator<Variable> {

    override fun compare(o1: Variable, o2: Variable): Int = value(o1) - value(o2)

    companion object {
        @JvmField
        val comparator: Comparator<Variable> = VariableComparator()

        internal fun value(v: Variable): Int {
            return when (v) {
                is TechnicalVariable -> 0
                is IntegerVariable -> 1
                is DoubleVariable -> 2
                is Fraction -> if (v.integer()) 3 else 11
                is Sqrt -> if (v.imaginary()) 4 else 11
                is Constant -> 5
                is Root -> 6
                is Algebraic -> 7
                is ImplicitFunction -> 8
                is jscl.math.function.Function -> 9
                is Factorial -> 10
                is Operator -> 11
                is ExpressionVariable -> 12
                is VectorVariable -> 13
                is MatrixVariable -> 14
                else -> throw ArithmeticException("Forget to add compare object of type: ${v::class.simpleName}")
            }
        }
    }
}
