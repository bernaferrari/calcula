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

package org.solovyev.android.calculator.history

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.util.Date

@Root(name = "HistoryState")
internal class OldHistoryState() {

    @field:Element
    @get:JvmName("editorStateProperty")
    var editorState: OldEditorHistoryState? = null

    @field:Element
    @get:JvmName("displayStateProperty")
    var displayState: OldDisplayHistoryState? = null

    @field:Element
    @get:JvmName("timeProperty")
    var time: Long = Date().time

    @field:Element(required = false)
    @get:JvmName("commentProperty")
    var comment: String? = null

    fun getTime(): Long = time

    fun getComment(): String? = comment

    fun getEditorState(): OldEditorHistoryState {
        return editorState!!
    }

    fun getDisplayState(): OldDisplayHistoryState {
        return displayState!!
    }
}
