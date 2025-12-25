package org.solovyev.android.calculator.functions

import android.content.Context
import android.os.Bundle
import android.view.View
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.FragmentTab
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.operators.OperatorCategory
import org.solovyev.android.calculator.view.Tabs

open class FunctionsActivity : BaseActivity(R.string.c_functions) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val function = intent.extras?.getParcelable<CppFunction>(EXTRA_FUNCTION)
            function?.let { EditFunctionFragment.show(it, this) }
        }

        withFab(R.drawable.ic_add_white_36dp) { _ ->
            EditFunctionFragment.show(this)
        }
    }

    override fun populateTabs(tabs: Tabs) {
        super.populateTabs(tabs)

        FunctionCategory.values().forEach { category ->
            tabs.addTab(category, FragmentTab.functions)
        }
        tabs.setDefaultSelectedTab(
            FunctionCategory.values().indexOf(FunctionCategory.trigonometric)
        )

        OperatorCategory.values().forEach { category ->
            val title = if (category == OperatorCategory.Common || category == OperatorCategory.Other) {
                "${getString(R.string.c_operators)}: ${getString(category.title)}"
            } else {
                getString(category.title)
            }
            tabs.addTab(category, FragmentTab.operators, title)
        }
    }

    class Dialog : FunctionsActivity()

    companion object {
        const val EXTRA_FUNCTION = "function"

        @JvmStatic
        fun getClass(context: Context): Class<out FunctionsActivity> =
            if (App.isTablet(context)) Dialog::class.java else FunctionsActivity::class.java
    }
}
