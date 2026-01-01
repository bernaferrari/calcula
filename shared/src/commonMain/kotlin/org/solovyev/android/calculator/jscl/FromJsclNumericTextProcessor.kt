package org.solovyev.android.calculator.jscl

import jscl.math.Generic
import org.solovyev.android.calculator.text.TextProcessor

class FromJsclNumericTextProcessor private constructor() : TextProcessor<String, Generic> {

    override fun process(from: Generic): String {
        return from.toString().replace("*", "")
    }

    companion object {
        // @JvmField not needed for KMP logic usually, but harmless
        val instance = FromJsclNumericTextProcessor()
    }
}
