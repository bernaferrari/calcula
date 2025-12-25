package org.solovyev.android.calculator.json

import org.json.JSONObject

interface Jsonable {
    fun toJson(): JSONObject
}
