package org.solovyev.android.calculator

internal object ProgrammerLiteralNormalizer {
    private val hexPattern = Regex("(?<![A-Za-z0-9_])0[xX](?!:)([0-9A-Fa-f]+)(?![A-Za-z0-9_])")
    private val octPattern = Regex("(?<![A-Za-z0-9_])0[oO](?!:)([0-7]+)(?![A-Za-z0-9_])")
    private val binPattern = Regex("(?<![A-Za-z0-9_])0[bB](?!:)([01]+)(?![A-Za-z0-9_])")
    private val decPattern = Regex("(?<![A-Za-z0-9_])0[dD](?!:)([0-9]+)(?![A-Za-z0-9_])")

    fun normalize(expression: String): String {
        var result = expression
        result = hexPattern.replace(result) { match ->
            "0x:${match.groupValues[1].uppercase()}"
        }
        result = octPattern.replace(result) { match ->
            "0o:${match.groupValues[1]}"
        }
        result = binPattern.replace(result) { match ->
            "0b:${match.groupValues[1]}"
        }
        result = decPattern.replace(result) { match ->
            "0d:${match.groupValues[1]}"
        }
        return result
    }
}
