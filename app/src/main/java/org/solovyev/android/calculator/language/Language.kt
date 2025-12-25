package org.solovyev.android.calculator.language

import android.content.Context
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.preferences.PreferenceEntry
import java.util.Locale

data class Language(
    val code: String,
    val locale: Locale
) : PreferenceEntry {

    val name: String = makeName(code, locale)

    override fun getName(context: Context): String {
        return if (!isSystem()) {
            name
        } else {
            context.getString(R.string.cpp_system_language) + " (" + locale.getDisplayLanguage(locale) + ")"
        }
    }

    override val id: CharSequence
        get() = code

    fun isSystem(): Boolean = code == Languages.SYSTEM_LANGUAGE_CODE

    companion object {
        private fun makeName(code: String, locale: Locale): String {
            if (code == Languages.SYSTEM_LANGUAGE_CODE) {
                return ""
            }

            val underscore = code.indexOf("_")
            if (underscore >= 0 && locale.getDisplayCountry(locale).isEmpty()) {
                return locale.getDisplayName(locale) + " (" + code.substring(underscore + 1) + ")"
            }

            return locale.getDisplayName(locale)
        }
    }
}
