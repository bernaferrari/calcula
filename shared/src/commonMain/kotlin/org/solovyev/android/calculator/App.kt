package org.solovyev.android.calculator

object App {
    const val TAG = "Calculator++"

    fun find(tokens: List<String>, text: String, position: Int): String? {
        return tokens.firstOrNull { token ->
            text.startsWith(token, position)
        }
    }
}

expect fun App.isTablet(context: Any): Boolean

