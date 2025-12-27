package org.solovyev.android.calculator.jscl

import jscl.MathEngine
import jscl.math.Generic
import jscl.text.ParseException
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.text.DummyTextProcessor
import org.solovyev.android.calculator.text.FromJsclSimplifyTextProcessor
import org.solovyev.android.calculator.text.TextProcessor

enum class JsclOperation {
    simplify {
        override fun makeFromProcessor(engine: Engine): TextProcessor<String, Generic> {
            return FromJsclSimplifyTextProcessor(engine)
        }
    },
    elementary {
        override fun makeFromProcessor(engine: Engine): TextProcessor<String, Generic> {
            return DummyTextProcessor
        }
    },
    numeric {
        override fun makeFromProcessor(engine: Engine): TextProcessor<String, Generic> {
            return FromJsclNumericTextProcessor.instance
        }
    };

    private var fromProcessor: TextProcessor<String, Generic>? = null

    protected abstract fun makeFromProcessor(engine: Engine): TextProcessor<String, Generic>

    fun getFromProcessor(engine: Engine): TextProcessor<String, Generic> {
        if (fromProcessor == null) {
            fromProcessor = makeFromProcessor(engine)
        }
        return fromProcessor!!
    }

    @Throws(ParseException::class)
    fun evaluate(expression: String, engine: MathEngine): String {
        return when (this) {
            simplify -> engine.simplify(expression)
            elementary -> engine.elementary(expression)
            numeric -> engine.evaluate(expression)
        }
    }

    @Throws(ParseException::class)
    fun evaluateGeneric(expression: String, engine: MathEngine): Generic {
        return when (this) {
            simplify -> engine.simplifyGeneric(expression)
            elementary -> engine.elementaryGeneric(expression)
            numeric -> engine.evaluateGeneric(expression)
        }
    }
}
