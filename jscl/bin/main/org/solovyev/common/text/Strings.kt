package org.solovyev.common.text

import kotlin.random.Random

object Strings {

    val LINE_SEPARATOR: String = System.getProperty("line.separator")
    val EMPTY_CHARACTER_OBJECT_ARRAY: Array<Char> = emptyArray()

    // random variable for generating random strings
    private val RANDOM = Random(System.currentTimeMillis())

    fun isEmpty(s: CharSequence?): Boolean {
        return s == null || s.isEmpty()
    }

    fun getNotEmpty(s: CharSequence?, defaultValue: String): String {
        return if (isEmpty(s)) defaultValue else s.toString()
    }

    fun toObjects(array: CharArray?): Array<Char> {
        if (array == null || array.isEmpty()) {
            return EMPTY_CHARACTER_OBJECT_ARRAY
        }

        return array.toTypedArray()
    }

    fun generateRandomString(length: Int): String {
        val result = StringBuilder(length)
        for (i in 0 until length) {
            // 'A' = 65
            val ch = (Random.nextInt(52) + 65).toChar()
            result.append(ch)
        }
        return result.toString()
    }
}
