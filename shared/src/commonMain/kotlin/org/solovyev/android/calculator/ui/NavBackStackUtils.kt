package org.solovyev.android.calculator.ui

/**
 * Pops one element when there is navigation history.
 * When already at root, ensures the fallback key is the only element.
 */
internal fun <T> MutableList<T>.popOrFallback(fallback: T) {
    if (size > 1) {
        removeLastOrNull()
        return
    }
    if (firstOrNull() != fallback) {
        clear()
        add(fallback)
    }
}

/**
 * Pushes a key only when it is different from the current top.
 */
internal fun <T> MutableList<T>.pushUnique(key: T) {
    if (lastOrNull() == key) return
    add(key)
}
