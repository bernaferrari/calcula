package org.solovyev.android.calculator.history

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.BaseDialogFragment
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.databinding.FragmentHistoryEditBinding
import javax.inject.Inject

@AndroidEntryPoint
class EditHistoryFragment : BaseDialogFragment() {

    @Inject
    lateinit var history: History

    private lateinit var state: HistoryState
    private var newState: Boolean = false

    private lateinit var expressionView: TextView
    private lateinit var commentView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = requireArguments()
        state = arguments.getParcelable(ARG_STATE)!!
        newState = arguments.getBoolean(ARG_NEW)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onPrepareDialog(builder: AlertDialog.Builder) {
        builder.setNegativeButton(R.string.cpp_cancel, null)
        builder.setPositiveButton(R.string.c_save, null)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val builder = HistoryState.builder(state, newState)
                    .withComment(commentView.text.toString())
                history.updateSaved(builder.build())
                dismiss()
            }
            else -> super.onClick(dialog, which)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialogView(
        context: Context,
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryEditBinding.inflate(inflater, null, false)
        expressionView = binding.historyExpression
        commentView = binding.historyComment

        if (savedInstanceState == null) {
            expressionView.text = BaseHistoryFragment.getHistoryText(state)
            commentView.setText(state.comment)
        }

        return binding.root
    }

    companion object {
        const val ARG_STATE = "state"
        const val ARG_NEW = "new"

        @JvmStatic
        fun show(state: HistoryState, newState: Boolean, fm: FragmentManager) {
            val fragment = EditHistoryFragment()
            val args = Bundle()
            args.putParcelable(ARG_STATE, state)
            args.putBoolean(ARG_NEW, newState)
            fragment.arguments = args
            fragment.show(fm, "edit-history-fragment")
        }
    }
}
