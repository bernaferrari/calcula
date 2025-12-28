package jscl

import jscl.math.JsclInteger
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger

enum class NumeralBase(
    val radix: Int,
    val groupingSize: Int
) {
    dec(10, 3) {
        private val characters = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

        override fun toDouble(doubleString: String): Double {
            return doubleString.toDouble()
        }

        override fun getJsclPrefix(): String {
            return "0d:"
        }

        override fun getAcceptableCharacters(): Set<Char> {
            return characters
        }
    },

    hex(16, 2) {
        private val characters = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

        override fun getJsclPrefix(): String {
            return "0x:"
        }

        override fun getAcceptableCharacters(): Set<Char> {
            return characters
        }
    },

    oct(8, 4) {
        private val characters = setOf('0', '1', '2', '3', '4', '5', '6', '7')

        override fun getJsclPrefix(): String {
            return "0o:"
        }

        override fun getAcceptableCharacters(): Set<Char> {
            return characters
        }
    },

    bin(2, 4) {
        private val characters = setOf('0', '1')

        override fun getJsclPrefix(): String {
            return "0b:"
        }

        override fun getAcceptableCharacters(): Set<Char> {
            return characters
        }
    };

    open fun toDouble(doubleString: String): Double {
        return Double.fromBits(doubleString.toLong(radix))
    }

    fun toInteger(integerString: String): Int {
        return integerString.toInt(radix)
    }

    fun toJsclInteger(integerString: String): JsclInteger {
        return JsclInteger(toBigInteger(integerString))
    }

    fun toBigInteger(value: String): BigInteger {
        return BigInteger.parseString(value, radix)
    }

    fun toString(value: BigInteger): String {
        return value.toString(radix).uppercase()
    }

    fun toString(value: Int): String {
        return value.toString(radix).uppercase()
    }

    abstract fun getJsclPrefix(): String

    abstract fun getAcceptableCharacters(): Set<Char>

    fun toString(value: Double, fractionDigits: Int): String {
        return toString(value, radix, fractionDigits)
    }

    companion object {
        fun getByPrefix(prefix: String): NumeralBase? {
            for (nb in values()) {
                if (prefix == nb.getJsclPrefix()) {
                    return nb
                }
            }
            return null
        }

        protected fun toString(value: Double, radix: Int, fractionDigits: Int): String {
            val mult = BigDecimal.fromLong(radix.toLong()).pow(fractionDigits)
            val bd = BigDecimal.fromDouble(value).multiply(mult)

            val bi = bd.toBigInteger()
            val result = StringBuilder(bi.toString(radix))

            while (result.length < fractionDigits + 1) {  // +1 for leading zero
                result.insert(0, "0")
            }
            result.insert(result.length - fractionDigits, ".")

            return result.toString().uppercase()
        }
    }
}
