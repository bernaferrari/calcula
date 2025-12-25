package jscl

import jscl.math.Expression
import jscl.math.Generic
import jscl.math.function.Constants
import jscl.math.function.ConstantsRegistry
import jscl.math.function.Function
import jscl.math.function.FunctionsRegistry
import jscl.math.function.IConstant
import jscl.math.function.PostfixFunctionsRegistry
import jscl.math.operator.Operator
import jscl.math.operator.matrix.OperatorsRegistry
import jscl.math.operator.Percent
import jscl.math.operator.Rand
import jscl.text.ParseException
import midpcalc.Real
import org.solovyev.common.NumberFormatter
import org.solovyev.common.math.MathRegistry
import org.solovyev.common.msg.MessageRegistry
import org.solovyev.common.msg.Messages
import com.ionspin.kotlin.bignum.integer.BigInteger

open class JsclMathEngine : MathEngine {

    private val numberFormatter = ThreadLocal.withInitial { NumberFormatter() }

    private var groupingSeparator: Char = NumberFormatter.NO_GROUPING
    private var notation: Int = Real.NumberFormat.FSE_NONE
    private var precision: Int = NumberFormatter.MAX_PRECISION

    internal var angleUnits: AngleUnit = DEFAULT_ANGLE_UNITS

    private var numeralBase: NumeralBase = DEFAULT_NUMERAL_BASE

    internal var messageRegistry: MessageRegistry = Messages.synchronizedMessageRegistry(FixedCapacityListMessageRegistry(10))

    override fun evaluate(expression: String): String {
        return evaluateGeneric(expression).toString()
    }

    override fun simplify(expression: String): String {
        return simplifyGeneric(expression).toString()
    }

    override fun elementary(expression: String): String {
        return elementaryGeneric(expression).toString()
    }

    override fun evaluateGeneric(expression: String): Generic {
        return if (expression.contains(Percent.NAME) || expression.contains(Rand.NAME)) {
            Expression.valueOf(expression).numeric()
        } else {
            Expression.valueOf(expression).expand().numeric()
        }
    }

    override fun simplifyGeneric(expression: String): Generic {
        return if (expression.contains(Percent.NAME) || expression.contains(Rand.NAME)) {
            Expression.valueOf(expression)
        } else {
            Expression.valueOf(expression).expand().simplify()
        }
    }

    override fun elementaryGeneric(expression: String): Generic {
        return Expression.valueOf(expression).elementary()
    }

    override fun getFunctionsRegistry(): MathRegistry<Function> {
        return FunctionsRegistry.lazyInstance()
    }

    override fun getOperatorsRegistry(): MathRegistry<Operator> {
        return OperatorsRegistry.lazyInstance()
    }

    override fun getPostfixFunctionsRegistry(): MathRegistry<Operator> {
        return PostfixFunctionsRegistry.lazyInstance()
    }

    override fun getAngleUnits(): AngleUnit {
        return angleUnits
    }

    override fun setAngleUnits(angleUnits: AngleUnit) {
        this.angleUnits = angleUnits
    }

    override fun getNumeralBase(): NumeralBase {
        return numeralBase
    }

    override fun setNumeralBase(numeralBase: NumeralBase) {
        this.numeralBase = numeralBase
    }

    override fun getConstantsRegistry(): MathRegistry<IConstant> {
        return ConstantsRegistry.lazyInstance()
    }

    override fun format(value: Double): String {
        return format(value, numeralBase)
    }

    override fun format(value: Double, nb: NumeralBase): String {
        if (value.isInfinite()) {
            return formatInfinity(value)
        }
        if (value.isNaN()) {
            return value.toString()
        }
        if (nb == NumeralBase.dec) {
            if (value == 0.0) {
                return "0"
            }
            val constant = findConstant(value)
            if (constant != null) {
                return constant.name
            }
        }
        return prepareNumberFormatter(nb).format(value, nb.radix).toString()
    }

    private fun prepareNumberFormatter(nb: NumeralBase): NumberFormatter {
        val nf = numberFormatter.get()!!
        nf.groupingSeparator = if (hasGroupingSeparator()) getGroupingSeparator(nb) else NumberFormatter.NO_GROUPING
        nf.precision = precision
        when (notation) {
            Real.NumberFormat.FSE_ENG -> nf.useEngineeringFormat(NumberFormatter.DEFAULT_MAGNITUDE)
            Real.NumberFormat.FSE_SCI -> nf.useScientificFormat(NumberFormatter.DEFAULT_MAGNITUDE)
            else -> nf.useSimpleFormat()
        }
        return nf
    }

    override fun format(value: BigInteger): String {
        return format(value, numeralBase)
    }

    fun format(value: BigInteger, nb: NumeralBase): String {
        if (nb == NumeralBase.dec) {
            if (BigInteger.ZERO == value) {
                return "0"
            }
        }
        return prepareNumberFormatter(nb).format(value, nb.radix).toString()
    }

    private fun findConstant(value: Double): IConstant? {
        val constants = ConstantsRegistry.getInstance()
        val constant = findConstant(constants.getSystemEntities(), value)
        if (constant != null) {
            return constant
        }
        val piInv = constants.get(Constants.PI_INV.name)
        if (piInv != null) {
            val piInvValue = piInv.getDoubleValue()
            if (piInvValue != null && piInvValue == value) {
                return piInv
            }
        }
        return null
    }

    private fun formatInfinity(value: Double): String {
        return if (value >= 0) {
            Constants.INF.name
        } else {
            Constants.INF.expressionValue().negate().toString()
        }
    }

    private fun findConstant(constants: List<IConstant>, value: Double): IConstant? {
        for (i in constants.indices) {
            val constant = constants[i]
            if (value != constant.getDoubleValue()) {
                continue
            }
            val name = constant.name
            if (name == Constants.PI_INV.name || name == Constants.ANS) {
                continue
            }
            if (name != Constants.PI.name || angleUnits == AngleUnit.rad) {
                return constant
            }
        }
        return null
    }

    override fun getMessageRegistry(): MessageRegistry {
        return messageRegistry
    }

    override fun setMessageRegistry(messageRegistry: MessageRegistry) {
        this.messageRegistry = messageRegistry
    }

    override fun format(value: String, nb: NumeralBase): String {
        if (!hasGroupingSeparator()) {
            return value
        }
        val dot = value.indexOf('.')
        if (dot >= 0) {
            val intPart = if (dot != 0) insertSeparators(value.substring(0, dot), nb) else ""
            return intPart + value.substring(dot)
        }
        val e = if (nb == NumeralBase.hex) -1 else value.indexOf('E')
        if (e >= 0) {
            val intPart = if (e != 0) insertSeparators(value.substring(0, e), nb) else ""
            return intPart + value.substring(e)
        }
        return insertSeparators(value, nb)
    }

    fun insertSeparators(value: String, nb: NumeralBase): String {
        val separator = getGroupingSeparator(nb)
        val result = StringBuilder(value.length + nb.groupingSize)
        for (i in value.length - 1 downTo 0) {
            result.append(value[i])
            if (i != 0 && (value.length - i) % nb.groupingSize == 0) {
                result.append(separator)
            }
        }
        return result.reverse().toString()
    }

    private fun hasGroupingSeparator(): Boolean {
        return groupingSeparator != NumberFormatter.NO_GROUPING
    }

    private fun getGroupingSeparator(nb: NumeralBase): Char {
        return if (nb == NumeralBase.dec) groupingSeparator else ' '
    }

    override fun setPrecision(precision: Int) {
        this.precision = precision
    }

    override fun setNotation(notation: Int) {
        if (notation != Real.NumberFormat.FSE_SCI &&
            notation != Real.NumberFormat.FSE_ENG &&
            notation != Real.NumberFormat.FSE_NONE) {
            throw IllegalArgumentException("Unsupported notation: $notation")
        }
        this.notation = notation
    }

    fun getGroupingSeparator(): Char {
        return groupingSeparator
    }

    override fun setGroupingSeparator(separator: Char) {
        this.groupingSeparator = separator
    }

    companion object {
        @JvmField
        val DEFAULT_ANGLE_UNITS: AngleUnit = AngleUnit.deg

        @JvmField
        val DEFAULT_NUMERAL_BASE: NumeralBase = NumeralBase.dec

        const val GROUPING_SEPARATOR_DEFAULT: Char = ' '

        @JvmStatic
        private var instance: JsclMathEngine = JsclMathEngine()

        @JvmStatic
        fun getInstance(): JsclMathEngine {
            return instance
        }
    }
}
