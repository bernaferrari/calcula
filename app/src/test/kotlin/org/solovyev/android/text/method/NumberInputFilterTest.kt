package org.solovyev.android.text.method

import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NumberInputFilterTest {

    private lateinit var editable: Editable

    @Before
    fun setUp() {
        editable = SpannableStringBuilder()
        editable.filters = arrayOf<InputFilter>(NumberInputFilter())
    }

    @Test
    fun testShouldNotInsertExponentInTheBeginning() {
        editable.insert(0, "E")
        assertEquals("", editable.toString())
    }

    @Test
    fun testShouldInsertExponentAtTheEnd() {
        editable.insert(0, "1")
        editable.insert(1, "E")
        assertEquals("1E", editable.toString())
    }

    @Test
    fun testShouldNotInsertSecondMinusSign() {
        editable.insert(0, "-")
        editable.insert(1, "-")
        assertEquals("-", editable.toString())
    }

    @Test
    fun testShouldNotInsertTwoMinusSigns() {
        editable.insert(0, "--")
        assertEquals("-", editable.toString())
    }

    @Test
    fun testShouldInsertSecondMinusSignAfterExponent() {
        editable.insert(0, "-")
        editable.insert(1, "E")
        editable.insert(2, "-")
        assertEquals("-E-", editable.toString())
    }

    @Test
    fun testShouldInsertSecondMinusSignAlongWithExponent() {
        editable.insert(0, "-")
        editable.insert(1, "E-")
        assertEquals("-E-", editable.toString())
    }

    @Test
    fun testShouldNotInsertMinusSignBeforeExistingMinusSIgn() {
        editable.insert(0, "-")
        editable.insert(0, "-")
        assertEquals("-", editable.toString())
    }

    @Test
    fun testShouldNotInsertSecondDecimalPoint() {
        editable.insert(0, "0.2")
        editable.insert(3, ".")
        assertEquals("0.2", editable.toString())
    }

    @Test
    fun testShouldNotInsertTwoDecimalPoints() {
        editable.insert(0, "..")
        assertEquals(".", editable.toString())
    }

    @Test
    fun testShouldNotInsertDecimalPointAfterExponent() {
        editable.insert(0, "2E")
        editable.insert(2, ".")
        assertEquals("2E", editable.toString())

        editable.clear()
        editable.insert(0, "2E.")
        assertEquals("2E", editable.toString())
    }

    @Test
    fun testShouldNotInsertTwoExcponents() {
        editable.insert(0, "2EE")
        assertEquals("2E", editable.toString())
    }

    @Test
    fun testShouldNotInsertExponentBeforeDecimalPoint() {
        editable.insert(0, "0.2")
        editable.insert(0, "E")
        assertEquals("0.2", editable.toString())
    }
}
