package jscl.i18n

import java.util.Locale

/**
 * Abstraction over java.util.Locale to enable KMP support in the future.
 *
 * This wrapper maintains the same API as java.util.Locale while allowing
 * platform-specific implementations for different KMP targets.
 *
 * Currently wraps java.util.Locale for JVM/Android compatibility.
 */
class JsclLocale private constructor(private val locale: Locale) {

    companion object {
        /**
         * Common locales
         */
        val ENGLISH: JsclLocale = JsclLocale(Locale.ENGLISH)

        /**
         * Returns the current default locale.
         */
        fun getDefault(): JsclLocale = JsclLocale(Locale.getDefault())

        /**
         * Creates a locale from language code.
         */
        fun forLanguage(language: String): JsclLocale = JsclLocale(Locale(language))

        /**
         * Creates a locale from language and country codes.
         */
        fun forLanguageAndCountry(language: String, country: String): JsclLocale =
            JsclLocale(Locale(language, country))
    }

    /**
     * Returns the underlying java.util.Locale for internal use.
     * Internal API - should not be used by external code.
     */
    internal fun toJavaLocale(): Locale = locale

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsclLocale) return false
        return locale == other.locale
    }

    override fun hashCode(): Int = locale.hashCode()

    override fun toString(): String = locale.toString()
}
