package org.solovyev.android.text.method

import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.Spanned

class NumberInputFilter : InputFilter {

    companion object {
        private const val CHAR_SIGN = 0
        private const val CHAR_POINT = 1
        private const val CHAR_EXP = 2

        private val ACCEPTED = charArrayOf('E', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '+', '.')

        private val instance = NumberInputFilter()

        /**
         * Returns a NumberInputFilter that accepts the digits 0 through 9.
         */
        @JvmStatic
        fun getInstance(): NumberInputFilter = instance

        private fun Char.isSignChar() = this == '-' || this == '+'
        private fun Char.isDecimalPointChar() = this == '.'
        private fun Char.isExponentChar() = this == 'E'
        private fun Char.isAccepted() = this in ACCEPTED
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        var filteredSource = source
        var filteredStart = start
        var filteredEnd = end

        val out = filterIllegalCharacters(source, start, end)
        if (out != null) {
            filteredSource = out
            filteredStart = 0
            filteredEnd = out.length
        }

        val chars = IntArray(3) { -1 }
        findChars(dest, 0, dstart, 0, chars)
        findChars(dest, dend, dest.length, filteredEnd - filteredStart, chars)

        var filtered: SpannableStringBuilder? = null

        for (i in filteredStart until filteredEnd) {
            val c = filteredSource[i]
            var shouldFilter = false

            when {
                c.isSignChar() -> {
                    if (i == filteredStart && dstart == 0) {
                        if (chars[CHAR_SIGN] >= 0) {
                            shouldFilter = true
                        } else {
                            chars[CHAR_SIGN] = i + dstart
                        }
                    } else if (chars[CHAR_EXP] == i + dstart - 1) {
                        // allow sign after exponent symbol
                        shouldFilter = false
                    } else {
                        shouldFilter = true
                    }
                }
                c.isDecimalPointChar() -> {
                    when {
                        chars[CHAR_POINT] >= 0 -> shouldFilter = true
                        chars[CHAR_EXP] >= 0 && chars[CHAR_EXP] < i + dstart -> {
                            // no decimal point after exponent
                            shouldFilter = true
                        }
                        else -> chars[CHAR_POINT] = i + dstart
                    }
                }
                c.isExponentChar() -> {
                    when {
                        chars[CHAR_EXP] >= 0 -> shouldFilter = true
                        chars[CHAR_POINT] >= 0 && chars[CHAR_POINT] > i + dstart -> {
                            // no exponent before decimal point
                            shouldFilter = true
                        }
                        i + dstart == 0 -> {
                            // exponent can't be first
                            shouldFilter = true
                        }
                        else -> chars[CHAR_EXP] = i + dstart
                    }
                }
            }

            if (shouldFilter) {
                if (filteredEnd == filteredStart + 1) {
                    return ""  // Only one character, and it was stripped.
                }

                if (filtered == null) {
                    filtered = SpannableStringBuilder(filteredSource, filteredStart, filteredEnd)
                }

                filtered.delete(i - filteredStart, i + 1 - filteredStart)
            }
        }

        return filtered ?: out
    }

    private fun findChars(s: Spanned, start: Int, end: Int, offset: Int, out: IntArray) {
        for (i in start until end) {
            val c = s[i]

            when {
                c.isSignChar() -> {
                    if (out[CHAR_SIGN] == -1 && out[CHAR_EXP] == -1) {
                        // count in only signs before exponent
                        out[CHAR_SIGN] = i + offset
                    }
                }
                c.isDecimalPointChar() -> {
                    if (out[CHAR_POINT] == -1) {
                        out[CHAR_POINT] = i + offset
                    }
                }
                c.isExponentChar() -> {
                    if (out[CHAR_EXP] == -1) {
                        out[CHAR_EXP] = i + offset
                    }
                }
            }
        }
    }

    private fun filterIllegalCharacters(source: CharSequence, start: Int, end: Int): CharSequence? {
        val illegal = findIllegalChar(source, start, end)
        if (illegal == end) {
            // all OK
            return null
        }
        if (end - start == 1) {
            // it was not OK, and there is only one char, so nothing remains.
            return ""
        }

        val filtered = SpannableStringBuilder(source, start, end)
        val newEnd = end - start - 1
        // only count down to "illegal" because the chars before that were all OK.
        val newIllegal = illegal - start
        for (j in newEnd downTo newIllegal) {
            if (!source[j].isAccepted()) {
                filtered.delete(j, j + 1)
            }
        }
        return filtered
    }

    private fun findIllegalChar(s: CharSequence, start: Int, end: Int): Int {
        for (i in start until end) {
            if (!s[i].isAccepted()) {
                return i
            }
        }
        return end
    }
}
