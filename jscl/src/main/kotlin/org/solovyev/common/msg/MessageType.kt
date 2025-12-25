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

import org.solovyev.common.msg.MessageLevel.Companion.ERROR_LEVEL
import org.solovyev.common.msg.MessageLevel.Companion.INFO_LEVEL
import org.solovyev.common.msg.MessageLevel.Companion.WARNING_LEVEL

enum class MessageType(
    private val messageLevel: Int,
    private val stringValue: String
) : MessageLevel {

    error(ERROR_LEVEL, "ERROR"),
    warning(WARNING_LEVEL, "WARNING"),
    info(INFO_LEVEL, "INFO");

    companion object {
        fun getLowestMessageType(): MessageType {
            return info
        }
    }

    fun getStringValue(): String {
        return stringValue
    }

    override fun getMessageLevel(): Int {
        return messageLevel
    }

    override fun getName(): String {
        return stringValue
    }
}
