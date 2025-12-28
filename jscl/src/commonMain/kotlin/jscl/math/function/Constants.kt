package jscl.math.function

import jscl.math.JsclInteger

/**
 * User: serso
 * Date: 1/7/12
 * Time: 3:40 PM
 */
object Constants {
    val PI = Constant("Π")
    val PI_INV = Constant("π")
    val I = Constant("i")
    val INF = Constant("∞")
    val INF_2 = Constant("Infinity")

    const val ANS = "ans"

    object Generic {
        val E = Exp(JsclInteger.ONE).expressionValue()
        val PI = Constants.PI.expressionValue()
        val PI_INV = Constants.PI_INV.expressionValue()
        val INF = Constants.INF.expressionValue()
        val I = Sqrt(JsclInteger.valueOf(-1)).expressionValue()

        // i * PI
        val I_BY_PI = I.multiply(PI_INV)

        // fraction = 1/2
        val HALF = Inverse(JsclInteger.valueOf(2)).expressionValue()

        // fraction = 1/3
        val THIRD = Inverse(JsclInteger.valueOf(3)).expressionValue()

        // -1/2 * (1 - i * sqrt (3) )
        val J = HALF.negate().multiply(JsclInteger.ONE.subtract(I.multiply(Sqrt(JsclInteger.valueOf(3)).expressionValue())))

        // -1/2 * (1 + i * sqrt (3) )
        val J_BAR = HALF.negate().multiply(JsclInteger.ONE.add(I.multiply(Sqrt(JsclInteger.valueOf(3)).expressionValue())))
    }
}
