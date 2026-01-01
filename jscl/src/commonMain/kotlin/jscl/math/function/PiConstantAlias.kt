package jscl.math.function

import jscl.AngleUnit
import jscl.JsclMathEngine

class PiConstantAlias(
    private val alias: String
) : ExtendedConstant(Constant(alias), kotlin.math.PI, "JsclDouble.valueOf(kotlin.math.PI)") {

    override val name: String
        get() = alias

    override fun getDoubleValue(): Double? {
        return try {
            AngleUnit.rad.transform(JsclMathEngine.getInstance().getAngleUnits(), getValue()!!.toDouble())
        } catch (_: NumberFormatException) {
            null
        }
    }
}
