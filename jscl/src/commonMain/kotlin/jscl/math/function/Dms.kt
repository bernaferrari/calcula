package jscl.math.function

import jscl.math.Generic
import jscl.math.Variable

/**
 * User: serso
 * Date: 11/14/11
 * Time: 1:44 PM
 */
class Dms(degrees: Generic?, minutes: Generic?, seconds: Generic?) : AbstractDms("dms", degrees, minutes, seconds) {

    override fun newInstance(): Variable {
        return Dms(null, null, null)
    }

    override fun formatUndefinedParameter(i: Int): String {
        return when (i) {
            0 -> "d"
            1 -> "m"
            2 -> "s"
            else -> super.formatUndefinedParameter(i)
        }
    }

    override val constants: Set<Constant>
        get() {
            val result = HashSet<Constant>()
            for (parameter in parameters!!) {
                result.addAll(parameter.constants)
            }
            return result
        }
}
