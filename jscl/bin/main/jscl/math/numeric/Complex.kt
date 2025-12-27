package jscl.math.numeric

import jscl.AngleUnit
import jscl.JsclMathEngine
import jscl.math.NotDivisibleException
import jscl.math.NotDoubleException
import jscl.text.msg.JsclMessage
import jscl.text.msg.Messages
import org.solovyev.common.msg.MessageType
import kotlin.math.*

class Complex private constructor(
    private val real: Double,
    private val imaginary: Double
) : Numeric() {

    fun add(complex: Complex): Complex {
        return valueOf(real + complex.real, imaginary + complex.imaginary)
    }

    override fun add(that: Numeric): Numeric {
        return when (that) {
            is Complex -> add(that)
            is Real -> add(valueOf(that))
            else -> that.valueOf(this).add(that)
        }
    }

    fun subtract(complex: Complex): Complex {
        return valueOf(real - complex.real, imaginary - complex.imaginary)
    }

    override fun subtract(that: Numeric): Numeric {
        return when (that) {
            is Complex -> subtract(that)
            is Real -> subtract(valueOf(that))
            else -> that.valueOf(this).subtract(that)
        }
    }

    fun multiply(complex: Complex): Complex {
        return valueOf(
            real * complex.real - imaginary * complex.imaginary,
            real * complex.imaginary + imaginary * complex.real
        )
    }

    override fun multiply(that: Numeric): Numeric {
        return when (that) {
            is Complex -> multiply(that)
            is Real -> multiply(valueOf(that))
            else -> that.multiply(this)
        }
    }

    fun divide(complex: Complex): Complex {
        return multiply(complex.inverse() as Complex)
    }

    override fun divide(that: Numeric): Numeric {
        return when (that) {
            is Complex -> divide(that)
            is Real -> divide(valueOf(that))
            else -> that.valueOf(this).divide(that)
        }
    }

    override fun negate(): Numeric {
        return valueOf(-real, -imaginary)
    }

    override fun abs(): Numeric {
        val realSquare = Real(real).pow(2)
        val imaginarySquare = Real(imaginary).pow(2)
        val sum = realSquare.add(imaginarySquare)
        return sum.sqrt()
    }

    override fun signum(): Int {
        return when {
            real > 0.0 -> 1
            real < 0.0 -> -1
            else -> Real.signum(imaginary)
        }
    }

    fun magnitude(): Double {
        return sqrt(real * real + imaginary * imaginary)
    }

    fun magnitude2(): Double {
        return real * real + imaginary * imaginary
    }

    fun angle(): Double {
        return atan2(imaginary, real)
    }

    override fun ln(): Numeric {
        return if (signum() == 0) {
            Real.ZERO.ln()
        } else {
            valueOf(ln(magnitude()), angle())
        }
    }

    override fun lg(): Numeric {
        return if (signum() == 0) {
            Real.ZERO.lg()
        } else {
            valueOf(log10(magnitude()), angle())
        }
    }

    override fun exp(): Numeric {
        return valueOf(cos(defaultToRad(imaginary)), sin(defaultToRad(imaginary)))
            .multiply(exp(real))
    }

    override fun inverse(): Numeric {
        return (conjugate() as Complex).divide(magnitude2())
    }

    internal fun multiply(d: Double): Complex {
        return valueOf(real * d, imaginary * d)
    }

    internal fun divide(d: Double): Complex {
        return valueOf(real / d, imaginary / d)
    }

    override fun conjugate(): Numeric {
        return valueOf(real, -imaginary)
    }

    fun realPart(): Double {
        return real
    }

    fun imaginaryPart(): Double {
        return imaginary
    }

    fun compareTo(that: Complex): Int {
        return when {
            imaginary < that.imaginary -> -1
            imaginary > that.imaginary -> 1
            imaginary == that.imaginary -> when {
                real < that.real -> -1
                real > that.real -> 1
                real == that.real -> 0
                else -> throw ArithmeticException()
            }
            else -> throw ArithmeticException()
        }
    }

    override fun compareTo(other: Numeric): Int {
        return when (other) {
            is Complex -> compareTo(other)
            is Real -> compareTo(valueOf(other))
            else -> other.valueOf(this).compareTo(other)
        }
    }

    override fun doubleValue(): Double {
        throw NotDoubleException.get()
    }

    fun copyOf(complex: Complex): Complex {
        return valueOf(complex.real, complex.imaginary)
    }

    override fun valueOf(numeric: Numeric): Numeric {
        return when (numeric) {
            is Complex -> copyOf(numeric)
            is Real -> numeric.toComplex()
            else -> throw ArithmeticException()
        }
    }

    override fun toString(): String {
        val result = StringBuilder()

        if (imaginary == 0.0) {
            result.append(toString(real))
        } else {
            if (real != 0.0) {
                result.append(toString(real))
                if (imaginary > 0.0) {
                    result.append("+")
                }
            }

            if (imaginary != 1.0) {
                if (imaginary == -1.0) {
                    result.append("-")
                } else {
                    if (imaginary < 0.0) {
                        val imagStr = toString(imaginary)
                        // due to rounding we can forget sign (-0.00000000001 can be round to 0 => plus sign would not be added above and no sign will be before i)
                        if (imagStr.startsWith("-")) {
                            result.append(imagStr)
                        } else {
                            result.append("-").append(imagStr)
                        }
                    } else {
                        result.append(toString(imaginary))
                    }
                    result.append("*")
                }
            }
            result.append("i")
        }

        return result.toString()
    }

    companion object {
        @JvmField
        val I = Complex(0.0, 1.0)

        @JvmStatic
        fun valueOf(real: Double, imaginary: Double): Complex {
            if (JsclMathEngine.getInstance().angleUnits != AngleUnit.rad) {
                JsclMathEngine.getInstance().messageRegistry.addMessage(
                    JsclMessage(Messages.msg_23, MessageType.warning)
                )
            }

            return if (real == 0.0 && imaginary == 1.0) {
                I
            } else {
                Complex(real, imaginary)
            }
        }
    }
}
