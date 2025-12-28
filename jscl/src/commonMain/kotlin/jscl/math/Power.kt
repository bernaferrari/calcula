package jscl.math

class Power(
    val value: Generic,
    val exponent: Int
) {
    fun value(): Generic = value(false)

    fun value(content: Boolean): Generic =
        if (content) GenericVariable.content(value) else value

    fun exponent(): Int = exponent

    override fun toString(): String = "($value, $exponent)"
}
