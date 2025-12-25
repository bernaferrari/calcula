package org.solovyev.android.calculator.preferences

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.R
import org.solovyev.android.views.DiscreteSeekBar
import org.solovyev.common.NumberFormatter

class PrecisionPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DialogPreference(context, attrs, defStyleAttr) {

    init {
        isPersistent = false
        dialogLayoutResource = R.layout.preference_precision
    }

    class Dialog : PreferenceDialogFragmentCompat() {

        private var seekBar: DiscreteSeekBar? = null
        private var precision: Int = 0

        init {
            arguments = Bundle().apply {
                putString(ARG_KEY, Engine.Preferences.Output.precision.key)
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            seekBar?.let {
                outState.putInt(SAVE_STATE_PRECISION, it.getCurrentTick() + 1)
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            precision = if (savedInstanceState == null) {
                readPrecision()
            } else {
                savedInstanceState.getInt(SAVE_STATE_PRECISION, readPrecision())
            }
        }

        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)
            seekBar = view.findViewById<DiscreteSeekBar>(R.id.precision_seekbar).apply {
                max = NumberFormatter.MAX_PRECISION - 1
                setCurrentTick(precision - 1)
            }
        }

        private fun readPrecision(): Int {
            val pref = preference as DialogPreference
            val prefs = pref.sharedPreferences ?: return NumberFormatter.MIN_PRECISION
            val value = Engine.Preferences.Output.precision.getPreference(prefs)?.toInt()
                ?: NumberFormatter.MIN_PRECISION
            return value.coerceIn(NumberFormatter.MIN_PRECISION, NumberFormatter.MAX_PRECISION)
        }

        override fun onDialogClosed(positiveResult: Boolean) {
            if (!positiveResult) return

            val newPrecision = (seekBar?.getCurrentTick() ?: 0) + 1
            val pref = preference as DialogPreference

            if (pref.callChangeListener(newPrecision)) {
                pref.sharedPreferences?.edit()?.let { editor ->
                    Engine.Preferences.Output.precision.putPreference(editor, newPrecision as Integer)
                    editor.apply()
                }
            }
        }

        companion object {
            private const val SAVE_STATE_PRECISION = "PrecisionPreferenceDialog.precision"
        }
    }
}
