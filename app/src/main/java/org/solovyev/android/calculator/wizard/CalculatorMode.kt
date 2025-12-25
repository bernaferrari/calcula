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

package org.solovyev.android.calculator.wizard

import android.content.SharedPreferences
import jscl.AngleUnit
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences
import org.solovyev.common.NumberFormatter

enum class CalculatorMode {
    simple {
        override fun apply(preferences: SharedPreferences) {
            preferences.edit().apply {
                Preferences.Gui.mode.putPreference(this, Preferences.Gui.Mode.simple)
                Engine.Preferences.angleUnit.putPreference(this, AngleUnit.deg)
                Engine.Preferences.Output.notation.putPreference(this, Engine.Notation.dec)
                Engine.Preferences.Output.precision.putPreference(this, 5 as Integer)
                apply()
            }
        }
    },

    engineer {
        override fun apply(preferences: SharedPreferences) {
            preferences.edit().apply {
                Preferences.Gui.mode.putPreference(this, Preferences.Gui.Mode.engineer)
                Engine.Preferences.angleUnit.putPreference(this, AngleUnit.rad)
                Engine.Preferences.Output.notation.putPreference(this, Engine.Notation.eng)
                Engine.Preferences.Output.precision.putPreference(this, NumberFormatter.ENG_PRECISION as Integer)
                apply()
            }
        }
    };

    abstract fun apply(preferences: SharedPreferences)

    companion object {
        @JvmStatic
        fun getDefaultMode(): CalculatorMode = engineer

        @JvmStatic
        fun fromGuiLayout(mode: Preferences.Gui.Mode): CalculatorMode {
            return when (mode) {
                Preferences.Gui.Mode.engineer -> engineer
                Preferences.Gui.Mode.simple -> simple
            }
        }
    }
}
