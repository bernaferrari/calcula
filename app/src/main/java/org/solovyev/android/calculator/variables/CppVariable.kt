package org.solovyev.android.calculator.variables

import android.os.Parcelable
import jscl.math.function.IConstant
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject
import org.solovyev.android.Check
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.calculator.json.Jsonable

@Parcelize
data class CppVariable(
    var id: Int = NO_ID,
    val name: String,
    val value: String = "",
    val description: String = "",
    val system: Boolean = false
) : Jsonable, Parcelable {

    @Throws(JSONException::class)
    override fun toJson(): JSONObject = JSONObject().apply {
        put(JSON_NAME, name)
        if (value.isNotEmpty()) {
            put(JSON_VALUE, value)
        }
        if (description.isNotEmpty()) {
            put(JSON_DESCRIPTION, description)
        }
    }

    fun toJsclConstant(): IConstant = JsclConstant(this)

    override fun toString(): String =
        if (id == NO_ID) "$name=$value"
        else "$name[#$id]=$value"

    class Builder private constructor(private var variable: CppVariable) {
        private var built = false

        constructor(name: String) : this(CppVariable(name = name))
        constructor(constant: IConstant) : this(CppVariable(
            id = if (constant.isIdDefined()) constant.getId() else NO_ID,
            name = constant.name,
            value = constant.getValue().orEmpty(),
            description = constant.getDescription().orEmpty(),
            system = constant.isSystem()
        ))

        fun withDescription(description: String) = apply {
            Check.isTrue(!built)
            variable = variable.copy(description = description)
        }

        fun withValue(value: String) = apply {
            Check.isTrue(!built)
            variable = variable.copy(value = value)
        }

        fun withValue(value: Double) = withValue(value.toString())

        fun withSystem(system: Boolean) = apply {
            Check.isTrue(!built)
            variable = variable.copy(system = system)
        }

        fun withId(id: Int) = apply {
            Check.isTrue(!built)
            variable.id = id
        }

        fun build(): CppVariable {
            built = true
            return variable
        }
    }

    companion object {
        const val NO_ID = CppFunction.NO_ID
        private const val JSON_NAME = "n"
        private const val JSON_VALUE = "v"
        private const val JSON_DESCRIPTION = "d"

        val JSON_CREATOR = object : Json.Creator<CppVariable> {
            override fun create(json: JSONObject): CppVariable = CppVariable(
                name = json.getString(JSON_NAME),
                value = json.optString(JSON_VALUE, ""),
                description = json.optString(JSON_DESCRIPTION, "")
            )
        }

        @JvmStatic
        fun builder(name: String) = Builder(name)

        @JvmStatic
        fun builder(name: String, value: Double) = Builder(name).withValue(value)

        @JvmStatic
        fun builder(constant: IConstant) = Builder(constant)
    }
}
