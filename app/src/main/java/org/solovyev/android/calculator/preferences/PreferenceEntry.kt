package org.solovyev.android.calculator.preferences

import android.content.Context

interface PreferenceEntry {
    fun getName(context: Context): CharSequence
    val id: CharSequence
}
