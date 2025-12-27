package jscl.math.function

import jscl.AngleUnit
import jscl.JsclMathEngine

/**
 * User: serso
 * Date: 11/29/11
 * Time: 11:28 AM
 */
class PiConstant : ExtendedConstant(Constants.PI, Math.PI, "JsclDouble.valueOf(Math.PI)") {

    override val name: String
        get() = Constants.PI.name

    override fun getDoubleValue(): Double? {
        var result: Double? = null

        try {
            result = AngleUnit.rad.transform(JsclMathEngine.getInstance().getAngleUnits(), getValue()!!.toDouble())
        } catch (e: NumberFormatException) {
            // do nothing - string is not a double
        }

        return result
    }
}
