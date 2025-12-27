package org.solovyev.common.msg

import jscl.i18n.JsclLocale

/**
 * Interface represents translatable user message.
 * Implementation of this class will likely contains
 * some logic for translation message according to
 * it's message code and list of parameters.
 */
interface Message {

    /**
     * @return message code
     */
    fun getMessageCode(): String

    /**
     * @return list of message parameters
     */
    fun getParameters(): List<Any>

    /**
     * @return message level
     */
    fun getMessageLevel(): MessageLevel

    /**
     * @param locale locate to which current message should be translated
     * @return message string translated to specified locale
     */
    fun getLocalizedMessage(locale: JsclLocale): String

    /**
     * @return message string translated to deault locale (Locale.getDefault())
     */
    fun getLocalizedMessage(): String
}
