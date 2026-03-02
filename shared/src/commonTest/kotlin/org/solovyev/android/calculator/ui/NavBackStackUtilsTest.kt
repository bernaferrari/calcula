package org.solovyev.android.calculator.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class NavBackStackUtilsTest {
    @Test
    fun `pushUnique appends when top is different`() {
        val stack = mutableListOf("calculator")

        stack.pushUnique("settings")

        assertEquals(listOf("calculator", "settings"), stack)
    }

    @Test
    fun `pushUnique does not duplicate top element`() {
        val stack = mutableListOf("calculator", "settings")

        stack.pushUnique("settings")

        assertEquals(listOf("calculator", "settings"), stack)
    }

    @Test
    fun `popOrFallback removes top when there is history`() {
        val stack = mutableListOf("calculator", "settings", "about")

        stack.popOrFallback("calculator")

        assertEquals(listOf("calculator", "settings"), stack)
    }

    @Test
    fun `popOrFallback resets to fallback when root is different`() {
        val stack = mutableListOf("settings")

        stack.popOrFallback("calculator")

        assertEquals(listOf("calculator"), stack)
    }

    @Test
    fun `popOrFallback keeps fallback root as-is`() {
        val stack = mutableListOf("calculator")

        stack.popOrFallback("calculator")

        assertEquals(listOf("calculator"), stack)
    }
}
