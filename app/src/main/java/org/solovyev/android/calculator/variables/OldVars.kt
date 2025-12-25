package org.solovyev.android.calculator.variables

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "vars")
class OldVars {
    @field:ElementList(type = OldVar::class, name = "vars")
    var list: List<OldVar> = emptyList()

    companion object {
        const val PREFS_KEY = "org.solovyev.android.calculator.CalculatorModel_vars"

        @JvmStatic
        fun toCppVariables(oldVariables: OldVars): List<CppVariable> =
            oldVariables.list
                .filter { it.name.isNotEmpty() }
                .map { oldVar ->
                    CppVariable.builder(oldVar.name)
                        .withValue(oldVar.value.orEmpty())
                        .withDescription(oldVar.description.orEmpty())
                        .build()
                }
    }
}
