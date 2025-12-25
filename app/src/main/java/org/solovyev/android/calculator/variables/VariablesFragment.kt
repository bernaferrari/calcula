package org.solovyev.android.calculator.variables

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import jscl.math.function.IConstant
import org.solovyev.android.Check
import org.solovyev.android.calculator.*
import org.solovyev.android.calculator.entities.BaseEntitiesFragment
import org.solovyev.android.calculator.entities.Category
import org.solovyev.android.calculator.math.MathType
import javax.inject.Inject

class VariablesFragment : BaseEntitiesFragment<IConstant>() {

    @Inject
    lateinit var registry: VariablesRegistry
    
    @Inject
    lateinit var calculator: Calculator
    
    @Inject
    lateinit var bus: Bus

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

    override fun getEntities(): List<IConstant> {
        val entities = registry.getEntities().toMutableList()
        entities.removeAll { constant ->
            constant.name == MathType.INFINITY_JSCL || constant.name == MathType.NAN
        }
        return entities
    }

    override fun getCategory(variable: IConstant): Category<*> = registry.getCategory(variable)

    override fun onCreateContextMenu(
        menu: android.view.ContextMenu,
        variable: IConstant,
        listener: MenuItem.OnMenuItemClickListener
    ) {
        addMenu(menu, R.string.c_use, listener)
        if (!variable.isSystem()) {
            addMenu(menu, R.string.cpp_edit, listener)
            addMenu(menu, R.string.cpp_delete, listener)
        }
        if (variable.getValue()?.isNotEmpty() == true) {
            addMenu(menu, R.string.cpp_copy, listener)
        }
    }

    override fun onMenuItemClicked(item: MenuItem, variable: IConstant): Boolean {
        val activity: FragmentActivity = activity ?: return false
        return when (item.itemId) {
            R.string.c_use -> {
                onClick(variable)
                true
            }
            R.string.cpp_edit -> {
                EditVariableFragment.showDialog(CppVariable.builder(variable).build(), activity)
                true
            }
            R.string.cpp_delete -> {
                RemovalConfirmationDialog.showForVariable(activity, variable.name) { dialog, which ->
                    Check.isTrue(which == DialogInterface.BUTTON_POSITIVE)
                    registry.remove(variable)
                }
                true
            }
            R.string.cpp_copy -> {
                copyText(variable.getValue())
                true
            }
            else -> false
        }
    }

    @Subscribe
    fun onVariableRemoved(e: VariablesRegistry.RemovedEvent) {
        onEntityRemoved(e.variable)
    }

    @Subscribe
    fun onVariableAdded(e: VariablesRegistry.AddedEvent) {
        onEntityAdded(e.variable)
    }

    @Subscribe
    fun onVariableChanged(e: VariablesRegistry.ChangedEvent) {
        onEntityChanged(e.newVariable)
    }

    override fun getDescription(variable: IConstant): String? =
        registry.getDescription(variable.name)

    override fun getName(variable: IConstant): String {
        val name = variable.name
        if (!variable.isDefined()) {
            return name
        }
        val value = variable.getValue()
        return if (value.isNullOrEmpty()) name else "$name = $value"
    }
}
