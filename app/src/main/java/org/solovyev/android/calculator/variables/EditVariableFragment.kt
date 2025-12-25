package org.solovyev.android.calculator.variables

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import dagger.Lazy
import jscl.math.function.IConstant
import org.solovyev.android.Check
import org.solovyev.android.calculator.*
import org.solovyev.android.calculator.databinding.FragmentVariableEditBinding
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.keyboard.FloatingKeyboard
import org.solovyev.android.calculator.keyboard.FloatingKeyboardWindow
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.view.EditTextCompat
import org.solovyev.android.text.method.NumberInputFilter
import org.solovyev.common.text.Strings
import javax.inject.Inject

class EditVariableFragment : BaseDialogFragment(), 
    View.OnFocusChangeListener, View.OnKeyListener, View.OnClickListener {

    @Inject lateinit var calculator: Calculator
    @Inject lateinit var keyboard: Keyboard
    @Inject lateinit var functionsRegistry: FunctionsRegistry
    @Inject lateinit var variablesRegistry: VariablesRegistry
    @Inject lateinit var toJsclTextProcessor: Lazy<ToJsclTextProcessor>
    @Inject lateinit var engine: Engine

    private var variable: CppVariable? = null
    private val keyboardUser = KeyboardUser()
    private val keyboardWindow = FloatingKeyboardWindow(PopupWindow.OnDismissListener {
        nameView.setShowSoftInputOnFocusCompat(true)
    })

    private lateinit var nameLabel: TextInputLayout
    private lateinit var nameView: EditTextCompat
    private lateinit var keyboardButton: Button
    private lateinit var valueLabel: TextInputLayout
    private lateinit var valueView: EditText
    private lateinit var exponentButton: Button
    private lateinit var descriptionView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        variable = arguments?.getParcelable(ARG_VARIABLE)
    }

    override fun onPrepareDialog(builder: AlertDialog.Builder) {
        builder.setNegativeButton(R.string.cpp_cancel, null)
        builder.setPositiveButton(R.string.cpp_done, null)
        builder.setTitle(
            if (isNewVariable) R.string.c_var_create_var 
            else R.string.c_var_edit_var
        )
        if (!isNewVariable) {
            builder.setNeutralButton(R.string.cpp_delete, null)
        }
    }

    private val isNewVariable: Boolean
        get() = variable == null || variable?.id == CppVariable.NO_ID

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onShowDialog(dialog: AlertDialog, firstTime: Boolean) {
        if (firstTime) {
            nameView.selectAll()
            showIme(nameView)
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialogView(
        context: Context,
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentVariableEditBinding.inflate(inflater, null, false)
        nameLabel = binding.variableNameLabel
        nameView = binding.variableName
        keyboardButton = binding.variableKeyboardButton
        valueLabel = binding.variableValueLabel
        valueView = binding.variableValue
        exponentButton = binding.variableExponentButton
        descriptionView = binding.variableDescription

        if (savedInstanceState == null && variable != null) {
            nameView.setText(variable?.name)
            valueView.setText(variable?.value)
            descriptionView.setText(variable?.description)
        }

        nameView.onFocusChangeListener = this
        nameView.setOnKeyListener(this)
        valueView.onFocusChangeListener = this
        valueView.setEditableFactory(object : Editable.Factory() {
            override fun newEditable(source: CharSequence): Editable =
                NumberEditable(source)
        })
        exponentButton.setOnClickListener(this)
        descriptionView.onFocusChangeListener = this
        keyboardButton.setOnClickListener(this)

        return binding.root
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        when (v.id) {
            R.id.variable_name -> {
                if (hasFocus) {
                    clearError(nameLabel)
                } else {
                    keyboardUser.done()
                }
            }
            R.id.variable_value -> {
                if (hasFocus) {
                    clearError(valueLabel)
                } else {
                    validateValue()
                }
            }
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (v.id == R.id.variable_name) {
            if (event.action == KeyEvent.ACTION_UP &&
                keyCode == KeyEvent.KEYCODE_BACK &&
                keyboardWindow.isShown()) {
                keyboardUser.done()
                return true
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.variable_keyboard_button -> {
                if (keyboardWindow.isShown()) {
                    keyboardUser.showIme()
                } else {
                    showKeyboard()
                }
            }
            R.id.variable_exponent_button -> {
                val start = maxOf(valueView.selectionStart, 0)
                val end = maxOf(valueView.selectionEnd, 0)
                valueView.text.replace(minOf(start, end), maxOf(start, end), "E", 0, 1)
            }
            else -> super.onClick(v)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> tryClose()
            DialogInterface.BUTTON_NEUTRAL -> {
                Check.isNotNull(variable)
                showRemovalDialog(variable!!)
            }
            else -> super.onClick(dialog, which)
        }
    }

    private fun showRemovalDialog(variable: CppVariable) {
        RemovalConfirmationDialog.showForVariable(requireActivity(), variable.name) { dialog, which ->
            Check.isTrue(which == DialogInterface.BUTTON_POSITIVE)
            variablesRegistry.remove(variable.toJsclConstant())
            dismiss()
        }
    }

    private fun tryClose() {
        if (validate() && applyData()) {
            dismiss()
        }
    }

    private fun applyData(): Boolean {
        return try {
            val newVariable = CppVariable.builder(nameView.text.toString())
                .withId(if (isNewVariable) CppVariable.NO_ID else variable!!.id)
                .withValue(valueView.text.toString())
                .withDescription(descriptionView.text.toString())
                .build()
            val oldVariable = if (isNewVariable) null else variablesRegistry.getById(variable!!.id)
            variablesRegistry.addOrUpdate(newVariable.toJsclConstant(), oldVariable)
            true
        } catch (e: RuntimeException) {
            setError(valueLabel, Utils.getErrorMessage(e))
            false
        }
    }

    private fun validate(): Boolean = validateName() and validateValue()

    private fun validateValue(): Boolean {
        val value = valueView.text.toString()
        if (value.isNotEmpty()) {
            if (!isValidValue(value)) {
                setError(valueLabel, R.string.c_value_is_not_a_number)
                return false
            }
        }
        clearError(valueLabel)
        return true
    }

    private fun validateName(): Boolean {
        val name = nameView.text.toString()
        
        if (!Engine.isValidName(name)) {
            setError(nameLabel, getString(R.string.cpp_name_contains_invalid_characters))
            return false
        }

        for (c in name) {
            if (!ACCEPTABLE_CHARACTERS.contains(c.lowercaseChar())) {
                setError(nameLabel, getString(R.string.c_char_is_not_accepted, c))
                return false
            }
        }

        val existingVariable = variablesRegistry.get(name)
        if (existingVariable != null) {
            if (!existingVariable.isIdDefined()) {
                Check.shouldNotHappen()
                setError(nameLabel, getString(R.string.c_var_already_exists))
                return false
            }
            if (isNewVariable) {
                setError(nameLabel, getString(R.string.c_var_already_exists))
                return false
            }
            Check.isNotNull(variable)
            if (existingVariable.getId() != variable?.id) {
                setError(nameLabel, getString(R.string.c_var_already_exists))
                return false
            }
        }

        val type = MathType.getType(name, 0, false, engine)
        if (type.type != MathType.text && type.type != MathType.constant) {
            setError(nameLabel, getString(R.string.c_var_name_clashes))
            return false
        }

        clearError(nameLabel)
        return true
    }

    private fun isValidValue(value: String): Boolean {
        return try {
            val pe = toJsclTextProcessor.get().process(value)
            !pe.hasUndefinedVariables()
        } catch (e: RuntimeException) {
            false
        }
    }

    private fun showKeyboard() {
        nameView.dontShowSoftInputOnFocusCompat()
        keyboardWindow.show(GreekFloatingKeyboard(keyboardUser), dialog)
    }

    private class NumberEditable(source: CharSequence) : SpannableStringBuilder(source) {
        init {
            super.setFilters(arrayOf(NumberInputFilter.getInstance()))
        }

        override fun setFilters(filters: Array<InputFilter>) {
            // we don't want filters as we want to support numbers in scientific notation
        }
    }

    private inner class KeyboardUser : FloatingKeyboard.User {
        override fun getContext(): Context = requireActivity()

        override fun getEditor(): EditText = nameView

        override fun getKeyboard(): ViewGroup = keyboardWindow.getContentView()

        override fun done() {
            if (keyboardWindow.isShown()) {
                keyboardWindow.hide()
            }
            validateName()
        }

        override fun showIme() {
            val imm = getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(getEditor(), InputMethodManager.SHOW_FORCED)
            keyboardWindow.hide()
        }

        override fun isVibrateOnKeypress(): Boolean = keyboard.vibrateOnKeypress.value

        override fun getTypeface(): Typeface = this@EditVariableFragment.typeface
    }

    companion object {
        private const val ARG_VARIABLE = "variable"
        private val ACCEPTABLE_CHARACTERS = (
            "1234567890abcdefghijklmnopqrstuvwxyzйцукенгшщзхъфывапролджэячсмитьбюё_" + 
            GreekFloatingKeyboard.ALPHABET
        ).toList()

        @JvmStatic
        fun create(variable: CppVariable?): EditVariableFragment {
            return EditVariableFragment().apply {
                if (variable != null) {
                    arguments = Bundle().apply {
                        putParcelable(ARG_VARIABLE, variable)
                    }
                }
            }
        }

        @JvmStatic
        fun showDialog(activity: FragmentActivity) {
            showDialog(null, activity.supportFragmentManager)
        }

        @JvmStatic
        fun showDialog(variable: CppVariable?, context: Context) {
            if (context !is VariablesActivity) {
                val intent = Intent(context, VariablesActivity.getClass(context)).apply {
                    App.addIntentFlags(this, false, context)
                    putExtra(VariablesActivity.EXTRA_VARIABLE, variable)
                }
                context.startActivity(intent)
            } else {
                showDialog(variable, context.supportFragmentManager)
            }
        }

        @JvmStatic
        fun showDialog(variable: CppVariable?, fm: FragmentManager) {
            App.showDialog(create(variable), "variable-editor", fm)
        }
    }
}
