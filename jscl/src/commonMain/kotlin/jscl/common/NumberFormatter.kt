package jscl.common

import jscl.math.precision.Real
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class NumberFormatter {

    companion object {
        const val NO_GROUPING = '\u0000'
        const val NO_ROUNDING = -1
        const val DEFAULT_MAGNITUDE = 5
        const val MIN_PRECISION = 1
        const val MAX_PRECISION = 15
        const val ENG_PRECISION = 10

        // Constants from Real.NumberFormat for local use
        private val FSE_NONE = Real.NumberFormat.FSE_NONE
        private val FSE_FIX = Real.NumberFormat.FSE_FIX
        private val FSE_SCI = Real.NumberFormat.FSE_SCI
        private val FSE_ENG = Real.NumberFormat.FSE_ENG
    }

    private val numberFormat = Real.NumberFormat()
    private val real = Real()
    private var format = FSE_NONE
    private var simpleFormatMagnitude = DEFAULT_MAGNITUDE
    var precision = MAX_PRECISION
    var groupingSeparator: Char = NO_GROUPING

    fun useScientificFormat(simpleFormatMagnitude: Int) {
        this.format = FSE_SCI
        this.simpleFormatMagnitude = simpleFormatMagnitude
    }

    fun useEngineeringFormat(simpleFormatMagnitude: Int) {
        this.format = FSE_ENG
        this.simpleFormatMagnitude = simpleFormatMagnitude
    }

    fun useSimpleFormat() {
        this.format = FSE_NONE
        this.simpleFormatMagnitude = DEFAULT_MAGNITUDE
    }


    fun format(value: Double): CharSequence {
        return format(value, 10)
    }

    fun format(value: BigInteger): CharSequence {
        return format(value, 10)
    }

    fun format(value: Double, radix: Int): CharSequence {
        checkRadix(radix)
        var processedValue = value
        var absValue = kotlin.math.abs(value)
        val simpleFormat = useSimpleFormat(radix, absValue)

        var precision = effectivePrecision()
        if (simpleFormat) {
            precision += 1
            val newScale = max(1, (precision * max(1f, radix / 10f)).toInt() - 1)
            processedValue = BigDecimal.fromDouble(value).roundToDigitPositionAfterDecimalPoint(newScale.toLong(), RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false)
            absValue = kotlin.math.abs(processedValue)
        }
        if (simpleFormat) {
            numberFormat.fse = FSE_FIX
        } else if (format == FSE_NONE) {
            // originally, a simple format was requested but we have to use something more appropriate, f.e. scientific
            // format
            numberFormat.fse = FSE_SCI
        } else {
            numberFormat.fse = format
        }
        numberFormat.thousand = groupingSeparator
        numberFormat.precision = precision
        numberFormat.base = radix
        numberFormat.maxwidth = if (simpleFormat) 100 else 30

        if (radix == 2 && processedValue < 0) {
            return "-" + prepare(absValue)
        }
        return prepare(processedValue)
    }

    private fun effectivePrecision(): Int {
        return if (precision == NO_ROUNDING) MAX_PRECISION else precision
    }

    fun format(value: BigInteger, radix: Int): CharSequence {
        checkRadix(radix)
        val absValue = value.abs()
        val simpleFormat = useSimpleFormat(radix, absValue)

        if (simpleFormat) {
            numberFormat.fse = FSE_FIX
        } else if (format == FSE_NONE) {
            // originally, a simple format was requested but we have to use something more appropriate, f.e. scientific
            // format
            numberFormat.fse = FSE_SCI
        } else {
            numberFormat.fse = format
        }
        numberFormat.thousand = groupingSeparator
        numberFormat.precision = max(0, min(precision, MAX_PRECISION))
        numberFormat.base = radix
        numberFormat.maxwidth = if (simpleFormat) 100 else 30

        if (radix == 2 && value.compareTo(BigInteger.ZERO) < 0) {
            return "-" + prepare(absValue)
        }
        return prepare(value)
    }

    private fun checkRadix(radix: Int) {
        if (radix != 2 && radix != 8 && radix != 10 && radix != 16) {
            throw IllegalArgumentException("Unsupported radix: $radix")
        }
    }

    private fun useSimpleFormat(radix: Int, absValue: Double): Boolean {
        if (radix != 10) {
            return true
        }
        if (format == FSE_NONE) {
            // simple format should be used only if rounding is on or if number is big enough
            val round = precision != NO_ROUNDING
            return round || absValue >= 10.0.pow(-MAX_PRECISION)
        }
        if (10.0.pow(-simpleFormatMagnitude) <= absValue && absValue < 10.0.pow(simpleFormatMagnitude)) {
            return true
        }
        return false
    }

    private fun useSimpleFormat(radix: Int, absValue: BigInteger): Boolean {
        if (radix != 10) {
            return true
        }
        if (format == FSE_NONE) {
            return true
        }
        if (absValue.compareTo(BigInteger.fromLong(10.0.pow(simpleFormatMagnitude).toLong())) < 0) {
            return true
        }
        return false
    }

    private fun prepare(value: Double): CharSequence {
        return stripZeros(realFormat(value)).replace('e', 'E')
    }

    private fun prepare(value: BigInteger): CharSequence {
        return stripZeros(realFormat(value)).replace('e', 'E')
    }

    private fun realFormat(value: Double): String {
        real.assign(value.toString())
        return real.toString(numberFormat)
    }

    private fun realFormat(value: BigInteger): String {
        real.assign(value.toString())
        return real.toString(numberFormat)
    }

    private fun stripZeros(s: String): String {
        var dot = -1
        var firstNonZero = -1
        for (i in s.indices) {
            val c = s[i]
            if (c != '0' && c != groupingSeparator && firstNonZero == -1) {
                firstNonZero = i
            }
            if (c == '.') {
                dot = i
                break
            }
        }
        if (firstNonZero == -1) {
            // all zeros
            return ""
        }
        if (dot < 0) {
            // no dot - no trailing zeros
            return s.substring(firstNonZero)
        }
        if (firstNonZero == dot) {
            // one zero before dot must be kept
            firstNonZero--
        }
        val e = s.lastIndexOf('e')
        val i = findLastNonZero(s, e)
        val end = if (i == dot) i else i + 1
        return s.substring(firstNonZero, end) + getExponent(s, e)
    }

    private fun getExponent(s: String, e: Int): String {
        var exponent = ""
        if (e > 0) {
            exponent = s.substring(e)
            if (exponent.length == 2 && exponent[1] == '0') {
                exponent = ""
            }
        }
        return exponent
    }

    private fun findLastNonZero(s: String, e: Int): Int {
        var i = if (e > 0) e - 1 else s.length - 1
        while (i >= 0) {
            if (s[i] != '0') {
                break
            }
            i--
        }
        return i
    }
}
