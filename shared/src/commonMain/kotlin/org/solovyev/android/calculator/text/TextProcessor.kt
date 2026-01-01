package org.solovyev.android.calculator.text

fun interface TextProcessor<TO : CharSequence, FROM> {
    fun process(from: FROM): TO
}
