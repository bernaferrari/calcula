package jscl.text.msg

import jscl.i18n.JsclLocale
import jscl.text.msg.Messages.msg_1
import org.junit.Assert.assertFalse
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
    fun testTranslation() {
        val localizedMessage = JsclMessage(msg_1, error).getLocalizedMessage(JsclLocale.ENGLISH)
        assertTrue(localizedMessage.startsWith("Parsing error "))
    }

    @Test
    fun testShouldContainPolishStrings() {
        val localizedMessage = JsclMessage(msg_1, error).getLocalizedMessage(JsclLocale.forLanguageAndCountry("pl", "PL"))
        assertTrue(localizedMessage.startsWith("Wystąpił błąd "))
    }

    @Test
    fun testAllMessages() {
        for (i in 0 until Messages.COUNT) {
            val id = "msg_$i"
            val arguments = makeMessageArguments(i)
            val message = JsclMessage(id, MessageType.info, arguments)
            for (locale in LOCALES) {
                val text = message.getLocalizedMessage(locale)
                assertFalse(text.isEmpty())
                if (arguments.size == 1) {
                    assertTrue(text.contains("param0"))
                } else if (arguments.size == 2) {
                    assertTrue(text.contains("param1"))
                    assertTrue(text.contains("param2"))
                }
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

    companion object {
        private val LOCALES = arrayOf(
            JsclLocale.ENGLISH,
            JsclLocale.forLanguage("ar"),
            JsclLocale.forLanguage("cs"),
            JsclLocale.forLanguage("de"),
            JsclLocale.forLanguageAndCountry("es", "ES"),
            JsclLocale.forLanguage("fi"),
            JsclLocale.forLanguage("fr"),
            JsclLocale.forLanguage("it"),
            JsclLocale.forLanguage("ja"),
            JsclLocale.forLanguage("nl"),
            JsclLocale.forLanguage("pl"),
            JsclLocale.forLanguageAndCountry("pt", "BR"),
            JsclLocale.forLanguageAndCountry("pt", "PT"),
            JsclLocale.forLanguage("ru"),
            JsclLocale.forLanguage("uk"),
            JsclLocale.forLanguage("vi"),
            JsclLocale.forLanguageAndCountry("zh", "CN"),
            JsclLocale.forLanguageAndCountry("zh", "TW")
        )
    }
}
