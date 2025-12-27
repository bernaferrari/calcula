package org.solovyev.common.msg

import jscl.i18n.JsclLocale
import jscl.i18n.JsclMessageBundle

object Messages {

    fun synchronizedMessageRegistry(messageRegistry: MessageRegistry): MessageRegistry {
        return SynchronizedMessageRegistry.wrap(messageRegistry)
    }

    /**
     * @param locale     locale for which default formatting will be applied
     * @param pattern    message pattern which will be used for MessageFormat
     * @param parameters message parameters which will be used for MessageFormat
     * @return formatted message string according to default locale formatting, nested messages are
     * processed properly
     * (for each message from parameter method [Message.getLocalizedMessage] is
     * called)
     */
    fun prepareMessage(locale: JsclLocale, pattern: String, parameters: List<*>): String {
        val result: String

        if (parameters.isEmpty()) {
            result = pattern
        } else {
            result = JsclMessageBundle.formatMessage(locale, pattern, prepareParameters(parameters, locale))
        }

        return result
    }

    private fun prepareParameters(parameters: List<*>, locale: JsclLocale): List<Any?> {
        val result = mutableListOf<Any?>()

        for (param in parameters) {
            result.add(substituteParameter(param, locale))
        }

        return result
    }

    private fun substituteParameter(obj: Any?, locale: JsclLocale): Any? {
        return if (obj is Message) {
            obj.getLocalizedMessage(locale)
        } else {
            obj
        }
    }
}
