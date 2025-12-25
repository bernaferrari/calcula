package org.solovyev.android.calculator

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.ga.Ga
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseDialogFragment : DialogFragment(), View.OnClickListener, DialogInterface.OnClickListener {

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var ga: Ga

    @Inject
    lateinit var typeface: Typeface

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null
    private var neutralButton: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val theme = Preferences.Gui.getTheme(preferences)
        val context = requireActivity()
        val inflater = LayoutInflater.from(context)
        val builder = AlertDialog.Builder(context, theme.alertDialogTheme)
        val view = onCreateDialogView(context, inflater, savedInstanceState)

        if (view != null) {
            val spacing = context.resources.getDimensionPixelSize(R.dimen.cpp_dialog_spacing)
            builder.setView(view, spacing, spacing, spacing, spacing)
            BaseActivity.fixFonts(view, typeface)
        }

        onPrepareDialog(builder)

        val dialog = builder.create()
        dialog.setOnShowListener { d ->
            positiveButton = getButton(dialog, AlertDialog.BUTTON_POSITIVE)
            negativeButton = getButton(dialog, AlertDialog.BUTTON_NEGATIVE)
            neutralButton = getButton(dialog, AlertDialog.BUTTON_NEUTRAL)
            onShowDialog(dialog, savedInstanceState == null)
        }

        return dialog
    }

    protected open fun onShowDialog(dialog: AlertDialog, firstTime: Boolean) {
    }

    private fun getButton(dialog: AlertDialog, buttonId: Int): Button? {
        val button = dialog.getButton(buttonId)
        button?.setOnClickListener(this)
        return button
    }

    protected open fun onPrepareDialog(builder: AlertDialog.Builder) {
    }

    protected open fun onCreateDialogView(
        context: Context,
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View? {
        return null
    }

    protected fun setError(textInput: TextInputLayout, @StringRes error: Int, vararg errorArgs: Any) {
        setError(textInput, getString(error, *errorArgs))
    }

    protected fun setError(textInput: TextInputLayout, error: String) {
        textInput.error = error
        textInput.isErrorEnabled = true
    }

    protected fun clearError(textInput: TextInputLayout) {
        textInput.isErrorEnabled = false
        textInput.error = null
    }

    protected fun showIme(view: View) {
        val activity = activity ?: return
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onClick(v: View) {
        val d = dialog ?: return
        when (v) {
            positiveButton -> onClick(d, DialogInterface.BUTTON_POSITIVE)
            negativeButton -> onClick(d, DialogInterface.BUTTON_NEGATIVE)
            neutralButton -> onClick(d, DialogInterface.BUTTON_NEUTRAL)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE,
            DialogInterface.BUTTON_NEGATIVE -> dismiss()
        }
    }
}
