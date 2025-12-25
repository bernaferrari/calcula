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
import org.solovyev.common.msg.AbstractMessage
import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageType

/**
 * User: serso
 * Date: 9/20/12
 * Time: 8:06 PM
 */
class CalculatorMessage : AbstractMessage {

    constructor(
        messageCode: String,
        messageType: MessageType,
        vararg parameters: Any?
    ) : super(messageCode, messageType, *parameters)

    constructor(
        messageCode: String,
        messageType: MessageType,
        parameters: List<*>
    ) : super(messageCode, messageType, parameters)

    override fun getMessagePattern(locale: JsclLocale): String {
        val rb = CalculatorMessages.getBundle(locale)
        return rb.getString(getMessageCode())
    }

    companion object {
        @JvmStatic
        fun newInfoMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.info, *parameters)
        }

        @JvmStatic
        fun newWarningMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.warning, *parameters)
        }

        @JvmStatic
        fun newErrorMessage(messageCode: String, vararg parameters: Any?): Message {
            return CalculatorMessage(messageCode, MessageType.error, *parameters)
        }
    }
}
