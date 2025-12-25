package org.solovyev.android.calculator.converter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputLayout
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.AppComponent
import org.solovyev.android.calculator.AppModule
import org.solovyev.android.calculator.BaseDialogFragment
import org.solovyev.android.calculator.Clipboard
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.Named
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.databinding.CppUnitConverterBinding
import org.solovyev.android.calculator.keyboard.FloatingKeyboard
import org.solovyev.android.calculator.keyboard.FloatingKeyboardWindow
import org.solovyev.android.calculator.keyboard.FloatingNumberKeyboard
import org.solovyev.android.calculator.math.MathUtils
import org.solovyev.android.calculator.text.NaturalComparator
import org.solovyev.android.calculator.view.EditTextCompat
import javax.inject.Inject
import javax.inject.Named as JavaxNamed

class ConverterFragment : BaseDialogFragment(),
    AdapterView.OnItemSelectedListener,
    View.OnFocusChangeListener,
    TextView.OnEditorActionListener,
    View.OnClickListener,
    TextWatcher {

    @Inject
    lateinit var clipboard: Clipboard

    @Inject
    lateinit var keyboard: Keyboard

    @JavaxNamed(AppModule.PREFS_UI)
    @Inject
    lateinit var uiPreferences: SharedPreferences

    @Inject
    lateinit var editor: Editor

    private lateinit var dimensionsSpinner: Spinner
    private lateinit var spinnerFrom: Spinner
    private lateinit var labelFrom: TextInputLayout
    private lateinit var editTextFrom: EditTextCompat
    private lateinit var spinnerTo: Spinner
    private lateinit var labelTo: TextInputLayout
    private lateinit var editTextTo: EditText
    private lateinit var swapButton: ImageButton

    private lateinit var dimensionsAdapter: ArrayAdapter<Named<ConvertibleDimension>>
    private lateinit var adapterFrom: ArrayAdapter<Named<Convertible>>
    private lateinit var adapterTo: ArrayAdapter<Named<Convertible>>

    private val keyboardWindow = FloatingKeyboardWindow(null)
    private var pendingFromSelection = NONE
    private var pendingToSelection = NONE
    private var useSystemKeyboard = true

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onPrepareDialog(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.c_use, null)
        builder.setNegativeButton(R.string.cpp_cancel, null)
        builder.setNeutralButton(R.string.cpp_copy, null)
    }


    @SuppressLint("InflateParams")
    override fun onCreateDialogView(
        context: Context,
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        val binding = CppUnitConverterBinding.inflate(inflater, null, false)

        dimensionsSpinner = binding.converterDimensionsSpinner
        spinnerFrom = binding.converterSpinnerFrom
        labelFrom = binding.converterLabelFrom
        editTextFrom = binding.converterEdittextFrom
        spinnerTo = binding.converterSpinnerTo
        labelTo = binding.converterLabelTo
        editTextTo = binding.converterEdittextTo
        swapButton = binding.converterSwapButton

        dimensionsAdapter = App.makeSimpleSpinnerAdapter(context)
        UnitDimension.values().forEach { dimension ->
            dimensionsAdapter.add(dimension.named(context))
        }
        dimensionsAdapter.add(NumeralBaseDimension.get().named(context))

        adapterFrom = App.makeSimpleSpinnerAdapter(context)
        adapterTo = App.makeSimpleSpinnerAdapter(context)

        dimensionsSpinner.adapter = dimensionsAdapter
        spinnerFrom.adapter = adapterFrom
        spinnerTo.adapter = adapterTo

        dimensionsSpinner.onItemSelectedListener = this
        spinnerFrom.onItemSelectedListener = this
        spinnerTo.onItemSelectedListener = this

        editTextFrom.onFocusChangeListener = this
        editTextFrom.setOnEditorActionListener(this)
        editTextFrom.addTextChangedListener(this)
        editTextFrom.setOnClickListener(this)
        onKeyboardTypeChanged()

        swapButton.setOnClickListener(this)
        swapButton.setImageResource(
            if (App.getTheme().light) R.drawable.ic_swap_vert_black_24dp
            else R.drawable.ic_swap_vert_white_24dp
        )

        if (savedInstanceState == null) {
            val args = arguments ?: Bundle()
            editTextFrom.setText(args.getDouble(EXTRA_VALUE, 1.0).toString())
            pendingFromSelection = org.solovyev.android.calculator.UiPreferences.Converter
                .lastUnitsFrom.getPreference(uiPreferences) ?: NONE
            pendingToSelection = org.solovyev.android.calculator.UiPreferences.Converter
                .lastUnitsTo.getPreference(uiPreferences) ?: NONE
            dimensionsSpinner.setSelection(
                MathUtils.clamp(
                    org.solovyev.android.calculator.UiPreferences.Converter
                        .lastDimension.getPreference(uiPreferences) ?: 0,
                    0,
                    dimensionsAdapter.count - 1
                )
            )
        } else {
            pendingFromSelection = savedInstanceState.getInt(STATE_SELECTION_FROM, NONE)
            pendingToSelection = savedInstanceState.getInt(STATE_SELECTION_TO, NONE)
        }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SELECTION_FROM, spinnerFrom.selectedItemPosition)
        outState.putInt(STATE_SELECTION_TO, spinnerTo.selectedItemPosition)
    }

    override fun onPause() {
        saveLastUsedValues()
        super.onPause()
    }

    private fun saveLastUsedValues() {
        uiPreferences.edit().apply {
            org.solovyev.android.calculator.UiPreferences.Converter.lastDimension
                .putPreference(this, dimensionsSpinner.selectedItemPosition)
            org.solovyev.android.calculator.UiPreferences.Converter.lastUnitsFrom
                .putPreference(this, spinnerFrom.selectedItemPosition)
            org.solovyev.android.calculator.UiPreferences.Converter.lastUnitsTo
                .putPreference(this, spinnerTo.selectedItemPosition)
            apply()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.let {
            when (it.id) {
                R.id.converter_dimensions_spinner -> {
                    onDimensionChanged(dimensionsAdapter.getItem(position)!!.item)
                }
                R.id.converter_spinner_from -> {
                    onUnitFromChanged(adapterFrom.getItem(position)!!.item)
                }
                R.id.converter_spinner_to -> {
                    convert()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun onUnitFromChanged(unit: Convertible) {
        val dimensionPosition = dimensionsSpinner.selectedItemPosition
        updateUnitsTo(dimensionsAdapter.getItem(dimensionPosition)!!.item, unit)
        convert()
    }

    private fun onDimensionChanged(dimension: ConvertibleDimension) {
        updateUnitsFrom(dimension)
        updateUnitsTo(dimension, adapterFrom.getItem(spinnerFrom.selectedItemPosition)!!.item)
        convert()
        checkKeyboardType(dimension)
    }

    private fun checkKeyboardType(dimension: ConvertibleDimension) {
        keyboardWindow.hide()
        useSystemKeyboard = dimension !is NumeralBaseDimension
        onKeyboardTypeChanged()
    }

    private fun onKeyboardTypeChanged() {
        editTextFrom.inputType = if (useSystemKeyboard) NUMBER_INPUT_TYPE else InputType.TYPE_CLASS_TEXT
        editTextFrom.setShowSoftInputOnFocusCompat(useSystemKeyboard)
    }

    private fun updateUnitsFrom(dimension: ConvertibleDimension) {
        adapterFrom.setNotifyOnChange(false)
        adapterFrom.clear()
        dimension.getUnits().forEach { unit ->
            adapterFrom.add(unit.named(requireActivity()))
        }
        adapterFrom.sort(NaturalComparator)
        adapterFrom.setNotifyOnChange(true)
        adapterFrom.notifyDataSetChanged()

        spinnerFrom.setSelection(
            if (pendingFromSelection != NONE) {
                MathUtils.clamp(pendingFromSelection, 0, adapterFrom.count - 1)
                    .also { pendingFromSelection = NONE }
            } else {
                MathUtils.clamp(spinnerFrom.selectedItemPosition, 0, adapterFrom.count - 1)
            }
        )
    }

    private fun updateUnitsTo(dimension: ConvertibleDimension, except: Convertible) {
        val selectedUnit = if (pendingToSelection > NONE) {
            null
        } else {
            val selectedPosition = spinnerTo.selectedItemPosition
            if (selectedPosition >= 0 && selectedPosition < adapterTo.count) {
                adapterTo.getItem(selectedPosition)?.item
            } else {
                null
            }
        }

        adapterTo.setNotifyOnChange(false)
        adapterTo.clear()
        dimension.getUnits().forEach { unit ->
            if (unit != except) {
                adapterTo.add(unit.named(requireActivity()))
            }
        }
        adapterTo.sort(NaturalComparator)
        adapterTo.setNotifyOnChange(true)
        adapterTo.notifyDataSetChanged()

        if (selectedUnit != null && selectedUnit != except) {
            for (i in 0 until adapterTo.count) {
                val unit = adapterTo.getItem(i)?.item
                if (unit == selectedUnit) {
                    spinnerTo.setSelection(i)
                    return
                }
            }
        }

        spinnerTo.setSelection(
            if (pendingToSelection != NONE) {
                MathUtils.clamp(pendingToSelection, 0, adapterTo.count - 1)
                    .also { pendingToSelection = NONE }
            } else {
                MathUtils.clamp(spinnerTo.selectedItemPosition, 0, adapterTo.count - 1)
            }
        )
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (v.id == R.id.converter_edittext_from) {
            if (!hasFocus) {
                convert()
            } else {
                clearError(labelFrom)
                showKeyboard()
            }
        }
    }

    private fun showKeyboard() {
        if (useSystemKeyboard) {
            return
        }
        keyboardWindow.show(FloatingNumberKeyboard(KeyboardUser()), null)
    }

    private fun convert(validate: Boolean = true) {
        val value = editTextFrom.text.toString()
        if (TextUtils.isEmpty(value)) {
            if (validate) {
                setError(labelFrom, R.string.cpp_nan)
            }
            return
        }

        try {
            val from = adapterFrom.getItem(spinnerFrom.selectedItemPosition)!!.item
            val to = adapterTo.getItem(spinnerTo.selectedItemPosition)!!.item
            editTextTo.setText(from.convert(to, value))
            clearError(labelFrom)
        } catch (e: RuntimeException) {
            editTextTo.setText("")
            if (validate) {
                setError(labelFrom, R.string.cpp_nan)
            }
        }
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (v.id == R.id.converter_edittext_from) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                App.hideIme(editTextFrom)
                convert()
                return true
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.converter_swap_button -> {
                keyboardWindow.hide()
                swap()
            }
            R.id.converter_edittext_from -> {
                showKeyboard()
            }
            else -> super.onClick(v)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            dismiss()
            return
        }

        val text = editTextTo.text.toString()
        try {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    editor.insert(text)
                    dismiss()
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    clipboard.setText(text)
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.cpp_text_copied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (ignored: RuntimeException) {
        }
    }

    private fun swap() {
        editTextFrom.setText(editTextTo.text)
        val oldFromUnit = adapterFrom.getItem(spinnerFrom.selectedItemPosition)!!.item
        val oldToUnit = adapterTo.getItem(spinnerTo.selectedItemPosition)!!.item

        pendingToSelection = NONE
        for (i in 0 until adapterFrom.count) {
            pendingToSelection++
            val unit = adapterFrom.getItem(i)!!.item
            if (unit == oldToUnit) {
                pendingToSelection--
            } else if (unit == oldFromUnit) {
                break
            }
        }

        for (i in 0 until adapterFrom.count) {
            val unit = adapterFrom.getItem(i)!!.item
            if (unit == oldToUnit) {
                spinnerFrom.setSelection(i)
                break
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        convert(false)
    }

    override fun dismiss() {
        App.hideIme(this)
        super.dismiss()
    }

    private inner class KeyboardUser : FloatingKeyboard.User {
        override fun getContext(): Context = requireActivity()

        override fun getEditor(): EditText = editTextFrom

        override fun getKeyboard(): ViewGroup = keyboardWindow.getContentView()

        override fun done() {
            keyboardWindow.hide()
            convert()
        }

        override fun showIme() {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editTextFrom, InputMethodManager.SHOW_FORCED)
            keyboardWindow.hide()
        }

        override fun isVibrateOnKeypress(): Boolean = keyboard.vibrateOnKeypress.value

        override fun getTypeface(): Typeface = typeface
    }

    companion object {
        private const val NUMBER_INPUT_TYPE =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        private const val STATE_SELECTION_FROM = "selection.from"
        private const val STATE_SELECTION_TO = "selection.to"
        private const val EXTRA_VALUE = "value"
        const val NONE = -1

        @JvmStatic
        fun show(activity: FragmentActivity) {
            show(activity, 1.0)
        }

        @JvmStatic
        fun show(activity: FragmentActivity, value: Double) {
            val fragment = ConverterFragment()
            val args = Bundle(1)
            args.putDouble(EXTRA_VALUE, value)
            fragment.arguments = args
            App.showDialog(fragment, "converter", activity.supportFragmentManager)
        }
    }
}
