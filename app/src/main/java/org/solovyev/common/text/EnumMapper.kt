package org.solovyev.common.text

class EnumMapper<T : Enum<T>> private constructor(
    private val enumClass: Class<T>
) : Mapper<T> {

    override fun formatValue(value: T?): String? = value?.name

    override fun parseValue(value: String?): T? =
        value?.let { java.lang.Enum.valueOf(enumClass, it) }

    companion object {
        private val cachedMappers = mutableMapOf<Class<out Enum<*>>, Mapper<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <T : Enum<T>> of(enumClass: Class<T>): Mapper<T> {
            return cachedMappers.getOrPut(enumClass) {
                EnumMapper(enumClass)
            } as Mapper<T>
        }
    }
}
