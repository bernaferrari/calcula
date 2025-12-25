package org.solovyev.android.calculator.variables

import android.content.Context
import android.os.Bundle
import android.view.View
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.FragmentTab
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.view.Tabs

open class VariablesActivity : BaseActivity(R.string.cpp_vars_and_constants) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val variable = intent.extras?.getParcelable<CppVariable>(EXTRA_VARIABLE)
            variable?.let { EditVariableFragment.showDialog(it, this) }
        }

        withFab(R.drawable.ic_add_white_36dp) { _ ->
            EditVariableFragment.showDialog(this)
        }
    }

    override fun populateTabs(tabs: Tabs) {
        super.populateTabs(tabs)
        VariableCategory.values().forEach { category ->
            tabs.addTab(category, FragmentTab.variables)
        }
        tabs.setDefaultSelectedTab(VariableCategory.values().indexOf(VariableCategory.system))
    }

    class Dialog : VariablesActivity()

    companion object {
        const val EXTRA_VARIABLE = "variable"

        @JvmStatic
        fun getClass(context: Context): Class<out VariablesActivity> =
            if (App.isTablet(context)) Dialog::class.java else VariablesActivity::class.java
    }
}
