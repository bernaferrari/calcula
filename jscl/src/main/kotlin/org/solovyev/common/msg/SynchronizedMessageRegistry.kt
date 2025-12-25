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

import org.solovyev.common.SynchronizedObject

internal class SynchronizedMessageRegistry : SynchronizedObject<MessageRegistry>, MessageRegistry {

    private constructor(delegate: MessageRegistry) : super(delegate)

    private constructor(delegate: MessageRegistry, mutex: Any) : super(delegate, mutex)

    companion object {
        fun wrap(delegate: MessageRegistry): MessageRegistry {
            return SynchronizedMessageRegistry(delegate)
        }

        fun wrap(delegate: MessageRegistry, mutex: Any): MessageRegistry {
            return SynchronizedMessageRegistry(delegate, mutex)
        }
    }

    override fun addMessage(message: Message) {
        synchronized(this.mutex) {
            delegate.addMessage(message)
        }
    }

    override fun hasMessage(): Boolean {
        synchronized(this.mutex) {
            return delegate.hasMessage()
        }
    }

    override fun getMessage(): Message {
        synchronized(this.mutex) {
            return delegate.getMessage()
        }
    }
}
