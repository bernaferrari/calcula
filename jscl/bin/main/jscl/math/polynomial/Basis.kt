package jscl.math.polynomial

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.Variable
import jscl.math.polynomial.groebner.Standard
import jscl.util.ArrayUtils

class Basis(
    internal val element: Array<Generic>,
    internal val factory: Polynomial
) {

    fun valueof(generic: Array<Generic>): Basis {
        return Basis(generic, factory)
    }

    fun modulo(modulo: Int): Basis {
        return Basis(element, Polynomial.factory(factory, modulo))
    }

    fun elements(): Array<Generic> {
        return element
    }

    fun ordering(): Ordering {
        return factory.ordering()
    }

    fun polynomial(generic: Generic): Polynomial {
        return factory.valueOf(generic).normalize().freeze()
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append("{")
        for (i in element.indices) {
            buffer.append(polynomial(element[i])).append(if (i < element.size - 1) ", " else "")
        }
        buffer.append("}")
        @Suppress("UNCHECKED_CAST")
        buffer.append(", ${ArrayUtils.toString(factory.monomialFactory.unknown as Array<Any>)}")
        return buffer.toString()
    }

    companion object {
        const val DATA_STRUCT = 0x3
        const val ARRAY_DECLINED = 0x0
        const val ARRAY = 0x1
        const val TREE = 0x2
        const val LIST = 0x3
        const val DEGREE = 0x4
        const val DEFINING_EQS = 0x8
        const val POWER_SIZE = 0x30
        const val POWER_32 = 0x00
        const val POWER_8 = 0x10
        const val POWER_2 = 0x20
        const val POWER_2_DEFINED = 0x30
        const val GEO_BUCKETS = 0x40
        const val ALGORITHM = 0x180
        const val BUCHBERGER = 0x000
        const val F4 = 0x080
        const val BLOCK = 0x100
        const val INSTRUMENTED = 0x200
        const val GM_SETTING = 0x400
        const val SUGAR = 0x800
        const val FUSSY = 0x1000
        const val F4_SIMPLIFY = 0x2000
        internal const val DEFAULT = GM_SETTING or SUGAR

        @JvmStatic
        fun compute(generic: Array<Generic>, unknown: Array<Variable>): Basis {
            return compute(generic, unknown, Monomial.lexicographic)
        }

        @JvmStatic
        fun compute(generic: Array<Generic>, unknown: Array<Variable>, ordering: Ordering): Basis {
            return compute(generic, unknown, ordering, 0)
        }

        @JvmStatic
        fun compute(generic: Array<Generic>, unknown: Array<Variable>, ordering: Ordering, modulo: Int): Basis {
            return compute(generic, unknown, ordering, modulo, 0)
        }

        @JvmStatic
        fun compute(generic: Array<Generic>, unknown: Array<Variable>, ordering: Ordering, modulo: Int, flags: Int): Basis {
            val adjustedFlags = flags xor DEFAULT
            return compute(generic, unknown, ordering, modulo, adjustedFlags, (adjustedFlags and DEGREE) > 0, (adjustedFlags and DEFINING_EQS) > 0)
        }

        @JvmStatic
        internal fun compute(generic: Array<Generic>, unknown: Array<Variable>, ordering: Ordering, modulo: Int, flags: Int, degree: Boolean, defining: Boolean): Basis {
            if (degree)
                return compute(compute(generic, unknown, Monomial.degreeReverseLexicographic, modulo, flags, false, defining).elements(), unknown, ordering, modulo, flags, false, defining)
            return Standard.compute(Basis(if (defining) augment(defining(unknown, modulo), generic) else generic, Polynomial.factory(unknown, ordering, modulo, flags)), flags)
        }

        @JvmStatic
        fun defining(unknown: Array<Variable>, modulo: Int): Array<Generic> {
            val a = arrayOfNulls<Generic>(unknown.size)
            for (i in unknown.indices) {
                val s = unknown[i].expressionValue()
                a[i] = s.subtract(s.pow(modulo))
            }
            @Suppress("UNCHECKED_CAST")
            return a as Array<Generic>
        }

        @JvmStatic
        fun compatible(generic: Array<Generic>): Boolean {
            return !(generic.isNotEmpty() && generic[0].compareTo(JsclInteger.valueOf(1)) == 0)
        }

        @JvmStatic
        fun augment(element: Array<Generic>, generic: Array<Generic>): Array<Generic> {
            @Suppress("UNCHECKED_CAST")
            return ArrayUtils.concat(element as Array<Any?>, generic as Array<Any?>, arrayOfNulls(element.size + generic.size)) as Array<Generic>
        }

        @JvmStatic
        fun augmentUnknown(unknown: Array<Variable>, generic: Array<Generic>): Array<Variable> {
            val va = Expression.variables(generic)
            val l = ArrayList<Variable>()
            for (anUnknown in unknown) l.add(anUnknown)
            var n = 0
            for (i in va.indices) {
                val v = va[i]
                if (!l.contains(v)) {
                    l.add(n++, v)
                }
            }
            @Suppress("UNCHECKED_CAST")
            return ArrayUtils.toArray(l, arrayOfNulls<Variable>(l.size)) as Array<Variable>
        }
    }
}
