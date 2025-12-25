package jscl.math.function

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * User: serso
 * Date: 11/12/11
 * Time: 2:14 PM
 */
class FunctionsRegistryTest {

    @Test
    fun testOrder() {
        var prev: Function? = null
        for (function in FunctionsRegistry.getInstance().getEntities()) {
            if (prev != null) {
                assertTrue("${prev.name}<${function.name}", prev.name.length >= function.name.length)
            }
            prev = function
        }
    }
}
