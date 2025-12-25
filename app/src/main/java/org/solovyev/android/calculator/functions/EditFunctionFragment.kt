package org.solovyev.android.calculator.functions

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import jscl.math.function.Function
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.RemovalConfirmationDialog
import org.solovyev.android.calculator.Utils

class EditFunctionFragment : BaseFunctionFragment(R.layout.fragment_function_edit) {

    override fun applyData(function: CppFunction): Boolean {
        return try {
            val oldFunction: Function? = if (isNewFunction) null else functionsRegistry.getById(function.id)
            functionsRegistry.addOrUpdate(function.toJsclBuilder().create(), oldFunction)
            true
        } catch (e: RuntimeException) {
            setError(bodyLabel, Utils.getErrorMessage(e))
            false
        }
    }

    override fun validateName(): Boolean {
        val name = nameView.text.toString()
        if (name.isEmpty()) {
            setError(nameLabel, getString(R.string.cpp_field_cannot_be_empty))
            return false
        }
        if (!Engine.isValidName(name)) {
            setError(nameLabel, getString(R.string.cpp_name_contains_invalid_characters))
            return false
        }
        
        val existingFunction = functionsRegistry.get(name)
        if (existingFunction != null) {
            if (!existingFunction.isIdDefined()) {
                Check.shouldNotHappen()
                setError(nameLabel, getString(R.string.function_already_exists))
                return false
            }
            if (isNewFunction) {
                setError(nameLabel, getString(R.string.function_already_exists))
                return false
            }
            Check.isNotNull(function)
            if (existingFunction.getId() != function?.id) {
                setError(nameLabel, getString(R.string.function_already_exists))
                return false
            }
        }
        clearError(nameLabel)
        return true
    }

    override fun showRemovalDialog(function: CppFunction) {
        RemovalConfirmationDialog.showForFunction(requireActivity(), function.name) { dialog, which ->
            Check.isTrue(which == DialogInterface.BUTTON_POSITIVE)
            functionsRegistry.remove(function.toJsclBuilder().create())
            dismiss()
        }
    }

    companion object {
        private const val ARG_FUNCTION = "function"

        @JvmStatic
        fun show(activity: FragmentActivity) {
            show(null, activity.supportFragmentManager)
        }

        @JvmStatic
        fun show(function: CppFunction?, context: Context) {
            if (context !is FunctionsActivity) {
                val intent = Intent(context, FunctionsActivity.getClass(context)).apply {
                    App.addIntentFlags(this, false, context)
                    putExtra(FunctionsActivity.EXTRA_FUNCTION, function)
                }
                context.startActivity(intent)
            } else {
                show(function, context.supportFragmentManager)
            }
        }

        @JvmStatic
        fun show(function: CppFunction?, fm: FragmentManager) {
            App.showDialog(create(function), "function-editor", fm)
        }

        private fun create(function: CppFunction?): BaseFunctionFragment {
            return EditFunctionFragment().apply {
                if (function != null) {
                    arguments = Bundle().apply {
                        putParcelable(ARG_FUNCTION, function)
                    }
                }
            }
        }
    }
}
