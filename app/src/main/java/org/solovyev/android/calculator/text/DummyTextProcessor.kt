package org.solovyev.android.calculator.text

import jscl.math.Generic

object DummyTextProcessor : TextProcessor<String, Generic> {
    override fun process(from: Generic): String = from.toString()
}
