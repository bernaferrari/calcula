package jscl

import jscl.math.Generic
import jscl.text.ParseException
import jscl.common.msg.MessageRegistry

interface MathEngine : MathContext {

    @Throws(ParseException::class)
    fun evaluate(expression: String): String

    @Throws(ParseException::class)
    fun simplify(expression: String): String

    @Throws(ParseException::class)
    fun elementary(expression: String): String

    @Throws(ParseException::class)
    fun evaluateGeneric(expression: String): Generic

    @Throws(ParseException::class)
    fun simplifyGeneric(expression: String): Generic

    @Throws(ParseException::class)
    fun elementaryGeneric(expression: String): Generic

    fun getMessageRegistry(): MessageRegistry

    fun setMessageRegistry(messageRegistry: MessageRegistry)
}
