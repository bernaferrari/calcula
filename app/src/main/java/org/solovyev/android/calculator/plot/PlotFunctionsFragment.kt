package org.solovyev.android.calculator.plot

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.BaseDialogFragment
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.databinding.FragmentFunctionsFunctionBinding
import org.solovyev.android.plotter.BasePlotterListener
import org.solovyev.android.plotter.PlotFunction
import org.solovyev.android.plotter.PlotIconView
import org.solovyev.android.plotter.Plotter
import javax.inject.Inject

@AndroidEntryPoint
class PlotFunctionsFragment : BaseDialogFragment() {

    @Inject
    lateinit var plotter: Plotter

    private val plotterListener = PlotterListener()
    private lateinit var adapter: Adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        plotter.addListener(plotterListener)
        return view
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialogView(
        context: Context,
        inflater: LayoutInflater,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_plot_functions, null) as RecyclerView

        view.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        adapter = Adapter(plotter.plotData.functions)
        view.adapter = adapter
        return view
    }

    override fun onDestroyView() {
        plotter.removeListener(plotterListener)
        super.onDestroyView()
    }

    override fun onPrepareDialog(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.cpp_close, null)
        builder.setNeutralButton(R.string.cpp_add, null)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_NEUTRAL -> {
                PlotEditFunctionFragment.show(null, requireActivity().supportFragmentManager)
            }
            else -> super.onClick(dialog, which)
        }
    }

    inner class ViewHolder(binding: FragmentFunctionsFunctionBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnCreateContextMenuListener,
        MenuItem.OnMenuItemClickListener {

        val icon: PlotIconView = binding.functionIcon
        val name: TextView = binding.functionName
        private var function: PlotFunction? = null

        init {
            BaseActivity.fixFonts(itemView, typeface)
            itemView.setOnClickListener(this)
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(function: PlotFunction) {
            this.function = function
            name.text = function.function.name
            icon.setMeshSpec(function.meshSpec)
        }

        override fun onClick(v: View) {
            PlotEditFunctionFragment.show(function, requireActivity().supportFragmentManager)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu.add(ContextMenu.NONE, R.string.cpp_delete, ContextMenu.NONE, R.string.cpp_delete)
                .setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            if (function != null && item.itemId == R.string.cpp_delete) {
                plotter.remove(function)
                return true
            }
            return false
        }
    }

    private inner class Adapter(private val list: MutableList<PlotFunction>) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ViewHolder(FragmentFunctionsFunctionBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size

        fun remove(function: PlotFunction) {
            val i = list.indexOf(function)
            if (i >= 0) {
                list.removeAt(i)
                notifyItemRemoved(i)
            }
        }

        fun update(id: Int, function: PlotFunction) {
            val i = find(id)
            if (i >= 0) {
                list[i] = function
                notifyItemChanged(i)
            }
        }

        private fun find(id: Int): Int {
            for (i in list.indices) {
                if (list[i].function.id == id) {
                    return i
                }
            }
            return -1
        }

        fun add(function: PlotFunction) {
            list.add(function)
            notifyItemInserted(list.size - 1)
        }
    }

    private inner class PlotterListener : BasePlotterListener() {
        override fun onFunctionAdded(function: PlotFunction) {
            adapter.add(function)
        }

        override fun onFunctionUpdated(id: Int, function: PlotFunction) {
            adapter.update(id, function)
        }

        override fun onFunctionRemoved(function: PlotFunction) {
            adapter.remove(function)
        }
    }

    companion object {
        fun show(fm: FragmentManager) {
            App.showDialog(PlotFunctionsFragment(), "plot-functions", fm)
        }
    }
}
