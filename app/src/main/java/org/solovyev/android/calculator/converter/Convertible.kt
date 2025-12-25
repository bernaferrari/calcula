package org.solovyev.android.calculator.converter

import android.content.Context
import org.solovyev.android.calculator.Named

interface Convertible {
    @Throws(NumberFormatException::class)
    fun convert(to: Convertible, value: String): String

    fun named(context: Context): Named<Convertible>
}
