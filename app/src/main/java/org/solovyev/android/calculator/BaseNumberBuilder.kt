package org.solovyev.android.calculator

import android.text.SpannableStringBuilder
import com.google.common.base.Strings
import jscl.NumeralBase
import org.solovyev.android.calculator.math.MathType

abstract class BaseNumberBuilder(
    protected val engine: Engine
) {
    protected var numberBuilder: StringBuilder? = null
    protected var nb: NumeralBase? = engine.getMathEngine().getNumeralBase()

    /**
     * Method determines if we can continue to process current number
     *
     * @param result current math type result
     * @return true if we can continue of processing of current number, if false - new number should be constructed
     */
    protected fun canContinue(result: MathType.Result): Boolean {
        val number = result.type.groupType == MathType.MathGroupType.number
        return number && !spaceBefore(result) &&
                numeralBaseCheck(result) &&
                numeralBaseInTheStart(result.type) ||
                isSignAfterE(result)
    }

    private fun spaceBefore(mathTypeResult: MathType.Result): Boolean {
        return numberBuilder == null && Strings.isNullOrEmpty(mathTypeResult.match.trim())
    }

    private fun numeralBaseInTheStart(result: MathType): Boolean {
        return result != MathType.numeral_base || numberBuilder == null
    }

    private fun numeralBaseCheck(result: MathType.Result): Boolean {
        return result.type != MathType.digit ||
               getNumeralBase().getAcceptableCharacters().contains(result.match[0])
    }

    private fun isSignAfterE(mathTypeResult: MathType.Result): Boolean {
        if (isHexMode()) {
            return false
        }
        val match = mathTypeResult.match
        if (match != "−" && match != "-" && match != "+") {
            return false
        }
        val nb = numberBuilder ?: return false
        if (nb.isEmpty()) {
            return false
        }
        return nb[nb.length - 1] == MathType.EXPONENT
    }

    fun isHexMode(): Boolean {
        return getNumeralBase() == NumeralBase.hex
    }

    protected fun getNumeralBase(): NumeralBase {
        return nb ?: engine.getMathEngine().getNumeralBase()
    }

    abstract fun process(sb: SpannableStringBuilder, result: MathType.Result): Int
}
