package org.solovyev.android.calculator.errors

import jscl.AngleUnit
import jscl.text.msg.Messages
import org.solovyev.android.calculator.preferences.SettingsPreferencesStore

sealed class FixableErrorType(private val messageCodes: List<String>) {

    object MustBeRad : FixableErrorType(
        listOf(Messages.msg_23, Messages.msg_24, Messages.msg_25)
    ) {
        override suspend fun fix(settings: SettingsPreferencesStore) {
            settings.setAngleUnit(AngleUnit.rad)
        }
    }

    abstract suspend fun fix(settings: SettingsPreferencesStore)

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
