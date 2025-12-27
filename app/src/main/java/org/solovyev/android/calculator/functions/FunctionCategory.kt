package org.solovyev.android.calculator.functions

import androidx.annotation.StringRes
import jscl.math.function.ArcTrigonometric
import jscl.math.function.Comparison
import jscl.math.function.Function
import jscl.math.function.Trigonometric
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.entities.Category

enum class FunctionCategory(@StringRes override val title: Int) : Category<Function> {
    my(R.string.c_fun_category_my) {
        override fun isInCategory(entity: Function): Boolean = !entity.isSystem()
    },

    common(R.string.c_fun_category_common) {
        override fun isInCategory(entity: Function): Boolean {
            return values().none { category ->
                category != this && category.isInCategory(entity)
            }
        }
    },

    trigonometric(R.string.c_fun_category_trig) {
        override fun isInCategory(entity: Function): Boolean {
            return (entity is Trigonometric || entity is ArcTrigonometric) &&
                    !hyperbolic_trigonometric.isInCategory(entity)
        }
    },

    comparison(R.string.c_fun_category_comparison) {
        override fun isInCategory(entity: Function): Boolean = entity is Comparison
    },

    hyperbolic_trigonometric(R.string.c_fun_category_hyper_trig) {
        private val names = setOf("sinh", "cosh", "tanh", "coth", "asinh", "acosh", "atanh", "acoth")

        override fun isInCategory(entity: Function): Boolean = names.contains(entity.name)
    };

    override fun getCategoryOrdinal(): Int = ordinal
    override fun getCategoryName(): String = name
}
