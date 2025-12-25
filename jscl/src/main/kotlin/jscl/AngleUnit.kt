package jscl

import jscl.math.Generic
import jscl.math.NumericWrapper
import jscl.math.numeric.Numeric
import jscl.math.numeric.Real

enum class AngleUnit {
    deg {
        override fun getCoefficientTo(to: AngleUnit): Double {
            return when (to) {
                deg -> 1.0
                rad -> FROM_DEG_TO_RAD
                grad -> FROM_DEG_TO_GRAD
                turns -> FROM_DEG_TO_TURNS
            }
        }
    },

    rad {
        override fun getCoefficientTo(to: AngleUnit): Double {
            return when (to) {
                deg -> FROM_RAD_TO_DEG
                rad -> 1.0
                grad -> FROM_RAD_TO_GRAD
                turns -> FROM_RAD_TO_TURNS
            }
        }
    },

    grad {
        override fun getCoefficientTo(to: AngleUnit): Double {
            return when (to) {
                deg -> FROM_GRAD_TO_DEG
                rad -> FROM_GRAD_TO_RAD
                grad -> 1.0
                turns -> FROM_GRAD_TO_TURNS
            }
        }
    },

    turns {
        override fun getCoefficientTo(to: AngleUnit): Double {
            return when (to) {
                deg -> FROM_TURNS_TO_DEG
                rad -> FROM_TURNS_TO_RAD
                grad -> FROM_TURNS_TO_GRAD
                turns -> 1.0
            }
        }
    };

    fun transform(to: AngleUnit, value: Double): Double {
        return value * getCoefficientTo(to)
    }

    protected abstract fun getCoefficientTo(to: AngleUnit): Double

    fun transform(to: AngleUnit, value: Numeric): Numeric {
        return value.multiply(getRealCoefficientTo(to))
    }

    private fun getRealCoefficientTo(to: AngleUnit): Real {
        return Real.valueOf(getCoefficientTo(to))
    }

    fun transform(to: AngleUnit, value: Generic): Generic {
        return value.multiply(NumericWrapper(getRealCoefficientTo(to)))
    }

    companion object {
        private const val FROM_RAD_TO_DEG = 180.0 / Math.PI
        private const val FROM_RAD_TO_GRAD = 200.0 / Math.PI
        private const val FROM_RAD_TO_TURNS = 0.5 / Math.PI

        private const val FROM_DEG_TO_RAD = Math.PI / 180.0
        private const val FROM_DEG_TO_TURNS = 0.5 / 180.0
        private const val FROM_DEG_TO_GRAD = 10.0 / 9.0

        private const val FROM_GRAD_TO_RAD = Math.PI / 200.0
        private const val FROM_GRAD_TO_TURNS = 0.5 / 200.0
        private const val FROM_GRAD_TO_DEG = 9.0 / 10.0

        private const val FROM_TURNS_TO_GRAD = 200.0 / 0.5
        private const val FROM_TURNS_TO_RAD = Math.PI / 0.5
        private const val FROM_TURNS_TO_DEG = 180.0 / 0.5
    }
}
