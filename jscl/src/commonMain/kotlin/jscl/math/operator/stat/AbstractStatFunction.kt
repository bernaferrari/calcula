package jscl.math.operator.stat

import jscl.math.Generic
import jscl.math.operator.Operator

/**
 * User: serso
 * Date: 1/15/12
 * Time: 4:59 PM
 */
abstract class AbstractStatFunction protected constructor(name: String, parameters: Array<Generic>?) :
    Operator(name, parameters) {

    final override fun numeric(): Generic {
        for (i in parameters!!.indices) {
            parameters!![i] = parameters!![i].expand()
        }

        return selfNumeric()
    }
}
