package org.solovyev.common.text

interface Parser<T> {
    /**
     * Method parses specified value and returns converted object
     *
     * @param value string to be parsed
     * @return parsed object
     * @throws IllegalArgumentException illegal argument exception in case of any error (AND ONLY ONE EXCEPTION, I.E. NO NUMBER FORMAT EXCEPTIONS AND SO ON)
     */
    @Throws(IllegalArgumentException::class)
    fun parseValue(value: String?): T?
}
