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
        override fun isInCategory(function: Function): Boolean = !function.isSystem()
    },

    common(R.string.c_fun_category_common) {
        override fun isInCategory(function: Function): Boolean {
            return values().none { category ->
                category != this && category.isInCategory(function)
            }
        }
    },

    trigonometric(R.string.c_fun_category_trig) {
        override fun isInCategory(function: Function): Boolean {
            return (function is Trigonometric || function is ArcTrigonometric) &&
                    !hyperbolic_trigonometric.isInCategory(function)
        }
    },

    comparison(R.string.c_fun_category_comparison) {
        override fun isInCategory(function: Function): Boolean = function is Comparison
    },

    hyperbolic_trigonometric(R.string.c_fun_category_hyper_trig) {
        private val names = setOf("sinh", "cosh", "tanh", "coth", "asinh", "acosh", "atanh", "acoth")

        override fun isInCategory(function: Function): Boolean = names.contains(function.name)
    };

    override fun getCategoryOrdinal(): Int = ordinal
    override fun getCategoryName(): String = name
}
