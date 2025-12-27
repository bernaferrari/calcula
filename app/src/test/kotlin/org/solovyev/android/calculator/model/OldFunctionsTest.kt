package org.solovyev.android.calculator.model

import jscl.util.ExpressionGenerator
import org.junit.Assert
import org.junit.Test
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.solovyev.android.calculator.functions.OldFunctions
import java.io.StringWriter
import java.util.Date
import java.util.Random

/**
 * User: serso
 * Date: 11/14/12
 * Time: 8:06 PM
 */
class OldFunctionsTest {

    private fun testXml(input: OldFunctions, expectedXml: String?): OldFunctions {
        val actualXml = toXml(input)

        if (expectedXml != null) {
            Assert.assertEquals(expectedXml, actualXml)
        }

        val serializer: Serializer = Persister()
        val out = serializer.read(OldFunctions::class.java, actualXml)
        val actualXml2 = toXml(out)
        Assert.assertEquals(actualXml, actualXml2)
        return out
    }

    @Throws(Exception::class)
    private fun toXml(input: OldFunctions): String {
        val sw = StringWriter()
        val serializer: Serializer = Persister()
        serializer.write(input, sw)
        return sw.toString()
    }

    @Test
    @Throws(Exception::class)
    fun testRandomXml() {
        val input = OldFunctions()

        val random = Random(Date().time)

        val generator = ExpressionGenerator(10)
        for (i in 0 until 1000) {
/*            val content = generator.generate()

            val paramsString = Strings.generateRandomString(random.nextInt(10))
            val parameterNames = ArrayList<String>()
            for (j in 0 until paramsString.length) {
                parameterNames.add(paramsString[j].toString())
            }

            val builder = OldFunction.Builder("test_$i", content, parameterNames)

            if (random.nextBoolean()) {
                builder.setDescription(Strings.generateRandomString(random.nextInt(100)))
            }

            builder.setSystem(random.nextBoolean())

            input.entities.add(builder.create())*/
        }

        testXml(input, null)
    }

    companion object {
        private const val xml = """<functions>
   <functions class="java.util.ArrayList">
      <function>
         <name>test</name>
         <body>x+y</body>
         <parameterNames class="java.util.ArrayList">
            <string>x</string>
            <string>y</string>
         </parameterNames>
         <system>false</system>
         <description>description</description>
      </function>
      <function>
         <name>z_2</name>
         <body>e^(z_1^2+z_2^2)</body>
         <parameterNames class="java.util.ArrayList">
            <string>z_1</string>
            <string>z_2</string>
         </parameterNames>
         <system>true</system>
         <description></description>
      </function>
      <function>
         <name>z_2</name>
         <body>e^(z_1^2+z_2^2)</body>
         <parameterNames class="java.util.ArrayList">
            <string>z_1</string>
            <string>z_2</string>
         </parameterNames>
         <system>true</system>
         <description></description>
      </function>
   </functions>
</functions>"""
    }
}
