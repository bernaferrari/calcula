package org.solovyev.common.text

import org.solovyev.android.Check
import org.solovyev.android.prefs.CachingMapper

class NumberMapper<N : Number> private constructor(
    private val parser: Parser<out N>,
    private val formatter: Formatter<N>
) : Mapper<N> {

    private constructor(kind: NumberKind) : this(
        NumberParser.of(kind),
        ValueOfFormatter.getNotNullFormatter()
    )

    override fun formatValue(value: N?): String? = formatter.formatValue(value)

    override fun parseValue(value: String?): N? = parser.parseValue(value)

    companion object {
        private val supportedKinds = NumberParser.supportedKinds

        private val mappers = mutableMapOf<NumberKind, Mapper<*>>().apply {
            supportedKinds.forEach { kind ->
                put(kind, CachingMapper.of(newInstance(kind)))
            }
        }

        fun <N : Number> newInstance(
            parser: Parser<out N>,
            formatter: Formatter<N>
        ): Mapper<N> = NumberMapper(parser, formatter)

        private fun <N : Number> newInstance(kind: NumberKind): Mapper<N> =
            NumberMapper(kind)

        @Suppress("UNCHECKED_CAST")
        fun <N : Number> of(kind: NumberKind): Mapper<N> {
            Check.isTrue(
                supportedKinds.contains(kind),
                "Kind $kind is not supported by ${NumberMapper::class.java}"
            )
            return mappers[kind] as Mapper<N>
        }
    }
}
