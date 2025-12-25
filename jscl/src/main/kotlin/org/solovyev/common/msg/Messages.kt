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
 * ---------------------------------------------------------------------
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

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
