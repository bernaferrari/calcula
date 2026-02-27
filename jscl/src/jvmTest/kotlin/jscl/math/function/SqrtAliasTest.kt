package jscl.math.function

import kotlin.test.Test
import kotlin.test.assertEquals

class SqrtAliasTest {

    @Test
    fun templateToStringDoesNotCrash() {
        assertEquals("sqrt", SqrtAlias(null).toString())
    }
}
