package org.solovyev.common.text

import org.solovyev.android.Check

class NumberParser<N : Number> private constructor(
    private val kind: NumberKind
) : Parser<N> {

    @Suppress("UNCHECKED_CAST")
    override fun parseValue(value: String?): N? {
        return value?.let {
            when (kind) {
                NumberKind.Int -> it.toInt() as N
                NumberKind.Float -> it.toFloat() as N
                NumberKind.Long -> it.toLong() as N
                NumberKind.Double -> it.toDouble() as N
            }
        }
    }

    companion object {
        val supportedKinds: List<NumberKind> = NumberKind.values().toList()

        private val parsers = mutableMapOf<NumberKind, Parser<*>>().apply {
            supportedKinds.forEach { kind ->
                put(kind, NumberParser<Number>(kind))
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <N : Number> of(kind: NumberKind): Parser<N> {
            Check.isTrue(
                supportedKinds.contains(kind),
                "Kind $kind is not supported by ${NumberParser::class.java}"
            )
            return parsers[kind] as Parser<N>
        }
    }
}
