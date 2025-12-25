/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator

import jscl.i18n.JsclLocale
import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageLevel

class ParseException : RuntimeException, Message {

    private val internalMessage: Message
    val expression: String
    val position: Int?

    constructor(jsclParseException: jscl.text.ParseException) {
        this.internalMessage = jsclParseException
        this.expression = jsclParseException.expression
        this.position = jsclParseException.position
    }

    constructor(
        position: Int?,
        expression: String,
        message: Message
    ) {
        this.internalMessage = message
        this.expression = expression
        this.position = position
    }

    constructor(
        expression: String,
        message: Message
    ) : this(null, expression, message)

    override fun getMessageCode(): String {
        return internalMessage.getMessageCode()
    }

    override fun getParameters(): List<Any> {
        return internalMessage.getParameters()
    }

    override fun getMessageLevel(): MessageLevel {
        return internalMessage.getMessageLevel()
    }

    override fun getLocalizedMessage(): String {
        return internalMessage.getLocalizedMessage(JsclLocale.getDefault())
    }

    override fun getLocalizedMessage(locale: JsclLocale): String {
        return internalMessage.getLocalizedMessage(locale)
    }

    override val message: String?
        get() = getLocalizedMessage()
}
