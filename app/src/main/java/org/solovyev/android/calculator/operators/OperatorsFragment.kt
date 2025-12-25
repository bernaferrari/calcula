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

import android.view.ContextMenu
import android.view.MenuItem
import jscl.math.operator.Operator
import org.solovyev.android.calculator.AppComponent
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.entities.BaseEntitiesFragment
import org.solovyev.android.calculator.entities.Category
import org.solovyev.common.text.Strings
import javax.inject.Inject

class OperatorsFragment : BaseEntitiesFragment<Operator>() {

    @Inject
    lateinit var operatorsRegistry: OperatorsRegistry

    @Inject
    lateinit var postfixFunctionsRegistry: PostfixFunctionsRegistry

    override fun getEntities(): List<Operator> {
        return operatorsRegistry.getEntities() + postfixFunctionsRegistry.getEntities()
    }

    override fun getCategory(operator: Operator): Category<*>? {
        return operatorsRegistry.getCategory(operator) 
            ?: postfixFunctionsRegistry.getCategory(operator)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        operator: Operator,
        listener: MenuItem.OnMenuItemClickListener
    ) {
        addMenu(menu, R.string.c_use, listener)
    }

    override fun onMenuItemClicked(item: MenuItem, operator: Operator): Boolean {
        if (item.itemId == R.string.c_use) {
            onClick(operator)
            return true
        }
        return false
    }

    override fun getDescription(operator: Operator): String? {
        val name = operator.name
        val result = operatorsRegistry.getDescription(name)
        if (!Strings.isEmpty(result)) {
            return result
        }
        return postfixFunctionsRegistry.getDescription(name)
    }

    override fun getName(operator: Operator): String {
        return operator.toString()
    }
}
