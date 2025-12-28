package org.solovyev.android.calculator

import org.solovyev.common.msg.MessageType
import org.solovyev.common.msg.MessageType.*
import org.solovyev.common.msg.Message
import java.text.MessageFormat
import java.util.*

object CalculatorMessages {

    /* Arithmetic error occurred: {0} */
    const val msg_001 = "msg_1"
    /* Too complex expression*/
    const val msg_002 = "msg_2"
    /* Too long execution time - check the expression*/
    const val msg_003 = "msg_3"
    /* Evaluation was cancelled*/
    const val msg_004 = "msg_4"
    /* No parameters are specified for function: {0}*/
    const val msg_005 = "msg_5"
    /* Infinite loop is detected in expression*/
    const val msg_006 = "msg_6"
    /**
     * Some data could not be loaded. Contact authors of application with information below.\n\nUnable to load:\n{0}
     */
    const val msg_007 = "msg_7"
    /* Error */
    const val syntax_error = "syntax_error"
    /* Result copied to clipboard! */
    const val result_copied = "result_copied"
    /* Text copied to clipboard! */
    const val text_copied = "text_copied"
    /*	Last calculated value */
    const val ans_description = "ans_description"

    fun getBundle(): ResourceBundle = getBundle(Locale.getDefault())

    fun getBundle(locale: Locale): ResourceBundle = try {
        ResourceBundle.getBundle("org/solovyev/android/calculator/messages", locale)
    } catch (e: MissingResourceException) {
        ResourceBundle.getBundle("org/solovyev/android/calculator/messages", Locale.ENGLISH)
    }

    fun getLocalizedMessage(message: Message, locale: Locale = Locale.getDefault()): String {
        val bundle = getBundle(locale)
        val code = message.getMessageCode()
        val pattern = try {
            bundle.getString(code)
        } catch (e: MissingResourceException) {
            code
        }
        val formatter = MessageFormat(pattern, locale)
        return formatter.format(message.getParameters().toTypedArray())
    }

    fun newErrorMessage(messageCode: String, vararg parameters: Any?): CalculatorMessage =
        CalculatorMessage(messageCode, error, *parameters)

    fun toMessageType(messageLevel: Int): MessageType = when {
        messageLevel < info.getMessageLevel() -> info
        messageLevel < warning.getMessageLevel() -> warning
        else -> error
    }
}
