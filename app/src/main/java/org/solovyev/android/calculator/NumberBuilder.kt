package org.solovyev.android.calculator

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import jscl.MathContext
import jscl.MathEngine
import jscl.NumeralBase
import jscl.text.DoubleParser
import jscl.text.JsclIntegerParser
import jscl.text.ParseException
import jscl.text.Parser
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.text.NumberSpan

class NumberBuilder(engine: Engine) : BaseNumberBuilder(engine) {

    /**
     * Method replaces number in text according to some rules (e.g. formatting)
     *
     * @param sb     text where number can be replaced
     * @param result math type result of current token
     * @return offset between new number length and old number length (newNumberLength - oldNumberLength)
     */
    override fun process(sb: SpannableStringBuilder, result: MathType.Result): Int {
        if (canContinue(result)) {
            // let's continue building number
            if (numberBuilder == null) {
                // if new number => create new builder
                numberBuilder = StringBuilder()
            }

            if (result.type != MathType.numeral_base) {
                // just add matching string
                numberBuilder!!.append(result.match)
            } else {
                // set explicitly numeral base (do not include it into number)
                nb = NumeralBase.getByPrefix(result.match)
            }
            return 0
        } else {
            // process current number (and go to the next one)
            val offset = processNumber(sb)
            if (result.type == MathType.numeral_base) {
                // if current token is numeral base - update current numeral base
                nb = NumeralBase.getByPrefix(result.match)
            }
            return offset
        }
    }

    /**
     * Method replaces number in text according to some rules (e.g. formatting)
     *
     * @param sb text where number can be replaced
     * @return offset between new number length and old number length (newNumberLength - oldNumberLength)
     */
    fun processNumber(sb: SpannableStringBuilder): Int {
        // total number of trimmed chars
        var trimmedChars = 0

        var number: String? = null

        // toXml numeral base (as later it might be replaced)
        val localNb = getNumeralBase()

        if (numberBuilder != null) {
            try {
                number = numberBuilder.toString()

                // let's get rid of unnecessary characters (grouping separators, + after E)
                val tokens = mutableListOf<String>()
                tokens.addAll(MathType.grouping_separator.getTokens(engine))
                // + after E can be omitted: 10+E = 10E (NOTE: - cannot be omitted )
                tokens.add("+")
                for (groupingSeparator in tokens) {
                    val trimmedNumber = number!!.replace(groupingSeparator, "")
                    trimmedChars += number.length - trimmedNumber.length
                    number = trimmedNumber
                }

                // check if number still valid
                toDouble(number, getNumeralBase(), engine.getMathEngine())

            } catch (e: NumberFormatException) {
                // number is not valid => stop
                number = null
            }

            numberBuilder = null

            // must set default numeral base (exit numeral base mode)
            nb = engine.getMathEngine().getNumeralBase()
        }

        return replaceNumberInText(sb, number, trimmedChars, localNb, engine.getMathEngine())
    }

    companion object {
        private fun replaceNumberInText(
            sb: SpannableStringBuilder,
            oldNumber: String?,
            trimmedChars: Int,
            nb: NumeralBase,
            engine: MathEngine
        ): Int {
            if (oldNumber == null) {
                sb.setSpan(NumberSpan(nb), sb.length, sb.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                return 0
            }
            // in any case remove old number from text
            val oldNumberLength = oldNumber.length + trimmedChars
            sb.delete(sb.length - oldNumberLength, sb.length)

            val newNumber = SpannableString(engine.format(oldNumber, nb))
            newNumber.setSpan(NumberSpan(nb), 0, newNumber.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            sb.append(newNumber)
            // offset between old number and new number
            return newNumber.length - oldNumberLength
        }

        @Throws(NumberFormatException::class)
        private fun toDouble(s: String, nb: NumeralBase, mc: MathContext): Double {
            val defaultNb = mc.getNumeralBase()
            try {
                mc.setNumeralBase(nb)

                val p = Parser.Parameters.get(s)
                try {
                    return JsclIntegerParser.parser.parse(p, null).content().doubleValue()
                } catch (e: ParseException) {
                    p.exceptionsPool.release(e)
                    try {
                        p.reset()
                        return DoubleParser.parser.parse(p, null).content().doubleValue()
                    } catch (e1: ParseException) {
                        p.exceptionsPool.release(e1)
                        throw NumberFormatException()
                    }
                }
            } finally {
                mc.setNumeralBase(defaultNb)
            }
        }
    }
}
