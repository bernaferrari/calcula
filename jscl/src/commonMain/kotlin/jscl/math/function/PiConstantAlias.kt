package jscl.math.function

import jscl.AngleUnit
import jscl.JsclMathEngine

class PiConstantAlias(
    private val alias: String
) : ExtendedConstant(Constant(alias), Math.PI, "JsclDouble.valueOf(Math.PI)") {

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
