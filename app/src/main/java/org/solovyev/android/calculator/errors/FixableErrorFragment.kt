package org.solovyev.android.calculator.errors

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.AppComponent
import org.solovyev.android.calculator.BaseDialogFragment
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.UiPreferences
import javax.inject.Inject

class FixableErrorFragment : BaseDialogFragment() {

    @Inject
    lateinit var uiPreferences: UiPreferences

    private lateinit var error: FixableError
    private var activity: FixableErrorsActivity? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.activity = activity as? FixableErrorsActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        error = requireArguments().getParcelable(ARG_ERROR)!!
        Check.isNotNull(error)
    }

    override fun onPrepareDialog(builder: AlertDialog.Builder) {
        builder.setMessage(error.message)
        builder.setNeutralButton(R.string.cpp_dont_show_again, null)
        builder.setNegativeButton(R.string.close, null)
        if (error.error != null) {
            builder.setPositiveButton(R.string.fix, null)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_NEUTRAL -> {
                uiPreferences.showFixableErrorDialog = false
                dismiss()
            }
            DialogInterface.BUTTON_POSITIVE -> {
                error.error?.fix(preferences)
                dismiss()
            }
            else -> super.onClick(dialog, which)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.onDialogClosed()
        activity = null
    }

    companion object {
        const val FRAGMENT_TAG = "fixable-error"
        private const val ARG_ERROR = "error"

        private fun create(error: FixableError): FixableErrorFragment {
            return FixableErrorFragment().apply {
                arguments = Bundle(1).apply {
                    putParcelable(ARG_ERROR, error)
                }
            }
        }

        fun show(error: FixableError, fm: FragmentManager) {
            App.showDialog(create(error), FRAGMENT_TAG, fm)
        }
    }
}
