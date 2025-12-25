/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.operators

import androidx.annotation.StringRes
import jscl.math.operator.*
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.entities.Category

sealed class OperatorCategory(@StringRes override val title: Int, private val order: Int) : Category<Operator> {

    object Common : OperatorCategory(R.string.c_fun_category_common, 0) {
        override fun isInCategory(operator: Operator): Boolean {
            return values().none { it != this && it.isInCategory(operator) }
        }
    }

    object Derivatives : OperatorCategory(R.string.derivatives, 1) {
        override fun isInCategory(operator: Operator): Boolean {
            return operator is Derivative || operator is Integral || operator is IndefiniteIntegral
        }
    }

    object Other : OperatorCategory(R.string.other, 2) {
        override fun isInCategory(operator: Operator): Boolean {
            return operator is Sum || operator is Product
        }
    }

    override fun getCategoryOrdinal(): Int = order

    override fun getCategoryName(): String = when (this) {
        Common -> "Common"
        Derivatives -> "Derivatives"
        Other -> "Other"
    }

    companion object {
        fun values(): Array<OperatorCategory> = arrayOf(Common, Derivatives, Other)
    }
}
