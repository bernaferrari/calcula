package jscl.text

import org.junit.Assert.fail
import org.junit.Test

/**
 * User: serso
 * Date: 10/27/11
 * Time: 3:45 PM
 */
class PowerParserTest {

    @Test
    fun testParse() {
        PowerParser.parser.parse(Parser.Parameters.get("  ^"), null)
        PowerParser.parser.parse(Parser.Parameters.get(" **"), null)
        PowerParser.parser.parse(Parser.Parameters.get(" **7"), null)
        PowerParser.parser.parse(Parser.Parameters.get("^"), null)
        PowerParser.parser.parse(Parser.Parameters.get("**"), null)
        try {
            PowerParser.parser.parse(Parser.Parameters.get("*"), null)
            fail()
        } catch (e: ParseException) {
            // Expected
        }
    }
}
