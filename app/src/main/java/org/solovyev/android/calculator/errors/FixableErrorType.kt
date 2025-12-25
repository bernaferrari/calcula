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

package org.solovyev.android.calculator.errors

import android.content.SharedPreferences
import jscl.AngleUnit
import jscl.text.msg.Messages
import org.solovyev.android.calculator.Engine

sealed class FixableErrorType(private val messageCodes: List<String>) {

    object MustBeRad : FixableErrorType(
        listOf(Messages.msg_23, Messages.msg_24, Messages.msg_25)
    ) {
        override fun fix(preferences: SharedPreferences) {
            Engine.Preferences.angleUnit.putPreference(preferences, AngleUnit.rad)
        }
    }

    abstract fun fix(preferences: SharedPreferences)

    companion object {
        fun values(): Array<FixableErrorType> = arrayOf(MustBeRad)

        fun getErrorByCode(code: String): FixableErrorType? {
            return values().firstOrNull { code in it.messageCodes }
        }
    }
}

// Extension to support ordinal() for backward compatibility with Java enum
val FixableErrorType.ordinal: Int
    get() = when (this) {
        FixableErrorType.MustBeRad -> 0
    }
