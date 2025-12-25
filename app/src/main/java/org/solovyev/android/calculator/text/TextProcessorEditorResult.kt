package org.solovyev.android.calculator.text

/**
 * User: serso
 * Date: 6/27/13
 * Time: 8:07 PM
 */
data class TextProcessorEditorResult(
    private val charSequence: CharSequence,
    val offset: Int
) : CharSequence by charSequence {

    private var cachedString: String? = null

    override fun toString(): String {
        return cachedString ?: charSequence.toString().also { cachedString = it }
    }

    fun getCharSequence(): CharSequence = charSequence
}
