package org.solovyev.android.calculator

import com.ionspin.kotlin.bignum.integer.BigInteger
import jscl.NumeralBase

internal object BitwiseRange {
    private val two = BigInteger.fromInt(2)

    fun modeLabel(wordSize: Int, signed: Boolean, base: NumeralBase): String {
        val signLabel = if (signed) "S" else "U"
        return "${wordSize.coerceIn(1, 64)}b $signLabel ${base.name.uppercase()}"
    }

    fun isOverflow(
        displayText: String,
        wordSize: Int,
        signed: Boolean,
        base: NumeralBase
    ): Boolean {
        val value = parseDisplayInteger(displayText, base) ?: return false
        val size = wordSize.coerceIn(1, 64)

        return if (signed) {
            val min = two.pow(size - 1).negate()
            val max = two.pow(size - 1) - BigInteger.ONE
            value < min || value > max
        } else {
            val max = two.pow(size) - BigInteger.ONE
            value < BigInteger.ZERO || value > max
        }
    }

    private fun parseDisplayInteger(text: String, base: NumeralBase): BigInteger? {
        val normalized = text
            .trim()
            .replace("−", "-")
            .replace(" ", "")
            .replace("'", "")

        if (normalized.isEmpty()) return null
        if (normalized.contains('.')) return null
        if (normalized.contains(',')) return null
        if (normalized.contains('/')) return null
        if (normalized.contains("NaN", ignoreCase = true)) return null
        if (normalized.contains('∞')) return null
        if (normalized.contains('i')) return null
        if (normalized.contains('E') && base == NumeralBase.dec) return null

        return try {
            BigInteger.parseString(normalized, base.radix)
        } catch (_: Throwable) {
            null
        }
    }
}
