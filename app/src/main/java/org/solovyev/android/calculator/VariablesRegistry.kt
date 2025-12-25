package org.solovyev.android.calculator

import com.google.common.base.Strings
import jscl.JsclMathEngine
import jscl.math.function.IConstant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import org.simpleframework.xml.core.Persister
import org.solovyev.android.Check
import org.solovyev.android.calculator.entities.BaseEntitiesRegistry
import org.solovyev.android.calculator.entities.Category
import org.solovyev.android.calculator.entities.Entities
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.calculator.json.Jsonable
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.android.calculator.variables.OldVars
import org.solovyev.android.calculator.variables.VariableCategory
import org.solovyev.android.io.FileSaver
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VariablesRegistry @Inject constructor(
    mathEngine: JsclMathEngine
) : BaseEntitiesRegistry<IConstant>(mathEngine.getConstantsRegistry()) {

    init {
        addDescription("Π", R.string.c_var_description_PI)
        addDescription("π", R.string.c_var_description_pi)
        addDescription("e", R.string.c_var_description_e)
        addDescription("i", R.string.c_var_description_i)
        addDescription("c", R.string.c_var_description_c)
        addDescription("G", R.string.c_var_description_G)
        addDescription("h", R.string.c_var_description_h_reduced)
        addDescription("∞", R.string.c_var_description_inf)
        addDescription("inf", R.string.c_var_description_inf)
        addDescription("nan", R.string.c_var_description_nan)
        addDescription("NaN", R.string.c_var_description_nan)
    }

    private val _addedEvents = MutableSharedFlow<AddedEvent>()
    val addedEvents: SharedFlow<AddedEvent> = _addedEvents.asSharedFlow()

    private val _changedEvents = MutableSharedFlow<ChangedEvent>()
    val changedEvents: SharedFlow<ChangedEvent> = _changedEvents.asSharedFlow()

    private val _removedEvents = MutableSharedFlow<RemovedEvent>()
    val removedEvents: SharedFlow<RemovedEvent> = _removedEvents.asSharedFlow()

    fun addOrUpdate(newVariable: IConstant, oldVariable: IConstant?) {
        val variable = addOrUpdate(newVariable)
        if (oldVariable == null) {
            bus.post(AddedEvent(variable))
        } else {
            bus.post(ChangedEvent(oldVariable, variable))
        }
    }

    override fun remove(variable: IConstant) {
        super.remove(variable)
        bus.post(RemovedEvent(variable))
    }

    override fun onInit() {
        Check.isNotMainThread()

        migrateOldVariables()

        for (variable in loadEntities(CppVariable.JSON_CREATOR)) {
            addSafely(variable.toJsclConstant())
        }

        addSafely("x")
        addSafely("y")
        addSafely("t")
        addSafely("j")
    }

    private fun migrateOldVariables() {
        val xml = preferences.getString(OldVars.PREFS_KEY, null)
        if (Strings.isNullOrEmpty(xml)) {
            return
        }
        try {
            val serializer = Persister()
            val oldVariables = serializer.read(OldVars::class.java, xml)
            if (oldVariables != null) {
                val variables = OldVars.toCppVariables(oldVariables)
                runBlocking {
                    FileSaver.save(getEntitiesFile()!!, Json.toJson(variables).toString())
                }
            }
            preferences.edit().remove(OldVars.PREFS_KEY).apply()
        } catch (e: Exception) {
            errorReporter.onException(e)
        }
    }

    override fun getEntitiesFile(): File {
        return directories.getFile("variables.json")
    }

    override fun toJsonable(constant: IConstant): Jsonable? {
        return CppVariable.builder(constant).build()
    }

    private fun addSafely(name: String) {
        if (!contains(name)) {
            addSafely(CppVariable.builder(name).build().toJsclConstant())
        }
    }

    override fun getDescription(name: String): String? {
        val variable = get(name)
        return if (variable != null && !variable.isSystem()) {
            variable.getDescription()
        } else {
            super.getDescription(name)
        }
    }

    override fun getCategory(variable: IConstant): Category<IConstant> {
        return Entities.getCategory(variable, VariableCategory.values())!!
    }

    data class AddedEvent(val variable: IConstant)

    data class ChangedEvent(
        val oldVariable: IConstant,
        val newVariable: IConstant
    )

    data class RemovedEvent(val variable: IConstant)
}
