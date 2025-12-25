package org.solovyev.android.calculator.language

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import org.solovyev.android.Check
import org.solovyev.android.calculator.Preferences
import java.util.Locale

class Languages(
    private val application: Application,
    private val preferences: SharedPreferences
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val list = mutableListOf<Language>()

    /**
     * This method should be called only when default values have been set to application's preferences
     */
    fun init() {
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    fun getList(): List<Language> {
        Check.isMainThread()
        if (list.isEmpty()) {
            loadList()
        }
        return list
    }

    private fun loadList() {
        Check.isMainThread()
        Check.isEmpty(list)

        tryAddLanguage("ar")
        tryAddLanguage("cs")
        tryAddLanguage("en")
        tryAddLanguage("es_ES")
        tryAddLanguage("de")
        tryAddLanguage("fi")
        tryAddLanguage("fr")
        tryAddLanguage("it")
        tryAddLanguage("it")
        tryAddLanguage("pl")
        tryAddLanguage("pt_BR")
        tryAddLanguage("pt_PT")
        tryAddLanguage("ru")
        tryAddLanguage("tr")
        tryAddLanguage("vi")
        tryAddLanguage("uk")
        tryAddLanguage("ja")
        tryAddLanguage("zh_CN")
        tryAddLanguage("zh_TW")

        list.sortBy { it.name }
        list.add(0, SYSTEM_LANGUAGE)
    }

    private fun tryAddLanguage(locale: String) {
        val language = makeLanguage(locale)
        if (language != null) {
            list.add(language)
        }
    }

    fun getCurrent(): Language {
        return get(Preferences.Gui.language.getPreference(preferences) ?: SYSTEM_LANGUAGE_CODE)
    }

    fun get(code: String): Language {
        if (SYSTEM_LANGUAGE.code == code) {
            // quick check to avoid list loading
            return SYSTEM_LANGUAGE
        }
        return findLanguageByCode(code) ?: SYSTEM_LANGUAGE
    }

    private fun findLanguageByCode(code: String): Language? {
        return getList().find { it.code == code }
    }

    override fun onSharedPreferenceChanged(p: SharedPreferences, key: String?) {
        if (key != null && Preferences.Gui.language.isSameKey(key)) {
            updateContextLocale(application, false)
        }
    }

    fun updateContextLocale(context: Context, initial: Boolean) {
        val language = getCurrent()
        // we don't need to set system language while starting up the app
        if (initial && language.isSystem()) {
            return
        }
        if (Locale.getDefault() != language.locale) {
            Locale.setDefault(language.locale)
        }

        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        if (configuration.locale == null || configuration.locale != language.locale) {
            configuration.locale = language.locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, displayMetrics)
        }
    }

    companion object {
        const val SYSTEM_LANGUAGE_CODE = "00"
        val SYSTEM_LANGUAGE = Language(SYSTEM_LANGUAGE_CODE, Locale.getDefault())

        private var locales: Array<Locale>? = null

        private fun makeLanguage(localeId: String): Language? {
            val locale = findLocaleById(localeId) ?: return null
            return Language(localeId, locale)
        }

        private fun findLocaleById(id: String): Locale? {
            for (locale in getLocales()) {
                if (locale.toString() == id) {
                    return locale
                }
            }

            val language: String = if (id.contains("_")) {
                id.substringBefore("_")
            } else {
                id
            }

            for (locale in getLocales()) {
                if (locale.language == language) {
                    return locale
                }
            }

            Log.d("Languages", "No locale found for $id")
            return null
        }

        fun getLocales(): Array<Locale> {
            if (locales == null) {
                locales = Locale.getAvailableLocales()
            }
            return locales!!
        }
    }
}
