package jscl.math.function

import jscl.math.JsclInteger

/**
 * User: serso
 * Date: 1/7/12
 * Time: 3:40 PM
 */
object Constants {
    @JvmField
    val PI = Constant("Π")
    @JvmField
    val PI_INV = Constant("π")
    @JvmField
    val I = Constant("i")
    @JvmField
    val INF = Constant("∞")
    @JvmField
    val INF_2 = Constant("Infinity")

    const val ANS = "ans"

    object Generic {
        @JvmField
        val E = Exp(JsclInteger.ONE).expressionValue()
        @JvmField
        val PI = Constants.PI.expressionValue()
        @JvmField
        val PI_INV = Constants.PI_INV.expressionValue()
        @JvmField
        val INF = Constants.INF.expressionValue()
        @JvmField
        val I = Sqrt(JsclInteger.valueOf(-1)).expressionValue()

        // i * PI
        @JvmField
        val I_BY_PI = I.multiply(PI_INV)

        // fraction = 1/2
        @JvmField
        val HALF = Inverse(JsclInteger.valueOf(2)).expressionValue()

        // fraction = 1/3
        @JvmField
        val THIRD = Inverse(JsclInteger.valueOf(3)).expressionValue()

        // -1/2 * (1 - i * sqrt (3) )
        @JvmField
        val J = HALF.negate().multiply(JsclInteger.ONE.subtract(I.multiply(Sqrt(JsclInteger.valueOf(3)).expressionValue())))

        // -1/2 * (1 + i * sqrt (3) )
        @JvmField
        val J_BAR = HALF.negate().multiply(JsclInteger.ONE.add(I.multiply(Sqrt(JsclInteger.valueOf(3)).expressionValue())))
    }
}
