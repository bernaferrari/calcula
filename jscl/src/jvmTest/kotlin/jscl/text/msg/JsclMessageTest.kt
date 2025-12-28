package jscl.text.msg

import jscl.text.msg.Messages.msg_1
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.solovyev.common.msg.MessageType
import org.solovyev.common.msg.MessageType.error

/**
 * User: serso
 * Date: 11/30/11
 * Time: 9:53 PM
 */
class JsclMessageTest {

    @Test
    fun testMessageCodeAndType() {
        val message = JsclMessage(msg_1, error)
        assertEquals(msg_1, message.getMessageCode())
        assertEquals(error, message.getMessageLevel())
        assertTrue(message.getParameters().isEmpty())
    }

    @Test
    fun testParameters() {
        val message = JsclMessage(msg_1, error, "param0", "param1")
        assertEquals(listOf("param0", "param1"), message.getParameters())
    }

    @Test
    fun testAllMessages() {
        for (i in 0 until Messages.COUNT) {
            val id = "msg_$i"
            val arguments = makeMessageArguments(i)
            val message = JsclMessage(id, MessageType.info, arguments)
            assertEquals(id, message.getMessageCode())
            assertEquals(arguments.size, message.getParameters().size)
            assertEquals(arguments, message.getParameters())
            if (arguments.isEmpty()) {
                assertFalse(message.getMessageCode().isEmpty())
            }
        }
    }

    private fun makeMessageArguments(i: Int): List<String> {
        return when (i) {
            0, 10, 19 -> listOf("param1", "param2")
            1, 2, 3, 4, 6, 8, 11, 12, 13, 14, 17, 20, 21 -> listOf("param0")
            else -> emptyList()
        }
    }

}
