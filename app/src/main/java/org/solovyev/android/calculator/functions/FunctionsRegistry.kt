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

package org.solovyev.android.calculator.functions

import android.text.TextUtils
import jscl.JsclMathEngine
import jscl.math.function.CustomFunction
import jscl.math.function.Function
import jscl.math.function.IFunction
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import org.solovyev.android.Check
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.entities.BaseEntitiesRegistry
import org.solovyev.android.calculator.entities.Category
import org.solovyev.android.calculator.entities.Entities
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.calculator.json.Jsonable
import org.solovyev.android.io.FileSaver
import org.solovyev.common.text.Strings
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FunctionsRegistry @Inject constructor(
    mathEngine: JsclMathEngine
) : BaseEntitiesRegistry<Function>(mathEngine.getFunctionsRegistry()) {

    init {
        addDescription("sin", R.string.c_fun_description_sin)
        addDescription("cos", R.string.c_fun_description_cos)
        addDescription("tan", R.string.c_fun_description_tan)
        addDescription("cot", R.string.c_fun_description_cot)
        addDescription("asin", R.string.c_fun_description_asin)
        addDescription("acos", R.string.c_fun_description_acos)
        addDescription("atan", R.string.c_fun_description_atan)
        addDescription("acot", R.string.c_fun_description_acot)
        addDescription("ln", R.string.c_fun_description_ln)
        addDescription("lg", R.string.c_fun_description_lg)
        addDescription("log", R.string.c_fun_description_log)
        addDescription("exp", R.string.c_fun_description_exp)
        addDescription("√", R.string.c_fun_description_sqrt)
        addDescription("sqrt", R.string.c_fun_description_sqrt)
        addDescription("cubic", R.string.c_fun_description_cubic)
        addDescription("abs", R.string.c_fun_description_abs)
        addDescription("sgn", R.string.c_fun_description_sgn)
        addDescription("eq", R.string.c_fun_description_eq)
        addDescription("le", R.string.c_fun_description_le)
        addDescription("ge", R.string.c_fun_description_ge)
        addDescription("ne", R.string.c_fun_description_ne)
        addDescription("lt", R.string.c_fun_description_lt)
        addDescription("gt", R.string.c_fun_description_gt)
        addDescription("rad", R.string.c_fun_description_rad)
        addDescription("dms", R.string.c_fun_description_dms)
        addDescription("deg", R.string.c_fun_description_deg)
    }

    fun addOrUpdate(newFunction: Function, oldFunction: Function?) {
        val function = addOrUpdate(newFunction)
        if (oldFunction == null) {
            bus.post(AddedEvent(function))
        } else {
            bus.post(ChangedEvent(oldFunction, function))
        }
    }

    override fun onInit() {
        Check.isNotMainThread()
        migrateOldFunctions()

        val functions = mutableListOf<CustomFunction.Builder>()
        functions.add(
            CustomFunction.Builder(true, "log", listOf("base", "x"), "ln(x)/ln(base)")
        )
        functions.add(
            CustomFunction.Builder(true, "√3", listOf("x"), "x^(1/3)")
        )
        functions.add(
            CustomFunction.Builder(true, "√4", listOf("x"), "x^(1/4)")
        )
        functions.add(
            CustomFunction.Builder(true, "√n", listOf("x", "n"), "x^(1/n)")
        )
        functions.add(
            CustomFunction.Builder(true, "re", listOf("x"), "(x+conjugate(x))/2")
        )
        functions.add(
            CustomFunction.Builder(true, "im", listOf("x"), "(x-conjugate(x))/(2*i)")
        )

        for (function in loadEntities(CppFunction.JSON_CREATOR)) {
            functions.add(function.toJsclBuilder())
        }
        addSafely(functions)
    }

    /**
     * As some functions might depend on not-yet-loaded functions we need to try to add all functions first and then
     * re-run again if there are functions left. This process should continue until we can't add more functions
     * @param functions functions to add
     */
    private fun addSafely(functions: MutableList<CustomFunction.Builder>) {
        val exceptions = mutableListOf<Exception>()
        while (functions.isNotEmpty()) {
            val sizeBefore = functions.size
            // prepare exceptions list for new round
            exceptions.clear()
            addSafely(functions, exceptions)
            val sizeAfter = functions.size
            if (sizeBefore == sizeAfter) {
                break
            }
        }

        if (functions.isNotEmpty()) {
            // report exceptions
            for (exception in exceptions) {
                errorReporter.onException(exception)
            }
        }
    }

    private fun addSafely(
        functions: MutableList<CustomFunction.Builder>,
        exceptions: MutableList<Exception>
    ) {
        val iterator = functions.iterator()
        while (iterator.hasNext()) {
            val function = iterator.next()
            try {
                addSafely(function.create())
                iterator.remove()
            } catch (e: Exception) {
                exceptions.add(e)
            }
        }
    }

    override fun remove(entity: Function) {
        super.remove(entity)
        bus.post(RemovedEvent(entity))
    }

    override fun toJsonable(entity: Function): Jsonable? {
        if (entity is IFunction) {
            return CppFunction.builder(entity).build()
        }
        return null
    }

    private fun migrateOldFunctions() {
        val xml = preferences.getString(OldFunctions.PREFS_KEY, null)
        if (TextUtils.isEmpty(xml)) {
            return
        }
        try {
            val serializer: Serializer = Persister()
            val oldFunctions = serializer.read(OldFunctions::class.java, xml)
            if (oldFunctions != null) {
                val functions = OldFunctions.toCppFunctions(oldFunctions)
                getEntitiesFile().writeText(Json.toJson(functions).toString())
            }
            preferences.edit().remove(OldFunctions.PREFS_KEY).apply()
        } catch (e: Exception) {
            errorReporter.onException(e)
        }
    }

    override fun getEntitiesFile(): File {
        return directories.getFile("functions.json")
    }

    override fun getCategory(entity: Function): Category<Function> {
        return Entities.getCategory(entity, FunctionCategory.values()) ?: FunctionCategory.common
    }

    override fun getDescription(name: String): String? {
        val function = get(name)

        var description: String? = null
        if (function is CustomFunction) {
            description = function.getDescription()
        }

        if (!Strings.isEmpty(description)) {
            return description
        }
        return super.getDescription(name)
    }

    data class RemovedEvent(val function: Function)

    data class AddedEvent(val function: Function)

    data class ChangedEvent(
        val oldFunction: Function,
        val newFunction: Function
    )
}
