package jscl.math.function

import org.solovyev.common.math.AbstractMathRegistry
import org.solovyev.common.math.MathRegistry

class ConstantsRegistry : AbstractMathRegistry<IConstant>() {

    override fun onInit() {
        add(PiConstant())
        add(ExtendedConstant(Constants.PI_INV, Math.PI, null))
        add(ExtendedConstant(Constants.INF, Double.POSITIVE_INFINITY, "JsclDouble.valueOf(Double.POSITIVE_INFINITY)"))
        add(ExtendedConstant(Constants.INF_2, Double.POSITIVE_INFINITY, "JsclDouble.valueOf(Double.POSITIVE_INFINITY)"))
        add(ExtendedConstant(Constants.I, "√(-1)", null))
        add(ExtendedConstant(Constant(E), Math.E, null))
        add(ExtendedConstant(Constant(C), C_VALUE, null))
        add(ExtendedConstant(Constant(G), G_VALUE, null))
        add(ExtendedConstant(Constant(H_REDUCED), H_REDUCED_VALUE, null))
        add(ExtendedConstant(Constant(NAN), Double.NaN, null))
    }

    companion object {
        private val INSTANCE = ConstantsRegistry()

        const val E = "e"
        const val C = "c"
        const val C_VALUE = 299792458.0
        const val G = "G"
        const val G_VALUE = 6.6738480E-11
        const val H_REDUCED = "h"
        val H_REDUCED_VALUE = 6.6260695729E-34 / (2 * Math.PI)
        const val NAN = "NaN"

        @JvmStatic
        fun getInstance(): MathRegistry<IConstant> {
            INSTANCE.init()
            return INSTANCE
        }

        @JvmStatic
        fun lazyInstance(): MathRegistry<IConstant> {
            return INSTANCE
        }
    }
}
