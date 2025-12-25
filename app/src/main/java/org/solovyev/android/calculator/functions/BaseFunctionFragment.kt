package org.solovyev.android.calculator.functions

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import jscl.math.function.IConstant
import org.solovyev.android.Check
import org.solovyev.android.calculator.*
import org.solovyev.android.calculator.keyboard.FloatingKeyboardWindow
import org.solovyev.android.calculator.view.EditTextCompat
import org.solovyev.common.math.MathRegistry
import javax.inject.Inject

abstract class BaseFunctionFragment(@LayoutRes private val layout: Int) : 
    BaseDialogFragment(), View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {

    @Inject lateinit var calculator: Calculator
    @Inject lateinit var keyboard: Keyboard
    @Inject lateinit var functionsRegistry: FunctionsRegistry
    @Inject lateinit var variablesRegistry: VariablesRegistry

    protected var function: CppFunction? = null
    protected lateinit var paramsView: FunctionParamsView
    protected lateinit var nameLabel: TextInputLayout
    protected lateinit var nameView: EditText
    protected lateinit var bodyLabel: TextInputLayout
    protected lateinit var bodyView: EditTextCompat
    protected lateinit var descriptionLabel: TextInputLayout
    protected lateinit var descriptionView: EditText

    private val keyboardWindow = FloatingKeyboardWindow(null)
    private val keyboardUser = KeyboardUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        function = arguments?.getParcelable(ARG_FUNCTION)
    }


    override fun onPrepareDialog(builder: AlertDialog.Builder) {
        builder.setNegativeButton(R.string.cpp_cancel, null)
        builder.setPositiveButton(R.string.cpp_done, null)
        builder.setTitle(
            if (isNewFunction) R.string.function_create_function 
            else R.string.function_edit_function
        )
        if (!isNewFunction) {
            builder.setNeutralButton(R.string.cpp_delete, null)
        }
    }

    protected val isNewFunction: Boolean
        get() = function == null || function?.id == CppFunction.NO_ID

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
        val view = inflater.inflate(layout, null)
        paramsView = view.findViewById(R.id.function_params)
        nameLabel = view.findViewById(R.id.function_name_label)
        nameView = view.findViewById(R.id.function_name)
        bodyLabel = view.findViewById(R.id.function_body_label)
        bodyView = view.findViewById(R.id.function_body)
        descriptionLabel = view.findViewById(R.id.function_description_label)
        descriptionView = view.findViewById(R.id.function_description)

        if (savedInstanceState == null && function != null) {
            paramsView.addParams(function!!.parameters)
            nameView.setText(function!!.name)
            descriptionView.setText(function!!.description)
            bodyView.setText(function!!.body)
        }

        nameView.onFocusChangeListener = this
        paramsView.setOnFocusChangeListener(this)
        bodyView.setOnClickListener(this)
        bodyView.onFocusChangeListener = this
        bodyView.setOnKeyListener(this)
        bodyView.dontShowSoftInputOnFocusCompat()
        descriptionView.onFocusChangeListener = this

        return view
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (v is EditText && FunctionParamsView.PARAM_VIEW_TAG == v.tag) {
            val parentView = v.parent
            if (parentView is TextInputLayout) {
                if (hasFocus) {
                    clearError(parentView)
                } else {
                    validateParameters()
                }
            }
            return
        }

        when (v.id) {
            R.id.function_name -> {
                if (hasFocus) {
                    clearError(nameLabel)
                } else {
                    validateName()
                }
            }
            R.id.function_body -> {
                if (hasFocus) {
                    clearError(bodyLabel)
                    showKeyboard()
                } else {
                    keyboardWindow.hide()
                    validateBody()
                }
            }
        }
    }

    private fun showKeyboard() {
        keyboardWindow.show(
            FloatingCalculatorKeyboard(keyboardUser, collectParameters()),
            dialog
        )
    }

    protected fun collectParameters(): List<String> =
        paramsView.params.filter { it.isNotEmpty() }

    override fun onClick(v: View) {
        if (v.id == R.id.function_body) {
            showKeyboard()
        } else {
            super.onClick(v)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> tryClose()
            DialogInterface.BUTTON_NEUTRAL -> {
                Check.isNotNull(function)
                showRemovalDialog(function!!)
            }
            else -> super.onClick(dialog, which)
        }
    }

    protected abstract fun showRemovalDialog(function: CppFunction)

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (v.id == R.id.function_body) {
            if (event.action == KeyEvent.ACTION_UP &&
                keyCode == KeyEvent.KEYCODE_BACK &&
                keyboardWindow.isShown()) {
                keyboardWindow.hide()
                return true
            }
        }
        return false
    }

    private fun tryClose() {
        if (!validate()) {
            return
        }
        val function = collectData() ?: return
        if (applyData(function)) {
            dismiss()
        }
    }

    private fun collectData(): CppFunction? {
        return try {
            val body = calculator.prepare(bodyView.text.toString()).value

            CppFunction.builder(nameView.text.toString(), body)
                .withId(if (isNewFunction) CppFunction.NO_ID else function!!.id)
                .withParameters(collectParameters())
                .withDescription(descriptionView.text.toString())
                .build()
        } catch (e: RuntimeException) {
            setError(bodyLabel, Utils.getErrorMessage(e))
            null
        }
    }

    private fun validate(): Boolean = validateName() and validateParameters() and validateBody()

    protected open fun validateName(): Boolean = true

    private fun validateBody(): Boolean {
        val body = bodyView.text.toString()
        if (body.isEmpty()) {
            setError(bodyLabel, getString(R.string.cpp_field_cannot_be_empty))
            return false
        }
        try {
            val pe = calculator.prepare(body)
            if (pe.hasUndefinedVariables()) {
                val parameters = collectParameters()
                for (undefinedVariable in pe.undefinedVariables) {
                    if (!parameters.contains(undefinedVariable.name)) {
                        setError(bodyLabel, getString(R.string.c_error))
                        return false
                    }
                }
            }
            clearError(bodyLabel)
            return true
        } catch (e: ParseException) {
            setError(bodyLabel, Utils.getErrorMessage(e))
            return false
        }
    }

    private fun validateParameters(): Boolean {
        var valid = true
        val parameters = paramsView.params
        val usedParameters = mutableSetOf<String>()

        for (i in parameters.indices) {
            val parameter = parameters[i]
            val paramLabel = paramsView.getParamLabel(i)

            when {
                parameter.isEmpty() -> clearError(paramLabel)
                !Engine.isValidName(parameter) -> {
                    valid = false
                    setError(paramLabel, getString(R.string.cpp_name_contains_invalid_characters))
                }
                usedParameters.contains(parameter) -> {
                    valid = false
                    setError(paramLabel, getString(R.string.cpp_duplicate_parameter, parameter))
                }
                else -> {
                    usedParameters.add(parameter)
                    clearError(paramLabel)
                }
            }
        }
        return valid
    }

    private inner class KeyboardUser : FloatingCalculatorKeyboard.User, MenuItem.OnMenuItemClickListener {
        override fun getContext(): Context = requireActivity()
        override fun getEditor(): EditText = bodyView
        override fun getKeyboard(): ViewGroup = keyboardWindow.getContentView()

        override fun insertOperator(operator: Char) {
            insertOperator(operator.toString())
        }

        private fun clampSelection(selection: Int): Int = selection.coerceAtLeast(0)

        override fun insertOperator(operator: String) {
            val start = clampSelection(bodyView.selectionStart)
            val end = clampSelection(bodyView.selectionEnd)
            val e = bodyView.text
            e?.replace(start, end, getOperator(start, end, e, operator))
        }

        private fun getOperator(start: Int, end: Int, e: Editable, operator: CharSequence): String {
            var spaceBefore = true
            var spaceAfter = true
            if (start > 0 && e[start - 1].isWhitespace()) {
                spaceBefore = false
            }
            if (end < e.length && e[end].isWhitespace()) {
                spaceAfter = false
            }

            return when {
                spaceBefore && spaceAfter -> " $operator "
                spaceBefore -> " $operator"
                spaceAfter -> "$operator "
                else -> operator.toString()
            }
        }

        override fun showConstants(v: View) {
            bodyView.setOnCreateContextMenuListener { menu, _, _ ->
                if (v.id == R.id.function_body) {
                    menu.clear()
                    addEntities(menu, getNamesSorted(variablesRegistry), MENU_CONSTANT)
                    bodyView.setOnCreateContextMenuListener(null)
                }
            }
            bodyView.showContextMenu()
        }

        private fun getNamesSorted(registry: MathRegistry<*>): List<String> =
            registry.getNames().sorted()

        override fun showFunctions(v: View) {
            bodyView.setOnCreateContextMenuListener { menu, _, _ ->
                if (v.id == R.id.function_body) {
                    menu.clear()
                    addEntities(menu, getNamesSorted(functionsRegistry), MENU_FUNCTION)
                    bodyView.setOnCreateContextMenuListener(null)
                }
            }
            bodyView.showContextMenu()
        }

        private fun addEntities(menu: Menu, entities: List<String>, groupId: Int) {
            for (entity in entities) {
                menu.add(groupId, Menu.NONE, Menu.NONE, entity).setOnMenuItemClickListener(this)
            }
        }

        override fun showFunctionsConstants(v: View) {
            bodyView.setOnCreateContextMenuListener { menu, _, _ ->
                if (v.id == R.id.function_body) {
                    menu.clear()
                    menu.add(MENU_CATEGORY, MENU_CONSTANT, Menu.NONE, R.string.cpp_vars_and_constants)
                        .setOnMenuItemClickListener(this)
                    menu.add(MENU_CATEGORY, MENU_FUNCTION, Menu.NONE, R.string.c_functions)
                        .setOnMenuItemClickListener(this)
                    bodyView.setOnCreateContextMenuListener(null)
                }
            }
            bodyView.showContextMenu()
        }

        override fun insertText(text: CharSequence, selectionOffset: Int) {
            EditTextCompat.insert(text, bodyView)
            if (selectionOffset != 0) {
                val selection = clampSelection(bodyView.selectionEnd)
                val newSelection = selection + selectionOffset
                val textLength = bodyView.text?.length ?: 0
                if (newSelection in 0 until textLength) {
                    bodyView.setSelection(newSelection)
                }
            }
            if (text == "x" || text == "y") {
                val possibleParam = text.toString()
                if (!collectParameters().contains(possibleParam)) {
                    paramsView.addParam(possibleParam)
                }
            }
        }

        override fun isVibrateOnKeypress(): Boolean = keyboard.vibrateOnKeypress.value
        override fun getTypeface(): Typeface = this@BaseFunctionFragment.typeface

        override fun done() {
            keyboardWindow.hide()
            validateBody()
        }

        override fun showIme() {
            val ctx = getContext()
            val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(getEditor(), InputMethodManager.SHOW_FORCED)
            keyboardWindow.hide()
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val groupId = item.groupId
            val title = item.title ?: return false
            return when (groupId) {
                MENU_FUNCTION -> {
                    val argsListIndex = title.indexOf("(")
                    if (argsListIndex < 0) {
                        insertText("$title()", -1)
                    } else {
                        insertText("${title.subSequence(0, argsListIndex)}()", -1)
                    }
                    true
                }
                MENU_CONSTANT -> {
                    insertText(title.toString(), 0)
                    true
                }
                MENU_CATEGORY -> {
                    bodyView.post {
                        when (item.itemId) {
                            MENU_FUNCTION -> showFunctions(bodyView)
                            MENU_CONSTANT -> showConstants(bodyView)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    protected abstract fun applyData(function: CppFunction): Boolean

    companion object {
        protected const val ARG_FUNCTION = "function"
        private const val MENU_FUNCTION = Menu.FIRST
        private const val MENU_CONSTANT = Menu.FIRST + 1
        private const val MENU_CATEGORY = Menu.FIRST + 2
    }
}
