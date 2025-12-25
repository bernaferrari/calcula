package org.solovyev.android.calculator.functions

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import jscl.math.function.Function
import jscl.math.function.IFunction
import org.solovyev.android.Check
import org.solovyev.android.calculator.AppComponent
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.RemovalConfirmationDialog
import org.solovyev.android.calculator.entities.BaseEntitiesFragment
import org.solovyev.android.calculator.entities.Category
import javax.inject.Inject

class FunctionsFragment : BaseEntitiesFragment<Function>() {

    @Inject lateinit var registry: FunctionsRegistry
    @Inject lateinit var calculator: Calculator
    @Inject lateinit var bus: Bus

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        bus.register(this)
        return view
    }

    override fun onDestroyView() {
        bus.unregister(this)
        super.onDestroyView()
    }

    override fun getEntities(): List<Function> = registry.getEntities()

    override fun getCategory(function: Function): Category<*> = registry.getCategory(function)

    override fun onCreateContextMenu(
        menu: android.view.ContextMenu,
        function: Function,
        listener: MenuItem.OnMenuItemClickListener
    ) {
        addMenu(menu, R.string.c_use, listener)
        if (!function.isSystem()) {
            addMenu(menu, R.string.cpp_edit, listener)
            addMenu(menu, R.string.cpp_delete, listener)
        }
    }

    override fun onMenuItemClicked(item: MenuItem, function: Function): Boolean {
        val activity: FragmentActivity = activity ?: return false
        return when (item.itemId) {
            R.string.c_use -> {
                onClick(function)
                true
            }
            R.string.cpp_edit -> {
                if (function is IFunction) {
                    EditFunctionFragment.show(
                        CppFunction.builder(function).build(),
                        activity.supportFragmentManager
                    )
                }
                true
            }
            R.string.cpp_delete -> {
                RemovalConfirmationDialog.showForFunction(activity, function.name) { dialog, which ->
                    Check.isTrue(which == DialogInterface.BUTTON_POSITIVE)
                    registry.remove(function)
                }
                true
            }
            else -> false
        }
    }

    @Subscribe
    fun onFunctionAdded(event: FunctionsRegistry.AddedEvent) {
        onEntityAdded(event.function)
    }

    @Subscribe
    fun onFunctionChanged(event: FunctionsRegistry.ChangedEvent) {
        onEntityChanged(event.newFunction)
    }

    @Subscribe
    fun onFunctionRemoved(event: FunctionsRegistry.RemovedEvent) {
        onEntityRemoved(event.function)
    }

    override fun getDescription(function: Function): String? =
        registry.getDescription(function.name)

    override fun getName(function: Function): String = function.toString()
}
