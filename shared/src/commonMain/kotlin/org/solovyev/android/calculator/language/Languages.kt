package org.solovyev.android.calculator.language

interface Languages {
    fun getList(): List<Language>
    fun getCurrent(): Language
    fun get(code: String): Language

    companion object {
        const val SYSTEM_LANGUAGE_CODE = "00"
    }
}
