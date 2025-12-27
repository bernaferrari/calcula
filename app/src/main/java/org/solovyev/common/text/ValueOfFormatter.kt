package org.solovyev.common.text

class ValueOfFormatter<T> private constructor(
    private val processNulls: Boolean
) : Formatter<T> {

    override fun formatValue(value: T?): String? =
        when (value) {
            null if processNulls -> value.toString()
            null -> null
            else -> value.toString()
        }

    companion object {
        private val notNullFormatter = ValueOfFormatter<Any>(processNulls = false)
        private val nullableFormatter = ValueOfFormatter<Any>(processNulls = true)

        @Suppress("UNCHECKED_CAST")
        fun <T> getNotNullFormatter(): ValueOfFormatter<T> = notNullFormatter as ValueOfFormatter<T>

        @Suppress("UNCHECKED_CAST")
        fun <T> getNullableFormatter(): ValueOfFormatter<T> = nullableFormatter as ValueOfFormatter<T>
    }
}
