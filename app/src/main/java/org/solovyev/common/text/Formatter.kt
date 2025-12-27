package org.solovyev.common.text

interface Formatter<T> {
    /**
     * Method formats string value of specified object
     *
     * @param value object to be converted to string
     * @return string representation of current object
     * @throws IllegalArgumentException illegal argument exception in case of any error (AND ONLY ONE EXCEPTION, I.E. NO NUMBER FORMAT EXCEPTIONS AND SO ON)
     */
    @Throws(IllegalArgumentException::class)
    fun formatValue(value: T?): String?
}
