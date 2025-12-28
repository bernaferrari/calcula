package jscl.math.function

import jscl.AngleUnit
import jscl.math.Generic
import jscl.math.Variable

/**
 * User: serso
 * Date: 11/14/11
 * Time: 1:40 PM
 */
class Rad(degrees: Generic?, minutes: Generic?, seconds: Generic?) : AbstractDms("rad", degrees, minutes, seconds) {

    override val constants: Set<Constant>
        get() = parameters?.flatMap { it.constants }?.toSet() ?: emptySet()

    override fun selfNumeric(): Generic {
        return AngleUnit.deg.transform(AngleUnit.rad, super.selfNumeric())
    }

    override fun newInstance(): Variable {
        return Rad(null, null, null)
    }
}
