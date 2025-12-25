package org.solovyev.android.calculator.functions

import android.os.Parcelable
import jscl.math.function.CustomFunction
import jscl.math.function.IFunction
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.solovyev.android.Check
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.calculator.json.Jsonable
import org.solovyev.common.text.Strings

@Parcelize
data class CppFunction(
    var id: Int = NO_ID,
    val name: String,
    val body: String,
    val parameters: List<String> = emptyList(),
    val description: String = ""
) : Jsonable, Parcelable {

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        Check.isNotEmpty(name)
        Check.isNotEmpty(body)

        return JSONObject().apply {
            if (parameters.isNotEmpty()) {
                val array = JSONArray()
                parameters.filterNot { it.isEmpty() }.forEachIndexed { index, param ->
                    array.put(index, param)
                }
                put(JSON_PARAMETERS, array)
            }
            put(JSON_NAME, name)
            put(JSON_BODY, body)
            if (description.isNotEmpty()) {
                put(JSON_DESCRIPTION, description)
            }
        }
    }

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
                description = Strings.getNotEmpty(that.getDescription(), "")
            )
        }

        fun withDescription(description: String) = apply {
            Check.isTrue(!built)
            function = function.copy(description = description)
        }

        fun withParameters(parameters: Collection<String>) = apply {
            Check.isTrue(!built)
            function = function.copy(parameters = function.parameters + parameters)
        }

        fun withParameters(vararg parameters: String) = apply {
            Check.isTrue(!built)
            function = function.copy(parameters = function.parameters + parameters)
        }

        fun withParameter(parameter: String) = apply {
            Check.isTrue(!built)
            function = function.copy(parameters = function.parameters + parameter)
        }

        fun withId(id: Int) = apply {
            Check.isTrue(!built)
            function.id = id
        }

        fun build(): CppFunction {
            built = true
            return function
        }
    }

    companion object {
        const val NO_ID = -1
        private const val JSON_NAME = "n"
        private const val JSON_BODY = "b"
        private const val JSON_PARAMETERS = "ps"
        private const val JSON_DESCRIPTION = "d"

        val JSON_CREATOR = object : Json.Creator<CppFunction> {
            override fun create(json: JSONObject): CppFunction {
                val parameters = mutableListOf<String>()
                json.optJSONArray(JSON_PARAMETERS)?.let { array ->
                    for (i in 0 until array.length()) {
                        val parameter = array.getString(i)
                        if (parameter.isNotEmpty()) {
                            parameters.add(parameter)
                        }
                    }
                }
                return CppFunction(
                    name = json.getString(JSON_NAME),
                    body = json.getString(JSON_BODY),
                    parameters = parameters,
                    description = json.optString(JSON_DESCRIPTION, "")
                )
            }
        }

        @JvmStatic
        fun builder(name: String, body: String) = Builder(name, body)

        @JvmStatic
        fun builder(function: CppFunction) = Builder(function)

        @JvmStatic
        fun builder(function: IFunction) = Builder(function)
    }
}
