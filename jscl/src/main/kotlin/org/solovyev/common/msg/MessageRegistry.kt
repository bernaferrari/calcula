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

/**
 * Container for messages
 */
interface MessageRegistry {

    /**
     * Adds message to the registry.
     * Note: according to the implementation this method doesn't guarantee that new message will be added
     * in underlying container (e.g. if such message already exists)
     *
     * @param message message to be added
     */
    fun addMessage(message: Message)

    /**
     * @return true if there is any message available in the registry
     */
    fun hasMessage(): Boolean

    /**
     * Method returns message from registry and removes it from underlying container
     * Note: this method must be called after [MessageRegistry.hasMessage]
     *
     * @return message from the registry
     */
    fun getMessage(): Message
}
