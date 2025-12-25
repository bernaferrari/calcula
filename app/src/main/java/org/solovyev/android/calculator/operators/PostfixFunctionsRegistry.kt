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

import jscl.JsclMathEngine
import jscl.math.operator.Operator
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.entities.BaseEntitiesRegistry
import org.solovyev.android.calculator.entities.Category
import org.solovyev.android.calculator.entities.Entities
import org.solovyev.android.calculator.json.Jsonable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostfixFunctionsRegistry @Inject constructor(
    mathEngine: JsclMathEngine
) : BaseEntitiesRegistry<Operator>(mathEngine.getPostfixFunctionsRegistry()) {

    init {
        addDescription("%", R.string.c_pf_description_percent)
        addDescription("!", R.string.c_pf_description_factorial)
        addDescription("!!", R.string.c_pf_description_double_factorial)
        addDescription("°", R.string.c_pf_description_degree)
    }

    override fun toJsonable(entity: Operator): Jsonable? = null

    override fun getEntitiesFile(): File? = null

    override fun getCategory(entity: Operator): Category<*>? {
        return Entities.getCategory(entity, OperatorCategory.values())
    }
}
