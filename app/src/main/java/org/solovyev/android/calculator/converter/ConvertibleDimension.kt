package org.solovyev.android.calculator.converter

import android.content.Context
import org.solovyev.android.calculator.Named

interface ConvertibleDimension {
    fun named(context: Context): Named<ConvertibleDimension>

    fun getUnits(): List<Convertible>
}
