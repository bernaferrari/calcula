package org.solovyev.android.calculator.json

import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.solovyev.android.io.FileSystem
import java.io.File
import java.io.IOException

object Json {
    private const val TAG = "Json"

    fun interface Creator<T> {
        fun create(json: JSONObject): T
    }

    fun <T> fromJson(array: JSONArray, creator: Creator<T>): List<T> {
        return buildList {
            for (i in 0 until array.length()) {
                val json = array.optJSONObject(i) ?: continue
                try {
                    add(creator.create(json))
                } catch (e: JSONException) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    fun toJson(items: List<Jsonable>): JSONArray {
        return JSONArray().apply {
            items.forEachIndexed { index, item ->
                try {
                    put(index, item.toJson())
                } catch (e: JSONException) {
                    Log.e(TAG, e.message, e)
                }
            }
        }
    }

    @Throws(IOException::class, JSONException::class)
    fun <T> load(file: File, fileSystem: FileSystem, creator: Creator<T>): List<T> {
        if (!file.exists()) {
            return emptyList()
        }
        val json = runBlocking { fileSystem.read(file) }
        if (TextUtils.isEmpty(json)) {
            return emptyList()
        }
        return fromJson(JSONArray(json.toString()), creator)
    }
}

// Extension functions for more idiomatic Kotlin usage
fun JSONArray.toList(): List<JSONObject> {
    return buildList {
        for (i in 0 until this@toList.length()) {
            optJSONObject(i)?.let { add(it) }
        }
    }
}

fun <T> JSONArray.mapNotNull(creator: Json.Creator<T>): List<T> {
    return Json.fromJson(this, creator)
}

fun List<Jsonable>.toJsonArray(): JSONArray {
    return Json.toJson(this)
}
