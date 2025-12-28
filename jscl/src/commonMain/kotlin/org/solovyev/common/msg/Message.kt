package org.solovyev.common.msg

/**
 * Represents a message code + parameters so localization can happen outside this module.
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
}
