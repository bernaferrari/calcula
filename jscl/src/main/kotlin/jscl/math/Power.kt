package jscl.math

class Power(
    @JvmField val value: Generic,
    @JvmField val exponent: Int
) {
    fun value(): Generic = value(false)

    fun value(content: Boolean): Generic =
        if (content) GenericVariable.content(value) else value

    fun exponent(): Int = exponent

    override fun toString(): String = "($value, $exponent)"
}
