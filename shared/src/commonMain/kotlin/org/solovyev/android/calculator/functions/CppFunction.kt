package org.solovyev.android.calculator.functions

import jscl.math.function.CustomFunction
import jscl.math.function.IFunction
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmStatic

@Serializable
data class CppFunction(
    var id: Int = NO_ID,
    val name: String,
    val body: String,
    val parameters: List<String> = emptyList(),
    val description: String = ""
) {

    fun toJsclBuilder(): CustomFunction.Builder =
        CustomFunction.Builder(name, parameters, body).apply {
            setDescription(description)
            if (id != NO_ID) {
                setId(id)
            }
        }

    override fun toString(): String =
        if (id == NO_ID) "$name$parameters{$body}"
        else "$name[#$id]$parameters{$body}"

    class Builder {
        private var function: CppFunction
        private var built = false

        constructor(name: String, body: String) {
            function = CppFunction(name = name, body = body)
        }

        constructor(that: CppFunction) {
            function = that.copy()
        }

        constructor(that: IFunction) {
            function = CppFunction(
                id = if (that.isIdDefined()) that.getId() else NO_ID,
                name = that.name,
                body = that.getContent(),
                parameters = that.getParameterNames(),
                description = that.getDescription() ?: ""
            )
        }

        fun withDescription(description: String) = apply {
            check(!built)
            function = function.copy(description = description)
        }

        fun withParameters(parameters: Collection<String>) = apply {
            check(!built)
            function = function.copy(parameters = function.parameters + parameters)
        }

        fun withParameters(vararg parameters: String) = apply {
            check(!built)
            function = function.copy(parameters = function.parameters + parameters)
        }

        fun withParameter(parameter: String) = apply {
            check(!built)
            function = function.copy(parameters = function.parameters + parameter)
        }

        fun withId(id: Int) = apply {
            check(!built)
            function.id = id
        }

        fun build(): CppFunction {
            built = true
            return function
        }
    }

    companion object {
        const val NO_ID = -1

        
        fun builder(name: String, body: String) = Builder(name, body)

        
        fun builder(function: CppFunction) = Builder(function)

        
        fun builder(function: IFunction) = Builder(function)
    }
}
