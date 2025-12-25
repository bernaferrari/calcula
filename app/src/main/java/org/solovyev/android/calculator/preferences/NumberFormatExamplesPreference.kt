package org.solovyev.android.calculator.preferences

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import jscl.JsclMathEngine
import kotlin.math.pow
import kotlin.math.sqrt

class NumberFormatExamplesPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    fun update(engine: JsclMathEngine) {
        summary = buildString {
            append("     1/3 = ").append(engine.format(1.0 / 3)).append("\n")
            append("      √2 = ").append(engine.format(sqrt(2.0))).append("\n")
            append("\n")
            append("    1000 = ").append(engine.format(1000.0)).append("\n")
            append(" 1000000 = ").append(engine.format(1000000.0)).append("\n")
            append("   11^10 = ").append(engine.format(11.0.pow(10.0))).append("\n")
            append("   10^24 = ").append(engine.format(10.0.pow(24.0))).append("\n")
            append("\n")
            append("   0.001 = ").append(engine.format(0.001)).append("\n")
            append("0.000001 = ").append(engine.format(0.000001)).append("\n")
            append("  11^−10 = ").append(engine.format(11.0.pow(-10.0))).append("\n")
            append("  10^−24 = ").append(engine.format(10.0.pow(-24.0)))
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(android.R.id.summary) as? TextView)?.apply {
            maxLines = 12
            setLines(12)
            typeface = Typeface.MONOSPACE
        }
    }
}
