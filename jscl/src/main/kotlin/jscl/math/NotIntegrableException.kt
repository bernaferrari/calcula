package jscl.math

import jscl.JsclArithmeticException
import jscl.text.msg.Messages

open class NotIntegrableException : JsclArithmeticException {
    constructor(messageCode: String, vararg parameters: Any?) : super(messageCode, parameters = parameters.filterNotNull().toTypedArray())

    constructor(e: Expression) : this(Messages.msg_21, e.toString())

    constructor(v: Variable) : this(Messages.msg_21, v.name)

    constructor() : this(Messages.msg_22)
}
