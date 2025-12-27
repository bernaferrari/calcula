package org.solovyev.android.calculator

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.android.calculator.text.FromJsclSimplifyTextProcessor
import org.solovyev.android.calculator.variables.CppVariable

@RunWith(RobolectricTestRunner::class)
class FromJsclSimplifyTextProcessorTest {

    @Test
    fun testProcess() {
        val engine = Tests.makeEngine()
        val tp = FromJsclSimplifyTextProcessor(engine)
        engine.mathEngine.groupingSeparator = ' '

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("t2.718281828459045", 2).build().toJsclConstant())
        engine.variablesRegistry.addOrUpdate(CppVariable.builder("t").build().toJsclConstant())
        assertEquals("t×", tp.process("t*"))
        assertEquals("×t", tp.process("*t"))
        assertEquals("t2", tp.process("t*2"))
        assertEquals("2t", tp.process("2*t"))
        engine.variablesRegistry.addOrUpdate(CppVariable.builder("t").build().toJsclConstant())
        assertEquals("t×", tp.process("t*"))
        assertEquals("×t", tp.process("*t"))

        assertEquals("t2", tp.process("t*2"))
        assertEquals("2t", tp.process("2*t"))

        assertEquals("t^2×2", tp.process("t^2*2"))
        assertEquals("2t^2", tp.process("2*t^2"))

        assertEquals("t^[2×2t]", tp.process("t^[2*2*t]"))
        assertEquals("2t^2[2t]", tp.process("2*t^2[2*t]"))

        engine.variablesRegistry.addOrUpdate(CppVariable.builder("k").build().toJsclConstant())
        assertEquals("(t+2k)[k+2t]", tp.process("(t+2*k)*[k+2*t]"))
        assertEquals("(te+2k)e[k+2te]", tp.process("(t*e+2*k)*e*[k+2*t*e]"))


        assertEquals("tlog(3)", tp.process("t*log(3)"))
        assertEquals("t√(3)", tp.process("t*√(3)"))
        assertEquals("20x", tp.process("20*x"))
        assertEquals("20x", tp.process("20x"))
        assertEquals("2×0x3", tp.process("2*0x3"))
        assertEquals("2×0x:3", tp.process("2*0x:3"))
    }
}
