package org.solovyev.android.calculator.variables

import androidx.annotation.StringRes
import jscl.math.function.IConstant
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.entities.Category

enum class VariableCategory(@StringRes override val title: Int) : Category<IConstant> {
    my(R.string.c_var_my) {
        override fun isInCategory(variable: IConstant): Boolean = !variable.isSystem()
    },

    system(R.string.c_var_system) {
        override fun isInCategory(variable: IConstant): Boolean = variable.isSystem()
    };

    override fun getCategoryOrdinal(): Int = ordinal
    override fun getCategoryName(): String = name
}
