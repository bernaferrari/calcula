package org.solovyev.android.calculator.language

data class Language(
    val code: String,
    val name: String
) {
    fun isSystem(): Boolean = code == Languages.SYSTEM_LANGUAGE_CODE
}
