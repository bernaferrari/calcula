package jscl.math.function

import jscl.math.*
import jscl.mathml.MathML
import jscl.util.ArrayComparator

open class Constant(name: String, private val prime: Int, private val subscripts: Array<Generic>) : Variable(name) {

    private var hashArray: Array<Any?>? = null

    constructor(name: String) : this(name, 0, emptyArray())

    override fun antiDerivative(variable: Variable): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(variable: Variable): Generic {
        return if (isIdentity(variable)) {
            JsclInteger.valueOf(1)
        } else {
            JsclInteger.valueOf(0)
        }
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        val v = newInstance() as Constant
        for (i in subscripts.indices) {
            v.subscripts[i] = subscripts[i].substitute(variable, generic)
        }
        return if (v.isIdentity(variable)) {
            generic
        } else {
            v.expressionValue()
        }
    }

    override fun expand(): Generic {
        val v = newInstance() as Constant
        for (i in subscripts.indices) {
            v.subscripts[i] = subscripts[i].expand()
        }
        return v.expressionValue()
    }

    override fun factorize(): Generic {
        val v = newInstance() as Constant
        for (i in subscripts.indices) {
            v.subscripts[i] = subscripts[i].factorize()
        }
        return v.expressionValue()
    }

    override fun elementary(): Generic {
        val v = newInstance() as Constant
        for (i in subscripts.indices) {
            v.subscripts[i] = subscripts[i].elementary()
        }
        return v.expressionValue()
    }

    override fun simplify(): Generic {
        val v = newInstance() as Constant
        for (i in subscripts.indices) {
            v.subscripts[i] = subscripts[i].simplify()
        }
        return v.expressionValue()
    }

    override fun numeric(): Generic {
        return NumericWrapper(this)
    }

    override fun isConstant(variable: Variable): Boolean {
        return !isIdentity(variable)
    }

    override fun compareTo(other: Variable): Int {
        if (this === other) {
            return 0
        }

        var c = comparator.compare(this, other)
        if (c == 0) {
            val that = other as Constant
            c = name.compareTo(that.name)
            if (c == 0) {
                @Suppress("UNCHECKED_CAST")
                c = ArrayComparator.comparator.compare(subscripts as Array<Comparable<*>?>, that.subscripts as Array<Comparable<*>?>)
                if (c == 0) {
                    return when {
                        prime < that.prime -> -1
                        prime > that.prime -> 1
                        else -> 0
                    }
                } else {
                    return c
                }
            } else {
                return c
            }
        } else {
            return c
        }
    }

    override fun hashCode(): Int {
        val hashArray = getHashArray()
        hashArray[0] = "Constant"
        hashArray[1] = name
        hashArray[2] = subscripts
        hashArray[3] = prime
        return this.hashArray.contentDeepHashCode()
    }

    private fun getHashArray(): Array<Any?> {
        if (hashArray == null) {
            hashArray = arrayOfNulls(4)
        }
        return hashArray!!
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append(name)

        for (subscript in subscripts) {
            result.append("[").append(subscript).append("]")
        }

        if (prime != 0) {
            if (prime <= PRIME_CHARS) result.append(primeChars(prime))
            else result.append("{").append(prime).append("}")
        }

        return result.toString()
    }

    override fun toJava(): String {
        val constantFromRegistry = ConstantsRegistry.getInstance().get(name)

        if (constantFromRegistry != null) {
            return constantFromRegistry.toJava()
        }

        val result = StringBuilder()
        result.append(name)

        if (prime != 0) {
            if (prime <= PRIME_CHARS) result.append(underscores(prime))
            else result.append("_").append(prime)
        }

        for (subscript in subscripts) {
            result.append("[").append(subscript.integerValue().toInt()).append("]")
        }
        return result.toString()
    }

    override fun toMathML(element: MathML, data: Any?) {
        val exponent = if (data is Int) data else 1
        if (exponent == 1) bodyToMathML(element)
        else {
            val e1 = element.element("msup")
            bodyToMathML(e1)
            val e2 = element.element("mn")
            e2.appendChild(element.text(exponent.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    fun bodyToMathML(element: MathML) {
        if (subscripts.isEmpty()) {
            if (prime == 0) {
                nameToMathML(element)
            } else {
                val e1 = element.element("msup")
                nameToMathML(e1)
                primeToMathML(e1)
                element.appendChild(e1)
            }
        } else {
            if (prime == 0) {
                val e1 = element.element("msub")
                nameToMathML(e1)
                val e2 = element.element("mrow")
                for (i in subscripts.indices) {
                    subscripts[i].toMathML(e2, null)
                }
                e1.appendChild(e2)
                element.appendChild(e1)
            } else {
                val e1 = element.element("msubsup")
                nameToMathML(e1)
                val e2 = element.element("mrow")
                for (i in subscripts.indices) {
                    subscripts[i].toMathML(e2, null)
                }
                e1.appendChild(e2)
                primeToMathML(e1)
                element.appendChild(e1)
            }
        }
    }

    internal fun primeToMathML(element: MathML) {
        if (prime <= PRIME_CHARS) {
            primeCharsToMathML(element, prime)
        } else {
            val e1 = element.element("mfenced")
            val e2 = element.element("mn")
            e2.appendChild(element.text(prime.toString()))
            e1.appendChild(e2)
            element.appendChild(e1)
        }
    }

    override fun newInstance(): Variable {
        return Constant(name, prime, Array(subscripts.size) { JsclInteger.valueOf(0) })
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            result.add(this)
            return result
        }

    fun prime(): Int = prime

    fun subscript(): Array<Generic> = subscripts

    companion object {
        const val PRIME_CHARS = 3

        @JvmStatic
        internal fun primeChars(n: Int): String {
            val buffer = StringBuilder()
            for (i in 0 until n) buffer.append("'")
            return buffer.toString()
        }

        @JvmStatic
        internal fun underscores(n: Int): String {
            val buffer = StringBuilder()
            for (i in 0 until n) buffer.append("_")
            return buffer.toString()
        }

        @JvmStatic
        internal fun primeCharsToMathML(element: MathML, n: Int) {
            val e1 = element.element("mo")
            for (i in 0 until n) e1.appendChild(element.text("\u2032"))
            element.appendChild(e1)
        }
    }
}
