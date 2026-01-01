
package org.solovyev.android.calculator.preferences

// Placeholder for PreferenceEntry if it's used as a type constraint
interface PreferenceEntry {
    val id: CharSequence
    fun getName(context: Any): CharSequence // 'context' here is tricky. Abstract it out.
}
