package org.solovyev.android.calculator.plot

import android.content.Context
import android.content.DialogInterface
import android.graphics.RectF
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseDialogFragment
import org.solovyev.android.calculator.BaseFragment
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.databinding.FragmentPlotDimensionsBinding
import org.solovyev.android.plotter.Check
import org.solovyev.android.plotter.Plot
import org.solovyev.android.plotter.Plotter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PlotDimensionsFragment : BaseDialogFragment(), TextView.OnEditorActionListener {

    @Inject
    lateinit var plotter: Plotter

    private lateinit var xMin: EditText
    private lateinit var xMinLabel: TextInputLayout
    private lateinit var xMax: EditText
    private lateinit var xMaxLabel: TextInputLayout
    private lateinit var yMin: EditText
    private lateinit var yMinLabel: TextInputLayout
    private lateinit var yMax: EditText
    private lateinit var yMaxLabel: TextInputLayout
    private lateinit var yBounds: View

    private var bounds = RectF()
    private var d3 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = requireArguments()
        bounds = BaseFragment.getParcelable(arguments, ARG_BOUNDS)
        d3 = arguments.getBoolean(ARG_3D)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onShowDialog(dialog: AlertDialog, firstTime: Boolean) {
        super.onShowDialog(dialog, firstTime)
        if (firstTime) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(xMin, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onPrepareDialog(builder: AlertDialog.Builder) {
        builder.setTitle(R.string.cpp_plot_range)
        builder.setPositiveButton(R.string.cpp_done, null)
    }

    override fun onCreateDialogView(
        context: Context,
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPlotDimensionsBinding.inflate(inflater, null, false)

        xMin = binding.plotXMin
        xMinLabel = binding.plotXMinLabel
        xMax = binding.plotXMax
        xMaxLabel = binding.plotXMaxLabel
        yMin = binding.plotYMin
        yMinLabel = binding.plotYMinLabel
        yMax = binding.plotYMax
        yMaxLabel = binding.plotYMaxLabel
        yBounds = binding.yBounds

        setDimension(xMin, bounds.left)
        setDimension(xMax, bounds.right)
        setDimension(yMin, bounds.top)
        setDimension(yMax, bounds.bottom)
        xMin.addTextChangedListener(MyTextWatcher(xMinLabel, true))
        xMax.addTextChangedListener(MyTextWatcher(xMaxLabel, true))
        yMin.addTextChangedListener(MyTextWatcher(yMinLabel, false))
        yMax.addTextChangedListener(MyTextWatcher(yMaxLabel, false))
        if (d3) {
            yBounds.visibility = View.GONE
        }
        return binding.root
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> tryClose()
            else -> super.onClick(dialog, which)
        }
    }

    private fun setDimension(view: EditText, value: Float) {
        view.setOnEditorActionListener(this)
        view.setText(String.format(Locale.getDefault(), "%.2f", value))
    }

    private fun tryClose() {
        if (validate()) {
            applyData()
            dismiss()
        }
    }

    private fun validate(): Boolean {
        val bounds = collectData()
        return validXBounds(bounds) and validYBounds(bounds)
    }

    private fun validYBounds(bounds: RectF): Boolean {
        if (validNumbers(this.bounds.top, this.bounds.bottom, yMinLabel, yMaxLabel)) {
            return false
        }
        if (bounds.top >= bounds.bottom) {
            setError(yMinLabel, " ")
            setError(yMaxLabel, "max ≯ min")
            return false
        }
        clearError(yMinLabel)
        clearError(yMaxLabel)
        return true
    }

    private fun validXBounds(bounds: RectF): Boolean {
        if (validNumbers(bounds.left, bounds.right, xMinLabel, xMaxLabel)) {
            return false
        }
        if (bounds.left >= bounds.right) {
            setError(xMinLabel, " ")
            setError(xMaxLabel, "max ≯ min")
            return false
        }
        clearError(xMinLabel)
        clearError(xMaxLabel)
        return true
    }

    private fun validNumbers(
        l: Float,
        r: Float,
        lInput: TextInputLayout,
        rInput: TextInputLayout
    ): Boolean {
        val nanLeft = l.isNaN()
        val nanRight = r.isNaN()
        if (nanLeft || nanRight) {
            if (nanLeft) {
                setError(lInput, R.string.cpp_nan)
            } else {
                clearError(lInput)
            }
            if (nanRight) {
                setError(rInput, R.string.cpp_nan)
            } else {
                clearError(rInput)
            }
            return true
        }
        return false
    }

    private fun collectData(): RectF {
        return RectF(getDimension(xMin), getDimension(yMin), getDimension(xMax), getDimension(yMax))
    }

    private fun applyData() {
        val bounds = collectData()
        Plot.setGraphBounds(null, plotter, bounds, d3)
    }

    private fun getDimension(view: EditText): Float {
        return try {
            view.text.toString().replace(",", ".").replace("−", "-").toFloat()
        } catch (e: NumberFormatException) {
            Float.NaN
        }
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            tryClose()
            return true
        }
        return false
    }

    private inner class MyTextWatcher(
        private val input: TextInputLayout,
        private val x: Boolean
    ) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            if (!TextUtils.isEmpty(input.error)) {
                return
            }

            val bounds = collectData()
            if (x) {
                validXBounds(bounds)
            } else {
                validYBounds(bounds)
            }
        }
    }

    companion object {
        private const val ARG_BOUNDS = "arg-bounds"
        private const val ARG_3D = "arg-3d"

        fun show(bounds: RectF, d3: Boolean, fm: FragmentManager) {
            App.showDialog(create(bounds, d3), "plot-dimensions", fm)
        }

        private fun create(bounds: RectF, d3: Boolean): PlotDimensionsFragment {
            val dialog = PlotDimensionsFragment()
            val args = Bundle()
            args.putParcelable(ARG_BOUNDS, bounds)
            args.putBoolean(ARG_3D, d3)
            dialog.arguments = args
            return dialog
        }
    }
}
