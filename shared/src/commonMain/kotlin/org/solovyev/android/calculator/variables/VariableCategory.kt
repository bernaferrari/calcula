package org.solovyev.android.calculator.variables

import org.jetbrains.compose.resources.StringResource
import jscl.math.function.IConstant
import org.solovyev.android.calculator.ui.Res
import org.solovyev.android.calculator.ui.c_var_my
import org.solovyev.android.calculator.ui.c_var_system
import org.solovyev.android.calculator.entities.Category

enum class VariableCategory(override val title: StringResource) : Category<IConstant> {
    my(Res.string.c_var_my) {
        override fun isInCategory(entity: IConstant): Boolean = !entity.isSystem()
    },

    system(Res.string.c_var_system) {
        override fun isInCategory(entity: IConstant): Boolean = entity.isSystem()
    };

    override fun getCategoryOrdinal(): Int = ordinal
    override fun getCategoryName(): String = name
}
