package org.solovyev.android.calculator.functions

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Functions")
class OldFunctions {
    @field:ElementList(type = OldFunction::class, name = "functions")
    var list: List<OldFunction> = emptyList()

    companion object {
        const val PREFS_KEY = "org.solovyev.android.calculator.CalculatorModel_functions"

        @JvmStatic
        fun toCppFunctions(oldFunctions: OldFunctions): List<CppFunction> =
            oldFunctions.list
                .filter { it.name.isNotEmpty() && it.content.isNotEmpty() }
                .map { oldFunction ->
                    CppFunction.builder(oldFunction.name, oldFunction.content)
                        .withParameters(oldFunction.parameterNames)
                        .withDescription(oldFunction.description)
                        .build()
                }
    }
}
