package org.solovyev.android.calculator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.solovyev.common.msg.MessageType
import java.util.Arrays
import java.util.Collections
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class CalculatorMessagesTest {

    companion object {
        private val LOCALES = arrayOf(
            Locale.ENGLISH, Locale("ar"), Locale("cs"), Locale("de"), Locale("es"), Locale("es", "ES"), Locale("fi"), Locale("fr"), Locale("it"), Locale("ja"), Locale("nl"), Locale("pl"),
            Locale("pt", "BR"), Locale("pt", "PT"),
            Locale("ru"), Locale("uk"), Locale("vi"), Locale("zh"), Locale("zh", "CN"), Locale("zh", "TW")
        )

        private val MESSAGES = arrayOf(
            CalculatorMessages.msg_001,
            CalculatorMessages.msg_002,
            CalculatorMessages.msg_003,
            CalculatorMessages.msg_004,
            CalculatorMessages.msg_005,
            CalculatorMessages.msg_006,
            CalculatorMessages.msg_007,
            CalculatorMessages.syntax_error,
            CalculatorMessages.result_copied,
            CalculatorMessages.text_copied,
            CalculatorMessages.ans_description
        )
    }

    @Test
    fun testAllMessages() {
        for (id in MESSAGES) {
            val arguments = makeMessageArguments(id)
            val message = CalculatorMessage(id, MessageType.info, arguments)
            for (locale in LOCALES) {
                val text = CalculatorMessages.getLocalizedMessage(message, locale)
                assertFalse(text.isEmpty())
            }

        }

    }

    private fun makeMessageArguments(id: String): List<String> {
        return when (id) {
            CalculatorMessages.msg_001,
            CalculatorMessages.msg_005,
            CalculatorMessages.msg_007 -> Arrays.asList("param0")
            else -> Collections.emptyList()
        }
    }
}
