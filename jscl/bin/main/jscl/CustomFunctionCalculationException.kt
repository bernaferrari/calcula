package jscl

import jscl.math.function.CustomFunction
import jscl.text.msg.Messages
import org.solovyev.common.msg.Message

class CustomFunctionCalculationException(
    function: CustomFunction,
    val causeMessage: Message
) : JsclArithmeticException(Messages.msg_19, function.name, causeMessage)
