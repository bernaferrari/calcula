package jscl

class NotSupportedException(
    messageCode: String,
    vararg parameters: Any
) : JsclArithmeticException(messageCode, *parameters)
