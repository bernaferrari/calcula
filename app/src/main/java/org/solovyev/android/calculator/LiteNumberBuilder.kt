package org.solovyev.android.calculator

import android.text.SpannableStringBuilder
import jscl.NumeralBase
import org.solovyev.android.calculator.math.MathType

class LiteNumberBuilder(engine: Engine) : BaseNumberBuilder(engine) {

    override fun process(sb: SpannableStringBuilder, result: MathType.Result): Int {
        process(result)
        return 0
    }

    fun process(result: MathType.Result) {
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

        } else {
            // process current number (and go to the next one)
            if (numberBuilder != null) {
                numberBuilder = null

                // must set default numeral base (exit numeral base mode)
                nb = engine.getMathEngine().getNumeralBase()
            }
        }
    }
}
