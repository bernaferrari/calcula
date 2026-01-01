package org.solovyev.android.calculator.language

import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.solovyev.android.calculator.AppPreferences
import java.util.Locale

class AndroidLanguages(
    private val application: Application,
    private val appPreferences: AppPreferences
) : Languages {

    private val list = mutableListOf<Language>()

    override fun getList(): List<Language> {
        if (list.isEmpty()) {
            loadList()
        }
        return list
    }

    private fun loadList() {
        tryAddLanguage("ar")
        tryAddLanguage("cs")
        tryAddLanguage("en")
        tryAddLanguage("es_ES")
        tryAddLanguage("de")
        tryAddLanguage("fi")
        tryAddLanguage("fr")
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

    private fun tryAddLanguage(localeCode: String) {
        val locale = findLocaleById(localeCode) ?: return
        val language = Language(localeCode, makeName(localeCode, locale))
        list.add(language)
    }

    override fun getCurrent(): Language {
        val code = runBlocking { appPreferences.gui.language.first() }
        return get(code)
    }

    override fun get(code: String): Language {
        if (Languages.SYSTEM_LANGUAGE_CODE == code) {
            return SYSTEM_LANGUAGE
        }
        return list.find { it.code == code } ?: SYSTEM_LANGUAGE
    }

    companion object {
        val SYSTEM_LANGUAGE = Language(Languages.SYSTEM_LANGUAGE_CODE, "System")

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

        private fun findLocaleById(id: String): Locale? {
            val locales = Locale.getAvailableLocales()
            for (locale in locales) {
                if (locale.toString() == id) {
                    return locale
                }
            }

            val language: String = if (id.contains("_")) {
                id.substringBefore("_")
            } else {
                id
            }

            for (locale in locales) {
                if (locale.language == language) {
                    return locale
                }
            }

            return null
        }

        fun wrapContext(context: Context, appPreferences: AppPreferences): Context {
            val code = runBlocking { appPreferences.gui.language.first() }
            return wrapContext(context, code)
        }

        fun wrapContext(context: Context, code: String): Context {
            if (code == Languages.SYSTEM_LANGUAGE_CODE) {
                return context
            }
            val locale = findLocaleById(code) ?: return context

            if (Locale.getDefault() != locale) {
                Locale.setDefault(locale)
            }

            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }
}
