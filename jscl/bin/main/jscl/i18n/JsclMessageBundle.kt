package jscl.i18n

import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * Abstraction over ResourceBundle for localized messages.
 *
 * This wrapper maintains the same API while allowing platform-specific
 * implementations for different KMP targets in the future.
 *
 * Currently wraps java.util.ResourceBundle for JVM/Android compatibility.
 */
object JsclMessageBundle {

    private const val BUNDLE_PATH = "jscl/text/msg/messages"

    /**
     * Gets a localized message string for the given message code.
     *
     * @param messageCode the message code to look up
     * @param locale the locale for the message
     * @return the localized message string
     */
    fun getString(messageCode: String, locale: JsclLocale): String {
        val bundle = ResourceBundle.getBundle(BUNDLE_PATH, locale.toJavaLocale())
        return bundle.getString(messageCode)
    }

    /**
     * Formats a message with parameters according to the locale.
     *
     * @param locale the locale for formatting
     * @param pattern the message pattern (may contain {0}, {1}, etc.)
     * @param parameters the parameters to substitute
     * @return the formatted message
     */
    fun formatMessage(locale: JsclLocale, pattern: String, parameters: List<*>): String {
        if (parameters.isEmpty()) {
            return pattern
        }
        val format = MessageFormat(pattern, locale.toJavaLocale())
        return format.format(parameters.toTypedArray())
    }
}
