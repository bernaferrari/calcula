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
import org.solovyev.common.msg.MessageType
import org.solovyev.common.msg.MessageType.*
import java.util.*

object CalculatorMessages {

    /* Arithmetic error occurred: {0} */
    const val msg_001 = "msg_1"
    /* Too complex expression*/
    const val msg_002 = "msg_2"
    /* Too long execution time - check the expression*/
    const val msg_003 = "msg_3"
    /* Evaluation was cancelled*/
    const val msg_004 = "msg_4"
    /* No parameters are specified for function: {0}*/
    const val msg_005 = "msg_5"
    /* Infinite loop is detected in expression*/
    const val msg_006 = "msg_6"
    /**
     * Some data could not be loaded. Contact authors of application with information below.\n\nUnable to load:\n{0}
     */
    const val msg_007 = "msg_7"
    /* Error */
    const val syntax_error = "syntax_error"
    /* Result copied to clipboard! */
    const val result_copied = "result_copied"
    /* Text copied to clipboard! */
    const val text_copied = "text_copied"
    /*	Last calculated value */
    const val ans_description = "ans_description"

    fun getBundle(): ResourceBundle = getBundle(Locale.getDefault())

    fun getBundle(locale: Locale): ResourceBundle = try {
        ResourceBundle.getBundle("org/solovyev/android/calculator/messages", locale)
    } catch (e: MissingResourceException) {
        ResourceBundle.getBundle("org/solovyev/android/calculator/messages", Locale.ENGLISH)
    }

    fun getBundle(locale: JsclLocale): ResourceBundle {
        // Convert JsclLocale to java.util.Locale using string representation
        // JsclLocale.toString() returns the same format as Locale.toString() (e.g., "en_US", "en", etc.)
        val localeString = locale.toString()
        val javaLocale = if (localeString.isEmpty()) {
            Locale.getDefault()
        } else {
            val parts = localeString.split("_")
            when (parts.size) {
                1 -> Locale(parts[0])
                2 -> Locale(parts[0], parts[1])
                else -> Locale(parts[0], parts[1], parts.drop(2).joinToString("_"))
            }
        }
        return getBundle(javaLocale)
    }

    fun newErrorMessage(messageCode: String, vararg parameters: Any?): CalculatorMessage =
        CalculatorMessage(messageCode, error, *parameters)

    fun toMessageType(messageLevel: Int): MessageType = when {
        messageLevel < info.getMessageLevel() -> info
        messageLevel < warning.getMessageLevel() -> warning
        else -> error
    }
}
