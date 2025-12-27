package jscl.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * User: serso
 * Date: 11/19/11
 * Time: 7:22 PM
 */
class ParserUtilsTest {
    @Test
    fun testCopyOf() {
        val array = arrayOf(1, 2, 3, 7)
        val copy = ParserUtils.copyOf(array)

        assertEquals(array.size, copy.size)
        assertEquals(array[0], copy[0])
        assertEquals(array[1], copy[1])
        assertEquals(array[2], copy[2])
        assertEquals(array[3], copy[3])

        copy[3] = 12
        assertFalse(array[3] == copy[3])
    }
}
