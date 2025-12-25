package org.solovyev.android.calculator.plot

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.RemovalConfirmationDialog
import org.solovyev.android.calculator.Utils
import org.solovyev.android.calculator.databinding.FragmentPlotFunctionEditBinding
import org.solovyev.android.calculator.functions.BaseFunctionFragment
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.plotter.Color
import org.solovyev.android.plotter.PlotFunction
import org.solovyev.android.plotter.PlotIconView
import org.solovyev.android.plotter.Plotter
import org.solovyev.android.plotter.meshes.MeshSpec
import jscl.math.function.CustomFunction
import uz.shift.colorpicker.LineColorPicker
import uz.shift.colorpicker.OnColorChangedListener
import javax.inject.Inject

@AndroidEntryPoint
class PlotEditFunctionFragment : BaseFunctionFragment(R.layout.fragment_plot_function_edit),
    SeekBar.OnSeekBarChangeListener {

    @Inject
    lateinit var plotter: Plotter

    private lateinit var meshSpecViews: View
    private lateinit var colorLabel: TextView
    private lateinit var colorPicker: LineColorPicker
    private lateinit var lineWidthLabel: TextView
    private lateinit var lineWidthSeekBar: SeekBar
    private lateinit var iconView: PlotIconView
    private var plotFunction: PlotFunction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (function != null) {
            plotFunction = plotter.plotData.get(function!!.id)
            if (plotFunction == null) {
                dismiss()
            }
        }
    }

    override fun onCreateDialogView(
        context: Context,
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateDialogView(context, inflater, savedInstanceState)
        val binding = FragmentPlotFunctionEditBinding.bind(view)
        meshSpecViews = binding.fnMeshspecViews
        colorLabel = binding.fnColorLabel
        colorPicker = binding.fnColorPicker
        lineWidthLabel = binding.fnLinewidthLabel
        lineWidthSeekBar = binding.fnLinewidthSeekbar
        iconView = binding.fnIconview

        colorPicker.setOnColorChangedListener(object : OnColorChangedListener {
            override fun onColorChanged(c: Int) {
                iconView.setMeshSpec(applyMeshSpec())
            }
        })
        lineWidthSeekBar.max = MeshSpec.MAX_WIDTH - MeshSpec.MIN_WIDTH
        lineWidthSeekBar.setOnSeekBarChangeListener(this)

        val colors = MeshSpec.LightColors.asIntArray()
        colorPicker.colors = colors
        paramsView.setMaxParams(2)
        // no descriptions for functions in plotter
        descriptionLabel.visibility = View.GONE
        if (savedInstanceState == null) {
            if (plotFunction != null) {
                setupViews(plotFunction!!.meshSpec)
            } else {
                setupViews()
            }
        }
        return view
    }

    private fun setupViews(meshSpec: MeshSpec) {
        val color = meshSpec.color.toInt()
        val colors = colorPicker.colors
        val i = indexOf(colors, color)
        colorPicker.setSelectedColorPosition(maxOf(0, i))
        lineWidthSeekBar.progress = meshSpec.width - MeshSpec.MIN_WIDTH
        iconView.setMeshSpec(meshSpec)
    }

    private fun setupViews() {
        colorPicker.setSelectedColorPosition(0)
        lineWidthSeekBar.progress = MeshSpec.defaultWidth(requireActivity()) - MeshSpec.MIN_WIDTH
        iconView.setMeshSpec(applyMeshSpec())
    }

    private fun applyMeshSpec(): MeshSpec {
        val color = Color.create(colorPicker.getColor())
        val width = MeshSpec.MIN_WIDTH + lineWidthSeekBar.progress
        val meshSpec = MeshSpec.create(color, width)
        meshSpec.pointsCount = PlotActivity.POINTS_COUNT
        return meshSpec
    }

    override fun applyData(function: CppFunction): Boolean {
        return try {
            val expressionFunction = ExpressionFunction(function.toJsclBuilder().create())
            val plotFunction = PlotFunction.create(expressionFunction, applyMeshSpec())
            val id = function.id
            if (id != CppFunction.NO_ID) {
                plotter.update(id, plotFunction)
            } else {
                plotter.add(plotFunction)
            }
            true
        } catch (e: RuntimeException) {
            setError(bodyLabel, Utils.getErrorMessage(e))
            false
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        iconView.setMeshSpec(applyMeshSpec())
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    override fun showRemovalDialog(function: CppFunction) {
        Check.isNotNull(plotFunction)
        val functionName = plotFunction!!.function.name
        Check.isNotNull(functionName)
        RemovalConfirmationDialog.showForFunction(
            requireActivity(),
            functionName
        ) { dialog, which ->
            Check.isTrue(which == DialogInterface.BUTTON_POSITIVE)
            plotter.remove(plotFunction)
            dismiss()
        }
    }

    companion object {
        private const val ARG_FUNCTION = "function"

        fun show(function: PlotFunction?, fm: FragmentManager) {
            App.showDialog(create(function), "plot-function-editor", fm)
        }

        fun create(pf: PlotFunction?): PlotEditFunctionFragment {
            val fragment = PlotEditFunctionFragment()
            if (pf != null && pf.function is ExpressionFunction) {
                val args = Bundle()
                val ef = pf.function as ExpressionFunction
                val customFunction = ef.function as CustomFunction
                val parameters = ArrayList(customFunction.getParameterNames())
                args.putParcelable(
                    ARG_FUNCTION,
                    CppFunction.builder(ef.function.name, customFunction.getContent())
                        .withParameters(parameters)
                        .withId(pf.function.id)
                        .build()
                )
                fragment.arguments = args
            }
            return fragment
        }

        private fun indexOf(integers: IntArray, integer: Int): Int {
            for (i in integers.indices) {
                if (integers[i] == integer) {
                    return i
                }
            }
            return -1
        }
    }
}
