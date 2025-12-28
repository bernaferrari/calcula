package jscl.math.numeric

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.Arithmetic
import jscl.math.numeric.Complex.Companion.I
import jscl.math.numeric.Real.Companion.ONE
import jscl.math.numeric.Real.Companion.TWO
import com.ionspin.kotlin.bignum.integer.BigInteger

abstract class Numeric : Arithmetic<Numeric>, INumeric<Numeric>, Comparable<Numeric> {

    override fun abs(): Numeric {
        return if (signum() < 0) negate() else this
    }

    override fun sgn(): Numeric {
        return divide(abs())
    }

    override fun inverse(): Numeric {
        return ONE.divide(this)
    }

    override fun pow(exponent: Int): Numeric {
        var result: Numeric = ONE

        for (i in 0 until exponent) {
            result = result.multiply(this)
        }

        return result
    }

    /*
      * ******************************************************************************************
      *
      * CONVERSION FUNCTIONS (rad to default angle units and vice versa)
      *
      * *******************************************************************************************
      */

    open fun pow(numeric: Numeric): Numeric {
        return when {
            numeric.signum() == 0 -> ONE
            numeric.compareTo(ONE) == 0 -> this
            else -> numeric.multiply(this.ln()).exp()
        }
    }

    override fun sqrt(): Numeric {
        return nThRoot(2)
    }

    override fun nThRoot(n: Int): Numeric {
        return pow(Real.valueOf(1.0 / n))
    }

    abstract fun conjugate(): Numeric

    /*
      * ******************************************************************************************
      *
      * TRIGONOMETRIC FUNCTIONS
      *
      * *******************************************************************************************
      */

    override fun sin(): Numeric {
        // e = exp(i)
        val e = defaultToRad(this).multiply(I).exp()
        // e1 = exp(2ix)
        val e1 = e.pow(2)

        // result = [i - i * exp(2i)] / [2exp(i)]
        return I.subtract(e1.multiply(I)).divide(TWO.multiply(e))
    }

    override fun cos(): Numeric {
        // e = exp(ix)
        val e = defaultToRad(this).multiply(I).exp()
        // e1 = exp(2ix)
        val e1 = e.pow(2)

        // result = [ 1 + exp(2ix) ] / (2 *exp(ix))
        return ONE.add(e1).divide(TWO.multiply(e))
    }

    override fun tan(): Numeric {
        // e = exp(2xi)
        val e = defaultToRad(this).multiply(I).exp().pow(2)

        // e1 = i * exp(2xi)
        val e1 = e.multiply(I)

        // result = (i - i * exp(2xi)) / ( 1 + exp(2xi) )
        return I.subtract(e1).divide(ONE.add(e))
    }

    override fun cot(): Numeric {
        // e = exp(2xi)
        val e = I.multiply(defaultToRad(this)).exp().pow(2)

        // result = - (i + i * exp(2ix)) / ( 1 - exp(2xi))
        return I.add(I.multiply(e)).divide(ONE.subtract(e)).negate()
    }

    /**
     * ******************************************************************************************
     *
     * INVERSE TRIGONOMETRIC FUNCTIONS
     *
     * *******************************************************************************************
     */

    override fun asin(): Numeric {
        // e = √(1 - x^2)
        val e = ONE.subtract(this.pow(2)).sqrt()
        // result = -iln[xi + √(1 - x^2)]
        return radToDefault(this.multiply(I).add(e).ln().multiply(I.negate()))
    }

    override fun acos(): Numeric {
        // e = √(-1 + x^2) = i √(1 - x^2)
        val e = I.multiply(Real.ONE.subtract(this.pow(2)).sqrt())

        // result = -i * ln[ x + √(-1 + x^2) ]
        return radToDefault(this.add(e).ln().multiply(I.negate()))
    }

    override fun atan(): Numeric {
        // e = ln[(i + x)/(i-x)]
        val e = I.add(this).divide(I.subtract(this)).ln()
        // result = iln[(i + x)/(i-x)]/2
        return radToDefault(I.multiply(e).divide(TWO))
    }

    override fun acot(): Numeric {
        // e = ln[-(i + x)/(i-x)]
        val e = I.add(this).divide(I.subtract(this)).negate().ln()
        // result = iln[-(i + x)/(i-x)]/2
        return radToDefault(I.multiply(e).divide(TWO))
    }

    /**
     * ******************************************************************************************
     *
     * HYPERBOLIC TRIGONOMETRIC FUNCTIONS
     *
     * *******************************************************************************************
     */

    override fun sinh(): Numeric {
        val thisRad = defaultToRad(this)

        // e = exp(2x)
        val e = thisRad.exp().pow(2)

        // e1 = 2exp(x)
        val e1 = TWO.multiply(thisRad.exp())

        // result = -[1 - exp(2x)]/[2exp(x)]
        return ONE.subtract(e).divide(e1).negate()
    }

    override fun cosh(): Numeric {
        val thisExpRad = defaultToRad(this).exp()

        // e = exp(2x)
        val e = thisExpRad.pow(2)

        // e1 = 2exp(x)
        val e1 = TWO.multiply(thisExpRad)

        // result = [ 1 + exp(2x )] / 2exp(x)
        return ONE.add(e).divide(e1)
    }

    override fun tanh(): Numeric {
        // e = exp(2x)
        val e = defaultToRad(this).exp().pow(2)

        // result = - (1 - exp(2x)) / (1 + exp(2x))
        return ONE.subtract(e).divide(ONE.add(e)).negate()
    }

    override fun coth(): Numeric {
        // e = exp(2x)
        val e = defaultToRad(this).exp().pow(2)

        // result = - (1 + exp(2x)) / (1 - exp(2x))
        return ONE.add(e).divide(ONE.subtract(e)).negate()
    }

    /**
     * ******************************************************************************************
     *
     * INVERSE HYPERBOLIC TRIGONOMETRIC FUNCTIONS
     *
     * *******************************************************************************************
     */

    override fun asinh(): Numeric {
        // e = √( 1 + x ^ 2 )
        val e = ONE.add(this.pow(2)).sqrt()

        // result = ln [ x + √( 1 + x ^ 2 ) ]
        return radToDefault(this.add(e).ln())
    }

    override fun acosh(): Numeric {
        // e = √(x ^ 2 - 1)
        val e = Real.valueOf(-1.0).add(this.pow(2)).sqrt()

        // result = ln( x + √(x ^ 2 - 1) )
        return radToDefault(this.add(e).ln())
    }

    override fun atanh(): Numeric {
        // e = 1 - x
        val e = ONE.subtract(this)

        // result = ln [ ( 1 + x ) / ( 1 - x ) ] / 2
        return radToDefault(ONE.add(this).divide(e).ln().divide(TWO))
    }

    override fun acoth(): Numeric {
        // e = 1 - x
        val e = ONE.subtract(this)

        // result = ln [ - (1 + x) / (1 - x) ] / 2
        return radToDefault(ONE.add(this).divide(e).negate().ln().divide(TWO))
    }

    abstract fun valueOf(numeric: Numeric): Numeric

    abstract override fun compareTo(other: Numeric): Int

    override fun equals(other: Any?): Boolean {
        return other is Numeric && compareTo(other) == 0
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    protected fun toString(value: Double): String {
        return JsclMathEngine.getInstance().format(value)
    }

    open fun toBigInteger(): BigInteger? {
        return null
    }

    abstract fun doubleValue(): Double

    companion object {
        fun root(subscript: Int, parameter: Array<Numeric>): Numeric {
            throw ArithmeticException()
        }

        internal fun defaultToRad(value: Double): Double {
            return JsclMathEngine.getInstance().angleUnits.transform(AngleUnit.rad, value)
        }

        internal fun radToDefault(value: Double): Double {
            return AngleUnit.rad.transform(JsclMathEngine.getInstance().angleUnits, value)
        }

        internal fun defaultToRad(value: Numeric): Numeric {
            return JsclMathEngine.getInstance().angleUnits.transform(AngleUnit.rad, value)
        }

        internal fun radToDefault(value: Numeric): Numeric {
            return AngleUnit.rad.transform(JsclMathEngine.getInstance().angleUnits, value)
        }
    }
}
