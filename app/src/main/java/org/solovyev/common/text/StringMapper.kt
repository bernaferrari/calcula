package org.solovyev.common.text

object StringMapper : Mapper<String> {
    override fun formatValue(value: String?): String? = value

    override fun parseValue(value: String?): String? = value
}
