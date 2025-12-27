package org.solovyev.android.calculator.wizard

import jscl.AngleUnit
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.preferences.SettingsPreferencesStore
import org.solovyev.common.NumberFormatter

enum class CalculatorMode {
    simple {
        override suspend fun apply(settings: SettingsPreferencesStore) {
            settings.setMode(Preferences.Gui.Mode.simple)
            settings.setAngleUnit(AngleUnit.deg)
            settings.setOutputNotation(Engine.Notation.dec)
            settings.setOutputPrecision(5)
        }
    },

    engineer {
        override suspend fun apply(settings: SettingsPreferencesStore) {
            settings.setMode(Preferences.Gui.Mode.engineer)
            settings.setAngleUnit(AngleUnit.rad)
            settings.setOutputNotation(Engine.Notation.eng)
            settings.setOutputPrecision(NumberFormatter.ENG_PRECISION)
        }
    };

    abstract suspend fun apply(settings: SettingsPreferencesStore)

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
