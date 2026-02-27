package org.solovyev.android.calculator.memory

object MemoryRegisters {
    const val DEFAULT_REGISTER = "A"
    val QUICK_REGISTERS: List<String> = listOf("A", "B", "C", "D", "E", "F")

    fun normalizeName(raw: String): String {
        val cleaned = raw
            .trim()
            .replace(' ', '_')
            .filter { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '-' }
            .take(24)
        return if (cleaned.isEmpty()) DEFAULT_REGISTER else cleaned.uppercase()
    }

    fun add(current: String?, delta: String): String {
        val left = current?.toDoubleOrNull()
        val right = delta.toDoubleOrNull()
        return if (left != null && right != null) {
            (left + right).toString()
        } else {
            delta
        }
    }

    fun subtract(current: String?, delta: String): String {
        val left = current?.toDoubleOrNull()
        val right = delta.toDoubleOrNull()
        return if (left != null && right != null) {
            (left - right).toString()
        } else {
            delta
        }
    }
}
