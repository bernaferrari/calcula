package org.solovyev.android.calculator

import android.app.Activity
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

class RemovalConfirmationDialog private constructor(
    private val activity: Activity,
    private val name: String,
    private val listener: DialogInterface.OnClickListener,
    @StringRes private val message: Int
) {

    private fun show() {
        AlertDialog.Builder(activity, App.getTheme().alertDialogTheme)
            .setCancelable(true)
            .setTitle(R.string.removal_confirmation)
            .setMessage(activity.getString(message, name))
            .setNegativeButton(R.string.cpp_no, null)
            .setPositiveButton(R.string.cpp_yes) { dialog, _ ->
                listener.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    companion object {
        @JvmStatic
        fun showForFunction(
            activity: Activity,
            name: String,
            listener: DialogInterface.OnClickListener
        ) {
            RemovalConfirmationDialog(
                activity,
                name,
                listener,
                R.string.function_removal_confirmation_question
            ).show()
        }

        @JvmStatic
        fun showForVariable(
            activity: Activity,
            name: String,
            listener: DialogInterface.OnClickListener
        ) {
            RemovalConfirmationDialog(
                activity,
                name,
                listener,
                R.string.c_var_removal_confirmation_question
            ).show()
        }
    }
}
