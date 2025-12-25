package org.solovyev.android.calculator.plot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.BaseFragment
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.databinding.FragmentPlotBinding
import org.solovyev.android.plotter.PlotViewFrame
import org.solovyev.android.plotter.Plotter
import javax.inject.Inject

@AndroidEntryPoint
class PlotActivity : BaseActivity(R.layout.activity_empty, R.string.c_plot) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fm: FragmentManager = supportFragmentManager
            val t = fm.beginTransaction()
            t.add(R.id.main, MyFragment(), "plotter")
            t.commit()
        }
    }

    class MyFragment : BaseFragment(R.layout.fragment_plot), PlotViewFrame.Listener {

        @Inject
        lateinit var plotter: Plotter

        private lateinit var plotView: PlotViewFrame

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = super.onCreateView(inflater, container, savedInstanceState)!!
            val binding = FragmentPlotBinding.bind(view)
            plotView = binding.plotViewFrame

            val pd = plotter.plotData
            pd.axisStyle.backgroundColor = ContextCompat.getColor(requireActivity(), R.color.cpp_bg)
            plotter.setAxisStyle(pd.axisStyle)
            plotView.addControlView(R.id.plot_add_function)
            plotView.addControlView(R.id.plot_functions)
            plotView.addControlView(R.id.plot_dimensions)
            plotView.setPlotter(plotter)
            plotView.setListener(this)

            return view
        }

        override fun onPause() {
            plotView.onPause()
            super.onPause()
        }

        override fun onResume() {
            super.onResume()
            plotView.onResume()
        }

        override fun onButtonPressed(id: Int): Boolean {
            return when (id) {
                R.id.plot_dimensions -> {
                    val dimensions = plotter.dimensions
                    PlotDimensionsFragment.show(
                        dimensions.graph.makeBounds(),
                        plotter.is3d,
                        requireActivity().supportFragmentManager
                    )
                    true
                }
                R.id.plot_functions -> {
                    PlotFunctionsFragment.show(requireActivity().supportFragmentManager)
                    true
                }
                R.id.plot_add_function -> {
                    PlotEditFunctionFragment.show(null, requireActivity().supportFragmentManager)
                    true
                }
                else -> false
            }
        }

        override fun unableToZoom(`in`: Boolean) {
            Toast.makeText(requireActivity(), "Can't zoom anymore", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val POINTS_COUNT = 100
    }
}
